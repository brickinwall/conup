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

import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.impl.OperationImpl;
import org.apache.tuscany.sca.interfacedef.util.ElementInfo;
import org.apache.tuscany.sca.interfacedef.util.WrapperInfo;
import org.apache.tuscany.sdo.api.SDOUtil;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XMLDocument;
import commonj.sdo.helper.XMLHelper;
import commonj.sdo.helper.XSDHelper;
import commonj.sdo.impl.HelperProvider;

/**
 * @version $Rev: 1213702 $ $Date: 2011-12-13 14:12:38 +0000 (Tue, 13 Dec 2011) $
 */
public class SDOWrapperHandlerTestCase extends TestCase {
    private HelperContext context;
    private SDOWrapperHandler handler;

    @Override
    public void setUp() throws Exception {
    	new SDODataBinding(new DefaultExtensionPointRegistry());
        context = SDOUtil.createHelperContext();
        handler = new SDOWrapperHandler();
    }

    public void testWrapperAnyType() throws Exception {
        XMLHelper xmlHelper = context.getXMLHelper();
        XMLDocument document = xmlHelper.load(getClass().getResourceAsStream("/wrapper.xml"));
        Operation op = new OperationImpl();
        List children = handler.getChildren(document, op, true);
        assertEquals(5, children.size());
    }

    public void testWrapper() throws Exception {
        XSDHelper xsdHelper = context.getXSDHelper();
        xsdHelper.define(getClass().getResourceAsStream("/wrapper.xsd"), null);
        XMLHelper xmlHelper = context.getXMLHelper();
        XMLDocument document = xmlHelper.load(getClass().getResourceAsStream("/wrapper.xml"));
        Operation op = new OperationImpl();
        List children = handler.getChildren(document, op, true);
        assertEquals(5, children.size());
    }
    
    public void testCreate() {
        HelperContext context = HelperProvider.getDefaultContext();
        XSDHelper xsdHelper = context.getXSDHelper();
        xsdHelper.define(getClass().getResourceAsStream("/wrapper.xsd"), null);
        ElementInfo element = new ElementInfo(new QName("http://www.example.com/wrapper", "op"), null);
        Operation op = new OperationImpl();
        WrapperInfo wrapperInfo = new WrapperInfo(SDODataBinding.NAME, element, null);
        op.setInputWrapper(wrapperInfo);
        DataObject wrapper = (DataObject) handler.create(op, true);
        assertNotNull(wrapper);
    }

}
