import type { AddressInfo } from '../api/address';

interface AddressListProps {
  records: AddressInfo[];
  onRemove?: (id: number) => void;
}

function formatRegion(item: AddressInfo): string {
  return [item.province, item.city, item.district, item.detail]
    .filter((part): part is string => typeof part === 'string' && part.length > 0)
    .join(' ');
}

/** 纯展示组件：只吃数据，不发请求（请求一律走 api 层）。 */
export function AddressList({ records, onRemove }: AddressListProps) {
  if (records.length === 0) {
    return <p className="empty">还没有收货地址</p>;
  }
  return (
    <ul className="address-list">
      {records.map((item) => (
        <li key={item.id}>
          <div className="address-main">
            <strong>{item.recipient}</strong>
            <span className="phone">{item.phone}</span>
            <span className="region">{formatRegion(item)}</span>
          </div>
          {onRemove ? (
            <button type="button" className="link-danger" onClick={() => onRemove(item.id)}>
              删除
            </button>
          ) : null}
        </li>
      ))}
    </ul>
  );
}
