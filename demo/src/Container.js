import { message, InputNumber, Form, Modal, Table, Breadcrumb, Space, Divider, Input, Row, Col, Button } from 'antd';
import { HomeOutlined, CloudOutlined, ContainerOutlined } from '@ant-design/icons';
import { Link } from 'react-router-dom';
import { useState } from 'react';

const { Search } = Input;

function Container(props) {
	const [ isRegistryOpen, setIsRegistryOpen ] = useState(false);
	const [ isContainerOpen, setIsContainerOpen ] = useState(false);
	const registryColumns = [ 
		{ title: '', dataIndex: 'radio', key: 'radio' },
		{ title: 'Name', dataIndex: 'name', key: 'name' },
		{ title: 'URI', dataIndex: 'uri', key: 'uri' },
		{ title: 'Created', dataIndex: 'created', key: 'created' },
		{ title: 'Scan Frequency', dataIndex: 'frequency', key: 'frequency' }
	];
	const containerColumns = [
		{ title: '', dataIndex: 'radio', key: 'radio' },
		{ title: 'Name', dataIndex: 'name', key: 'name' },
		{ title: 'Image', dataIndex: 'image', key: 'image' },
	  { title: 'Status', dataIndex: 'status', key: 'status' }	
	];
  const [ registrySelected, setRegistrySelected ] = useState([]);
	const registryRowSelection = {
		onChange: (selectedRowKeys, selectedRows) => {
      setRegistrySelected(selectedRowKeys);
		}
	};
  const [ containerSelected, setContainerSelected ] = useState([]);
  const containerRowSelection = {
    onChange: (selectedRowKeys, selectedRows) => {
      setContainerSelected(selectedRowKeys);
    }
  };
	const formLayout = {
		labelCol: { span: 8 },
		wrapperCol: { span: 16 }
	};
	const [ registryForm ] = Form.useForm();
	const [ containerForm ] = Form.useForm();
	return (
		<>
		<Breadcrumb>
			<Breadcrumb.Item><Link to={'/'}><HomeOutlined /><span>Home</span></Link></Breadcrumb.Item>
			<Breadcrumb.Item><CloudOutlined/><span>Compute</span></Breadcrumb.Item>
			<Breadcrumb.Item><Link to={'/container'}><ContainerOutlined/><span>Container</span></Link></Breadcrumb.Item>
		</Breadcrumb>
		<h2>Container</h2>
		<Divider orientation="left">Registries</Divider>
		<Row gutter={24}>
			<Col span={6}><Search allowClear style={{ style: 100 }} onSearch={() => { message.error("Hey, it's a demo ;)") }} /></Col>
			<Col span={6} push={2}><Space>
				<Button type="default" disabled={registrySelected < 1} onClick={() => {
          var copy = props.registries.slice();
          registrySelected.forEach((item) => {
            copy = copy.filter(i => i.key !== item);
            message.info("Container registry " + item + " deleted");
          });
          props.setRegistries(copy);
          setRegistrySelected([]);
        }}>Delete</Button>
				<Button type="primary" onClick={() => { setIsRegistryOpen(true) }}>Create repository</Button>
			</Space></Col>
		</Row>
		<Row>
			<Col span={24}>
				<Table columns={registryColumns} dataSource={props.registries} rowSelection={registryRowSelection} />
			</Col>
		</Row>	
		<Divider orientation="left">Containers</Divider>
		<Row gutter={24}>
			<Col span={6}><Search allowClear style={{ style: 100 }} onSearch={() => { message.error("Hey, it's a demo ;)") }}/></Col>
			<Col span={6} push={2} ><Space>
        <Button type="default" disabled={containerSelected < 1} onClick={() => {
          var copy = props.containers.slice();
          containerSelected.forEach((row) => {
            for (var i = 0; i < copy.length; i++) {
              var copiedContainer = Object.assign({}, copy[i]);
              copiedContainer.status = 'Stopped';
              copy[i] = copiedContainer;
            }
            message.info("Container " + row + " stopped");
          });
          props.setContainers(copy);
        }}>Stop</Button>
        <Button type="default" disabled={containerSelected < 1} onClick={() => {
          var copy = props.containers.slice();
          containerSelected.forEach((row) => {
            for (var i = 0; i < copy.length; i++) {
              var copiedContainer = Object.assign({}, copy[i]);
              copiedContainer.status = 'Running';
              copy[i] = copiedContainer;
            }
            message.info("Container " + row + " started");
          });
          props.setContainers(copy);
        }}>Start</Button>
        <Button type="default" disabled={containerSelected < 1} onClick={() => {
          var copy = props.containers.slice();
          containerSelected.forEach((item) => {
            copy = copy.filter(i => i.key !== item);
            message.info("Container " + item + " deleted");
          });
          props.setContainers(copy);
          setContainerSelected([]);
        }}>Delete</Button>
        <Button type="primary" onClick={() => { setIsContainerOpen(true) }}>Create container</Button></Space></Col>
		</Row>
		<Row>
			<Col span={24}>
				<Table columns={containerColumns} dataSource={props.containers} rowSelection={containerRowSelection} />
			</Col>
		</Row>
		<Modal title="Container registry" okText="Create" open={isRegistryOpen} onCancel={() => setIsRegistryOpen(false) } footer={[ <Space><Button onClick={() => setIsRegistryOpen(false) }>Cancel</Button><Button onClick={registryForm.submit} type="primary">Create</Button></Space> ]} >
			<Form {...formLayout} form={registryForm} name="registry-form" onFinish={(values) => {
				var found = false;
				props.registries.forEach((container) => {
					if (container.key === values.name) {
						found = true;
					}
				});
				if (found) {
					message.error("Container registry " + values.name + " already exists");
				} else {
					const date = new Date().toISOString();
					if (!values.frequency) { values.frequency = 60 }
					const newRegistry = [ { key: values.name, name: values.name, uri: values.uri, created: date, frequency: values.frequency } ];
					const newRegistries = props.registries.concat(newRegistry);
					props.setRegistries(newRegistries);
					message.success("Registry " + values.name + " created");
					setIsRegistryOpen(false);
				}
			}}>
				<Form.Item name="name" label="Name" rules={[{ required: true }]}><Input allowClear placeholder={'e.g. my-registry'}/></Form.Item>
				<Form.Item name="uri" label="URI" rules={[{ required: true }]}><Input allowClear placeholder={'osc:/registry/' + registryForm.name}/></Form.Item>
				<Form.Item name="created" label="Created" initialValue={ new Date().toISOString() }><Input disabled={true} placeholder={ new Date().toISOString() } /></Form.Item>
				<Form.Item name="frequency" label="Scan Frequency" initialValue={60}><InputNumber min={1} /></Form.Item>
			</Form>
		</Modal>
		<Modal title="Container" okText="Start" open={isContainerOpen} onCancel={() => setIsContainerOpen(false) } footer={[ <Space><Button onClick={() => setIsContainerOpen(false) }>Cancel</Button><Button onClick={containerForm.submit} type="primary">Start</Button></Space> ]}>
			<Form {...formLayout} form={containerForm} name="container-form" onFinish={(values) => {
				var found = false;
				props.containers.forEach((container) => {
					if (container.key === values.name) {
						found = true;
					}
				});
				if (found) {
					message.error("Container " + values.name + " already exists");
				} else {
					const newContainer = [ { key: values.name, name: values.name, image: values.image, status: 'Running' } ];
					const newContainers = props.containers.concat(newContainer);
					props.setContainers(newContainers);
					message.success("Container " + values.name + " started");
					setIsContainerOpen(false);
				}
			}}>
				<Form.Item name="name" label="Name" rules={[{ required: true }]}><Input allowClear/></Form.Item>
				<Form.Item name="image" label="Image" rules={[{ required: true }]}><Input allowClear/></Form.Item>
			</Form>
		</Modal>
		</>
	);
}

export default Container;
