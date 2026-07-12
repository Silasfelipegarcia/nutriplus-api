#!/usr/bin/env bash
# Configura Maven, Colima (Docker) e variáveis para desenvolvimento Nutri+ no macOS.
set -euo pipefail

export PATH="/opt/homebrew/bin:$PATH"

echo "==> Instalando Maven, Colima e Docker CLI (se necessário)..."
brew install maven colima docker

if ! colima status >/dev/null 2>&1; then
  echo "==> Iniciando Colima..."
  colima start --cpu 2 --memory 4 --disk 20
else
  echo "==> Colima já está rodando."
fi

export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
mkdir -p "${HOME}/.docker/run"
ln -sf "${HOME}/.colima/default/docker.sock" "${HOME}/.docker/run/docker.sock" 2>/dev/null || true

echo "export PATH=\"/opt/homebrew/bin:\$PATH\"" >> "${HOME}/.zshrc"
echo "export DOCKER_HOST=\"unix://\${HOME}/.colima/default/docker.sock\"" >> "${HOME}/.zshrc"
echo "export JAVA_HOME=\$(/usr/libexec/java_home -v 21 2>/dev/null || true)" >> "${HOME}/.zshrc"

cat <<'EOF'

✅ Ferramentas instaladas.

Verifique:
  mvn -version
  docker ps
  colima status

Rodar testes funcionais:
  cd nutriplus-api && mvn test -Dtest='FunctionalTestUserScenarioIntegrationTest,FunctionalFlowIntegrationTest'

Ou na raiz do monorepo:
  ./run-functional-tests.sh

Opcional (melhora compatibilidade com algumas ferramentas):
  sudo ln -sf ~/.colima/default/docker.sock /var/run/docker.sock

EOF
