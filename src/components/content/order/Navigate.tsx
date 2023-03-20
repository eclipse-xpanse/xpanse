/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { useNavigate, To } from 'react-router-dom';
import '../../../styles/order.css';

function Navigate({ text, to }: { text: String; to: To }): JSX.Element {
    const navigate = useNavigate();

    function goBack() {
        navigate(to);
    }

    return (
        <div>
            <div onClick={goBack} className='order-navigate'>
                {text}
            </div>
        </div>
    );
}

export default Navigate;
