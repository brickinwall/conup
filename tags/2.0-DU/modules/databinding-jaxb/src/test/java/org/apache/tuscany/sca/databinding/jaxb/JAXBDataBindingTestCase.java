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
import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.junit.Before;
import org.junit.Test;

import com.example.ipo.jaxb.ObjectFactory;
import com.example.ipo.jaxb.PurchaseOrderType;
import com.example.ipo.jaxb.USAddress;
import com.example.ipo.jaxb.USState;

/**
 *
 * @version $Rev: 916888 $ $Date: 2010-02-27 00:44:05 +0000 (Sat, 27 Feb 2010) $
 */
public class JAXBDataBindingTestCase {
    private JAXBDataBinding binding;

    @Before
    public void setUp() throws Exception {
        binding = new JAXBDataBinding(new DefaultExtensionPointRegistry());
    }

    /**
     * Test method for
     * {@link org.apache.tuscany.sca.databinding.jaxb.JAXBDataBinding#introspect(java.lang.Class, Operation)}.
     */
    @Test
    public final void testIntrospect() {
        DataType dataType = new DataTypeImpl<Class>(JAXBElement.class, null);
        Operation op = null;
        boolean yes = binding.introspect(dataType, op);
        assertTrue(yes);
        assertTrue(dataType.getDataBinding().equals(binding.getName()));
        assertTrue(dataType.getPhysical() == JAXBElement.class && dataType.getLogical() == XMLType.UNKNOWN);
        dataType = new DataTypeImpl<Class>(MockJAXBElement.class, null);
        yes = binding.introspect(dataType, op);
        assertTrue(yes);
        assertEquals(MockJAXBElement.class, dataType.getPhysical());
        assertEquals(new QName("http://www.example.com/IPO", "PurchaseOrderType"), ((XMLType)dataType.getLogical())
            .getTypeName());
        dataType = new DataTypeImpl<Class>(USAddress.class, null);
        yes = binding.introspect(dataType, op);
        assertTrue(yes);
        assertEquals(USAddress.class, dataType.getPhysical());
        assertEquals(new QName("http://www.example.com/IPO", "USAddress"), ((XMLType)dataType.getLogical())
            .getTypeName());
        dataType = new DataTypeImpl<Class>(USState.class, null);
        yes = binding.introspect(dataType, op);
        assertTrue(yes);
        assertTrue(dataType.getDataBinding().equals(binding.getName()));
        assertEquals(USState.class, dataType.getPhysical());
        assertEquals(new QName("http://www.example.com/IPO", "USState"), ((XMLType)dataType.getLogical()).getTypeName());

    }

    private static class MockJAXBElement extends JAXBElement<PurchaseOrderType> {

        private static final long serialVersionUID = -2767569071002707973L;

        /**
         * @param elementName
         * @param type
         * @param value
         */
        public MockJAXBElement(QName elementName, Class<PurchaseOrderType> type, PurchaseOrderType value) {
            super(elementName, type, value);
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCopy() {
        ObjectFactory factory = new ObjectFactory();
        PurchaseOrderType poType = factory.createPurchaseOrderType();
        JAXBElement<PurchaseOrderType> po = factory.createPurchaseOrder(poType);
        JAXBElement<PurchaseOrderType> copy = (JAXBElement<PurchaseOrderType>)binding.copy(po, null, null, null, null);
        assertEquals(new QName("http://www.example.com/IPO", "purchaseOrder"), copy.getName());
    }

    @Test
    public void testCopyNonElement() {
        ObjectFactory factory = new ObjectFactory();
        PurchaseOrderType poType = factory.createPurchaseOrderType();
        poType.setComment("Comment");
        PurchaseOrderType copy = (PurchaseOrderType)binding.copy(poType, null, null, null, null);
        assertTrue(copy instanceof PurchaseOrderType);
        assertEquals("Comment", (copy).getComment());
    }

    @Test
    public void testCopyNonRoot() {
        ObjectFactory factory = new ObjectFactory();
        USAddress address = factory.createUSAddress();
        address.setCity("San Jose");
        USAddress copy = (USAddress)binding.copy(address, null, null, null, null);
        assertTrue(copy instanceof USAddress);
        assertEquals("San Jose", (copy).getCity());

    }
}
