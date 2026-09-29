import { useState } from 'react';
import type { FormEvent } from 'react';
import { createAddress } from '../api/address';
import type { AddressCreateRequest } from '../api/address';
import { ApiError } from '../api/client';

interface AddressFormProps {
  onCreated: () => void;
}

const EMPTY_FORM = { recipient: '', phone: '', province: '', city: '', district: '', detail: '' };

/** 新增地址表单：校验交给后端，前端只负责提交与展示 ApiError.message。 */
export function AddressForm({ onCreated }: AddressFormProps) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);

  function update(field: keyof typeof EMPTY_FORM, value: string): void {
    setForm((previous) => ({ ...previous, [field]: value }));
  }

  async function submit(): Promise<void> {
    setPending(true);
    setError(null);
    const body: AddressCreateRequest = { ...form, recipient: form.recipient.trim(), phone: form.phone.trim() };
    try {
      await createAddress(body);
      setForm(EMPTY_FORM);
      onCreated();
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : '新增失败，请稍后重试');
    } finally {
      setPending(false);
    }
  }

  return (
    <form
      className="address-form"
      onSubmit={(event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        void submit();
      }}
    >
      <h2>新增收货地址</h2>
      <input
        placeholder="收件人"
        value={form.recipient}
        onChange={(event) => update('recipient', event.target.value)}
      />
      <input placeholder="联系电话" value={form.phone} onChange={(event) => update('phone', event.target.value)} />
      <input placeholder="省" value={form.province} onChange={(event) => update('province', event.target.value)} />
      <input placeholder="市" value={form.city} onChange={(event) => update('city', event.target.value)} />
      <input placeholder="区" value={form.district} onChange={(event) => update('district', event.target.value)} />
      <input placeholder="详细地址" value={form.detail} onChange={(event) => update('detail', event.target.value)} />
      {error ? <p className="error">{error}</p> : null}
      <button type="submit" disabled={pending}>
        {pending ? '提交中…' : '保存'}
      </button>
    </form>
  );
}
