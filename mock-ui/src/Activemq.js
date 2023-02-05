import { useState } from 'react';
import { Breadcrumb, Row, Col, Input, message, Space, Button, Table, Form, Modal, Select, InputNumber } from 'antd';
import { HomeOutlined, NodeIndexOutlined } from '@ant-design/icons';
import Icon from '@ant-design/icons';
import { Link } from 'react-router-dom';
import { ReactComponent as ActivemqSvg } from './activemq_icon.svg';

function Activemq(props) {
  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Topology', dataIndex: 'topology', key: 'topology' },
    { title: 'Instances Count', dataIndex: 'count', key: 'count' },
    { title: 'Instances Type', dataIndex: 'type', key: 'type' }
  ];
  const [isWindowOpen, setIsWindowOpen] = useState(false);
  const formLayout = {
    labelCol: { span: 8 },
    wrapperCol: { span: 16 }
  };
  const [ form ] = Form.useForm();
  const [ selected, setSelected ] = useState([]);
  const rowSelection = {
    onChange: (selectedRowKeys, selectedRows) => {
      setSelected(selectedRowKeys);
    }
  };
  return (
    <>
    <Breadcrumb>
      <Breadcrumb.Item><Link to={'/'}><HomeOutlined /><span>Home</span></Link></Breadcrumb.Item>
      <Breadcrumb.Item><NodeIndexOutlined /><span>Integration</span></Breadcrumb.Item>
      <Breadcrumb.Item><Link to={'/activemq'}><Icon component={ ActivemqSvg } /><span>ActiveMQ</span></Link></Breadcrumb.Item>
    </Breadcrumb>
    <h2>Apache ActiveMQ brokers</h2>
    <Row gutter={24}>
      <Col span={6}><Input.Search allowClear style={{ style: 100 }} onSearch={() => message.error("Hey, it's a demo ;)") } /></Col>
      <Col span={6} push={6}><Space>
        <Button type="default" disabled={selected.length < 1} onClick={() => {
          var copy = props.activemq.slice();
          selected.forEach((item) => {
            copy = copy.filter((i) => (i.key !== item));
            message.info("ActiveMQ broker " + item + " deleted");
          });
          props.setActivemq(copy);
          setSelected([]);
        }}>Delete</Button>
        <Button type="primary" onClick={() => setIsWindowOpen(true)}>Create ActiveMQ broker</Button>
      </Space></Col>
    </Row>
    <Row>
      <Col span={24}>
        <Table bordered columns={columns} dataSource={props.activemq} rowSelection={rowSelection} />
      </Col>
    </Row>
    <Modal title="Create ActiveMQ broker" okText="Create" open={isWindowOpen} onCancel={() => setIsWindowOpen(false) } footer={[ <Space><Button onClick={() => setIsWindowOpen(false) }>Cancel</Button><Button type="primary" onClick={form.submit}>Create</Button></Space> ]}>
      <Form {...formLayout} form={form} name="activemq-form" onFinish={(values) => {
				var found = false;
				props.activemq.forEach((amq) => {
					if (amq.key === values.name) {
						found = true;
					}
				});
				if (found) {
					message.error("ActiveMQ broker " + values.name + " already exists");
				} else {
					const newBroker = [ { key: values.name, name: values.name, topology: values.topology, count: values.count, type: values.type, status: 'Running' } ];
					const newActivemq = props.activemq.concat(newBroker);
					props.setActivemq(newActivemq);
					message.success("ActiveMQ broker " + values.name + " created");
					setIsWindowOpen(false);
				}
      }}> 
        <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input allowClear placeholder={'e.g. my-amq-broker'} /></Form.Item>
        <Form.Item name="topology" label="Topology" rules={[{ required: true }]} initialValue={'single'}>
          <Select allowClear>
            <Select.Option key="single" value="single">Single</Select.Option>
            <Select.Option key="master-slave" value="master-slave">Master Slave</Select.Option>
            <Select.Option key="network-of-brokers" value="network-of-brokers">Network of Brokers</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="count" label="Instances Count" rules={[{ required: true }]} initialValue={3}>
          <InputNumber min={1} allowClear />
        </Form.Item>
        <Form.Item name="type" label="Instances Type" rules={[{ required: true }]} initialValue={'t1.micro'}>
          <Select allowClear options={props.vmTypes} />
        </Form.Item>
      </Form>
    </Modal>
    </>
  );
}
export default Activemq;
