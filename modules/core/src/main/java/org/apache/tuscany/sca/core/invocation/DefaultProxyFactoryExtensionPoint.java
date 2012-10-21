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

package org.apache.tuscany.sca.core.invocation;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.LifeCycleListener;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.core.invocation.impl.JDKProxyFactory;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.invocation.MessageFactory;

/**
 * Default implementation of a ProxyFactoryExtensionPoint.
 *
 * @version $Rev: 998309 $ $Date: 2010-09-17 21:37:49 +0100 (Fri, 17 Sep 2010) $
 */
public class DefaultProxyFactoryExtensionPoint implements ProxyFactoryExtensionPoint, LifeCycleListener {
    private InterfaceContractMapper interfaceContractMapper;
    private MessageFactory messageFactory;

    private ProxyFactory interfaceFactory;
    private ProxyFactory classFactory;

    public DefaultProxyFactoryExtensionPoint(ExtensionPointRegistry registry) {
        UtilityExtensionPoint utilities = registry.getExtensionPoint(UtilityExtensionPoint.class);
        this.interfaceContractMapper = utilities.getUtility(InterfaceContractMapper.class);

        FactoryExtensionPoint modelFactories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        this.messageFactory = modelFactories.getFactory(MessageFactory.class);

        interfaceFactory = new JDKProxyFactory(registry, messageFactory, interfaceContractMapper);
    }

    //    public DefaultProxyFactoryExtensionPoint(MessageFactory messageFactory, InterfaceContractMapper mapper) {
    //        this.interfaceContractMapper = mapper;
    //        this.messageFactory = messageFactory;
    //        interfaceFactory = new JDKProxyFactory(null, messageFactory, mapper);
    //    }

    public ProxyFactory getClassProxyFactory() {
        return classFactory;
    }

    public ProxyFactory getInterfaceProxyFactory() {
        return interfaceFactory;
    }

    public void setClassProxyFactory(ProxyFactory factory) {
        this.classFactory = factory;

    }

    public void setInterfaceProxyFactory(ProxyFactory factory) {
        this.interfaceFactory = factory;

    }

    public void start() {
        if (interfaceFactory instanceof LifeCycleListener) {
            ((LifeCycleListener)interfaceFactory).start();
        }
        if (classFactory instanceof LifeCycleListener) {
            ((LifeCycleListener)classFactory).start();
        }
    }

    public void stop() {
        if (interfaceFactory instanceof LifeCycleListener) {
            ((LifeCycleListener)interfaceFactory).stop();
        }
        if (classFactory instanceof LifeCycleListener) {
            ((LifeCycleListener)classFactory).stop();
        }

    }

}
