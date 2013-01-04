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
package org.apache.tuscany.sca.binding.ejb.provider;

import org.apache.tuscany.sca.binding.ejb.EJBBinding;
import org.apache.tuscany.sca.binding.ejb.util.EJBHandler;
import org.apache.tuscany.sca.binding.ejb.util.NamingEndpoint;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaOperation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;

/**
 * EJBTargetInvoker
 *
 * @version $Rev: 980450 $ $Date: 2010-07-29 15:04:40 +0100 (Thu, 29 Jul 2010) $
 */
public class EJBBindingInvoker implements Invoker {

    private Operation operation;
    private String location;
    private Class serviceInterface;

    public EJBBindingInvoker(EJBBinding ejbBinding, Class serviceInterface, Operation operation) {
        this.serviceInterface = serviceInterface;
        this.location = ejbBinding.getURI();
        this.operation = operation;
    }

    public Message invoke(Message msg) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(serviceInterface.getClassLoader());
            Object resp = doInvoke(msg.getBody());
            msg.setBody(resp);
        } catch (Throwable e) {
            e.printStackTrace();
            msg.setFaultBody(e);
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
        return msg;
    }

    /**
     * Invoke a EJB operation
     * 
     * @param payload
     * @return
     */
    public Object doInvoke(final Object payload) {

        // construct NamingendPoint
        NamingEndpoint endpoint = getNamingEndpoint();

        // lookup home and ejb stub
        EJBHandler ejbHandler = new EJBHandler(endpoint, serviceInterface);

        //
        // If we really couldn't have anything but a JavaOperation maybe we should
        // remove the if-block.  Assuming we had some other type of operation, if
        // that is possible, we might still need to map to a Java operation name,
        // (for example because the WSDL operation name might be set using a JSR-181
        // annotation to something other than the Java operation name.
        //
        // But for now we'll keep the else-block in here.
        //
        String methodName = null;
        if (operation instanceof JavaOperation) {
            JavaOperation javaOp = (JavaOperation) operation;
            methodName = javaOp.getJavaMethod().getName();
        } else {
            methodName = operation.getName();
        }

        // invoke business method on ejb
        Object response = ejbHandler.invoke(methodName, (Object[])payload);

        return response;
    }

    protected NamingEndpoint getNamingEndpoint() {
        return new NamingEndpoint(location);
    }
    
}
