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
package org.apache.tuscany.sca.interfacedef.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.ParameterMode;
import org.apache.tuscany.sca.interfacedef.util.WrapperInfo;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySet;

/**
 * Represents an operation on a service interface.
 *
 * @version $Rev: 1213702 $ $Date: 2011-12-13 14:12:38 +0000 (Tue, 13 Dec 2011) $
 * @tuscany.spi.extension.inheritfrom
 */
public class OperationImpl implements Operation {

    private String name;
    private boolean unresolved;    
    private DataType<List<DataType>> inputType;
    private List<DataType> faultTypes;
    private Interface interfaze;
    private List<ParameterMode> parameterModes = new ArrayList<ParameterMode>();
    private boolean nonBlocking;
    private boolean inputWrapperStyle;
    private boolean outputWrapperStyle;
    private WrapperInfo inputWrapper;
    private WrapperInfo outputWrapper;
    private boolean dynamic;
    private boolean notSubjectToWrapping;

    private Map<Object, Object> attributes = new ConcurrentHashMap<Object, Object>();

    private Map<QName, List<DataType<XMLType>>> faultBeans;

    private List<PolicySet> applicablePolicySets = new ArrayList<PolicySet>();
    private List<PolicySet> policySets = new ArrayList<PolicySet>();
    private List<Intent> requiredIntents = new ArrayList<Intent>();
    private ExtensionType type;
    private DataType<List<DataType>> outputType;
    private boolean hasArrayWrappedOutput;

    /**
     * @param name
     */
    public OperationImpl() {
        inputType = new DataTypeImpl<List<DataType>>(IDL_INPUT, Object[].class, new ArrayList<DataType>());
        outputType = new DataTypeImpl<List<DataType>>(IDL_OUTPUT, Object[].class, new ArrayList<DataType>());
        faultTypes = new ArrayList<DataType>();
        faultBeans = new HashMap<QName, List<DataType<XMLType>>>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean undefined) {
        this.unresolved = undefined;
    }

    /**
     * @return the faultTypes
     */
    public List<DataType> getFaultTypes() {
        return faultTypes;
    }

    /**
     * @param faultTypes the faultTypes to set
     */
    public void setFaultTypes(List<DataType> faultTypes) {
        this.faultTypes = faultTypes;
    }

    /**
     * @return the inputType
     */
    public DataType<List<DataType>> getInputType() {
        return inputType;
    }

    /**
     * @param inputType the inputType to set
     */
    public void setInputType(DataType<List<DataType>> inputType) {
        this.inputType = inputType;
    }

    /**
     * @return the outputType
     */
    public DataType<List<DataType>> getOutputType() {
        return this.outputType;
    }     


    /**
     * @param outputType the outputType to set
     */
    public void setOutputType(DataType<List<DataType>> outputType) {
        this.outputType = outputType;
    }

    /**
     * @return the interface
     */
    public Interface getInterface() {
        return interfaze;
    }

    /**
     * @param interfaze the interface to set
     */
    public void setInterface(Interface interfaze) {
        this.interfaze = interfaze;
    }

    /**
     * @return the nonBlocking
     */
    public boolean isNonBlocking() {
        return nonBlocking;
    }

    /**
     * @param nonBlocking the nonBlocking to set
     */
    public void setNonBlocking(boolean nonBlocking) {
        this.nonBlocking = nonBlocking;
    }

    /**
     * @return the wrapperInfo
     */
    public WrapperInfo getInputWrapper() {
        return inputWrapper;
    }

    /**
     * @param wrapperInfo the wrapperInfo to set
     */
    public void setInputWrapper(WrapperInfo wrapperInfo) {
        this.inputWrapper = wrapperInfo;
    }
    
    /**
     * @return the wrapperInfo
     */
    public WrapperInfo getOutputWrapper() {
        return outputWrapper;
    }

    /**
     * @param wrapperInfo the wrapperInfo to set
     */
    public void setOutputWrapper(WrapperInfo wrapperInfo) {
        this.outputWrapper = wrapperInfo;
    }    

    /**
     * @return the wrapperStyle
     */
    public boolean isInputWrapperStyle() {
        return inputWrapperStyle;
    }

    /**
     * @param wrapperStyle the wrapperStyle to set
     */
    public void setInputWrapperStyle(boolean wrapperStyle) {
        this.inputWrapperStyle = wrapperStyle;
    }
    
