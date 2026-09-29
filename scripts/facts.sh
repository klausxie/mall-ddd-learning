#!/usr/bin/env bash
#
# 一条命令看清仓库现状：两端体量 / 封顶占比 / 覆盖率 / 真库置备级别 / 验证通道耗时。
#
# 用途：AI agent（或人）不用跑四五条零散命令去凑这些数——那是纯浪费的来回。
# 只读，不联网，不构建；唯一的例外是 --timings（它会真的跑一遍各条通道）。
#
# 用法：
#   scripts/facts.sh               # 现状快照（< 1 秒）
#   scripts/facts.sh --timings     # 额外实测各条验证通道
#
# 两个"封顶"口径与 ARCHITECTURE.md §五 保持一致，改动那里的定义请同步这里。
# 封顶**按端分别算**：分母一混，这条规则就失去意义。
set -euo pipefail
cd "$(dirname "$0")/.."

count_lines() { local total=0 f; for f in "$@"; do [ -f "$f" ] && total=$((total + $(wc -l < "$f"))); done; echo "$total"; }
java_lines() { local total=0 f; for f in $(find "$1" -name '*.java' 2>/dev/null); do total=$((total + $(wc -l < "$f"))); done; echo "$total"; }
java_files() { find "$1" -name '*.java' 2>/dev/null | wc -l | xargs; }
# $1 起始目录；$2 可选，追加给 find 的过滤（如 -name '*.test.ts'）
ts_files() { find "$1" -type f \( -name '*.ts' -o -name '*.tsx' \) ${2:-} 2>/dev/null | grep -v node_modules || true; }
ts_lines() { local total=0 f; for f in $(ts_files "$1" "${2:-}"); do total=$((total + $(wc -l < "$f"))); done; echo "$total"; }
pct() { [ "$2" -eq 0 ] && { echo 0; return; }; echo $(( $1 * 100 / $2 )); }
verdict() { [ "$1" -le "$2" ] && echo "达标" || echo "**超限**"; }
backend_base_pkg() { dirname "$(find backend/src/main/java -name 'Application.java' -print -quit)" | sed 's|^backend/src/main/java/||' | tr '/' '.'; }

echo "== 目录 =="
printf '  backend/    Maven：Java 后端（%s 文件 / %s 行）\n' "$(java_files backend/src/main)" "$(java_lines backend/src/main)"
if [ -d frontend/src ]; then
    printf '  frontend/   pnpm：React + Vite + TS（%s 文件 / %s 行）\n' \
        "$(ts_files frontend/src | wc -l | xargs)" "$(ts_lines frontend/src)"
fi
printf '  文档        %s 行（根 5 份 + 两端各一份 CLAUDE.md）\n' \
    "$(count_lines AGENTS.md CLAUDE.md ARCHITECTURE.md README.md TEMPLATE.md backend/CLAUDE.md frontend/CLAUDE.md)"

echo
echo "== 封顶（定义见 ARCHITECTURE.md §五）=="
be_main=$(java_lines backend/src/main)
be_guard=$(count_lines backend/src/test/java/cn/mklaus/app/GuardrailsTest.java \
    backend/src/test/java/cn/mklaus/app/EntityMappingTest.java .claude/hooks/guard-rules.sh \
    backend/src/test/resources/guardrails/bad-source.txt backend/src/test/resources/guardrails/clean-source.txt)
