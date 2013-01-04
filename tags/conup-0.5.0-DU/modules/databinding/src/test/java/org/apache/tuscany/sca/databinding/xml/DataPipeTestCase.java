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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.tuscany.sca.common.xml.dom.DOMHelper;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.DataPipe;
import org.apache.tuscany.sca.databinding.DataPipeTransformer;
import org.apache.tuscany.sca.databinding.impl.PipedTransformer;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test case for DataPipe
 *
 * @version $Rev: 945980 $ $Date: 2010-05-19 01:53:52 +0100 (Wed, 19 May 2010) $
 */
public class DataPipeTestCase {

    @Test
    public final void testStreamPipe() throws IOException {
        byte[] bytes = new byte[] {1, 2, 3};
        DataPipeTransformer<OutputStream, InputStream> pipe = new StreamDataPipe();
        DataPipe<OutputStream, InputStream> dataPipe = pipe.newInstance();
        OutputStream os = dataPipe.getSink();
        os.write(bytes);
        byte[] newBytes = new byte[16];
        int count = dataPipe.getResult().read(newBytes);
        Assert.assertEquals(3, count);
        for (int i = 0; i < bytes.length; i++) {
            Assert.assertEquals(bytes[i], newBytes[i]);
        }
    }

    @Test
    public final void testWriter2ReaderPipe() throws IOException {
        String str = "ABC";
        Writer2ReaderDataPipe pipe = new Writer2ReaderDataPipe();
        Assert.assertSame(Writer.class, pipe.getSourceType());
        Assert.assertSame(Reader.class, pipe.getTargetType());
        DataPipe<Writer, Reader> dataPipe = pipe.newInstance();
        dataPipe.getSink().write(str);
        char[] buf = new char[16];
        int count = dataPipe.getResult().read(buf);
        Assert.assertEquals(3, count);
        for (int i = 0; i < str.length(); i++) {
            Assert.assertEquals(str.charAt(i), buf[i]);
        }
    }

    @Test
    public final void testPiped() throws Exception {
        ExtensionPointRegistry registry = new DefaultExtensionPointRegistry();
        Node2Writer node2Writer = new Node2Writer(registry);
        Writer2ReaderDataPipe pipe = new Writer2ReaderDataPipe();
        PipedTransformer<Node, Writer, Reader> transformer =
            new PipedTransformer<Node, Writer, Reader>(node2Writer, pipe);
        Document document = DOMHelper.getInstance(registry).newDocument();
        Element element = document.createElementNS("http://ns1", "root");
        document.appendChild(element);
        Reader reader = transformer.transform(document, null);
        Assert.assertEquals(transformer.getWeight(), node2Writer.getWeight() + pipe.getWeight());
        Assert.assertEquals(transformer.getSourceDataBinding(), node2Writer.getSourceDataBinding());
        Assert.assertEquals(transformer.getTargetDataBinding(), pipe.getTargetDataBinding());
        char[] buf = new char[120];
        int count = reader.read(buf);
        String xml = new String(buf, 0, count);
        Assert.assertTrue(xml.contains("<root xmlns=\"http://ns1\"/>"));
    }

}
