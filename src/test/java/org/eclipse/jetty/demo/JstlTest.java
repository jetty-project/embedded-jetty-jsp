package org.eclipse.jetty.demo;

import org.junit.Test;
import support.AgainstJetty;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static support.ResourceFetcher.resource;

public class JstlTest extends AgainstJetty {

    @Test
    public void canServeJspWithTaglib() throws Exception {
        assertThat(resource("http://localhost:8080/test/jstl.jsp"), containsString("10"));
    }
}
