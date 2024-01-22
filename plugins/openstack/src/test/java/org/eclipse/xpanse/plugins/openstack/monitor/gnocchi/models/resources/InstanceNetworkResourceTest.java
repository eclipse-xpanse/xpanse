package org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class InstanceNetworkResourceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testSettersAndGetters() throws Exception {
        InstanceNetworkResource test = getInstanceByJson();
        assertEquals("91ac6b28dc5f4a519422f3154bc4d14d:2c2c51522b0342fc94254c1b3afb95b5", test.getCreator());
        assertEquals("2023-06-05T11:10:18.130486+00:00", test.getStartedAt());
        assertEquals("2023-06-05T11:10:18.130492+00:00", test.getRevisionStart());
        assertNull(test.getEndedAt());
        assertEquals("08ad2f8d537349ee9862c55398fc3b29", test.getUserId());
        assertEquals("1c190e0a4fc14e72a871810b1fc08788", test.getProjectId());
        assertEquals("instance-00000005-7b5b6ee6-cab4-4e72-be6e-854a67c6d381-tapffdde028-eb", test.getOriginalResourceId());
        assertEquals("f4b607a9-ff04-5566-a5a4-399ad0cadd26", test.getId());
        assertEquals("instance_network_interface", test.getType());
        assertEquals("tapffdde028-eb", test.getName());
        assertNull(test.getRevisionEnd());
        assertEquals(8, test.getMetrics().size());
        assertEquals("91ac6b28dc5f4a519422f3154bc4d14d", test.getCreatedByUserId());
        assertEquals("2c2c51522b0342fc94254c1b3afb95b5", test.getCreatedByProjectId());

    }

    InstanceNetworkResource getInstanceByJson() throws Exception {
        String json = """
                {
                    "creator": "91ac6b28dc5f4a519422f3154bc4d14d:2c2c51522b0342fc94254c1b3afb95b5",
                    "started_at": "2023-06-05T11:10:18.130486+00:00",
                    "revision_start": "2023-06-05T11:10:18.130492+00:00",
                    "ended_at": null,
                    "user_id": "08ad2f8d537349ee9862c55398fc3b29",
                    "project_id": "1c190e0a4fc14e72a871810b1fc08788",
                    "original_resource_id": "instance-00000005-7b5b6ee6-cab4-4e72-be6e-854a67c6d381-tapffdde028-eb",
                    "id": "f4b607a9-ff04-5566-a5a4-399ad0cadd26",
                    "type": "instance_network_interface",
                    "name": "tapffdde028-eb",
                    "instance_id": "7b5b6ee6-cab4-4e72-be6e-854a67c6d381",
                    "revision_end": null,
                    "metrics": {
                      "network.incoming.bytes": "13e29c07-324e-456f-bac5-d02366570b79",
                      "network.incoming.packets": "74f6e4fb-9c1a-403a-80df-06ec1fcb9335",
                      "network.incoming.packets.drop": "dbc4fe1d-f8d5-4980-9057-61b6d3512e4a",
                      "network.incoming.packets.error": "5b27ac01-dc16-448f-bb09-1e1e8813e4ab",
                      "network.outgoing.bytes": "c07586ab-3b38-44fd-acea-ccb23900cbc4",
                      "network.outgoing.packets": "f812d3f0-8cbd-458b-b105-8256f825e310",
                      "network.outgoing.packets.drop": "6667c6ca-5959-4f24-b6cc-03eb4a495ac1",
                      "network.outgoing.packets.error": "45baf68a-0749-4ffe-acdc-17779b2c51b9"
                    },
                    "created_by_user_id": "91ac6b28dc5f4a519422f3154bc4d14d",
                    "created_by_project_id": "2c2c51522b0342fc94254c1b3afb95b5"
                }
                """;
        return mapper.readValue(json, InstanceNetworkResource.class);

    }
}
