#!/usr/bin/env node
/**
 * PreToolUse 钩子：堵住"改规则让检查通过"这条捷径。
 *
 * 拦这类操作：改规则让检查通过。
 *   1. 编辑 backend/config/{checkstyle,eclipse-formatter}.xml，或前端的 eslint / prettier / tsconfig / vite 配置；
 *   2. 在 backend/pom.xml 里把 spotless / checkstyle / archunit 摘掉；
 *   3. 在 frontend/package.json 里把 lint / typecheck / test / verify / format:check 脚本摘掉。
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
    // 后端规则在 backend/config/ 下；前端是它自己那套检查配置。
    // 注意路径前缀：后端搬进 backend/ 之后，如果这里还写 ^config/，钩子会**静默失效**——
    // 看着还在，其实一次都没拦。
    const ruleFiles = [
        /(^|\/)backend\/config\/(checkstyle|eclipse-formatter)\.xml$/,
        /(^|\/)frontend\/(eslint\.config\.(js|mjs|cjs|ts)|\.prettierrc(\.[a-z]+)?|tsconfig(\.[a-z]+)?\.json|vite\.config\.ts|vitest\.config\.ts)$/,
    ];
    if (ruleFiles.some((pattern) => pattern.test(relPath))) {
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

    // 3) 保护前端 package.json 里的检查脚本（与 pom.xml 的保护同构）
    if (/(^|\/)frontend\/package\.json$/.test(relPath)) {
        const oldText = String(toolInput.old_string || '');
        const newText = String(
            toolInput.new_string !== undefined ? toolInput.new_string : toolInput.content || ''
        );
        for (const script of ['"lint"', '"typecheck"', '"test"', '"verify"', '"format:check"']) {
            if (oldText.includes(script) && !newText.includes(script)) {
                deny(`不允许从 frontend/package.json 里移除 ${script} 脚本：它是前端规范的一部分。`
                    + `如果确实需要移除，请先说明理由并征求确认。`);
                process.exit(0);
            }
        }
    }

    process.exit(0);
});
