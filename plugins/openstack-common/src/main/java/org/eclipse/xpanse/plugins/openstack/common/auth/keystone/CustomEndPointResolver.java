/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.common.auth.keystone;

import com.google.common.collect.SortedSetMultimap;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import org.openstack4j.api.exceptions.RegionEndpointNotFoundException;
import org.openstack4j.api.identity.EndpointURLResolver;
import org.openstack4j.api.types.Facing;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.model.identity.URLResolverParams;
import org.openstack4j.model.identity.v2.Access;
import org.openstack4j.model.identity.v2.Endpoint;
import org.openstack4j.model.identity.v3.Service;
import org.openstack4j.model.identity.v3.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Overrides the DefaultEndpointResolver from OpenStack4j. This is required because the default
 * implementation of OpenStack4j assumes that the metric measures are available via Ceilometer API.
 * But this is already not available in the recent Openstack releases, and we must use Gnocchi API.
 */
public class CustomEndPointResolver implements EndpointURLResolver {
    private static final Logger LOG = LoggerFactory.getLogger(CustomEndPointResolver.class);
    private static final Map<Key, String> CACHE = new ConcurrentHashMap<>();
    private static final boolean LEGACY_EP_HANDLING = Boolean.getBoolean(LEGACY_EP_RESOLVING_PROP);
    private String publicHostIp;

    @Override
    public String findURLV2(URLResolverParams p) {
        if (p.type == null) {
            return p.access.getEndpoint();
        }

        Key key = Key.of(p.access.getCacheIdentifier(), p.type, p.perspective, p.region);
        String url = CACHE.get(key);

        if (url != null) {
            return url;
        }

        url = resolveV2(p);

        if (url != null) {
            return url;
        } else if (p.region != null) {
            throw RegionEndpointNotFoundException.create(p.region, p.type.getServiceName());
        }

        return p.access.getEndpoint();
    }

    @Override
    public String findURLV3(URLResolverParams p) {

        if (p.type == null) {
            return p.token.getEndpoint();
        }

        Key key = Key.of(p.token.getCacheIdentifier(), p.type, p.perspective, p.region);

        String url = CACHE.get(key);

        if (url != null) {
            return url;
        }

        url = resolveV3(p);

        if (url != null) {
            CACHE.put(key, url);
            return url;
        } else if (p.region != null) {
            throw RegionEndpointNotFoundException.create(p.region, p.type.getServiceName());
        }

        return p.token.getEndpoint();
    }

    private String resolveV2(URLResolverParams p) {
        SortedSetMultimap<String, ? extends Access.Service> catalog =
                p.access.getAggregatedCatalog();
        SortedSet<? extends Access.Service> services = catalog.get(p.type.getServiceName());

        if (services.isEmpty()) {
            services = catalog.get(p.type.getType());
        }

        if (!services.isEmpty()) {
            Access.Service sc = p.getV2Resolver().resolveV2(p.type, services);
            for (Endpoint ep : sc.getEndpoints()) {
                if (p.region != null && !p.region.equalsIgnoreCase(ep.getRegion())) {
                    continue;
                }

                if (sc.getServiceType() == ServiceType.NETWORK) {
                    sc.getEndpoints().getFirst().toBuilder().type(sc.getServiceType().name());
                }

                if (p.perspective == null) {
                    return getEndpointUrl(p.access, ep);
                }

                return switch (p.perspective) {
                    case ADMIN -> ep.getAdminURL().toString();
                    case INTERNAL -> ep.getInternalURL().toString();
                    default -> ep.getPublicURL().toString();
                };
            }
        } else {
            // if no catalog returned, if is identity service, just return endpoint
            if (ServiceType.IDENTITY.equals(p.type)) {
                return p.access.getEndpoint();
            }
        }
        return null;
    }

    private String resolveV3(URLResolverParams urlResolverParams) {
        Token token = urlResolverParams.token;

        // in v3 api, if user has no default project, and token is unscoped,
        // no catalog will be returned
        // then if service is Identity service, should directly return the endpoint back
        if (token.getCatalog() == null) {
            if (ServiceType.IDENTITY.equals(urlResolverParams.type)) {
                return token.getEndpoint();
            } else {
                return null;
            }
        }

        for (Service service : token.getCatalog()) {
            // Special handling for metric to get the correct end point.
            if (urlResolverParams.type == ServiceType.TELEMETRY
                            && service.getType().equals("metric")
                    || urlResolverParams.type == ServiceType.forName(service.getType())
                    || urlResolverParams.type == ServiceType.forName(service.getName())) {
                if (urlResolverParams.perspective == null) {
                    urlResolverParams.perspective = Facing.PUBLIC;
                }

                for (org.openstack4j.model.identity.v3.Endpoint ep : service.getEndpoints()) {

                    if (matches(ep, urlResolverParams)) {
                        return ep.getUrl().toString();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns <code>true</code> for any endpoint that matches a given {@link URLResolverParams}.
     *
     * @param endpoint Endpoint to be resolved
     * @param p URL parameters
     * @return returns if endpoint matches
     */
    private boolean matches(
            org.openstack4j.model.identity.v3.Endpoint endpoint, URLResolverParams p) {
        boolean matches = endpoint.getIface() == p.perspective;
        if (Optional.ofNullable(p.region).isPresent()) {
            matches &= endpoint.getRegion().equals(p.region);
        }
        return matches;
    }

    /**
     * Gets the endpoint url.
     *
     * @param access the current access data source
     * @param endpoint the endpoint
     * @return the endpoint url
     */
    private String getEndpointUrl(Access access, Endpoint endpoint) {
        if (LEGACY_EP_HANDLING) {
            if (endpoint.getAdminURL() != null) {
                if (getPublicIp(access) != null
                        && !getPublicIp(access).equals(endpoint.getAdminURL().getHost())) {
                    return endpoint.getAdminURL()
                            .toString()
                            .replaceAll(endpoint.getAdminURL().getHost(), getPublicIp(access));
                }
                return endpoint.getAdminURL().toString();
            }
        }
        return endpoint.getPublicURL().toString();
    }

    private String getPublicIp(Access access) {
        if (publicHostIp == null) {
            try {
                publicHostIp = new URI(access.getEndpoint()).getHost();
            } catch (URISyntaxException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return publicHostIp;
    }

    private record Key(String uid, ServiceType type, Facing perspective) {

        static Key of(String uid, ServiceType type, Facing perspective, String region) {
            return new Key((region == null) ? uid : uid + region, type, perspective);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Key other = (Key) obj;
            if (perspective != other.perspective) {
                return false;
            }
            if (type != other.type) {
                return false;
            }
            if (uid == null) {
                return other.uid == null;
            } else {
                return uid.equals(other.uid);
            }
        }

        public int hashCode() {
            int result = 1;
            result = 31 * result + (this.perspective == null ? 0 : this.perspective.hashCode());
            result = 31 * result + (this.type == null ? 0 : this.type.hashCode());
            result = 31 * result + (this.uid == null ? 0 : this.uid.hashCode());
            return result;
        }
    }
}
