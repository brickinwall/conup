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

import java.lang.reflect.Type;

import org.apache.tuscany.sca.core.factory.ObjectCreationException;

/**
 * Implementations inject a pre-configured value on an instance
 *
 * @version $Rev: 986676 $ $Date: 2010-08-18 14:52:28 +0100 (Wed, 18 Aug 2010) $
 */
public interface Injector<T> {

    /**
     * Inject a value on the given instance
     */
    void inject(T instance) throws ObjectCreationException;
    void injectNull(T instance) throws ObjectCreationException;
    Class<?> getType();
	Type getGenericType();

}
