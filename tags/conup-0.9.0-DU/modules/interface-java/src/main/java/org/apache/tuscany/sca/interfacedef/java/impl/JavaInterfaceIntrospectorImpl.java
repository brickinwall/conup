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

import static org.apache.tuscany.sca.interfacedef.Operation.IDL_INPUT;
import static org.apache.tuscany.sca.interfacedef.Operation.IDL_OUTPUT;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;

import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.InvalidAnnotationException;
import org.apache.tuscany.sca.interfacedef.InvalidCallbackException;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.InvalidOperationException;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.OverloadedOperationException;
import org.apache.tuscany.sca.interfacedef.ParameterMode;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaOperation;
import org.apache.tuscany.sca.interfacedef.java.introspect.JavaInterfaceVisitor;
import org.apache.tuscany.sca.interfacedef.util.JavaXMLMapper;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.oasisopen.sca.ResponseDispatch;
import org.oasisopen.sca.annotation.AsyncFault;
import org.oasisopen.sca.annotation.AsyncInvocation;
import org.oasisopen.sca.annotation.OneWay;
import org.oasisopen.sca.annotation.Remotable;

/**
 * Default implementation of a Java interface introspector.
 * 
 * @version $Rev: 1291191 $ $Date: 2012-02-20 10:57:50 +0000 (Mon, 20 Feb 2012) $
 */
public class JavaInterfaceIntrospectorImpl {

    private static final String UNKNOWN_DATABINDING = null;

    private JavaInterfaceFactory javaFactory = null;
    private List<JavaInterfaceVisitor> visitors = new ArrayList<JavaInterfaceVisitor>();
    private boolean loadedVisitors;

    public JavaInterfaceIntrospectorImpl(JavaInterfaceFactory javaFactory) {
        this.javaFactory = javaFactory;
    }

    public void introspectInterface(JavaInterface javaInterface, Class<?> clazz) throws InvalidInterfaceException {

        if(!loadedVisitors) {
            this.visitors = javaFactory.getInterfaceVisitors();
        }

        javaInterface.setJavaClass(clazz);

        boolean remotable = clazz.isAnnotationPresent(Remotable.class);

        // Consider @javax.ejb.Remote, java.rmi.Remote and javax.ejb.EJBObject
        // equivalent to @Remotable
        if (!remotable) {
            for (Annotation annotation : clazz.getAnnotations()) {
                if ("javax.ejb.Remote".equals(annotation.annotationType().getName())) {
                    remotable = true;
                    break;
                }
            }
        }
        if (!remotable) {
            for (Class<?> superInterface : clazz.getInterfaces()) {
                if (Remote.class == superInterface || "javax.ejb.EJBObject".equals(superInterface.getName())) {
                    remotable = true;
                    break;
                }
            }
        }

        if (remotable) {
            if (javaInterface.isRemotableSetFromSCDL() && javaInterface.isRemotable() == false) {
                throw new InvalidAnnotationException("[JCA30005] @Remotable annotation present in a interface marked as not remotable in the SCDL", Remotable.class);
            }
        } else {
            if (javaInterface.isRemotableSet()) {
                remotable = javaInterface.isRemotable();
            }
        }

        javaInterface.setRemotable(remotable);

        Class<?> callbackClass = null;
        org.oasisopen.sca.annotation.Callback callback = clazz.getAnnotation(org.oasisopen.sca.annotation.Callback.class);
        if (callback != null && !Void.class.equals(callback.value())) {
            callbackClass = callback.value();
            if (remotable && !callbackClass.isAnnotationPresent(Remotable.class)) {
                throw new InvalidCallbackException("Callback " + callbackClass.getName() + 
                                                   " must be remotable on remotable interface " + clazz.getName());
            }
            if (!remotable && callbackClass.isAnnotationPresent(Remotable.class)) {
                throw new InvalidCallbackException("Callback" + callbackClass.getName() + 
                                                   " must not be remotable on local interface " + clazz.getName());
            }
        } else if (callback != null && Void.class.equals(callback.value())) {
            throw new InvalidCallbackException("No callback interface specified on callback annotation in " + clazz.getName());
        }

        javaInterface.setCallbackClass(callbackClass);

        String ns = JavaXMLMapper.getNamespace(clazz);
        javaInterface.getOperations().addAll(getOperations(clazz, remotable, ns));

        for (JavaInterfaceVisitor extension : visitors) {
            extension.visitInterface(javaInterface);
        } // end for

        // Check if any methods have disallowed annotations
        // Check if any private methods have illegal annotations that should be raised as errors
        checkMethodAnnotations(clazz, javaInterface);
    } // end method introspectInterface

