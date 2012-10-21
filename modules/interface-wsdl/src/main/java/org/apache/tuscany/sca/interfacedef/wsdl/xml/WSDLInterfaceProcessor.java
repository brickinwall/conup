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

package org.apache.tuscany.sca.interfacedef.wsdl.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;

import java.net.URI;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.xml.PolicySubjectProcessor;
import org.apache.tuscany.sca.contribution.Artifact;
import org.apache.tuscany.sca.contribution.processor.BaseStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionRuntimeException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
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
 * Handles a <interface.wsdl ... /> element in a SCDL file
 * @version $Rev: 1295564 $ $Date: 2012-03-01 13:58:17 +0000 (Thu, 01 Mar 2012) $
 */
public class WSDLInterfaceProcessor extends BaseStAXArtifactProcessor implements StAXArtifactProcessor<WSDLInterfaceContract>, WSDLConstants {

    private WSDLFactory wsdlFactory;
    private InterfaceContractMapper interfaceContractMapper;
    private PolicyFactory policyFactory;
    private PolicySubjectProcessor policyProcessor;

    public WSDLInterfaceProcessor(ExtensionPointRegistry registry) {
        FactoryExtensionPoint modelFactories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        this.interfaceContractMapper =
            registry.getExtensionPoint(UtilityExtensionPoint.class).getUtility(InterfaceContractMapper.class);
        
        this.wsdlFactory = modelFactories.getFactory(WSDLFactory.class);
        
        this.policyFactory = modelFactories.getFactory(PolicyFactory.class);
        this.policyProcessor = new PolicySubjectProcessor(policyFactory);      
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
            Problem problem = monitor.createProblem(this.getClass().getName(), "interface-wsdlxml-validation-messages", Severity.WARNING, model, message, (Object[])messageParameters);
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
            Problem problem = monitor.createProblem(this.getClass().getName(), "interface-wsdlxml-validation-messages", Severity.ERROR, model, message, (Object[])messageParameters);
            monitor.problem(problem);
        }
     }
   
   /**
    * Report a exception.
    * 
    * @param problems
    * @param message
    * @param model
    */
    private void error(Monitor monitor, String message, Object model, Exception ex) {
        if (monitor != null) {
            Problem problem = monitor.createProblem(this.getClass().getName(), "interface-wsdlxml-validation-messages", Severity.ERROR, model, message, ex);
            monitor.problem(problem);
        }        
    }
    
    /**
     * Create a WSDL interface from a URI.
     * @param uri - the URI in the form nameSpace#wsdl.interface(porttypeName) or nameSpace#wsdl.porttype(porttypeName)
     * @return a WSDLInterface object 
     * @throws ContributionReadException
     */
    private static String FRAGMENT_INTERFACE = "wsdl.interface";
    private static String FRAGMENT_PORTTYPE = "wsdl.porttype";
    private WSDLInterface createWSDLInterface(String uri, Monitor monitor) throws ContributionReadException {
        
    	WSDLInterface wsdlInterface = null;        

        // Read a QName in the form:
        // namespace#wsdl.interface(name)
        int index = uri.indexOf('#');
        if (index == -1) {
        	error(monitor, "InvalidWSDLInterfaceAttr", wsdlFactory, uri);
            //throw new ContributionReadException("Invalid WSDL interface attribute: " + uri);
        } else {
        	// Read the URI and extract namespace and fragment
        	String namespace = uri.substring(0, index);
        	String name = uri.substring(index + 1);
        	String porttype = null;
        	if( name.contains(FRAGMENT_INTERFACE)) {
        		// Deal with the case where #wsdl.interface is used
        		porttype = name.substring("wsdl.interface(".length(), name.length() - 1);
        	} // end if
        	if( name.contains(FRAGMENT_PORTTYPE)) {
        		// Deal with the case where #wsdl.porttype is used
        		porttype = name.substring("wsdl.porttype(".length(), name.length() - 1);
        	} // end if
        	if( porttype == null ) {
        		error(monitor, "InvalidWSDLInterfaceAttr", wsdlFactory, uri);
        		return null;
        	} // end if
        	wsdlInterface = wsdlFactory.createWSDLInterface();
            wsdlInterface.setUnresolved(true);
            wsdlInterface.setName(new QName(namespace, porttype));
        } // end if       
        
        return wsdlInterface;
    } // end method createWSDLInterface

    /**
     * Creates a WSDLInterfaceContract from a <interface.wsdl/> element in a SCDL file
     * 
     * The form of the <interface.wsdl/> element is as follows:
     * 
     * <interface.wsdl interface="http://sampleNamespace#wsdl.interface(porttypeName)"
     *                 callbackInterface="http://sampleNamespace#wsdl.porttype(callbackPorttypeName)"/>
     * where interface = URI pointing to the WSDL document containing a WSDL interface or porttype for the forward call interface
     *       callbackInterface = URI pointing to the WSDL document containing a WSDL interface or porttype for the callback interface
     * 
     * @param reader - XMLStreamReader holding the <interface.wsdl/> element
     * @return - the WSDLInterfaceContract
     */
    public WSDLInterfaceContract read(XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {
        // Read an <interface.wsdl>
        WSDLInterfaceContract wsdlInterfaceContract = wsdlFactory.createWSDLInterfaceContract();
        Monitor monitor = context.getMonitor();
        
        // Read wsdlLocation
        String location = reader.getAttributeValue(WSDLI_NS, WSDL_LOCATION);
        wsdlInterfaceContract.setLocation(location);
        
        String uri = getURIString(reader, INTERFACE);
        if (uri != null) {
            WSDLInterface wsdlInterface = createWSDLInterface(uri, monitor);
            if (wsdlInterface != null)
                wsdlInterfaceContract.setInterface(wsdlInterface);
        }
        
        uri = getURIString(reader, CALLBACK_INTERFACE);
        if (uri != null) {
            WSDLInterface wsdlCallbackInterface = createWSDLInterface(uri, monitor);
            if (wsdlCallbackInterface != null)
                wsdlInterfaceContract.setCallbackInterface(wsdlCallbackInterface);
        }
        
        String remotable = reader.getAttributeValue(null, REMOTABLE);
        if (remotable != null &&
            !remotable.equals("true")){
            Monitor.error(monitor,
                          this,
                          "interface-wsdlxml-validation-messages",
                          "InvalidRemotableValue", 
                          ((WSDLInterface)wsdlInterfaceContract.getInterface()).getName().toString(),
                          remotable);
        }
        
        // Read intents and policy sets
        policyProcessor.readPolicies(wsdlInterfaceContract.getInterface(), reader);
            
        // Skip to end element
        while (reader.hasNext()) {
            if (reader.next() == END_ELEMENT && INTERFACE_WSDL_QNAME.equals(reader.getName())) {
                break;
            }
        }
        return wsdlInterfaceContract;
    }
    
    public void write(WSDLInterfaceContract wsdlInterfaceContract, XMLStreamWriter writer, ProcessorContext context) throws ContributionWriteException, XMLStreamException {
        // Write an <interface.wsdl>
        writer.writeStartElement(WSDLConstants.SCA11_NS, INTERFACE_WSDL);

        // Write interface name
        WSDLInterface wsdlInterface = (WSDLInterface)wsdlInterfaceContract.getInterface();
        if (wsdlInterface != null) {
            QName qname = wsdlInterface.getName();
            String uri = qname.getNamespaceURI() + "#wsdl.interface(" + qname.getLocalPart() + ")";
            writer.writeAttribute(INTERFACE, uri);
        }

        WSDLInterface wsdlCallbackInterface = (WSDLInterface)wsdlInterfaceContract.getCallbackInterface();
        if (wsdlCallbackInterface != null) {
            QName qname = wsdlCallbackInterface.getName();
            String uri = qname.getNamespaceURI() + "#wsdl.interface(" + qname.getLocalPart() + ")";
            writer.writeAttribute(CALLBACK_INTERFACE, uri);
        }
        
        // Write location
        if (wsdlInterfaceContract.getLocation() != null) {
            writer.writeAttribute(WSDLI_NS, WSDL_LOCATION, wsdlInterfaceContract.getLocation());
        }
        
        policyProcessor.writePolicyAttributes(wsdlInterface, writer);
        
        writer.writeEndElement();
    }
    
    private WSDLInterface resolveWSDLInterface(WSDLInterface wsdlInterface, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        
        if (wsdlInterface != null && wsdlInterface.isUnresolved()) {
            Monitor monitor = context.getMonitor();
            // Resolve the WSDL interface
            wsdlInterface = resolver.resolveModel(WSDLInterface.class, wsdlInterface, context);
            if (wsdlInterface.isUnresolved()) {

                // If the WSDL interface has never been resolved yet, do it now
                // First, resolve the WSDL definition for the given namespace
                WSDLDefinition wsdlDefinition = wsdlFactory.createWSDLDefinition();
                wsdlDefinition.setUnresolved(true);
                wsdlDefinition.setNamespace(wsdlInterface.getName().getNamespaceURI());
                wsdlDefinition.setNameOfPortTypeToResolve(wsdlInterface.getName());
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
                    WSDLObject<PortType> portType = wsdlDefinition.getWSDLObject(PortType.class, wsdlInterface.getName());
                    if (portType != null) {                        
                        // Introspect the WSDL portType and add the resulting
                        // WSDLInterface to the resolver
                        try {
                            wsdlDefinition.setDefinition(portType.getDefinition());
                            WSDLInterface newWSDLInterface = wsdlFactory.createWSDLInterface(portType.getElement(), wsdlDefinition, resolver, monitor);
                            newWSDLInterface.setWsdlDefinition(wsdlDefinition);
                            newWSDLInterface.getRequiredIntents().addAll(wsdlInterface.getRequiredIntents());
                            newWSDLInterface.getPolicySets().addAll(wsdlInterface.getPolicySets());
                            resolver.addModel(newWSDLInterface, context);
                            wsdlInterface = newWSDLInterface;
                        } catch (InvalidInterfaceException e) {
                            String message = context.getMonitor().getMessageString(WSDLInterfaceProcessor.class.getName(),
                                                                                   "interface-wsdlxml-validation-messages", 
                                                                                   "InvalidInterface");
                            message = message.replace("{0}",  portType.toString());

                        	ContributionResolveException ce = new ContributionResolveException(message, e);
                        	error(monitor, "ContributionResolveException", wsdlFactory, ce);
                            //throw ce;
                        } // end try                      
                    }
                    else {
                    	warning(monitor, "WsdlInterfaceDoesNotMatch", wsdlDefinition, wsdlInterface.getName());
                    } // end if
                } else {
                	// If we get here, the WSDLDefinition is unresolved...
                    String message = context.getMonitor().getMessageString(WSDLInterfaceProcessor.class.getName(),
                                                                           "interface-wsdlxml-validation-messages", 
                                                                           "WSDLDefinitionUnresolved");
                    message = message.replace("{0}",  wsdlInterface.getName().toString());

                	ContributionResolveException ce = new ContributionResolveException(message);
                    error(monitor, "ContributionResolveException", wsdlFactory, ce);
                } // end if
            } // end if
        } // end if
        return wsdlInterface;
    }
    
    public static WSDLInterface resolveWSDLInterface( WSDLInterface wsdlInterface, ModelResolver resolver, 
    		                                   Monitor monitor, WSDLFactory wsdlFactory) {
        if (wsdlInterface != null && wsdlInterface.isUnresolved()) {

            ProcessorContext context = new ProcessorContext(monitor);
            // Resolve the WSDL interface
            wsdlInterface = resolver.resolveModel(WSDLInterface.class, wsdlInterface, context);
            if (wsdlInterface.isUnresolved()) {

                // If the WSDL interface has never been resolved yet, do it now
                // First, resolve the WSDL definition for the given namespace
                WSDLDefinition wsdlDefinition = wsdlFactory.createWSDLDefinition();
                wsdlDefinition.setUnresolved(true);
                wsdlDefinition.setNamespace(wsdlInterface.getName().getNamespaceURI());
                WSDLDefinition resolved = resolver.resolveModel(WSDLDefinition.class, wsdlDefinition, context);
                if (!resolved.isUnresolved()) {
                    wsdlDefinition.setDefinition(resolved.getDefinition());
                    wsdlDefinition.setLocation(resolved.getLocation());
                    wsdlDefinition.setURI(resolved.getURI());
                    wsdlDefinition.getImportedDefinitions().addAll(resolved.getImportedDefinitions());
                    wsdlDefinition.getXmlSchemas().addAll(resolved.getXmlSchemas());
                    wsdlDefinition.setUnresolved(false);
                    WSDLObject<PortType> portType = wsdlDefinition.getWSDLObject(PortType.class, wsdlInterface.getName());
                    if (portType != null) {                        
                        // Introspect the WSDL portType and add the resulting
                        // WSDLInterface to the resolver
                        try {
                            wsdlDefinition.setDefinition(portType.getDefinition());
                            wsdlInterface = wsdlFactory.createWSDLInterface(portType.getElement(), wsdlDefinition, resolver, monitor);
                            wsdlInterface.setWsdlDefinition(wsdlDefinition);
                            resolver.addModel(wsdlInterface, context);
                        } catch (InvalidInterfaceException e) {
                            String message = context.getMonitor().getMessageString(WSDLInterfaceProcessor.class.getName(),
                                                                                   "interface-wsdlxml-validation-messages", 
                                                                                   "InvalidInterface");
                            message = message.replace("{0}",  portType.toString());
                        	ContributionResolveException ce = new ContributionResolveException(message, e);
                        	Monitor.error(monitor, WSDLInterfaceProcessor.class.getName(), 
                        			"interface-wsdlxml-validation-messages", "ContributionResolveException", 
                        			wsdlFactory.getClass().getName(), ce.getMessage());
                            //throw ce;
                        } // end try                      
                    }
                    else {
                    	Monitor.warning(monitor, WSDLInterfaceProcessor.class.getName(),
                    			"interface-wsdlxml-validation-messages", "WsdlInterfaceDoesNotMatch", 
                    			wsdlDefinition.getNamespace(), wsdlInterface.getName().toString() );
                    } // end if
                } else {
                	// If we get here, the WSDLDefinition is unresolved...
                    String message = context.getMonitor().getMessageString(WSDLInterfaceProcessor.class.getName(),
                                                                           "interface-wsdlxml-validation-messages", 
                                                                           "WSDLDefinitionUnresolved");
                    message = message.replace("{0}",  wsdlInterface.getName().toString());
                	ContributionResolveException ce = new ContributionResolveException(message);
                    Monitor.error(monitor, WSDLInterfaceProcessor.class.getName(), 
                			"interface-wsdlxml-validation-messages", "ContributionResolveException", 
                			wsdlFactory.getClass().getName(), ce.getMessage());
                } // end if
            } // end if
        } // end if
        return wsdlInterface;    	
    } // end method resolveWSDLInterface
    
    /**
     * Resolve a WSDLInterfaceContract
     */
    public void resolve(WSDLInterfaceContract wsdlInterfaceContract, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        Monitor monitor = context.getMonitor();
        
        WSDLInterface wsdlInterface = (WSDLInterface)wsdlInterfaceContract.getInterface();
        
        // if the contract has a location but no WSDL definition yet we need to read the WSDL
        // from the specified location and create an interface based on the first port type
        // this is required if the user uses the @WebService(wsdlLocation="") or 
        // @WebServiceProvider(wsdlLocation="") annotation in a Java component implementation. 
        if (wsdlInterfaceContract.getLocation() != null &&
            wsdlInterface.getWsdlDefinition() == null){
            
            WSDLDefinition wsdlDefinition = null;
 
            URI wsdlFileURI = null;
            
            try {
                wsdlFileURI = new URI(wsdlInterfaceContract.getLocation());
            } catch (Exception ex) {
                Monitor.error(context.getMonitor(), 
                        WSDLInterfaceProcessor.class.getName(), 
                        "interface-wsdlxml-validation-messages", 
                        "wsdliLocationException", 
                        ex.getMessage() );
                return;
            }
            
            // We need to find a portType from the user specified WSDL (the first one?) from which to defined
            // the service interface. We can't just use the Tuscany resolution mechanism to find the WSDL file
            // as that lumps together all WSDL in the same namespace. That's fine if you already know what portType
            // your after. In this case we don't so we have to get the WSDL specified, find out what it's first portType
            // is and then go from there with the usual Tuscany resolution mechanism
            try {
                if (wsdlFileURI.isAbsolute()){
                    // use the wsdli:wsdlLocation mechanism in the WSDLModelResolver to 
                    // load the WSDL from an absolute location                
                    wsdlDefinition = wsdlFactory.createWSDLDefinition();
                    wsdlDefinition.setUnresolved(true);
                    wsdlDefinition.setNamespace("nonamespace"); 
                    wsdlDefinition.getWsdliLocations().put("nonamespace", wsdlInterfaceContract.getLocation());
                    wsdlDefinition.setLocation(new URI(wsdlInterfaceContract.getLocation()));
                } else {
                    // Find the wsdl in the contribution ready for further resolution
                    for (Artifact artifact : context.getContribution().getArtifacts()) {
                        if (artifact.getLocation().endsWith(wsdlInterfaceContract.getLocation())){
                            WSDLDefinition artifactWSDLDefinition = artifact.getModel();
                            wsdlDefinition = wsdlFactory.createWSDLDefinition();
                            wsdlDefinition.setUnresolved(true);
                            wsdlDefinition.setNamespace(artifactWSDLDefinition.getNamespace()); 
                            wsdlDefinition.getWsdliLocations().put(artifactWSDLDefinition.getNamespace(), 
                                                                   artifact.getLocation());
                            wsdlDefinition.setLocation(new URI(artifact.getLocation()));
                            break;
                        }
                    }
                    
                    if (wsdlDefinition == null){
                        Monitor.error(context.getMonitor(), 
                                WSDLInterfaceProcessor.class.getName(), 
                                "interface-wsdlxml-validation-messages", 
                                "wsdliLocationException", 
                                "WSDL not found inside contribution at relative URI " + wsdlFileURI );
                        return;
                    }
                }
            } catch (Exception ex) {
                Monitor.error(context.getMonitor(), 
                              WSDLInterfaceProcessor.class.getName(), 
                              "interface-wsdlxml-validation-messages", 
                              "wsdliLocationException", 
                              ex.getMessage() );
                return;
            }
            
            wsdlDefinition.setUnresolved(true);
            wsdlDefinition = resolver.resolveModel(WSDLDefinition.class, wsdlDefinition, context);
            // create the interface based on the first port type
            PortType portType = (PortType)wsdlDefinition.getDefinition().getAllPortTypes().values().iterator().next();
            try {
                WSDLInterface newWSDLInterface = wsdlFactory.createWSDLInterface(portType, wsdlDefinition, resolver, monitor);
                newWSDLInterface.getRequiredIntents().addAll(wsdlInterface.getRequiredIntents());
                newWSDLInterface.getPolicySets().addAll(wsdlInterface.getPolicySets());
                wsdlInterface = newWSDLInterface;
            } catch (InvalidInterfaceException e) {
                String message = context.getMonitor().getMessageString(WSDLInterfaceProcessor.class.getName(),
                                        "interface-wsdlxml-validation-messages", 
                                        "InvalidInterface");
                message = message.replace("{0}",  portType.toString());
                ContributionResolveException ce = new ContributionResolveException(message, e);
                error(monitor, "ContributionResolveException", wsdlFactory, ce);
            }    
            
            wsdlInterface.setWsdlDefinition(wsdlDefinition);
            wsdlInterfaceContract.setInterface(wsdlInterface);
        }
        
        // Resolve the interface and callback interface
        wsdlInterface = resolveWSDLInterface(wsdlInterface, resolver, context);
        wsdlInterfaceContract.setInterface(wsdlInterface);
        
        // The forward interface (portType) may have a callback interface declared on it using an sca:callback attribute
        WSDLInterface intrinsicWSDLCallbackInterface = wsdlInterface.getCallbackInterface();
        
        // There may be a callback interface explicitly declared on the <interface.wsdl .../> element
        WSDLInterface wsdlCallbackInterface = resolveWSDLInterface((WSDLInterface)wsdlInterfaceContract.getCallbackInterface(), resolver, context);
        if( intrinsicWSDLCallbackInterface != null ) {
        	if( wsdlCallbackInterface != null ) {
        		// If there is both a callback interface declared on the forward interface and also one declared on the
        		// interface.wsdl element, then the two interfaces must match [ASM80011]
        		if( !interfaceContractMapper.isMutuallyCompatible(intrinsicWSDLCallbackInterface, wsdlCallbackInterface) ) {
                    Monitor.error(context.getMonitor(), WSDLInterfaceProcessor.class.getName(), 
                			"interface-wsdlxml-validation-messages", "IncompatibleCallbacks", 
                			intrinsicWSDLCallbackInterface.getName().toString(), 
                			wsdlCallbackInterface.getName().toString() );
        		} // end if
        	} // end if
        	wsdlInterfaceContract.setCallbackInterface(intrinsicWSDLCallbackInterface);
        } else {
        	wsdlInterfaceContract.setCallbackInterface(wsdlCallbackInterface);
        } // end if
    } // end method resolve( WSDLInterfaceContract, ModelResolver)
    
    public QName getArtifactType() {
        return WSDLConstants.INTERFACE_WSDL_QNAME;
    }
    
    public Class<WSDLInterfaceContract> getModelType() {
        return WSDLInterfaceContract.class;
    }
}
