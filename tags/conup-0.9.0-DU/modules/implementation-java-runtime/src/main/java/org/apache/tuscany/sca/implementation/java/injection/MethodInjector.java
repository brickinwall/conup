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
package org.apache.tuscany.sca.implementation.java.injection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.tuscany.sca.core.factory.ObjectCreationException;
import org.apache.tuscany.sca.core.factory.ObjectFactory;

/**
 * Injects a value created by an {@link org.apache.tuscany.sca.core.factory.ObjectFactory} using a given method
 *
 * @version $Rev: 986676 $ $Date: 2010-08-18 14:52:28 +0100 (Wed, 18 Aug 2010) $
 */
public class MethodInjector<T> implements Injector<T> {
    private final Method method;
    private final ObjectFactory<?> objectFactory;

    public MethodInjector(Method aMethod, ObjectFactory<?> objectFactory) {
        assert aMethod != null;
        assert objectFactory != null;
        this.method = aMethod;
        // Allow privileged access to set accessibility. Requires ReflectPermission in security
        // policy.
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                method.setAccessible(true);
                return null;
            }
        });           
        this.objectFactory = objectFactory;
    }

    public void inject(T instance) throws ObjectCreationException {
    	inject(instance, objectFactory.getInstance());
    }
    
    private void inject(T instance, Object value) {
        try {
            method.invoke(instance, value);
        } catch (IllegalAccessException e) {
            throw new ObjectCreationException("Method is not accessible [" + method + "]", e);
        } catch (IllegalArgumentException e) {
            throw new ObjectCreationException("Exception thrown by setter: " + method.getName(), e);
        } catch (InvocationTargetException e) {
            throw new ObjectCreationException("Exception thrown by setter: " + method.getName(), e);
        }
    }

	public Class<?> getType() {
		return method.getParameterTypes()[0];
	}
	
	public Type getGenericType() {
		return method.getGenericParameterTypes()[0];
	}

	public void injectNull(T instance) throws ObjectCreationException {
		inject(instance, null);		
	}

	
}
