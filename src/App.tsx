/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Route, Routes } from 'react-router-dom';
import './styles/app.css';
import Home from './components/content/home/Home';
import LoginScreen from './components/content/login/LoginScreen';
import Protected from './components/protectedRoutes/ProtectedRoute';
import {
  catalogPageRoute, createServicePageRoute,
  homePageRoute,
  orderPageRoute,
  registerPageRoute,
  servicesPageRoute
} from './components/utils/constants';
import RegisterPanel from './components/content/register/RegisterPanel';
import Catalog from './components/content/catalog/Catalog';
import { DefaultOrderExtendParams } from './components/content/order/OrderSubmit';
import Services from './components/content/order/Services';
import CreateService from './components/content/order/CreateService';

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
            <Route
                path={catalogPageRoute}
                element={
                    <Protected>
                        <Catalog />
                    </Protected>
                }
            />
            <Route
                path={orderPageRoute}
                element={
                    <Protected>
                        <DefaultOrderExtendParams />
                    </Protected>
                }
            />
            <Route
                path={servicesPageRoute}
                element={
                    <Protected>
                        <Services />
                    </Protected>
                }
            />
            <Route
                path={createServicePageRoute}
                element={
                    <Protected>
                        <CreateService />
                    </Protected>
                }
            />
            <Route path='*' element={<LoginScreen />} />
        </Routes>
    );
}

export default App;