    private void checkMethodAnnotations(Class clazz, JavaInterface javaInterface) throws InvalidAnnotationException {

        final Class _clazz = clazz;
        Method[] declaredMethods = (Method[])AccessController.doPrivileged(new PrivilegedAction<Method[]>() {
            public Method[] run() {
                return _clazz.getDeclaredMethods();
            }
        });

        for (final Method method : declaredMethods) {
            for ( Annotation a : method.getAnnotations() ) {
                if( a instanceof Remotable ) {
                    // [JCA90053] @Remotable annotation cannot be on a method that is not a setter method
                    if( !JavaIntrospectionHelper.isSetter(method) ) {
                        throw new InvalidAnnotationException("[JCA90053] @Remotable annotation present on an interface method" +
                                                             " which is not a Setter method: " + javaInterface.getName() + "/" + method.getName(), Remotable.class);
                    } // end if
                } // end if		
            } // end for
            
            // Parameter annotations
            for (Annotation[] parmAnnotations : method.getParameterAnnotations()) {
                for (Annotation annotation : parmAnnotations) {
                    if (annotation instanceof Remotable ) {
                        throw new InvalidAnnotationException("[JCA90053] @Remotable annotation present on an interface method" +
                                                             " parameter: " + javaInterface.getName() + "/" + method.getName(), Remotable.class);
                    } // end if
                } // end for		
            } // end for
        }
    } // end method checkMethodAnnotations

    private Class<?>[] getActualTypes(Type[] types, Class<?>[] rawTypes, Map<String, Type> typeBindings, boolean ignoreAsyncHolder) {
        int x = ignoreAsyncHolder ? types.length -1 : types.length;
        Class<?>[] actualTypes = new Class<?>[x];
        for (int i = 0; i < x; i++) {
            actualTypes[i] = getActualType(types[i], rawTypes[i], typeBindings);
        }
        return actualTypes;
    }

