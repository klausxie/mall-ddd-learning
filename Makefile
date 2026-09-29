# 仓库级入口：两端各自的构建细节都在 backend/ 与 frontend/ 里，这里只做编排。
#
# 为什么用 Makefile 而不是再写一个 pom/package.json：它是"薄薄一层指路牌"——
# 不引入任何运行时依赖，`make` 会列出所有目标，人和 AI 都不用先读脚本。
.DEFAULT_GOAL := help
.PHONY: help verify verify-backend verify-frontend dev db-up db-down facts clean

help: ## 列出所有目标
	@grep -hE '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) | awk -F':.*?## ' '{printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'

verify: verify-backend verify-frontend ## 两端全量校验（收尾必须跑这个）

verify-backend: ## 只跑后端：格式化 + Checkstyle + ArchUnit + 测试（含真库）+ 覆盖率门槛
	cd backend && ./mvnw -B -Pcoverage-check verify

verify-frontend: ## 只跑前端：格式检查 + lint + 类型 + 测试 + 构建
	cd frontend && pnpm run verify

dev: ## 并行起后端(:8080)与前端(:5173，/api 代理到后端)
	./scripts/dev.sh

db-up: ## 起本地 MySQL（只建空库，建表由应用启动时的 Flyway 完成）
	docker compose up -d

db-down: ## 停掉本地 MySQL
	docker compose down

facts: ## 仓库现状速览（体量/封顶/覆盖率/真库走哪一级），0.5 秒
	./scripts/facts.sh

clean: ## 清掉两端的构建产物
	cd backend && ./mvnw -q clean
	rm -rf frontend/dist frontend/coverage
