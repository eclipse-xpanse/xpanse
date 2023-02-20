import { UserOutlined } from '@ant-design/icons';
import { Dropdown, Layout, Space } from 'antd';
import { usernameKey } from '../../utils/constants';
import items from './headerUserMenuItems';

function LayoutHeader(): JSX.Element {
  return (
    <Layout.Header style={{ width: '100%', background: '#ffffff' }}>
      <div className="header-menu">
        <Dropdown menu={{ items }} placement="topRight">
          <Space>
            {localStorage.getItem(usernameKey)}
            <UserOutlined />
          </Space>
        </Dropdown>
      </div>
    </Layout.Header>
  );
}

export default LayoutHeader;
