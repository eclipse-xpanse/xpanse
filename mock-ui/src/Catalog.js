import { useState } from 'react';
import { Link } from 'react-router-dom';
import { message, Row, Col, Input, Divider, Space, Breadcrumb, Tree, Button, Descriptions, Badge } from 'antd';
import Icon from '@ant-design/icons';
import { SettingOutlined, CloudOutlined, DatabaseOutlined, NodeIndexOutlined, UpCircleOutlined, HomeOutlined, AppstoreOutlined, PlayCircleOutlined } from '@ant-design/icons';
import { ReactComponent as KubernetesSvg } from './kubernetes_icon.svg';
import { ReactComponent as RancherSvg } from './rancher_icon.svg';
import { ReactComponent as CassandraSvg } from './cassandra_icon.svg';
import { ReactComponent as ActivemqSvg } from './activemq_icon.svg';
import { ReactComponent as PulsarSvg } from './pulsar_icon.svg';

function Property(props) {
  if (!props.service) {
    return;
  }
  var badge = <Badge status="default" text="Registered" />;
  var title;
  var category;
  var provider;
  var regularAmount;
  var discountAmount;
  var amount;
  var registerTime;
  var starttime;
  if (props.service[0] === 'kubernetes') {
    props.items[0].children.forEach((service) => {
      if (service.key === 'kubernetes') {
        badge = <Badge status="success" text="Deployed" />
        starttime = service.starttime.date;
      }
    });
    title = "Kubernetes Service";
    category = "Compute";
    provider = "CNCF";
    regularAmount = "$80.00";
    discountAmount = "$20.00";
    amount = "$60.00";
    registerTime = "2022-09-21T10:18:42.208Z";
  }
  if (props.service[0] === 'rancher') {
    props.items[0].children.forEach((service) => {
      if (service.key === 'rancher') {
        badge = <Badge status='success' text="Deployed" />
        starttime = service.starttime.date;
      }
    });
    title = "Rancher Service";
    category = "Compute";
    provider = "SUSE";
    regularAmount = "$100.00";
    discountAmount = "$00.00";
    amount = "$100.00";
    registerTime = "2022-09-19T13:48:42.208Z";
  }
  if (props.service[0] === 'cassandra') {
    props.items[1].children.forEach((service) => {
      if (service.key === 'cassandra') {
        badge = <Badge status='success' text='Deployed'/>
        starttime = service.starttime.date;
      }
    }); 
    title = "Cassandra Service";
    category = "Database";
    provider = "DataStax";
    regularAmount = "$66.00";
    discountAmount = "$00.00";
    amount = "$66.00";
    registerTime = "2022-08-16T17:25:15.208Z";
  }
  if (props.service[0] === 'activemq') {
    props.items[2].children.forEach((service) => {
      if (service.key === 'activemq') {
        badge = <Badge status='success' text='Deployed' />
        starttime = service.starttime.date;
      }
    });
    title = "ActiveMQ Service";
    category = "Integration";
    provider = "The ASF";
    regularAmount = "$55.00";
    discountAmount = "$00.00";
    amount = "$55.00";
    registerTime = "2022-08-26T08:25:15.208Z";
  }
  if (props.service[0] === 'pulsar') {
    props.items[2].children.forEach((service) => {
      if (service.key === 'pulsar') {
        badge = <Badge status='success' text='Deployed' />
        starttime = service.starttime.date;
      }
    });
    title = "Pulsar Service";
    category = "Integration";
    provider = "StreamNative";
    regularAmount = "$44.00";
    discountAmount = "$00.00";
    amount = "$44.00";
    props.treeData.forEach((category) => {
      if (category.key === 'integration') {
        category.children.forEach((service) => {
          if (service.key === 'pulsar') {
            registerTime = service.registerTime.date;
          }
        });
      }
    });
  }
  return (
    <Descriptions title={title} column={1} bordered>
      <Descriptions.Item label="Category">{category}</Descriptions.Item>
      <Descriptions.Item label="Provider">{provider}</Descriptions.Item>
      <Descriptions.Item label="Zone">eu-fr-west-01</Descriptions.Item>
      <Descriptions.Item label="Billing Mode">Monthly Per Service Instance</Descriptions.Item>
      <Descriptions.Item label="Regular Pricing">{regularAmount}</Descriptions.Item>
      <Descriptions.Item label="Discount">{discountAmount}</Descriptions.Item>
      <Descriptions.Item label="Amount">{amount}</Descriptions.Item>
      <Descriptions.Item label="Register Time">{registerTime}</Descriptions.Item>
      <Descriptions.Item label="Start Time">{starttime}</Descriptions.Item>
      <Descriptions.Item label="Status">{badge}</Descriptions.Item>
    </Descriptions>
  );
}

