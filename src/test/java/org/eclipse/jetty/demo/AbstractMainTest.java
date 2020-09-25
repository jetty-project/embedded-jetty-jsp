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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractMainTest
{
    private Main main;

    @BeforeEach
    public void aJettyServer() throws Exception
    {
        main = new Main(8080);
        main.start();
    }

    @AfterEach
    public void stopServer() throws Exception
    {
        main.stop();
    }

    public String resourceWithUrl(String uri) throws Exception
    {
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        InputStream inputStream = connection.getInputStream();
        byte[] response = new byte[inputStream.available()];
        inputStream.read(response);

        return new String(response);
    }
}
