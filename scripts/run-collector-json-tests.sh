#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COLLECTOR_PID=""

cleanup() {
  local exit_code=$?

  if [[ -n "${COLLECTOR_PID}" ]] && kill -0 "${COLLECTOR_PID}" 2>/dev/null; then
    kill "${COLLECTOR_PID}" 2>/dev/null || true
    wait "${COLLECTOR_PID}" 2>/dev/null || true
  fi

  (cd "${ROOT_DIR}" && docker compose down >/dev/null 2>&1) || true

  exit "${exit_code}"
}

wait_for_collector() {
  local status=""

  for _ in {1..30}; do
    status="$(curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:8080 || true)"
    if [[ "${status}" != "000" ]]; then
      return 0
    fi
    sleep 1
  done

  echo "Collector did not become available on http://127.0.0.1:8080 within 30 seconds." >&2
  return 1
}

trap cleanup EXIT INT TERM

cd "${ROOT_DIR}"

mvn -q -pl telemetry/collector -am package -DskipTests
docker compose up -d

java -jar telemetry/collector/target/collector-1.0-SNAPSHOT.jar &
COLLECTOR_PID=$!

wait_for_collector

bash hub-router/scripts/macos_linux/1-collector-json-tests.sh
