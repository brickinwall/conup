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

package org.apache.tuscany.sca.interfacedef.java.jaxws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.databinding.DataBindingExtensionPoint;
import org.apache.tuscany.sca.databinding.javabeans.JavaExceptionDataBinding;
import org.apache.tuscany.sca.databinding.jaxb.JAXBDataBinding;
import org.apache.tuscany.sca.databinding.jaxb.XMLAdapterExtensionPoint;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.FaultExceptionMapper;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.ParameterMode;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaOperation;
import org.apache.tuscany.sca.interfacedef.java.introspect.JavaInterfaceVisitor;
import org.apache.tuscany.sca.interfacedef.util.ElementInfo;
import org.apache.tuscany.sca.interfacedef.util.TypeInfo;
import org.apache.tuscany.sca.interfacedef.util.WrapperInfo;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLFactory;

/**
 * Introspect the java class/interface with JSR-181 and JAXWS annotations
 * 
 * @version $Rev: 1292896 $ $Date: 2012-02-23 18:58:30 +0000 (Thu, 23 Feb 2012) $
 */
public class JAXWSJavaInterfaceProcessor implements JavaInterfaceVisitor {
    private static final String JAXB_DATABINDING = JAXBDataBinding.NAME;
    private static final String GET = "get";
    private DataBindingExtensionPoint dataBindingExtensionPoint;
    private FaultExceptionMapper faultExceptionMapper;
    private XMLAdapterExtensionPoint xmlAdapterExtensionPoint;
    protected JavaInterfaceFactory javaInterfaceFactory;
    private WSDLFactory wsdlFactory;


    public JAXWSJavaInterfaceProcessor(ExtensionPointRegistry registry) {
        dataBindingExtensionPoint = registry.getExtensionPoint(DataBindingExtensionPoint.class);
        faultExceptionMapper = registry.getExtensionPoint(UtilityExtensionPoint.class).getUtility(FaultExceptionMapper.class);
        xmlAdapterExtensionPoint = registry.getExtensionPoint(XMLAdapterExtensionPoint.class);
        
        FactoryExtensionPoint factories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        this.javaInterfaceFactory = factories.getFactory(JavaInterfaceFactory.class);
        this.wsdlFactory = factories.getFactory(WSDLFactory.class);
    }

  
    public JAXWSJavaInterfaceProcessor() {
        super();
    }

    private ParameterMode getParameterMode(WebParam.Mode mode) {
    	if (mode == Mode.INOUT) {
    		return ParameterMode.INOUT;
    	} else if (mode == Mode.OUT) {
    		return ParameterMode.OUT;
    	} else {
    		return ParameterMode.IN;
    	}
    }
    
