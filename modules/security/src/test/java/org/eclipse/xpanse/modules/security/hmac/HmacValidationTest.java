package org.eclipse.xpanse.modules.security.hmac;

import java.io.IOException;
import java.io.InputStream;
import org.assertj.core.api.Assertions;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientAuthenticationFailedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import wiremock.org.apache.commons.io.IOUtils;

@ExtendWith(SpringExtension.class)
@TestPropertySource(
        properties = {
            "xpanse.webhook.hmac.request.signing.key=1c30e4b1fad574f88572e25d0da03f34365f4ae92eda22bfd3a8c53cb5102f27",
        })
@ContextConfiguration(classes = {HmacSignatureHeaderManage.class})
public class HmacValidationTest {

    @Autowired HmacSignatureHeaderManage hmacSignatureHeaderManage;

    @Test
    void testHmacSignatureHeader() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/webhook/terra-boot/order/bf83d609-79f0-486f-a883-6c3187589c90");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setProtocol("http");
        request.setContentType("application/json");
        request.setCharacterEncoding("UTF-8");
        request.setContent(getRequestBodyAsByteArray());
        request.addHeader(
                "x-signature",
                "algorithm=HmacSHA256;headers=x-nonce-signature"
                    + " x-timestamp-signature;signature=848bd434987cc70af0a0892eaf386931ffff4fc70f7415671304e9ea91546bd9");
        request.addHeader("x-nonce-signature", "6f9e88eab38a9c5606dda8813f98706b");
        request.addHeader("x-timestamp-signature", "1738854803851");
        RereadbleBodyHttpServletRequest newRequest = new RereadbleBodyHttpServletRequest(request);
        Assertions.assertThat(this.hmacSignatureHeaderManage.validateHmacSignature(newRequest))
                .isTrue();
    }

    @Test
    void testInvalidHmacAlgorithmThrowsError() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/webhook/terra-boot/order/bf83d609-79f0-486f-a883-6c3187589c90");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setProtocol("http");
        request.setContentType("application/json");
        request.setCharacterEncoding("UTF-8");
        request.setContent(getRequestBodyAsByteArray());
        request.addHeader(
                "x-signature",
                "algorithm=HmacSHA256xxx;headers=x-nonce-signature"
                    + " x-timestamp-signature;signature=d631342033398238dad36efe2d5df0fc7c6d52e028494f17f930c353dc5c16ad");
        request.addHeader("x-nonce-signature", "6f9e88eab38a9c5606dda8813f98706b");
        request.addHeader("x-timestamp-signature", "1738854803851");
        RereadbleBodyHttpServletRequest newRequest = new RereadbleBodyHttpServletRequest(request);
        Assertions.assertThatThrownBy(
                        () -> this.hmacSignatureHeaderManage.validateHmacSignature(newRequest))
                .isInstanceOf(ClientAuthenticationFailedException.class)
                .hasMessage("Invalid HMAC algorithm: HmacSHA256xxx");
    }

    @Test
    void testMissingSignatureHeaderThrowsError() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/webhook/terra-boot/order/bf83d609-79f0-486f-a883-6c3187589c90");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setProtocol("http");
        request.setContentType("application/json");
        request.setCharacterEncoding("UTF-8");
        request.setContent(getRequestBodyAsByteArray());
        request.addHeader(
                "x-signature",
                "algorithm=HmacSHA256;headers=x-nonce-signature"
                    + " x-timestamp-signature;signature=d631342033398238dad36efe2d5df0fc7c6d52e028494f17f930c353dc5c16ad");
        request.addHeader("x-timestamp-signature", "1738854803851");
        RereadbleBodyHttpServletRequest newRequest = new RereadbleBodyHttpServletRequest(request);
        Assertions.assertThatThrownBy(
                        () -> this.hmacSignatureHeaderManage.validateHmacSignature(newRequest))
                .isInstanceOf(ClientAuthenticationFailedException.class)
                .hasMessage("Invalid or missing HMAC header: x-nonce-signature");
    }

    @Test
    void testWrongSignatureHeaderFormatThrowsError() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/webhook/terra-boot/order/bf83d609-79f0-486f-a883-6c3187589c90");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setProtocol("http");
        request.setContentType("application/json");
        request.setCharacterEncoding("UTF-8");
        request.setContent(getRequestBodyAsByteArray());
        request.addHeader(
                "x-signature",
                "algorithm=HmacSHA256;headersxxx=x-nonce-signature"
                    + " x-timestamp-signature;signature=d631342033398238dad36efe2d5df0fc7c6d52e028494f17f930c353dc5c16ad");
        request.addHeader("x-timestamp-signature", "1738854803851");
        RereadbleBodyHttpServletRequest newRequest = new RereadbleBodyHttpServletRequest(request);
        Assertions.assertThatThrownBy(
                        () -> this.hmacSignatureHeaderManage.validateHmacSignature(newRequest))
                .isInstanceOf(ClientAuthenticationFailedException.class)
                .hasMessage("Invalid HMAC signature. Missing key in x-signature header: headers");
    }

    private byte[] getRequestBodyAsByteArray() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        byte[] bytes;
        try (InputStream inputStream =
                classLoader.getResourceAsStream("webhook-failure-request.json")) {
            assert inputStream != null;
            bytes = IOUtils.toByteArray(inputStream);
        }
        return bytes;
    }
}