    /**
     * @return the wrapperStyle
     */
    public boolean isOutputWrapperStyle() {
        return outputWrapperStyle;
    }

    /**
     * @param wrapperStyle the wrapperStyle to set
     */
    public void setOutputWrapperStyle(boolean wrapperStyle) {
        this.outputWrapperStyle = wrapperStyle;
    }    

    public String getDataBinding() {
        if (inputWrapper != null){
            return inputWrapper.getDataBinding();
        }
        if (outputWrapper != null){
            return outputWrapper.getDataBinding();
        }
        return null;
    }

    public void setDataBinding(String dataBinding) {
        if (inputWrapper != null) {
            inputWrapper.setDataBinding(dataBinding);
        }
        if (outputWrapper != null) {
            outputWrapper.setDataBinding(dataBinding);
        }
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean b) {
        this.dynamic = b;
    }

    public Map<QName, List<DataType<XMLType>>> getFaultBeans() {
        return faultBeans;
    }

    public void setFaultBeans(Map<QName, List<DataType<XMLType>>> faultBeans) {
        this.faultBeans = faultBeans;
    }

    @Override
    public OperationImpl clone() throws CloneNotSupportedException {
        OperationImpl copy = (OperationImpl) super.clone();

        final List<DataType> clonedFaultTypes = new ArrayList<DataType>(this.faultTypes.size());
        for (DataType t : this.faultTypes) {
            clonedFaultTypes.add((DataType) t.clone());
        }
        copy.faultTypes = clonedFaultTypes;

        List<DataType> clonedLogicalTypes = new ArrayList<DataType>();
        for (DataType t : inputType.getLogical()) {
            DataType type = (DataType) t.clone();
            clonedLogicalTypes.add(type);
        }
        DataType<List<DataType>> clonedInputType =
            new DataTypeImpl<List<DataType>>(inputType.getPhysical(), clonedLogicalTypes);
        clonedInputType.setDataBinding(inputType.getDataBinding());
        copy.inputType = clonedInputType;

        if ( outputType != null ) {
            List<DataType> clonedLogicalOutputTypes = new ArrayList<DataType>();
            for ( DataType t : outputType.getLogical()) {
                if ( t == null ) {
                    clonedLogicalOutputTypes.add(null);
                } else {
                    DataType type = (DataType) t.clone();
                    clonedLogicalOutputTypes.add(type);
                }
            }
            DataType<List<DataType>> clonedOutputType = 
                new DataTypeImpl<List<DataType>>(outputType.getPhysical(), clonedLogicalOutputTypes);
            clonedOutputType.setDataBinding(outputType.getDataBinding());
            copy.outputType = clonedOutputType;    
        }

        copy.attributes = new ConcurrentHashMap<Object, Object>();
        copy.attributes.putAll(attributes);

        // [rfeng] We need to clone the wrapper as it holds the databinding information
        if (inputWrapper != null) {
            copy.inputWrapper = (WrapperInfo)inputWrapper.clone();
        }
        
        if (outputWrapper != null) {
            copy.outputWrapper = (WrapperInfo)outputWrapper.clone();
        }

        return copy;
    }

    public List<PolicySet> getApplicablePolicySets() {
        return applicablePolicySets;
    }

    public List<PolicySet> getPolicySets() {
        return policySets;
    }

    public List<Intent> getRequiredIntents() {
        return requiredIntents;
    }

    public ExtensionType getExtensionType() {
        return type;
    }

    public void setExtensionType(ExtensionType type) {
        this.type = type;
    }

    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    /**
     * Indicates if this operation is an async server style of operation
     * @return true if the operation is async server style
     */
    public boolean isAsyncServer() {
        return false;
    }

    public List<ParameterMode> getParameterModes() {
        return this.parameterModes;
    }

    public boolean hasArrayWrappedOutput() {
        return this.hasArrayWrappedOutput;
    }

    public void setHasArrayWrappedOutput(boolean value) {
        this.hasArrayWrappedOutput = value;
    }

	public void setNotSubjectToWrapping(boolean notSubjectToWrapping) {
		this.notSubjectToWrapping = notSubjectToWrapping;
	}

	public boolean isNotSubjectToWrapping() {
		return notSubjectToWrapping;
	}

}
