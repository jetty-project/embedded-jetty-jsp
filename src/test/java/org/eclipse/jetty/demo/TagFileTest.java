package org.eclipse.jetty.demo;

import org.junit.Test;
import support.AgainstJetty;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static support.ResourceFetcher.resource;

public class TagFileTest extends AgainstJetty {

    @Test
    public void canServeJspWithCustomTag() throws Exception {
        assertThat(resource("http://localhost:8080/test/tagfile.jsp"), containsString("<td><b>Panel 1</b></td>"));
    }
}
