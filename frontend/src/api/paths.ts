/**
 * 后端接口路径的**唯一**清单（跨端契约面）。
 *
 * 规则：
 * - 路径值必须是字符串字面量、camelCase，且与后端 Controller 的 mapping 完全一致；
 * - 后端路径本身**不带** `/api` 前缀，`/api` 只由 client.ts 的 API_BASE 拼接，
 *   开发态再由 vite.config.ts 的 proxy rewrite 去掉（见 CLAUDE.md）；
 * - 除本文件外，任何地方都不许再出现字面量路径——领域函数一律引用 API_PATHS，
 *   该规则由 src/__tests__/guardrails.test.ts 机械强制。
 */
export const API_PATHS = {
  // 用户
  userCreate: '/user/create',
  userLogin: '/user/login',
  userLogout: '/user/logout',
  userGet: '/user/get',
  // 收货地址
  addressCreate: '/address/create',
  addressUpdate: '/address/update',
  addressRemove: '/address/remove',
  addressPage: '/address/page',
} as const;

/** API_PATHS 的值的联合类型。 */
export type ApiPath = (typeof API_PATHS)[keyof typeof API_PATHS];
