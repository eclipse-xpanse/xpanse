/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Route, Routes } from 'react-router-dom';
import './styles/app.css';
import Home from './components/content/home/Home';
import LoginScreen from './components/content/login/LoginScreen';
import Protected from './components/protectedRoutes/ProtectedRoute';
import { homePageRoute, registerPageRoute } from './components/utils/constants';
import RegisterPanel from './components/content/register/RegisterPanel';

function App(): JSX.Element {
    return (
        <Routes>
            <Route
                path={homePageRoute}
                element={
                    <Protected>
                        <Home />
                    </Protected>
                }
            />
            <Route
                path={registerPageRoute}
                element={
                    <Protected>
                        <RegisterPanel />
                    </Protected>
                }
            />
            <Route path='*' element={<LoginScreen />} />
        </Routes>
    );
}

export default App;
