import { useState } from 'react';
import { Breadcrumb, Row, Col, Input, message, Space, Button, Table, Modal, Form, Select, InputNumber } from 'antd';
import { HomeOutlined, CloudOutlined } from '@ant-design/icons';
import Icon from '@ant-design/icons';
import { Link } from 'react-router-dom';
import { ReactComponent as KubernetesSvg } from './kubernetes_icon.svg';

const { Search } = Input;
const { Option } = Select;

function Kubernetes(props) {
  const columns = [
    { title: 'Cluster Name', dataIndex: 'name', key: 'name' },
    { title: 'Kubernetes Version', dataIndex: 'version', key: 'version' },
    { title: 'Instances Count', dataIndex: 'count', key: 'count' },
    { title: 'Instances Type', dataIndex: 'type', key: 'type' },
    { title: 'Status', dataIndex: 'status', key: 'status' }
  ];
  const formLayout = {
    labelCol: { span: 8 },
    wrapperCol: { span: 16 }
  };
  const [isWindowOpen, setIsWindowOpen] = useState(false);
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
      <Breadcrumb.Item><CloudOutlined /><span>Compute</span></Breadcrumb.Item>
      <Breadcrumb.Item><Link to={'/kubernetes'}><Icon component={KubernetesSvg} /><span>Kubernetes</span></Link></Breadcrumb.Item>
    </Breadcrumb>
		<h2>Kubernetes Clusters</h2>
    <Row gutter={24}>
      <Col span={6}><Search allowClear style={{ style: 100 }} onSearch={() => message.error("Hey, it's a demo ;)") } /></Col>
      <Col span={6} push={6}><Space>
        <Button type="default" disabled={selected.length < 1} onClick={() => {
          var copy = props.kubernetes.slice();
          selected.forEach((item) => {
            copy = copy.filter(i => i.key !== item);
            message.info("Kubernetes cluster " + item + " deleted");
          });
          props.setKubernetes(copy);
          setSelected([]);
        }}>Delete</Button>
        <Button type="primary" onClick={() => setIsWindowOpen(true)}>Create Kubernetes cluster</Button>
      </Space></Col>
    </Row>
    <Row>
      <Col span={24}>
        <Table bordered columns={columns} dataSource={props.kubernetes} rowSelection={rowSelection}/>
      </Col>
    </Row>
    <Modal title="Create Kubernetes cluster" okText="Create" open={isWindowOpen} onCancel={() => setIsWindowOpen(false)} footer={[ <Space><Button onClick={() => setIsWindowOpen(false)}>Cancel</Button><Button type="primary" onClick={form.submit}>Create</Button></Space> ]}>
      <Form {...formLayout} form={form} name="kuberenetes-form" onFinish={(values) => {
				var found = false;
				props.kubernetes.forEach((kube) => {
					if (kube.key === values.name) {
						found = true;
					}
				});
				if (found) {
					message.error("Kubernetes cluster " + values.name + " already exists");
				} else {
					const newKube = [ { key: values.name, name: values.name, version: values.version, count: values.count, type: values.type, status: 'Running' }];
					const newClusters = props.kubernetes.concat(newKube);
					props.setKubernetes(newClusters);
					message.success("Kubernetes cluster " + values.name + " created");
					setIsWindowOpen(false);
				}
      }}>
        <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input allowClear placeholder={'e.g. My Cluster'} /></Form.Item>
        <Form.Item name="version" label="Kubernetes Version" rules={[{ required: true }]} initialValue={'1.22'}>
          <Select allowClear>
            <Option key="1.22" value="1.22">1.22</Option>
            <Option key="1.21" value="1.21">1.21</Option>
            <Option key="1.20" value="1.20">1.20</Option>
          </Select>
        </Form.Item>
        <Form.Item name="count" label="Instances Count" rules={[{ required: true }]} initialValue={3}><InputNumber min={3} /></Form.Item>
        <Form.Item name="type" label="Instances Type" rules={[{ required: true }]} initialValue={'t2.micro'}>
          <Select allowClear options={props.vmTypes} />
        </Form.Item>
      </Form>
    </Modal>
    </>
	);
}
export default Kubernetes;
