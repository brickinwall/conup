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
package org.apache.tuscany.sca.interfacedef.impl;

import static org.junit.Assert.fail;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.interfacedef.Compatibility;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.IncompatibleInterfaceContractException;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO some tests commented out due to DataType.equals() needing to be strict
 * 
 * @version $Rev: 1035091 $ $Date: 2010-11-14 22:34:32 +0000 (Sun, 14 Nov 2010) $
 */
public class ContractCompatibilityTestCase {

    private InterfaceContractMapper mapper;
    
    @Before
    public void setUp() throws Exception {
        ExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        UtilityExtensionPoint utilities = extensionPoints.getExtensionPoint(UtilityExtensionPoint.class);
        mapper = utilities.getUtility(InterfaceContractMapper.class);
    }

    @Test
    public void testNoOperation() throws Exception {
        InterfaceContract source = new MockContract("FooContract");
        InterfaceContract target = new MockContract("FooContract");
        mapper.checkCompatibility(source, target, Compatibility.SUBSET, false, false);
    }

    @Test
    public void testBasic() throws Exception {
        InterfaceContract source = new MockContract("FooContract");
        Operation opSource1 = newOperation("op1");
        Map<String, Operation> sourceOperations = new HashMap<String, Operation>();
        sourceOperations.put("op1", opSource1);
        source.getInterface().getOperations().addAll(sourceOperations.values());
        InterfaceContract target = new MockContract("FooContract");
        Operation opSource2 = newOperation("op1");
        Map<String, Operation> targetOperations = new HashMap<String, Operation>();
        targetOperations.put("op1", opSource2);
        target.getInterface().getOperations().addAll(targetOperations.values());
        mapper.checkCompatibility(source, target, Compatibility.SUBSET, false, false);
    }

    @Test
    public void testBasicIncompatibleOperationNames() throws Exception {
        InterfaceContract source = new MockContract("FooContract");
        Operation opSource1 = newOperation("op1");
        Map<String, Operation> sourceOperations = new HashMap<String, Operation>();
        sourceOperations.put("op1", opSource1);
        source.getInterface().getOperations().addAll(sourceOperations.values());
        InterfaceContract target = new MockContract("FooContract");
        Operation opSource2 = newOperation("op2");
        Map<String, Operation> targetOperations = new HashMap<String, Operation>();
        targetOperations.put("op2", opSource2);
        target.getInterface().getOperations().addAll(targetOperations.values());
        try {
            mapper.checkCompatibility(source, target, Compatibility.SUBSET, false, false);
            fail();
        } catch (IncompatibleInterfaceContractException e) {
            // expected
        }
    }

    @Test
    public void testInputTypes() throws Exception {
        InterfaceContract source = new MockContract("FooContract");
        List<DataType> sourceInputTypes = new ArrayList<DataType>();
        sourceInputTypes.add(new DataTypeImpl<Type>(Object.class, Object.class));
        DataType<List<DataType>> inputType = new DataTypeImpl<List<DataType>>(String.class, sourceInputTypes);
        Operation opSource1 = newOperation("op1");
        opSource1.setInputType(inputType);
        Map<String, Operation> sourceOperations = new HashMap<String, Operation>();
        sourceOperations.put("op1", opSource1);
        source.getInterface().getOperations().addAll(sourceOperations.values());

        InterfaceContract target = new MockContract("FooContract");
        List<DataType> targetInputTypes = new ArrayList<DataType>();
        targetInputTypes.add(new DataTypeImpl<Type>(Object.class, Object.class));
        DataType<List<DataType>> targetInputType = new DataTypeImpl<List<DataType>>(String.class, targetInputTypes);

        Operation opTarget = newOperation("op1");
        opTarget.setInputType(targetInputType);
        Map<String, Operation> targetOperations = new HashMap<String, Operation>();
        targetOperations.put("op1", opTarget);
        target.getInterface().getOperations().addAll(targetOperations.values());
        mapper.checkCompatibility(source, target, Compatibility.SUBSET, false, false);
    }

