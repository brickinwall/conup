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
package org.apache.tuscany.sca.binding.ejb.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJBObject;
import javax.rmi.CORBA.Util;

import org.apache.tuscany.sca.binding.ejb.corba.ClassLoadingUtil;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * EJBMessageHandler
 *
 * @version $Rev: 738490 $ $Date: 2009-01-28 14:07:54 +0000 (Wed, 28 Jan 2009) $
 */
public class EJBHandler {
    private static final Map<String, Class> PRIMITIVE_TYPES = new HashMap<String, Class>();
    static {
        PRIMITIVE_TYPES.put("boolean", boolean.class);
        PRIMITIVE_TYPES.put("byte", byte.class);
        PRIMITIVE_TYPES.put("char", char.class);
        PRIMITIVE_TYPES.put("short", short.class);
        PRIMITIVE_TYPES.put("int", int.class);
        PRIMITIVE_TYPES.put("long", long.class);
        PRIMITIVE_TYPES.put("float", float.class);
        PRIMITIVE_TYPES.put("double", double.class);
        PRIMITIVE_TYPES.put("void", void.class);
    }

    private Object ejbStub;

    private InterfaceInfo interfaceInfo;
    private Class ejbInterface;

    public EJBHandler(NamingEndpoint namingEndpoint, Class ejbInterface) {
        this(namingEndpoint, InterfaceInfo.getInstance(ejbInterface));
        this.ejbInterface = ejbInterface;
    }

