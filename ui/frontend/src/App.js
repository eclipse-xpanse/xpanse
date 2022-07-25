import React, { useReducer, useState } from 'react';
import { Layout, Menu } from 'antd';
import { Redirect, Route, Switch } from 'react-router';
import { BrowserRouter as Router, Link } from 'react-router-dom';
import { ReactComponent as Logo } from './logo.svg';
import './App.css';

import routes from './routes';
import reducer from './reducers';

function SwitchRoutes({ component, includeRedirect, filter }) {
    return (
        <Switch>
            {routes.filter(route => filter == null || filter(route)).map((route, key) => (
                <Route path={route.path} key={key} exact={route.exact}>
                    {component(route)}
                </Route>
            ))}
            {includeRedirect && <Redirect from="*" to="/" />}
    );
}

function SideMenuItems(props) {
    return (
        <Menu theme="dark" selectedKeys={[props.selectedKeys]} mode="inline">

        </Menu>
    )
}