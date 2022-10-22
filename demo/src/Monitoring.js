import { Breadcrumb, Row, Col, Card, Table, Divider } from 'antd';
import { HomeOutlined, MonitorOutlined } from '@ant-design/icons';
import { Line } from '@ant-design/plots';
import { Link } from 'react-router-dom';

function Monitoring(props) {
  var logData = [];
  var cpuVmData = [];
  props.vms.forEach((vm) => {
    var itemData = [
      { "time": "08:00", "value": 0, "category": "vm-" + vm.name },
      { "time": "08:30", "value": 10, "category": "vm-" + vm.name },
      { "time": "09:00", "value": 20, "category": "vm-" + vm.name },
      { "time": "09:30", "value": 10, "category": "vm-" + vm.name },
      { "time": "10:00", "value": 50, "category": "vm-" + vm.name },
      { "time": "10:30", "value": 40, "category": "vm-" + vm.name },
      { "time": "11:00", "value": 35, "category": "vm-" + vm.name },
      { "time": "11:30", "value": 30, "category": "vm-" + vm.name },
    ];
    cpuVmData = cpuVmData.concat(itemData);
    var vmLog = [
      { timestamp: '2022-10-25 08:12:32', loc: 'vm-' + vm.name, level: 'INFO', message: 'This is a VM log message' }, 
      { timestamp: '2022-10-25 08:17:14', loc: 'vm-' + vm.name, level: 'INFO', message: 'Another VM log message' }, 
      { timestamp: '2022-10-25 10:47:14', loc: 'vm-' + vm.name, level: 'WARN', message: 'Ouch, potential issue' }, 
    ];
    logData = logData.concat(vmLog);
  }); 
  var cpuRdsData = [];
  props.rds.forEach((rds) => {
    var itemData = [
      { "time": "08:00", "value": 90, "category": "rds-" + rds.name },
      { "time": "08:30", "value": 30, "category": "rds-" + rds.name },
      { "time": "09:00", "value": 50, "category": "rds-" + rds.name },
      { "time": "09:30", "value": 40, "category": "rds-" + rds.name },
      { "time": "10:00", "value": 45, "category": "rds-" + rds.name },
      { "time": "10:30", "value": 40, "category": "rds-" + rds.name },
      { "time": "11:00", "value": 30, "category": "rds-" + rds.name },
      { "time": "11:30", "value": 50, "category": "rds-" + rds.name },
    ];
    cpuRdsData = cpuRdsData.concat(itemData);
  });
  var cpuKafkaData = [];
  props.kafka.forEach((kafka) => {
    var itemData = [
      { "time": "08:00", "value": 10, "category": "kafka-" + kafka.name },
      { "time": "08:30", "value": 5, "category": "kafka-" + kafka.name },
      { "time": "09:00", "value": 5, "category": "kafka-" + kafka.name },
      { "time": "09:30", "value": 10, "category": "kafka-" + kafka.name },
      { "time": "10:00", "value": 15, "category": "kafka-" + kafka.name },
      { "time": "10:30", "value": 20, "category": "kafka-" + kafka.name },
      { "time": "11:00", "value": 10, "category": "kafka-" + kafka.name },
      { "time": "11:30", "value": 10, "category": "kafka-" + kafka.name },
    ];
    cpuKafkaData = cpuKafkaData.concat(itemData);
  });
  var cpuKubernetesData = [];
  props.kubernetes.forEach((kubernetes) => {
    var itemData = [
      { "time": "08:00", "value": 1, "category": "kubernetes-" + kubernetes.name },
      { "time": "08:30", "value": 5, "category": "kubernetes-" + kubernetes.name },
      { "time": "09:00", "value": 5, "category": "kubernetes-" + kubernetes.name },
      { "time": "09:30", "value": 1, "category": "kubernetes-" + kubernetes.name },
      { "time": "10:00", "value": 5, "category": "kubernetes-" + kubernetes.name },
      { "time": "10:30", "value": 2, "category": "kubernetes-" + kubernetes.name },
      { "time": "11:00", "value": 5, "category": "kubernetes-" + kubernetes.name },
      { "time": "11:30", "value": 10, "category": "kubernetes-" + kubernetes.name },
    ];
    cpuKubernetesData = cpuKubernetesData.concat(itemData);
  });

  const cpuData = cpuVmData.concat(cpuRdsData).concat(cpuKafkaData).concat(cpuKubernetesData);

  const logColumns = [
    { title: 'Timestamp', dataIndex: 'timestamp', key: 'timestamp' },
    { title: 'Location', dataIndex: 'loc', key: 'loc' },
    { title: 'Level', dataIndex: 'level', key: 'level' },
    { title: 'Message', dataIndex: 'message', key: 'message' }
  ];

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
						<Line data={cpuData} xField='time' yField='value' seriesField='category' />
					</Card>
				</Col>
				<Col span={12}>
					<Card title="Network in (bytes)" bordered={true}>
						<Line data={cpuData} xField='time' yField='value' seriesField='category' />
					</Card>
				</Col>
			</Row>
      <Row>
        <Col span={24}>
          <Divider orientation="left">Logging</Divider>
          <Table columns={logColumns} dataSource={logData} />
        </Col>
      </Row>
		</>
	);
}

export default Monitoring;
