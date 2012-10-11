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
package org.apache.tuscany.sca.interfacedef.java.impl;

import static org.apache.tuscany.sca.interfacedef.java.impl.JavaInterfaceUtil.findOperation;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.impl.OperationImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Rev: 722922 $ $Date: 2008-12-03 15:07:30 +0000 (Wed, 03 Dec 2008) $
 */
public class JavaInterfaceUtilTestCase {
    private List<Operation> operations;

    @Test
    public void testNoParamsFindOperation() throws Exception {
        Method method = Foo.class.getMethod("foo");
        Operation ret = findOperation(method, operations);
        assertEquals("foo", ret.getName());
        assertEquals(0, method.getParameterTypes().length);
    }

    @Test
    public void testParamsFindOperation() throws Exception {
        Method method = Foo.class.getMethod("foo", String.class);
        Operation ret = findOperation(method, operations);
        assertEquals("foo", ret.getName());
        assertEquals(String.class, method.getParameterTypes()[0]);
    }

    @Test
    public void testPrimitiveParamFindOperation() throws NoSuchMethodException {
        Method method = Foo.class.getMethod("foo", Integer.TYPE);
        Operation operation = findOperation(method, operations);
        assertEquals(Integer.TYPE, operation.getInputType().getLogical().get(0).getPhysical());
    }

    @Before
    public void setUp() throws Exception {
        Operation operation = newOperation("foo");
        List<DataType> types = new ArrayList<DataType>();
        DataType<List<DataType>> inputType = new DataTypeImpl<List<DataType>>(Object[].class, types);
        operation.setInputType(inputType);

        operations = new ArrayList<Operation>();
        operations.add(operation);

        types = new ArrayList<DataType>();
        inputType = new DataTypeImpl<List<DataType>>(Object[].class, types);
        DataType type = new DataTypeImpl<Class>(String.class, Object.class);
        types.add(type);
        operation = newOperation("foo");
        operation.setInputType(inputType);
        operations.add(operation);

        types = new ArrayList<DataType>();
        type = new DataTypeImpl<Class>(String.class, Object.class);
        DataType type2 = new DataTypeImpl<Class>(String.class, Object.class);
        types.add(type);
        types.add(type2);
        inputType = new DataTypeImpl<List<DataType>>(Object[].class, types);
        operation = newOperation("foo");
        operation.setInputType(inputType);
        operations.add(operation);

        types = new ArrayList<DataType>();
        type = new DataTypeImpl<Class>(Integer.class, Object.class);
        types.add(type);
        inputType = new DataTypeImpl<List<DataType>>(Object[].class, types);
        operation = newOperation("foo");
        operation.setInputType(inputType);
        operations.add(operation);

        types = new ArrayList<DataType>();
        type = new DataTypeImpl<Class>(Integer.TYPE, Object.class);
        types.add(type);
        inputType = new DataTypeImpl<List<DataType>>(Object[].class, types);
        operation = newOperation("foo");
        operation.setInputType(inputType);
        operations.add(operation);

    }

    private interface Foo {
        void foo();

        void foo(String foo);

        void foo(int b);
    }

    private static Operation newOperation(String name) {
        Operation operation = new OperationImpl();
        operation.setName(name);
        return operation;
    }
}
