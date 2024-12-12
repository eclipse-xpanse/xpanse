package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceProviderContactDetailsTest {

    private final List<String> emails = List.of("test@email");
    private final List<String> phones = List.of("110000000");
    private final List<String> chats = List.of("chat");
    private final List<String> websites = List.of("website");

    private ServiceProviderContactDetails test;

    @BeforeEach
    void setUp() throws Exception {
        test = new ServiceProviderContactDetails();
        test.setEmails(emails);
        test.setPhones(phones);
        test.setChats(chats);
        test.setWebsites(websites);
    }

    @Test
    void testGetters() {
        assertThat(test.getEmails()).isEqualTo(emails);
        assertThat(test.getPhones()).isEqualTo(phones);
        assertThat(test.getChats()).isEqualTo(chats);
        assertThat(test.getWebsites()).isEqualTo(websites);
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
        test2.setEmails(emails);
        test2.setPhones(phones);
        test2.setChats(chats);
        test2.setWebsites(websites);
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
        String result =
                "ServiceProviderContactDetails(emails="
                        + emails
                        + ", phones="
                        + phones
                        + ", chats="
                        + chats
                        + ", websites="
                        + websites
                        + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
