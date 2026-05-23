#!/usr/bin/env bash
set -euo pipefail

skip_infra=false
skip_build=false
dry_run=false
commerce_port=8080

usage() {
  cat <<'EOF'
Usage: ./run-project.sh [options]

Options:
  --skip-infra              Do not start Docker Compose services.
  --skip-build              Do not install Maven modules before running services.
  --dry-run                 Print commands without changing files or starting services.
  --commerce-port <port>    HTTP port for commerce-app. Default: 8080.
  -h, --help                Show this help message.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-infra)
      skip_infra=true
      shift
      ;;
    --skip-build)
      skip_build=true
      shift
      ;;
    --dry-run)
      dry_run=true
      shift
      ;;
    --commerce-port)
      if [[ $# -lt 2 ]]; then
        echo "Missing value for --commerce-port" >&2
        exit 1
      fi
      commerce_port="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$script_dir"

maven_command="mvn"
if [[ -x "./mvnw" ]]; then
  maven_command="./mvnw"
fi

commerce_args=(-f commerce-app/pom.xml spring-boot:run)
payments_args=(-f payments-service/pom.xml spring-boot:run)

if [[ "$dry_run" == true ]]; then
  echo "Dry run only. No files, containers, builds, or services will be changed."
  echo "Maven command: ${maven_command}"
  if [[ "$skip_infra" == false ]]; then
    echo "Would run: docker compose up -d postgres rabbitmq"
  fi
  if [[ "$skip_build" == false ]]; then
    echo "Would run: ${maven_command} -DskipTests install"
  fi
  echo "Would set SERVER_PORT=${commerce_port} for commerce-app"
  echo "Would run commerce-app: ${maven_command} ${commerce_args[*]}"
  echo "Would run payments-service: ${maven_command} ${payments_args[*]}"
  exit 0
fi

if [[ ! -f ".env" && -f ".env.example" ]]; then
  cp .env.example .env
  echo "Created .env from .env.example. Review it if you need custom credentials."
fi

if [[ "$skip_infra" == false ]]; then
  echo "Starting local dependencies: postgres and rabbitmq..."
  docker compose up -d postgres rabbitmq
fi

if [[ "$skip_build" == false ]]; then
  echo "Installing Maven modules without tests so runnable modules can resolve local dependencies..."
  "$maven_command" -DskipTests install
fi

commerce_pid=""
payments_pid=""

stop_services() {
  echo
  echo "Stopping local service processes..."
  if [[ -n "$commerce_pid" ]]; then
    kill "$commerce_pid" 2>/dev/null || true
  fi
  if [[ -n "$payments_pid" ]]; then
    kill "$payments_pid" 2>/dev/null || true
  fi
}

trap stop_services EXIT INT TERM

echo "Starting commerce-app on http://localhost:${commerce_port} ..."
SERVER_PORT="$commerce_port" "$maven_command" "${commerce_args[@]}" &
commerce_pid=$!

echo "Starting payments-service worker..."
"$maven_command" "${payments_args[@]}" &
payments_pid=$!

echo "Project startup launched. Press Ctrl+C to stop both services."
echo "RabbitMQ management UI: http://localhost:15672"

while kill -0 "$commerce_pid" 2>/dev/null && kill -0 "$payments_pid" 2>/dev/null; do
  sleep 2
done
