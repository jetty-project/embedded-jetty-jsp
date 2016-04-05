package org.eclipse.jetty.demo;

import org.junit.Test;
import support.AgainstJetty;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static support.ResourceFetcher.resource;

public class TagClassTest extends AgainstJetty {

    @Test
    public void canServeJspWithCustomTagDefinedInJavaClass() throws Exception {
        assertThat(resource("http://localhost:8080/test/tagclass.jsp"), containsString("Leaving scripting behind"));
    }
}
