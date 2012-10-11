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

package org.apache.tuscany.sca.databinding.json;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;

import org.apache.axiom.om.OMElement;
import org.apache.tuscany.sca.common.xml.stax.StAXHelper;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.impl.TransformationContextImpl;
import org.apache.tuscany.sca.databinding.json.axiom.JSON2OMElement;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.json.JSONObject;
import org.junit.Test;

public class JSONTransformerTestCase {
    private static final String IPO_XML =
        "<?xml version=\"1.0\"?>" + "<ipo:purchaseOrder"
            + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + "  xmlns:ipo=\"http://www.example.com/IPO\""
            + "  xsi:schemaLocation=\"http://www.example.com/IPO ipo.xsd\""
            + "  orderDate=\"1999-12-01\">"
            + "  <shipTo exportCode=\"1\" xsi:type=\"ipo:UKAddress\">"
            + "    <name>Helen Zoe</name>"
            + "    <street>47 Eden Street</street>"
            + "    <city>Cambridge</city>"
            + "    <postcode>CB1 1JR</postcode>"
            + "  </shipTo>"
            + "  <billTo xsi:type=\"ipo:USAddress\">"
            + "    <name>Robert Smith</name>"
            + "    <street>8 Oak Avenue</street>"
            + "    <city>Old Town</city>"
            + "    <state>PA</state>"
            + "    <zip>95819</zip>"
            + "  </billTo>"
            + "  <items>"
            + "    <item partNum=\"833-AA\">"
            + "      <productName>Lapis necklace</productName>"
            + "      <quantity>1</quantity>"
            + "      <USPrice>99.95</USPrice>"
            + "      <ipo:comment>Want this for the holidays</ipo:comment>"
            + "      <shipDate>1999-12-05</shipDate>"
            + "    </item>"
            + "  </items>"
            + "</ipo:purchaseOrder>";

    private static final String JSON_STR =
        "{\"xsl:root\":{\"@xmlns\":{\"xsl\":\"http://foo.com\"},\"data\":{\"$\":\"my json string\"}}}";

    @Test
    public void testXML2JSON() throws Exception {
        ExtensionPointRegistry extensionPointRegistry = new DefaultExtensionPointRegistry();
        StAXHelper staxHelper = StAXHelper.getInstance(extensionPointRegistry);

        XMLStreamReader reader = staxHelper.createXMLStreamReader(new StringReader(IPO_XML));
        XMLStreamReader2JSON t1 = new XMLStreamReader2JSON(extensionPointRegistry);
        JSONObject json = (JSONObject)t1.transform(reader, null);
        Assert.assertNotNull(json);

        // Cannot round-trip as we hit a bug in Jettison: http://jira.codehaus.org/browse/JETTISON-93
        /*
        JSON2XMLStreamReader t2 = new JSON2XMLStreamReader();
        XMLStreamReader reader2 = t2.transform(json, null);
        StringWriter sw = new StringWriter();
        XMLStreamWriter streamWriter = staxHelper.createXMLStreamWriter(sw);
        staxHelper.save(reader2, streamWriter);
        streamWriter.flush();
        System.out.println(sw.toString());
        */

    }

    @Test
    public void testJSON2XML() throws Exception {
        ExtensionPointRegistry extensionPointRegistry = new DefaultExtensionPointRegistry();
        StAXHelper helper = StAXHelper.getInstance(extensionPointRegistry);

        JSON2XMLStreamReader t2 = new JSON2XMLStreamReader();
        XMLStreamReader reader2 = t2.transform(new JSONObject(JSON_STR), null);
        StringWriter sw = new StringWriter();
        XMLStreamWriter streamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
        helper.save(reader2, streamWriter);
        Assert.assertTrue(sw.toString()
            .contains("<xsl:root xmlns:xsl=\"http://foo.com\"><data>my json string</data></xsl:root>"));
    }

    @Test
    public void testJSON2OMElement() throws Exception {
        JSON2OMElement t1 = new JSON2OMElement();
        TransformationContext context = new TransformationContextImpl();
        DataType dt = new DataTypeImpl(Object.class, new XMLType(new QName("http://foo.com", "root"), null));
        context.setTargetDataType(dt);
        OMElement element = t1.transform(new JSONObject(JSON_STR), context);
        StringWriter writer = new StringWriter();
        element.serialize(writer);
        // System.out.println(writer.toString());
    }

    @Test
    public void testString2JSON() throws Exception {
        String json = "{\"name\":\"John\",\"age\":25}";
        String2JSON t1 = new String2JSON();
        JSONObject jsonObject = (JSONObject)t1.transform(json, null);
        Assert.assertEquals(jsonObject.getString("name"), "John");
        Assert.assertEquals(jsonObject.getInt("age"), 25);
        JSON2String t2 = new JSON2String();
        String str = t2.transform(jsonObject, null);
        Assert.assertTrue(str.contains("\"name\":\"John\""));
        Assert.assertTrue(str.contains("\"age\":25"));
    }
}
