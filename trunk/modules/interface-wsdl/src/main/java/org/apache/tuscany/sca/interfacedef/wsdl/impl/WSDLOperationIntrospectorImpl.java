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

package org.apache.tuscany.sca.interfacedef.wsdl.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.ParameterMode;

import static org.apache.tuscany.sca.interfacedef.Operation.IDL_INPUT;
import static org.apache.tuscany.sca.interfacedef.Operation.IDL_OUTPUT;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.util.ElementInfo;
import org.apache.tuscany.sca.interfacedef.util.FaultException;
import org.apache.tuscany.sca.interfacedef.util.TypeInfo;
import org.apache.tuscany.sca.interfacedef.util.WrapperInfo;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLOperation;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.xsd.XSDFactory;
import org.apache.tuscany.sca.xsd.XSDefinition;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * Metadata for a WSDL operation
 *
 * @version $Rev: 1213702 $ $Date: 2011-12-13 14:12:38 +0000 (Tue, 13 Dec 2011) $
 */
public class WSDLOperationIntrospectorImpl {
    private static final Logger logger = Logger.getLogger(WSDLOperationIntrospectorImpl.class.getName());
    private static final QName ANY = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "any");

    private XSDFactory xsdFactory;
    private ModelResolver resolver;
    private ProcessorContext context;
    private WSDLDefinition wsdlDefinition;
    private javax.wsdl.Operation operation;
    private WSDLOperation operationModel;
    private DataType<List<DataType>> inputType;
    private DataType outputType;
    private List<DataType> faultTypes;
    private String dataBinding;

    /**
     * @param wsdlFactory The WSDLFactory to use
     * @param operation The WSDL4J operation
     * @param wsdlDefinition The WSDL Definition
     * @param dataBinding The default databinding
     * @param resolver The ModelResolver to use
     */
    public WSDLOperationIntrospectorImpl(XSDFactory xsdFactory,
                                         javax.wsdl.Operation operation,
                                         WSDLDefinition wsdlDefinition,
                                         String dataBinding,
                                         ModelResolver resolver,
                                         Monitor monitor) {
        super();
        this.xsdFactory = xsdFactory;
        this.operation = operation;
        this.wsdlDefinition = wsdlDefinition;
        this.resolver = resolver;
        this.dataBinding = dataBinding;
        this.wrapper = new Wrapper();
        this.context = new ProcessorContext(monitor);
    }

    private Wrapper wrapper;

    private Boolean wrapperStyle;

    /**
     * Test if the operation qualifies wrapper style as defined by the JAX-WS
     * 2.0 Specification
     *
     * @return true if the operation qualifies wrapper style, otherwise false
     */
    public boolean isWrapperStyle() throws InvalidWSDLException {
        if (wrapperStyle == null) {
            wrapperStyle =
                (operation.getInput() == null || operation.getInput().getMessage() == null
                    || operation.getInput().getMessage().getParts().size() == 0 || wrapper.getInputChildElements() != null) && (operation
                    .getOutput() == null || operation.getOutput().getMessage() == null
                    || operation.getOutput().getMessage().getParts().size() == 0 || wrapper.getOutputChildElements() != null);
        }
        return wrapperStyle;
    }

    public Wrapper getWrapper() throws InvalidWSDLException {
        if (!isWrapperStyle()) {
            throw new IllegalStateException("The operation is not wrapper style.");
        } else {
            return wrapper;
        }
    }

    /**
     * @return
     * @throws InvalidWSDLException
     */
    public DataType<List<DataType>> getInputType() throws InvalidWSDLException {
        if (inputType == null) {
            Input input = operation.getInput();
            Message message = (input == null) ? null : input.getMessage();
            inputType = getMessageType(message);
            inputType.setDataBinding(IDL_INPUT);
        }
        return inputType;
    }

    /**
     * @return
     * @throws InvalidWSDLException
     */
    @SuppressWarnings("unchecked")
    public DataType<List<DataType>> getOutputType() throws InvalidWSDLException {
        if (outputType == null) {
            Output output = operation.getOutput();
            Message outputMsg = (output == null) ? null : output.getMessage();
            outputType = getMessageType(outputMsg);
            outputType.setDataBinding(IDL_OUTPUT);
        }
        return outputType;
    }

    /**
     * @return
     * @throws NotSupportedWSDLException
     */
    public List<DataType> getFaultTypes() throws InvalidWSDLException {
        if (faultTypes == null) {
            Collection faults = operation.getFaults().values();
            faultTypes = new ArrayList<DataType>();
            for (Object f : faults) {
                Fault fault = (Fault)f;
                Message faultMsg = fault.getMessage();
                List faultParts = faultMsg.getOrderedParts(null);
                if (faultParts.size() != 1) {
                    throw new InvalidWSDLException("The fault message MUST have a single part");
                }
                Part part = (Part)faultParts.get(0);
                WSDLPart wsdlPart = new WSDLPart(part, Object.class);
                faultTypes.add(new DataTypeImpl<DataType>(FaultException.class, wsdlPart.getDataType()));
            }
        }
        return faultTypes;
    }

    private DataType<List<DataType>> getMessageType(Message message) throws InvalidWSDLException {
        List<DataType> partTypes = new ArrayList<DataType>();
        if (message != null) {
            Collection parts = message.getOrderedParts(null);
            for (Object p : parts) {
                WSDLPart part = new WSDLPart((Part)p, Object.class);
                DataType<XMLType> partType = part.getDataType();
                partTypes.add(partType);
            }
        }
        return new DataTypeImpl<List<DataType>>(dataBinding, Object[].class, partTypes);
    }

    /**
     * @return
     * @throws NotSupportedWSDLException
     */
    public Operation getOperation() throws InvalidWSDLException {
        if (operationModel == null) {
            boolean oneway = (operation.getOutput() == null);
            operationModel = new WSDLOperationImpl();
            operationModel.setWsdlOperation(operation);
            operationModel.setName(operation.getName());
            operationModel.setFaultTypes(getFaultTypes());
            operationModel.setNonBlocking(oneway);
            DataType<List<DataType>> inputType = getInputType(); 
            operationModel.setInputType(inputType);
            List<ParameterMode> modes = operationModel.getParameterModes();
            for (DataType dt : inputType.getLogical()) {
                modes.add(ParameterMode.IN);                
            }
            operationModel.setOutputType(getOutputType());

            operationModel.setInputWrapperStyle(isWrapperStyle());
            operationModel.setOutputWrapperStyle(isWrapperStyle());
            
            if (isWrapperStyle()) {
                operationModel.setInputWrapper(getWrapper().getInputWrapperInfo());
                operationModel.setOutputWrapper(getWrapper().getOutputWrapperInfo());                
            }
        }
        return operationModel;
    }

    private XmlSchemaElement getElement(QName elementName) {

        XmlSchemaElement element = wsdlDefinition.getXmlSchemaElement(elementName);
        if (element == null) {
            XSDefinition definition = xsdFactory.createXSDefinition();
            definition.setUnresolved(true);
            definition.setNamespace(elementName.getNamespaceURI());
            definition = resolver.resolveModel(XSDefinition.class, definition, context);
            if (definition.getSchema() != null) {
                element = definition.getSchema().getElementByName(elementName);
            }
        }
        return element;
    }

    private XmlSchemaType getType(QName typeName) {
        XmlSchemaType type = wsdlDefinition.getXmlSchemaType(typeName);
        if (type == null) {
            XSDefinition definition = xsdFactory.createXSDefinition();
            definition.setUnresolved(true);
            definition.setNamespace(typeName.getNamespaceURI());
            definition = resolver.resolveModel(XSDefinition.class, definition, context);
            if (definition.getSchema() != null) {
                type = definition.getSchema().getTypeByName(typeName);
            }
        }
        return type;
    }

    /**
     * Metadata for a WSDL part
     */
    public class WSDLPart {
        private Part part;

        private XmlSchemaElement element;

        private DataType dataType;

        public WSDLPart(Part part, Class javaType) throws InvalidWSDLException {
            this.part = part;
            QName elementName = part.getElementName();
            if (elementName != null) {
                element = WSDLOperationIntrospectorImpl.this.getElement(elementName);
                if (element == null) {
                    throw new InvalidWSDLException("Element cannot be resolved: " + elementName.toString());
                }
            } else {
                // Create an faked XSD element to host the metadata
                element = new XmlSchemaElement();
                element.setName(part.getName());
                element.setQName(new QName(null, part.getName()));
                QName typeName = part.getTypeName();
                if (typeName != null) {
                    XmlSchemaType type = WSDLOperationIntrospectorImpl.this.getType(typeName);
                    if (type == null) {
                        throw new InvalidWSDLException("Type cannot be resolved: " + typeName.toString());
                    }
                    element.setSchemaType(type);
                    element.setSchemaTypeName(type.getQName());
                }
            }
            XMLType xmlType = new XMLType(getElementInfo(element));
            xmlType.setNillable(element.isNillable());
            xmlType.setMany(element.getMaxOccurs() > 1);
            dataType = new DataTypeImpl<XMLType>(dataBinding, javaType, xmlType);
        }

        /**
         * @return the element
         */
        public XmlSchemaElement getElement() {
            return element;
        }

        /**
         * @return the part
         */
        public Part getPart() {
            return part;
        }

        /**
         * @return the dataType
         */
        public DataType<XMLType> getDataType() {
            return dataType;
        }
    }

    /**
     * The "Wrapper Style" WSDL operation is defined by The Java API for
     * XML-Based Web Services (JAX-WS) 2.0 specification, section 2.3.1.2
     * Wrapper Style. <p/> A WSDL operation qualifies for wrapper style mapping
     * only if the following criteria are met:
     * <ul>
     * <li>(i) The operation�s input and output messages (if present) each
     * contain only a single part
     * <li>(ii) The input message part refers to a global element declaration
     * whose localname is equal to the operation name
     * <li>(iii) The output message part refers to a global element declaration
     * <li>(iv) The elements referred to by the input and output message parts
     * (henceforth referred to as wrapper elements) are both complex types
     * defined using the xsd:sequence compositor
     * <li>(v) The wrapper elements only contain child elements, they must not
     * contain other structures such as wildcards (element or attribute),
     * xsd:choice, substitution groups (element references are not permitted) or
     * attributes; furthermore, they must not be nillable.
     * </ul>
     */
    public class Wrapper {
        private XmlSchemaElement inputWrapperElement;

        private XmlSchemaElement outputWrapperElement;

        private List<XmlSchemaElement> inputElements;

        private List<XmlSchemaElement> outputElements;

        private transient WrapperInfo inputWrapperInfo;
        private transient WrapperInfo outputWrapperInfo;

        private List<XmlSchemaElement> getChildElements(XmlSchemaElement element) throws InvalidWSDLException {
            if (element == null) {
                return null;
            }
            if (element.isNillable()) {
                // Wrapper element cannot be nillable
                // return null;
            }
            XmlSchemaType type = element.getSchemaType();
            if (type == null) {
                String qName = element.getQName().toString();
                throw new InvalidWSDLException("The XML schema element does not have a type: " + qName);
            }
            if (!(type instanceof XmlSchemaComplexType)) {
                // Has to be a complexType
                return null;
            }
            XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
            if (complexType.getAttributes().getCount() != 0 || complexType.getAnyAttribute() != null) {
                // No attributes
                return null;
            }
            XmlSchemaParticle particle = complexType.getParticle();
            if (particle == null) {
                // No particle
                return Collections.emptyList();
            }
            if (!(particle instanceof XmlSchemaSequence)) {
                return null;
            }
            XmlSchemaSequence sequence = (XmlSchemaSequence)complexType.getParticle();
            XmlSchemaObjectCollection items = sequence.getItems();
            List<XmlSchemaElement> childElements = new ArrayList<XmlSchemaElement>();
            for (int i = 0; i < items.getCount(); i++) {
                XmlSchemaObject schemaObject = items.getItem(i);
                if (!(schemaObject instanceof XmlSchemaElement)) {
                    // Should contain elements only
                    return null;
                }
                XmlSchemaElement childElement = (XmlSchemaElement)schemaObject;
                /*
                if (childElement.getSubstitutionGroup() != null) {
                    return null;
                }
                */
                if (childElement.getName() == null || childElement.getRefName() != null) {
                    XmlSchemaElement ref = getElement(childElement.getRefName());
                    if (ref == null) {
                        throw new InvalidWSDLException("XML schema element ref cannot be resolved: " + childElement);
                    }
                    childElement = ref;
                }
                if (ANY.equals(childElement.getQName())) {
                    // Wildcard is not allowed
                    return null;
                }
                // TODO: Do we support maxOccurs >1 ?
                if (childElement.getMaxOccurs() > 1) {
                    // TODO: [rfeng] To be implemented
                    /*
                    if(logger.isLoggable(Level.WARNING)) {
                    	logger.warning("Support for elements with maxOccurs>1 is not implemented.");
                    }
                    */
                    // return null;
                }
                childElements.add(childElement);
            }
            return childElements;
        }

        /**
         * Return a list of child XSD elements under the wrapped request element
         *
         * @return a list of child XSD elements or null if if the request
         *         element is not wrapped
         */
        public List<XmlSchemaElement> getInputChildElements() throws InvalidWSDLException {
            if (inputElements != null) {
                return inputElements;
            }
            Input input = operation.getInput();
            if (input != null) {
                Message inputMsg = input.getMessage();
                Collection parts = inputMsg.getParts().values();
                if (parts.size() != 1) {
                    return null;
                }
                Part part = (Part)parts.iterator().next();
                QName elementName = part.getElementName();
                if (elementName == null) {
                    return null;
                }
                if (!operation.getName().equals(elementName.getLocalPart())) {
                    return null;
                }
                inputWrapperElement = getElement(elementName);
                if (inputWrapperElement == null) {
                    throw new InvalidWSDLException("The element is not declared in a XML schema: " + elementName
                        .toString());
                }
                if (inputWrapperElement.isNillable()) {
                    // The wrapper element cannot be nilable
                    // FIXME: Java2WSDL create nillable
                    // return null;
                }
                inputElements = getChildElements(inputWrapperElement);
                return inputElements;
            } else {
                return null;
            }
        }

        /**
         * Return a list of child XSD elements under the wrapped response
         * element
         *
         * @return a list of child XSD elements or null if if the response
         *         element is not wrapped
         */
        public List<XmlSchemaElement> getOutputChildElements() throws InvalidWSDLException {
            if (outputElements != null) {
                return outputElements;
            }
            Output output = operation.getOutput();
            if (output != null) {
                Message outputMsg = output.getMessage();
                Collection parts = outputMsg.getParts().values();
                if (parts.size() != 1) {
                    return null;
                }
                Part part = (Part)parts.iterator().next();
                QName elementName = part.getElementName();
                if (elementName == null) {
                    throw new InvalidWSDLException("The element is not declared in the XML schema: " + part.getName());
                }
                outputWrapperElement = WSDLOperationIntrospectorImpl.this.getElement(elementName);
                if (outputWrapperElement == null) {
                    return null;
                }
                if (outputWrapperElement.isNillable()) {
                    // The wrapper element cannot be nilable
                    // return null;
                }
                outputElements = getChildElements(outputWrapperElement);
                // FIXME: Do we support multiple child elements for the response?
                return outputElements;
            } else {
                return null;
            }
        }

        /**
         * @return the inputWrapperElement
         */
        public XmlSchemaElement getInputWrapperElement() {
            return inputWrapperElement;
        }

        /**
         * @return the outputWrapperElement
         */
        public XmlSchemaElement getOutputWrapperElement() {
            return outputWrapperElement;
        }

        public WrapperInfo getInputWrapperInfo() throws InvalidWSDLException {
            if (inputWrapperInfo == null) {
                ElementInfo in = getElementInfo(getInputWrapperElement());
                List<ElementInfo> inChildren = new ArrayList<ElementInfo>();
                if (in != null) {
                    for (XmlSchemaElement e : getInputChildElements()) {
                        inChildren.add(getElementInfo(e));
                    }
                }
                inputWrapperInfo = new WrapperInfo(dataBinding, in, inChildren);
            }
            return inputWrapperInfo;
        }
    
        public WrapperInfo getOutputWrapperInfo() throws InvalidWSDLException {
            if (outputWrapperInfo == null) {
                ElementInfo out = getElementInfo(getOutputWrapperElement());
                List<ElementInfo> outChildren = new ArrayList<ElementInfo>();
                if (out != null) {
                    for (XmlSchemaElement e : getOutputChildElements()) {
                        outChildren.add(getElementInfo(e));
                    }
                }
                outputWrapperInfo = new WrapperInfo(dataBinding, out, outChildren);
            }
            return outputWrapperInfo;
        }
    }    

    private static ElementInfo getElementInfo(XmlSchemaElement element) {
        if (element == null) {
            return null;
        }
        ElementInfo elementInfo = new ElementInfo(element.getQName(), getTypeInfo(element.getSchemaType()));
        elementInfo.setMany(element.getMaxOccurs() > 1);
        elementInfo.setNillable(element.isNillable());
        elementInfo.setOmissible(element.getMinOccurs()==0);
        return elementInfo;
    }

    private static TypeInfo getTypeInfo(XmlSchemaType type) {
        if (type == null) {
            return null;
        }
        XmlSchemaType baseType = (XmlSchemaType)type.getBaseSchemaType();
        QName name = type.getQName();
        boolean simple = (type instanceof XmlSchemaSimpleType);
        if (baseType == null) {
            return new TypeInfo(name, simple, null);
        } else {
            return new TypeInfo(name, simple, getTypeInfo(baseType));
        }
    }

}
