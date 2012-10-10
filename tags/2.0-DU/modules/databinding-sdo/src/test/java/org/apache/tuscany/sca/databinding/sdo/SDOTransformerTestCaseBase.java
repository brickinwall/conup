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

package org.apache.tuscany.sca.databinding.sdo;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.impl.TransformationContextImpl;
import org.apache.tuscany.sca.interfacedef.DataType;

import com.example.ipo.sdo.PurchaseOrderType;
import com.example.ipo.sdo.SdoFactory;
import com.example.ipo.sdo.USAddress;
import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.impl.HelperProvider;

/**
 * The base class for SDO-related test cases
 *
 * @version $Rev: 941719 $ $Date: 2010-05-06 14:30:35 +0100 (Thu, 06 May 2010) $
 */
public abstract class SDOTransformerTestCaseBase extends TestCase {
    protected static final QName ORDER_QNAME = new QName("http://www.example.com/IPO", "purchaseOrder");

    protected HelperContext helperContext;
    protected String binding = DataObject.class.getName();
    protected TransformationContext context;
    protected TransformationContext reversedContext; 
    protected DataObject dataObject;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        new SDODataBinding(new DefaultExtensionPointRegistry());
        helperContext = HelperProvider.getDefaultContext();
        SdoFactory.INSTANCE.register(helperContext);
        
        context = new TransformationContextImpl();
        context.setSourceDataType(getSourceDataType());
        context.setTargetDataType(getTargetDataType());

        reversedContext = new TransformationContextImpl();
        reversedContext.setSourceDataType(getTargetDataType());
        reversedContext.setTargetDataType(getSourceDataType());
        
        PurchaseOrderType po = SdoFactory.INSTANCE.createPurchaseOrderType();
        USAddress address = SdoFactory.INSTANCE.createUSAddress();
        address.setCity("San Jose");
        address.setStreet("123 ABC St");
        address.setState("CA");
        address.setStreet("95131");
        po.setBillTo(address);
        dataObject = (DataObject) po;
    }

    protected abstract DataType<?> getSourceDataType();

    protected abstract DataType<?> getTargetDataType();

}