    @Test
    public void testIncompatibleInputTypes() throws Exception {
        InterfaceContract source = new MockContract("FooContract");
        List<DataType> sourceInputTypes = new ArrayList<DataType>();
        sourceInputTypes.add(new DataTypeImpl<Type>(Integer.class, Integer.class));
        DataType<List<DataType>> inputType = new DataTypeImpl<List<DataType>>(String.class, sourceInputTypes);
        Operation opSource1 = newOperation("op1");
        opSource1.setInputType(inputType);
        Map<String, Operation> sourceOperations = new HashMap<String, Operation>();
        sourceOperations.put("op1", opSource1);
        source.getInterface().getOperations().addAll(sourceOperations.values());

        InterfaceContract target = new MockContract("FooContract");
        List<DataType> targetInputTypes = new ArrayList<DataType>();
        targetInputTypes.add(new DataTypeImpl<Type>(String.class, String.class));
        DataType<List<DataType>> targetInputType = new DataTypeImpl<List<DataType>>(String.class, targetInputTypes);

        Operation opTarget = newOperation("op1");
        opTarget.setInputType(targetInputType);
        Map<String, Operation> targetOperations = new HashMap<String, Operation>();
        targetOperations.put("op1", opTarget);
        target.getInterface().getOperations().addAll(targetOperations.values());
        try {
            mapper.checkCompatibility(source, target, Compatibility.SUBSET, false, false);
            fail();
        } catch (IncompatibleInterfaceContractException e) {
            // expected
        }
    }

    /**
     * Verifies source input types can be super types of the target
     */
    @Test
    public void testSourceSuperTypeInputCompatibility() throws Exception {
        // InterfaceContract source = new MockContract("FooContract");
        // List<DataType> sourceInputTypes = new ArrayList<DataType>();
        // sourceInputTypes.add(new DataTypeImpl<Type>(Object.class,
        // Object.class));
        // DataType<List<DataType>> inputType = new
        // DataTypeImpl<List<DataType>>(String.class, sourceInputTypes);
        // Operation opSource1 = newOperationImpl("op1", inputType, null, null,
        // false, null);
        // Map<String, Operation> sourceOperations = new HashMap<String,
        // Operation>();
        // sourceOperations.put("op1", opSource1);
        // source.getInterface().getOperations().addAll(sourceOperations.values());
        //
        // InterfaceContract target = new MockContract("FooContract");
        // List<DataType> targetInputTypes = new ArrayList<DataType>();
        // targetInputTypes.add(new DataTypeImpl<Type>(String.class,
        // String.class));
        // DataType<List<DataType>> targetInputType =
        // new DataTypeImpl<List<DataType>>(String.class, targetInputTypes);
        //
        // Operation opTarget = newOperationImpl("op1", targetInputType, null,
        // null, false, null);
        // Map<String, Operation> targetOperations = new HashMap<String,
        // Operation>();
        // targetOperations.put("op1", opTarget);
        // target.getInterface().getOperations().addAll(targetOperations.values());
        // wireService.checkCompatibility(source, target, false);
    }

    @Test
    public void testOutputTypes() throws Exception {
        InterfaceContract source = new MockContract("FooContract");
        DataType sourceStringType =  new DataTypeImpl<Type>(String.class, String.class);
        ArrayList sourceTypes = new ArrayList();
        sourceTypes.add(sourceStringType);
        DataType sourceOutputType = new DataTypeImpl(Object[].class, sourceTypes);
        Operation opSource1 = newOperation("op1");
        opSource1.setOutputType(sourceOutputType);
        Map<String, Operation> sourceOperations = new HashMap<String, Operation>();
        sourceOperations.put("op1", opSource1);
        source.getInterface().getOperations().addAll(sourceOperations.values());

        InterfaceContract target = new MockContract("FooContract");
        DataType stringType = new DataTypeImpl<Type>(String.class, String.class);
        ArrayList types = new ArrayList();
        types.add(stringType);
        DataType targetOutputType = new DataTypeImpl(Object[].class, types);
        Operation opTarget = newOperation("op1");
        opTarget.setOutputType(targetOutputType);
        Map<String, Operation> targetOperations = new HashMap<String, Operation>();
        targetOperations.put("op1", opTarget);
        target.getInterface().getOperations().addAll(targetOperations.values());
        mapper.checkCompatibility(source, target, Compatibility.SUBSET, false, false);
    }

