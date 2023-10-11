package org.eclipse.xpanse.plugins.openstack.monitor.keystone;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.SortedSetMultimap;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.model.identity.AuthStore;
import org.openstack4j.model.identity.AuthVersion;
import org.openstack4j.model.identity.URLResolverParams;
import org.openstack4j.model.identity.v3.Domain;
import org.openstack4j.model.identity.v3.Project;
import org.openstack4j.model.identity.v3.Role;
import org.openstack4j.model.identity.v3.Service;
import org.openstack4j.model.identity.v3.User;

class CustomEndPointResolverTest {

    private CustomEndPointResolver customEndPointResolverUnderTest;

    @BeforeEach
    void setUp() {
        customEndPointResolverUnderTest = new CustomEndPointResolver();
    }

    @Test
    void testFindURLV3() {
        // Setup
        final URLResolverParams p = URLResolverParams.create(
                new org.openstack4j.model.identity.v3.Token() {
                    @Override
                    public String getId() {
                        return null;
                    }

                    @Override
                    public void setId(String s) {

                    }

                    @Override
                    public List<? extends Service> getCatalog() {
                        return null;
                    }

                    @Override
                    public Date getExpires() {
                        return null;
                    }

                    @Override
                    public Date getIssuedAt() {
                        return null;
                    }

                    @Override
                    public Project getProject() {
                        return null;
                    }

                    @Override
                    public Domain getDomain() {
                        return null;
                    }

                    @Override
                    public User getUser() {
                        return null;
                    }

                    @Override
                    public AuthStore getCredentials() {
                        return null;
                    }

                    @Override
                    public String getEndpoint() {
                        return "http://localhost:7077";
                    }

                    @Override
                    public List<? extends Role> getRoles() {
                        return null;
                    }

                    @Override
                    public List<String> getAuditIds() {
                        return null;
                    }

                    @Override
                    public List<String> getMethods() {
                        return null;
                    }

                    @Override
                    public AuthVersion getVersion() {
                        return null;
                    }

                    @Override
                    public String getCacheIdentifier() {
                        return null;
                    }

                    @Override
                    public SortedSetMultimap<String, Service> getAggregatedCatalog() {
                        return null;
                    }
                },
                ServiceType.IDENTITY);

        // Run the test
        final String result = customEndPointResolverUnderTest.findURLV3(p);

        // Verify the results
        assertThat(result).isEqualTo("http://localhost:7077");
    }
}
