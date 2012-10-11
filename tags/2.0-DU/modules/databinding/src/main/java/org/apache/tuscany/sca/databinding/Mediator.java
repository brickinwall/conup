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

import java.util.Map;

import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;

/**
 * This interface will be used as a Tuscany system service to perform data mediations
 *
 * Mediate the data from one type to the other one
 *
 * @version $Rev: 1058748 $ $Date: 2011-01-13 20:54:22 +0000 (Thu, 13 Jan 2011) $
 * @tuscany.spi.extension.asclient
 */
public interface Mediator {

	// Constants used by the databinding framework's TransformationContext metadata
    public final static String SOURCE_OPERATION = "source.operation";
    public final static String TARGET_OPERATION = "target.operation";

    // Use String constants rather than an enum, in case the dependency isn't factored 
    // out well enough and someone needs to use the String values. 
    public static final String BODY_TYPE = "body.type";
    public static final String BODY_TYPE_INPUT = "input";
    public static final String BODY_TYPE_OUTPUT = "output";
    public static final String BODY_TYPE_FAULT = "fault";
    
    /**
     * Mediate the data from the source type to the target type
     * @param source The data to be mediated
     * @param sourceDataType Data type for the source data
     * @param targetDataType Data type for the target data
     * @param context
     * @return
     */
    Object mediate(Object source, DataType sourceDataType, DataType targetDataType, Map<String, Object> context);

    /**
     * Mediate the source data into the target which is a sink to receive the data
     * @param source The data to be mediated
     * @param target The sink to receive data
     * @param sourceDataType Data type for the source data
     * @param targetDataType Data type for the target data
     */
    void mediate(Object source,
                 Object target,
                 DataType sourceDataType,
                 DataType targetDataType,
                 Map<String, Object> context);

    /**
     * Transform the input parameters for the source operation to the expected parameters for
     * the target operation
     * @param input The input data, typically an array of parameters
     * @param sourceOperation The source operation
     * @param targetOperation The target operation
     * @param metadata Additional metadata
     * @return The transformed input data for the target operation
     */
    Object mediateInput(Object input, Operation sourceOperation, Operation targetOperation, Map<String, Object> metadata);

    /**
     * Transform the return value for the target operation to the expected return value for
     * the source operation
     * @param output The output data, typically the return value
     * @param sourceOperation The source operation
     * @param targetOperation The target operation
     * @param metadata Additional metadata
     * @return The transformed output data for the source operation
     */
    Object mediateOutput(Object output,
                         Operation sourceOperation,
                         Operation targetOperation,
                         Map<String, Object> metadata);

    /**
     * Transform the fault data for the target operation to the expected fault data for
     * the source operation
     * @param fault The fault data, such as Java exception or fault message
     * @param sourceOperation The source operation
     * @param targetOperation The target operation
     * @param metadata Additional metadata
     * @return The transformed fault data for the source operation
     */
    Object mediateFault(Object fault, Operation sourceOperation, Operation targetOperation, Map<String, Object> metadata);

    /**
     * Copy the data
     * @param data The orginal data
     * @param dataType The data type
     * @return The copy
     */
    Object copy(Object data, DataType dataType);

    /**
     * Copy the data
     * @param data The orginal data
     * @param dataType The source data type
     * @param targetDataType The target data type
     * @return The copy
     */
    Object copy(Object data, DataType sourceDataType, DataType targetDataType);

    /**
     * Copy the data
     * @param data The orginal data
     * @param sourceDataType The source data type
     * @param targetDataType The target data type
     * @param sourceOperation The source operation
     * @param targetOperation The target operation
     * @return The copy
     */
    Object copy(Object data,
                DataType sourceDataType,
                DataType targetDataType,
                Operation sourceOperation,
                Operation targetOperation);
    
    /**
     * Copy an array of data objects passed to an operation
     * @param data array of objects to copy
     * @return the copy
     */
    public Object copyInput(Object input, Operation operation);

    /**
     * Copy an array of data objects passed to an operation
     * @param sourceOperation The source operation
     * @param targetOperation The target operation
     * @return the copy
     */
    public Object copyInput(Object input, Operation sourceOperation, Operation targetOperation);

    /**
     * Copy the output data
     * @param data The orginal output
     * @param operation The operation
     * @return The copy
     */
    Object copyOutput(Object data, Operation operation);

    /**
     * Copy the output data from target operation into source operation
     * @param data The orginal output
     * @param sourceOperation The operation
     * @param targetOperation The target operation
     * @return The copy
     */
    Object copyOutput(Object data, Operation sourceOperation, Operation targetOperation);

    /**
     * Copy the fault data
     * @param fault The orginal fault data
     * @param operation The operation
     * @return The copy
     */
    Object copyFault(Object fault, Operation operation);

    /**
     * Copy the fault data from target operation into source operation
     * @param fault The orginal fault data
     * @param sourceOperation The operation
     * @param targetOperation The target operation
     * @return The copy
     */
    Object copyFault(Object fault, Operation sourceOperation, Operation targetOperation);

    /**
     * Get the DataBindings used by this mediator.
     * @return
     */
    DataBindingExtensionPoint getDataBindings();

    /**
     * Get the Transformers used by this mediator.
     * @return
     */
    TransformerExtensionPoint getTransformers();

    /**
     * Create an instance of TransformationContext
     * @return
     */
    TransformationContext createTransformationContext();

    /**
     * Create an instance of TransformationContext
     * @param sourceDataType
     * @param targetDataType
     * @param metadata
     * @return
     */
    TransformationContext createTransformationContext(DataType sourceDataType,
                                                      DataType targetDataType,
                                                      Map<String, Object> metadata);
    
}
