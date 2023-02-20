import { MenuProps } from 'antd';
import Logout from '../../content/Logout';

const items: MenuProps['items'] = [{ key: 'signout', label: <Logout /> }];

export default items;
