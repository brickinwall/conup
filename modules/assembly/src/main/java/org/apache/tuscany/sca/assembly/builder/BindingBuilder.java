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

package org.apache.tuscany.sca.assembly.builder;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Contract;

/**
 * A builder that handles any build-time configuration needed by bindings.
 *
 * @version $Rev: 955601 $ $Date: 2010-06-17 14:55:03 +0100 (Thu, 17 Jun 2010) $
 * @tuscany.spi.extension.inheritfrom
 */
public interface BindingBuilder<B extends Binding> {

    /**
     * Configure a binding.
     * 
     * @param component The component for the binding's service or reference
     * @param contract The binding's service or reference
     * @param context The context for the builder
     * @param rebuild Set true to have derived data in the binding rebuilt
     *                caching can cause problems in the late binding case so we
     *                need to be able to remove cached data
     */
    void build(Component component, Contract contract, B binding, BuilderContext context, boolean rebuild);

    /**
     * Get QName of the binding type
     * @return The binding type
     */
    QName getBindingType();

}
