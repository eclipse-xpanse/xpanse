package org.eclipse.xpanse.plugins.flexibleengine.monitor.utils;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataResponse;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import com.huaweicloud.sdk.core.http.HttpMethod;
import com.huaweicloud.sdk.core.internal.model.KeystoneListProjectsResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Objects;
import org.apache.http.client.methods.HttpRequestBase;
import org.eclipse.xpanse.modules.logging.RestTemplateLoggingInterceptor;
import org.eclipse.xpanse.plugins.flexibleengine.FlexibleEngineRestTemplateConfig;
import org.eclipse.xpanse.plugins.flexibleengine.RetryTemplateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RetryTemplateService.class, FlexibleEngineRestTemplateConfig.class,
        RestTemplateLoggingInterceptor.class})
class RetryTemplateServiceTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                        .extensions(new ResponseTemplateTransformer(TemplateEngine.defaultTemplateEngine(),
                                false, new ClasspathFileSource("src/test/resources/mappings"),
                                Collections.emptyList())))
            .build();

    @Autowired
    private RetryTemplateService retryTemplateService;


    @Test
    void testQueryProjectInfo() throws URISyntaxException {
        // Setup
        final HttpRequestBase httpRequestBase =
                getHttpRequestBase(wireMockExtension.baseUrl() + "/v3/projects",
                        HttpMethod.GET.name());

        // Run the test
        final KeystoneListProjectsResponse result =
                retryTemplateService.queryProjectInfo(httpRequestBase);

        // Verify the results
        Assertions.assertFalse(Objects.isNull(result));
        Assertions.assertEquals(1, result.getProjects().size());
        String projectId = "cddffdf04cf441c8b94aac85dfdc2a69";
        Assertions.assertEquals(projectId, result.getProjects().get(0).getId());
    }

    @Test
    void testQueryMetricDataByNamespaceCpuUsage() throws URISyntaxException {
        // Setup
        final HttpRequestBase httpRequestBase =
                getHttpRequestBase(
                        wireMockExtension.baseUrl() +
                                "/V1.0/project_id/metric-data?namespace=cpu_usage",
                        HttpMethod.GET.name());

        // Run the test
        final ShowMetricDataResponse result =
                retryTemplateService.queryMetricData(httpRequestBase);

        // Verify the results
        Assertions.assertFalse(Objects.isNull(result));
        Assertions.assertEquals(result.getMetricName(), "cpu_usage");
        Assertions.assertEquals(result.getDatapoints().size(), 5);
    }


    @Test
    void testQueryMetricDataByNamespaceMemUsedPercent() throws URISyntaxException {
        // Setup
        final HttpRequestBase httpRequestBase =
                getHttpRequestBase(
                        wireMockExtension.baseUrl() +
                                "/V1.0/project_id/metric-data?namespace=mem_usedPercent",
                        HttpMethod.GET.name());

        // Run the test
        final ShowMetricDataResponse result =
                retryTemplateService.queryMetricData(httpRequestBase);

        // Verify the results
        Assertions.assertFalse(Objects.isNull(result));
        Assertions.assertEquals(result.getMetricName(), "mem_usedPercent");
        Assertions.assertEquals(result.getDatapoints().size(), 5);
    }

    @Test
    void testQueryMetricDataByNamespaceNetBitRecv() throws URISyntaxException {
        // Setup
        final HttpRequestBase httpRequestBase =
                getHttpRequestBase(
                        wireMockExtension.baseUrl() +
                                "/V1.0/project_id/metric-data?namespace=net_bitRecv",
                        HttpMethod.GET.name());

        // Run the test
        final ShowMetricDataResponse result =
                retryTemplateService.queryMetricData(httpRequestBase);

        // Verify the results
        Assertions.assertFalse(Objects.isNull(result));
        Assertions.assertEquals(result.getMetricName(), "net_bitRecv");
        Assertions.assertEquals(result.getDatapoints().size(), 5);
    }

    @Test
    void testQueryMetricDataByNamespaceNetBitSend() throws URISyntaxException {
        // Setup
        final HttpRequestBase httpRequestBase =
                getHttpRequestBase(
                        wireMockExtension.baseUrl() +
                                "/V1.0/project_id/metric-data?namespace=net_bitSent",
                        HttpMethod.GET.name());

        // Run the test
        final ShowMetricDataResponse result =
                retryTemplateService.queryMetricData(httpRequestBase);

        // Verify the results
        Assertions.assertFalse(Objects.isNull(result));
        Assertions.assertEquals(result.getMetricName(), "net_bitSent");
        Assertions.assertEquals(result.getDatapoints().size(), 5);
    }

    @Test
    void testQueryMetricItemList() throws URISyntaxException {
        // Setup
        final HttpRequestBase httpRequestBase =
                getHttpRequestBase(wireMockExtension.baseUrl() + "/V1.0/project_id/metrics",
                        HttpMethod.GET.name());

        // Run the test
        final ListMetricsResponse result =
                retryTemplateService.queryMetricItemList(httpRequestBase);

        // Verify the results
        Assertions.assertFalse(Objects.isNull(result));
        Assertions.assertEquals(42, result.getMetrics().size());
    }

    @Test
    void testBatchQueryMetricData() throws URISyntaxException {
        // Setup
        final HttpRequestBase httpRequestBase =
                getHttpRequestBase(
                        wireMockExtension.baseUrl() + "/V1.0/project_id/batch-query-metric-data",
                        HttpMethod.GET.name());

        // Run the test
        final BatchListMetricDataResponse result =
                retryTemplateService.batchQueryMetricData(httpRequestBase, "{}");

        // Verify the results
        Assertions.assertFalse(Objects.isNull(result));
        Assertions.assertEquals(16, result.getMetrics().size());

        Assertions.assertEquals("net_bitRecv", result.getMetrics().get(0).getMetricName());
        Assertions.assertEquals("mem_usedPercent", result.getMetrics().get(1).getMetricName());
        Assertions.assertEquals("net_bitSent", result.getMetrics().get(2).getMetricName());
        Assertions.assertEquals("cpu_usage", result.getMetrics().get(3).getMetricName());
    }


    @Test
    void testRecoverGetRequest() throws URISyntaxException {
        // Setup
        final HttpRequestBase httpRequestBase =
                getHttpRequestBase(wireMockExtension.baseUrl() + "/test/get/error",
                        HttpMethod.GET.name());
        // Run the test
        final Object result = retryTemplateService.queryProjectInfo(httpRequestBase);

        // Verify the results
        assertThat(result).isNull();
    }

    @Test
    void testRecoverPostRequest() throws URISyntaxException {
        // Setup
        final HttpRequestBase httpRequestBase =
                getHttpRequestBase(
                        wireMockExtension.baseUrl() + "/test/post/error",
                        HttpMethod.GET.name());

        // Run the test
        final BatchListMetricDataResponse result =
                retryTemplateService.batchQueryMetricData(httpRequestBase, "{}");

        // Verify the results
        assertThat(result).isNull();
    }

    private HttpRequestBase getHttpRequestBase(String url,
                                               String httpMethod) throws URISyntaxException {
        HttpRequestBase httpRequestBase = new HttpRequestBase() {
            @Override
            public String getMethod() {
                return httpMethod;
            }
        };
        httpRequestBase.setURI(new URI(url));
        httpRequestBase.setHeader("Content-Type", "application/json");
        return httpRequestBase;
    }
}
