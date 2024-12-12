package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScriptsRepoTest {

    final String repoUrl = "repoUrl";
    final String branch = "branch";
    final String scriptsPath = "scriptsPath";
    private ScriptsRepo test;

    @BeforeEach
    void setUp() throws Exception {
        test = new ScriptsRepo();
        test.setRepoUrl(repoUrl);
        test.setBranch(branch);
        test.setScriptsPath(scriptsPath);
    }

    @Test
    void testGetters() {
        assertThat(test.getRepoUrl()).isEqualTo(repoUrl);
        assertThat(test.getBranch()).isEqualTo(branch);
        assertThat(test.getScriptsPath()).isEqualTo(scriptsPath);
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(test).isNotEqualTo(obj);
        assertThat(test.hashCode()).isNotEqualTo(obj.hashCode());
        ScriptsRepo test1 = new ScriptsRepo();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        ScriptsRepo test2 = new ScriptsRepo();
        test2.setRepoUrl(repoUrl);
        test2.setBranch(branch);
        test2.setScriptsPath(scriptsPath);
        assertThat(test).isEqualTo(test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
        assertThat(test.canEqual(new ScriptsRepo())).isTrue();
    }

    @Test
    void testToString() throws Exception {
        String result =
                "ScriptsRepo(repoUrl="
                        + repoUrl
                        + ", branch="
                        + branch
                        + ", scriptsPath="
                        + scriptsPath
                        + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
