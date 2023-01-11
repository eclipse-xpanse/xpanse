package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.eclipse.osc.modules.ocl.loader.OclResource;

public class TFResources {

    List<OclResource> resources = new ArrayList<>();

    public void update(TFState tfState) {
        if (tfState == null) {
            return;
        }
        for (TFStateResource tfStateResource : tfState.getResources()) {
            List<OclResource> tfResourceList =
                resources.stream()
                    .filter(tfResource -> {
                        if (tfResource instanceof TFResource) {
                            return ((TFResource) tfResource).isMatch(tfStateResource);
                        } else {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            if (tfResourceList.size() > 0) {
                OclResource oclResource = tfResourceList.get(0);
                ((TFResource) oclResource).update(tfStateResource);
                return;
            }
            OclResource tfResource = new TFResource(tfStateResource);
            resources.add(tfResource);
        }
    }

    public List<OclResource> getResources() {
        return resources;
    }
}

class TFResource extends OclResource {

    TFResource(TFStateResource tfStateResource) {
        TFResourceSchema tfResourceSchema = TFResourceSchemas.getTFResourceSchema(tfStateResource);
        setName(tfStateResource.getName());
        updateWithSchema(tfResourceSchema, tfStateResource);
    }

    private void updateWithSchema(
        TFResourceSchema tfResourceSchema, TFStateResource tfStateResource) {
        var attrs = tfStateResource.getInstances().get(0).getAttributes();
        setId(attrs.get("id").toString());
        setState("active");
        if (tfResourceSchema != null) {
            setType(tfResourceSchema.oclType);
            for (TFProperty key : tfResourceSchema.getOutput()) {
                var attr = attrs.get(key.getTf());
                if (attr == null) {
                    getProperties().put(key.getOcl(), "null");
                } else {
                    getProperties().put(key.getOcl(), attr.toString());
                }
            }
        } else {
            for (var key : attrs.keySet()) {
                if (attrs.get(key) == null) {
                    getProperties().put(key, null);
                } else {
                    getProperties().put(key, attrs.get(key).toString());
                }
            }
        }
    }

    public void update(TFStateResource tfStateResource) {
        TFResourceSchema tfResourceSchema = TFResourceSchemas.getTFResourceSchema(tfStateResource);
        updateWithSchema(tfResourceSchema, tfStateResource);
    }

    public boolean isMatch(TFStateResource tfStateResource) {
        TFResourceSchema tfResourceSchema = TFResourceSchemas.getTFResourceSchema(tfStateResource);
        if (tfResourceSchema != null) {
            return tfResourceSchema.isMatch(this, tfStateResource);
        }
        return false;
    }
}

class TFResourceSchemas {

    private volatile static List<TFResourceSchema> schemas;

    private static List<TFResourceSchema> getInstance() {
        if (schemas == null) {
            synchronized (TFResourceSchemas.class) {
                if (schemas == null) {
                    schemas = List.of(new TFResourceZone(), new TFResourceCompute(),
                        new TFResourceEIPAssociate(), new TFResourceStorage(), new TFResourceVPC(),
                        new TFResourceSubnet(), new TFResourceSecurityGroup(),
                        new TFResourceSecurityGroupRule(), new TFResourceEip());
                }
            }
        }
        return schemas;
    }

    public static TFResourceSchema getTFResourceSchema(TFStateResource tfStateResource) {
        List<TFResourceSchema> tfResourceSchemaList =
            getInstance()
                .stream()
                .filter(tfResourceSchema
                    -> tfResourceSchema.getTFType().equals(tfStateResource.getType()))
                .collect(Collectors.toList());

        if (tfResourceSchemaList.size() < 1) {
            return null;
        }

        return tfResourceSchemaList.get(0);
    }
}

@Data
class TFProperty {

    @Getter
    String ocl;

    @Getter
    String tf;

    TFProperty(String ocl, String tf) {
        this.ocl = ocl;
        this.tf = tf;
    }

    TFProperty(String property) {
        this.ocl = property;
        this.tf = property;
    }
}

@Data
class TFResourceSchema {

    String oclType;
    String TFType;

    List<TFProperty> input;
    List<TFProperty> output;

    public boolean isMatch(OclResource oclResource, TFStateResource tfStateResource) {
        return getTFType().equals(tfStateResource.getType())
            && oclResource.getName().equals(tfStateResource.getName());
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
class TFResourceZone extends TFResourceSchema {

    TFResourceZone() {
        oclType = "internal";
        TFType = "huaweicloud_availability_zones";

        input = List.of();

        output = List.of(new TFProperty("names"), new TFProperty("state"));
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
class TFResourceEIPAssociate extends TFResourceSchema {

    TFResourceEIPAssociate() {
        oclType = "internal";
        TFType = "huaweicloud_compute_eip_associate";

        input = List.of();

        output = List.of(new TFProperty("instance_id", "instance_id"),
            new TFProperty("port_id", "port_id"), new TFProperty("public_ip", "public_ip"),
            new TFProperty("id", "id"), new TFProperty("vm", "instance_id"));
    }

    public boolean isMatch(OclResource oclResource, TFStateResource tfStateResource) {
        return oclResource.getType().equals("compute")
            && ("osc-eip-associated-" + oclResource.getName()).equals(tfStateResource.getName());
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
class TFResourceCompute extends TFResourceSchema {

    TFResourceCompute() {
        oclType = "compute";
        TFType = "huaweicloud_compute_instance";

        input = List.of();

        output = List.of(
            new TFProperty("ipv6", "access_ip_v6"),
            new TFProperty("ip", "access_ip_v4"),
            new TFProperty("id", "id"));
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
class TFResourceStorage extends TFResourceSchema {

    TFResourceStorage() {
        oclType = "storage";
        TFType = "huaweicloud_evs_volume";

        input = List.of();

        output = List.of(
            new TFProperty("size", "size"),
            new TFProperty("type", "volume_type"));
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
class TFResourceSecurityGroup extends TFResourceSchema {

    TFResourceSecurityGroup() {
        oclType = "security";
        TFType = "huaweicloud_networking_secgroup";

        input = List.of();

        output = List.of();
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
class TFResourceSecurityGroupRule extends TFResourceSchema {

    TFResourceSecurityGroupRule() {
        oclType = "security";
        TFType = "huaweicloud_networking_secgroup_rule";

        input = List.of();

        output = List.of(
            new TFProperty("priority", "priority"),
            new TFProperty("cidr", "remote_ip_prefix"),
            new TFProperty("direction", "direction"),
            new TFProperty("ports", "ports"),
            new TFProperty("action", "action"),
            new TFProperty("protocol", "protocol"));
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
class TFResourceVPC extends TFResourceSchema {

    TFResourceVPC() {
        oclType = "vpc";
        TFType = "huaweicloud_vpc";

        input = List.of();

        output = List.of(
            new TFProperty("id", "id"),
            new TFProperty("cidr", "cidr"),
            new TFProperty("region", "region"));
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
class TFResourceSubnet extends TFResourceSchema {

    TFResourceSubnet() {
        oclType = "subnet";
        TFType = "huaweicloud_vpc_subnet";

        input = List.of();

        output = List.of(
            new TFProperty("cidr", "cidr"),
            new TFProperty("vpc", "vpc_id"));
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
class TFResourceEip extends TFResourceSchema {

    TFResourceEip() {
        oclType = "internal";
        TFType = "huaweicloud_vpc_eip";

        input = List.of();

        output = List.of(
            new TFProperty("address", "address"));
    }
}