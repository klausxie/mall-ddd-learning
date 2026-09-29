import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { API_BASE, ApiError, MALFORMED_RESPONSE_CODE, NETWORK_ERROR_CODE, request } from '../api/client';
import { API_PATHS } from '../api/paths';

interface CapturedRequest {
  readonly url: string;
  readonly init: RequestInit | undefined;
}

const captured: CapturedRequest[] = [];
let respond: () => Promise<Response> = () => Promise.resolve(jsonResponse({ code: 0, message: 'ok' }));

function urlOf(input: RequestInfo | URL): string {
  if (typeof input === 'string') return input;
  if (input instanceof URL) return input.href;
  return input.url;
}

/** 手写一个只有 client 用到的三个成员的响应替身，避免依赖运行时是否提供 Response。 */
function jsonResponse(payload: unknown, status = 200): Response {
  const body = payload === undefined ? '' : JSON.stringify(payload);
  const stub = { ok: status >= 200 && status < 300, status, text: () => Promise.resolve(body) };
  return stub as unknown as Response;
}

function lastRequest(): CapturedRequest {
  const item = captured[captured.length - 1];
  if (!item) throw new Error('fetch 没有被调用');
  return item;
}

beforeEach(() => {
  captured.length = 0;
  respond = () => Promise.resolve(jsonResponse({ code: 0, message: 'ok' }));
  vi.stubGlobal('fetch', (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
    captured.push({ url: urlOf(input), init });
    return respond();
  });
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('client.request', () => {
  it('code=0 时解出 data，并带上 /api 前缀、GET 与 Cookie 凭证', async () => {
    respond = () => Promise.resolve(jsonResponse({ code: 0, message: 'ok', data: { id: 7, mobile: '139' } }));

    const data = await request<{ id: number; mobile: string }>(API_PATHS.userGet);

    expect(data).toEqual({ id: 7, mobile: '139' });
    expect(lastRequest().url).toBe(`${API_BASE}${API_PATHS.userGet}`);
    expect(lastRequest().init?.method).toBe('GET');
    expect(lastRequest().init?.credentials).toBe('include');
  });

  it('code != 0 时抛带后端 code 与 message 的 ApiError', async () => {
    respond = () => Promise.resolve(jsonResponse({ code: 40005, message: '未登录' }));

    const error: unknown = await request(API_PATHS.userGet).catch((cause: unknown) => cause);

    expect(error).toBeInstanceOf(ApiError);
    expect(error).toMatchObject({ code: 40005, message: '未登录' });
  });

  it('非 2xx 与 code != 0 走同一个错误类型', async () => {
    respond = () => Promise.resolve(jsonResponse({ code: 40002, message: '参数不合法' }, 400));
    const business: unknown = await request(API_PATHS.userCreate, { method: 'POST', body: {} }).catch(
      (c: unknown) => c,
    );
    expect(business).toBeInstanceOf(ApiError);
    expect(business).toMatchObject({ code: 40002, message: '参数不合法' });

    respond = () => Promise.resolve(jsonResponse(undefined, 502));
    const gateway: unknown = await request(API_PATHS.userGet).catch((cause: unknown) => cause);
    expect(gateway).toBeInstanceOf(ApiError);
    expect(gateway).toMatchObject({ code: 502 });
  });

  it('响应不符合 {code,message,data} 约定时抛 MALFORMED_RESPONSE_CODE', async () => {
    respond = () => Promise.resolve(jsonResponse({ foo: 'bar' }));

    await expect(request(API_PATHS.userGet)).rejects.toMatchObject({ code: MALFORMED_RESPONSE_CODE });
  });

  it('网络层失败也抛同一种 ApiError', async () => {
    respond = () => Promise.reject(new TypeError('fetch failed'));

    await expect(request(API_PATHS.userGet)).rejects.toMatchObject({ code: NETWORK_ERROR_CODE });
  });

  it('GET 的 query 序列化进 URL，undefined 值被忽略', async () => {
    await request(API_PATHS.addressPage, { query: { curPage: 2, pageSize: 5, ignored: undefined } });

    expect(lastRequest().url).toBe(`${API_BASE}${API_PATHS.addressPage}?curPage=2&pageSize=5`);
  });

  it('POST 带 JSON body 与 Content-Type', async () => {
    await request(API_PATHS.userLogin, { method: 'POST', body: { mobile: '139', password: 'x' } });

    const request0 = lastRequest();
    expect(request0.init?.method).toBe('POST');
    expect(request0.init?.body).toBe(JSON.stringify({ mobile: '139', password: 'x' }));
    expect(request0.init?.headers).toMatchObject({ 'Content-Type': 'application/json' });
  });
});
