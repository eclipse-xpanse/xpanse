import { useState } from 'react';
import { Breadcrumb, Row, Col, message, Space, Button, Input, Table, Modal, Form, InputNumber, Select } from 'antd';
import { HomeOutlined, NodeIndexOutlined } from '@ant-design/icons';
import Icon from '@ant-design/icons';
import { Link } from 'react-router-dom';
import { ReactComponent as KafkaSvg } from './kafka_icon.svg';

const { Search } = Input;

function Kafka(props) {
  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Instances Count', dataIndex: 'count', key: 'count' },
    { title: 'Size', dataIndex: 'size', key: 'size' },
    { title: 'Partitions', dataIndex: 'partitions', key: 'partitions' },
    { title: 'Status', dataIndex: 'status', key: 'status' }
  ];
  const [isWindowOpen, setIsWindowOpen] = useState(false);
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
      <Breadcrumb.Item><Link to={'/'}><HomeOutlined /><span>Home</span></Link></Breadcrumb.Item>
      <Breadcrumb.Item><NodeIndexOutlined /><span>Integration</span></Breadcrumb.Item>
      <Breadcrumb.Item><Link to={'/kafka'}><Icon component={KafkaSvg} /><span>Kafka</span></Link></Breadcrumb.Item>
    </Breadcrumb>
    <h2>Kafka Clusters</h2>
    <Row gutter={24}>
      <Col span={6}><Search allowClear style={{ style: 100 }} onSearch={() => message.error("Hey, it's a demo ;)") } /></Col>
      <Col span={6} push={6}><Space>
        <Button type="default" disabled={select.length < 1} onClick={() => {
          var copy = props.kafka.slice();
          select.forEach((item) => {
            copy = copy.filter(i => i.key !== item);
            message.info("Kafka cluster " + item + " deleted");
          });
          props.setKafka(copy);
          setSelect([]);
        }}>Delete</Button>
        <Button type="primary" onClick={() => setIsWindowOpen(true) }>Create Kafka cluster</Button>
      </Space></Col>
    </Row>
    <Row>
      <Col span={24}>
        <Table bordered columns={columns} dataSource={props.kafka} rowSelection={rowSelection} />
      </Col>
    </Row>
    <Modal title="Create Kafka cluster" okText="Create" open={isWindowOpen} onCancel={() => setIsWindowOpen(false) } footer={[ <Space><Button onClick={() => setIsWindowOpen(false)}>Cancel</Button><Button type="primary" onClick={form.submit}>Create</Button></Space> ]}>
      <Form {...formLayout} form={form} name="kafka-form" onFinish={(values) => {
				var found = false;
				props.kafka.forEach((k) => {
					if (k.key === values.name) {
						found = true;
					}
				});
				if (found) {
					message.error("Kafka cluster " + values.name + " already exists");
				} else {
					const newCluster = [ { key: values.name, name: values.name, count: values.count, size: values.size, partitions: values.partitions, status: 'Running' } ];
					const newKafka = props.kafka.concat(newCluster);
					props.setKafka(newKafka);
					message.success("Kafka cluster " + values.name + " created");
					setIsWindowOpen(false);
				}
      }}>
        <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input allowClear placeholder={'e.g. My Cluster'} /></Form.Item>
        <Form.Item name="count" label="Instances Count" rules={[{ required: true }]} initialValue={3}><InputNumber min={3} /></Form.Item>
        <Form.Item name="size" label="Instances Type" rules={[{ required: true }]} initialValue={'t2.micro'}>
          <Select allowClear options={props.vmTypes} />
        </Form.Item>
        <Form.Item name="partitions" label="Partitions" rules={[{ required: true }]} initialValue={3}><InputNumber min={1} /></Form.Item>
      </Form>
    </Modal>
    </>
  );
}
export default Kafka;