function handleStartClick(service, setItems, items) {
  if (!service) {
    return;
  }

  const date = new Date().toISOString();
	service.forEach((service) => {
		if (service.toString() === "kubernetes") {
			var kubernetesFound = false;
			items[0].children.forEach((item) => {
				if (item.key === 'kubernetes') {
					kubernetesFound = true;
				}
			});

			if (kubernetesFound) {
				message.error(service + " already started");
			} else {
				message.info("Deploying " + service + " service");
				const compute = items[0].children;
				const kubernetes = [ { label: <Link to="/kubernetes">Kubernetes</Link>, key: 'kubernetes', starttime: {date}, icon: <Icon component={KubernetesSvg} /> } ];
				const newCompute = compute.concat(kubernetes);
				var kubernetesCopy = items.slice();
				kubernetesCopy[0].children = newCompute;
				setItems(kubernetesCopy);
				message.success(service + " service started");
			}
		}
		if (service.toString() === "rancher") {
			var rancherFound = false;
			items[0].children.forEach((item) => {
				if (item.key === 'rancher') {
					rancherFound = true
				}
			});

			if (rancherFound) {
				message.error(service + " already started");
			} else {
				message.info("Deploying " + service + " service");
				const compute = items[0].children;
				const rancher = [ { label: <Link to="/rancher">Rancher</Link>, key: 'rancher', starttime: {date}, icon: <Icon component={RancherSvg} /> } ];
				const newCompute = compute.concat(rancher);
				var rancherCopy = items.slice();
				rancherCopy[0].children = newCompute;
				setItems(rancherCopy);
				message.success(service + " service started");
			}
		}
		if (service.toString() === "cassandra") {
			var cassandraFound = false;
			items[1].children.forEach((item) => {
				if (item.key === 'cassandra') {
					cassandraFound = true;
				}
			});
			if (cassandraFound) {
				message.error(service + " already started");
			} else {
				message.info("Deploying " + service + " service");
				const database = items[1].children;
				const cassandra = [ { label: <Link to="/cassandra">Cassandra</Link>, key: 'cassandra', starttime: {date}, icon: <Icon component={CassandraSvg} /> } ];
				const newDatabase = database.concat(cassandra);
				var cassandraCopy = items.slice();
				cassandraCopy[1].children = newDatabase;
				setItems(cassandraCopy);
				message.success(service + " service started");
			}
		}
		if (service.toString() === "activemq") {
			var activemqFound = false;
			items[2].children.forEach((item) => {
				if (item.key === 'activemq') {
					activemqFound = true;
				}
			});
			if (activemqFound) {
				message.error(service + " already started");
			} else {
				message.info("Deploying " + service + " service");
				const integration = items[2].children;
				const activemq = [ { label: <Link to="/activemq">ActiveMQ</Link>, key: 'activemq', starttime: {date}, icon: <Icon component={ActivemqSvg} /> } ];
				const newIntegration = integration.concat(activemq);
				var activemqCopy = items.slice();
				activemqCopy[2].children = newIntegration;
				setItems(activemqCopy);
				message.success(service + " service started");
			}
		}
		if (service.toString() === "pulsar") {
			var pulsarFound = false;
			items[2].children.forEach((item) => {
				if (item.key === 'pulsar') {
					pulsarFound = true;
				}
			});
			if (pulsarFound) {
				message.error(service + " already started");
			} else {
				message.info("Deploying " + service + " service");
				const integration = items[2].children;
				const pulsar = [ { label: <Link to="/pulsar">Pulsar</Link>, key: 'pulsar', starttime: {date}, icon: <Icon component={PulsarSvg} /> } ];
				const newIntegration = integration.concat(pulsar);
				var pulsarCopy = items.slice();
				pulsarCopy[2].children = newIntegration;
				setItems(pulsarCopy);
				message.success(service + " service started");
			}
		}
	});

}

