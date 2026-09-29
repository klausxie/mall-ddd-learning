/**
 * 唯一的 HTTP 出口。除本文件外，src/ 下不许再直接调用 fetch。
 *
 * 约定：
 * - 所有请求都带 `/api` 前缀（开发态由 Vite 代理 rewrite 掉）与 `credentials: 'include'`，
 *   令牌走 HttpOnly Cookie，前端不碰 token；
 * - 后端统一返回 `{code, message, data}`，`code === 0` 才算成功，成功时把 data 解出来；
 * - 非 2xx 与 `code !== 0` 都抛同一个 ApiError（带 code 与 message），调用方只需 catch 一种错误。
 */
export const API_BASE = '/api';

/** 请求没能送达（网络层失败）时使用的本地错误码，与后端错误码不冲突。 */
export const NETWORK_ERROR_CODE = -1;
/** 响应不是 `{code, message, data}` 形状时使用的本地错误码。 */
export const MALFORMED_RESPONSE_CODE = -2;

export class ApiError extends Error {
  readonly code: number;

  constructor(code: number, message: string) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
  }
}

/** 后端统一响应体的形状。 */
export interface ApiEnvelope<T> {
  readonly code: number;
  readonly message: string;
  readonly data: T | undefined;
}

export type QueryValue = string | number | boolean | undefined;

export interface RequestOptions {
  readonly method?: 'GET' | 'POST';
  readonly body?: unknown;
  readonly query?: Readonly<Record<string, QueryValue>>;
  readonly signal?: AbortSignal;
}

function readProperty(source: unknown, key: string): unknown {
  if (typeof source !== 'object' || source === null) {
    return undefined;
  }
  return (source as Record<string, unknown>)[key];
}

function parseEnvelope(payload: unknown): ApiEnvelope<unknown> | undefined {
  const code = readProperty(payload, 'code');
  const message = readProperty(payload, 'message');
  if (typeof code !== 'number' || typeof message !== 'string') {
    return undefined;
  }
  return { code, message, data: readProperty(payload, 'data') };
}

function buildQuery(query: RequestOptions['query']): string {
  if (!query) {
    return '';
  }
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(query)) {
    if (value === undefined) {
      continue;
    }
    params.append(key, String(value));
  }
  const serialized = params.toString();
  return serialized ? `?${serialized}` : '';
}

async function readPayload(response: Response): Promise<unknown> {
  const text = await response.text();
  if (!text) {
    return undefined;
  }
  try {
    return JSON.parse(text) as unknown;
  } catch {
    return undefined;
  }
}

/**
 * 发一个请求并解出 data。失败一律抛 {@link ApiError}。
 */
export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, query, signal } = options;
  const headers: Record<string, string> = { Accept: 'application/json' };
  const init: RequestInit = { method, credentials: 'include', headers };
  if (signal) {
    init.signal = signal;
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    init.body = JSON.stringify(body);
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE}${path}${buildQuery(query)}`, init);
  } catch {
    throw new ApiError(NETWORK_ERROR_CODE, `网络异常，请求未送达：${path}`);
  }

  const envelope = parseEnvelope(await readPayload(response));
  if (!response.ok) {
    throw new ApiError(envelope?.code ?? response.status, envelope?.message ?? `HTTP ${response.status}`);
  }
  if (!envelope) {
    throw new ApiError(MALFORMED_RESPONSE_CODE, `响应不符合 {code,message,data} 约定：${path}`);
  }
  if (envelope.code !== 0) {
    throw new ApiError(envelope.code, envelope.message);
  }
  return envelope.data as T;
}

/** 查询接口用 GET，query 会被序列化到 URL 上（分页参数固定 curPage / pageSize）。 */
export function getJson<T>(path: string, query?: RequestOptions['query']): Promise<T> {
  return request<T>(path, query ? { method: 'GET', query } : { method: 'GET' });
}

/** 新增 / 修改 / 删除一律 POST，body 走 JSON。 */
export function postJson<T>(path: string, body?: unknown): Promise<T> {
  return request<T>(path, body === undefined ? { method: 'POST' } : { method: 'POST', body });
}
