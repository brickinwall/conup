/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tuscany.sca.http.jetty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.host.http.DefaultResourceServlet;
import org.apache.tuscany.sca.host.http.extensibility.HttpPortAllocator;
import org.apache.tuscany.sca.work.NotificationListener;
import org.apache.tuscany.sca.work.WorkScheduler;
import org.junit.Assert;

/**
 * @version $Rev: 1095242 $ $Date: 2011-04-20 01:19:13 +0100 (Wed, 20 Apr 2011) $
 */
public class JettyServerTestCase extends TestCase {

    private static final String REQUEST1_HEADER = "GET / HTTP/1.0\n" + "Host: localhost\n"
        + "Content-Type: text/xml\n"
        + "Connection: close\n"
        + "Content-Length: ";
    private static final String REQUEST1_CONTENT = "";
    private static final String REQUEST1 = REQUEST1_HEADER + REQUEST1_CONTENT.getBytes().length
        + "\n\n"
        + REQUEST1_CONTENT;

    private static final String REQUEST2_HEADER = "GET /webcontent/test.html HTTP/1.0\n" + "Host: localhost\n"
        + "Content-Type: text/xml\n"
        + "Connection: close\n"
        + "Content-Length: ";
    private static final String REQUEST2_CONTENT = "";
    private static final String REQUEST2 = REQUEST2_HEADER + REQUEST2_CONTENT.getBytes().length
        + "\n\n"
        + REQUEST2_CONTENT;

    private static final int HTTP_PORT = 8085;

    private WorkScheduler workScheduler = new WorkScheduler() {
        private ExecutorService executorService = Executors.newCachedThreadPool();

        public <T extends Runnable> void scheduleWork(T work) {
            executorService.submit(work);
        }

        public <T extends Runnable> void scheduleWork(T work, NotificationListener<T> listener) {
            scheduleWork(work);
        }

        public ExecutorService getExecutorService() {
            return executorService;
        }
    };

    private HttpPortAllocator httpPortAllocator = new DefaultExtensionPointRegistry()
        .getExtensionPoint(UtilityExtensionPoint.class).getUtility(HttpPortAllocator.class);

    /**
     * Verifies requests are properly routed according to the Servlet mapping
     */
    public void testRegisterServletMapping() throws Exception {
        JettyServer service = new JettyServer(workScheduler, httpPortAllocator);
        service.start();
        TestServlet servlet = new TestServlet();
        service.addServletMapping("http://127.0.0.1:" + HTTP_PORT + "/", servlet);
        Socket client = new Socket("127.0.0.1", HTTP_PORT);
        OutputStream os = client.getOutputStream();
        os.write(REQUEST1.getBytes());
        os.flush();
        read(client);
        service.stop();
        assertTrue(servlet.invoked);
    }

    /**
     * Verifies requests are properly routed according to the Servlet mapping
     */
    public void testDeployedURI() throws Exception {
        JettyServer service = new JettyServer(workScheduler, httpPortAllocator);
        service.setDefaultPort(8085);
        service.start();
        TestServlet servlet = new TestServlet();
        String host = InetAddress.getLocalHost().getHostAddress();
        String hostName = InetAddress.getLocalHost().getHostName();
        String url1 = service.addServletMapping("/MyService", servlet);
        Assert.assertEquals("http://" + host + ":8085/MyService", url1);
        String url2 = service.addServletMapping("http://localhost:8086/MyService", servlet);
        Assert.assertEquals("http://localhost:8086/MyService", url2);
        String url3 = service.addServletMapping("http://" + host + ":8087/MyService", servlet);
        Assert.assertEquals("http://" + host + ":8087/MyService", url3);
        String url4 = service.addServletMapping("http://0.0.0.0:8088/MyService", servlet);
        Assert.assertEquals("http://" + host + ":8088/MyService", url4);
        String url5 = service.addServletMapping("http://" + hostName + ":8089/MyService", servlet);
        // Can't reliably test like this as on some hosts registering a servlet with a hostname
        // produces a url with an IP address.
        //Assert.assertEquals("http://" + hostName + ":8089/MyService", url5);

        service.stop();
    }

    public void testRegisterServletMappingSSL() throws Exception {
        System.setProperty("javax.net.ssl.keyStore", "target/test-classes/tuscany.keyStore");
        System.setProperty("javax.net.ssl.keyStorePassword", "apache");
        System.setProperty("jetty.ssl.password", "apache");
        JettyServer service = new JettyServer(workScheduler, httpPortAllocator);
        service.start();
        TestServlet servlet = new TestServlet();
        try {
            service.addServletMapping("https://127.0.0.1:" + HTTP_PORT + "/foo", servlet);
        } finally {
            System.clearProperty("javax.net.ssl.keyStore");
            System.clearProperty("javax.net.ssl.keyStorePassword");
            System.clearProperty("jetty.ssl.password");
        }
        System.setProperty("javax.net.ssl.trustStore", "target/test-classes/tuscany.keyStore");
        System.setProperty("javax.net.ssl.trustStorePassword", "apache");
        URL url = new URL("https://127.0.0.1:8085/foo");
        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.setHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        conn.connect();
        read(conn.getInputStream());

        service.stop();
        assertTrue(servlet.invoked);

    }

