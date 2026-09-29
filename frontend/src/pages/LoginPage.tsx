import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../api/auth';
import { ApiError } from '../api/client';

/** 登录页：手机号 + 密码。成功后后端下发 HttpOnly Cookie，前端只负责跳转。 */
export function LoginPage() {
  const navigate = useNavigate();
  const [mobile, setMobile] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);

  async function submit(): Promise<void> {
    setPending(true);
    setError(null);
    try {
      await login({ mobile, password });
      await navigate('/addresses');
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : '登录失败，请稍后重试');
    } finally {
      setPending(false);
    }
  }

  return (
    <main className="card">
      <h1>登录 mall</h1>
      <form
        onSubmit={(event: FormEvent<HTMLFormElement>) => {
          event.preventDefault();
          void submit();
        }}
      >
        <label htmlFor="mobile">手机号</label>
        <input id="mobile" value={mobile} onChange={(event) => setMobile(event.target.value)} autoComplete="username" />
        <label htmlFor="password">密码</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          autoComplete="current-password"
        />
        {error ? <p className="error">{error}</p> : null}
        <button type="submit" disabled={pending}>
          {pending ? '登录中…' : '登录'}
        </button>
      </form>
    </main>
  );
}
