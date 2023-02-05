import { useState } from 'react';
import { Breadcrumb, Row, Col, Input, message, Space, Button, Table, Form, Modal, InputNumber, Select } from 'antd';
import { HomeOutlined, DatabaseOutlined } from '@ant-design/icons';
import Icon from '@ant-design/icons';
import { Link } from 'react-router-dom';
import { ReactComponent as CassandraSvg } from './cassandra_icon.svg';

function Cassandra(props) {
  const columns = [
    { title: 'Cassandra Cluster Name', dataIndex: 'name', key: 'name' },
    { title: 'Number of Tokens', dataIndex: 'tokens', key: 'tokens' },
    { title: 'Seeds', dataIndex: 'seeds', key: 'seeds' },
    { title: 'Instances Type', dataIndex: 'type', key: 'type' }
  ];
  const [isWindowOpen, setIsWindowOpen] = useState(false);
  const [ form ] = Form.useForm();
  const formLayout = {
    labelCol: { span: 8 },
    wrapperCol: { span: 16 }
  };
  const [selected, setSelected] = useState([]);
  const rowSelection = {
    onChange: (selectedRowKeys, selectedRows) => {
      setSelected(selectedRowKeys);
    }
  };
  return (
    <>
    <Breadcrumb>
      <Breadcrumb.Item><Link to={'/'}><HomeOutlined /><span>Home</span></Link></Breadcrumb.Item>
      <Breadcrumb.Item><DatabaseOutlined /><span>Database</span></Breadcrumb.Item>
      <Breadcrumb.Item><Link to={'/cassandra'}><Icon component={CassandraSvg} /><span>Cassandra</span></Link></Breadcrumb.Item>
    </Breadcrumb>
    <h2>Apache Cassandra databases</h2>
    <Row gutten={24}>
      <Col span={6}><Input.Search allowClear style={{ style: 100 }} onSearch={() => message.error("Hey, it's a demo ;)") } /></Col>
      <Col span={6} push={6}><Space>
        <Button type="default" disabled={selected.length < 1} onClick={() => {
          var copy = props.cassandra.slice();
          selected.forEach((item) => {
            copy = copy.filter(i => i.key !== item);
            message.info("Cassandra cluster " + item + " deleted");
          });
          props.setCassandra(copy);
          setSelected([]);
        }}>Delete</Button>
        <Button type="primary" onClick={() => setIsWindowOpen(true)}>Create Cassandra database</Button>
      </Space></Col>
    </Row>
    <Row>
      <Col span={24}>
        <Table bordered columns={columns} dataSource={props.cassandra} rowSelection={rowSelection} />
      </Col>
    </Row>
    <Modal title="Create Cassandra database cluster" okText="Create" open={isWindowOpen} onCancel={() => setIsWindowOpen(false)} footer={[ <Space><Button onClick={() => setIsWindowOpen(false)}>Cancel</Button><Button type="primary" onClick={form.submit}>Create</Button></Space> ]}>
      <Form {...formLayout} form={form} name="cassandra-form" onFinish={(values) => {
				var found = false;
				props.cassandra.forEach((cas) => {
					if (cas.key === values.name) {
						found = true;
					}
				});
				if (found) {
					message.error("Cassandra cluster " + values.name + " already exists");
				} else {
					const newCluster = [ { key: values.name, name: values.name, tokens: values.tokens, seeds: values.seeds, type: values.type, status: 'Running' } ];
					const newCassandra = props.cassandra.concat(newCluster);
					props.setCassandra(newCassandra);
					message.success("Cassandra cluster " + values.name + " created");
					setIsWindowOpen(false);
				}
      }}>
        <Form.Item name="name" label="Cluster Name" rules={[{ required: true }]}><Input allowClear placeholder={'e.g. my-cassandra-cluster'} /></Form.Item>
        <Form.Item name="tokens" label="Number of Tokens" initialValue={16} rules={[{ required: true }]}><InputNumber min={1} /></Form.Item>
        <Form.Item name="seeds" label="Seeds"><Input allowClear /></Form.Item>
        <Form.Item name="type" label="Instances Type" rules={[{ required: true }]} initialValue={'t1.micro'}>
          <Select allowClear options={props.vmTypes} />
        </Form.Item>
      </Form>
    </Modal>
    </>
  );
}
export default Cassandra;
