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
package org.apache.tuscany.sca.implementation.bpel.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Multiplicity;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.contribution.processor.BaseStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.implementation.bpel.BPELFactory;
import org.apache.tuscany.sca.implementation.bpel.BPELImplementation;
import org.apache.tuscany.sca.implementation.bpel.BPELProcessDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLFactory;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterfaceContract;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;

/**
 * Implements a StAX artifact processor for BPEL implementations.
 * 
 * The artifact processor is responsible for processing <implementation.bpel>
 * elements in SCA assembly XML composite files and populating the BPEL
 * implementation model, resolving its references to other artifacts in the SCA
 * contribution, and optionally write the model back to SCA assembly XML.
 * 
 *  @version $Rev: 889531 $ $Date: 2009-12-11 08:26:48 +0000 (Fri, 11 Dec 2009) $
 */
public class BPELImplementationProcessor extends BaseStAXArtifactProcessor implements StAXArtifactProcessor<BPELImplementation> {
    private static final String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200912";
    private static final String PROCESS = "process";
    private static final String IMPLEMENTATION_BPEL = "implementation.bpel";
    private static final QName IMPLEMENTATION_BPEL_QNAME = new QName(SCA11_NS, IMPLEMENTATION_BPEL);
    
    private AssemblyFactory assemblyFactory;
    private BPELFactory bpelFactory;
    private WSDLFactory wsdlFactory;
    
    
    public BPELImplementationProcessor(FactoryExtensionPoint modelFactories) {
        this.assemblyFactory = modelFactories.getFactory(AssemblyFactory.class);
        this.wsdlFactory = modelFactories.getFactory(WSDLFactory.class);
        this.bpelFactory = modelFactories.getFactory(BPELFactory.class);
    }

    public QName getArtifactType() {
        // Returns the QName of the XML element processed by this processor
        return IMPLEMENTATION_BPEL_QNAME;
    }

    public Class<BPELImplementation> getModelType() {
        // Returns the type of model processed by this processor
        return BPELImplementation.class;
    }

