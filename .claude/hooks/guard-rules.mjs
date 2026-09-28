#!/usr/bin/env node
/**
 * PreToolUse 钩子：堵住"改规则让检查通过"这条捷径。
 *
 * 拦两类操作：
 *   1. 编辑 config/checkstyle.xml 或 config/eclipse-formatter.xml —— 规则来源本身；
 *   2. 在 pom.xml 里把 spotless / checkstyle / archunit 摘掉。
 *
 * 协议：stdout 输出 JSON，exit 0。permissionDecision=deny 时 Claude 会看到 reason。
 */
let raw = '';
process.stdin.on('data', (chunk) => (raw += chunk));
process.stdin.on('end', () => {
    let input = {};
    try {
        input = JSON.parse(raw || '{}');
    } catch {
        process.exit(0);
    }

    const toolInput = input.tool_input || {};
    const projectDir = (process.env.CLAUDE_PROJECT_DIR || '').replace(/\\/g, '/');
    const absPath = String(toolInput.file_path || '').replace(/\\/g, '/');
    const relPath = projectDir && absPath.startsWith(projectDir + '/')
        ? absPath.slice(projectDir.length + 1)
        : absPath;

    const deny = (reason) => {
        process.stdout.write(JSON.stringify({
            hookSpecificOutput: {
                hookEventName: 'PreToolUse',
                permissionDecision: 'deny',
                permissionDecisionReason: reason,
            },
        }));
    };

    // 1) 保护规则文件
    if (/^config\/(checkstyle|eclipse-formatter)\.xml$/.test(relPath)) {
        deny(`${relPath} 是代码规范的规则来源，不允许为了让检查通过而修改它。`
            + `如果确实认为规则需要调整，请先说明理由并征求确认，再同步更新 CLAUDE.md。`);
        process.exit(0);
    }

    // 2) 保护 pom.xml 里的检查插件
    if (/(^|\/)pom\.xml$/.test(relPath)) {
        const oldText = String(toolInput.old_string || '');
        const newText = String(
            toolInput.new_string !== undefined ? toolInput.new_string : toolInput.content || ''
        );
        const protectedIds = ['spotless-maven-plugin', 'maven-checkstyle-plugin', 'archunit'];
        for (const id of protectedIds) {
            if (oldText.includes(id) && !newText.includes(id)) {
                deny(`不允许从 pom.xml 中移除 ${id}：它是项目规范的一部分。`
                    + `如果确实需要移除，请先说明理由并征求确认。`);
                process.exit(0);
            }
        }
    }

    process.exit(0);
});
