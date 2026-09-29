/** 收货地址域接口。 */
import { getJson, postJson } from './client';
import { API_PATHS } from './paths';
import type { Page, PageQuery } from './types';

/** 对应后端 application/user/query/AddressInfo.java。 */
export interface AddressInfo {
  readonly id: number;
  readonly recipient: string;
  readonly phone: string;
  readonly province: string | null;
  readonly city: string | null;
  readonly district: string | null;
  readonly detail: string | null;
}

/** 对应后端 application/user/command/AddressCreateRequest.java。 */
export interface AddressCreateRequest {
  readonly recipient: string;
  readonly phone: string;
  readonly province?: string;
  readonly city?: string;
  readonly district?: string;
  readonly detail?: string;
}

/** 对应后端 application/user/command/AddressUpdateRequest.java。 */
export interface AddressUpdateRequest extends AddressCreateRequest {
  readonly addressId: number;
}

/** 新增地址，返回落库后的完整地址。 */
export function createAddress(body: AddressCreateRequest): Promise<AddressInfo> {
  return postJson<AddressInfo>(API_PATHS.addressCreate, body);
}

/** 修改地址（后端返回 void）。 */
export function updateAddress(body: AddressUpdateRequest): Promise<void> {
  return postJson<void>(API_PATHS.addressUpdate, body);
}

/** 删除地址（后端返回 void）。 */
export function removeAddress(addressId: number): Promise<void> {
  return postJson<void>(API_PATHS.addressRemove, { addressId });
}

/** 分页查询地址，参数固定 curPage / pageSize。 */
export function pageAddress(query: PageQuery = {}): Promise<Page<AddressInfo>> {
  return getJson<Page<AddressInfo>>(API_PATHS.addressPage, query);
}
