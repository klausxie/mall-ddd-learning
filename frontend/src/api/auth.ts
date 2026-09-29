/**
 * 令牌生命周期：登录 / 退出。
 *
 * 登录成功后后端会下发 HttpOnly Cookie（SameSite=Lax），JS 读不到令牌，也不该读：
 * 前端只负责把凭证带上（client.ts 的 `credentials: 'include'`），不做 token 存储。
 * 响应体里另有 `token` 字段给 App / 开放 API 用，Web 端不用它。
 */
import { postJson } from './client';
import { API_PATHS } from './paths';
import type { UserInfo } from './user';

/** 对应后端 application/user/command/UserLoginRequest.java。 */
export interface LoginRequest {
  readonly mobile: string;
  readonly password: string;
}

/** 对应后端 application/user/command/UserLoginResponse.java。 */
export interface LoginResponse {
  readonly token: string;
  readonly user: UserInfo;
}

/** 登录：手机号 + 密码。 */
export function login(body: LoginRequest): Promise<LoginResponse> {
  return postJson<LoginResponse>(API_PATHS.userLogin, body);
}

/** 退出登录：让后端清掉令牌 Cookie（自签令牌无法在服务端吊销）。 */
export function logout(): Promise<void> {
  return postJson<void>(API_PATHS.userLogout);
}