    private static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        } else {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
    }

    public void visitInterface(JavaInterface contract) throws InvalidInterfaceException {

        final Class<?> clazz = contract.getJavaClass();
        
        contract = JAXWSUtils.configureJavaInterface(contract, clazz);
        String tns = contract.getQName().getNamespaceURI();      
        
        // run this regardless in case the user overrides 
        // the remotable flag in the SCDL
        //if (!contract.isRemotable()) {
        //    return;
        //}

        // SOAP binding (doc/lit/wrapped|bare or rpc/lit)
        SOAPBinding soapBinding = clazz.getAnnotation(SOAPBinding.class);

        for (Iterator<Operation> it = contract.getOperations().iterator(); it.hasNext();) {
            final JavaOperation operation = (JavaOperation)it.next();
            final Method method = operation.getJavaMethod();
            introspectFaultTypes(operation);

            // SOAP binding (doc/lit/wrapped|bare or rpc/lit)
            SOAPBinding methodSOAPBinding = method.getAnnotation(SOAPBinding.class);
            if (methodSOAPBinding == null) {
                methodSOAPBinding = soapBinding;
            }

            boolean documentStyle = true;
            boolean bare = false;
            if (methodSOAPBinding != null) {
                bare = methodSOAPBinding.parameterStyle() == SOAPBinding.ParameterStyle.BARE;
                if(bare) {
                    // For BARE parameter style, the data won't be unwrapped
                    // The wrapper should be null
                    operation.setInputWrapperStyle(false);
                    operation.setOutputWrapperStyle(false);
                }
                documentStyle = methodSOAPBinding.style() == Style.DOCUMENT;
            }

            String operationName = operation.getName();
            // WebMethod
            WebMethod webMethod = method.getAnnotation(WebMethod.class);
            if (webMethod != null) {
                if (webMethod.exclude()) {
                    // Exclude the method
                    it.remove();
                    continue;
                }
                operationName = getValue(webMethod.operationName(), operationName);
                operation.setName(operationName);
                operation.setAction(webMethod.action());
            }

            // Is one way?
            Oneway oneway = method.getAnnotation(Oneway.class);
            if (oneway != null) {
                // JSR 181
                assert method.getReturnType() == void.class;
                operation.setNonBlocking(true);
            }

            // Handle BARE mapping
            if (bare) {
                for (int i = 0; i < method.getParameterTypes().length; i++) {
                    String ns = tns;
                    // Default to <operationName> for doc-bare
                    String name = (documentStyle ? operationName : "arg" + i);
                    WebParam param = getAnnotation(method, i, WebParam.class);
                    if (param != null) {
                        if (!"".equals(param.targetNamespace()))
                            ns = param.targetNamespace();
                        if (!"".equals(param.name()))
                            name = param.name();
                        operation.getParameterModes().set(i, getParameterMode(param.mode()));
                    }
                    QName element = new QName(ns, name);
                    Object logical = operation.getInputType().getLogical().get(i).getLogical();
                    if (logical instanceof XMLType) {
                        ((XMLType)logical).setElementName(element);
                    }
                }

                if (!operation.hasReturnTypeVoid()) {
                    String ns = tns;
                    // Default to <operationName>Response for doc-bare
                    String name = (documentStyle ? operationName + "Response" : "return");
                    WebResult result = method.getAnnotation(WebResult.class);
                    if (result != null) {
                        if (!"".equals(result.targetNamespace()))
                            ns = result.targetNamespace();
                        if (!"".equals(result.name()))
                            name = result.name();
                    }
                    QName element = new QName(ns, name);
                    List<DataType> outputDataTypes = operation.getOutputType().getLogical();                    
                    DataType returnDataType = outputDataTypes.get(0);
                    if (returnDataType instanceof XMLType) {
                        ((XMLType)returnDataType).setElementName(element);
                    }
                }
                // Rather than relying on null wrapper, we use a flag with a clearer meaning.
                operation.setNotSubjectToWrapping(true);
            } else {

                RequestWrapper requestWrapper = method.getAnnotation(RequestWrapper.class);
                String ns = requestWrapper == null ? tns : getValue(requestWrapper.targetNamespace(), tns);
                String name =
                    requestWrapper == null ? operationName : getValue(requestWrapper.localName(), operationName);
                String wrapperBeanName = requestWrapper == null ? "" : requestWrapper.className();
                if ("".equals(wrapperBeanName)) {
                    wrapperBeanName = CodeGenerationHelper.getPackagePrefix(clazz) + capitalize(method.getName());
                }

                DataType<XMLType> inputWrapperDT = null;

                final String inputWrapperClassName = wrapperBeanName;
                final String inputNS = ns;
                final String inputName = name;
                inputWrapperDT = AccessController.doPrivileged(new PrivilegedAction<DataType<XMLType>>() {
                    public DataType<XMLType> run() {
                        try {
                            Class<?> wrapperClass = Class.forName(inputWrapperClassName, false, clazz.getClassLoader());
                            QName qname = new QName(inputNS, inputName);
                            DataType dt = new DataTypeImpl<XMLType>(wrapperClass, new XMLType(qname, qname));
                            dataBindingExtensionPoint.introspectType(dt, operation);
                            // TUSCANY-2505
                            if (dt.getLogical() instanceof XMLType) {
                                XMLType xmlType = (XMLType)dt.getLogical();
                                xmlType.setElementName(qname);
                            }
                            return dt;
                        } catch (ClassNotFoundException e) {
                            GeneratedClassLoader cl = new GeneratedClassLoader(clazz.getClassLoader());
                            return new GeneratedDataTypeImpl(xmlAdapterExtensionPoint, method, inputWrapperClassName, inputNS, inputName, true,
                                                             cl);
                        }
                    }
                });

                QName inputWrapper = inputWrapperDT.getLogical().getElementName();

                ResponseWrapper responseWrapper = method.getAnnotation(ResponseWrapper.class);
                ns = responseWrapper == null ? tns : getValue(responseWrapper.targetNamespace(), tns);
                name =
                    responseWrapper == null ? operationName + "Response" : getValue(responseWrapper.localName(),
                                                                                    operationName + "Response");
                wrapperBeanName = responseWrapper == null ? "" : responseWrapper.className();
                if ("".equals(wrapperBeanName)) {
                    wrapperBeanName =
                        CodeGenerationHelper.getPackagePrefix(clazz) + capitalize(method.getName()) + "Response";
                }

                DataType<XMLType> outputWrapperDT = null;
                final String outputWrapperClassName = wrapperBeanName;
                final String outputNS = ns;
                final String outputName = name;

                outputWrapperDT = AccessController.doPrivileged(new PrivilegedAction<DataType<XMLType>>() {
                    public DataType<XMLType> run() {
                        try {
                            Class<?> wrapperClass =
                                Class.forName(outputWrapperClassName, false, clazz.getClassLoader());
                            QName qname = new QName(outputNS, outputName);
                            DataType dt = new DataTypeImpl<XMLType>(wrapperClass, new XMLType(qname, qname));
                            dataBindingExtensionPoint.introspectType(dt, operation);
                            // TUSCANY-2505
                            if (dt.getLogical() instanceof XMLType) {
                                XMLType xmlType = (XMLType)dt.getLogical();
                                xmlType.setElementName(qname);
                            }
                            return dt;
                        } catch (ClassNotFoundException e) {
                            GeneratedClassLoader cl = new GeneratedClassLoader(clazz.getClassLoader());
                            return new GeneratedDataTypeImpl(xmlAdapterExtensionPoint, method, outputWrapperClassName, outputNS, outputName,
                                                             false, cl);
                        }
                    }
                });
                QName outputWrapper = outputWrapperDT.getLogical().getElementName();

                
                //
                // Since JAX-WS specifies that the output wrapper bean consists of the return type output first followed
                // by any other outputs carried in Holder(s), let's look at the output first.
                //
                List<ElementInfo> outputElements = new ArrayList<ElementInfo>();
                WebResult result = method.getAnnotation(WebResult.class);
                // Default to "" for doc-lit-wrapped && non-header
                ns = result != null ? result.targetNamespace() : "";
                ns = getValue(ns, documentStyle && (result == null || !result.header()) ? "" : tns);
                name = result != null ? result.name() : "";
                name = getValue(name, "return");
                QName element = new QName(ns, name);

                if (!operation.hasReturnTypeVoid()) {
                    Object logical = operation.getOutputType().getLogical().get(0).getLogical();
                    QName type = null;
                    if (logical instanceof XMLType) {
                        ((XMLType)logical).setElementName(element);
                        type = ((XMLType)logical).getTypeName();
                    }
                    outputElements.add(new ElementInfo(element, new TypeInfo(type, false, null)));
                }
                
                List<ElementInfo> inputElements = new ArrayList<ElementInfo>();
                for (int i = 0; i < operation.getInputType().getLogical().size(); i++) {
                    WebParam param = getAnnotation(method, i, WebParam.class);
                    ns = param != null ? param.targetNamespace() : "";
                    // Default to "" for doc-lit-wrapped && non-header
                    ns = getValue(ns, documentStyle && (param == null || !param.header()) ? "" : tns);
                    name = param != null ? param.name() : "";
                    name = getValue(name, "arg" + i);
                    element = new QName(ns, name);
                    Object logical = operation.getInputType().getLogical().get(i).getLogical();
                    QName type = null;
                    if (logical instanceof XMLType) {
                        ((XMLType)logical).setElementName(element);
                        type = ((XMLType)logical).getTypeName();
                    }
                                        
                    if (param != null) {
                        ParameterMode mode = getParameterMode(param.mode());
                        operation.getParameterModes().set(i, mode);
                    }
                    ParameterMode mode = operation.getParameterModes().get(i);

                    if (mode.equals(ParameterMode.INOUT)) {
                        inputElements.add(new ElementInfo(element, new TypeInfo(type, false, null)));
                        outputElements.add(new ElementInfo(element, new TypeInfo(type, false, null)));
                    } else if (mode.equals(ParameterMode.OUT)) {
                        outputElements.add(new ElementInfo(element, new TypeInfo(type, false, null)));
                    } else {
                        inputElements.add(new ElementInfo(element, new TypeInfo(type, false, null)));
                    }
                }                                  
                    
                // TUSCANY-3804 - handle output wrapper separately
                String dbIn = inputWrapperDT != null ? inputWrapperDT.getDataBinding() : JAXB_DATABINDING;
                String dbOut = outputWrapperDT != null ? outputWrapperDT.getDataBinding() : JAXB_DATABINDING;
                
                WrapperInfo inputWrapperInfo = new WrapperInfo(dbIn, new ElementInfo(inputWrapper, null), inputElements);
                WrapperInfo outputWrapperInfo = new WrapperInfo(dbOut, new ElementInfo(outputWrapper, null), outputElements);                  

                inputWrapperInfo.setWrapperType(inputWrapperDT);
                outputWrapperInfo.setWrapperType(outputWrapperDT);

                operation.setInputWrapper(inputWrapperInfo);
                operation.setOutputWrapper(outputWrapperInfo);
            }
            
            // In both bare and wrapped cases, remove OUT-only parameters from input DataType.
            // This is a key point then because it's the last time in which the number of parameters in 
            // Java matches the number of logical inputs.  After this, things will be out of synch, for
            // example the number of parameter modes won't match the number of inputs.
            List<ParameterMode> parmModes = operation.getParameterModes();
            List<DataType> inputDTs = operation.getInputType().getLogical();
            for (int i = parmModes.size() - 1; i>=0; i--) {
                if (parmModes.get(i).equals(ParameterMode.OUT)) {
                    inputDTs.remove(i);
                }
            }
            
        }
    }

    @SuppressWarnings("unchecked")
    private void introspectFaultTypes(Operation operation) {
        if (operation != null && operation.getFaultTypes() != null) {
            for (DataType exceptionType : operation.getFaultTypes()) {
                faultExceptionMapper.introspectFaultDataType(exceptionType, operation, true);
                DataType faultType = (DataType)exceptionType.getLogical();
                if (JavaExceptionDataBinding.NAME.equals(faultType.getDataBinding())) {
                    // The exception class doesn't have an associated bean class, so
                    // synthesize a virtual bean by introspecting the exception class.
                    createSyntheticBean(operation, exceptionType);
                }
            }
        }
    }

    private void createSyntheticBean(Operation operation, DataType exceptionType) {
        DataType faultType = (DataType)exceptionType.getLogical();
        QName faultBeanName = ((XMLType)faultType.getLogical()).getElementName();
        List<DataType<XMLType>> beanDataTypes = new ArrayList<DataType<XMLType>>();
        for (Method aMethod : exceptionType.getPhysical().getMethods()) {
            if (Modifier.isPublic(aMethod.getModifiers()) && aMethod.getName().startsWith(GET)
                && aMethod.getParameterTypes().length == 0
                && JAXWSFaultExceptionMapper.isMappedGetter(aMethod.getName())) {
                String propName = resolvePropertyFromMethod(aMethod.getName());
                QName propQName = new QName(faultBeanName.getNamespaceURI(), propName);
                Class<?> propType = aMethod.getReturnType();
                XMLType xmlPropType = new XMLType(propQName, null);
                DataType<XMLType> propDT = new DataTypeImpl<XMLType>(propType, xmlPropType);
                org.apache.tuscany.sca.databinding.annotation.DataType dt =
                    aMethod.getAnnotation(org.apache.tuscany.sca.databinding.annotation.DataType.class);
                if (dt != null) {
                    propDT.setDataBinding(dt.value());
                }
                dataBindingExtensionPoint.introspectType(propDT, operation);

                // sort the list lexicographically as specified in JAX-WS spec section 3.7
                int i = 0;
                for (; i < beanDataTypes.size(); i++) {
                    if (beanDataTypes.get(i).getLogical().getElementName().getLocalPart().compareTo(propName) > 0) {
                        break;
                    }
                }
                beanDataTypes.add(i, propDT);
            }
        }
        operation.getFaultBeans().put(faultBeanName, beanDataTypes);
    }

    private String resolvePropertyFromMethod(String methodName) {
        StringBuffer propName = new StringBuffer();
        propName.append(Character.toLowerCase(methodName.charAt(GET.length())));
        propName.append(methodName.substring(GET.length() + 1));
        return propName.toString();
    }

    private <T extends Annotation> T getAnnotation(Method method, int index, Class<T> annotationType) {
        Annotation[] annotations = method.getParameterAnnotations()[index];
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == annotationType) {
                return annotationType.cast(annotation);
            }
        }
        return null;
    }

    private static String getValue(String value, String defaultValue) {
        return "".equals(value) ? defaultValue : value;
    }

}
