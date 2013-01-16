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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.tuscany.sca.core.factory.ObjectCreationException;
import org.apache.tuscany.sca.core.factory.ObjectFactory;

/**
 * Injects a value created by an {@link org.apache.tuscany.sca.core.factory.ObjectFactory} on a given field
 *
 * @version $Rev: 986676 $ $Date: 2010-08-18 14:52:28 +0100 (Wed, 18 Aug 2010) $
 */
public class FieldInjector<T> implements Injector<T> {
//	  modified for conup
//    private final Field field;
//	  private final ObjectFactory<?> objectFactory;
	private Field field;

	private ObjectFactory<?> objectFactory;

    /**
     * Create an injector and have it use the given <code>ObjectFactory</code> to inject a value on the instance using
     * the reflected <code>Field</code>
     */
    public FieldInjector(Field pField, ObjectFactory<?> objectFactory) {
        field = pField;
        // Allow privileged access to set accessibility. Requires ReflectPermission
        // in security policy.
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                field.setAccessible(true); // ignore Java accessibility
                return null;
            }
        });
        
        this.objectFactory = objectFactory;
    }

    /**
     * Inject a new value on the given instance
     */
    public void inject(T instance) throws ObjectCreationException {
    	inject(instance, objectFactory.getInstance());
    }
    
    public void inject(T instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new ObjectCreationException("Field is not accessible [" + field + "]", e);
        }
    }
    
    public Class<?> getType() {
    	return field.getType();
    }
    
	public Type getGenericType() {
		return field.getGenericType();
	}

	public void injectNull(T instance) throws ObjectCreationException {
		inject(instance, null);		
	}
	
	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}
	
	public void setObjectFactory(ObjectFactory<?> objectFactory) {
		this.objectFactory = objectFactory;
	}

}
