import { Breadcrumb, Divider, Descriptions } from 'antd';
import { HomeOutlined, BankOutlined } from '@ant-design/icons';
import { Link } from 'react-router-dom';

function Billing(props) {

  var monthForecast = 0;
  var totalServices = 0;
  var vmsAmount = 0;
  var containersAmount = 0;
  var rdsAmount = 0;
  var kafkaAmount = 0;
  var displayKubernetes;
  var displayRancher;
  var displayCassandra;
  var displayActivemq;
  var displayPulsar;

  if (props.vms.length > 0) {
    vmsAmount = 10 * props.vms.length;;
    monthForecast = monthForecast + vmsAmount;
    totalServices = totalServices + props.vms.length;
  }
  if (props.containers.length > 0) {
    containersAmount = 5 * props.containers.length; 
    monthForecast = monthForecast + containersAmount;
    totalServices = totalServices + props.containers.length;
  }
  if (props.rds.length > 0) {
    rdsAmount = 20 * props.rds.length;
    monthForecast = monthForecast + rdsAmount;
    totalServices = totalServices + props.rds.length;
  }
  if (props.kafka.length > 0) {
    kafkaAmount = 12 * props.kafka.length;
    monthForecast = monthForecast + kafkaAmount;
    totalServices = totalServices + props.kafka.length; 
  }
  props.items[0].children.forEach((service) => {
    if (service.key === 'kubernetes') {
      const kubernetesAmount = props.kubernetes.length * 60;
      monthForecast = monthForecast + kubernetesAmount;
      totalServices = totalServices + props.kubernetes.length;
      displayKubernetes = <Descriptions.Item label="Kubernetes">{kubernetesAmount}</Descriptions.Item>;
    }
    if (service.key === 'rancher') {
      const rancherAmount = props.rancher.length * 100;
      monthForecast = monthForecast + rancherAmount;
      totalServices = totalServices + props.rancher.length;
      displayRancher = <Descriptions.Item label="Rancher">{rancherAmount}</Descriptions.Item>;
    }
  });
  props.items[1].children.forEach((service) => {
    if (service.key === 'cassandra') {
      const cassandraAmount = props.cassandra.length * 66;
      monthForecast = monthForecast + cassandraAmount;
      totalServices = totalServices + props.cassandra.length;
      displayCassandra = <Descriptions.Item label="Cassandra">{cassandraAmount}</Descriptions.Item>;
    }
  });
  props.items[2].children.forEach((service) => {
    if (service.key === 'activemq') {
      const activemqAmount = props.activemq.length * 55;
      monthForecast = monthForecast + activemqAmount;
      totalServices = totalServices + props.activemq.length;
      displayActivemq = <Descriptions.Item label="ActiveMQ">{activemqAmount}</Descriptions.Item>;
    }
    if (service.key === 'pulsar') {
      const pulsarAmount = props.pulsar.length * 44;
      monthForecast = monthForecast + pulsarAmount;
      totalServices = totalServices + props.pulsar.length;
      displayPulsar = <Descriptions.Item label="Pulsar">{pulsarAmount}</Descriptions.Item>;
    }
  });

  return (
    <>
    <Breadcrumb>
      <Breadcrumb.Item><Link to={'/'}><HomeOutlined /><span>Home</span></Link></Breadcrumb.Item>
      <Breadcrumb.Item><Link to={'/billing'}><BankOutlined /><span>Billing</span></Link></Breadcrumb.Item>
    </Breadcrumb>
    <h2>Billing</h2>
    <Divider orientation="left">Summary</Divider>
    <Descriptions>
      <Descriptions.Item label="Current month's total forecast">{monthForecast}</Descriptions.Item>
      <Descriptions.Item label="Total number of active services">{totalServices}</Descriptions.Item>
    </Descriptions>
    <Divider orientation="left">Cost By Service</Divider>
    <Descriptions bordered>
      <Descriptions.Item label="VM">{vmsAmount}</Descriptions.Item>
      <Descriptions.Item label="Container">{containersAmount}</Descriptions.Item>
      <Descriptions.Item label="Relational Database">{rdsAmount}</Descriptions.Item>
      <Descriptions.Item label="Kafka">{kafkaAmount}</Descriptions.Item>
      {displayKubernetes}
      {displayRancher}
      {displayCassandra}
      {displayActivemq}
      {displayPulsar}
    </Descriptions>
    </>
  );
}
export default Billing;
