package org.eclipse.xpanse.modules.database.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

class DeployResourceEntityTest {

    private final UUID id = UUID.randomUUID();
    private final String resourceId = "f0dcb6ea-cbe7-4c88-9c94-a5d00e82a4f2";
    private final String groupName = "zookeeper";
    private final String groupType = "huaweicloud_compute_instance";
    private final String resourceName = "resourceName";
    private final DeployResourceKind resourceKind = DeployResourceKind.VM;
    private final Map<String, String> properties = Collections.singletonMap("key", "value");
    @Mock
    private DeployServiceEntity mockDeployService;
    private DeployResourceEntity resource;

    @BeforeEach
    void setUp() {
        resource = new DeployResourceEntity();
        resource.setId(id);
        resource.setDeployService(mockDeployService);
        resource.setGroupType(groupType);
        resource.setGroupName(groupName);
        resource.setResourceId(resourceId);
        resource.setResourceName(resourceName);
        resource.setResourceKind(resourceKind);
        resource.setProperties(properties);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(id, resource.getId());
        assertEquals(mockDeployService, resource.getDeployService());
        assertEquals(groupType, resource.getGroupType());
        assertEquals(groupName, resource.getGroupName());
        assertEquals(resourceId, resource.getResourceId());
        assertEquals(resourceName, resource.getResourceName());
        assertEquals(resourceKind, resource.getResourceKind());
        assertEquals(properties, resource.getProperties());
    }

    @Test
    void testEqualsAndHashCode() {

        Object obj = new Object();
        assertNotEquals(resource, obj);
        assertNotEquals(resource.hashCode(), obj.hashCode());

        DeployResourceEntity resource1 = new DeployResourceEntity();
        assertNotEquals(resource, resource1);
        assertNotEquals(resource.hashCode(), resource1.hashCode());

        BeanUtils.copyProperties(resource, resource1);
        assertEquals(resource, resource1);
        assertEquals(resource.hashCode(), resource1.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "DeployResourceEntity(id=" + id +
                ", groupType=" + groupType +
                ", groupName=" + groupName +
                ", resourceId=" + resourceId +
                ", resourceName=" + resourceName +
                ", resourceKind=" + resourceKind +
                ", deployService=" + mockDeployService +
                ", properties=" + properties +
                ')';
        assertEquals(expectedToString, resource.toString());
    }

}
