#!/usr/bin/env bash
#
# 并行起后端与前端，Ctrl-C 一起收摊。
#
# 为什么需要它：前后端同仓之后，"开发时到底要开几个终端、命令分别是什么"是最容易忘的事。
# 一条 `make dev` 把答案固定下来，agent 也不用去猜。
set -euo pipefail
cd "$(dirname "$0")/.."

pids=()
cleanup() {
    for pid in "${pids[@]:-}"; do
        [ -n "$pid" ] && kill "$pid" 2>/dev/null || true
    done
    wait 2>/dev/null || true
}
trap cleanup EXIT INT TERM

echo "==> 后端 :8080（cd backend && ./mvnw spring-boot:run）"
(cd backend && ./mvnw -q spring-boot:run) &
pids+=("$!")

echo "==> 前端 :5173（cd frontend && pnpm dev），/api 代理到 :8080"
if [ ! -d frontend/node_modules ]; then
    echo "    首次运行，先装依赖：cd frontend && pnpm install"
    (cd frontend && pnpm install)
fi
(cd frontend && pnpm dev) &
pids+=("$!")

echo
echo "两端已启动。前端 http://127.0.0.1:5173，后端 http://127.0.0.1:8080。Ctrl-C 结束。"
wait
