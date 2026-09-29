#!/usr/bin/env bash
#
# 把本模板初始化成一个新项目：改包名 / 坐标 / 配置前缀。
#
# 用法：
#   scripts/init.sh <groupId> <artifactId> <basePackage> [configPrefix] [--yes] [--force] [--reset-git]
#
# 示例：
#   scripts/init.sh cn.acme order-service cn.acme.order
#   scripts/init.sh cn.acme order-service cn.acme.order order --yes --reset-git
#
# 设计要点：
#   1. 只处理 git 跟踪的文件——与 GitHub "Use this template" 的行为一致，
#      所以 target/、application-local.yaml（含真实凭证）这类文件不会被带进新项目；
#   2. 默认要求工作区干净并要一次人工确认，避免有人直接在模板仓库里跑它把模板改掉；
#   3. 顺手把样例标识符一起改掉：MALL_DB_* 环境变量、配置前缀、文档标题。
#
# 它不会替你做的事：删减业务样例、写业务代码、commit。见 TEMPLATE.md 的"初始化后清单"。
set -euo pipefail

OLD_GROUP="cn.mklaus"
OLD_BASE="cn.mklaus.app"
OLD_ARTIFACT="mall"
OLD_CONFIG_PREFIX="mall"

die() { printf '\033[31m错误：%s\033[0m\n' "$*" >&2; exit 1; }
info() { printf '\033[36m==> %s\033[0m\n' "$*"; }

usage() { sed -n '2,20p' "$0" | sed 's/^# \{0,1\}//'; }

ARGS=()
YES=0
FORCE=0
RESET_GIT=0
for arg in "$@"; do
    case "$arg" in
        --yes | -y) YES=1 ;;
        --force) FORCE=1 ;;
        --reset-git) RESET_GIT=1 ;;
        -h | --help)
            usage
            exit 0
            ;;
        -*) die "未知参数：$arg（--help 看用法）" ;;
        *) ARGS+=("$arg") ;;
    esac
done

[ "${#ARGS[@]}" -ge 3 ] || {
    usage
    exit 1
}

NEW_GROUP="${ARGS[0]}"
NEW_ARTIFACT="${ARGS[1]}"
NEW_BASE="${ARGS[2]}"
NEW_PREFIX="${ARGS[3]:-$(printf '%s' "$NEW_ARTIFACT" | cut -d- -f1 | tr -cd 'a-z0-9')}"
ENV_PREFIX="$(printf '%s' "$NEW_ARTIFACT" | tr '[:lower:]-.' '[:upper:]__')"

[[ "$NEW_BASE" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]] || die "basePackage 不合法：$NEW_BASE"
[[ "$NEW_GROUP" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]] || die "groupId 不合法：$NEW_GROUP"
[[ "$NEW_ARTIFACT" =~ ^[a-z][a-z0-9-]*$ ]] || die "artifactId 不合法：$NEW_ARTIFACT"
[[ "$NEW_PREFIX" =~ ^[a-z][a-z0-9]*$ ]] || die "configPrefix 不合法：$NEW_PREFIX"
[ "$NEW_BASE" != "$OLD_BASE" ] || die "basePackage 与模板相同，没什么可初始化的"

cd "$(dirname "$0")/.."
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || die "当前目录不是 git 仓库"

if [ "$FORCE" -ne 1 ] && [ -n "$(git status --porcelain)" ]; then
    die "工作区不干净（有未提交改动）。先 commit / stash，或加 --force 明确跳过这项检查"
fi

cat <<EOF
即将把本仓库初始化为：

  groupId        : $OLD_GROUP -> $NEW_GROUP
  artifactId     : $OLD_ARTIFACT -> $NEW_ARTIFACT
  基础包         : $OLD_BASE -> $NEW_BASE
  配置前缀       : $OLD_CONFIG_PREFIX -> $NEW_PREFIX
  环境变量前缀   : MALL_DB_ -> ${ENV_PREFIX}_DB_
  重置 git 历史  : $([ "$RESET_GIT" -eq 1 ] && echo 是 || echo 否)
EOF

if [ "$YES" -ne 1 ]; then
    read -r -p "确认继续？[y/N] " answer
    [[ "$answer" =~ ^[Yy]$ ]] || die "已取消"
fi

# 就地编辑要兼容 GNU sed（Linux）与 BSD sed（macOS）
if sed --version >/dev/null 2>&1; then
    SED_INPLACE=(sed -i)
else
    SED_INPLACE=(sed -i '')
fi

# 对所有 git 跟踪的文本文件做全局替换
replace_everywhere() {
    local from="$1" to="$2" file
    while IFS= read -r file; do
        "${SED_INPLACE[@]}" "s|$from|$to|g" "$file"
    done < <(git ls-files | xargs grep -lI -- "$from" 2>/dev/null || true)
}