    // locates the stub
    private EJBHandler(NamingEndpoint namingEndpoint, InterfaceInfo ejbInterface) {
        try {
            this.ejbStub = EJBStubHelper.lookup(namingEndpoint, ejbInterface);
            this.interfaceInfo = ejbInterface;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private static Class loadClass(final String name) {
        try {
            return ClassLoadingUtil.loadClass(name, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    // invokes EJB method
    public Object invoke(String methodName, Object[] args) {
        Object response = null;
        try {
            if (ejbStub instanceof ObjectImpl) {
                ObjectImpl objectImpl = (ObjectImpl)ejbStub;
                // TODO: If the Java 2 security is turned on, then
                // the ORB will try to create proxy
                // from the interfaces defined on the stub
                if (System.getSecurityManager() == null && objectImpl._is_local()) {
                    /*
                     * CORBA.Stub is what the object from JNDI will be for a
                     * remote EJB in the same JVM as the client, but with no
                     * stub classes available on the client
                     */
                    response = invokeLocalCORBACall(objectImpl, methodName, args);
                } else {
                    /*
                     * _EJBObject_Stub is what the object from JNDI will be for
                     * a remote EJB with no stub classes available on the client
                     */
                    response = invokeRemoteCORBACall(objectImpl, methodName, args);
                }
            } else {
                /*
                 * A generated ejb stub or it must be an EJB in the same ear as
                 * the client or an AppServer with a single ClassLoader, so
                 * reflection can be used directly on the JNDI
                 */
                JavaReflectionAdapter reflectionAdapter =
                    JavaReflectionAdapter.createJavaReflectionAdapter(ejbStub.getClass());
                try {
                    Method method = reflectionAdapter.getMethod(methodName);
                    response = method.invoke(ejbStub, args);
                } catch (InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    // FIXME need to throw really a business exception.
                    // ServiceBusinessException?
                    // Tuscany core doesn't have ServiceBusinessException
                    throw new ServiceRuntimeException(t);
                }
            }

            return response;
        } catch (Exception e) {
            // FIXME this be business exception? Tuscany core doesn't have
            // ServiceBusinessException
            throw new ServiceRuntimeException(e);

        } catch (Throwable e) {
            throw new ServiceRuntimeException(e);
        }
    }

    /**
     * Get the IDL operation name for a java method
     * 
     * @param methodName java method name
     * @return The IDL operation name
     */
    private String getOperation(String methodName) {
        if (interfaceInfo == null) {
            return methodName;
        }
        MethodInfo methodInfo = interfaceInfo.getMethod(methodName);
        if (methodInfo != null) {
            return methodInfo.getIDLName();
        } else {
            return null;
        }
    }

    /*
     * Derive the EJB interface name from the Stub When loading a stub class
     * corresponding to an interface or class <packagename>.<typename>, the
     * class <packagename>._<typename>_Stub shall be used if it exists;
     * otherwise, the class org.omg.stub.<packagename>._<typename>_Stub shall
     * be used.
     */
    private static String getInterface(String stubName) {
        int index = stubName.lastIndexOf('.');
        String packageName = null;
        String typeName = stubName;
        if (index != -1) {
            packageName = stubName.substring(0, index);
            if (packageName.startsWith("org.omg.stub.")) {
                packageName = packageName.substring("org.omg.stub.".length());
            }
            typeName = stubName.substring(index + 1);
        }
        if (typeName.startsWith("_") && typeName.endsWith("_Stub")) {
            typeName = typeName.substring(1, typeName.length() - "_Stub".length());
        }
        if (packageName != null)
            return packageName + "." + typeName;
        else
            return typeName;
    }

    /**
     * Invoke a method on the local CORBA object
     * 
     * @param stub
     * @param methodName
     * @param args
     * @return
     * @throws RemoteException
     * @throws ServiceBusinessException
     */
    private Object invokeLocalCORBACall(final ObjectImpl stub, String methodName, Object[] args)
        throws RemoteException {

        final String operation = getOperation(methodName);

        Class type = loadClass(getInterface(stub.getClass().getName()));
        if (type == null)
            type = (ejbInterface != null) ? ejbInterface : EJBObject.class;

        ServantObject so = stub._servant_preinvoke(operation, type);
        if (so == null) {
            // The Servant is not local any more
            return invokeRemoteCORBACall(stub, methodName, args);
        }
        Object[] newArgs = null;
        ORB orb = stub._orb();
        try {
            if (args != null)
                newArgs = Util.copyObjects(args, orb);
            JavaReflectionAdapter reflectionAdapter =
                JavaReflectionAdapter.createJavaReflectionAdapter(so.servant.getClass());
            Method method = reflectionAdapter.getMethod(methodName);
            Object obj = reflectionAdapter.invoke(method, so.servant, newArgs);
            Object result = Util.copyObject(obj, orb);
            return result;

        } catch (InvocationTargetException e) {
            Throwable exCopy = (Throwable)Util.copyObject(e.getTargetException(), orb);
            MethodInfo methodInfo = interfaceInfo.getMethod(methodName);
            String[] exceptionTypes = methodInfo.getExceptionTypes();
            for (int i = 0; i < exceptionTypes.length; i++) {
                Class exceptionType =
                    methodInfo.getMethod() != null ? methodInfo.getMethod().getExceptionTypes()[i]
                        : loadClass(exceptionTypes[i]);
                if (exceptionType.isAssignableFrom(exCopy.getClass()))
                    throw new ServiceRuntimeException(exCopy); // FIXME should
                // be business
                // exception?
            }
            throw Util.wrapException(exCopy);
        } catch (Throwable e) {
            // Other exceptions thrown from "invoke"
            throw new ServiceRuntimeException(e);
        } finally {
            stub._servant_postinvoke(so);
        }
    }

    /**
     * Invoke a method on a remote CORBA object
     * 
     * @param stub The remote stub
     * @param methodName The name of the method
     * @param args Argument list
     * @return
     * @throws RemoteException
     * @throws ServiceBusinessException
     */
    private Object invokeRemoteCORBACall(ObjectImpl stub, String methodName, Object[] args) throws RemoteException {

        try {
            String operation = getOperation(methodName);

            MethodInfo methodInfo = interfaceInfo.getMethod(methodName);
            if (methodInfo == null) {
                throw new ServiceRuntimeException("Invalid Method " + methodName);
            }
            Class[] parameterTypes = null;
            Class returnType = null;
            if (methodInfo.getMethod() != null) {
                parameterTypes = methodInfo.getMethod().getParameterTypes();
                returnType = methodInfo.getMethod().getReturnType();
            } else {
                String[] types = methodInfo.getParameterTypes();
                if (args != null) {
                    if (types.length != args.length)
                        throw new ServiceRuntimeException(
                                                          "The argument list doesn't match the method signature of " + methodName);
                }

                parameterTypes = new Class[types.length];
                for (int i = 0; i < types.length; i++) {
                    parameterTypes[i] = loadClass(types[i]);
                }
                returnType = loadClass(methodInfo.getReturnType());
            }

            InputStream in = null;
            try {
                OutputStream out = (OutputStream)stub._request(operation, true);

                for (int i = 0; i < parameterTypes.length; i++) {
                    // Object arg = (args.length < i) ? null : args[i];
                    writeValue(out, args[i], parameterTypes[i]);
                }
                if (returnType == void.class) {
                    // void return
                    stub._invoke(out);
                    return null;
                } else {
                    // read the return value
                    in = (InputStream)stub._invoke(out);
                    Object response = readValue(in, returnType);
                    return response;
                }

            } catch (ApplicationException ex) {
                in = (InputStream)ex.getInputStream();
                try {
                    org.apache.tuscany.sca.binding.ejb.corba.Java2IDLUtil.throwException(methodInfo.getMethod(), in);
                    return null;
                } catch (Throwable e) {
                    throw new RemoteException(e.getMessage(), e);
                }
            } catch (RemarshalException ex) {
                return invokeRemoteCORBACall(stub, methodName, args);
            } finally {
                stub._releaseReply(in);
            }
        } catch (SystemException ex) {
            throw Util.mapSystemException(ex);
        }
    }

    /**
     * @param out
     * @param value
     * @param type
     */
    private void writeValue(OutputStream out, Object value, Class type) {
        org.apache.tuscany.sca.binding.ejb.corba.Java2IDLUtil.writeObject(type, value, out);
    }

    /**
     * @param in
     * @param type
     * @return
     */
    private Object readValue(InputStream in, Class type) {
        return org.apache.tuscany.sca.binding.ejb.corba.Java2IDLUtil.readObject(type, in);
    }
}
