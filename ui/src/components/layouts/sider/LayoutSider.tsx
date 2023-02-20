import { Layout, Menu, Image } from 'antd';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { homePageRoute } from '../../utils/constants';

function LayoutSider(): JSX.Element {
  const [collapsed, setCollapsed] = useState(false);
  return (
    <Layout.Sider collapsible collapsed={collapsed} onCollapse={(newValue) => setCollapsed(newValue)}>
      <div className="logo">
        <Link to={homePageRoute}>
          <Image width={50} src="./logo.png" preview={false} />
        </Link>
      </div>
      <Menu items={[]} mode="inline" theme="dark" />
    </Layout.Sider>
  );
}

export default LayoutSider;
