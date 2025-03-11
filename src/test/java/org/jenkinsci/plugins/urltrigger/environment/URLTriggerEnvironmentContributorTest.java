package org.jenkinsci.plugins.urltrigger.environment;

import hudson.EnvVars;
import hudson.matrix.*;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.StreamBuildListener;
import org.jenkinsci.plugins.urltrigger.URLTriggerCause;
import org.jenkinsci.plugins.urltrigger.URLTriggerCauseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WithJenkins
class URLTriggerEnvironmentContributorTest {

    private static JenkinsRule jenkins;

    private BuildListener listener;

    @BeforeAll
    static void setUp(JenkinsRule r) {
        jenkins = r;
    }

    @BeforeEach
    void setup() {
        listener = new StreamBuildListener(jenkins.createTaskListener().getLogger(), Charset.defaultCharset());
    }

    @Test
    void freeStyleProjectTest() throws IOException, InterruptedException, ExecutionException {
        FreeStyleProject p = jenkins.createFreeStyleProject();
        URLTriggerCause cause = new URLTriggerCauseTest( URLTriggerCause.NAME , URLTriggerCause.CAUSE , true );
        cause.setUrlTrigger("http://test.com");
        FreeStyleBuild b = p.scheduleBuild2(0, cause).get();
        EnvVars env = b.getEnvironment(listener);

        assertEquals("http://test.com", env.get("URL_TRIGGER_CAUSE"));
    }

    @Test
    void matrixProjectTest() throws IOException, InterruptedException, ExecutionException {
        EnvVars env;
        MatrixProject p = jenkins.jenkins.createProject(MatrixProject.class, "matrixbuild");
        URLTriggerCause cause = new URLTriggerCauseTest(URLTriggerCause.NAME , URLTriggerCause.CAUSE , true );
        cause.setUrlTrigger("http://test.com");
        // set up 2x2 matrix
        AxisList axes = new AxisList();
        axes.add(new TextAxis("db","mysql","oracle"));
        axes.add(new TextAxis("direction","north","south"));
        p.setAxes(axes);

        MatrixBuild build = p.scheduleBuild2(0, cause).get();
        List<MatrixRun> runs = build.getRuns();
        for (MatrixRun run : runs) {
            env = run.getEnvironment(listener);
            assertNotNull(env.get("db"));
            assertEquals("http://test.com", env.get("URL_TRIGGER_CAUSE"));
        }
    }

}