function handleRegisterClick(setTreeData) {
  message.info("Registering pulsar service");
  const date = new Date().toISOString();
	const newTreeData = [
		{
			title: <Space><CloudOutlined />Compute</Space>,
			key: 'compute',
      checkable: false,
      selectable: false,
			children: [
				{ title: <Space><Icon component={KubernetesSvg} />Kubernetes</Space>, key: 'kubernetes', },
				{ title: <Space><Icon component={RancherSvg} />Rancher</Space>, key: 'rancher' }
			],
		},
		{
			title: <Space><DatabaseOutlined />Database</Space>,
			key: 'database',
      checkable: false,
      selectable: false,
			children: [
				{ title: <Space><Icon component={CassandraSvg} />Cassandra</Space>, key: 'cassandra' },
			]
		},
		{
			title: <Space><NodeIndexOutlined />Integration</Space>,
			key: 'integration',
      checkable: false,
      selectable: false,
			children: [
				{ title: <Space><Icon component={ActivemqSvg} />ActiveMQ</Space>, key: 'activemq' },
        { title: <Space><Icon component={PulsarSvg} />Pulsar</Space>, key: 'pulsar', registerTime: {date} }
			]
		}
	];
  setTreeData(newTreeData);
  message.success('Pulsar service registered');
}

function Catalog(props) {
	const [service, setService] = useState();
  const [serviceToInstall, setServiceToInstall] = useState();
  if (props.user === 'csp') {
    return (
      <>
      <Breadcrumb><Breadcrumb.Item href="/"><HomeOutlined /></Breadcrumb.Item><Breadcrumb.Item href="/catalog"><AppstoreOutlined /><span>Catalog</span></Breadcrumb.Item></Breadcrumb>
      <h2>Services Catalog <small>(powered by Open Services Cloud)</small></h2>
      <Divider orientation="left">Deploy</Divider>
      <Row>
        <Col span={4}>
          <Tree checkable defaultExpandAll={true} treeData={props.treeData} onSelect={(selectedKeys, info) => { setService(selectedKeys) }} onCheck={(checkedKeys, info) => { setServiceToInstall(checkedKeys) }}/>
        </Col>
        <Col span={18} push={2}>
          <Property service={service} items={props.items} treeData={props.treeData}/>
        </Col>
      </Row>
      <Row>
        <Col span={24}>
          <Button type="primary" icon={<PlayCircleOutlined/>} onClick={() => handleStartClick(serviceToInstall, props.setItems, props.items)}>Deploy</Button>
        </Col>
      </Row>
      <Divider orientation="left">Register</Divider>
      <Row>
        <Col span={18}>
          <Input addonAfter={<SettingOutlined/>} disabled={false} defaultValue="https://raw.githubusercontent.com/huaweicloud/osc/6ee7f2ade1445b9a502a18cc8a504d0eefac66ed/demo/src/pulsar.json" />
        </Col>
        <Col span={6}>
          <Button type="primary" icon={<UpCircleOutlined/>} onClick={() => handleRegisterClick(props.setTreeData)}>Fetch & Register</Button>
        </Col>
      </Row>
      </>
    );
  }
}

export default Catalog;
