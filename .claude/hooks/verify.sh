#!/usr/bin/env bash
#
# Stop 钩子：一轮对话结束前跑完整校验，失败则把错误回喂给 Claude 让它继续修。
#
# 为什么不在 PostToolUse 里逐文件跑：Java 每次都要起一个 Maven JVM（2-5 秒），
# 一次任务里编辑十几个文件就会变得很慢。所以格式化 + 检查统一放在这里做一次。
#
# 退出码约定：0 = 通过；2 = 阻断，stderr 会作为反馈交给 Claude。
set -uo pipefail

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0

[ -x ./mvnw ] || exit 0

# 1) 先自动格式化（幂等，独立于校验结果，失败也不阻断）
./mvnw -B -q spotless:apply >/dev/null 2>&1

# 2) 完整校验：spotless:check + checkstyle:check + ArchUnit 测试
if out=$(./mvnw -B verify 2>&1); then
    exit 0
fi

printf '%s\n' "$out" | grep -E '^\[ERROR\]' | head -40 >&2
{
    echo
    echo "校验未通过，请修改代码使其通过。"
    echo "禁止通过编辑 config/ 下的规则文件、从 pom.xml 移除插件、或加 -D...skip 参数来绕过检查。"
    echo "如果确实认为规则需要调整，先说明理由并征求确认。"
} >&2
exit 2
