import { PoweroffOutlined } from '@ant-design/icons';
import { Button, Space } from 'antd';
import { NavigateFunction, useNavigate } from 'react-router-dom';

function clearStorage(navigate: NavigateFunction): void {
  localStorage.clear();
  console.log('navigate');
  navigate('/login');
}

function Logout(): JSX.Element {
  const navigate = useNavigate();
  return (
    <Space wrap>
      <Button type="link" onClick={() => clearStorage(navigate)} icon={<PoweroffOutlined />} block={true} size="small">
        LogOut
      </Button>
    </Space>
  );
}

export default Logout;
