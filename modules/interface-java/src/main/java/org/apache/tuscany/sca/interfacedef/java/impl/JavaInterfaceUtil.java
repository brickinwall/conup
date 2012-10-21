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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.ParameterMode;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaOperation;

/**
 * Contains methods for mapping between an operation in a
 * {@link org.apache.tuscany.spi.model.ServiceContract} and a method defined by
 * a Java interface
 * 
 * @version $Rev: 1214229 $ $Date: 2011-12-14 13:08:30 +0000 (Wed, 14 Dec 2011) $
 * @tuscany.spi.extension.asclient
 */
public final class JavaInterfaceUtil {

    private JavaInterfaceUtil() {
    }

    /**
     * Return the method on the implementation class that matches the operation.
     * 
     * @param implClass the implementation class or interface
     * @param operation the operation to match
     * @return the method described by the operation
     * @throws NoSuchMethodException if no such method exists
     * @Deprecated
     */
    public static Method findMethod(Class<?> implClass, Operation operation) throws NoSuchMethodException {
        String name = operation.getName();
        if (operation instanceof JavaOperation) {
        	if( ((JavaOperation)operation).isAsyncServer() ) {
        		// In this case, the operation is a mapped async server style method and needs special handling
        		return findAsyncServerMethod( implClass, (JavaOperation)operation );
        	} else {
        		name = ((JavaOperation)operation).getJavaMethod().getName();
        	} // end if
        }
        Interface interface1 = operation.getInterface();
        int numParams = operation.getInputType().getLogical().size();
        
        // Account for OUT-only in matching. (Should we cache this number in JavaOperation?) 
        List<ParameterMode> parmModes = operation.getParameterModes();
        int numOutOnlyHolders = 0;
        for (ParameterMode mode : parmModes) {
            if (mode.equals(ParameterMode.OUT)) {
                numOutOnlyHolders++;
            }
        }
        numParams += numOutOnlyHolders;
        
        if (interface1 != null && interface1.isRemotable()) {
            List<Method> matchingMethods = new ArrayList<Method>();
            for (Method m : implClass.getMethods()) {
                if (m.getName().equals(name) && m.getParameterTypes().length == numParams) {
                    matchingMethods.add(m);
                } else if (m.getName().equals(name + "Async") && m.getParameterTypes().length == numParams + 1) {
                    matchingMethods.add(m);
                }
            }
            
            // TUSCANY-2180 If there is only one method then we just match on the name 
            // (this is the same as the existing behaviour)
            if (matchingMethods.size() == 1) {
                return matchingMethods.get(0);
            }
            if (matchingMethods.size() > 1) {
                // TUSCANY-2180 We need to check the parameter types too
                Class<?>[] paramTypes = getPhysicalTypes(operation);
                return implClass.getMethod(name, paramTypes);
            }
            
            // No matching method found
            throw new NoSuchMethodException("No matching method for operation " + operation.getName()
                + " is found on "
                + implClass);
        }
        Class<?>[] paramTypes = getPhysicalTypes(operation);
        return implClass.getMethod(name, paramTypes);
    }
    
    /**
     * Return the method on the implementation class that matches the async server version of the operation.
     * 
     * @param implClass the implementation class or interface
     * @param operation the operation to match - this is the sync equivalent of an async server operation
     * @return the method described by the operation
     * @throws NoSuchMethodException if no such method exists
     */
    public static Method findAsyncServerMethod(Class<?> implClass, JavaOperation operation) throws NoSuchMethodException {
        
        if (operation.getJavaMethod() != null) {
            return operation.getJavaMethod();
        }
        
        String name = operation.getJavaMethod().getName();
        List<Operation> actualOps = (List<Operation>) operation.getInterface().getAttributes().get("ASYNC-SERVER-OPERATIONS");
        Operation matchingOp = null;
        for( Operation op: actualOps ) {
        	if( op.getName().equals(name) ) {
        		matchingOp = op;
        		break;
        	}
        } // end for
        if( matchingOp == null ) throw new NoSuchMethodException("No matching async method for operation " + operation.getName());
        
        int numParams = matchingOp.getInputType().getLogical().size();
        
        List<Method> matchingMethods = new ArrayList<Method>();
        for (Method m : implClass.getMethods()) {
            if (m.getName().equals(name) && m.getParameterTypes().length == (numParams) ) {
                matchingMethods.add(m);
            }
        }
        
        if (matchingMethods.size() == 1) {
            return matchingMethods.get(0);
        }
        if (matchingMethods.size() > 1) {
            Class<?>[] paramTypes = getPhysicalTypes(matchingOp);
            return implClass.getMethod(name, paramTypes);
        } 
        
        // No matching method found
        throw new NoSuchMethodException("No matching method for operation " + operation.getName()
            + " is found on " + implClass);
        
    } // end method findAsyncServerMethod

