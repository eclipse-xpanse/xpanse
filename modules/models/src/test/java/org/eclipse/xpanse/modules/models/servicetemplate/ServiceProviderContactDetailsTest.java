package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceProviderContactDetailsTest {

    private final List<String> email = List.of("test@email");
    private final List<String> phone = List.of("110000000");
    private final List<String> chat = List.of("chat");
    private final List<String> website = List.of("website");

    private ServiceProviderContactDetails test;

    @BeforeEach
    void setUp() throws Exception {
        test = new ServiceProviderContactDetails();
        test.setEmail(email);
        test.setPhone(phone);
        test.setChat(chat);
        test.setWebsite(website);
    }

    @Test
    void testGetters() {
        assertThat(test.getEmail()).isEqualTo(email);
        assertThat(test.getPhone()).isEqualTo(phone);
        assertThat(test.getChat()).isEqualTo(chat);
        assertThat(test.getWebsite()).isEqualTo(website);
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(test).isNotEqualTo(obj);
        assertThat(test.hashCode()).isNotEqualTo(obj.hashCode());
        ServiceProviderContactDetails test1 = new ServiceProviderContactDetails();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        ServiceProviderContactDetails test2 = new ServiceProviderContactDetails();
        test2.setEmail(email);
        test2.setPhone(phone);
        test2.setChat(chat);
        test2.setWebsite(website);
        assertThat(test).isEqualTo(test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
        assertThat(test.canEqual(new ServiceProviderContactDetails())).isTrue();
    }

    @Test
    void testToString() throws Exception {
        String result = "ServiceProviderContactDetails(email=" + email
                + ", phone=" + phone + ", chat=" + chat
                + ", website=" + website + ")";
        assertThat(test.toString()).isEqualTo(result);
    }

}