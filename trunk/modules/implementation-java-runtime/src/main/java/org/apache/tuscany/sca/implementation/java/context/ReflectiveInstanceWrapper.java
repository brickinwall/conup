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
package org.apache.tuscany.sca.implementation.java.context;

import org.apache.tuscany.sca.core.factory.InstanceWrapper;
import org.apache.tuscany.sca.core.scope.TargetDestructionException;
import org.apache.tuscany.sca.core.scope.TargetInitializationException;
import org.apache.tuscany.sca.implementation.java.injection.Injector;
import org.apache.tuscany.sca.implementation.java.invocation.EventInvoker;

/**
 * @version $Rev: 986678 $ $Date: 2010-08-18 14:53:47 +0100 (Wed, 18 Aug 2010) $
 */
public class ReflectiveInstanceWrapper<T> implements InstanceWrapper<T> {
    private final EventInvoker<T> initInvoker;
    private final EventInvoker<T> destroyInvoker;
    private final T instance;
	private final Injector<T>[] callbackInjectors;

    public ReflectiveInstanceWrapper(T instance, EventInvoker<T> initInvoker, EventInvoker<T> destroyInvoker, Injector<T>[] callbackInjectors) {
        this.instance = instance;
        this.initInvoker = initInvoker;
        this.destroyInvoker = destroyInvoker;
        this.callbackInjectors = callbackInjectors;
    }
    
    public T getInstance() {
        return instance;
    }

    public void start() throws TargetInitializationException {
        if (initInvoker != null) {
            try {
                initInvoker.invokeEvent(instance);
            } catch (Exception e) {
                try {
                    stop();
                } catch (TargetDestructionException e1) {
                    throw new TargetInitializationException("TargetDestructionException while handling init exception", e);
                }
                throw new TargetInitializationException(e);
            }
        }
    }

    public void stop() throws TargetDestructionException {
        if (destroyInvoker != null) {
            destroyInvoker.invokeEvent(instance);
        }
    }

    public Injector<T>[] getCallbackInjectors() {
    	return this.callbackInjectors;
    }

}
