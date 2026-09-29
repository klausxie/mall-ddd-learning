#!/usr/bin/env bash
#
# 一条命令看清项目现状：体量 / 护栏封顶 / 覆盖率 / 真库置备级别 / 验证通道耗时。
#
# 用途：AI agent（或人）不用再跑四五条零散命令去凑这些数——那是纯浪费的来回。
# 只读，不联网，不构建；唯一的例外是 --timings（它会真的跑一遍三条通道）。
#
# 用法：
#   scripts/facts.sh               # 现状快照（< 1 秒）
#   scripts/facts.sh --timings     # 额外实测三条验证通道各自多久（约 30 秒）
#
# 两个"封顶"口径与 ARCHITECTURE.md §五 保持一致，改动那里的定义请同步这里。
set -euo pipefail
cd "$(dirname "$0")/.."

java_lines() { local total=0 f; for f in $(find "$1" -name '*.java' 2>/dev/null); do total=$((total + $(wc -l < "$f"))); done; echo "$total"; }
java_files() { find "$1" -name '*.java' 2>/dev/null | wc -l | xargs; }
file_lines() { local total=0 f; for f in "$@"; do [ -f "$f" ] && total=$((total + $(wc -l < "$f"))); done; echo "$total"; }
base_pkg() {  # 从目录结构推导根包名，而不是硬编码——init.sh 改名后这里不用跟着改
    local app_file
    app_file=$(find src/main/java -name 'Application.java' -print -quit)
    dirname "$app_file" | sed 's|^src/main/java/||' | tr '/' '.'
}
biggest() { find src/main -name '*.java' -exec wc -l {} + | sort -rn | sed -n 2p | awk '{print $1" 行  "$2}' | sed 's|src/main/java/cn/mklaus/app/||'; }

main_lines=$(java_lines src/main)
test_lines=$(java_lines src/test)
docs_lines=$(file_lines AGENTS.md CLAUDE.md ARCHITECTURE.md README.md TEMPLATE.md)

guard_lines=$(file_lines src/test/java/cn/mklaus/app/GuardrailsTest.java \
    src/test/java/cn/mklaus/app/EntityMappingTest.java .claude/hooks/guard-rules.sh \
    src/test/resources/guardrails/bad-source.txt src/test/resources/guardrails/clean-source.txt)
infra_lines=$(file_lines src/test/java/cn/mklaus/app/support/*.java)

pct() { echo $(( $1 * 100 / $2 )); }
verdict() { [ "$1" -le "$2" ] && echo "达标" || echo "**超限**"; }

echo "== 体量 =="
printf '  主代码      %3s 文件 / %5s 行   最大 %s\n' "$(java_files src/main)" "$main_lines" "$(biggest)"
printf '  测试        %3s 文件 / %5s 行\n' "$(java_files src/test)" "$test_lines"
printf '  文档        %3s 行（AGENTS / CLAUDE / ARCHITECTURE / README / TEMPLATE）\n' "$docs_lines"

echo
echo "== 封顶（定义见 ARCHITECTURE.md §五）=="
printf '  检查类护栏      %4s 行 = 主代码 %2s%%（上限 40%%）  %s\n' "$guard_lines" "$(pct "$guard_lines" "$main_lines")" "$(verdict "$(pct "$guard_lines" "$main_lines")" 40)"
printf '  测试基础设施    %4s 行 = 主代码 %2s%%（上限 15%%）  %s\n' "$infra_lines" "$(pct "$infra_lines" "$main_lines")" "$(verdict "$(pct "$infra_lines" "$main_lines")" 15)"

echo
echo "== 覆盖率（最近一次构建，target/site/jacoco）=="
if [ -f target/site/jacoco/jacoco.csv ]; then
    awk -F, 'NR>1 {m+=$8; c+=$9} END {
        if (c+m == 0) { print "  domain 无数据"; exit }
        printf "  全局行覆盖      %d%%（%d/%d）\n", c*100/(c+m), c, c+m
    }' target/site/jacoco/jacoco.csv
    # 地板是"domain 每个包 ≥ 70%"，而 jacoco 不产出 per-package 的 CSV，所以从聚合 CSV 按 PACKAGE 分组
    awk -F, -v base="$(base_pkg).domain" 'NR>1 && index($2, base) == 1 {m[$2]+=$8; c[$2]+=$9}
        END {
            for (pkg in m) {
                if (m[pkg]+c[pkg] == 0) continue
                pc = c[pkg]*100/(m[pkg]+c[pkg])
                if (n == 0 || pc < min) { min = pc; worst = pkg }
                n++
            }
            if (n > 0) printf "  domain 最低包   %d%%（%s，共 %d 个包，地板 70%%）\n", min, worst, n
            else print "  domain 无数据"
        }' target/site/jacoco/jacoco.csv
else
    echo "  无报告（跑一次 ./mvnw verify 就会有）"
fi

echo
echo "== 真库置备级别（本地判定，近似 support/RealDatabaseProvisioner）=="
tier=""
for key in APP_DB_URL APP_DB_USERNAME APP_DB_PASSWORD SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD; do
    if [ -n "${!key:-}" ]; then tier="① 显式配置（环境变量 $key）"; break; fi
done
if [ -z "$tier" ]; then
    for f in src/main/resources/application-*.yaml; do
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
        printf '  %-26s %3ds  %s\n' "$label" "$((SECONDS - start))" "$([ $rc -eq 0 ] && echo 通过 || echo "失败(rc=$rc)")"
    }
    timed "静态三连" ./mvnw -B -o -q spotless:apply checkstyle:check test-compile
    timed "单测（不含真库）" ./mvnw -B -o test -Dtest='!*ApiTest,!MapperSmokeTest'
    timed "全量 verify" ./mvnw -B -o verify
fi

cat <<'EOF'

== 迭代建议 ==
  改一版代码      ./mvnw -B -o -q spotless:apply checkstyle:check test-compile   # ~2 秒
  验某个类        ./mvnw -B -o test -Dtest=AddressApiTest                      # ~5-11 秒
  收尾必须        ./mvnw verify                                               # ~14-18 秒
  更快的真库      TESTCONTAINERS_REUSE_ENABLE=true ./mvnw verify              # 复用容器，省 ~5 秒/轮
EOF
