#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# run-demo.sh
#
# Roda o PIT (Pitest) para os 3 cenários da POC e abre o relatório HTML
# resultante no navegador padrão, para facilitar demos ao vivo.
# ---------------------------------------------------------------------------
set -euo pipefail

cd "$(dirname "$0")"

echo "==> Rodando ./gradlew pitestAll ..."
./gradlew pitestAll

REPORT="build/reports/pitest/index.html"

if [ ! -f "$REPORT" ]; then
    echo "ERRO: relatório não encontrado em $REPORT" >&2
    exit 1
fi

echo "==> Abrindo relatório: $REPORT"

if command -v open >/dev/null 2>&1; then
    # macOS
    open "$REPORT"
elif command -v xdg-open >/dev/null 2>&1; then
    # Linux
    xdg-open "$REPORT"
elif command -v start >/dev/null 2>&1; then
    # Windows (Git Bash)
    start "$REPORT"
else
    echo "Não foi possível detectar um comando para abrir o navegador automaticamente."
    echo "Abra manualmente: file://$(pwd)/$REPORT"
fi
