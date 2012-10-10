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

package org.apache.tuscany.sca.implementation.java.invocation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.assembly.Contract;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.core.invocation.CallbackInterfaceInterceptor;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.invocation.InvocationChain;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;
import org.apache.tuscany.sca.runtime.RuntimeWireProcessor;

/**
 * @version $Rev: 912034 $ $Date: 2010-02-19 23:38:00 +0000 (Fri, 19 Feb 2010) $
 */
public class JavaCallbackRuntimeWireProcessor implements RuntimeWireProcessor {
    private static final Logger logger = Logger.getLogger(JavaCallbackRuntimeWireProcessor.class.getName());
    private InterfaceContractMapper interfaceContractMapper;
    private JavaInterfaceFactory javaInterfaceFactory;

    public JavaCallbackRuntimeWireProcessor(ExtensionPointRegistry registry) {
        FactoryExtensionPoint factories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        this.javaInterfaceFactory = factories.getFactory(JavaInterfaceFactory.class);

        UtilityExtensionPoint utilities = registry.getExtensionPoint(UtilityExtensionPoint.class);
        this.interfaceContractMapper = utilities.getUtility(InterfaceContractMapper.class);
    }
    
    /**
     * @param interfaceContractMapper
     * @param javaInterfaceFactory
     */
    protected JavaCallbackRuntimeWireProcessor(InterfaceContractMapper interfaceContractMapper,
                                            JavaInterfaceFactory javaInterfaceFactory) {
        super();
        this.interfaceContractMapper = interfaceContractMapper;
        this.javaInterfaceFactory = javaInterfaceFactory;
    }


    private boolean supportsCallbackInterface(Interface iface, JavaImplementation impl) {
        if (iface instanceof JavaInterface) {
            Class<?> ifaceClass = ((JavaInterface)iface).getJavaClass();
            if (ifaceClass.isAssignableFrom(impl.getJavaClass())) {
                return true;
            }
        }
        try {
            Interface implType = javaInterfaceFactory.createJavaInterface(impl.getJavaClass());
            // Ignore the remotable/conversational testing
            implType.setRemotable(iface.isRemotable());
            return interfaceContractMapper.isCompatibleSubset(iface, implType);
        } catch (InvalidInterfaceException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
    }

    public void process(RuntimeEndpoint endpoint) {
        // No operation
    }

    public void process(RuntimeEndpointReference endpointReference) {
        if(!(endpointReference instanceof RuntimeEndpointReference)) {
            return;
        }
        RuntimeEndpointReference epr = (RuntimeEndpointReference) endpointReference;
        Contract contract = epr.getReference();
        if (!(contract instanceof RuntimeComponentReference)) {
            return;
        }
        RuntimeComponent component = (RuntimeComponent) epr.getComponent();
        if (component == null) {
            return;
        }
        Implementation implementation = component.getImplementation();
        if (!(implementation instanceof JavaImplementation)) {
            return;
        }
        JavaImplementation javaImpl = (JavaImplementation)implementation;
        Endpoint callbackEndpoint = epr.getCallbackEndpoint();
        if (callbackEndpoint != null) {
            Interface iface = callbackEndpoint.getService().getInterfaceContract().getInterface();
            if (!supportsCallbackInterface(iface, javaImpl)) {
                // callback to this impl is not possible, so ensure a callback object is set
                for (InvocationChain chain : epr.getInvocationChains()) {
                    chain.addInterceptor(Phase.REFERENCE, new CallbackInterfaceInterceptor());
                }
            }
        }
    }
}
