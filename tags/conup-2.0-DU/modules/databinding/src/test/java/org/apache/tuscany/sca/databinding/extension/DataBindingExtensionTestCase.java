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

package org.apache.tuscany.sca.databinding.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.BaseDataBinding;
import org.apache.tuscany.sca.databinding.DataBindingExtensionPoint;
import org.apache.tuscany.sca.databinding.DefaultDataBindingExtensionPoint;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @version $Rev: 933855 $ $Date: 2010-04-14 05:16:49 +0100 (Wed, 14 Apr 2010) $
 */
public class DataBindingExtensionTestCase {

    @Test
    @SuppressWarnings("unchecked")
    public void testExtension() {
        DataBinding1 binding1 = new DataBinding1(Node.class);
        assertEquals(Node.class.getName(), binding1.getName());
        DataType dt1 = new DataTypeImpl<Class>(Element.class, null);
        assertTrue(binding1.introspect(dt1, null));
        DataType dt2 = new DataTypeImpl<Class>(String.class, null);
        assertFalse(binding1.introspect(dt2, null));
        assertNull(binding1.getWrapperHandler());
        
        DataBindingExtensionPoint registry = new DefaultDataBindingExtensionPoint(new DefaultExtensionPointRegistry());
        registry.addDataBinding(binding1);
        
        assertNotNull(registry.getDataBinding(Node.class.getName()));

        DataBinding1 binding2 = new DataBinding1("dom", Node.class);
        assertEquals("dom", binding2.getName());
    }

    private static class DataBinding1 extends BaseDataBinding {

        /**
         * @param baseType
         */
        public DataBinding1(Class<?> baseType) {
            super(baseType);
        }

        /**
         * @param name
         * @param baseType
         */
        public DataBinding1(String name, Class<?> baseType) {
            super(name, baseType);
        }

    }

}
