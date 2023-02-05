import React, { useState } from 'react';
import { MonitorOutlined, DownOutlined, AppstoreOutlined, BankOutlined, ConsoleSqlOutlined, DatabaseOutlined, CloudOutlined, CloudServerOutlined, ContainerOutlined, NodeIndexOutlined } from '@ant-design/icons';
import Icon from '@ant-design/icons';
import { Layout, Menu, Image, Space, Form, Input, Button, Modal, Dropdown, message } from 'antd';
import { Route, Switch } from 'react-router';
import { BrowserRouter as Router } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { ReactComponent as KafkaSvg } from './kafka_icon.svg';
import { ReactComponent as KubernetesSvg } from './kubernetes_icon.svg';
import { ReactComponent as CassandraSvg } from './cassandra_icon.svg';
import { ReactComponent as ActivemqSvg } from './activemq_icon.svg';
import { ReactComponent as RancherSvg } from './rancher_icon.svg';
import './App.css';
import Catalog from './Catalog';
import Home from './Home';
import VM from './VM';
import Container from './Container';
import Kubernetes from './Kubernetes';
import RDS from './RDS';
import Kafka from './Kafka';
import Rancher from './Rancher';
import Cassandra from './Cassandra';
import Activemq from './Activemq';
import Pulsar from './Pulsar';
import Billing from './Billing';
import Monitoring from './Monitoring';

function SideMenu(props) {
  const [collapsed, setCollapsed] = useState(false);
  var menuItems = props.items.slice();
  if (props.user === 'user' || props.user === 'otc') {
    var serviceItems = menuItems.filter(i => i.key !== 'catalog');
    menuItems = [ 
			{ id: 'services', label: 'Services', icon: <AppstoreOutlined />,  children: [] },
			{ id: 'monitoring', label: <Link to="/monitoring">Monitoring</Link>, icon: <MonitorOutlined/> }
		];
    menuItems[0].children = serviceItems;
  } else {
    menuItems = menuItems.filter(i => i.key === 'catalog');
  }
  if (props.user === 'otc') {
    return (
      <Layout.Sider style={{ background: '#e30074' }}>
        <div className="logo">
          <Link to="/"><Image width={50} src="./otc.jpg" preview={false}/></Link>
        </div>
        <Menu items={menuItems} mode="inline" theme="light" style={{ color: '#000000', background: '#e30074' }}/>
      </Layout.Sider>
    );
  } else {
    return (
      <Layout.Sider collapsible collapsed={collapsed} onCollapse={newValue => setCollapsed(newValue)}>
        <div className="logo">
          <Link to="/"><Image width={50} src="./logo.png" preview={false}/></Link>
        </div>
        <Menu items={menuItems} mode="inline" theme="dark"/>
      </Layout.Sider>
    );
  }
}

function Content(props) {
  return (
    <Layout.Content>
      <div className="site-layout-background">
			<Switch>
				<Route path="/" key="home" exact={true}>
					<Home treeData={props.treeData} items={props.items} />
				</Route>
				<Route path="/vm" key="vm">
					<VM vms={props.vms} setVms={props.setVms} vmTypes={props.vmTypes} />
				</Route>
				<Route path="/container" key="container">
					<Container registries={props.registries} setRegistries={props.setRegistries} containers={props.containers} setContainers={props.setContainers} />
				</Route>
				<Route path="/catalog" key="catalog">
					<Catalog items={props.items} setItems={props.setItems} treeData={props.treeData} setTreeData={props.setTreeData} user={props.user} />
				</Route>
				<Route path="/kubernetes" key="kubernetes">
					<Kubernetes kubernetes={props.kubernetes} setKubernetes={props.setKubernetes} vmTypes={props.vmTypes} />
				</Route>
        <Route path="/rds" key="rds">
          <RDS rds={props.rds} setRds={props.setRds} vmTypes={props.vmTypes} />
        </Route>
        <Route path="/kafka" key="kafka">
          <Kafka kafka={props.kafka} setKafka={props.setKafka} vmTypes={props.vmTypes} />
        </Route>
        <Route path="/rancher" key="rancher">
          <Rancher rancher={props.rancher} setRancher={props.setRancher} vmTypes={props.vmTypes} />
        </Route>
        <Route path="/cassandra" key="cassandra">
          <Cassandra cassandra={props.cassandra} setCassandra={props.setCassandra} vmTypes={props.vmTypes} />
        </Route>
        <Route path="/activemq" key="activemq">
          <Activemq activemq={props.activemq} setActivemq={props.setActivemq} vmTypes={props.vmTypes} />
        </Route>
        <Route path="/pulsar" key="pulsar">
          <Pulsar pulsar={props.pulsar} setPulsar={props.setPulsar} vmTypes={props.vmTypes} />
        </Route>
        <Route path="/billing" key="billing">
          <Billing vms={props.vms} containers={props.containers} rds={props.rds} kafka={props.kafka} kubernetes={props.kubernetes} rancher={props.rancher} cassandra={props.cassandra} activemq={props.activemq} pulsar={props.pulsar} items={props.items} />
        </Route>
				<Route path="/monitoring" key="monitoring">
					<Monitoring vms={props.vms} rds={props.rds} kafka={props.kafka} kubernetes={props.kubernetes} rancher={props.rancher} cassandra={props.cassandra} activemq={props.activemq} pulsar={props.pulsar}  />
				</Route>
			</Switch>
      </div>
    </Layout.Content>
  );
}

