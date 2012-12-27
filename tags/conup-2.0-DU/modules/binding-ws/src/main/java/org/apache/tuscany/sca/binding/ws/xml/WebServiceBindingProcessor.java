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

package org.apache.tuscany.sca.binding.ws.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Callback;
import org.apache.tuscany.sca.assembly.Extensible;
import org.apache.tuscany.sca.assembly.Extension;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.xml.Messages;
import org.apache.tuscany.sca.assembly.xml.PolicySubjectProcessor;
import org.apache.tuscany.sca.binding.ws.WebServiceBinding;
import org.apache.tuscany.sca.binding.ws.WebServiceBindingFactory;
import org.apache.tuscany.sca.common.xml.stax.StAXHelper;
import org.apache.tuscany.sca.contribution.processor.BaseStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionRuntimeException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXAttributeProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXAttributeProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXAttributeProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLFactory;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterfaceContract;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLObject;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.apache.tuscany.sca.policy.PolicyFactory;

/**
 * This is the StAXArtifactProcessor for the Web Services Binding.
 *
 * @version $Rev: 1295144 $ $Date: 2012-02-29 15:06:57 +0000 (Wed, 29 Feb 2012) $
 */
public class WebServiceBindingProcessor extends BaseStAXArtifactProcessor implements StAXArtifactProcessor<WebServiceBinding>, WebServiceConstants {

    private ExtensionPointRegistry extensionPoints;
    private WSDLFactory wsdlFactory;
    private WebServiceBindingFactory wsFactory;
    private PolicyFactory policyFactory;
    private AssemblyFactory assemblyFactory;
    private PolicySubjectProcessor policyProcessor;
    //private PolicyFactory intentAttachPointTypeFactory;
    private StAXHelper staxHelper;
    private StAXAttributeProcessor<Object> extensionAttributeProcessor;
    private ProcessorContext processorContext;
    
    
    public WebServiceBindingProcessor(ExtensionPointRegistry extensionPoints) {
        this.extensionPoints = extensionPoints;
        FactoryExtensionPoint modelFactories = extensionPoints.getExtensionPoint(FactoryExtensionPoint.class);
        this.policyFactory = modelFactories.getFactory(PolicyFactory.class);
        this.wsFactory = modelFactories.getFactory(WebServiceBindingFactory.class);
        this.wsdlFactory = modelFactories.getFactory(WSDLFactory.class);
        this.assemblyFactory = modelFactories.getFactory(AssemblyFactory.class);        
        this.policyProcessor = new PolicySubjectProcessor(policyFactory);
        staxHelper = StAXHelper.getInstance(extensionPoints);
        XMLInputFactory inputFactory = extensionPoints.getExtensionPoint(XMLInputFactory.class);
        XMLOutputFactory outputFactory = extensionPoints.getExtensionPoint(XMLOutputFactory.class);
        StAXAttributeProcessorExtensionPoint attributeExtensionPoint = extensionPoints.getExtensionPoint(StAXAttributeProcessorExtensionPoint.class);
        this.extensionAttributeProcessor = new ExtensibleStAXAttributeProcessor(attributeExtensionPoint ,inputFactory, outputFactory);
        this.processorContext = new ProcessorContext(extensionPoints);
    }
    
    /**
     * Report a warning.
     * 
     * @param problems
     * @param message
     * @param model
     */
    private void warning(Monitor monitor, String message, Object model, Object... messageParameters) {
       if (monitor != null) {
           Problem problem = monitor.createProblem(this.getClass().getName(), "binding-wsxml-validation-messages", Severity.WARNING, model, message, (Object[])messageParameters);
           monitor.problem(problem);
       }
    }
         
