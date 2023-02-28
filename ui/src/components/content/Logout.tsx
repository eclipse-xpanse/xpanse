/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { PoweroffOutlined } from '@ant-design/icons';
import { Button } from 'antd';
import { NavigateFunction, useNavigate } from 'react-router-dom';
import { loginPageRoute } from '../utils/constants';

function clearStorage(navigate: NavigateFunction): void {
  localStorage.clear();
  console.log('navigate');
  navigate(loginPageRoute);
}

function Logout(): JSX.Element {
  const navigate = useNavigate();
  return (
    <Button type='link' onClick={() => clearStorage(navigate)} icon={<PoweroffOutlined />} block={true} size='small'>
      LogOut
    </Button>
  );
}

export default Logout;
