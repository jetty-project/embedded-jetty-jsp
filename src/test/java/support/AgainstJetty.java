package support;

import org.eclipse.jetty.demo.Main;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;

public class AgainstJetty {

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
}
