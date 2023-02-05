import { Breadcrumb, Row, Col, Card, Table, Divider } from 'antd';
import { HomeOutlined, MonitorOutlined } from '@ant-design/icons';
import { Line } from '@ant-design/plots';
import { Link } from 'react-router-dom';

function Monitoring(props) {
  var logData = [];
  var cpuVmData = [];
  var networkVmData = [];
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
    var networkItemData = [
      { "time": "08:00", "value": 500, "category": "vm-" + vm.name },
      { "time": "08:30", "value": 1000, "category": "vm-" + vm.name },
      { "time": "09:00", "value": 200, "category": "vm-" + vm.name },
      { "time": "09:30", "value": 100, "category": "vm-" + vm.name },
      { "time": "10:00", "value": 500, "category": "vm-" + vm.name },
      { "time": "10:30", "value": 400, "category": "vm-" + vm.name },
      { "time": "11:00", "value": 3500, "category": "vm-" + vm.name },
      { "time": "11:30", "value": 300, "category": "vm-" + vm.name },
    ];
    networkVmData = networkVmData.concat(networkItemData);
    var vmLog = [
      { timestamp: '2022-10-25 08:12:32', loc: 'vm-' + vm.name, level: 'INFO', message: 'This is a VM log message' }, 
      { timestamp: '2022-10-25 08:17:14', loc: 'vm-' + vm.name, level: 'INFO', message: 'Another VM log message' }, 
      { timestamp: '2022-10-25 10:47:14', loc: 'vm-' + vm.name, level: 'WARN', message: 'Ouch, potential issue' }, 
    ];
    logData = logData.concat(vmLog);
  }); 
  var cpuRdsData = [];
  var networkRdsData = [];
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
    var networkItemData = [
      { "time": "08:00", "value": 9000, "category": "rds-" + rds.name },
      { "time": "08:30", "value": 3000, "category": "rds-" + rds.name },
      { "time": "09:00", "value": 5000, "category": "rds-" + rds.name },
      { "time": "09:30", "value": 4000, "category": "rds-" + rds.name },
      { "time": "10:00", "value": 4500, "category": "rds-" + rds.name },
      { "time": "10:30", "value": 4000, "category": "rds-" + rds.name },
      { "time": "11:00", "value": 3000, "category": "rds-" + rds.name },
      { "time": "11:30", "value": 5000, "category": "rds-" + rds.name },
    ];
    networkRdsData = networkRdsData.concat(networkItemData);
    var rdsLog = [
      { timestamp: '2022-10-25 09:52:12', loc: 'rds-' + rds.name, level: 'INFO', message: 'Create table' }, 
      { timestamp: '2022-10-25 10:27:04', loc: 'rds-' + rds.name, level: 'INFO', message: 'Update schema' }, 
      { timestamp: '2022-10-25 11:47:10', loc: 'rds-' + rds.name, level: 'INFO', message: 'SQL query' }, 
    ];
    logData = logData.concat(rdsLog);
  });
  var cpuKafkaData = [];
  var networkKafkaData = [];
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
    var networkItemData = [
      { "time": "08:00", "value": 1000, "category": "kafka-" + kafka.name },
      { "time": "08:30", "value": 50, "category": "kafka-" + kafka.name },
      { "time": "09:00", "value": 50, "category": "kafka-" + kafka.name },
      { "time": "09:30", "value": 1000, "category": "kafka-" + kafka.name },
      { "time": "10:00", "value": 150, "category": "kafka-" + kafka.name },
      { "time": "10:30", "value": 200, "category": "kafka-" + kafka.name },
      { "time": "11:00", "value": 10, "category": "kafka-" + kafka.name },
      { "time": "11:30", "value": 10, "category": "kafka-" + kafka.name },
    ];
    networkKafkaData = networkKafkaData.concat(networkItemData);
    var kafkaLog = [
      { timestamp: '2022-10-25 08:32:12', loc: 'kafka-' + kafka.name, level: 'INFO', message: 'Create topic foo' }, 
      { timestamp: '2022-10-25 10:27:04', loc: 'kafka-' + kafka.name, level: 'INFO', message: 'Create partition' }, 
    ];
    logData = logData.concat(kafkaLog);
  });
  var cpuKubernetesData = [];
  var networkKubernetesData = [];
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
    var networkItemData = [
      { "time": "08:00", "value": 100, "category": "kubernetes-" + kubernetes.name },
      { "time": "08:30", "value": 5000, "category": "kubernetes-" + kubernetes.name },
      { "time": "09:00", "value": 500, "category": "kubernetes-" + kubernetes.name },
      { "time": "09:30", "value": 100, "category": "kubernetes-" + kubernetes.name },
      { "time": "10:00", "value": 5000, "category": "kubernetes-" + kubernetes.name },
      { "time": "10:30", "value": 200, "category": "kubernetes-" + kubernetes.name },
      { "time": "11:00", "value": 500, "category": "kubernetes-" + kubernetes.name },
      { "time": "11:30", "value": 1000, "category": "kubernetes-" + kubernetes.name },
    ];
    networkKubernetesData = networkKubernetesData.concat(networkItemData);
    var kubernetesLog = [
      { timestamp: '2022-10-25 09:12:02', loc: 'kubernetes-' + kubernetes.name, level: 'INFO', message: 'Launching pods' }, 
      { timestamp: '2022-10-25 11:17:36', loc: 'kubernetes-' + kubernetes.name, level: 'INFO', message: 'Create kubernetes cluster' }, 
    ];
    logData = logData.concat(kubernetesLog);
  });
  var cpuRancherData = [];
  var networkRancherData = [];
  props.rancher.forEach((rancher) => {
    var itemData = [
      { "time": "08:00", "value": 10, "category": "rancher-" + rancher.name },
      { "time": "08:30", "value": 15, "category": "rancher-" + rancher.name },
      { "time": "09:00", "value": 15, "category": "rancher-" + rancher.name },
      { "time": "09:30", "value": 10, "category": "rancher-" + rancher.name },
      { "time": "10:00", "value": 15, "category": "rancher-" + rancher.name },
      { "time": "10:30", "value": 20, "category": "rancher-" + rancher.name },
      { "time": "11:00", "value": 50, "category": "rancher-" + rancher.name },
      { "time": "11:30", "value": 10, "category": "rancher-" + rancher.name },
    ];
    cpuRancherData = cpuRancherData.concat(itemData);
    var networkItemData = [
      { "time": "08:00", "value": 200, "category": "rancher-" + rancher.name },
      { "time": "08:30", "value": 2000, "category": "rancher-" + rancher.name },
      { "time": "09:00", "value": 100, "category": "rancher-" + rancher.name },
      { "time": "09:30", "value": 700, "category": "rancher-" + rancher.name },
      { "time": "10:00", "value": 4000, "category": "rancher-" + rancher.name },
      { "time": "10:30", "value": 800, "category": "rancher-" + rancher.name },
      { "time": "11:00", "value": 200, "category": "rancher-" + rancher.name },
      { "time": "11:30", "value": 9000, "category": "rancher-" + rancher.name },
    ];
    networkRancherData = networkRancherData.concat(networkItemData);
    var rancherLog = [
      { timestamp: '2022-10-25 11:17:36', loc: 'rancher-' + rancher.name, level: 'INFO', message: 'Create rancher manager' }, 
    ];
    logData = logData.concat(rancherLog);
  });
  var cpuCassandraData = [];
  var networkCassandraData = [];
  props.cassandra.forEach((cassandra) => {
    var itemData = [
      { "time": "08:00", "value": 80, "category": "cassandra-" + cassandra.name },
      { "time": "08:30", "value": 95, "category": "cassandra-" + cassandra.name },
      { "time": "09:00", "value": 35, "category": "cassandra-" + cassandra.name },
      { "time": "09:30", "value": 20, "category": "cassandra-" + cassandra.name },
      { "time": "10:00", "value": 45, "category": "cassandra-" + cassandra.name },
      { "time": "10:30", "value": 50, "category": "cassandra-" + cassandra.name },
      { "time": "11:00", "value": 70, "category": "cassandra-" + cassandra.name },
      { "time": "11:30", "value": 20, "category": "cassandra-" + cassandra.name },
    ];
    cpuCassandraData = cpuCassandraData.concat(itemData);
    var networkItemData = [
      { "time": "08:00", "value": 400, "category": "cassandra-" + cassandra.name },
      { "time": "08:30", "value": 8000, "category": "cassandra-" + cassandra.name },
      { "time": "09:00", "value": 200, "category": "cassandra-" + cassandra.name },
      { "time": "09:30", "value": 700, "category": "cassandra-" + cassandra.name },
      { "time": "10:00", "value": 7000, "category": "cassandra-" + cassandra.name },
      { "time": "10:30", "value": 600, "category": "cassandra-" + cassandra.name },
      { "time": "11:00", "value": 300, "category": "cassandra-" + cassandra.name },
      { "time": "11:30", "value": 8000, "category": "cassandra-" + cassandra.name },
    ];
    networkCassandraData = networkCassandraData.concat(networkItemData);
    var cassandraLog = [
      { timestamp: '2022-10-25 10:37:56', loc: 'cassandra-' + cassandra.name, level: 'INFO', message: 'Create Cassandra query' }, 
    ];
    logData = logData.concat(cassandraLog);
  });
  var cpuActivemqData = [];
  var networkActivemqData = [];
  props.activemq.forEach((activemq) => {
    var itemData = [
      { "time": "08:00", "value": 20, "category": "activemq-" + activemq.name },
      { "time": "08:30", "value": 45, "category": "activemq-" + activemq.name },
      { "time": "09:00", "value": 55, "category": "activemq-" + activemq.name },
      { "time": "09:30", "value": 30, "category": "activemq-" + activemq.name },
      { "time": "10:00", "value": 75, "category": "activemq-" + activemq.name },
      { "time": "10:30", "value": 20, "category": "activemq-" + activemq.name },
      { "time": "11:00", "value": 30, "category": "activemq-" + activemq.name },
      { "time": "11:30", "value": 80, "category": "activemq-" + activemq.name },
    ];
    cpuActivemqData = cpuActivemqData.concat(itemData);
    var networkItemData = [
      { "time": "08:00", "value": 4000, "category": "activemq-" + activemq.name },
      { "time": "08:30", "value": 3000, "category": "activemq-" + activemq.name },
      { "time": "09:00", "value": 2000, "category": "activemq-" + activemq.name },
      { "time": "09:30", "value": 7000, "category": "activemq-" + activemq.name },
      { "time": "10:00", "value": 7000, "category": "activemq-" + activemq.name },
      { "time": "10:30", "value": 6000, "category": "activemq-" + activemq.name },
      { "time": "11:00", "value": 3000, "category": "activemq-" + activemq.name },
      { "time": "11:30", "value": 2000, "category": "activemq-" + activemq.name },
    ];
    networkActivemqData = networkActivemqData.concat(networkItemData);
    var activemqLog = [
      { timestamp: '2022-10-25 12:27:16', loc: 'activemq-' + activemq.name, level: 'INFO', message: 'ActiveMQ NoW booked' }, 
    ];
    logData = logData.concat(activemqLog);
  });
  var cpuPulsarData = [];
  var networkPulsarData = [];
  props.pulsar.forEach((pulsar) => {
    var itemData = [
      { "time": "08:00", "value": 40, "category": "pulsar-" + pulsar.name },
      { "time": "08:30", "value": 35, "category": "pulsar-" + pulsar.name },
      { "time": "09:00", "value": 45, "category": "pulsar-" + pulsar.name },
      { "time": "09:30", "value": 38, "category": "pulsar-" + pulsar.name },
      { "time": "10:00", "value": 79, "category": "pulsar-" + pulsar.name },
      { "time": "10:30", "value": 66, "category": "pulsar-" + pulsar.name },
      { "time": "11:00", "value": 62, "category": "pulsar-" + pulsar.name },
      { "time": "11:30", "value": 70, "category": "pulsar-" + pulsar.name },
    ];
    cpuPulsarData = cpuPulsarData.concat(itemData);
    var networkItemData = [
      { "time": "08:00", "value": 3000, "category": "pulsar-" + pulsar.name },
      { "time": "08:30", "value": 3000, "category": "pulsar-" + pulsar.name },
      { "time": "09:00", "value": 200, "category": "pulsar-" + pulsar.name },
      { "time": "09:30", "value": 300, "category": "pulsar-" + pulsar.name },
      { "time": "10:00", "value": 8000, "category": "pulsar-" + pulsar.name },
      { "time": "10:30", "value": 8000, "category": "pulsar-" + pulsar.name },
      { "time": "11:00", "value": 9000, "category": "pulsar-" + pulsar.name },
      { "time": "11:30", "value": 8000, "category": "pulsar-" + pulsar.name },
    ];
    networkPulsarData = networkPulsarData.concat(networkItemData);
    var pulsarLog = [
      { timestamp: '2022-10-25 10:47:16', loc: 'pulsar-' + pulsar.name, level: 'INFO', message: 'Pulsar broker updated' }, 
    ];
    logData = logData.concat(pulsarLog);
  });

  const cpuData = cpuVmData.concat(cpuRdsData).concat(cpuKafkaData).concat(cpuKubernetesData).concat(cpuRancherData).concat(cpuCassandraData).concat(cpuActivemqData).concat(cpuPulsarData);
  const networkData = networkVmData.concat(networkRdsData).concat(networkKafkaData).concat(networkKubernetesData).concat(networkRancherData).concat(networkCassandraData).concat(networkActivemqData).concat(networkPulsarData);

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
					<Card title="Network (bytes)" bordered={true}>
						<Line data={networkData} xField='time' yField='value' seriesField='category' />
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
