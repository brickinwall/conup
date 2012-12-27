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
package org.apache.tuscany.sca.databinding;


import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;

/**
 * An extension point for data binding extensions.
 *
 * @version $Rev: 937310 $ $Date: 2010-04-23 15:27:50 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface DataBindingExtensionPoint {

    /**
     * Register a data binding
     * 
     * @param dataBinding
     */
    void addDataBinding(DataBinding dataBinding);

    /**
     * Look up a data binding by id
     * 
     * @param id The name of the databinding
     * @return The databinding
     */
    DataBinding getDataBinding(String id);

    /**
     * Unregister a data binding
     * 
     * @param id
     * @return The unregistered databinding
     */
    DataBinding removeDataBinding(String id);

    /**
     * Introspect the java class to figure out what DataType supports it.
     * 
     * @param dataType The initial data type
     * @param operation TODO
     * @return A DataType representing the java type or null if no databinding
     *         recognizes the java type
     */
    boolean introspectType(DataType dataType, Operation operation);

    /**
     * Introspect the value to figure out the corresponding DataType
     * 
     * @param value The object value
     * @param operation TODO
     * @return A DataType representing the value or null if no databinding
     *         recognizes the value
     */
    DataType introspectType(Object value, Operation operation);
}
