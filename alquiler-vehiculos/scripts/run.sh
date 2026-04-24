#!/usr/bin/env bash
# Ejecutor de comandos del proyecto (Git Bash / Linux / macOS).
# Uso:
#   ./scripts/run.sh list
#   ./scripts/run.sh eureka-up
# En Windows (Git Bash): delega en run.ps1 (no requiere Python).
# En Linux/macOS: usa python3 para leer registry.json.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REGISTRY="$SCRIPT_DIR/registry.json"

load_env_local() {
  local f="$SCRIPT_DIR/.env.local"
  [[ -f "$f" ]] || return 0
  set -a
  # shellcheck disable=SC1090
  source "$f"
  set +a
}

load_env_local

is_windows_shell() {
  case "$(uname -s 2>/dev/null)" in
    MINGW*|MSYS*|CYGWIN*) return 0 ;;
    *) return 1 ;;
  esac
}

delegate_powershell() {
  local ps1="$SCRIPT_DIR/run.ps1"
  if ! command -v powershell.exe >/dev/null 2>&1; then
    echo "No se encontró powershell.exe. Abre PowerShell y usa: .\\scripts\\run.ps1 -List" >&2
    exit 1
  fi
  if [[ "${1:-}" == "list" || "${1:-}" == "" || "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "$ps1" -List
    if [[ "${1:-}" == "" ]]; then
      echo
      echo "Uso: ./scripts/run.sh <id>   (ej.: ./scripts/run.sh eureka-up)"
    fi
    return
  fi
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File "$ps1" -Id "$1"
}

list_cmds_unix() {
  if command -v python3 >/dev/null 2>&1; then
    python3 - "$REGISTRY" <<'PY'
import json, sys
path = sys.argv[1]
with open(path, encoding="utf-8") as f:
    reg = json.load(f)
for c in reg.get("commands", []):
    print(f"  {c['id']:<18} {c.get('title','')}")
PY
    return
  fi
  if command -v python >/dev/null 2>&1; then
    python - "$REGISTRY" <<'PY'
import json, sys
path = sys.argv[1]
with open(path, encoding="utf-8") as f:
    reg = json.load(f)
for c in reg.get("commands", []):
    print(f"  {c['id']:<18} {c.get('title','')}")
PY
    return
  fi
  echo "Instala Python 3 o usa PowerShell: .\\scripts\\run.ps1 -List" >&2
  exit 1
}

run_cmd_unix() {
  local id="$1"
  local py="python3"
  command -v python3 >/dev/null 2>&1 || py="python"
  command -v "$py" >/dev/null 2>&1 || {
    echo "Se necesita python3 (o python) para ejecutar '$id' desde este shell." >&2
    exit 1
  }
  "$py" - "$REGISTRY" "$SCRIPT_DIR" "$id" <<'PY'
import json, os, sys

registry_path, script_dir, cmd_id = sys.argv[1], sys.argv[2], sys.argv[3]
with open(registry_path, encoding="utf-8") as f:
    reg = json.load(f)
cmd = next((c for c in reg.get("commands", []) if c["id"] == cmd_id), None)
if not cmd:
    print(f"No existe el id '{cmd_id}' en registry.json. Usa: ./scripts/run.sh list", file=sys.stderr)
    sys.exit(1)
unix = cmd.get("unix")
if not unix:
    print(f"La entrada '{cmd_id}' no define bloque 'unix'.", file=sys.stderr)
    sys.exit(1)
cwd = os.path.normpath(os.path.join(script_dir, cmd.get("cwd", "..")))
prog = os.path.join(cwd, unix["program"])
args = [prog, *unix.get("args", [])]
print(f"→ {cmd.get('title','')}")
print(f"  cwd: {cwd}")
print(f"  {' '.join(args)}")
os.chdir(cwd)
os.execvp(args[0], args)
PY
}

if is_windows_shell; then
  delegate_powershell "${1:-}"
  exit $?
fi

case "${1:-}" in
  list|-h|--help)
    echo "Comandos disponibles (registry.json):"
    list_cmds_unix
    ;;
  "")
    echo "Comandos disponibles (registry.json):"
    list_cmds_unix
    echo
    echo "Uso: ./scripts/run.sh <id>   (ej.: ./scripts/run.sh eureka-up)"
    ;;
  *)
    run_cmd_unix "$1"
    ;;
esac
