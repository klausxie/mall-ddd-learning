import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { pageAddress, removeAddress } from '../api/address';
import type { AddressInfo } from '../api/address';
import { logout } from '../api/auth';
import { ApiError } from '../api/client';
import { AddressForm } from '../components/AddressForm';
import { AddressList } from '../components/AddressList';

function messageOf(cause: unknown, fallback: string): string {
  return cause instanceof ApiError ? cause.message : fallback;
}

/** 地址列表页：证明 Vite 代理 + api 层（含 HttpOnly Cookie 凭证）是通的。 */
export function AddressPage() {
  const navigate = useNavigate();
  const [records, setRecords] = useState<AddressInfo[]>([]);
  const [total, setTotal] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshToken, setRefreshToken] = useState(0);

  useEffect(() => {
    let cancelled = false;
    void pageAddress({ curPage: 1, pageSize: 10 })
      .then((page) => {
        if (cancelled) return;
        setRecords(page.records);
        setTotal(page.total);
      })
      .catch((cause: unknown) => {
        if (!cancelled) setError(messageOf(cause, '加载失败，请稍后重试'));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [refreshToken]);

  /** 拉数据的状态改动放在事件里，不在 effect 里同步 setState。 */
  function reload(): void {
    setLoading(true);
    setError(null);
    setRefreshToken((value) => value + 1);
  }

  async function handleRemove(id: number): Promise<void> {
    try {
      await removeAddress(id);
      reload();
    } catch (cause) {
      setError(messageOf(cause, '删除失败，请稍后重试'));
    }
  }

  async function handleLogout(): Promise<void> {
    await logout();
    await navigate('/login');
  }

  return (
    <main className="page">
      <header className="page-header">
        <h1>收货地址</h1>
        <button type="button" onClick={() => void handleLogout()}>
          退出登录
        </button>
      </header>
      <p className="hint">共 {total} 条</p>
      {error ? <p className="error">{error}</p> : null}
      {loading ? (
        <p className="empty">加载中…</p>
      ) : (
        <AddressList records={records} onRemove={(id) => void handleRemove(id)} />
      )}
      <AddressForm onCreated={reload} />
    </main>
  );
}
