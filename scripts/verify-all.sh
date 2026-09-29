#!/usr/bin/env bash
#
# 两端全量校验：后端 verify + 前端 verify。`make verify` 就是跑它。
#
# 为什么还要一个脚本、而不是让 make 直接连两条命令：这样"两段输出 + 失败即停 + 最后汇总"
# 只有一份实现，CI 与本地、人和 agent 看到的都是同一条路径。
set -euo pipefail
cd "$(dirname "$0")/.."

run() {
    local label="$1"
    shift
    echo
    echo "==================== $label ===================="
    "$@"
}

run "后端：格式化 + Checkstyle + ArchUnit + 测试（含真库）+ 覆盖率门槛" \
    bash -c 'cd backend && ./mvnw -B -Pcoverage-check verify'

if [ ! -d frontend/node_modules ]; then
    echo
    echo "==================== 前端：首次运行，先装依赖 ===================="
    (cd frontend && pnpm install --frozen-lockfile)
fi

run "前端：格式检查 + lint + 类型 + 测试 + 构建" \
    bash -c 'cd frontend && pnpm run verify'

echo
echo "==================== 两端全绿 ===================="