    /**
     * Report a error.
     * 
     * @param problems
     * @param message
     * @param model
     */
    private void error(Monitor monitor, String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem = monitor.createProblem(this.getClass().getName(), "binding-wsxml-validation-messages", Severity.ERROR, model, message, (Object[])messageParameters);
            monitor.problem(problem);
        }        
    }
    
    /**
     * Report an exception.
     * 
     * @param problem
     * @param model
     * @param exception
     */
    private void error(Monitor monitor, String message, Object model, Exception ex) {
        if (monitor != null) {
            Problem problem = monitor.createProblem(this.getClass().getName(), "binding-wsxml-validation-messages", Severity.ERROR, model, message, ex);
            monitor.problem(problem);
        }        
    }    

    public WebServiceBinding read(XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {
        Monitor monitor = context.getMonitor();
        // Read a <binding.ws>
        WebServiceBinding wsBinding = wsFactory.createWebServiceBinding();
        /*ExtensionType bindingType = intentAttachPointTypeFactory.createBindingType();
        bindingType.setName(getArtifactType());
        bindingType.setUnresolved(true);
        ((PolicySubject)wsBinding).setType(bindingType);*/
        wsBinding.setUnresolved(true);

        // Read policies
        policyProcessor.readPolicies(wsBinding, reader);

        // Read the binding name
        String name = reader.getAttributeValue(null, NAME);
        if (name != null) {
            wsBinding.setName(name);
        }
        
        // a collection of endpoint specifications so that we can test that 
        // only one is present
        List<String> endpointSpecifications = new ArrayList<String>();

        // Read URI
        String uri = getURIString(reader, URI);
        if (uri != null) {
            wsBinding.setURI(uri);
            wsBinding.setUserSpecifiedURI(uri);
            
            // BWS20001
            if (context.getParentModel() instanceof Reference){
                try {
                    URI tmpURI = new URI(uri);
                    
                    if (!tmpURI.isAbsolute()){
                        error(monitor, "URINotAbsolute", reader, uri);
                    }
                } catch (URISyntaxException ex){
                    error(monitor, "InvalidURISyntax", reader, ex.getMessage());
                }
                endpointSpecifications.add("uri");
            }
            
            // BWS20020
            if ((context.getParentModel() instanceof Callback) &&
                (((Callback)context.getParentModel()).getParentContract() instanceof org.apache.tuscany.sca.assembly.Service)){
                error(monitor, "URIFoundForServiceCallback", reader, uri);
            }
        }

        // Read a qname in the form:
        // namespace#wsdl.???(name)
        Boolean wsdlElementIsBinding = null;
        String wsdlElement = getURIString(reader, WSDL_ELEMENT);
        if (wsdlElement != null) {
            int index = wsdlElement.indexOf('#');
            if (index == -1) {
            	error(monitor, "InvalidWsdlElementAttr", reader, wsdlElement);
                //throw new ContributionReadException("Invalid WebService binding wsdlElement attribute: " + wsdlElement);
            	return wsBinding;
            }
            String namespace = wsdlElement.substring(0, index);
            wsBinding.setNamespace(namespace);
            String localName = wsdlElement.substring(index + 1);
            if (localName.startsWith("wsdl.service")) {

                // BWS20003
                if (context.getParentModel() instanceof org.apache.tuscany.sca.assembly.Service){
                    error(monitor, "WSDLServiceOnService", reader, wsdlElement);
                }
                
                // Read a wsdl.service
                localName = localName.substring("wsdl.service(".length(), localName.length() - 1);
                wsBinding.setServiceName(new QName(namespace, localName));
                
                endpointSpecifications.add("#wsdl.service");

            } else if (localName.startsWith("wsdl.port")) {

                // Read a wsdl.port
                localName = localName.substring("wsdl.port(".length(), localName.length() - 1);
                int s = localName.indexOf('/');
                if (s == -1) {
                	error(monitor, "InvalidWsdlElementAttr", reader, wsdlElement);
                    //throw new ContributionReadException("Invalid WebService binding wsdlElement attribute: " + wsdlElement);
                } else {
                    wsBinding.setServiceName(new QName(namespace, localName.substring(0, s)));
                    wsBinding.setPortName(localName.substring(s + 1));
                }
                
                endpointSpecifications.add("#wsdl.port");
            } else if (localName.startsWith("wsdl.endpoint")) {

                // Read a wsdl.endpoint
                localName = localName.substring("wsdl.endpoint(".length(), localName.length() - 1);
                int s = localName.indexOf('/');
                if (s == -1) {
                	error(monitor, "InvalidWsdlElementAttr", reader, wsdlElement);
                    //throw new ContributionReadException("Invalid WebService binding wsdlElement attribute: " + wsdlElement);
                } else {
                    wsBinding.setServiceName(new QName(namespace, localName.substring(0, s)));
                    wsBinding.setEndpointName(localName.substring(s + 1));
                }
                
            } else if (localName.startsWith("wsdl.binding")) {

                // Read a wsdl.binding
                localName = localName.substring("wsdl.binding(".length(), localName.length() - 1);
                wsBinding.setBindingName(new QName(namespace, localName));

                wsdlElementIsBinding = true;

            } else {
            	error(monitor, "InvalidWsdlElementAttr", reader, wsdlElement);
                //throw new ContributionReadException("Invalid WebService binding wsdlElement attribute: " + wsdlElement);
            }
        }

        // Read wsdlLocation
        String wsdliLocation = reader.getAttributeValue(WSDLI_NS, WSDL_LOCATION);
        if (wsdliLocation != null) {
            if (wsdlElement == null) {
                error(monitor, "WsdliLocationMissingWsdlElement", reader);
            }
            String[] iris = wsdliLocation.split(" ");
            if (iris.length % 2 != 0) {
                error(monitor, "WsdliLocationNotIRIPairs", reader);
            }
            for (int i=0; i<iris.length-1; i=i+2) {
                wsBinding.getWsdliLocations().put(iris[i], iris[i+1]);
            }
        }

        //add binding extensions
        QName elementName = reader.getName();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            QName attributeName = reader.getAttributeName(i);
            if(attributeName.getNamespaceURI() != null && attributeName.getNamespaceURI().length() > 0 && !(attributeName.getNamespaceURI().equals(WSDLI_NS))) {                
                if(!elementName.getNamespaceURI().equals(attributeName.getNamespaceURI()) ) {
                    Object attributeValue = extensionAttributeProcessor.read(attributeName, reader, processorContext);
                    Extension attributeExtension;
                    if (attributeValue instanceof Extension) {
                        attributeExtension = (Extension)attributeValue;
                    } else {
                        attributeExtension = assemblyFactory.createExtension();
                        attributeExtension.setQName(attributeName);
                        attributeExtension.setValue(attributeValue);
                        attributeExtension.setAttribute(true);
                    }
                    ((Extensible)wsBinding).getAttributeExtensions().add(attributeExtension);
                }
            }
        }
        // Skip to end element
        while (reader.hasNext()) {
            int event = reader.next();
            switch (event) {
                case START_ELEMENT: {
                    if (END_POINT_REFERENCE.equals(reader.getName().getLocalPart())) {
                        if (wsdlElement != null && (wsdlElementIsBinding == null || !wsdlElementIsBinding)) {
                        	error(monitor, "MustUseWsdlBinding", reader, wsdlElement);
                            String message = context.getMonitor().getMessageString(WebServiceBindingProcessor.class.getName(),
                                                                                   "binding-wsxml-validation-messages", 
                                                                                   "MustUseWsdlBinding");
                            message = message.replace("{0}", wsdlElement);
                            throw new ContributionReadException(message);
                        }
                        
                        wsBinding.setEndPointReference(EndPointReferenceHelper.readEndPointReference(reader));
                        endpointSpecifications.add("wsa:EndpointReference");
                    } 
                }
                    break;

            }

            if (event == END_ELEMENT && BINDING_WS_QNAME.equals(reader.getName())) {
                break;
            }
        }
        
        if (endpointSpecifications.size() > 1){
            error(monitor, "MultipleEndpointsSpecified", reader, endpointSpecifications.toString() );
        }
        
        return wsBinding;
    }

    protected void processEndPointReference(XMLStreamReader reader, WebServiceBinding wsBinding) {
    }

    public void write(WebServiceBinding wsBinding, XMLStreamWriter writer, ProcessorContext context) throws ContributionWriteException,
        XMLStreamException {

        // Write a <binding.ws>
        writer.writeStartElement(SCA11_NS, BINDING_WS);
        policyProcessor.writePolicyAttributes(wsBinding, writer);

        // Write binding name
        if (wsBinding.getName() != null) {
            writer.writeAttribute(NAME, wsBinding.getName());
        }

        // Write binding URI
        if (wsBinding.getURI() != null) {
            writer.writeAttribute(URI, wsBinding.getURI());
        }

        // Write wsdlElement attribute
        if (wsBinding.getPortName() != null) {

            // Write namespace#wsdl.port(service/port)
            String wsdlElement =
                wsBinding.getServiceName().getNamespaceURI() + "#wsdl.port("
                    + wsBinding.getServiceName().getLocalPart()
                    + "/"
                    + wsBinding.getPortName()
                    + ")";
            writer.writeAttribute(WSDL_ELEMENT, wsdlElement);

        } else if (wsBinding.getEndpointName() != null) {

            // Write namespace#wsdl.endpoint(service/endpoint)
            String wsdlElement =
                wsBinding.getServiceName().getNamespaceURI() + "#wsdl.endpoint("
                    + wsBinding.getServiceName().getLocalPart()
                    + "/"
                    + wsBinding.getEndpointName()
                    + ")";
            writer.writeAttribute(WSDL_ELEMENT, wsdlElement);

        } else if (wsBinding.getBindingName() != null) {

            // Write namespace#wsdl.binding(binding)
            String wsdlElement =
                wsBinding.getBindingName().getNamespaceURI() + "#wsdl.binding("
                    + wsBinding.getBindingName().getLocalPart()
                    + ")";
            writer.writeAttribute(WSDL_ELEMENT, wsdlElement);

        } else if (wsBinding.getServiceName() != null) {

            // Write namespace#wsdl.service(service)
            String wsdlElement =
                wsBinding.getServiceName().getNamespaceURI() + "#wsdl.service("
                    + wsBinding.getServiceName().getLocalPart()
                    + ")";
            writer.writeAttribute(WSDL_ELEMENT, wsdlElement);
        }

        // Write wsdli:location
        if (wsBinding.getWsdliLocations().size() > 0) {
            StringBuilder wsdliLocation = new StringBuilder();
            Map<String, String> wl = wsBinding.getWsdliLocations();
            for (String ns : wl.keySet()) {
                if (wsdliLocation.length() > 0) {
                    wsdliLocation.append(' ');
                }
                wsdliLocation.append(ns); 
                wsdliLocation.append(' '); 
                wsdliLocation.append(wl.get(ns)); 
            }
            writer.writeAttribute(WSDLI_NS, WSDL_LOCATION, wsdliLocation.toString());
        }

        // Write extended attributes
        for(Extension extension : ((Extensible)wsBinding).getAttributeExtensions()) {
            if(extension.isAttribute()) {
                extensionAttributeProcessor.write(extension, writer, processorContext);
            }
        }
        if (wsBinding.getEndPointReference() != null) {
            EndPointReferenceHelper.writeEndPointReference(wsBinding.getEndPointReference(), writer, staxHelper);
        }

        writer.writeEndElement();
    }

    public void resolve(WebServiceBinding model, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        
    	if (model == null || !model.isUnresolved())
    		return;
    	Monitor monitor = context.getMonitor();	
    	WSDLDefinition wsdlDefinition = wsdlFactory.createWSDLDefinition();
        wsdlDefinition.setUnresolved(true);
        wsdlDefinition.setNamespace(model.getNamespace());
        wsdlDefinition.setNameOfBindingToResolve(model.getBindingName());
        wsdlDefinition.setNameOfServiceToResolve(model.getServiceName());
        wsdlDefinition.getWsdliLocations().putAll(model.getWsdliLocations());
        //WSDLDefinition resolved = resolver.resolveModel(WSDLDefinition.class, wsdlDefinition, context);
        WSDLDefinition resolved = null;
        try {
            resolved = resolver.resolveModel(WSDLDefinition.class, wsdlDefinition, context);
        } catch (ContributionRuntimeException e) {
            ContributionResolveException ce = new ContributionResolveException(e.getCause());
            error(monitor, "ContributionResolveException", wsdlDefinition, ce);
         } 

        if (resolved != null && !resolved.isUnresolved()) {
            wsdlDefinition.setDefinition(resolved.getDefinition());
            wsdlDefinition.setLocation(resolved.getLocation());
            wsdlDefinition.setURI(resolved.getURI());
            wsdlDefinition.getImportedDefinitions().addAll(resolved.getImportedDefinitions());
            wsdlDefinition.getXmlSchemas().addAll(resolved.getXmlSchemas());
            wsdlDefinition.setUnresolved(false);
            model.setUserSpecifiedWSDLDefinition(wsdlDefinition);
            if (model.getBindingName() != null) {
                WSDLObject<Binding> binding = wsdlDefinition.getWSDLObject(Binding.class, model.getBindingName());
                if (binding != null) {
                    wsdlDefinition.setDefinition(binding.getDefinition());
                    model.setBinding(binding.getElement());
                } else {
                	error(monitor, "WsdlBindingDoesNotMatch", wsdlDefinition, model.getBindingName());
                }
            }
            if (model.getServiceName() != null) {
                WSDLObject<Service> service = wsdlDefinition.getWSDLObject(Service.class, model.getServiceName());
                if (service != null) {
                    wsdlDefinition.setDefinition(service.getDefinition());
                    model.setService(service.getElement());
                    
                    Port port = null;
                    if (model.getPortName() != null) {
                        port = service.getElement().getPort(model.getPortName());
                    } else {
                        // BWS20006 - no port specified so pick the first one
                        port = (Port)service.getElement().getPorts().values().iterator().next();
                    }
                    
                    if (port != null) {
                        model.setPort(port);
                        model.setBinding(port.getBinding());
                        
                        // if no URI specified set it from the WSDL port location
                        if (model.getURI() == null){
                            model.setURI(getPortAddress(port));
                            model.setUserSpecifiedURI(model.getURI());
                        }
                    } else {
                        error(monitor, "WsdlPortTypeDoesNotMatch", wsdlDefinition, model.getPortName());
                    }
                } else {
                	error(monitor, "WsdlServiceDoesNotMatch", wsdlDefinition, model.getServiceName());
                }
            }

            PortType portType = getPortType(model);
            if (portType != null) {
                WSDLInterfaceContract interfaceContract = wsdlFactory.createWSDLInterfaceContract();
                WSDLInterface wsdlInterface = null;
                try {
                    wsdlInterface = wsdlFactory.createWSDLInterface(portType, wsdlDefinition, resolver, context.getMonitor());
                    // save the wsdlDefinition that was used to generate the interface
                    wsdlInterface.setWsdlDefinition(wsdlDefinition);
                    interfaceContract.setInterface(wsdlInterface);
                    interfaceContract.setCallbackInterface(wsdlInterface.getCallbackInterface());
                    model.setBindingInterfaceContract(interfaceContract);
                } catch (InvalidInterfaceException e) {
                	warning(monitor, "InvalidInterfaceException", wsdlFactory, model.getName(), e.getMessage()); 
                }
            }
            
            validateWSDL(context, model);
        } else {
            if (model.getBindingName() != null){
                error(monitor, "WsdlBindingDoesNotMatch", model, model.getBindingName());
            }
            
            if (model.getServiceName() != null){
                error(monitor, "WsdlServiceDoesNotMatch", model, model.getServiceName());
            }
        }
        
        policyProcessor.resolvePolicies(model, resolver, context);
    }

    private void validateWSDL(ProcessorContext context, WebServiceBinding model) {
        WSDLDefinition wsdlDefinition = model.getUserSpecifiedWSDLDefinition();
        
        Port port = model.getPort();
        
        if (port != null){
            validateWSDLPort(context, model, port);
        } 
        
        Binding binding = model.getBinding();
        
        if (binding != null){
            validateWSDLBinding(context, model, binding);
        } 
    }
    
    private void validateWSDLPort(ProcessorContext context, WebServiceBinding model, Port port){
        
        validateWSDLBinding(context, model, port.getBinding());
        
    }
    
    private void validateWSDLBinding(ProcessorContext context, WebServiceBinding model, Binding binding){
        // BWS20005 & BWS20010 
        // Check that the WSDL binding is of a supported type
        if (!model.isHTTPTransport() && !model.isJMSTransport()){
            error(context.getMonitor(), 
                  "InvalidWSDLBindingTransport", 
                  model, 
                  model.getBindingTransport());
        }
    }
    
    private PortType getPortType(WebServiceBinding model) {
        PortType portType = null;
        if (model.getPort() != null) {
            portType = model.getPort().getBinding().getPortType();
        } else if (model.getEndpoint() != null) {
            portType = model.getPort().getBinding().getPortType();
        } else if (model.getBinding() != null) {
            portType = model.getBinding().getPortType();
        } else if (model.getService() != null) {
            // FIXME: How to find the compatible port?
            Map ports = model.getService().getPorts();
            if (!ports.isEmpty()) {
                Port port = (Port)ports.values().iterator().next();
                portType = port.getBinding().getPortType();
            }
        }
        return portType;
    }
    
    public static String getPortAddress(Port port) {
        Object ext = port.getExtensibilityElements().get(0);
        if (ext instanceof SOAPAddress) {
            return ((SOAPAddress)ext).getLocationURI();
        }
        if (ext instanceof SOAP12Address) {
            return ((SOAP12Address)ext).getLocationURI();
        }
        return null;
    }    

    public QName getArtifactType() {
        return WebServiceConstants.BINDING_WS_QNAME;
    }

    public Class<WebServiceBinding> getModelType() {
        return WebServiceBinding.class;
    }

}