    /**
     * @Deprecated
     */
    //TODO - account for Holder(s)
    private static Class<?>[] getPhysicalTypes(Operation operation) {
        DataType<List<DataType>> inputType = operation.getInputType();
        if (inputType == null) {
            return new Class<?>[] {};
        }
        List<DataType> types = inputType.getLogical();
        Class<?>[] javaTypes = new Class<?>[types.size()];
        for (int i = 0; i < javaTypes.length; i++) {
            DataType<?> type = types.get(i);
            javaTypes[i] = getClassOfDataType(type);
        }
        return javaTypes;
    }

    /**
     * Searches a collection of operations for a match against the given method
     * 
     * @param method the method to match
     * @param operations the operations to match against
     * @return a matching operation or null
     * @Deprecated
     */
    public static Operation findOperation(Method method, Collection<Operation> operations) {
        for (Operation operation : operations) {
            if (match(operation, method)) {
                return operation;
            }
        }
        return null;
    }

    /**
     * Determines if the given operation matches the given method
     * 
     * @return true if the operation matches, false if does not
     */
    //TODO - account for Holder(s)    
    private static boolean match(Operation operation, Method method) {
        Class<?>[] params = method.getParameterTypes();
        DataType<List<DataType>> inputType = operation.getInputType();
        List<DataType> types = inputType.getLogical();
        boolean found = true;
        if (types.size() == params.length && method.getName().equals(operation.getName())) {
            for (int i = 0; i < params.length; i++) {
                Class<?> clazz = params[i];
                if (!clazz.equals(operation.getInputType().getLogical().get(i).getPhysical())) {
                    found = false;
                }
            }
        } else {
            found = false;
        }
        return found;

    }
    
    private static String getPackageName(Class<?> cls) {
        String name = cls.getName();
        int index = name.lastIndexOf('.');
        return index == -1 ? "" : name.substring(0, index);
    }

    public static String getNamespace(Class<?> cls) {
        String packageName = getPackageName(cls);
        if ("".equals(packageName)) {
            return "";
        }
        StringBuffer ns = new StringBuffer("http://");
        String[] names = packageName.split("\\.");
        for (int i = names.length - 1; i >= 0; i--) {
            ns.append(names[i]);
            if (i != 0) {
                ns.append('.');
            }
        }
        ns.append('/');
        return ns.toString();
    }

    /**
     * Get the Java Type that represent the DataType informed
     * When dataType.getGenericType() is GenericArrayType or WildcardType the Physical type is used, 
     * because the physical type have the correct information about this DataType.
     * @param dataType DataType
     * @return The Class<?> that represent the DataType
     */
    private static Class<?> getClassOfDataType(DataType<?> dataType){
        Type generic = dataType.getGenericType();
        boolean isGeneric = (generic != null 
                        && generic != dataType.getPhysical()
                        && (generic instanceof TypeVariable<?>
                                || generic instanceof ParameterizedType));
        Class<?> javaType = null;
        if (isGeneric) {
            javaType = getClassOfSimpleGeneric(generic);
        }else {
            Type physical = dataType.getPhysical();
            javaType = getClassOfPhysical(physical);
        }
        if (javaType == null) {
            throw new UnsupportedOperationException();
        }
        return javaType;
    }
    
    /**
     * Return Class<?> of Type Generic informed
     * @param generic The Generic Type 
     * @return  The Class<?> that represent the Generic
     */
    private static Class<?> getClassOfSimpleGeneric(Type generic){
        Class<?> javaType = null;
        if (generic instanceof TypeVariable<?>){
            javaType = (Class<?>)Object.class;
        } else if (generic instanceof ParameterizedType){
            javaType = (Class<?>)((ParameterizedType)generic).getRawType();
        }
        return javaType;
    }

    /**
     * Return Class<?> of Type Physical informed
     * @param physical The Physical 
     * @return  The Class<?> that represent the Physical
     */
    private static Class<?> getClassOfPhysical(Type physical){
        Class<?> javaType = null; 
        if (physical instanceof Class<?>) {
            javaType = (Class<?>)physical;
        }
        return javaType;
    }     

}
