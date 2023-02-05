import { useState } from 'react';
import { Breadcrumb, Row, Col, Input, message, Space, Button, Table, Modal, Form, InputNumber, Select } from 'antd';
import { HomeOutlined, NodeIndexOutlined } from '@ant-design/icons';
import Icon from '@ant-design/icons';
import { ReactComponent as PulsarSvg } from './pulsar_icon.svg';
import { Link } from 'react-router-dom';

function Pulsar(props) {
  const columns = [ 
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Zookeeper Count', dataIndex: 'zk', key: 'zk' },
    { title: 'Bookeeper Count', dataIndex: 'bk', key: 'bk' },
    { title: 'Pulsar Broker Count', dataIndex: 'count', key: 'count' },
    { title: 'Instances Type', dataIndex: 'type', key: 'type' }
  ];
  const [ selected, setSelected ] = useState([]);
  const rowSelection = {
    onChange: (selectedRowKeys, selectedRows) => {
      setSelected(selectedRowKeys);
    }
  };
  const [ isWindowOpen, setIsWindowOpen ] = useState(false);
  const [ form ] = Form.useForm();
  const formLayout = {
    labelCol: { span: 8 },
    wrapperCol: { span: 16 }
  };
  return (
    <>
    <Breadcrumb>
      <Breadcrumb.Item><Link to={'/'}><HomeOutlined /><span>Home</span></Link></Breadcrumb.Item>
      <Breadcrumb.Item><NodeIndexOutlined /><span>Integration</span></Breadcrumb.Item>
      <Breadcrumb.Item><Link to={'/pulsar'}><Icon component={ PulsarSvg } /><span>Pulsar</span></Link></Breadcrumb.Item>
    </Breadcrumb>
    <h2>Apache Pulsar Brokers</h2>
    <Row gutter={24}>
      <Col span={6}><Input.Search allowClear style={{ style: 100 }} onSearch={() => message.error("Hey, it's a demo ;)") } /></Col>
      <Col span={6} push={6}><Space>
        <Button type="default" disabled={selected.length < 1} onClick={() => {
          var copy = props.pulsar.slice();
          selected.forEach((item) => {
            copy = copy.filter(i => i.key !== item);
            message.info("Pulsar broker " + item + " deleted");
          });
          props.setPulsar(copy);
          setSelected([]);
        }}>Delete</Button>
        <Button type="primary" onClick={() => setIsWindowOpen(true)}>Create Pulsar broker</Button>
      </Space></Col>
    </Row>
    <Row>
      <Col span={24}>
        <Table bordered columns={columns} dataSource={props.pulsar} rowSelection={rowSelection} />
      </Col>
    </Row>
    <Modal title="Create Pulsar broker" okText="Create" open={isWindowOpen} onCancel={() => setIsWindowOpen(false) } footer={[ <Space><Button onClick={() => setIsWindowOpen(false) }>Cancel</Button><Button type="primary" onClick={form.submit}>Create</Button></Space> ]}>
      <Form {...formLayout} form={form} name="pulsar-form" onFinish={(values) => {
				var found = false;
				props.pulsar.forEach((pul) => {
					if (pul.key === values.name) {
						found = true;
					}
				});
				if (found) {
					message.error("Pulsar broker " + values.name + " already exists");
				} else {
					const newBroker = [ { key: values.name, name: values.name, zk: values.zk, bk: values.bk, count: values.count, type: values.type, status: 'Running' } ];
					const newPulsar = props.pulsar.concat(newBroker);
					props.setPulsar(newPulsar);
					message.success("Pulsar broker " + values.name + " created");
					setIsWindowOpen(false);
				}
      }}>
        <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input allowClear placeholder={'e.g. my-pulsar-broker'} /></Form.Item>
        <Form.Item name="zk" label="Zookeeper Count" rules={[{ required: true }]} initialValue={3}><InputNumber min={1} /></Form.Item>      
        <Form.Item name="bk" label="Bookeeper Count" rules={[{ required: true }]} initialValue={3}><InputNumber min={1} /></Form.Item>
        <Form.Item name="count" label="Pulsar Broker Count" rules={[{ required: true }]} initialValue={3}><InputNumber min={1} /></Form.Item>
        <Form.Item name="type" label="Instances Type" rules={[{ required: true }]} initialValue={'t1.micro'}>
          <Select allowClear options={props.vmTypes} />
        </Form.Item>
      </Form>
    </Modal>
    </>
  );
}
export default Pulsar;