be_infra=$(count_lines backend/src/test/java/cn/mklaus/app/support/*.java)
printf '  后端 检查类护栏   %4s 行 = 后端主代码 %2s%%（上限 40%%）  %s\n' "$be_guard" "$(pct "$be_guard" "$be_main")" "$(verdict "$(pct "$be_guard" "$be_main")" 40)"
printf '  后端 测试基础设施 %4s 行 = 后端主代码 %2s%%（上限 15%%）  %s\n' "$be_infra" "$(pct "$be_infra" "$be_main")" "$(verdict "$(pct "$be_infra" "$be_main")" 15)"
if [ -d frontend/src ]; then
    fe_src=$(ts_lines frontend/src)
    fe_test=$(count_lines $(ts_files frontend/src "-name *.test.ts -o -name *.test.tsx"))
    fe_cfg=$(count_lines frontend/eslint.config.js frontend/eslint.config.mjs frontend/.prettierrc \
        frontend/.prettierrc.json frontend/tsconfig.json frontend/tsconfig.app.json frontend/tsconfig.node.json \
        frontend/vite.config.ts frontend/vitest.config.ts)
    fe_guard=$(count_lines $(find frontend -name '*guardrail*' -not -path '*/node_modules/*' 2>/dev/null))
    fe_src_only=$((fe_src - fe_test))
    printf '  前端 检查类护栏   %4s 行 = 前端源码 %2s%%（上限 40%%）  %s\n' "$((fe_cfg + fe_guard))" "$(pct "$((fe_cfg + fe_guard))" "$fe_src_only")" "$(verdict "$(pct "$((fe_cfg + fe_guard))" "$fe_src_only")" 40)"
    printf '  前端 测试         %4s 行（单列参考，不设上限）\n' "$fe_test"
fi

echo
echo "== 覆盖率（最近一次后端构建，backend/target/site/jacoco）=="
if [ -f backend/target/site/jacoco/jacoco.csv ]; then
    awk -F, 'NR>1 {m+=$8; c+=$9} END {
        if (c+m == 0) { print "  全局行覆盖      无数据"; exit }
        printf "  全局行覆盖      %d%%（%d/%d）\n", c*100/(c+m), c, c+m
    }' backend/target/site/jacoco/jacoco.csv
    awk -F, -v base="$(backend_base_pkg).domain" 'NR>1 && index($2, base) == 1 {m[$2]+=$8; c[$2]+=$9}
        END {
            for (pkg in m) {
                if (m[pkg]+c[pkg] == 0) continue
                pc = c[pkg]*100/(m[pkg]+c[pkg])
                if (n == 0 || pc < min) { min = pc; worst = pkg }
                n++
            }
            if (n > 0) printf "  domain 最低包   %d%%（%s，共 %d 个包，地板 70%%）\n", min, worst, n
            else print "  domain 无数据"
        }' backend/target/site/jacoco/jacoco.csv
else
    echo "  无报告（跑一次 make verify-backend 就会有）"
fi

echo
echo "== 真库置备级别（本地判定，近似 backend 的 RealDatabaseProvisioner）=="
tier=""
for key in APP_DB_URL APP_DB_USERNAME APP_DB_PASSWORD SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD; do
    if [ -n "${!key:-}" ]; then tier="① 显式配置（环境变量 $key）"; break; fi
done
if [ -z "$tier" ]; then
    for f in backend/src/main/resources/application-*.yaml; do
        case "$f" in *.example) continue ;; esac
        [ -f "$f" ] || continue
        if grep -qE "^[[:space:]]*password:[[:space:]]*['\"]?[^'\"<\$[:space:]]" "$f"; then
            tier="① 显式配置（$f）"
            break
        fi
    done
fi
if [ -z "$tier" ]; then
    if docker info >/dev/null 2>&1; then
        tier="② Docker 容器（mysql:8.0，服务端 $(docker version --format '{{.Server.Version}}' 2>/dev/null || echo '?'))"
    else
        tier="③ H2(MODE=MySQL) 快速通道"
    fi
fi
echo "  当前会走：$tier"

if [ "${1:-}" = "--timings" ]; then
    echo
    echo "== 验证通道实测 =="
    timed() {
        local label="$1" start=$SECONDS rc=0
        shift
        "$@" >/dev/null 2>&1 || rc=$?
        printf '  %-28s %3ds  %s\n' "$label" "$((SECONDS - start))" "$([ $rc -eq 0 ] && echo 通过 || echo "失败(rc=$rc)")"
    }
    timed "后端 静态三连" bash -c 'cd backend && ./mvnw -B -o -q spotless:apply checkstyle:check test-compile'
    timed "后端 单测（不含真库）" bash -c 'cd backend && ./mvnw -B -o test -Dtest="!*ApiTest,!MapperSmokeTest"'
    timed "前端 类型+lint" bash -c 'cd frontend && pnpm typecheck && pnpm lint'
fi

cat <<'EOF'

== 迭代建议 ==
  改后端一行    cd backend && ./mvnw -B -o -q spotless:apply checkstyle:check test-compile   # ~2 秒
  验后端某个类  cd backend && ./mvnw -B -o test -Dtest=AddressApiTest                      # ~5-11 秒
  改前端一行    cd frontend && pnpm typecheck && pnpm lint                                 # ~2 秒
  收尾必须      make verify                                                                # 两端全量
  更快的真库    TESTCONTAINERS_REUSE_ENABLE=true make verify-backend                       # 复用容器，省 ~5 秒/轮
EOF
