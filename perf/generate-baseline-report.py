#!/usr/bin/env python3
"""Generate docs/PERFORMANCE_BASELINE.md from audit JSON (+ optional k6 summaries)."""
from __future__ import annotations

import argparse
import json
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOC = ROOT / "docs" / "PERFORMANCE_BASELINE.md"
BASELINE = ROOT / "perf" / "k6" / "baseline.json"

SLA = {"S": 200, "A": 500, "B": 2000, "C": 30000}


def load_json(path: Path):
    if path and path.exists():
        return json.loads(path.read_text())
    return None


def latest_k6_metric(results_dir: Path, pattern: str, metric_suffix: str):
    files = sorted(results_dir.glob(pattern), reverse=True)
    for f in files:
        data = json.loads(f.read_text())
        metrics = data.get("metrics", {})
        for key, value in metrics.items():
            if metric_suffix in key and "p(95)" in value.get("values", {}):
                return value["values"]["p(95)"]
    return None


def render_env_section(env: str, audit: dict | None, k6_dir: Path) -> list[str]:
    lines = [f"### {env.capitalize()}", ""]
    if not audit:
        lines.append("_Sem dados de audit para este ambiente._")
        lines.append("")
        return lines

    summary = audit.get("summary", {})
    lines.append(f"- **Base URL:** `{audit.get('base', '—')}`")
    lines.append(f"- **Audit em:** {audit.get('generated_at', '—')}")
    lines.append(f"- **Autenticado:** {'sim' if audit.get('authenticated') else 'não'}")
    warmup = audit.get("warmup_health_ms") or []
    if warmup:
        lines.append(f"- **Warm-up /health (ms):** {warmup}")
    lines.append(f"- **Tier S p95 agregado:** {summary.get('tier_s_p95_ms', '—')} ms")
    lines.append(f"- **Tier S warm p95:** {summary.get('tier_s_warm_p95_ms', '—')} ms")
    lines.append(f"- **Fluxo dashboard (soma p95):** {summary.get('dashboard_flow_sum_p95_ms', '—')} ms")
    lines.append(f"- **Fluxo dashboard warm:** {summary.get('dashboard_flow_sum_warm_ms', '—')} ms")
    lines.append(f"- **Falhas críticas:** {summary.get('critical_failures', 0)}")
    lines.append("")

    smoke_p95 = latest_k6_metric(k6_dir, "smoke-*.json", "tier:S")
    dash_p95 = latest_k6_metric(k6_dir, "tier-s-full-dashboard-*.json", "dashboard_flow_duration")
    if smoke_p95 is not None:
        lines.append(f"- **k6 smoke Tier S p95:** {smoke_p95:.0f} ms")
    if dash_p95 is not None:
        lines.append(f"- **k6 dashboard flow p95:** {dash_p95:.0f} ms")
    lines.append("")

    lines.extend(
        [
            "| Endpoint | Tier | cold (ms) | warm (ms) | p95 (ms) | SLA | Status |",
            "|----------|------|-----------|-----------|----------|-----|--------|",
        ]
    )
    for row in sorted(audit.get("results", []), key=lambda r: (r.get("tier", ""), r.get("path", ""))):
        tier = row.get("tier", "?")
        sla = SLA.get(tier, "—")
        p95 = row.get("p95_ms", 0)
        status = row.get("grade", "")
        if row.get("status", 0) >= 500:
            sla_status = "FAIL"
        elif isinstance(sla, int) and p95 > sla and row.get("status") == 200:
            sla_status = "SLOW"
        elif "FALHA" in status:
            sla_status = "FAIL"
        else:
            sla_status = "OK"
        lines.append(
            f"| `{row.get('method')} {row.get('path')}` | {tier} | {row.get('cold_ms', '—')} | "
            f"{row.get('warm_avg_ms', '—')} | {p95} | {sla} | {sla_status} |"
        )
    lines.append("")
    return lines


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--env", default="prod")
    parser.add_argument("--audit", required=True)
    parser.add_argument("--merge", action="store_true", help="Merge into existing baseline doc")
    args = parser.parse_args()

    audit_path = Path(args.audit)
    audit = load_json(audit_path)
    if audit:
        audit["generated_at"] = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")

    k6_dir = ROOT / "perf" / "k6" / "results"
    baseline = load_json(BASELINE) or {}

    sections: dict[str, list[str]] = {}
    if args.merge and DOC.exists():
        # preserve other env sections naively by re-running only current env block
        pass

    sections[args.env] = render_env_section(args.env, audit, k6_dir)

    lines = [
        "# Performance Baseline — Nutri+ API",
        "",
        f"Gerado em {datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M UTC')}.",
        "",
        "Comparativo de latência por ambiente. Gates k6 em `perf/k6/baseline.json`.",
        "",
        "## Gates de referência",
        "",
        f"- Tier S p95 por endpoint: **{baseline.get('thresholds', {}).get('tier_s_p95_ms', 200)} ms**",
        f"- Fluxo dashboard agregado: **{baseline.get('thresholds', {}).get('dashboard_flow_p95_ms', 800)} ms**",
        f"- Regressão máxima: **×{baseline.get('thresholds', {}).get('regression_factor', 1.2)}**",
        "",
        "## Ambientes",
        "",
    ]

    for env in ("local", "homolog", "prod"):
        if env in sections:
            lines.extend(sections[env])
        elif env == args.env:
            lines.extend(sections[args.env])
        else:
            env_files = sorted((ROOT / "perf" / "results").glob(f"audit-{env}-*.json"), reverse=True)
            if env_files:
                lines.extend(render_env_section(env, load_json(env_files[0]), k6_dir))
            else:
                lines.extend([f"### {env.capitalize()}", "", "_Rodar `./perf/run-baseline.sh {env}`._", ""])

    lines.extend(
        [
            "## Como reproduzir",
            "",
            "```bash",
            "# Prod / homolog (read-only)",
            "./perf/run-baseline.sh prod",
            "",
            "# Local (API + MySQL)",
            "BASE_URL=http://localhost:8080 ./perf/run-baseline.sh local",
            "```",
            "",
        ]
    )

    DOC.write_text("\n".join(lines))
    print(f"Wrote {DOC}")


if __name__ == "__main__":
    main()
