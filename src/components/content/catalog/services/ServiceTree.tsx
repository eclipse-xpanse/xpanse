/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import React, { useState } from 'react';
import { Tree } from 'antd';
import type { DataNode } from 'antd/es/tree';

function ServiceTree({
    treeData,
    setKey,
}: {
    treeData: DataNode[];
    setKey: React.Dispatch<React.SetStateAction<string>>;
}): JSX.Element {
    const [autoExpandParent, setAutoExpandParent] = useState<boolean>(true);

    const onSelect = (selectedKeysValue: React.Key[], info: any) => {
        setKey(selectedKeysValue[0].toString());
    };

    return (
        <>
            <Tree defaultExpandAll={true} autoExpandParent={autoExpandParent} onSelect={onSelect} treeData={treeData} />
        </>
    );
}

export default ServiceTree;
