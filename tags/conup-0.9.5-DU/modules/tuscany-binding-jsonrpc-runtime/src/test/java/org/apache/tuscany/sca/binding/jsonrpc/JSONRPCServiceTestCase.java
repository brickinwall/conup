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
package org.apache.tuscany.sca.binding.jsonrpc;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;

import junit.framework.Assert;

import org.apache.axiom.om.util.Base64;
import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * @version $Rev: 1180782 $ $Date: 2011-10-10 13:01:22 +0800 (周一, 10 十月 2011) $
 */
public class JSONRPCServiceTestCase {

    private static String SERVICE_URL;
    private static String SERVICE20_URL;

    private static Node node;

    @BeforeClass
    public static void setUp() throws Exception {
        try {
            String contribution = ContributionLocationHelper.getContributionLocation(JSONRPCServiceTestCase.class);
            node = NodeFactory.newInstance().createNode("JSONRPCBinding.composite", new Contribution("test", contribution));
            node.start();
            SERVICE_URL = node.getEndpointAddress("EchoComponent/Echo/Echo");
            SERVICE20_URL = node.getEndpointAddress("EchoComponent/Echo/jsonrpc20");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        node.stop();
    }

    @Test
    public void testEchoWithJSONRPCBinding() throws Exception {
        JSONObject jsonRequest = new JSONObject("{ \"method\": \"echo\", \"params\": [\"Hello JSON-RPC\"], \"id\": 1}");

        WebConversation wc = new WebConversation();
        WebRequest request   = new PostMethodWebRequest( SERVICE_URL, new ByteArrayInputStream(jsonRequest.toString().getBytes("UTF-8")),"application/json");
        WebResponse response = wc.getResource(request);

        Assert.assertEquals(200, response.getResponseCode());

        JSONObject jsonResp = new JSONObject(response.getText());
        Assert.assertEquals("echo: Hello JSON-RPC", jsonResp.getString("result"));
    }
    
    @Test
    public void testEchoWithJSONRPC20Binding() throws Exception {
        JSONObject jsonRequest = new JSONObject("{ \"jsonrpc\": \"2.0\", \"method\": \"echo\", \"params\": [\"Hello JSON-RPC\"], \"id\": 1}");

        WebConversation wc = new WebConversation();
        WebRequest request   = new PostMethodWebRequest( SERVICE20_URL, new ByteArrayInputStream(jsonRequest.toString().getBytes("UTF-8")),"application/json");
        WebResponse response = wc.getResource(request);

        Assert.assertEquals(200, response.getResponseCode());

        JSONObject jsonResp = new JSONObject(response.getText());
        Assert.assertEquals("echo: Hello JSON-RPC", jsonResp.getString("result"));
    }
    
    
    @Test
    public void testEchoWithJSONRPC20BindingBatch() throws Exception {
        JSONObject jsonRequest1 = new JSONObject("{ \"jsonrpc\": \"2.0\", \"method\": \"echo\", \"params\": [\"Hello JSON-RPC\"], \"id\": 1}");
        JSONObject jsonRequest2 = new JSONObject("{ \"jsonrpc\": \"2.0\", \"method\": \"echo\", \"params\": [\"Hello JSON-RPC 2.0\"], \"id\": 2}");
        JSONArray batchReq = new JSONArray();
        batchReq.put(jsonRequest1);
        batchReq.put(jsonRequest2);
        
        WebConversation wc = new WebConversation();
        WebRequest request   = new PostMethodWebRequest( SERVICE20_URL, new ByteArrayInputStream(batchReq.toString().getBytes("UTF-8")),"application/json");
        WebResponse response = wc.getResource(request);

        Assert.assertEquals(200, response.getResponseCode());

        JSONArray jsonResp = new JSONArray(response.getText());
        Assert.assertEquals("echo: Hello JSON-RPC", ((JSONObject) jsonResp.get(0)).getString("result"));
        Assert.assertEquals("echo: Hello JSON-RPC 2.0", ((JSONObject) jsonResp.get(1)).getString("result"));
    }

    @Test
    public void testJSONRPCBindingGET() throws Exception {
        String params = Base64.encode("[\"Hello JSON-RPC\"]".getBytes());
        String queryString = "?method=echo&params=" + URLEncoder.encode(params,"UTF-8") + "&id=1";

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(SERVICE_URL + queryString);
        WebResponse response = wc.getResource(request);

        Assert.assertEquals(200, response.getResponseCode());

        JSONObject jsonResp = new JSONObject(response.getText());
        Assert.assertEquals("echo: Hello JSON-RPC", jsonResp.getString("result"));
    }
    
    @Test
    public void testEchoVoidWithJSONRPCBinding() throws Exception {
        JSONObject jsonRequest = new JSONObject("{ \"method\": \"echoVoid\", \"params\": [], \"id\": 1}");

        WebConversation wc = new WebConversation();
        WebRequest request   = new PostMethodWebRequest( SERVICE_URL, new ByteArrayInputStream(jsonRequest.toString().getBytes("UTF-8")),"application/json");
        WebResponse response = wc.getResource(request);

        Assert.assertEquals(200, response.getResponseCode());
        
        JSONObject jsonResp = new JSONObject(response.getText());
        Assert.assertTrue(jsonResp.isNull("result"));
    }
}