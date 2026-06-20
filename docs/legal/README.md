# Publicar site e documentos legais (GitHub Pages)

1. No repositório `nutriplus-api`, vá em **Settings → Pages**.
2. **Source:** Deploy from a branch.
3. **Branch:** `main` (ou `master`) e pasta **`/docs/legal/site`**.
4. Salve. A URL será algo como `https://<org>.github.io/nutriplus-api/` (ou subpath conforme configuração).

## Arquivos

| URL | Arquivo | Conteúdo |
|-----|---------|----------|
| `/` | `index.html` | Landing B2C (Luna/Bruno + nutricionista) |
| `/privacidade.html` | `privacidade.html` | Política de Privacidade |
| `/termos.html` | `termos.html` | Termos de Uso |

Atualize `landingUrl`, `privacyPolicyUrl` e `termsUrl` em `nutriplus-frontend` (`lib/src/core/constants.dart`) após publicar.

## Sincronização

Os textos canônicos para a API estão em `src/main/resources/legal/`. Ao alterar, copie também para `docs/legal/` e regenere o HTML se necessário.
