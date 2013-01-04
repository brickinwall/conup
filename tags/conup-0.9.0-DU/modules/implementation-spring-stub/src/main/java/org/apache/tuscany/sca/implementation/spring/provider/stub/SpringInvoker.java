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
package org.apache.tuscany.sca.implementation.spring.provider.stub;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.tuscany.sca.implementation.spring.SpringBeanElement;
import org.apache.tuscany.sca.implementation.spring.SpringImplementation;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.impl.JavaInterfaceUtil;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;

/**
 * Initial implementation of a Spring bean invoker
 * @version $Rev: 987670 $ $Date: 2010-08-21 00:42:07 +0100 (Sat, 21 Aug 2010) $ 
 */
public class SpringInvoker implements Invoker {

    private Method theMethod = null;
    private Object bean;
    private SpringBeanElement beanElement;
    private boolean badInvoker = false;

    private SpringContextStub springContext;
    private Operation operation;

    /**
     * SpringInvoker constructor
     * @param component - the Spring component to invoke
     * @param service - the service to invoke
     * @param operation - the operation to invoke
     */
    public SpringInvoker(RuntimeComponent component,
                         SpringContextStub springContext,
                         RuntimeComponentService service,
                         Operation operation) {

        this.springContext = springContext;
        this.operation = operation;

        // From the component and the service, identify the Spring Bean which is the target
        SpringImplementation theImplementation = (SpringImplementation)component.getImplementation();
        beanElement = theImplementation.getBeanFromService(service.getService());

        if (beanElement == null) {
            badInvoker = true;
            return;
        }

    } // end constructor SpringInvoker

    // Lazy-load the method to avoid timing problems with the Spring Context
    private void setupMethod() throws SpringInvocationException {
        try {
            bean = springContext.getBean(beanElement.getId());
            Class<?> beanClass = bean.getClass();
            theMethod = JavaInterfaceUtil.findMethod(beanClass, operation);
            //System.out.println("SpringInvoker - found method " + theMethod.getName() );
        } catch (NoSuchMethodException e) {
            throw new SpringInvocationException(e);
        }
    }

    private Object doInvoke(Object payload) throws SpringInvocationException {
        if (theMethod == null)
            setupMethod();

        if (badInvoker)
            throw new SpringInvocationException("Spring invoker incorrectly configured");
        // Invoke the method on the Spring bean using the payload, returning the results
        try {
            Object ret;

            if (payload != null && !payload.getClass().isArray()) {
                ret = theMethod.invoke(bean, payload);
            } else {
                ret = theMethod.invoke(bean, (Object[])payload);
            }
            return ret;
        } catch (InvocationTargetException e) {
            throw new SpringInvocationException("Spring invoker invoke method '" + theMethod.getName() + "' error.",
                                                e.getCause());
        } catch (Exception e) {
            throw new SpringInvocationException("Spring invoker invoke method '" + theMethod.getName() + "' error.", e);
        }

    } // end method doInvoke

    /**
     * @param msg the message to invoke on the target bean
     */
    public Message invoke(Message msg) {
        try {
            Object resp = doInvoke(msg.getBody());
            msg.setBody(resp);
        } catch (SpringInvocationException e) {
            msg.setFaultBody(e.getCause());
        } catch (Throwable e) {
            msg.setFaultBody(e);
        }
        //System.out.println("Spring Invoker - invoke called");
        return msg;
    } // end method invoke

} // end class SpringInvoker
