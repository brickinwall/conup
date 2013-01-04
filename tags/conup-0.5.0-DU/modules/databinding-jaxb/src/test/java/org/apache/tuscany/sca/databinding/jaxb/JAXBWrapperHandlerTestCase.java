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

package org.apache.tuscany.sca.databinding.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.impl.OperationImpl;
import org.apache.tuscany.sca.interfacedef.util.ElementInfo;
import org.apache.tuscany.sca.interfacedef.util.WrapperInfo;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.example.stock.StockQuoteOffer;

/**
 * Test case for JAXBExceptionHandler
 *
 * @version $Rev: 1213702 $ $Date: 2011-12-13 14:12:38 +0000 (Tue, 13 Dec 2011) $
 */
public class JAXBWrapperHandlerTestCase {
    private static final QName ELEMENT = new QName("http://www.example.com/stock", "stockQuoteOffer");
    private static final QName INPUT = new QName("", "input");
    private JAXBWrapperHandler handler;

    @Before
    public void setUp() throws Exception {
        this.handler = new JAXBWrapperHandler();
    }

    @Test
    public void testCreate() {
        ElementInfo element = new ElementInfo(ELEMENT, null);
        Operation op = new OperationImpl();
        WrapperInfo wrapperInfo = new WrapperInfo(JAXBDataBinding.NAME, element, null);
        wrapperInfo.setWrapperType(new DataTypeImpl<XMLType>(JAXBDataBinding.NAME, StockQuoteOffer.class,
                                                             XMLType.UNKNOWN));
        op.setInputWrapper(wrapperInfo);
        Object offer = handler.create(op, true);
        Assert.assertTrue(offer instanceof StockQuoteOffer);
    }

    @Test
    public void testSetChild() {
        StockQuoteOffer wrapper = new StockQuoteOffer();
        handler.setChild(wrapper, 0, new ElementInfo(INPUT, null), "IBM");
        Assert.assertEquals("IBM", wrapper.getInput());
    }

    @Test
    public void testGetChildren() {
        StockQuoteOffer wrapper = new StockQuoteOffer();
        wrapper.setInput("IBM");
        List<ElementInfo> elements = new ArrayList<ElementInfo>();
        elements.add(new ElementInfo(INPUT, null));
        WrapperInfo wrapperInfo = new WrapperInfo(JAXBDataBinding.NAME, null, elements);
        Operation op = new OperationImpl();
        op.setInputWrapper(wrapperInfo);
        List children = handler.getChildren(wrapper, op, true);
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals("IBM", children.get(0));
    }
}
