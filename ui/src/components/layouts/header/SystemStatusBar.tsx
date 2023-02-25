import { Button, Space } from 'antd';
import { useState } from 'react';
import { SystemStatus, SystemStatusHealthStatusEnum } from '../../../xpanse-api/generated';
import { apiInstance } from '../../../xpanse-api/xpanseRestApiClient';
import SystemStatusIcon from './SystemStatusIcon';

function SystemStatusBar(): JSX.Element {
  const [healthState, setHealthState] = useState<SystemStatusHealthStatusEnum>('NOK');
  const [isReloadSystemStatus, setReloadSystemStatus] = useState<boolean>(false);

  apiInstance
    .health()
    .then((systemStatus: SystemStatus) => setHealthState(systemStatus.healthStatus))
    .catch((error: any) => {
      console.error(error);
    });

  return (
    <Space>
      <Button
        className="header-menu-botton"
        icon={<SystemStatusIcon isSystemUp={healthState === 'OK'} />}
        onClick={() => setReloadSystemStatus(!isReloadSystemStatus)}
      >
        System Status
      </Button>
    </Space>
  );
}

export default SystemStatusBar;