    /**
     * Verifies a return type that is a supertype of of the target is compatible
     */
    @Test
    public void testSupertypeOutputTypes() throws Exception {
        // InterfaceContract source = new MockContract("FooContract");
        // DataType sourceOutputType = new DataTypeImpl<Type>(Object.class,
        // Object.class);
        // Operation opSource1 = newOperationImpl("op1", null,
        // sourceOutputType, null, false, null);
        // Map<String, Operation> sourceOperations = new HashMap<String,
        // Operation>();
        // sourceOperations.put("op1", opSource1);
        // source.getInterface().getOperations().addAll(sourceOperations.values());
        //
        // InterfaceContract target = new MockContract("FooContract");
        // DataType targetOutputType = new DataTypeImpl<Type>(String.class,
        // String.class);
        // Operation opTarget = newOperationImpl("op1", null, targetOutputType,
        // null, false, null);
        // Map<String, Operation> targetOperations = new HashMap<String,
        // Operation>();
        // targetOperations.put("op1", opTarget);
        // target.getInterface().getOperations().addAll(targetOperations.values());
        // wireService.checkCompatibility(source, target, false);
    }

    @Test
    public void testIncompatibleOutputTypes() throws Exception {
        InterfaceContract source = new MockContract("FooContract");
        DataType sourceType = new DataTypeImpl<Type>(String.class, String.class);
        ArrayList sourceTypes = new ArrayList();
        sourceTypes.add(sourceType);
        DataType sourceOutputType = new DataTypeImpl(Object[].class, sourceTypes);
        Operation opSource1 = newOperation("op1");
        opSource1.setOutputType(sourceOutputType);
        Map<String, Operation> sourceOperations = new HashMap<String, Operation>();
        sourceOperations.put("op1", opSource1);
        source.getInterface().getOperations().addAll(sourceOperations.values());

        InterfaceContract target = new MockContract("FooContract");
        DataType targetType = new DataTypeImpl<Type>(Integer.class, Integer.class);
        ArrayList targetTypes = new ArrayList();
        targetTypes.add(targetType);
        DataType targetOutputType = new DataTypeImpl(Object[].class, targetTypes);
        Operation opTarget = newOperation("op1");
        opTarget.setOutputType(targetOutputType);
        Map<String, Operation> targetOperations = new HashMap<String, Operation>();
        targetOperations.put("op1", opTarget);
        target.getInterface().getOperations().addAll(targetOperations.values());
        try {
            mapper.checkCompatibility(source, target, Compatibility.SUBSET, false, false);
            fail();
        } catch (IncompatibleInterfaceContractException e) {
            // expected
        }
    }

    @Test
    public void testFaultTypes() throws Exception {
        InterfaceContract source = new MockContract("FooContract");
        DataType sourceFaultType = new DataTypeImpl<Type>(String.class, String.class);
        List<DataType> sourceFaultTypes = new ArrayList<DataType>();
        sourceFaultTypes.add(0, sourceFaultType);
        Operation opSource1 = newOperation("op1");
        opSource1.setFaultTypes(sourceFaultTypes);
        Map<String, Operation> sourceOperations = new HashMap<String, Operation>();
        sourceOperations.put("op1", opSource1);
        source.getInterface().getOperations().addAll(sourceOperations.values());

        InterfaceContract target = new MockContract("FooContract");
        DataType targetFaultType = new DataTypeImpl<Type>(String.class, String.class);
        List<DataType> targetFaultTypes = new ArrayList<DataType>();
        targetFaultTypes.add(0, targetFaultType);

        Operation opTarget = newOperation("op1");
        opTarget.setFaultTypes(targetFaultTypes);
        Map<String, Operation> targetOperations = new HashMap<String, Operation>();
        targetOperations.put("op1", opTarget);
        target.getInterface().getOperations().addAll(targetOperations.values());
        mapper.checkCompatibility(source, target, Compatibility.SUBSET, false, false);
    }

    @Test
    public void testSourceFaultTargetNoFaultCompatibility() throws Exception {
        InterfaceContract source = new MockContract("FooContract");
        DataType sourceFaultType = new DataTypeImpl<Type>(String.class, String.class);
        List<DataType> sourceFaultTypes = new ArrayList<DataType>();
        sourceFaultTypes.add(0, sourceFaultType);
        Operation opSource1 = newOperation("op1");
        opSource1.setFaultTypes(sourceFaultTypes);
        Map<String, Operation> sourceOperations = new HashMap<String, Operation>();
        sourceOperations.put("op1", opSource1);
        source.getInterface().getOperations().addAll(sourceOperations.values());

        InterfaceContract target = new MockContract("FooContract");
        Operation opTarget = newOperation("op1");
        Map<String, Operation> targetOperations = new HashMap<String, Operation>();
        targetOperations.put("op1", opTarget);
        target.getInterface().getOperations().addAll(targetOperations.values());
        mapper.checkCompatibility(source, target, Compatibility.SUBSET, false, false);
    }

