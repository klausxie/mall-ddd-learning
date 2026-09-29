import { existsSync, readFileSync, readdirSync } from 'node:fs';
import { join } from 'node:path';
import { describe, expect, it } from 'vitest';

/**
 * 护栏：用**真实读取文件**的方式确认检查规则没被改弱。
 * 与后端 GuardrailsTest 同精神——"改规则让检查通过"必须过不了 pnpm verify。
 *
 * 路径用工作目录而不是 import.meta.url：jsdom 环境下后者不是 file: 协议；
 * pnpm 脚本（含 make verify）始终在 frontend/ 下执行。
 */
const FRONTEND = process.cwd();
const REPO = join(FRONTEND, '..');

const read = (...parts: string[]): string => readFileSync(join(FRONTEND, ...parts), 'utf8');
const readJson = (...parts: string[]): Record<string, unknown> => JSON.parse(read(...parts)) as Record<string, unknown>;

function listSourceFiles(relativeDir: string): string[] {
  const files: string[] = [];
  for (const entry of readdirSync(join(FRONTEND, relativeDir), { withFileTypes: true })) {
    const relative = `${relativeDir}/${entry.name}`;
    if (entry.isDirectory()) {
      files.push(...listSourceFiles(relative));
    } else if (/\.tsx?$/.test(entry.name)) {
      files.push(relative);
    }
  }
  return files;
}

/** 把一个 .gitignore 读成规范化后的条目列表（去掉注释、前后斜杠与 globstar 前缀）。 */
function ignorePatterns(absolutePath: string): string[] {
  if (!existsSync(absolutePath)) return [];
  return readFileSync(absolutePath, 'utf8')
    .split('\n')
    .map((line) =>
      line
        .trim()
        .replace(/^\*\*\//, '')
        .replace(/^\//, '')
        .replace(/\/$/, ''),
    )
    .filter((line) => line !== '' && !line.startsWith('#') && !line.startsWith('!'));
}

const SOURCE_FILES = listSourceFiles('src').filter((file) => !file.includes('__tests__'));

describe('护栏：检查没有被改弱', () => {
  it('tsconfig 仍然开着 strict 与各项严格检查', () => {
    const options = readJson('tsconfig.json')['compilerOptions'] as Record<string, unknown>;
    const mustBeOn = [
      'strict',
      'noUncheckedIndexedAccess',
      'exactOptionalPropertyTypes',
      'noImplicitOverride',
      'noImplicitReturns',
      'noFallthroughCasesInSwitch',
      'noUnusedLocals',
      'noUnusedParameters',
      'noPropertyAccessFromIndexSignature',
      'useUnknownInCatchVariables',
    ];
    for (const flag of mustBeOn) {
      expect(options[flag], `tsconfig.compilerOptions.${flag}`).toBe(true);
    }
    for (const flag of ['allowUnreachableCode', 'allowUnusedLabels']) {
      expect(options[flag], `tsconfig.compilerOptions.${flag}`).toBe(false);
    }
  });

  it('eslint 配置仍在跑 typescript-eslint / react-hooks / react-refresh / prettier', () => {
    const config = read('eslint.config.js');
    for (const needle of ['typescript-eslint', 'react-hooks', 'react-refresh', 'eslint-config-prettier']) {
      expect(config, needle).toContain(needle);
    }
  });

  it('package.json 的关键脚本没被删，也没加绕过', () => {
    const scripts = readJson('package.json')['scripts'] as Record<string, string>;
    for (const name of ['dev', 'build', 'preview', 'lint', 'format', 'format:check', 'typecheck', 'test', 'verify']) {
      expect(scripts[name], name).toBeTruthy();
    }
    expect(scripts['lint']).toContain('eslint');
    expect(scripts['typecheck']).toContain('tsc');
    expect(scripts['test']).toContain('vitest');
    expect(scripts['verify']).toContain('pnpm test --run');
    for (const [name, command] of Object.entries(scripts)) {
      for (const bypass of ['|| true', 'exit 0', '--passwithnotests', '--no-verify', '; true', 'skip']) {
        expect(command.toLowerCase(), `${name}: ${command}`).not.toContain(bypass);
      }
    }
  });

  it('.gitignore 仍然忽略 node_modules 与 dist（frontend 与仓库根任一份声明都算数）', () => {
    const patterns = [...ignorePatterns(join(FRONTEND, '.gitignore')), ...ignorePatterns(join(REPO, '.gitignore'))];
    for (const entry of ['node_modules', 'dist', 'coverage', '.vite']) {
      expect(patterns, entry).toContain(entry);
    }
  });

  it('vite 代理把 /api 转到 127.0.0.1:8080 并 rewrite 掉前缀', () => {
    const config = read('vite.config.ts');
    expect(config).toContain("'/api'");
    expect(config).toContain('http://127.0.0.1:8080');
    expect(config).toContain('rewrite');
    expect(config).toContain('^\\/api');
  });

  it('源码里没有 eslint-disable，也没有 any', () => {
    const banDisable = ['eslint', 'disable'].join('-');
    for (const file of SOURCE_FILES) {
      const content = read(file);
      expect(content, file).not.toContain(banDisable);
      expect(content, file).not.toMatch(/\bas\s+any\b|:\s*any\b/);
    }
  });

  it('字面量接口路径只允许出现在 src/api/paths.ts', () => {
    const literalPath = /['"`]\/[a-z][A-Za-z0-9]*\/[A-Za-z]/;
    const offenders = SOURCE_FILES.filter((file) => file.startsWith('src/api/') && file !== 'src/api/paths.ts').filter(
      (file) => literalPath.test(read(file)),
    );
    expect(offenders).toEqual([]);
  });
});