    public BPELImplementation read(XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {
        assert IMPLEMENTATION_BPEL_QNAME.equals(reader.getName());
        
        // Read an <implementation.bpel> element
        BPELImplementation implementation = null;
        
        // Read the process attribute. 
        QName process = getAttributeValueNS(reader, PROCESS, context.getMonitor());
        if (process == null) {
        	return implementation;
        }

        // Create and initialize the BPEL implementation model
        implementation = bpelFactory.createBPELImplementation();
        implementation.setProcess(process);
        implementation.setUnresolved(true);
        
        // Skip to end element
        while (reader.hasNext()) {
            if (reader.next() == END_ELEMENT && IMPLEMENTATION_BPEL_QNAME.equals(reader.getName())) {
                break;
            }
        }
        
        return implementation;
    }

    public void resolve(BPELImplementation implementation, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        
    	if( implementation != null && implementation.isUnresolved()) 
    	{
    	    implementation.setModelResolver(resolver);
    	    
            BPELProcessDefinition processDefinition = resolveBPELProcessDefinition(implementation, resolver, context);
            //resolveBPELImports(processDefinition, resolver);
            if(processDefinition.isUnresolved()) {
            	error(context.getMonitor(), "BPELProcessNotFound", implementation, processDefinition.getName());
            } else {            
                implementation.setProcessDefinition(processDefinition);
            
                // Get the component type from the process definition
                generateComponentType( implementation, context.getMonitor() );
                        
                //set current implementation resolved 
                implementation.setUnresolved(false);
            }
        }
        
    } // end resolve

    public void write( BPELImplementation bpelImplementation, 
    		           XMLStreamWriter writer, ProcessorContext context ) throws ContributionWriteException, XMLStreamException {
        //FIXME Deal with policy processing...
        // Write <implementation.bpel process="..."/>
        // policyProcessor.writePolicyPrefixes(bpelImplementation, writer);
        writer.writeStartElement(SCA11_NS, IMPLEMENTATION_BPEL);
        // policyProcessor.writePolicyAttributes(bpelImplementation, writer);
        
        if (bpelImplementation.getProcess() != null) {
            writer.writeAttribute(PROCESS, bpelImplementation.getProcess().toString() );
        }

        writer.writeEndElement();

    } // end write

    private BPELProcessDefinition resolveBPELProcessDefinition(BPELImplementation impl, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        QName processName = impl.getProcess();
        BPELProcessDefinition processDefinition = this.bpelFactory.createBPELProcessDefinition();
        processDefinition.setName(processName);
        processDefinition.setUnresolved(true);
        
        return resolver.resolveModel(BPELProcessDefinition.class, processDefinition, context);
    } // end resolveBPELProcessDefinition
    
    private void resolveBPELImports(BPELProcessDefinition processDefinition, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
    	for (BPELImportElement bpelImport : processDefinition.getImports()) {
    		String namespace = bpelImport.getNamespace();
    		String location = bpelImport.getLocation();
    		
    		WSDLDefinition wsdl = bpelImport.getWSDLDefinition();
    		if (wsdl == null) {
        			try {
        				wsdl = wsdlFactory.createWSDLDefinition();
        				wsdl.setUnresolved(true);
        				wsdl.setNamespace(bpelImport.getNamespace());
						wsdl.setLocation(new URI(null, bpelImport.getLocation(), null));
						wsdl = resolver.resolveModel(WSDLDefinition.class, wsdl, context);
						
						if(! wsdl.isUnresolved()) {
							bpelImport.setWSDLDefinition(wsdl);
						} else {
							//error("BPELProcessNotFound", implementation, processDefinition.getName());
						}
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

    		}
    		
    	}
    }

    
    /**
     * Calculates the component type of the supplied implementation and attaches it to the
     * implementation.
     * 
     * @param impl
     * @throws ContributionResolveException
     */
    private void generateComponentType(BPELImplementation impl, Monitor monitor) throws ContributionResolveException {

        // Create a ComponentType and mark it unresolved
        ComponentType componentType = assemblyFactory.createComponentType();
        componentType.setUnresolved(true);
        impl.setComponentType(componentType);

        // Each partner link in the process represents either a service or a reference
        // - or both, in the sense of involving a callback
        BPELProcessDefinition theProcess = impl.getProcessDefinition();
        List<BPELPartnerLinkElement> partnerLinks = theProcess.getPartnerLinks();

        for (BPELPartnerLinkElement pLink : partnerLinks) {

            // check that the partner link has been designated as service or
            // reference in SCA terms
            if (pLink.isSCATyped()) {
                String scaName = pLink.getSCAName();
                if (pLink.querySCAType().equals("reference")) {
                    componentType.getReferences().add(generateReference(scaName, pLink.getMyRolePortType(), pLink.getPartnerRolePortType(), theProcess.getInterfaces(), monitor));
                } else {
                    componentType.getServices().add(generateService(scaName, pLink.getMyRolePortType(), pLink.getPartnerRolePortType(), theProcess.getInterfaces(), monitor));
                } // end if
            } // end if
        } // end for
        
        // Each SCA Property in the process becomes a Property in the ComponentType
        for( Property property : theProcess.getProperties() ) {
        	componentType.getProperties().add(property);
        } // end for

    } // end getComponentType
    
    /**
     * Create an SCA reference for a partnerLink
     * @param name - name of the reference
     * @param myRolePT - partner link type of myRole
     * @param partnerRolePT - partner link type of partnerRole
     * @param theInterfaces - list of WSDL interfaces associated with the BPEL process
     * @return
     */
    private Reference generateReference( String name, PortType myRolePT, 
    		PortType partnerRolePT, Collection<WSDLInterface> theInterfaces, Monitor monitor) throws ContributionResolveException {
        
        Reference reference = assemblyFactory.createReference();
        WSDLInterfaceContract interfaceContract = wsdlFactory.createWSDLInterfaceContract();
        reference.setInterfaceContract(interfaceContract);

        // Establish whether there is just a call interface or a call + callback interface
        PortType callPT = null;
        PortType callbackPT = null;
        if (partnerRolePT != null) {
            callPT = partnerRolePT;
            // If the 2 port types are not the same one, there is a callback...
            if (myRolePT != null) {
                if (!myRolePT.getQName().equals(partnerRolePT.getQName())) {
                    callbackPT = myRolePT;
                } // end if
            } // end if
        } else if (myRolePT != null) {
            callPT = myRolePT;
        } // end if

        // No interfaces mean an error
        if (callPT == null && callbackPT == null) {
            error(monitor, "MyRolePartnerRoleNull", theInterfaces);
        } // end if

        // Set the name of the reference to the supplied name and the
        // multiplicity of the reference to 1..1
        // TODO: support other multiplicities
        reference.setName(name);
        reference.setMultiplicity(Multiplicity.ONE_ONE);

        if (callPT != null) {
            // Set the call interface and, if present, the callback interface
            WSDLInterface callInterface = null;
            for (WSDLInterface anInterface : theInterfaces) {
                if (anInterface.getPortType().getQName().equals(callPT.getQName()))
                    callInterface = anInterface;
            } // end for
            if (callInterface == null) {
                error(monitor, "NoInterfaceForPortType", theInterfaces, callPT.getQName().toString());
            } else
                reference.getInterfaceContract().setInterface(callInterface);
        } // end if

        // There is a callback if the partner role is not null and if the
        // partner role port type is not the same as the port type for my role
        if (callbackPT != null) {
            WSDLInterface callbackInterface = null;
            for (WSDLInterface anInterface : theInterfaces) {
                if (anInterface.getPortType().getQName().equals(callbackPT.getQName()))
                    callbackInterface = anInterface;
            } // end for
            if (callbackInterface == null) {
                error(monitor, "NoInterfaceForPortType", theInterfaces, callbackPT.getQName().toString());
            } else
                reference.getInterfaceContract().setCallbackInterface(callbackInterface);
        } // end if

        return reference;
    } // end generateReference
    
    /**
     * Create an SCA service for a partnerLink
     * @param name - name of the service
     * @param myRolePT - partner link type of myRole
     * @param partnerRolePT - partner link type of partnerRole
     * @param theInterfaces - list of WSDL interfaces associated with the BPEL process
     * @return
     */
    private Service generateService( String name, PortType myRolePT, 
    		PortType partnerRolePT, Collection<WSDLInterface> theInterfaces, Monitor monitor ) 
    		throws ContributionResolveException {
        Service service = assemblyFactory.createService();
        WSDLInterfaceContract interfaceContract = wsdlFactory.createWSDLInterfaceContract();
        service.setInterfaceContract(interfaceContract);

        // Set the name of the service to the supplied name
        service.setName(name);

        // Establish whether there is just a call interface or a call + callback
        // interface
        PortType callPT = null;
        PortType callbackPT = null;
        if (myRolePT != null) {
            callPT = myRolePT;
            // If the 2 port types are not the same one, there is a callback...
            if (partnerRolePT != null) {
                if (!myRolePT.getQName().equals(partnerRolePT.getQName())) {
                    callbackPT = partnerRolePT;
                } // end if
            } // end if
        } else if (partnerRolePT != null) {
            callPT = partnerRolePT;
        } // end if

        // No interfaces mean an error
        if (callPT == null && callbackPT == null) {
            error(monitor, "MyRolePartnerRoleNull", theInterfaces);
        } // end if

        if (callPT != null) {
            // Set the call interface and, if present, the callback interface
            WSDLInterface callInterface = null;
            for (WSDLInterface anInterface : theInterfaces) {
                if (anInterface.getPortType().getQName().equals(callPT.getQName()))
                    callInterface = anInterface;
            } // end for
            if (callInterface == null) {
                error(monitor, "NoInterfaceForPortType", theInterfaces, callPT.getQName().toString());
            } else
                service.getInterfaceContract().setInterface(callInterface);
        } // end if

        // There is a callback if the partner role is not null and if the
        // partner role port type is not the same as the port type for my role
        if (callbackPT != null) {
            WSDLInterface callbackInterface = null;
            for (WSDLInterface anInterface : theInterfaces) {
                if (anInterface.getPortType().getQName().equals(callbackPT.getQName()))
                    callbackInterface = anInterface;
            } // end for
            if (callbackInterface == null) {
                error(monitor, "NoInterfaceForPortType", theInterfaces, callbackPT.getQName().toString());
            } else
                service.getInterfaceContract().setCallbackInterface(callbackInterface);
        } // end if

        return service;
    } // end generateService
    
    /**
     * Returns a QName from its string representation in a named attribute of an XML element
     * supplied in an XMLStreamReader
     * 
     * QName attributes of an XML element (such as  BPEL process) is presented in one of
     * two alternative formats:
     * 1) In the form of a local name with a prefix, with the prefix referencing a namespace
     * URI declaration elsewhere in the composite (typically on the composite element)
     * 
     * ie:   nms:SomeName
     *       xmlns:nms="http://example.com/somenamespace"
     *       
     * 2) In the XML Namespaces recommendation format (see http://jclark.com/xml/xmlns.htm )
     * where the namespace URI and the local name are encoded into a single string, with the 
     * namespace URI enclosed between a pair of braces {...}
     * 
     *  ie:  {http://example.com/somenamespace}SomeName
     */
    private QName getAttributeValueNS(XMLStreamReader reader, String attribute, Monitor monitor) {
        String fullValue = reader.getAttributeValue(null, attribute);
        if (fullValue == null) {
            error(monitor, "AttributeProcessMissing", reader);
            return null;
        }

        // Deal with the attribute in the XML Namespaces recommendation format
        // - trim off any leading/trailing spaces and check that the first
        // character is '{'
        if (fullValue.trim().charAt(0) == '{') {
            try {
                // Attempt conversion to a QName object
                QName theProcess = QName.valueOf(fullValue);
                return theProcess;
            } catch (IllegalArgumentException e) {
                // This exception happens if the attribute begins with '{' but
                // doesn't conform
                // to the XML Namespaces recommendation format
                error(monitor, "AttributeWithoutNamespace", reader, attribute, fullValue);
                return null;
            }
        } // endif

        // Deal with the attribute in the local name + prefix format
        if (fullValue.indexOf(":") < 0) {
            error(monitor, "AttributeWithoutPrefix", reader, attribute, fullValue);
            return null;
        }
        String prefix = fullValue.substring(0, fullValue.indexOf(":"));
        String name = fullValue.substring(fullValue.indexOf(":") + 1);
        String nsUri = reader.getNamespaceContext().getNamespaceURI(prefix);
        if (nsUri == null) {
            error(monitor, "AttributeUnrecognizedNamespace", reader, attribute, fullValue);
            return null;
        }
        return new QName(nsUri, name, prefix);
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
                Problem problem = monitor.createProblem(this.getClass().getName(), "impl-bpel-validation-messages", Severity.ERROR, model, message, (Object[])messageParameters);
                monitor.problem(problem);
         }
    }
     
}
