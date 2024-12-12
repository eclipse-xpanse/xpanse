package org.eclipse.xpanse.plugins.openstack.common.aggregates.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.models.resources.InstanceResource;
import org.junit.jupiter.api.Test;

class InstanceResourceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testSettersAndGetters() throws Exception {
        InstanceResource test = mapper.readValue(setUpObjectJson(), InstanceResource.class);
        assertEquals(
                "91ac6b28dc5f4a519422f3154bc4d14d:2c2c51522b0342fc94254c1b3afb95b5",
                test.getCreator());
        assertEquals("2023-06-05T11:05:39.758133+00:00", test.getStartedAt());
        assertEquals("2023-06-05T12:00:47.073036+00:00", test.getRevisionStart());
        assertNull(test.getEndedAt());
        assertEquals("08ad2f8d537349ee9862c55398fc3b29", test.getUserId());
        assertEquals("1c190e0a4fc14e72a871810b1fc08788", test.getProjectId());
        assertEquals("e2e75bfc-ff9e-4957-a64b-11abc3c5ea26", test.getOriginalResourceId());
        assertEquals("{{request.path.[4]}}", test.getId());
        assertEquals("instance", test.getType());
        assertEquals("c1", test.getFlavorId());
        assertEquals("cadae4a1-87d7-4121-b2c0-5b141e328170", test.getImageRef());
        assertEquals("ecs-bfd6", test.getHost());
        assertEquals("kafka-broker-0-979a5376", test.getDisplayName());
        assertNull(test.getServerGroup());
        assertEquals("cirros256", test.getFlavorName());
        assertEquals("2023-06-05T11:05:39+00:00", test.getLaunchedAt());
        assertEquals("2023-06-05T11:05:33+00:00", test.getCreatedAt());
        assertNull(test.getDeletedAt());
        assertNull(test.getAvailabilityZone());
        assertNull(test.getRevisionEnd());
        assertEquals(10, test.getMetrics().size());
        assertEquals("91ac6b28dc5f4a519422f3154bc4d14d", test.getCreatedByUserId());
        assertEquals("2c2c51522b0342fc94254c1b3afb95b5", test.getCreatedByProjectId());
    }

    private String setUpObjectJson() {
        return """
                {
                  "creator": "91ac6b28dc5f4a519422f3154bc4d14d:2c2c51522b0342fc94254c1b3afb95b5",
                  "started_at": "2023-06-05T11:05:39.758133+00:00",
                  "revision_start": "2023-06-05T12:00:47.073036+00:00",
                  "ended_at": null,
                  "user_id": "08ad2f8d537349ee9862c55398fc3b29",
                  "project_id": "1c190e0a4fc14e72a871810b1fc08788",
                  "original_resource_id": "e2e75bfc-ff9e-4957-a64b-11abc3c5ea26",
                  "id": "{{request.path.[4]}}",
                  "type": "instance",
                  "flavor_id": "c1",
                  "image_ref": "cadae4a1-87d7-4121-b2c0-5b141e328170",
                  "host": "ecs-bfd6",
                  "display_name": "kafka-broker-0-979a5376",
                  "server_group": null,
                  "flavor_name": "cirros256",
                  "launched_at": "2023-06-05T11:05:39+00:00",
                  "created_at": "2023-06-05T11:05:33+00:00",
                  "deleted_at": null,
                  "availability_zone": null,
                  "revision_end": null,
                  "metrics": {
                    "compute.instance.booting.time": "0a0757dc-b518-43bf-819d-3544349cb9a8",
                    "cpu": "69262402-f2a9-42d7-9724-8467d7af90da",
                    "disk.ephemeral.size": "4aae88d0-c88c-40f3-be49-151b06055abf",
                    "disk.root.size": "d30fd4bf-3537-4867-b268-62d985cc1c18",
                    "memory": "6fba44d6-f95e-434d-b315-82c3ba7f461f",
                    "memory.resident": "9f0c121d-ae62-4733-aa85-2a35d006e4f6",
                    "memory.swap.in": "2eb36570-0081-4358-9d60-9c5a814f82b4",
                    "memory.swap.out": "42904ace-b49b-4ced-8d3c-4f29f5dbacc6",
                    "memory.usage": "6352de8b-3b8b-4159-b569-15f3d8988485",
                    "vcpus": "f3228146-8e3f-493c-b781-a5d56596a512"
                  },
                  "created_by_user_id": "91ac6b28dc5f4a519422f3154bc4d14d",
                  "created_by_project_id": "2c2c51522b0342fc94254c1b3afb95b5"
                }
                """;
    }
}
