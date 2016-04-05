package org.eclipse.jetty.demo;

import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import support.Resource;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class JstlTest {

    protected Server server;
    private Main main;

    @Before
    public void aJettyServer() throws Exception {
        main = new Main(8080);
        main.start();
    }

    @After
    public void stopServer() throws Exception {
        main.stop();
    }

    @Test
    public void canServeJspWithTaglib() throws Exception {
        assertThat(Resource.withUrl("http://localhost:8080/test/jstl.jsp"), containsString("10"));
    }

    @Test
    public void canServeJspWithCustomTag() throws Exception {
        assertThat(Resource.withUrl("http://localhost:8080/test/tagfile.jsp"), containsString("<td><b>Panel 1</b></td>"));
    }
}
