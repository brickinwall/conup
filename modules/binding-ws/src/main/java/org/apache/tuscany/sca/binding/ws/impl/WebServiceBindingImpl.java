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

package org.apache.tuscany.sca.binding.ws.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Extensible;
import org.apache.tuscany.sca.assembly.Extension;
import org.apache.tuscany.sca.assembly.OperationSelector;
import org.apache.tuscany.sca.assembly.WireFormat;
import org.apache.tuscany.sca.binding.ws.WebServiceBinding;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;
import org.apache.tuscany.sca.policy.DefaultIntent;
import org.apache.tuscany.sca.policy.DefaultingPolicySubject;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySubject;
import org.w3c.dom.Element;

/**
 * Represents a WebService binding.
 *
 * @version $Rev: 1294065 $ $Date: 2012-02-27 08:40:10 +0000 (Mon, 27 Feb 2012) $
 */
class WebServiceBindingImpl implements WebServiceBinding, DefaultingPolicySubject, Extensible {
    private String name;
    private String uri;
    private boolean unresolved;
    private List<Object> extensions = new ArrayList<Object>();
    private List<Extension> attributeExtensions = new ArrayList<Extension>();
    private List<DefaultIntent> defaultIntents = new ArrayList<DefaultIntent>();
    private List<Intent> requiredIntents = new ArrayList<Intent>();
    private List<PolicySet> policySets = new ArrayList<PolicySet>();
    private ExtensionType extensionType;
    private String location;
    private Binding binding;
    private Service service;
    private Port port;
    private Port endpoint;
    private QName bindingName;
    private String portName;
    private QName serviceName;
    private String endpointName;
    private InterfaceContract bindingInterfaceContract;
    private Element endPointReference;
    private String wsdlNamespace;
    private WSDLDefinition userSpecifiedWSDLDefinition;
    private Definition generatedWSDLDocument;
    private boolean isDocumentStyle;
    private boolean isLiteralEncoding;
    private boolean isMessageWrapped;
    private Map<String, String> wsdliLocations = new HashMap<String, String>();
    private String userSpecifiedUri;

    protected WebServiceBindingImpl() {
    }

    /**
     * Provide a meaningful representation of this Binding
     */
    public String toString() {
        return "Web Service Binding: " + name;
    } // end method toString

    public String getName() {
        return name;
    }

    public String getURI() {
        return uri;
    }
    
