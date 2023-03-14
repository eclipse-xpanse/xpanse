/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import WelcomeCard from './WelcomeCard';

function Home(): JSX.Element {
    return (
        <div className={'home-data-display'}>
            <WelcomeCard />
        </div>
    );
}

export default Home;
