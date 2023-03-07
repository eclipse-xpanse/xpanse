/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Layout } from 'antd';
import { footerText } from '../../utils/constants';

function LayoutFooter(): JSX.Element {
    return <Layout.Footer>{footerText}</Layout.Footer>;
}

export default LayoutFooter;