    private Class<?> getActualType(Type type, Class<?> rawType, Map<String, Type> typeBindings) {
        if (type instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>)type;
            type = typeBindings.get(typeVariable.getName());
            if (type instanceof Class<?>) {
                return (Class<?>)type;
            }
        }
        return rawType;
    }

    @SuppressWarnings("rawtypes")
    private <T> List<Operation> getOperations(Class<T> clazz,
                                              boolean remotable,
                                              String ns) throws InvalidInterfaceException {

        Set<Type> genericInterfaces = new HashSet<Type>();
        collectGenericInterfaces(clazz, genericInterfaces);
        Map<String, Type> typeBindings = new HashMap<String, Type>();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType)genericInterface;
                TypeVariable<?>[] typeVariables = ((Class<?>)parameterizedType.getRawType()).getTypeParameters();
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                for (int i = 0; i < typeArguments.length; i++) {
                    typeBindings.put(typeVariables[i].getName(), typeArguments[i]);
                }
            }
        }
        
        boolean isAsyncService = clazz.isAnnotationPresent(AsyncInvocation.class);

        Method[] methods = clazz.getMethods();
        List<Operation> operations = new ArrayList<Operation>(methods.length);
        Set<String> names = remotable ? new HashSet<String>() : null;
        for (Method method : methods) {
            boolean hasMultipleOutputs = false;
            if (method.getDeclaringClass() == Object.class) {
                // Skip the methods on the Object.class
                continue;
            }
            String name = method.getName();

            Class<?> lastParameter = method.getParameterTypes().length > 0 ? method.getParameterTypes()[method.getParameterTypes().length-1] : null;
            boolean isAsyncMethod = isAsyncService && name.endsWith("Async") && lastParameter != null && ResponseDispatch.class.equals(lastParameter);

            if (isAsyncMethod) {
                name = name.substring(0, name.length()-5);
            }

            if (remotable && names.contains(name)) {
                throw new OverloadedOperationException(method);
            }
            if (remotable && !jaxwsAsyncMethod(method)) {
                names.add(name);
            }

            Class<?>[] parameterTypes = getActualTypes(method.getGenericParameterTypes(), method.getParameterTypes(), typeBindings, isAsyncMethod);

            Class<?> returnType;
            if (isAsyncMethod) {
                ParameterizedType t = (ParameterizedType)method.getGenericParameterTypes()[method.getGenericParameterTypes().length-1];
                returnType = (Class<?>)t.getActualTypeArguments()[0];
            } else {
                returnType = getActualType(method.getGenericReturnType(), method.getReturnType(), typeBindings);
            }

            Class<?>[] faultTypes =
                getActualTypes(method.getGenericExceptionTypes(), method.getExceptionTypes(), typeBindings, false);
            Class<?>[] allOutputTypes = getOutputTypes(returnType, parameterTypes);

            // For async server interfaces, faults are described using the @AsyncFaults annotation
            if( method.isAnnotationPresent(AsyncFault.class) ) {
                faultTypes = readAsyncFaultTypes( method );
            } // end if 

            boolean nonBlocking = method.isAnnotationPresent(OneWay.class);
            if (nonBlocking) {
                if (!(returnType == void.class)) {
                    throw new InvalidOperationException(
                                                        "Method should return 'void' when declared with an @OneWay annotation. " + method,
                                                        method);
                }
                if (!(faultTypes.length == 0)) {
                    throw new InvalidOperationException(
                                                        "Method should not declare exceptions with an @OneWay annotation. " + method,
                                                        method);
                }
            }

            JavaOperation operation = new JavaOperationImpl();
            operation.setName(name);

            // Given details of Holder mapping, it's easier to handle output first.                
            List<DataType> outputDataTypes = new ArrayList<DataType>();
            XMLType xmlReturnType = new XMLType(new QName(ns, "return"), null);            
            DataType<XMLType> returnDataType = null;
            if (returnType == void.class) {
                operation.setReturnTypeVoid(true);
            } else {
                if (isAsyncMethod) {
                    returnDataType = new DataTypeImpl<XMLType>(UNKNOWN_DATABINDING, returnType, returnType, xmlReturnType);
                } else {
                    returnDataType = new DataTypeImpl<XMLType>(UNKNOWN_DATABINDING, returnType, method.getGenericReturnType(), xmlReturnType);
                }
                 operation.setReturnTypeVoid(false);
                 outputDataTypes.add(returnDataType);
            }

            // Handle Input Types
            List<DataType> paramDataTypes = new ArrayList<DataType>(parameterTypes.length);
            Type[] genericParamTypes = method.getGenericParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> paramType = parameterTypes[i];
                XMLType xmlParamType = new XMLType(new QName(ns, "arg" + i), null);                            

                DataTypeImpl<XMLType> xmlDataType = new DataTypeImpl<XMLType>(
                    UNKNOWN_DATABINDING, paramType, genericParamTypes[i],xmlParamType);
                ParameterMode mode = ParameterMode.IN;
                // Holder pattern. Physical types of Holder<T> classes are updated to <T> to aid in transformations.
                if ( Holder.class == paramType) {
                    hasMultipleOutputs = true;
                    Type firstActual = getFirstActualType( genericParamTypes[ i ] );
                    if ( firstActual != null ) {
                        xmlDataType.setPhysical( (Class<?>)firstActual );
                        mode = ParameterMode.INOUT;
                    } 
                    outputDataTypes.add(xmlDataType);
                }
                paramDataTypes.add( xmlDataType);
                operation.getParameterModes().add(mode);
            }

            // Fault types                                                          
            List<DataType> faultDataTypes = new ArrayList<DataType>(faultTypes.length);
            Type[] genericFaultTypes = method.getGenericExceptionTypes();
            if( method.isAnnotationPresent(AsyncFault.class) ) {
                genericFaultTypes = readAsyncGenericFaultTypes( method );
            } // end if
            for (int i = 0; i < faultTypes.length; i++) {
                Class<?> faultType = faultTypes[i];
                // Only add checked exceptions
                // JAXWS Specification v2.1 section 3.7 says RemoteException should not be mapped
                if (Exception.class.isAssignableFrom(faultType) && (!RuntimeException.class.isAssignableFrom(faultType))
                    && (!RemoteException.class.isAssignableFrom(faultType))) {
                    XMLType xmlFaultType = new XMLType(new QName(ns, faultType.getSimpleName()), null);
                    DataType<XMLType> faultDataType =
                        new DataTypeImpl<XMLType>(UNKNOWN_DATABINDING, faultType, genericFaultTypes[i], xmlFaultType);
                    faultDataTypes.add(new DataTypeImpl<DataType>(UNKNOWN_DATABINDING, faultType, genericFaultTypes[i],
                        faultDataType));
                }
            }

            DataType<List<DataType>> inputType =
                new DataTypeImpl<List<DataType>>(IDL_INPUT, Object[].class, paramDataTypes);
            DataType<List<DataType>> outputType = 
                new DataTypeImpl<List<DataType>>(IDL_OUTPUT, Object[].class, outputDataTypes);

            operation.setOutputType(outputType);

            operation.setInputType(inputType);                     
            operation.setFaultTypes(faultDataTypes);
            operation.setNonBlocking(nonBlocking);
            operation.setJavaMethod(method);
            operation.setHasArrayWrappedOutput(hasMultipleOutputs); 
            operation.setAsyncServer(isAsyncMethod);
            operations.add(operation);
        }
        return operations;
    }


    private Class<?>[] getOutputTypes(Class<?> returnType, Class<?>[] parameterTypes) {

        ArrayList<Class<?>> returnTypes = new ArrayList<Class<?>>();
        returnTypes.add(returnType);
        for ( Class<?> clazz : parameterTypes ) {
            if ( Holder.class == clazz )
                returnTypes.add(clazz);
        }
        Class[] arrayType = new Class[0];
        return returnTypes.toArray(arrayType);
    }



    /**
     * Reads the fault types declared in an @AsyncFault annotation on an async server method
     * @param method - the Method
     * @return - an array of fault/exception classes
     */
    private  Class<?>[] readAsyncFaultTypes( Method method ) {
        AsyncFault theFaults = method.getAnnotation(AsyncFault.class);
        if ( theFaults == null ) return null;
        return theFaults.value();
    } // end method readAsyncFaultTypes

    /**
     * Reads the generic fault types declared in an @AsyncFault annotation on an async server method
     * @param method - the Method
     * @return - an array of fault/exception classes
     */
    private  Type[] readAsyncGenericFaultTypes( Method method ) {
        AsyncFault theFaults = method.getAnnotation(AsyncFault.class);
        if ( theFaults == null ) return null;
        return theFaults.value();
    } // end method readAsyncFaultTypes

    private boolean jaxwsAsyncMethod(Method method) {
        if (method.getName().endsWith("Async")) {
            if (method.getReturnType().isAssignableFrom(Future.class)) {
                if (method.getParameterTypes().length > 0) {
                    if (method.getParameterTypes()[method.getParameterTypes().length-1].isAssignableFrom(AsyncHandler.class)) {
                        return true;
                    }
                }
            }
            if (method.getReturnType().isAssignableFrom(Response.class)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Given a Class<T>, returns T, otherwise null.
     * @param testClass
     * @return
     */
    protected static Type getFirstActualType(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType)genericType;
            Type[] actualTypes = pType.getActualTypeArguments();
            if ((actualTypes != null) && (actualTypes.length > 0)) {
                return actualTypes[0];
            }
        }
        return null;
    }

    private void collectGenericInterfaces(Class<?> clazz, Set<Type> genericInterfaces) {
        for (Type t : clazz.getGenericInterfaces()) {
            genericInterfaces.add(t);
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        for(Class<?> c : interfaces){
            collectGenericInterfaces(c, genericInterfaces);
        }
    }
    
}
