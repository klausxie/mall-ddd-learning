/** 用户域接口：注册与当前用户查询。登录 / 退出属于令牌生命周期，见 auth.ts。 */
import { getJson, postJson } from './client';
import { API_PATHS } from './paths';

/** 对应后端 application/user/query/UserInfo.java（刻意不含 password）。 */
export interface UserInfo {
  readonly id: number;
  readonly mobile: string;
  readonly nickname: string | null;
  readonly avatar: string | null;
  readonly age: number | null;
}

/** 对应后端 application/user/command/UserCreateRequest.java。 */
export interface UserCreateRequest {
  readonly mobile: string;
  readonly captcha: string;
  readonly password: string;
  readonly age: number;
}

/** 对应后端 application/user/command/UserCreateResponse.java。 */
export interface UserCreateResponse {
  readonly user: UserInfo;
  readonly points: number;
}

/** 注册。响应里回显本次赠送的积分。 */
export function createUser(body: UserCreateRequest): Promise<UserCreateResponse> {
  return postJson<UserCreateResponse>(API_PATHS.userCreate, body);
}

/** 查询当前登录用户（需要 Cookie 或 Bearer 凭证，未登录时后端返回 code=40005）。 */
export function getCurrentUser(): Promise<UserInfo> {
  return getJson<UserInfo>(API_PATHS.userGet);
}