info "1/6 移动源码目录"
# 用 tr 而不是 ${VAR//./\/}：后者在 bash 里会保留反斜杠，得到 "cn\/mklaus\/app"，
# 目录判断会静默失败——结果就是内容改了、目录没动，编译直接崩。
OLD_PATH="$(printf '%s' "$OLD_BASE" | tr '.' '/')"
NEW_PATH="$(printf '%s' "$NEW_BASE" | tr '.' '/')"
for root in src/main/java src/test/java; do
    if [ -d "$root/$OLD_PATH" ]; then
        mkdir -p "$(dirname "$root/$NEW_PATH")"
        git mv "$root/$OLD_PATH" "$root/$NEW_PATH"
    fi
done

info "2/6 替换包名、坐标与环境变量前缀"
# 必须**先**替换斜杠形式：replace_everywhere 用的是 sed 正则，点号会匹配任意字符，
# 于是 "cn.mklaus.app" 这一遍会把 "cn/mklaus/app" 也一起吃掉，替换成点号形式，
# 让文档/脚本里的路径引用指向不存在的目录（而且自检看不出问题，因为 mklaus 已经没了）。
replace_everywhere "$OLD_PATH" "$NEW_PATH"
replace_everywhere "$OLD_BASE" "$NEW_BASE"
replace_everywhere "$OLD_GROUP" "$NEW_GROUP"
replace_everywhere "MALL_DB_" "${ENV_PREFIX}_DB_"

info "3/6 替换 pom / compose / 配置里的项目标识"
"${SED_INPLACE[@]}" "s|<artifactId>$OLD_ARTIFACT</artifactId>|<artifactId>$NEW_ARTIFACT</artifactId>|" pom.xml
[ -f docker-compose.yml ] && "${SED_INPLACE[@]}" \
    -e "s|container_name: $OLD_ARTIFACT-mysql|container_name: $NEW_ARTIFACT-mysql|" \
    -e "s|MYSQL_DATABASE: $OLD_ARTIFACT|MYSQL_DATABASE: $NEW_PREFIX|" docker-compose.yml
for file in src/main/resources/application.yaml src/main/resources/application-local.yaml.example .github/workflows/verify.yml; do
    [ -f "$file" ] || continue
    "${SED_INPLACE[@]}" \
        -e "s|/$OLD_ARTIFACT?|/$NEW_PREFIX?|" \
        -e "s|^$OLD_CONFIG_PREFIX:|$NEW_PREFIX:|" \
        -e "s|$OLD_CONFIG_PREFIX\.captcha|$NEW_PREFIX.captcha|" \
        -e "s|MYSQL_DATABASE: $OLD_ARTIFACT|MYSQL_DATABASE: $NEW_PREFIX|" "$file"
done

info "4/6 替换文档里的项目名"
# 注意：变量一律用 ${} 包起来。后面紧跟全角括号等多字节字符时，
# 不加花括号会被 bash 当成变量名的一部分（unbound variable）。
for file in README.md CLAUDE.md AGENTS.md ARCHITECTURE.md TEMPLATE.md; do
    [ -f "$file" ] || continue
    "${SED_INPLACE[@]}" \
        -e "s|^# ${OLD_ARTIFACT}|# ${NEW_ARTIFACT}|" \
        -e "s|（${OLD_ARTIFACT}）|（${NEW_ARTIFACT}）|g" \
        -e "s|${OLD_ARTIFACT} 项目|${NEW_ARTIFACT} 项目|g" \
        -e "s|${OLD_CONFIG_PREFIX}\.captcha|${NEW_PREFIX}.captcha|g" "$file"
done

info "5/6 清理空目录"
find src -type d -empty -delete 2>/dev/null || true

info "6/6 自检：确认没有旧标识残留"
# 这一步是给"改名靠记忆必漏"兜底的：漏了就报错，而不是等编译/运行时才发现。
# 排除脚本自己（它的常量里当然有旧标识）。
LEFTOVERS="$(git ls-files | grep -v '^scripts/init.sh$' | xargs grep -lI \
    -e "$OLD_BASE" -e "$OLD_PATH" -e "$OLD_GROUP" -e "MALL_DB_" 2>/dev/null || true)"
if [ -n "$LEFTOVERS" ]; then
    printf '\033[31m以下文件里仍有旧标识，请手工确认（改名不完整会让新项目一开始就是坏的）：\033[0m\n' >&2
    printf '%s\n' "$LEFTOVERS" >&2
    exit 1
fi

if [ "$RESET_GIT" -eq 1 ]; then
    info "重置 git 历史"
    rm -rf .git
    git init -q
    git add -A
fi

cat <<EOF

初始化完成。接下来：

  1. ./mvnw spotless:apply && ./mvnw clean verify     # 骨架必须是绿的
  2. docker compose up -d && export ${ENV_PREFIX}_DB_PASSWORD=klaus
  3. 读 TEMPLATE.md 的"初始化后清单"，删掉用不上的样例并确认替身配置
  4. $([ "$RESET_GIT" -eq 1 ] && echo "git commit -m 'chore: 从模板初始化'" || echo "确认无误后提交")

EOF
