//
//  ========================================================================
//  Copyright (c) 1995-2016 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.eclipse.jetty.util.log.Log;

import com.acme.DateServlet;

/**
 * Example of using JSP's with embedded jetty and using a
 * lighter-weight ServletContextHandler instead of a WebAppContext. 
 * 
 * This example is somewhat odd in that it uses custom tag libs which reside
 * in a WEB-INF directory, even though WEB-INF is not meaningful to
 * a ServletContextHandler. This just shows that once we have
 * properly initialized the jsp engine, you can even use this type of
 * custom taglib, even if you don't have a full-fledged webapp.
 * 
 */
public class Main
{
    // Resource path pointing to where the WEBROOT is
    private static final String WEBROOT_INDEX = "/webroot/";
    
    /**
     * JspStarter
     * 
     * This is added as a bean that is a jetty LifeCycle on the ServletContextHandler.
     * This bean's doStart method will be called as the ServletContextHandler starts,
     * and will call the ServletContainerInitializer for the jsp engine.
     *
     */
    public static class JspStarter extends AbstractLifeCycle implements ServletContextHandler.ServletContainerInitializerCaller
    {
        JettyJasperInitializer sci;
        ServletContextHandler context;

        
        public JspStarter (ServletContextHandler context)
        {
            this.sci = new JettyJasperInitializer();
            this.context = context;
            this.context.setAttribute("org.apache.tomcat.JarScanner", new StandardJarScanner());
        }

        @Override
        protected void doStart() throws Exception
        {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(context.getClassLoader());
            try
            {
                sci.onStartup(null, context.getServletContext());   
                super.doStart();
            }
            finally
            {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        int port = 8080;
        LoggingUtil.config();
        Log.setLog(new JavaUtilLog());

        Main main = new Main(port);
        main.start();
        main.waitForInterrupt();
    }

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private int port;
    private Server server;
    private URI serverURI;

    public Main(int port)
    {
        this.port = port;
    }

    public URI getServerURI()
    {
        return serverURI;
    }

    public void start() throws Exception
    {
        server = new Server();
        ServerConnector connector = connector();
        server.addConnector(connector);

        URI baseUri = getWebRootResourceUri(); 
        ServletContextHandler sch  = getServletContextHandler(baseUri, getScratchDir());
        server.setHandler(sch);

        // Start Server
        server.start();

        // Show server state
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine(server.dump());
        }
        this.serverURI = getServerUri(connector);
    }

    private ServerConnector connector()
    {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        return connector;
    }

    private URI getWebRootResourceUri() throws FileNotFoundException, URISyntaxException
    {
        URL indexUri = this.getClass().getResource(WEBROOT_INDEX);
        if (indexUri == null)
        {
            throw new FileNotFoundException("Unable to find resource " + WEBROOT_INDEX);
        }
        // Points to wherever /webroot/ (the resource) is
        return indexUri.toURI();
    }

    /**
     * Establish Scratch directory for the servlet context (used by JSP compilation)
     */
    private File getScratchDir() throws IOException
    {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

        if (!scratchDir.exists())
        {
            if (!scratchDir.mkdirs())
            {
                throw new IOException("Unable to create scratch directory: " + scratchDir);
            }
        }
        return scratchDir;
    }

  

    /** 
     * Create a ServletContextHandler and configure it.
     * 
     * @param baseUri the uri of the static content
     * @param scratchDir the scratch output directory
     * @return the freshly created ServletContextHandler
     */
    private ServletContextHandler getServletContextHandler(URI baseUri, File scratchDir)
    {
        ServletContextHandler sch = new ServletContextHandler(ServletContextHandler.SESSIONS);
        sch.setContextPath("/");
        sch.setAttribute("javax.servlet.context.tempdir", scratchDir);
        sch.setResourceBase(baseUri.toASCIIString());
        sch.setAttribute(InstanceManager.class.getName(),  new SimpleInstanceManager());
        //add a bean that will call the jsp engine's ServletContaineInitializer as the context starts
        sch.addBean(new JspStarter(sch));
        sch.setClassLoader(getUrlClassLoader());
        sch.addServlet(jspServletHolder(), "*.jsp");
        // Add Application Servlets
        sch.addServlet(DateServlet.class, "/date/");
        sch.addServlet(exampleJspFileMappedServletHolder(), "/test/foo/");
        sch.addServlet(defaultServletHolder(baseUri), "/");
        return sch;
    }
  

    /**
     * Set Classloader of Context to be sane (needed for JSTL)
     * JSP requires a non-System classloader, this simply wraps the
     * embedded System classloader in a way that makes it suitable
     * for JSP to use
     */
    private ClassLoader getUrlClassLoader()
    {
        ClassLoader jspClassLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
        return jspClassLoader;
    }

    /**
     * Create JSP Servlet (must be named "jsp")
     */
    private ServletHolder jspServletHolder()
    {
        ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
        holderJsp.setInitOrder(0);
        holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
        holderJsp.setInitParameter("fork", "false");
        holderJsp.setInitParameter("xpoweredBy", "false");
        holderJsp.setInitParameter("compilerTargetVM", "1.7");
        holderJsp.setInitParameter("compilerSourceVM", "1.7");
        holderJsp.setInitParameter("keepgenerated", "true");
        return holderJsp;
    }

    /**
     * Create Example of mapping jsp to path spec
     */
    private ServletHolder exampleJspFileMappedServletHolder()
    {
        ServletHolder holderAltMapping = new ServletHolder();
        holderAltMapping.setName("foo.jsp");
        holderAltMapping.setForcedPath("/test/foo/foo.jsp");
        return holderAltMapping;
    }

    /**
     * Create Default Servlet (must be named "default")
     */
    private ServletHolder defaultServletHolder(URI baseUri)
    {
        ServletHolder holderDefault = new ServletHolder("default", DefaultServlet.class);
        LOG.info("Base URI: " + baseUri);
        holderDefault.setInitParameter("resourceBase", baseUri.toASCIIString());
        holderDefault.setInitParameter("dirAllowed", "true");
        return holderDefault;
    }

    /**
     * Establish the Server URI
     */
    private URI getServerUri(ServerConnector connector) throws URISyntaxException
    {
        String scheme = "http";
        for (ConnectionFactory connectFactory : connector.getConnectionFactories())
        {
            if (connectFactory.getProtocol().equals("SSL-http"))
            {
                scheme = "https";
            }
        }
        String host = connector.getHost();
        if (host == null)
        {
            host = "localhost";
        }
        int port = connector.getLocalPort();
        serverURI = new URI(String.format("%s://%s:%d/", scheme, host, port));
        LOG.info("Server URI: " + serverURI);
        return serverURI;
    }

    public void stop() throws Exception
    {
        server.stop();
    }

    /**
     * Cause server to keep running until it receives a Interrupt.
     * <p>
     * Interrupt Signal, or SIGINT (Unix Signal), is typically seen as a result of a kill -TERM {pid} or Ctrl+C
     * @throws InterruptedException if interrupted
     */
    public void waitForInterrupt() throws InterruptedException
    {
        server.join();
    }
}
