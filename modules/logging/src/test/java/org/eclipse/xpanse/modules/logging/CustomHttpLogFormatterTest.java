package org.eclipse.xpanse.modules.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.Precorrelation;

class CustomHttpLogFormatterTest {

    String baseUrl = "/xpanse";
    String requestId = "requestId";
    private CustomHttpLogFormatter customHttpLogFormatterUnderTest;

    private CustomHttpLogWriter customHttpLogWriterUnderTest;

    @BeforeEach
    void setUp() {
        customHttpLogFormatterUnderTest = new CustomHttpLogFormatter();
        customHttpLogWriterUnderTest = new CustomHttpLogWriter(true);
    }

    @Test
    void testRequestLogFormatAndWrite() throws Exception {
        // Setup
        final HttpRequest request = getHttpRequest(baseUrl, "GET", "id=1", "");

        // Run the test
        final String result = customHttpLogFormatterUnderTest.format(getPrecorrelation(), request);

        // Verify the results
        assertThat(result).isEqualTo("Request: GET /xpanse?id=1");
        customHttpLogWriterUnderTest.write(getPrecorrelation(), "request");
    }

    @Test
    void testResponseLogFormatAndWrite() throws Exception {
        // Setup
        final Correlation correlation = getCorrelation();
        final HttpResponse response = getHttpResponse(200, "OK", "{}");

        // Run the test
        final String result = customHttpLogFormatterUnderTest.format(correlation, response);

        // Verify the results
        assertThat(result).isEqualTo("Response: 200 OK Duration: 1000ms  {}");

        customHttpLogWriterUnderTest.write(correlation, "request");
    }

    @Test
    HttpRequest getHttpRequest(String baseUrl, String httpMethod, String query, String body) {
        // Setup
        return new HttpRequest() {
            @Override
            public String getRemote() {
                return "http://localhost:8080";
            }

            @Override
            public String getMethod() {
                return httpMethod;
            }

            @Override
            public String getScheme() {
                return "http";
            }

            @Override
            public String getHost() {
                return "localhost";
            }

            @Override
            public Optional<Integer> getPort() {
                return Optional.of(8080);
            }

            @Override
            public String getPath() {
                return baseUrl;
            }

            @Override
            public String getQuery() {
                return query;
            }

            @Override
            public HttpRequest withBody() {
                return null;
            }

            @Override
            public HttpRequest withoutBody() {
                return null;
            }

            @Override
            public Origin getOrigin() {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public byte[] getBody() {
                return body.getBytes(StandardCharsets.UTF_8);
            }
        };
    }

    @Test
    HttpResponse getHttpResponse(int statusCode, String reason, String body) {
        // Setup
        return new HttpResponse() {

            @Override
            public int getStatus() {
                return statusCode;
            }

            @Override
            public HttpResponse withBody() {
                return null;
            }

            @Override
            public HttpResponse withoutBody() {
                return null;
            }

            @Override
            public String getReasonPhrase() {
                return reason;
            }

            @Override
            public Origin getOrigin() {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public byte[] getBody() {
                return body.getBytes(StandardCharsets.UTF_8);
            }
        };
    }

    Precorrelation getPrecorrelation() {
        return new Precorrelation() {
            @Override
            public String getId() {
                return requestId;
            }

            @Override
            public Instant getStart() {
                return Instant.now();
            }

            @Override
            public Correlation correlate() {
                return null;
            }
        };
    }

    Correlation getCorrelation() {
        return new Correlation() {
            @Override
            public Instant getEnd() {
                return Instant.now().plusSeconds(1);
            }

            @Override
            public Duration getDuration() {
                return Duration.ofSeconds(1);
            }

            @Override
            public String getId() {
                return requestId;
            }

            @Override
            public Instant getStart() {
                return Instant.now();
            }

            @Override
            public Correlation correlate() {
                return Correlation.super.correlate();
            }
        };
    }
}
