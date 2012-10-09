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
package org.apache.tuscany.sca.binding.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * HTTP binding unit tests.
 * 
 * @version $Rev: 937850 $ $Date: 2010-04-25 18:52:22 +0100 (Sun, 25 Apr 2010) $
 */
public class RESTBindingCacheTestCase {
	// RFC 822 date time
	protected static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss Z");

	// Request with no predicates in header.
	private static final String REQUEST1 = "{0} /httpbinding/{1} HTTP/1.0\n"
			+ "Host: localhost\n" + "Content-Type: text/xml\n"
			+ "Connection: close\n" + "Content-Length: {2}" + "\n\n{3}";

	// Request with predicates in header
	private static final String REQUEST2 = "{0} /httpbinding/{1} HTTP/1.0\n"
			+ "Host: localhost\n" + "Content-Type: text/xml\n" + "{2}: {3}\n" // predicate (If-Match, If-None-Match, If-Modified-Since, If-NotModified-Since): value (date or ETag)
			+ "Connection: close\n" + "Content-Length: {4}" + "\n\n{5}";

	private static final int HTTP_PORT = 8085;

	private static Node node;

	@BeforeClass
	public static void setUp() throws Exception {
		try {
			String contribution = ContributionLocationHelper.getContributionLocation(RESTBindingCacheTestCase.class);
			node = NodeFactory.newInstance().createNode("testCache.composite", new Contribution("test", contribution));
			node.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		node.stop();
	}

	/**
	 * Test invoking a POJO get method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testGet() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST1, "GET", index, content
				.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		Assert.assertTrue(document.indexOf("<body><p>item=" + index) != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalGetIfModifiedNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "GET", index,
				"If-Modified-Since", dateFormat.format(new Date(0)), 
				content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		Assert.assertTrue(document.indexOf("<body><p>item=" + index) != -1);
		// Should return code 304 Not Modified.
		// assertTrue(document.indexOf("HTTP/1.1 304") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalGetIfModifiedPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "GET", index,
				"If-Modified-Since", dateFormat.format(new Date(0)), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("<body><p>item=" + index) != -1);
		// Should return code 304 Not Modified.
		Assert.assertTrue(document.indexOf("HTTP/1.1 304") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalGetIfUnmodifiedNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "GET", index,
				"If-Unmodified-Since", dateFormat.format(new Date()), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		Assert.assertTrue(document.indexOf("<body><p>item=" + index) != -1);
		// Should return code 304 Not Modified.
		// assertTrue(document.indexOf("HTTP/1.1 304") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalGetIfUnmodifiedPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "GET", index,
				"If-Unmodified-Since", dateFormat.format(new Date(0)), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("<body><p>item=" + index) != -1);
		// Should return code 412 PreconditionFailed.
		Assert.assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalGetIfMatchNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "GET", index,
				"If-Match", "eTagXXX", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("<body><p>item=" + index) != -1);
		// Should return code 412 precondition failed.
		Assert.assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalGetIfMatchPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "GET", index,
				"If-Match", "eTagXXX", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		Assert.assertTrue(document.indexOf("<body><p>item=" + index) != -1);
		// Should return code 412 PreconditionFailed.
		// assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalGetIfNoneMatchNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "GET", index,
				"If-None-Match", "eTagXXX", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		Assert.assertTrue(document.indexOf("<body><p>item=" + index) != -1);
		// Should return code 412 precondition failed.
		// assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalGetIfNoneMatchPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "GET", index,
				"If-None-Match", "eTagXXX", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("<body><p>item=" + index) != -1);
		// Should return code 412 PreconditionFailed.
		Assert.assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a POJO get method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testDelete() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST1, "DELETE", index,
				content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		Assert.assertTrue(document.indexOf("deleted item=" + index) != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalDeleteIfModifiedNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "DELETE", index,
				"If-Modified-Since", dateFormat.format(new Date(0)), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		Assert.assertTrue(document.indexOf("deleted item=" + index) != -1);
		// Should return code 304 Not Modified.
		// assertTrue(document.indexOf("HTTP/1.1 304") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalDeleteIfModifiedPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "DELETE", index,
				"If-Modified-Since", dateFormat.format(new Date(0)), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("deleted item=" + index) != -1);
		// Should return code 304 Not Modified.
		Assert.assertTrue(document.indexOf("HTTP/1.1 304") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalDeleteIfUnmodifiedNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "DELETE", index,
				"If-Unmodified-Since", dateFormat.format(new Date()), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		Assert.assertTrue(document.indexOf("deleted item=" + index) != -1);
		// Should return code 304 Not Modified.
		// assertTrue(document.indexOf("HTTP/1.1 304") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalDeleteIfUnmodifiedPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "DELETE", index,
				"If-Unmodified-Since", dateFormat.format(new Date(0)), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("deleted item=" + index) != -1);
		// Should return code 412 PreconditionFailed.
		Assert.assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalDeleteIfMatchNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "DELETE", index,
				"If-Match", "eTagXXX", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("deleted item=" + index) != -1);
		// Should return code 412 precondition failed.
		Assert.assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalDeleteIfMatchPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "DELETE", index,
				"If-Match", "eTagXXX", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		Assert.assertTrue(document.indexOf("deleted item=" + index) != -1);
		// Should return code 412 PreconditionFailed.
		// assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalDeleteIfNoneMatchNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "DELETE", index,
				"If-None-Match", "eTagXXX", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		Assert.assertTrue(document.indexOf("deleted item=" + index) != -1);
		// Should return code 412 precondition failed.
		// assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalDeleteIfNoneMatchPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "DELETE", index,
				"If-None-Match", "eTagXXX", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("deleted item=" + index) != -1);
		// Should return code 412 PreconditionFailed.
		Assert.assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a POJO get method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testPost() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST1, "POST", index, content
				.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		Assert.assertTrue(document.indexOf("HTTP/1.1 200 OK") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPostIfModifiedNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "POST", index,
				"If-Modified-Since", dateFormat.format(new Date()), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return code 200 OK
		// assertTrue(document.indexOf("posted item=" + index) != -1);
		Assert.assertTrue(document.indexOf("HTTP/1.1 200 OK") != -1);
		// Should return code 304 Not Modified.
		// assertTrue(document.indexOf("HTTP/1.1 304") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPostIfModifiedPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "POST", index,
				"If-Modified-Since", dateFormat.format(new Date(0)), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("posted item=" + index) != -1);
		// Should return code 304 Not Modified.
		Assert.assertTrue(document.indexOf("HTTP/1.1 304") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPostIfUnmodifiedNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "POST", index,
				"If-Unmodified-Since", dateFormat.format(new Date()), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return code 200 OK
		Assert.assertTrue(document.indexOf("HTTP/1.1 200 OK") != -1);
		// Should return code 304 Not Modified.
		// assertTrue(document.indexOf("HTTP/1.1 304") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPostIfUnmodifiedPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "POST", index,
				"If-Unmodified-Since", dateFormat.format(new Date(0)), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("posted item=" + index) != -1);
		// Should return code 412 PreconditionFailed.
		Assert.assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPostIfMatchNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "POST", index,
				"If-Match", "eTagMatch", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return code 200 OK.
		Assert.assertTrue(document.indexOf("HTTP/1.1 200 OK") != -1);
		// Should return code 412 precondition failed.
		// assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPostIfMatchPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat
				.format(REQUEST2, "POST", index, "If-Match", "eTagNoneMatch",
						content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("posted item=" + index) != -1);
		// Should return code 412 PreconditionFailed.
		Assert.assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPostIfNoneMatchNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "POST", index,
				"If-None-Match", "eTagNoneMatch", content.getBytes().length,
				content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return code 200 OK
		Assert.assertTrue(document.indexOf("HTTP/1.1 200 OK") != -1);
		// Should return code 412 precondition failed.
		// assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPostIfNoneMatchPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "POST", index,
				"If-None-Match", "eTagMatch", content.getBytes().length,
				content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("posted item=" + index) != -1);
		// Should return code 412 PreconditionFailed.
		Assert.assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a POJO get method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testPut() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST1, "PUT", index, content
				.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		Assert.assertTrue(document.indexOf("updated item=" + index) != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPutIfModifiedNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "PUT", index,
				"If-Modified-Since", dateFormat.format(new Date(0)), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		Assert.assertTrue(document.indexOf("updated item=" + index) != -1);
		// Should return code 304 Not Modified.
		// assertTrue(document.indexOf("HTTP/1.1 304") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPutIfModifiedPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "PUT", index,
				"If-Modified-Since", dateFormat.format(new Date(0)), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("updated item=" + index) != -1);
		// Should return code 304 Not Modified.
		Assert.assertTrue(document.indexOf("HTTP/1.1 304") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPutIfUnmodifiedNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "PUT", index,
				"If-Unmodified-Since", dateFormat.format(new Date()), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		Assert.assertTrue(document.indexOf("updated item=" + index) != -1);
		// Should return code 304 Not Modified.
		// assertTrue(document.indexOf("HTTP/1.1 304") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPutIfUnmodifiedPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "PUT", index,
				"If-Unmodified-Since", dateFormat.format(new Date(0)), content
						.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("updated item=" + index) != -1);
		// Should return code 412 PreconditionFailed.
		Assert.assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPutIfMatchNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "PUT", index,
				"If-Match", "eTagXXX", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("updated item=" + index) != -1);
		// Should return code 412 precondition failed.
		Assert.assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPutIfMatchPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "PUT", index,
				"If-Match", "eTagXXX", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		Assert.assertTrue(document.indexOf("updated item=" + index) != -1);
		// Should return code 412 PreconditionFailed.
		// assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPutIfNoneMatchNegative() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 1;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "PUT", index,
				"If-None-Match", "eTagXXX", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		Assert.assertTrue(document.indexOf("updated item=" + index) != -1);
		// Should return code 412 precondition failed.
		// assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Test invoking a conditional method implementation using the HTTP binding. 
	 * @throws Exception
	 */
	@Test
	public void testConditionalPutIfNoneMatchPositive() throws Exception {
		Socket client = new Socket("127.0.0.1", HTTP_PORT);
		OutputStream os = client.getOutputStream();
		int index = 0;
		String content = "";
		String request = MessageFormat.format(REQUEST2, "PUT", index,
				"If-None-Match", "eTagXXX", content.getBytes().length, content);
		os.write(request.getBytes());
		os.flush();

		String document = read(client);
		// Should return item
		// assertTrue(document.indexOf("updated item=" + index) != -1);
		// Should return code 412 PreconditionFailed.
		Assert.assertTrue(document.indexOf("HTTP/1.1 412") != -1);
	}

	/**
	 * Read response stream from the given socket.
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	private static String read(Socket socket) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
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
}
