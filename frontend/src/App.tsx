import { Link, Navigate, Route, Routes } from 'react-router-dom';
import { AddressPage } from './pages/AddressPage';
import { LoginPage } from './pages/LoginPage';

export function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/addresses" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/addresses" element={<AddressPage />} />
      <Route
        path="*"
        element={
          <p>
            页面不存在，<Link to="/addresses">返回地址列表</Link>
          </p>
        }
      />
    </Routes>
  );
}
