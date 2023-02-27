/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Route, Routes } from 'react-router-dom';
import './styles/app.css';
import Home from './components/content/Home';
import LoginScreen from './components/content/LoginScreen';
import Protected from './components/content/ProtectedRoute';
import { homePageRoute } from './components/utils/constants';

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
      <Route path='*' element={<LoginScreen />} />
    </Routes>
  );
}

export default App;