    /**
     * Verifies a source's fault which is a supertype of the target's fault are
     * compatible
     * 
     * @throws Exception
     */
    @Test
    public void testFaultSuperTypes() throws Exception {
        // InterfaceContract source = new MockContract("FooContract");
        // DataType sourceFaultType = new DataTypeImpl<Type>(Exception.class,
        // Exception.class);
        // List<DataType> sourceFaultTypes = new ArrayList<DataType>();
        // sourceFaultTypes.add(0, sourceFaultType);
        // Operation opSource1 = newOperationImpl("op1", null, null,
        // sourceFaultTypes, false, null);
        // Map<String, Operation> sourceOperations = new HashMap<String,
        // Operation>();
        // sourceOperations.put("op1", opSource1);
        // source.getInterface().getOperations().addAll(sourceOperations.values());
        //
        // InterfaceContract target = new MockContract("FooContract");
        // DataType targetFaultType = new
        // DataTypeImpl<Type>(TuscanyException.class, TuscanyException.class);
        // List<DataType> targetFaultTypes = new ArrayList<DataType>();
        // targetFaultTypes.add(0, targetFaultType);
        //
        // Operation opTarget = newOperationImpl("op1", null, null,
        // targetFaultTypes, false, null);
        // Map<String, Operation> targetOperations = new HashMap<String,
        // Operation>();
        // targetOperations.put("op1", opTarget);
        // target.getInterface().getOperations().addAll(targetOperations.values());
        // wireService.checkCompatibility(source, target, false);
    }

    /**
     * Verifies a source's faults which are supertypes and a superset of the
     * target's faults are compatible
     */
    @Test
    public void testFaultSuperTypesAndSuperset() throws Exception {
        // InterfaceContract source = new MockContract("FooContract");
        // DataType sourceFaultType = new DataTypeImpl<Type>(Exception.class,
        // Exception.class);
        // DataType sourceFaultType2 = new
        // DataTypeImpl<Type>(RuntimeException.class, RuntimeException.class);
        // List<DataType> sourceFaultTypes = new ArrayList<DataType>();
        // sourceFaultTypes.add(0, sourceFaultType);
        // sourceFaultTypes.add(1, sourceFaultType2);
        // Operation opSource1 = newOperationImpl("op1", null, null,
        // sourceFaultTypes, false, null);
        // Map<String, Operation> sourceOperations = new HashMap<String,
        // Operation>();
        // sourceOperations.put("op1", opSource1);
        // source.getInterface().getOperations().addAll(sourceOperations.values());
        //
        // InterfaceContract target = new MockContract("FooContract");
        // DataType targetFaultType = new
        // DataTypeImpl<Type>(TuscanyException.class, TuscanyException.class);
        // List<DataType> targetFaultTypes = new ArrayList<DataType>();
        // targetFaultTypes.add(0, targetFaultType);
        //
        // Operation opTarget = newOperationImpl("op1", null, null,
        // targetFaultTypes, false, null);
        // Map<String, Operation> targetOperations = new HashMap<String,
        // Operation>();
        // targetOperations.put("op1", opTarget);
        // target.getInterface().getOperations().addAll(targetOperations.values());
        // wireService.checkCompatibility(source, target, false);
    }

    private static class MockInterface extends InterfaceImpl {

    }

    private class MockContract<T> extends InterfaceContractImpl {
        public MockContract() {
        }

        public MockContract(String interfaceClass) {
            Interface jInterface = new MockInterface();
            setInterface(jInterface);
        }
    }

    private static Operation newOperation(String name) {
        Operation operation = new OperationImpl();
        operation.setName(name);
        ArrayList<Object> outputTypes = new ArrayList<Object>();
        outputTypes.add(new DataTypeImpl(Object.class, Object.class));
        operation.setOutputType(new DataTypeImpl(Object[].class, outputTypes));
        return operation;
    }
}
