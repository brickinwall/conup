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
package org.apache.tuscany.sca.databinding.xml;

import static org.junit.Assert.assertTrue;

import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.common.xml.stax.reader.DOMXmlNodeImpl;
import org.apache.tuscany.sca.common.xml.stax.reader.XmlTreeStreamReaderImpl;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

/**
 *
 * @version $Rev: 801902 $ $Date: 2009-08-07 08:37:15 +0100 (Fri, 07 Aug 2009) $
 */
public class DOM2StAXTestCase {
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

    private static final String CRAZY_XML =
        "<p:e1 xmlns=\"http://ns0\" xmlns:p=\"http://p1\">" + "<p:e2 xmlns:p=\"http://p2\"/><e3/><e4 xmlns=\"\">E4</e4></p:e1>";

    private static ExtensionPointRegistry registry;
    
    @BeforeClass
    public static void init() {
        registry = new DefaultExtensionPointRegistry();
    }
    
    @Test
    public void testTransformation() throws Exception {
        String2Node t1 = new String2Node(registry);
        Node node = t1.transform(IPO_XML, null);
        Node2XMLStreamReader t2 = new Node2XMLStreamReader();
        XMLStreamReader reader = t2.transform(node, null);
        XMLStreamReader2String t3 = new XMLStreamReader2String(registry);
        String xml = t3.transform(reader, null);
        XMLAssert.assertXMLEqual(IPO_XML, xml);
        // assertTrue(xml != null && xml.indexOf("<shipDate>1999-12-05</shipDate>") != -1);
    }

    @Test
    public void testTransformation2() throws Exception {
        String2Node t1 = new String2Node(registry);
        Node node = t1.transform(CRAZY_XML, null);
        Node2XMLStreamReader t2 = new Node2XMLStreamReader();
        XMLStreamReader reader = t2.transform(node, null);
        XMLStreamReader2String t3 = new XMLStreamReader2String(registry);
        String xml = t3.transform(reader, null);
        // System.out.println(xml);
        XMLAssert.assertXMLEqual(CRAZY_XML, xml);
        assertTrue(xml.contains("<p:e2 xmlns:p=\"http://p2\""));
    }

    @Test
    public void testTransformation3() throws Exception {
        String2Node t1 = new String2Node(registry);
        Node node = t1.transform(IPO_XML, null);
        DOMXmlNodeImpl element = new DOMXmlNodeImpl(node);
        XmlTreeStreamReaderImpl reader = new XmlTreeStreamReaderImpl(element);
        XMLStreamReader2String t3 = new XMLStreamReader2String(registry);
        String xml = t3.transform(reader, null);
        XMLAssert.assertXMLEqual(IPO_XML, xml);
        // assertTrue(xml != null && xml.indexOf("<shipDate>1999-12-05</shipDate>") != -1);
    }

}
