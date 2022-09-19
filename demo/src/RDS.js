import { useState } from 'react';
import { Link } from 'react-router-dom';
import { message, Breadcrumb, Row, Col, Input, Space, Button, Table, Modal, Form, Select } from 'antd';
import { HomeOutlined, DatabaseOutlined, ConsoleSqlOutlined } from '@ant-design/icons';

const { Search } = Input;
const { Option } = Select;

function RDS(props) {
  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Engine', dataIndex: 'engine', key: 'engine' },
    { title: 'Size', dataIndex: 'size', key: 'size' },
    { title: 'Status', dataIndex: 'status', key: 'status' }
  ];
  const formLayout = {
    labelCol: { span: 8 },
    wrapperCol: { span: 16 }
  };
  const [ form ] = Form.useForm();
  const [select, setSelect] = useState([]);
  const rowSelection = {
    onChange: (selectedRowKeys, selectedRows) => {
      setSelect(selectedRowKeys);
    }
  };
  const [isWindowOpen, setIsWindowOpen] = useState(false);
  return (
    <>
    <Breadcrumb>
      <Breadcrumb.Item><Link to={'/'}><HomeOutlined /><span>Home</span></Link></Breadcrumb.Item>
      <Breadcrumb.Item><DatabaseOutlined /><span>Database</span></Breadcrumb.Item>
      <Breadcrumb.Item><Link to={'/rds'}><ConsoleSqlOutlined /><span>Relational</span></Link></Breadcrumb.Item>
    </Breadcrumb>
    <h2>Relational Databases</h2>
    <Row gutter={24}>
      <Col span={6}><Search allowClear style={{ style: 100 }} onSearch={() => message.error("Hey, it's a demo ;)") } /></Col>
      <Col span={6} push={6}><Space>
      <Button type="default" disabled={select < 1} onClick={() => {
          var copy = props.rds.slice();
          select.forEach((item) => {
            copy = copy.filter(i => i.key !== item);
            message.info("Database " + item + " deleted");
          });
          props.setRds(copy);
          setSelect([]);
        }}>Delete</Button>
        <Button type="primary" onClick={() => { setIsWindowOpen(true) }}>Create database</Button>
      </Space></Col>
    </Row>
    <Row>
      <Col span={24}>
        <Table bordered columns={columns} dataSource={props.rds} rowSelection={rowSelection} />
      </Col>
    </Row>
    <Modal title="Create Relational Database" okText="Create" open={isWindowOpen} onCancel={() => setIsWindowOpen(false) } footer={[ <Space><Button onClick={() => setIsWindowOpen(false) }>Cancel</Button><Button type="primary" onClick={form.submit}>Create</Button></Space> ]} >
      <Form {...formLayout} form={form} name="database-form" onFinish={(values) => {
					var found = false;
					props.rds.forEach((db) => {
						if (db.key === values.name) {
							found = true;
						}
					});
					if (found) {
						message.error("Database " + values.name + " already exists");
					} else {
						const newDatabase = [ { key: values.name, name: values.name, engine: values.engine, size: values.size, status: 'Started' } ];
						const newDatabases = props.rds.concat(newDatabase);
						props.setRds(newDatabases);
						message.success("Database " + values.name + " created");
						setIsWindowOpen(false);
					}
        }}>
        <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input allowClear placeholder={'e.g. My Database'} /></Form.Item>
        <Form.Item name="engine" label="Engine" rules={[{ required: true }]} initialValue={'MySQL'}>
          <Select allowClear>
            <Option key="mysql" value="MySQL">MySQL</Option>
            <Option key="oracle" value="Oracle">Oracle</Option>
          </Select>
        </Form.Item>
        <Form.Item name="size" label="Instance Type" rules={[{ required: true }]} initialValue={'t2.micro'}>
          <Select allowClear options={props.vmTypes} />
        </Form.Item>
      </Form>
    </Modal>
    </>
  );
}

export default RDS;
