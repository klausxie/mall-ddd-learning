import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { createAddress, pageAddress, removeAddress, updateAddress } from '../api/address';
import { login, logout } from '../api/auth';
import { API_BASE } from '../api/client';
import { API_PATHS } from '../api/paths';
import { createUser, getCurrentUser } from '../api/user';

interface CapturedRequest {
  readonly url: string;
  readonly init: RequestInit | undefined;
}

const captured: CapturedRequest[] = [];

function urlOf(input: RequestInfo | URL): string {
  if (typeof input === 'string') return input;
  if (input instanceof URL) return input.href;
  return input.url;
}

function okResponse(): Response {
  const stub = { ok: true, status: 200, text: () => Promise.resolve('{"code":0,"message":"ok"}') };
  return stub as unknown as Response;
}

function pathOf(url: string): string {
  return url.split('?')[0] ?? '';
}

/** 调用 api 层的全部领域函数，覆盖 API_PATHS 的每一条。 */
async function callEveryDomainFunction(): Promise<void> {
  await createUser({ mobile: '13900000001', captcha: '1234', password: 'Passw0rd!', age: 30 });
  await getCurrentUser();
  await login({ mobile: '13900000001', password: 'Passw0rd!' });
  await logout();
  await createAddress({ recipient: '张三', phone: '13900000000' });
  await updateAddress({ addressId: 1, recipient: '张三', phone: '13900000000' });
  await removeAddress(1);
  await pageAddress({ curPage: 1, pageSize: 10 });
}

beforeEach(() => {
  captured.length = 0;
  vi.stubGlobal('fetch', (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
    captured.push({ url: urlOf(input), init });
    return Promise.resolve(okResponse());
  });
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('api 层与 API_PATHS 一致', () => {
  it('八个领域函数正好覆盖 API_PATHS 的八条路径，没有多余条目也没有漏用', async () => {
    await callEveryDomainFunction();

    const hit = captured.map((item) => pathOf(item.url));
    const declared = Object.values(API_PATHS).map((path) => `${API_BASE}${path}`);

    expect([...hit].sort()).toEqual([...declared].sort());
    expect(new Set(hit).size).toBe(declared.length);
  });

  it('查询用 GET，写操作一律 POST，路径全部取自 API_PATHS', async () => {
    await callEveryDomainFunction();

    const methodOf = new Map(captured.map((item) => [pathOf(item.url), item.init?.method]));
    const method = (path: string): string | undefined => methodOf.get(`${API_BASE}${path}`);

    expect(method(API_PATHS.userGet)).toBe('GET');
    expect(method(API_PATHS.addressPage)).toBe('GET');
    for (const path of [
      API_PATHS.userCreate,
      API_PATHS.userLogin,
      API_PATHS.userLogout,
      API_PATHS.addressCreate,
      API_PATHS.addressUpdate,
      API_PATHS.addressRemove,
    ]) {
      expect(method(path), path).toBe('POST');
    }
  });

  it('registerAddress 的 POST body 是 JSON', async () => {
    await removeAddress(42);

    expect(captured[0]?.init?.body).toBe(JSON.stringify({ addressId: 42 }));
  });
});
