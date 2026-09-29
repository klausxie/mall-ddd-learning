#!/bin/sh
#
# PreToolUse 钩子包装器：先确认 node 可用，再把 stdin 原样交给 guard-rules.mjs。
#
# 为什么需要这一层：guard-rules.mjs 靠 `#!/usr/bin/env node` 启动，而 macOS + nvm 这类环境
# 下 node 只写在**交互式** shell 的 PATH 里（~/.zshrc）。钩子跑在非交互 shell 里，`env node`
# 会失败并返回 127；在 Claude Code 里非 0/2 的退出码只是"非阻断错误"，
# 结果是护栏**静默失效**——看起来装了，其实一次都没拦。
#
# 所以这里显式探测 node（含 nvm / Homebrew 常见路径），全都找不到时 fail-closed：
# 宁可拒绝本次编辑并说明原因，也不静默放行。
set -u

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

find_node() {
    found=$(command -v node 2>/dev/null) && {
        printf '%s\n' "$found"
        return 0
    }
    for candidate in \
        /usr/local/bin/node \
        /opt/homebrew/bin/node \
        /usr/bin/node \
        "$HOME/.nvm/versions/node"/*/bin/node; do
        if [ -x "$candidate" ]; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done
    return 1
}

NODE=$(find_node) || NODE=''

if [ -z "$NODE" ]; then
    cat <<'JSON'
{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"deny","permissionDecisionReason":"护栏脚本无法运行：PATH 及常见路径里都找不到 node。为了不让『禁止改规则文件/摘检查插件』的护栏静默失效，本次编辑被拒绝。请安装 node 或把 node 加进 PATH（也可在 .claude/settings.json 里直接写 node 的绝对路径）。"}}
JSON
    exit 0
fi

exec "$NODE" "$SCRIPT_DIR/guard-rules.mjs"
