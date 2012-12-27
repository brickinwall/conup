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
 * DataBinding represents a data representation, for example, SDO, JAXB and AXIOM
 *
 * @version $Rev: 1061329 $ $Date: 2011-01-20 14:57:06 +0000 (Thu, 20 Jan 2011) $
 * @tuscany.spi.extension.asclient
 */
public interface DataBinding {

    /**
     * The name of a databinding should be case-insensitive and unique
     * 
     * @return The name of the databinding
     */
    String getName();

    /**
     * Introspect and populate information to a DataType model
     * 
     * @param dataType The data type to be introspected
     * @param operation The operation
     * @return true if the databinding has recognized the given data type
     */
    boolean introspect(DataType dataType, Operation operation);

    /**
     * Introspect the data to figure out the corresponding data type
     * 
     * @param value The object to be checked
     * @param operation The operation
     * @return The DataType or null if the java type is not supported by this databinding
     */
    DataType introspect(Object value, Operation operation);

    /**
     * Provide a WrapperHandler for this databinding
     * @return A wrapper handler which can handle wrapping/wrapping for this databinding
     */
    WrapperHandler getWrapperHandler();

    /**
     * Make a copy of the object for "pass-by-value" semantics and cross classloader invocations.
     *
     * @param object source object to copy 
     * @param sourceDataType The source data type
     * @param targetDataType the target data type
     * @param sourceOperation The source operation
     * @param targetOperation The target operation
     * @return copy of the object passed in as argument
     */
    Object copy(Object object, DataType sourceDataType, DataType targetDataType, Operation sourceOperation, Operation targetOperation);

    /**
     * Get the XML type helper for Java types
     * @return The databinding-specific XML type helper class
     */
    XMLTypeHelper getXMLTypeHelper();

}