    /**
     * Verifies that Servlets can be registered with multiple ports
     */
    public void testRegisterMultiplePorts() throws Exception {
        JettyServer service = new JettyServer(workScheduler, httpPortAllocator);
        service.start();
        TestServlet servlet = new TestServlet();
        service.addServletMapping("http://127.0.0.1:" + HTTP_PORT + "/", servlet);
        TestServlet servlet2 = new TestServlet();
        service.addServletMapping("http://127.0.0.1:" + (HTTP_PORT + 1) + "/", servlet2);
        {
            Socket client = new Socket("127.0.0.1", HTTP_PORT);
            OutputStream os = client.getOutputStream();
            os.write(REQUEST1.getBytes());
            os.flush();
            read(client);
        }
        {
            Socket client = new Socket("127.0.0.1", HTTP_PORT + 1);
            OutputStream os = client.getOutputStream();
            os.write(REQUEST1.getBytes());
            os.flush();
            read(client);
        }

        service.stop();
        assertTrue(servlet.invoked);
        assertTrue(servlet2.invoked);
    }

    public void testUnregisterMapping() throws Exception {
        JettyServer service = new JettyServer(workScheduler, httpPortAllocator);
        service.start();
        TestServlet servlet = new TestServlet();
        String uri = "http://127.0.0.1:" + HTTP_PORT + "/foo";
        service.addServletMapping(uri, servlet);
        service.removeServletMapping(uri);
        try {
            Socket client = new Socket("127.0.0.1", HTTP_PORT);
            OutputStream os = client.getOutputStream();
            os.write(REQUEST1.getBytes());
            os.flush();
            read(client);
            fail("Server still bound to port");
        } catch (ConnectException e) {
        }
        service.stop();
        assertFalse(servlet.invoked);
    }

    public void testRequestSession() throws Exception {
        JettyServer service = new JettyServer(workScheduler, httpPortAllocator);
        service.start();
        TestServlet servlet = new TestServlet();
        service.addServletMapping("http://127.0.0.1:" + HTTP_PORT + "/", servlet);
        Socket client = new Socket("127.0.0.1", HTTP_PORT);
        OutputStream os = client.getOutputStream();
        os.write(REQUEST1.getBytes());
        os.flush();
        read(client);
        service.stop();
        assertTrue(servlet.invoked);
        assertNotNull(servlet.sessionId);
    }

    public void testRestart() throws Exception {
        JettyServer service = new JettyServer(workScheduler, httpPortAllocator);
        service.start();
        service.stop();
        service.stop();
    }

    public void testNoMappings() throws Exception {
        JettyServer service = new JettyServer(workScheduler, httpPortAllocator);
        service.start();
        Exception ex = null;
        try {
            new Socket("127.0.0.1", HTTP_PORT);
        } catch (ConnectException e) {
            ex = e;
        }
        assertNotNull(ex);
        service.stop();
    }

    public void testResourceServlet() throws Exception {
        JettyServer service = new JettyServer(workScheduler, httpPortAllocator);
        service.start();

        String documentRoot = getClass().getClassLoader().getResource("content/test.html").toString();
        documentRoot = documentRoot.substring(0, documentRoot.lastIndexOf('/'));
        DefaultResourceServlet resourceServlet = new DefaultResourceServlet(documentRoot);
        TestResourceServlet servlet = new TestResourceServlet(resourceServlet);
        service.addServletMapping("http://127.0.0.1:" + HTTP_PORT + "/webcontent/*", servlet);

        Socket client = new Socket("127.0.0.1", HTTP_PORT);
        OutputStream os = client.getOutputStream();
        os.write(REQUEST2.getBytes());
        os.flush();

        String document = read(client);
        assertTrue(document.indexOf("<body><p>hello</body>") != -1);

        service.stop();
    }

    public void testDefaultServlet() throws Exception {
        JettyServer service = new JettyServer(workScheduler, httpPortAllocator);
        service.start();

        String documentRoot = getClass().getClassLoader().getResource("content/test.html").toString();
        documentRoot = documentRoot.substring(0, documentRoot.lastIndexOf('/'));
        DefaultResourceServlet resourceServlet = new DefaultResourceServlet(documentRoot);
        service.addServletMapping("http://127.0.0.1:" + HTTP_PORT + "/webcontent/*", resourceServlet);

        Socket client = new Socket("127.0.0.1", HTTP_PORT);
        OutputStream os = client.getOutputStream();
        os.write(REQUEST2.getBytes());
        os.flush();

        String document = read(client);
        assertTrue(document.indexOf("<body><p>hello</body>") != -1);

        service.stop();
    }

    public void testDefaultPort() throws IOException {
        try {
            // Open 9085
            System.setProperty("HTTP_PORT", "9085");
            JettyServer service = new JettyServer(workScheduler, httpPortAllocator);
            assertEquals(9085, service.getDefaultPort());

            // Try to find a free port
            System.setProperty("HTTP_PORT", "0");
            service = new JettyServer(workScheduler, httpPortAllocator);
            int port = service.getDefaultPort();
            assertNotSame(0, port);

            // Try to find the next free port
            ServerSocket socket = null;
            try {
                socket = new ServerSocket(port);
                service = new JettyServer(workScheduler, httpPortAllocator);
                assertNotSame(port, service.getDefaultPort());
            } finally {
                socket.close();
            }
        } finally {
            System.clearProperty("HTTP_PORT");
        }
    }

    private static String read(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        return read(is);
    }

    private static String read(InputStream is) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private class TestServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;
        boolean invoked;
        String sessionId;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            invoked = true;
            sessionId = req.getSession().getId();
            OutputStream writer = resp.getOutputStream();
            try {
                writer.write("result".getBytes());
            } finally {
                writer.close();
            }
        }

    }

    private class TestResourceServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;
        private HttpServlet delegate;

        public TestResourceServlet(HttpServlet delegate) {
            this.delegate = delegate;
        }

        @Override
        public void init() throws ServletException {
            super.init();
            delegate.init();
        }

        @Override
        public void init(ServletConfig config) throws ServletException {
            super.init();
            delegate.init(config);
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            delegate.service(req, resp);
        }

        @Override
        public void destroy() {
            super.destroy();
            delegate.destroy();
        }
    }
}
