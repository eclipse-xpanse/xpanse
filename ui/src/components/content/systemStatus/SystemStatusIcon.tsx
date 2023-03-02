/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { CheckCircleTwoTone, CloseCircleTwoTone } from '@ant-design/icons';

function SystemStatusIcon({ isSystemUp }: { isSystemUp: boolean }): JSX.Element {
    if (isSystemUp) {
        return <CheckCircleTwoTone twoToneColor='#52c41a' />;
    } else {
        return <CloseCircleTwoTone twoToneColor='#eb2f38' />;
    }
}

export default SystemStatusIcon;
