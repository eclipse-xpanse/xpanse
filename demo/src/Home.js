import { Breadcrumb, Row, Col, Card, Divider, Descriptions } from 'antd';
import { HomeOutlined, RocketOutlined, SettingOutlined } from '@ant-design/icons';

function WelcomeCard() {
  return (
    <Card title="Welcome to OSC" bordered={true}>
      <RocketOutlined /> <a href="https://jbonofre.github.io/osc/">Getting started with OSC</a>
      <div>
        Learn the fundamentals about Open Services Cloud and cloud providers supporting it.
      </div>
      <Divider/>
      <SettingOutlined /> <a href="https://jbonofre.github.io/osc/docs/ocl">OSC Configuration Language</a>
      <div>
        Use the OSC Configuration Language to describe portable managed services.
      </div>
    </Card>
  );
}

function HealthCard(props) {
  var registeredCount = 0;
  var deployedCount = 0;
  props.treeData.forEach((category) => {
    category.children.forEach((service) => {
      registeredCount += 1;
      props.items.forEach((menuCategory) => {
        if (menuCategory.children) {
          menuCategory.children.forEach((menuService) => {
            if (menuService.key === service.key) {
              deployedCount += 1;
            }
          });
        }
      });
    });
  });
  return (
    <Card title="Health" bordered={true}>
      <Descriptions title="Services" layout="vertical">
        <Descriptions.Item label="Registered">{registeredCount}</Descriptions.Item>
        <Descriptions.Item label="Deployed">{deployedCount}</Descriptions.Item>
        <Descriptions.Item label="Failed">0</Descriptions.Item>
      </Descriptions>
    </Card>
  );  
}


function Home(props) {
  return (
		<>
		<Breadcrumb><Breadcrumb.Item href="/"><HomeOutlined/></Breadcrumb.Item></Breadcrumb>
		<h2>Console Home</h2>
		<div className="site-card-wrapper">
			<Row gutter={16}>
				<Col span={12}>
          <WelcomeCard />
				</Col>
				<Col span={12}>
          <HealthCard items={props.items} treeData={props.treeData} />
				</Col>
			</Row>
		</div>
		</>
	);
}

export default Home;
