import { Breadcrumb, Row, Col, Card } from 'antd';
import { HomeOutlined, MonitorOutlined } from '@ant-design/icons';
import { Area } from '@ant-design/plots';
import { Link } from 'react-router-dom';

function Monitoring(props) {
	const data = [
  {
    "time": "08:00",
    "value": 0,
    "category": "VM test"
  },
  {
    "time": "08:00",
    "value": 54,
    "category": "VM test2"
  },
  {
    "time": "08:00",
    "value": 0,
    "category": "Database test"
  },
  {
    "time": "08:00",
    "value": 0,
    "category": "Kafka test"
  },
  {
    "time": "08:00",
    "value": 0,
    "category": "Kubernetes test"
  },
  {
    "time": "08:30",
    "value": 0,
    "category": "VM test"
  },
  {
    "time": "08:30",
    "value": 54,
    "category": "VM test2"
  },
  {
    "time": "08:30",
    "value": 0,
    "category": "Database test"
  },
  {
    "time": "08:30",
    "value": 0,
    "category": "Kafka test"
  },
  {
    "time": "08:30",
    "value": 0,
    "category": "Kubernetes test"
  },
  {
    "time": "09:00",
    "value": 0,
    "category": "VM test"
  },
  {
    "time": "09:00",
    "value": 57,
    "category": "VM test2"
  },
  {
    "time": "09:30",
    "value": 0,
    "category": "VM test"
  },
  {
    "time": "09:30",
    "value": 0,
    "category": "VM test2"
  },
  {
    "time": "09:30",
    "value": 0,
    "category": "Database test"
  },
  {
    "time": "10:00",
    "value": 0,
    "category": "VM test"
  },
  {
    "time": "10:00",
    "value": 59,
    "category": "VM test2"
  },
  {
    "time": "10:00",
    "value": 0,
    "category": "Database test"
  },
  {
    "time": "10:00",
    "value": 0,
    "category": "Kubernetes test"
  },
  {
    "time": "10:00",
    "value": 0,
    "category": "Kafka test"
  },
];
const config = {
    data,
    xField: 'time',
    yField: 'value',
    seriesField: 'category',
  };

	return (
		<>
			<Breadcrumb>
				<Breadcrumb.Item><Link to={'/'}><HomeOutlined /><span>Home</span></Link></Breadcrumb.Item>
				<Breadcrumb.Item><Link to={'/monitoring'}><MonitorOutlined /><span>Monitoring</span></Link></Breadcrumb.Item>
			</Breadcrumb>
			<h2>Monitoring</h2>
			<Row>
				<Col span={12}>
					<Card title="CPU Utilization (%)" bordered={true}>
						<Area {...config} />
					</Card>
					// Status check failed (count)
				</Col>
				<Col span={12}>
					<Card title="Network in (bytes)" bordered={true}>
						<Area {...config} />
					</Card>
					// network out (bytes)
				</Col>
				// disk reads (bytes)
				// dist writes (bytes)
			</Row>
		</>
	);
}

export default Monitoring;