export default function App() {
  const [ user, setUser ] = useState();
	const vmTypes = [
		{ label: 't2.nano', value: 't2.nano' },
		{ label: 't2.micro', value: 't2.micro' },
		{ label: 't2.small', value: 't2.small' },
		{ label: 't2.medium', value: 't2.medium' },
		{ label: 't2.large', value: 't2.large' },
		{ label: 't2.xlarge', value: 't2.xlarge' },
		{ label: 't2.2xlarge', value: 't2.2xlarge' },
		{ label: 't3.small', value: 't3.small' },
		{ label: 't3.medium', value: 't3.medium' },
		{ label: 't3.large', value: 't3.large' },
		{ label: 't3.xlarge', value: 't3.xlarge' },
		{ label: 't3.2xlarge', value: 't3.2xlarge' },
		{ label: 'c5.large', value: 'c5.large' },
		{	label: 'c5.xlarge', value: 'c5.xlarge' },
		{ label: 'c5.2xlarge', value: 'c5.2xlarge' },
		{ label: 'c5.4xlarge', value: 'c5.4xlarge' },
		{ label: 'c5.9xlarge', value: 'c5.9xlarge' },
		{ label: 'c5.12xlarge', value: 'c5.12xlarge' },
		{ label: 'c5.18xlarge', value: 'c5.18xlarge' },
		{ label: 'c5.24xlarge', value: 'c5.24xlarge' },
		{ label: 'c5.metal', value: 'c5.metal' },
		{ label: 'c5a.xlarge', value: 'c5a.xlarge' },
		{ label: 'c5a.2xlarge', value: 'c5a.2xlarge' }
	];
	const [vms, setVms] = useState([]);
	const [registries, setRegistries] = useState([]);
  const [containers, setContainers] = useState([]);
  const [rds, setRds] = useState([]);
  const [kafka, setKafka] = useState([]);
  const [kubernetes, setKubernetes] = useState([]);
  const [rancher, setRancher] = useState([]);
  const [cassandra, setCassandra] = useState([]);
  const [activemq, setActivemq] = useState([]);
  const [pulsar, setPulsar] = useState([]);
	const [items, setItems] = useState([ 
		{ label: 'Compute', key: 'compute', icon: <CloudOutlined/>, children : 
			[ { label: <Link to="/vm">VM</Link>, key: 'vm', starttime: '', icon: <CloudServerOutlined/> }, { label: <Link to="/container">Container</Link>, key: 'container', icon: <ContainerOutlined/> } ] },
		{ label: 'Database', key: 'database', icon: <DatabaseOutlined/>, children:
      [ { label: <Link to="/rds">Relational</Link>, key: 'rds', icon: <ConsoleSqlOutlined /> } ]
     },
    { label: 'Integration', key: 'integration', icon: <NodeIndexOutlined/>, children:
			[ { label: <Link to="/kafka">Kafka</Link>, key: 'kafka', icon: <Icon component={KafkaSvg} /> } ]
		 },
		{ label: <Link to="/catalog">Catalog</Link>, key: 'catalog', icon: <AppstoreOutlined/> },
		 ]);
	const initTreeData = [
		{
			title: <Space><CloudOutlined />Compute</Space>,
			key: 'compute',
      selectable: false,
      checkable: false,
			children: [
				{ title: <Space><Icon component={KubernetesSvg} />Kubernetes</Space>, key: 'kubernetes', },
				{ title: <Space><Icon component={RancherSvg} />Rancher</Space>, key: 'rancher' }
			],
		},
		{
			title: <Space><DatabaseOutlined />Database</Space>,
			key: 'database',
      selectable: false,
      checkable: false,
			children: [
				{ title: <Space><Icon component={CassandraSvg} />Cassandra</Space>, key: 'cassandra' },
			]
		},
		{
			title: <Space><NodeIndexOutlined />Integration</Space>,
			key: 'integration',
      selectable: false,
      checkable: false,
			children: [
				{ title: <Space><Icon component={ActivemqSvg} />ActiveMQ</Space>, key: 'activemq' }
			]
		}
	];
  const [treeData, setTreeData] = useState(initTreeData);
  const [ loginForm ] = Form.useForm();
  var userMenu = (
    <Menu items={[
      { key: 'billing', label: <Link to='/billing'><Space><BankOutlined />Billing Dashboard</Space></Link> },
      { key: 'signout', label: <Button type='primary' onClick={ () => setUser(null) }>Sign Out</Button> }
    ]} />
  );
  if (user === 'otc') {
    userMenu = (
      <Menu items={[
        { key: 'billing', label: <Link to='/billing'><Space><BankOutlined />Billing Dashboard</Space></Link> },
        { key: 'signout', label: <Button style={{ background: '#e30074' }} type='primary' onClick={ () => setUser(null) }>Sign Out</Button> }
      ]} />
    );
  }
  if (user === 'csp') {
    userMenu = (
      <Menu items={[
        { key: 'signout', label: <Button type='primary' onClick={ () => setUser(null) }>Sign Out</Button> }
      ]} />
    );
  }
  if (user) {
    return (
      <Layout className="layout" hasSider>
        <Router>
          <SideMenu user={user} items={items} />
          <Layout className="site-layout">
            <Layout.Header style={{ width: '100%', background: '#ffffff' }}>
              <div align="right"><Dropdown overlay={userMenu}><Space>John Doo ({user})<DownOutlined /></Space></Dropdown></div>
            </Layout.Header>
            <Content vms={vms} setVms={setVms} items={items} setItems={setItems} treeData={treeData} setTreeData={setTreeData} registries={registries} setRegistries={setRegistries} containers={containers} setContainers={setContainers} rds={rds} setRds={setRds} kafka={kafka} setKafka={setKafka} kubernetes={kubernetes} setKubernetes={setKubernetes} rancher={rancher} setRancher={setRancher} cassandra={cassandra} setCassandra={setCassandra} activemq={activemq} setActivemq={setActivemq} pulsar={pulsar} setPulsar={setPulsar} vmTypes={vmTypes} user={user} />
            <Layout.Footer>©2022 Eclipse Foundation - Open Services Cloud</Layout.Footer>
          </Layout>
        </Router>
      </Layout>
    );
  } else {
    return (
      <>
      <Modal centered mask={false} title={<Space><Image width={50} src="./logo.png" preview={false}/>Welcome to OSC Demo</Space>} open={true} okText="Log In" cancelText="Reset" onOk={loginForm.submit} onCancel={() => loginForm.resetFields()}>
        <Form name="login" form={loginForm} labelCol={{ span: 8 }} wrapperCol={{ span: 16 }} autoComplete="off" onFinish={(values) => {
          if (values.username !== 'csp' && values.username !== 'user' && values.username !== 'otc') {
            message.error("Please use valid user: csp, user");
          } else {
            setUser(values.username);
          }
        }} onKeyUp={(event) => {
          if (event.keyCode === 13) {
            loginForm.submit();
          }
        }}>
          <Form.Item name="username" label="Username" rules={[{ required: true, message: 'Please input a persona username!' }]}><Input placeholder="username" /></Form.Item>
          <Form.Item name="password" label="Password" rules={[{ required: true }]}><Input type="password" placeholder="password"/></Form.Item>
        </Form>
        <Space><i>You have three username depending of the persona you want to use:
          <ul>
            <li><b>csp</b> for cloud service provider admin user</li>
            <li><b>user</b> for OSC end user</li>
            <li><b>otc</b> for OTC end user</li>
          </ul>
        </i></Space>
      </Modal>
      </>
    );
  }
}