    public String getUserSpecifiedURI() {
        return userSpecifiedUri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setURI(String uri) {
        this.uri = uri;
        
        // when the binding is cloned for callback puposes
        // when the user has not configured a callback binding
        // we want all the URIs to be null so that the builder
        // will calculate the correct callback URI
        if (uri == null){
            setUserSpecifiedURI(null);
        }
    }
    
    public void setUserSpecifiedURI(String uri) {
        this.userSpecifiedUri = uri;
    }    

    public boolean isUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean unresolved) {
        this.unresolved = unresolved;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public List<Object> getExtensions() {
        return extensions;
    }

    public List<Extension> getAttributeExtensions() {
        return attributeExtensions;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Binding getBinding() {
        if (binding == null) {
            if (getUserSpecifiedWSDLDefinition() != null && userSpecifiedWSDLDefinition.getBinding() != null) {
                binding = userSpecifiedWSDLDefinition.getBinding();
                determineWSDLCharacteristics();
            }
        }
        return binding;
    }

    public QName getBindingName() {
        if (isUnresolved()) {
            return bindingName;
        } else if (binding != null) {
            return binding.getQName();
        } else {
            return null;
        }
    }

    public String getEndpointName() {
        if (isUnresolved()) {
            return endpointName;
        } else if (endpoint != null) {
            //TODO support WSDL 2.0
            return endpoint.getName();
        } else {
            return null;
        }
    }

    public Port getEndpoint() {
        return endpoint;
    }

    public Port getPort() {
        return port;
    }

    public String getPortName() {
        if (isUnresolved()) {
            return portName;
        } else if (port != null) {
            return port.getName();
        } else {
            return null;
        }
    }

    public Service getService() {
        return service;
    }

    public QName getServiceName() {
        if (isUnresolved()) {
            return serviceName;
        } else if (service != null) {
            return service.getQName();
        } else {
            return null;
        }
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
        determineWSDLCharacteristics();
    }

    public void setBindingName(QName bindingName) {
        if (!isUnresolved()) {
            throw new IllegalStateException();
        }
        this.bindingName = bindingName;
    }

    public void setEndpoint(Port endpoint) {
        this.endpoint = endpoint;
    }

    public void setEndpointName(String endpointName) {
        if (!isUnresolved()) {
            throw new IllegalStateException();
        }
        this.endpointName = endpointName;
    }

    public void setPort(Port port) {
        this.port = port;
    }

    public void setPortName(String portName) {
        if (!isUnresolved()) {
            throw new IllegalStateException();
        }
        this.portName = portName;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setServiceName(QName serviceName) {
        if (!isUnresolved()) {
            throw new IllegalStateException();
        }
        this.serviceName = serviceName;
    }

    public WSDLDefinition getUserSpecifiedWSDLDefinition() {
        if (userSpecifiedWSDLDefinition == null) {
            Interface iface = bindingInterfaceContract.getInterface();
            if (iface instanceof WSDLInterface) {
                userSpecifiedWSDLDefinition = ((WSDLInterface)iface).getWsdlDefinition();
            }
        }
        return userSpecifiedWSDLDefinition;
    }

    public void setUserSpecifiedWSDLDefinition(WSDLDefinition wsdlDefinition) {
        this.userSpecifiedWSDLDefinition = wsdlDefinition;
    }

    public String getNamespace() {
        return wsdlNamespace;
    }

    public void setNamespace(String namespace) {
        this.wsdlNamespace = namespace;
    }

    public InterfaceContract getBindingInterfaceContract() {
        return bindingInterfaceContract;
    }

    public void setBindingInterfaceContract(InterfaceContract bindingInterfaceContract) {
        this.bindingInterfaceContract = bindingInterfaceContract;
    }

    public List<PolicySet> getPolicySets() {
        return policySets;
    }

    public List<Intent> getRequiredIntents() {
        return requiredIntents;
    }

    public ExtensionType getExtensionType() {
        return extensionType;
    }

    public void setExtensionType(ExtensionType intentAttachPointType) {
        this.extensionType = intentAttachPointType;
    }

    public Element getEndPointReference() {
        return endPointReference;
    }

    public void setEndPointReference(Element epr) {
        this.endPointReference = epr;
    }

    public Definition getGeneratedWSDLDocument() {
        return generatedWSDLDocument;
    }

    public void setGeneratedWSDLDocument(Definition definition) {
        this.generatedWSDLDocument = definition;
        determineWSDLCharacteristics();
    }

    public QName getType() {
        return TYPE;
    }

    public WireFormat getRequestWireFormat() {
        return null;
    }

    public void setRequestWireFormat(WireFormat wireFormat) {
    }

    public WireFormat getResponseWireFormat() {
        return null;
    }

    public void setResponseWireFormat(WireFormat wireFormat) {
    }

    public OperationSelector getOperationSelector() {
        return null;
    }

    public void setOperationSelector(OperationSelector operationSelector) {
    }

    /**
     * Some items get calculated and cached as they are used at runtime
     * to decide what message processing is required
     */
    protected void determineWSDLCharacteristics() {
        setIsDocumentStyle();
        setIsLiteralEncoding();
        setIsMessageWrapped();
    }

    protected void setIsDocumentStyle() {

        if (binding == null) {
            if (userSpecifiedWSDLDefinition != null && userSpecifiedWSDLDefinition.getDefinition() != null) {
                Message firstMessage =
                    (Message)userSpecifiedWSDLDefinition.getDefinition().getMessages().values().iterator().next();
                Part firstPart = (Part)firstMessage.getParts().values().iterator().next();
                if (firstPart.getTypeName() != null) {
                    isDocumentStyle = false;
                    return;
                }
            }

            // default to document style
            isDocumentStyle = true;
            return;
        } else {
            for (Object ext : binding.getExtensibilityElements()) {
                if (ext instanceof SOAPBinding) {
                    isDocumentStyle = !"rpc".equals(((SOAPBinding)ext).getStyle());
                    return;
                }
            }
            isDocumentStyle = true;
            return;
        }

    }

    protected void setIsLiteralEncoding() {

        if (binding == null) {
            // default to literal encoding
            isLiteralEncoding = true;
            return;
        } else {
            for (Object bop : binding.getBindingOperations()) {
                BindingInput bindingInput = ((BindingOperation)bop).getBindingInput();
                if (bindingInput != null) {
                    for (Object ext : bindingInput.getExtensibilityElements()) {
                        if (ext instanceof SOAPBody) {
                            isLiteralEncoding = "literal".equals(((SOAPBody)ext).getUse());
                            return;
                        }
                    }
                }
            }
            isLiteralEncoding = true;
            return;
        }
    }

    protected void setIsMessageWrapped() {
        if (getBindingInterfaceContract() != null) {
            isMessageWrapped = getBindingInterfaceContract().getInterface().getOperations().get(0).isInputWrapperStyle();
        }
    }

    public boolean isRpcEncoded() {
        return (!isDocumentStyle) && (!isLiteralEncoding);
    }

    public boolean isRpcLiteral() {
        return (!isDocumentStyle) && (isLiteralEncoding);
    }

    public boolean isDocEncoded() {
        return (isDocumentStyle) && (!isLiteralEncoding);
    }

    public boolean isDocLiteralUnwrapped() {
        setIsMessageWrapped();
        return (isDocumentStyle) && (isLiteralEncoding) && (!isMessageWrapped);
    }

    public boolean isDocLiteralWrapped() {
        setIsMessageWrapped();
        return (isDocumentStyle) && (isLiteralEncoding) && (isMessageWrapped);
    }

    public boolean isDocLiteralBare() {
        setIsMessageWrapped();
        return (isDocumentStyle) && (isLiteralEncoding);
    }

    public boolean isHTTPTransport() {
        return getBindingTransport().equals("http://schemas.xmlsoap.org/soap/http");
    }

    public boolean isJMSTransport() {
        return getBindingTransport().equals("http://schemas.xmlsoap.org/soap/jms");
    }

    public String getBindingTransport() {
        if (binding != null) {
            for (Object ext : binding.getExtensibilityElements()) {
                if (ext instanceof SOAPBinding) {
                    return ((SOAPBinding)ext).getTransportURI();
                }
            }
        }

        // if no binding is explicitly specified by the user then default to http
        return "http://schemas.xmlsoap.org/soap/http";
    }

    public Map<String, String> getWsdliLocations() {
        return wsdliLocations;
    }
    
    @Override
    public List<DefaultIntent> getDefaultIntents() {
        return defaultIntents;
    }
}
