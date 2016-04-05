package org.eclipse.jetty.demo;

import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class JspTest {

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
        assertThat(resource("http://localhost:8080/test/jstl.jsp"), containsString("10"));
    }

    @Test
    public void canServeJspWithCustomTag() throws Exception {
        assertThat(resource("http://localhost:8080/test/tagfile.jsp"), containsString("<td><b>Panel 1</b></td>"));
    }

    @Test
    public void canServeJspWithCustomTagDefinedInJavaClassAndTldInWebInf() throws Exception {
        assertThat(resource("http://localhost:8080/test/tagclass-with-tld-in-web-inf.jsp"), containsString("Leaving scripting behind"));
    }

    @Test
    public void canServeJspWithCustomTagDefinedInJavaClassAndTldInWebInfTags() throws Exception {
        assertThat(resource("http://localhost:8080/test/tagclass-with-tld-in-web-inf-tags.jsp"), containsString("Leaving scripting behind"));
    }

    private String resource(String spec) throws Exception {
        URL url = new URL( spec );
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = connection.getInputStream();
        byte[] response = new byte[ inputStream.available() ];
        inputStream.read(response);

        return new String(response);
    }
}
