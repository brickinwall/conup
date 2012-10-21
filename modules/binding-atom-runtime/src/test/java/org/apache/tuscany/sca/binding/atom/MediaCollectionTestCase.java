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
package org.apache.tuscany.sca.binding.atom;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.tuscany.sca.host.http.client.HttpClientFactory;
import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests use of server provided entry entity tags for Atom binding in Tuscany.
 * Tests conditional gets (e.g. get if-none-match) or conditional posts (post if-match)
 * using entity tags or last modified header entries. 
 * Uses the SCA provided Provider composite to act as a server.
 * Uses the Abdera provided Client to act as a client.
 * 
 * @version $Rev: 1043774 $ $Date: 2010-12-08 23:45:29 +0000 (Wed, 08 Dec 2010) $
 */
public class MediaCollectionTestCase {
    public final static String providerURI = "http://localhost:8084/receipt";

    protected static Node scaProviderNode;

    protected static CustomerClient testService;
    protected static Abdera abdera;
    protected static AbderaClient client;
    protected static Parser abderaParser;
    protected static String eTag;
    protected static Date lastModified;
    protected static String mediaId;
    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z"); // RFC 822 date time

    @BeforeClass
    public static void init() throws Exception {
        try {
            //System.out.println(">>>MediaCollectionTestCase.init");
            String contribution = ContributionLocationHelper.getContributionLocation(MediaCollectionTestCase.class);

            scaProviderNode =
                NodeFactory.newInstance().createNode("org/apache/tuscany/sca/binding/atom/ReceiptProvider.composite",
                                                     new Contribution("provider", contribution));
            scaProviderNode.start();

            abdera = new Abdera();
            client = new AbderaClient(abdera);
            abderaParser = Abdera.getNewParser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() throws Exception {
        System.out.println(">>>MediaCollectionTestCase.destroy");
        if (scaProviderNode != null) {
            scaProviderNode.stop();
        }
    }

    @Test
    public void testPrelim() throws Exception {
        Assert.assertNotNull(scaProviderNode);
        Assert.assertNotNull(client);
    }

    @Test
    public void testMediaEntryPost() throws Exception {
        // Pseudo Code (see APP (http://tools.ietf.org/html/rfc5023#section-9.6)
        // Post request 
        // POST /edit/ HTTP/1.1
        // Host: media.example.org
        // Content-Type: image/png
        // Slug: The Beach
        // Content-Length: nnn
        // ...binary data...

        // Testing of entry creation
        String receiptName = "Auto Repair Bill";
        String fileName = "target/test-classes/ReceiptToms.gif";
        File input = new File(fileName);
        boolean exists = input.exists();
        Assert.assertTrue(exists);

        // Prepare HTTP post
        // PostMethod post = new PostMethod( colUri.toString() );
        HttpPost post = new HttpPost(providerURI);
        post.addHeader("Content-Type", "image/gif");
        post.addHeader("Title", "Title " + receiptName + "");
        post.addHeader("Slug", "Slug " + receiptName + "");

        post.setEntity(new FileEntity(input, "image/gif"));

        // Get HTTP client
        org.apache.http.client.HttpClient httpclient = new HttpClientFactory().createHttpClient();
        try {
            // Execute request
            HttpResponse response = httpclient.execute(post);
            int result = response.getStatusLine().getStatusCode();
            // Pseudo Code (see APP (http://tools.ietf.org/html/rfc5023#section-9.6)
            // Post response
            // Tuscany responds with proper media links. Note that the media is 
            // stored in a different location than the media information which is
            // stored in the Atom feed.
            // HTTP/1.1 201 Created
            // Display status code
            // System.out.println("Response status code: " + result + ", status text=" + post.getStatusText() );
            Assert.assertEquals(201, result);
            // Display response
            // System.out.println("Response body: ");
            // System.out.println(post.getResponseBodyAsString()); // Warning: BodyAsString recommends BodyAsStream

            // Location: http://example.org/media/edit/the_beach.atom (REQUIRED)
            // System.out.println( "Response Location=" + response.getFirstHeader( "Location" ).getValue() + "." );
            Header header = response.getFirstHeader("Location");
            Assert.assertNotNull(header);
            Assert.assertNotNull(header.getValue());
            // ContentLocation: http://example.org/media/edit/the_beach.jpg (REQUIRED)
            // System.out.println( "Response Content-Location=" + response.getFirstHeader( "Content-Location" ).getValue() );
            header = response.getFirstHeader("Content-Location");
            Assert.assertNotNull(header);
            Assert.assertNotNull(header.getValue());
            // Content-Type: application/atom+xml;type=entry;charset="utf-8"
            // System.out.println( "Response Content-Type=" + response.getFirstHeader( "Content-Type" ).getValue());
            header = response.getFirstHeader("Content-Type");
            Assert.assertNotNull(header);
            Assert.assertNotNull(header.getValue());
            // Content-Length: nnn (OPTIONAL)
            // System.out.println( "Response Content-Length=" + response.getFirstHeader( "Content-Length" ).getValue() );
            header = response.getFirstHeader("Content-Length");
            Assert.assertNotNull(header);
            Assert.assertNotNull(header.getValue());
            // <?xml version="1.0"?>
            // <entry xmlns="http://www.w3.org/2005/Atom">
            //   <title>The Beach</title> (REQUIRED) 
            //   <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id> (REQUIRED)
            //   <updated>2005-10-07T17:17:08Z</updated>
            //   <author><name>Daffy</name></author> 
            //   <summary type="text" /> (REQUIRED, OPTIONAL to populate
            //   <content type="image/png" src="http://media.example.org/the_beach.png"/>
            // <link rel="edit-media" href="http://media.example.org/edit/the_beach.png" />
            // <link rel="edit" href="http://example.org/media/edit/the_beach.atom" />
            // </entry>  		
            Document<Entry> document = abderaParser.parse(response.getEntity().getContent());
            Entry entry = document.getRoot();
            String title = entry.getTitle();
            // System.out.println( "mediaPost entry.title=" + title );
            Assert.assertNotNull(title);
            IRI id = entry.getId();
            // System.out.println( "mediaPost entry.id=" + id );
            Assert.assertNotNull(id);
            mediaId = id.toString();
            Assert.assertNotNull(mediaId); // Save for put/update request
            Date updated = entry.getUpdated();
            // System.out.println( "mediaPost entry.updated=" + updated);
            Assert.assertNotNull(updated);
            String summary = entry.getSummary();
            // System.out.println( "mediaPost entry.summary=" + summary);
            Assert.assertNotNull(summary);
            IRI contentSrc = entry.getContentSrc();
            // System.out.println( "mediaPost entry.content.src=" + contentSrc + ", type=" + entry.getContentType());
            Assert.assertNotNull(contentSrc);
            Link editLink = entry.getEditLink();
            // System.out.println( "mediaPost entry.editLink" + " rel=" + editLink.getRel() + ", href=" +  editLink.getHref() );
            Assert.assertNotNull(editLink);
            Assert.assertNotNull(editLink.getRel());
            Assert.assertNotNull(editLink.getHref());
            Link editMediaLink = entry.getEditMediaLink();
            // System.out.println( "mediaPost entry.editMediaLink" + " rel=" + editMediaLink.getRel() + ", href=" +  editMediaLink.getHref() );
            Assert.assertNotNull(editMediaLink);
            Assert.assertNotNull(editMediaLink.getRel());
            Assert.assertNotNull(editMediaLink.getHref());

        } finally {
            // Release current connection to the connection pool once you are done
            // post.releaseConnection();
        }
    }

    @Test
    public void testMediaEntryPutFound() throws Exception {
        // Pseudo Code (see APP (http://tools.ietf.org/html/rfc5023#section-9.6)
        // Testing of entry update
        String receiptName = "Value Autoglass Bill";
        String fileName = "target/test-classes/ReceiptValue.jpg";
        File input = new File(fileName);
        boolean exists = input.exists();
        Assert.assertTrue(exists);

        // Prepare HTTP put request
        // PUT /edit/the_beach.png HTTP/1.1
        // Host: media.example.org
        // Content-Type: image/png
        // Content-Length: nnn
        // ...binary data...
        HttpPut put = new HttpPut(providerURI + "/" + mediaId);
        put.addHeader("Content-Type", "image/jpg");
        put.addHeader("Title", "Title " + receiptName + "");
        put.addHeader("Slug", "Slug " + receiptName + "");
        put.setEntity(new FileEntity(input, "image/jpg"));

        // Get HTTP client
        HttpClient httpclient = new HttpClientFactory().createHttpClient();
        try {
            // Execute request
            HttpResponse response = httpclient.execute(put);
            response.getEntity().consumeContent();
            int result = response.getStatusLine().getStatusCode();
            // Pseudo Code (see APP (http://tools.ietf.org/html/rfc5023#section-9.6)
            // Display status code
            // System.out.println("Response status code: " + result + ", status text=" + put.getStatusText() );
            Assert.assertEquals(200, result);
            // Display response. Should be empty for put.
            // System.out.println("Response body: ");
            // System.out.println(put.getResponseBodyAsString()); // Warning: BodyAsString recommends BodyAsStream
        } finally {
            // Release current connection to the connection pool once you are done
            // put.releaseConnection();
        }
    }

    @Test
    public void testMediaEntryPutNotFound() throws Exception {
        // Pseudo Code (see APP (http://tools.ietf.org/html/rfc5023#section-9.6)
        // Testing of entry update
        String receiptName = "Value Autoglass Bill";
        String fileName = "target/test-classes/ReceiptValue.jpg";
        File input = new File(fileName);
        boolean exists = input.exists();
        Assert.assertTrue(exists);

        // Prepare HTTP put request
        // PUT /edit/the_beach.png HTTP/1.1
        // Host: media.example.org
        // Content-Type: image/png
        // Content-Length: nnn
        // ...binary data...
        HttpPut put = new HttpPut(providerURI + "/" + mediaId + "-bogus"); // Does not exist.
        put.addHeader("Content-Type", "image/jpg");
        put.addHeader("Title", "Title " + receiptName + "");
        put.addHeader("Slug", "Slug " + receiptName + "");
        put.setEntity(new FileEntity(input, "image/jpg"));

        // Get HTTP client
        HttpClient httpclient = new HttpClientFactory().createHttpClient();
        try {
            // Execute request
            HttpResponse response = httpclient.execute(put);
            int result = response.getStatusLine().getStatusCode();
            // Pseudo Code (see APP (http://tools.ietf.org/html/rfc5023#section-9.6)
            // Display status code
            // System.out.println("Response status code: " + result + ", status text=" + put.getStatusText() );
            Assert.assertEquals(404, result);
            // Display response. Should be empty for put.
            // System.out.println("Response body: ");
            // System.out.println(put.getResponseBodyAsString()); // Warning: BodyAsString recommends BodyAsStream
        } finally {
            // Release current connection to the connection pool once you are done
            // put.releaseConnection();
        }
    }
}
