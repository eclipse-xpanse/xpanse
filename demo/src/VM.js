import { message, Breadcrumb, Select, Form, Modal, Row, Col, Input, Space, Button, Table } from 'antd';
import { HomeOutlined, CloudOutlined, CloudServerOutlined } from '@ant-design/icons';
import { useState } from 'react';
import { Link } from 'react-router-dom';

const { Option } = Select;
const { Search } = Input;

function VM(props) {
	const [ isInstanceOpen, setIsInstanceOpen ] = useState(false);
	const vmColumns = [
		{ title: 'Name', dataIndex: 'name', key: 'name' },
		{ title: 'OS', dataIndex: 'os', key: 'os' },
		{ title: 'Instance Type', dataIndex: 'type', key: 'type' },
		{ title: 'Status', dataIndex: 'status', key: 'status' }
	];
	const formLayout = {
		labelCol: { span: 8 },
		wrapperCol: { span: 16 }
	};
	const [ form ] = Form.useForm();
	const [ select, setSelect ] = useState([]);
	const rowSelection = {
		onChange: (selectedRowKeys, selectedRows) => {
      setSelect(selectedRowKeys);
		}
	};
  return (
		<>
		<Breadcrumb>
			<Breadcrumb.Item><Link to={'/'}><HomeOutlined/><span>Home</span></Link></Breadcrumb.Item>
			<Breadcrumb.Item><CloudOutlined/><span>Compute</span></Breadcrumb.Item>
			<Breadcrumb.Item><Link to={'/vm'}><CloudServerOutlined/><span>VM</span></Link></Breadcrumb.Item>
		</Breadcrumb>
		<h2>Virtual Machines</h2>
		<Row gutter={24}>
			<Col span={6}><Search allowClear style={{ style: 100 }} onSearch={() => message.error("Hey, it's a demo ;)") }/></Col>
			<Col span={6} push={6}><Space>
				<Button type="default" disabled={select.length < 1} onClick={() => {
					var copy = props.vms.slice();
					select.forEach((row) => {
						for (var i = 0; i < copy.length; i++) {
							if (copy[i].key === row) {
								var copiedVm = Object.assign({}, copy[i]);
								copiedVm.status = 'Stopped';
								copy[i] = copiedVm;
							}
						}
						message.info("VM " + row + " stopped");
					});
					props.setVms(copy);
				}}>Stop</Button>
				<Button type="default" disabled={select.length < 1} onClick={() => {
					var copy = props.vms.slice();
					select.forEach((row) => {
						for (var i = 0; i < copy.length; i++) {
							var copiedVm = Object.assign({}, copy[i]);
							copiedVm.status = 'Started';
							copy[i] = copiedVm;	
						}
						message.info("VM " + row + " started");
					});
					props.setVms(copy);
				}}>Start</Button>
				<Button type="default" disabled={select < 1} onClick={() => {
					var copy = props.vms.slice();
					select.forEach((item) => {
						copy = copy.filter(i => i.key !== item);
						message.info("VM " + item + " deleted");
					});
					props.setVms(copy);
					setSelect([]);
				}}>Delete</Button>
				<Button type="primary" onClick={() => { setIsInstanceOpen(true) }}>Launch instance</Button>
			</Space></Col>
		</Row>
		<Row>
			<Col span={24}>
				<Table bordered columns={vmColumns} dataSource={props.vms} rowSelection={rowSelection} />
			</Col>
		</Row>
		<Modal title="Launch instance" okText="Launch" open={isInstanceOpen} onCancel={() => setIsInstanceOpen(false) } footer={[ <Space><Button onClick={() =>setIsInstanceOpen(false) }>Cancel</Button><Button onClick={form.submit} type="primary">Launch</Button></Space> ]} >
			<Form {...formLayout} form={form} name="instance-form" onFinish={(values) => {
				var found = false;
				props.vms.forEach((vm) => {
					if (vm.key === values.name) {
						found = true;
					}
				});
				if (found) {
					message.error("VM "+ values.name + " already exists");
				} else {
					const newVm =[ { key: values.name, name: values.name, os: values.os, type: values.type, status: 'Started' } ];
					const newVms = props.vms.concat(newVm);
					props.setVms(newVms);
					message.success("Instance " + values.name + " launched");	
					setIsInstanceOpen(false);
				}
				}}>
				<Form.Item name="name" label="Name" rules={[{ required: true }]}><Input allowClear placeholder={'e.g. My Web Service'}/></Form.Item>
				<Form.Item name="os" label="Instance OS" rules={[{ required: true }]} initialValue={'ubuntu-x86'}>
					<Select allowClear>
						<Option key="ubuntu-x86" value="ubuntu-x86">Ubuntu 64-bit (x86)</Option>
						<Option key="ubuntu-arm" value="ubuntu-arm">Ubuntu 64-bit (Arm)</Option>
						<Option key="redhat-x86" value="redhat-x86">Red Hat 64-bit (x86)</Option>
						<Option key="redhat-arm" value="redhat-arm">Red Hat 64-bit (Arm)</Option>
						<Option key="suse-x86" value="suse-x86">SUSE 64-bit (x86)</Option>
						<Option key="suse-arm" value="suse-arm">SUSE 64-bit (Arm)</Option>
					</Select>
				</Form.Item>
				<Form.Item name="type" label="Instance Type" rules={[{ required: true }]} initialValue={'t1.micro'}>
					<Select allowClear options={props.vmTypes}/>
				</Form.Item>
			</Form>
		</Modal>
		</>
  );
}

export default VM;
