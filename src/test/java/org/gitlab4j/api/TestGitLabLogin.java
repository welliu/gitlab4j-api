package org.gitlab4j.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import org.gitlab4j.api.GitLabApi.ApiVersion;
import org.gitlab4j.api.models.Version;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * In order for these tests to run you must set the following properties in test-gitlab4j.properties
 * 
 * TEST_HOST_URL
 * TEST_USERNAME
 * TEST_PASSWORD
 * TEST_PRIVATE_TOKEN
 * 
 * If any of the above are NULL, all tests in this class will be skipped.
 */
public class TestGitLabLogin {

    // The following needs to be set to your test repository
    private static final String TEST_USERNAME;
    private static final String TEST_PASSWORD;
    private static final String TEST_HOST_URL;
    private static final String TEST_PRIVATE_TOKEN;
    static {
        TEST_USERNAME = TestUtils.getProperty("TEST_USERNAME");
        TEST_PASSWORD = TestUtils.getProperty("TEST_PASSWORD");
        TEST_HOST_URL = TestUtils.getProperty("TEST_HOST_URL");
        TEST_PRIVATE_TOKEN = TestUtils.getProperty("TEST_PRIVATE_TOKEN");
    }

    private static String problems = "";
    private static boolean hasSession;

    public TestGitLabLogin() {
        super();
    }

    @BeforeClass
    public static void setup() {

        problems = "";

        if (TEST_USERNAME == null || TEST_USERNAME.trim().length() == 0) {
            problems += "TEST_USERNAME cannot be empty\n";
        }

        if (TEST_PASSWORD == null || TEST_PASSWORD.trim().length() == 0) {
            problems += "TEST_PASSWORD cannot be empty\n";
        }

        if (TEST_HOST_URL == null || TEST_HOST_URL.trim().length() == 0) {
            problems += "TEST_HOST_URL cannot be empty\n";
        }

        if (TEST_PRIVATE_TOKEN == null || TEST_PRIVATE_TOKEN.trim().length() == 0) {
            problems += "TEST_PRIVATE_TOKEN cannot be empty\n";
        }

        if (problems.isEmpty()) {
            try {
                GitLabApi gitLabApi = new GitLabApi(ApiVersion.V4, TEST_HOST_URL, TEST_PRIVATE_TOKEN);
                Version version = gitLabApi.getVersion();
                String[] parts = version.getVersion().split(".", -1);
                if (parts.length == 3) {
                    if (Integer.parseInt(parts[0]) < 10 || 
                            (Integer.parseInt(parts[0]) == 10 && Integer.parseInt(parts[1]) < 2)) {
                        hasSession = true;
                    }
                }
            } catch (Exception e) {                
            }
        }

        if (!problems.isEmpty()) {
            System.err.print(problems);
        }
    }

    @Before
    public void beforeMethod() {
        assumeTrue(problems != null && problems.isEmpty());
    }

    @Test
    public void testSession() throws GitLabApiException {

        assumeTrue(hasSession);
        GitLabApi gitLabApi = GitLabApi.login(ApiVersion.V4, TEST_HOST_URL, TEST_USERNAME, TEST_PASSWORD);
        assertNotNull(gitLabApi);
        assertNotNull(gitLabApi.getSession());
        assertEquals(TEST_PRIVATE_TOKEN, gitLabApi.getSession().getPrivateToken());
    }

    @Test
    public void testSessionV3() throws GitLabApiException {

        assumeTrue(hasSession);
        GitLabApi gitLabApi = GitLabApi.login(ApiVersion.V3, TEST_HOST_URL, TEST_USERNAME, TEST_PASSWORD);
        assertNotNull(gitLabApi);
        assertNotNull(gitLabApi.getSession());
        assertEquals(TEST_PRIVATE_TOKEN, gitLabApi.getSession().getPrivateToken());
    }

    @Test
    public void testSessionFallover() throws GitLabApiException {

        assumeFalse(hasSession);
        GitLabApi gitLabApi = GitLabApi.login(ApiVersion.V4, TEST_HOST_URL, TEST_USERNAME, TEST_PASSWORD);
        assertNotNull(gitLabApi);
        Version version = gitLabApi.getVersion();
        assertNotNull(version);
    }

    @Test
    public void testOauth2Login() throws GitLabApiException {

        GitLabApi gitLabApi = GitLabApi.oauth2Login(TEST_HOST_URL, TEST_USERNAME, TEST_PASSWORD, null, null, true);
        assertNotNull(gitLabApi);
        Version version = gitLabApi.getVersion();
        assertNotNull(version);
    }
}
