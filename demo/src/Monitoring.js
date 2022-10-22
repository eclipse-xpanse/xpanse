import { Breadcrumb, Row, Col, Card } from 'antd';
import { HomeOutlined, MonitorOutlined } from '@ant-design/icons';
import { Line } from '@ant-design/plots';
import { Link } from 'react-router-dom';

function Monitoring(props) {
  var vmData = [];
  props.vms.forEach((vm) => {
    var itemData = [
      { "time": "08:00", "value": 0, "category": "vm-" + vm.name },
      { "time": "08:30", "value": 10, "category": "vm-" + vm.name },
      { "time": "09:00", "value": 20, "category": "vm-" + vm.name }
    ];
    vmData = vmData.concat(itemData);
  }); 
  var rdsData = [];
  props.rds.forEach((rds) => {
    var itemData = [
      { "time": "08:00", "value": 90, "category": "rds-" + rds.name },
      { "time": "08:30", "value": 30, "category": "rds-" + rds.name },
      { "time": "09:00", "value": 50, "category": "rds-" + rds.name }
    ];
    rdsData = rdsData.concat(itemData);
  });

  const data = vmData.concat(rdsData);

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
						<Line {...config} />
					</Card>
					// Status check failed (count)
				</Col>
				<Col span={12}>
					<Card title="Network in (bytes)" bordered={true}>
						<Line {...config} />
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
