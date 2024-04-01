#!/bin/bash
xpanse_domain_endpoint="http://192.168.0.19:8080"
project_id="09711ed50380f4882fc4c0037b80fcf4"
enterprise_project_id="bad95a97-9c31-4f29-ab4f-7bfe934ae7ab"
vpc_name="xpanse_apig_vpc"
vpc_cidr="192.168.0.0/16"
subnet_name="xpanse_apig_subnet"
subnet_cidr="192.168.0.0/24"
subnet_getaway_ip="192.168.0.1"
security_group_name="xpanse_apig_security_group"
instance_name="Xpanse"
spec_id="BASIC"
env_name="Test"
env_remark="test"

curl -sSL https://cn-north-4-hdn-koocli.obs.cn-north-4.myhuaweicloud.com/cli/latest/hcloud_install.sh -o ./hcloud_install.sh && bash ./hcloud_install.sh -y
hcloud configure init
hcloud version

hcloud_configure_init_result=$(hcloud configure show --cli-profile=default)
echo "hcloud_configure_init_result: $hcloud_configure_init_result"

region=""
if [ -n "$hcloud_configure_init_result" ]; then
	region=$(echo "$hcloud_configure_init_result" | jq -r '.region')
	echo "region: $region"
else
	echo "hcloud configure init error,region is empty"
	exit 1
fi

query_az_result=$(hcloud APIG ListAvailableZonesV2 --cli-region="$region")
echo "query_az_result: $query_az_result"

available_zone_ids=""
if [ -n "$query_az_result" ]; then
	available_zone_ids=$(echo "$query_az_result" | jq -r '.available_zones[0].code')
	echo "available_zone_ids: $available_zone_ids"
else
	echo "querying AZ under region: $region error"
	exit 1
fi

create_vpc_result=$(hcloud VPC CreateVpc/v2 --project_id="$project_id" --vpc.enterprise_project_id="$enterprise_project_id" --cli-region="$region" --vpc.name="$vpc_name" --vpc.cidr="$vpc_cidr")
echo "create_vpc_result: $create_vpc_result"

vpc_id=""
if [ -n "$create_vpc_result" ]; then
	vpc_id=$(echo "$create_vpc_result" | jq -r '.vpc.id')
	echo "vpc_id: $vpc_id"
else
	echo "vpc create error"
	exit 1
fi

while true; do
	query_vpc_result=$(hcloud VPC ListVpcs/v2 --project_id="$project_id" --enterprise_project_id="$enterprise_project_id" --cli-region="$region" --id="$vpc_id")
	echo "query_vpc_result: $query_vpc_result"
	vpc_status=""
	if [ -n "$query_vpc_result" ]; then
		vpc_status=$(echo "$query_vpc_result" | jq -r '.vpcs[0].status')
		echo "vpc_status: $vpc_status"
	else
		echo "vpc query error"
		exit 1
	fi
	if [ "$vpc_status" = "OK" ]; then
		break
	else
		echo "VPC id: $vpc_id is $vpc_status"
	fi
	sleep 1
done

create_subnet_result=$(hcloud VPC CreateSubnet/v2 --cli-region="$region" --subnet.vpc_id="$vpc_id" --subnet.name="$subnet_name" --subnet.extra_dhcp_opts.1.opt_name="ntp" --subnet.cidr="$subnet_cidr" --subnet.gateway_ip="$subnet_getaway_ip")
echo "create_subnet_result: $create_subnet_result"

subnet_id=""
if [ -n "$create_subnet_result" ]; then
	subnet_id=$(echo "$create_subnet_result" | jq -r '.subnet.id')
	echo "subnet_id: $subnet_id"
else
	echo "subnet create error"
	exit 1
fi

create_security_group_result=$(hcloud VPC CreateSecurityGroup/v2 --security_group.enterprise_project_id="$enterprise_project_id" --cli-region="$region" --security_group.vpc_id="$vpc_id" --security_group.name="$security_group_name")
echo "create_security_group_result: $create_security_group_result"

security_group_id=""
if [ -n "$create_security_group_result" ]; then
	security_group_id=$(echo "$create_security_group_result" | jq -r ".security_group.id")
	echo "security_group_id: $security_group_id"
else
	echo "security_group create error"
	exit 1
fi

create_instance_result=$(hcloud APIG CreateInstanceV2 --project_id="$project_id" --enterprise_project_id="$enterprise_project_id" --cli-region="$region" --vpc_id="$vpc_id" --subnet_id="$subnet_id" --security_group_id="$security_group_id" --available_zone_ids.1="$available_zone_ids" --instance_name="$instance_name" --spec_id="$spec_id")
echo "create_instance_result: $create_instance_result"

instance_id=""
if [ -n "$create_instance_result" ]; then
	instance_id=$(echo "$create_instance_result" | jq -r '.instance_id')
	echo "apig_instance_id: $instance_id"
else
	echo "apig instance create error"
	exit 1
fi

while true; do
    instance_details=$(hcloud APIG ShowDetailsOfInstanceV2 --cli-region="$region" --instance_id="$instance_id")
    status=""
    if [ -n "$instance_details" ]; then
	    status=$(echo "$instance_details" | jq -r '.status')
	    echo "$status"
    else
	    echo "query apig instance error"
	    exit 1
    fi
    if [ "$status" = "Running" ]; then
        break
    else
        echo "APIG instance_id: $instance_id is $status"
    fi
    sleep 5
done

create_env_result=$(hcloud APIG CreateEnvironmentV2 --project_id="$project_id" --cli-region="$region" --instance_id="$instance_id" --name="$env_name" --remark="$env_remark")
echo "create_env_result: $create_env_result"

env_id=""
if [ -n "$create_env_result" ]; then
	env_id=$(echo "$create_env_result" | jq -r '.id')
	echo "env_id: $env_id"
else
	echo "create env error"
	exit 1
fi

curl -s  "$xpanse_domain_endpoint/v3/api-docs" > xpanse_api.json
jq '.' xpanse_api.json > temp.json
mv temp.json xpanse_api.json

import_xpanse_api_result=$(hcloud APIG ImportApiDefinitionsV2 --project_id="$project_id" --cli-region="$region" --instance_id="$instance_id" --is_create_group=true --simple_mode=true --file_name="$(echo $(pwd))/xpanse_api.json")
echo "import_xpanse_api_result: $import_xpanse_api_result"

group_id=""
if [ -n "$import_xpanse_api_result" ]; then
	group_id=$(echo "$import_xpanse_api_result" | jq -r '.group_id')
	echo "group_id: $group_id"
else
	echo "import xpanse_api error"
	exit 1
fi

publish_xpanse_api_result=$(hcloud APIG BatchPublishOrOfflineApiV2 --project_id="$project_id" --cli-region="$region" --instance_id="$instance_id" --env_id="$env_id" --group_id="$group_id" --action="online")

echo "publish_xpanse_api_result: $publish_xpanse_api_result"

