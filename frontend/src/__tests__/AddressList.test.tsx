import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { AddressInfo } from '../api/address';
import { AddressList } from '../components/AddressList';

const RECORD: AddressInfo = {
  id: 1,
  recipient: '张三',
  phone: '13900000000',
  province: '浙江省',
  city: '杭州市',
  district: '西湖区',
  detail: '文一西路 1 号',
};

describe('AddressList', () => {
  it('渲染收件人、电话与拼好的完整地址', () => {
    render(<AddressList records={[RECORD]} />);

    expect(screen.getByText('张三')).toBeInTheDocument();
    expect(screen.getByText('13900000000')).toBeInTheDocument();
    expect(screen.getByText('浙江省 杭州市 西湖区 文一西路 1 号')).toBeInTheDocument();
  });

  it('空列表给出提示而不是空白', () => {
    render(<AddressList records={[]} />);

    expect(screen.getByText('还没有收货地址')).toBeInTheDocument();
  });

  it('传了 onRemove 才渲染删除按钮', () => {
    render(<AddressList records={[RECORD]} onRemove={() => undefined} />);

    expect(screen.getByRole('button', { name: '删除' })).toBeInTheDocument();
  });
});
