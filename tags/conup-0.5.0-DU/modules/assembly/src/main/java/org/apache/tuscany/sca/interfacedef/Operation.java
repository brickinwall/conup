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
package org.apache.tuscany.sca.interfacedef;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.interfacedef.util.WrapperInfo;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.apache.tuscany.sca.policy.PolicySubject;

/**
 * Represents an operation on a service interface.
 *
 * @version $Rev: 1213702 $ $Date: 2011-12-13 14:12:38 +0000 (Tue, 13 Dec 2011) $
 * @tuscany.spi.extension.inheritfrom
 */
public interface Operation extends Cloneable, PolicySubject {
    /**
     * Returns the name of the operation.
     *
     * @return the name of the operation
     */
    String getName();

    /**
     * Sets the name of the operation.
     *
     * @param name the name of the operation
     */
    void setName(String name);

    /**
     * Returns true if the model element is unresolved.
     *
     * @return true if the model element is unresolved.
     */
    boolean isUnresolved();

    /**
     * Sets whether the model element is unresolved.
     *
     * @param unresolved whether the model element is unresolved
     */
    void setUnresolved(boolean unresolved);

    /**
     * Get the data type that represents the input of this operation. The logic
     * type is a list of data types and each element represents a parameter
     *
     * @return the inputType
     */
    DataType<List<DataType>> getInputType();

    /**
     * @param inputType
     */
    void setInputType(DataType<List<DataType>> inputType);

    /**
     * Get the data type for the output
     *
     * @return the outputType
     */  
    DataType<List<DataType>> getOutputType();
    /**
     * @param outputType
     */
    void setOutputType(DataType<List<DataType>> outputType);

    /**
     * Get a list of data types to represent the faults/exceptions
     *
     * @return the faultTypes
     */
    List<DataType> getFaultTypes();

    /**
     * @param faultTypes
     */
    void setFaultTypes(List<DataType> faultTypes);

    /**
     * Get the owning interface
     * @return
     */
    Interface getInterface();

    /**
     * Set the owning interface
     * @param interfaze
     */
    void setInterface(Interface interfaze);

    /**
     * Indicate if the operation is non-blocking
     * @return
     */
    boolean isNonBlocking();
    
    /**
     * Indicate if the operation is an async server operation
     * @return - true if the operation is an async server operation
     */
    boolean isAsyncServer();

    /**
     * Set the operation to be non-blocking
     */
    void setNonBlocking(boolean nonBlocking);

    /**
     * @return the wrapperInfo
     */
    WrapperInfo getInputWrapper();

    /**
     * @param wrapperInfo the wrapperInfo to set
     */
    void setInputWrapper(WrapperInfo wrapperInfo);
    
    /**
     * @return the wrapperInfo
     */
    WrapperInfo getOutputWrapper();

    /**
     * @param wrapperInfo the wrapperInfo to set
     */
    void setOutputWrapper(WrapperInfo wrapperInfo);    

    /**
     * @return the wrapperStyle
     */
    boolean isInputWrapperStyle();

    /**
     * @param wrapperStyle the wrapperStyle to set
     */
    void setInputWrapperStyle(boolean wrapperStyle);

    /**
     * @return the wrapperStyle
     */
    boolean isOutputWrapperStyle();

    /**
     * @param wrapperStyle the wrapperStyle to set
     */
    void setOutputWrapperStyle(boolean wrapperStyle);
    
    /**
     * @deprecated This should be the WrapperInfo.getDataBinding()
     * Get the databinding for the operation
     * @return
     */
    @Deprecated
    String getDataBinding();

    /**
     * @deprecated This should be the WrapperInfo.setDataBinding()
     * Set the databinding for the operation
     * @param dataBinding
     */
    @Deprecated
    void setDataBinding(String dataBinding);

    /**
     * Returns true if the operation is dynamic.
     *
     * @return true if the operation is dynamic otherwise false
     */
    boolean isDynamic();

    /**
     * Set if the operation is dynamic
     * @param b
     */
    void setDynamic(boolean b);

    /**
     * Get the synthesized fault beans for this operation
     *
     * @return the fault beans
     */
    Map<QName, List<DataType<XMLType>>> getFaultBeans();

    /**
     * Set the synthesized fault beans for this operation
     * @param faultBeans
     */
    void setFaultBeans(Map<QName, List<DataType<XMLType>>> faultBeans);

    /**
     * Get a map of attributes assoicated with the operation
     * @return A map of attributes
     */
    Map<Object, Object> getAttributes();

    /**
     * Implementations must support cloning.
     */
    Object clone() throws CloneNotSupportedException;

    /**
     * Returns the ParameterModes
     * @return
     */
    List<ParameterMode> getParameterModes();
    
    /**
     * Returns whether the operation's outputs will flow wrapped in an array
     * or not.  (Needed to distinguish whether an array represents a single output 
     * or if it wrappers multiple outputs).
     * 
     * @return
     */
    public boolean hasArrayWrappedOutput();

    /**
     * Sets whether the operation's outputs will flow wrapped in an array
     * or not.  (Needed to distinguish whether an array represents a single output 
     * or if it wrappers multiple outputs).
     * @param value
     */
    public void setHasArrayWrappedOutput(boolean value);

    /**
     * Sets whether operation data is not subject to wrapping along with
     * a data transformation.
     * @param notSubjectToWrapping
     */  
	public void setNotSubjectToWrapping(boolean notSubjectToWrapping);

    /**
     * Returns whether operation data is not subject to wrapping along with
     * a data transformation.
     * @return
     */
	public boolean isNotSubjectToWrapping();
    
    /**
     * A special databinding for input message of an operation
     */
    String IDL_INPUT = "idl:input";
    /**
     * A special databinding for output message of an operation
     */
    String IDL_OUTPUT = "idl:output";
    /**
     * A special databinding for fault message of an operation
     */
    String IDL_FAULT = "idl:fault";
}
