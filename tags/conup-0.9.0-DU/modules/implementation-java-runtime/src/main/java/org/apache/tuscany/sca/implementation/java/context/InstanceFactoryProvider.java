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

import org.apache.tuscany.sca.core.factory.ObjectFactory;
import org.apache.tuscany.sca.implementation.java.JavaElementImpl;

/**
 * @version $Rev: 697649 $ $Date: 2008-09-22 01:58:11 +0100 (Mon, 22 Sep 2008) $
 */
public interface InstanceFactoryProvider<T> {
    /**
     * Return the implementation class.
     *
     * @return the implementation class.
     */
    Class<T> getImplementationClass();

    /**
     * Sets an object factory for an injection site
     *
     * @param element          the injection site name
     * @param objectFactory the object factory
     */
    void setObjectFactory(JavaElementImpl element, ObjectFactory<?> objectFactory);

    /**
     * Create an instance factory that can be used to create component instances.
     *
     * @return a new instance factory
     */
    InstanceFactory<T> createFactory();
}
