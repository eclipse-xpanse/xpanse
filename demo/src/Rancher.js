import { useState } from 'react';
import { Breadcrumb, Row, Col, Input, Space, Button, message, Table, Modal, Form, Select } from 'antd';
import { HomeOutlined, CloudOutlined } from '@ant-design/icons';
import Icon from '@ant-design/icons';
import { Link } from 'react-router-dom';
import { ReactComponent as RancherSvg } from './rancher_icon.svg';

const { Search } = Input;


function Rancher(props) {
  const columns = [
    { title: 'Manager Name', dataIndex: 'name', key: 'name' },
    { title: 'Rancher Version', dataIndex: 'version', key: 'version' },
    { title: 'Instance Type', dataIndex: 'type', key: 'type' },
    { title: 'Status', dataIndex: 'status', key: 'status' }
  ];
  const [ isWindowOpen, setIsWindowOpen ] = useState(false);
  const [ form ] = Form.useForm();
  const formLayout = {
    labelCol: { span: 8 },
    wrapperCol: { span: 16 }
  };
  const [ selected, setSelected ] = useState({});
  const rowSelection = {
    onChange: (selectedRowKeys, selectedRows) => {
      setSelected(selectedRowKeys);
    } 
  };
  return (
    <>
    <Breadcrumb>
      <Breadcrumb.Item><Link to={'/'}><HomeOutlined /><span>Home</span></Link></Breadcrumb.Item>
      <Breadcrumb.Item><CloudOutlined /><span>Compute</span></Breadcrumb.Item>
      <Breadcrumb.Item><Link to={'/rancher'}><Icon component={RancherSvg} /><span>Rancher</span></Link></Breadcrumb.Item>
    </Breadcrumb>
    <h2>Suse Rancher Managers</h2>
    <Row gutter={24}>
      <Col span={6}><Search allowClear style={{ style: 100 }} onSearch={() => message.error("Hey, it's a demo ;)") } /></Col>
      <Col span={6} push={6}><Space>
        <Button type="default" disabled={selected.length < 1} onClick={() => {
          var copy = props.rancher.slice();
          selected.forEach((item) => {
            copy = copy.filter(i => i.key !== item);
            message.info("Rancher manager " + item + " deleted");
          }); 
          props.setRancher(copy);
          setSelected([]);
        }}>Delete</Button>
        <Button type="primary" onClick={() => setIsWindowOpen(true)}>Create Rancher manager</Button>
      </Space></Col>
    </Row>
    <Row>
      <Col span={24}>
        <Table bordered columns={columns} dataSource={props.rancher} rowSelection={rowSelection} />
      </Col>
    </Row>
    <Modal title="Create Rancher manager" okText="Create" open={isWindowOpen} onCancel={() => setIsWindowOpen(false)} footer={[ <Space><Button onClick={() => setIsWindowOpen(false)}>Cancel</Button><Button type="primary" onClick={form.submit}>Create</Button></Space> ]}>
      <Form {...formLayout} form={form} name="rancher-form" onFinish={(values) => {
				var found = false;
				props.rancher.forEach((ranch) => {
					if (ranch.key === values.name) {
						found = true;
					}
				});
				if (found) {
					message.error("Rancher manager " + values.name + " already exists");
				} else {
					const newManager = [ { key: values.name, name: values.name, version: values.version, type: values.type, status: 'Running' } ];
					const newRancher = props.rancher.concat(newManager);
					props.setRancher(newRancher);
					message.success("Rancher manager " + values.name + " created");
					setIsWindowOpen(false);
				}
      }}>
        <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input allowClear placeholder={'e.g. my-manager'} /></Form.Item>
        <Form.Item name="version" label="Rancher Version" rules={[{ required: true }]} initialValue={'2.6'}>
          <Select allowClear>
            <Select.Option key="2.6" value="2.6">2.6</Select.Option>
            <Select.Option key="2.5" value="2.5">2.5</Select.Option>
            <Select.Option key="2.4" value="2.4">2.4</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="type" label="Instance Type" rules={[{ required: true }]} initialValue={'t1.micro'}>
          <Select allowClear options={props.vmTypes} />
        </Form.Item>
      </Form>      
    </Modal>
    </>
  );
}
export default Rancher;
