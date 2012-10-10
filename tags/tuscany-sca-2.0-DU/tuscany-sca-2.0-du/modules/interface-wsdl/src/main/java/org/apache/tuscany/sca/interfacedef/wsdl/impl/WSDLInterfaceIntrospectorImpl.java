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
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.wsdl.PortType;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.wsdl.RequiresExt;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLFactory;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;
import org.apache.tuscany.sca.interfacedef.wsdl.xml.WSDLInterfaceProcessor;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.xsd.XSDFactory;
import org.w3c.dom.Element;

/**
 * Introspector for creating WSDLInterface definitions from WSDL PortTypes.
 *
 * @version $Rev: 1186027 $ $Date: 2011-10-19 09:37:03 +0100 (Wed, 19 Oct 2011) $
 */
public class WSDLInterfaceIntrospectorImpl {
    private static final QName POLICY_REQUIRES 			= new QName("http://docs.oasis-open.org/ns/opencsa/sca/200912", "requires");
    
    private static final QName CALLBACK_ATTRIBUTE 		= new QName("http://docs.oasis-open.org/ns/opencsa/sca/200912", "callback" );
    
    private WSDLFactory wsdlFactory;
    private XSDFactory xsdFactory;
    private PolicyFactory policyFactory;
    
    public WSDLInterfaceIntrospectorImpl(FactoryExtensionPoint modelFactories, WSDLFactory wsdlFactory) {
        this.xsdFactory = modelFactories.getFactory(XSDFactory.class);
        this.policyFactory = modelFactories.getFactory(PolicyFactory.class);
        this.wsdlFactory = wsdlFactory;
    }

    // FIXME: Do we want to deal with document-literal wrapped style based on the JAX-WS Specification?
    private List<Operation> introspectOperations(PortType portType, WSDLDefinition wsdlDefinition, ModelResolver resolver, Monitor monitor) throws InvalidWSDLException {
        List<Operation> operations = new ArrayList<Operation>();
        for (Object o : portType.getOperations()) {
            javax.wsdl.Operation wsdlOp = (javax.wsdl.Operation)o;
            Operation operation = getOperation(wsdlOp, wsdlDefinition, resolver, xsdFactory, monitor);
            operations.add(operation);
        }
        return operations;
    }

    public void introspectPortType(WSDLInterface wsdlInterface, PortType portType, WSDLDefinition wsdlDefinition, ModelResolver resolver, Monitor monitor) throws InvalidWSDLException {
        processIntents(wsdlInterface, portType);
        WSDLInterface callback = processCallbackAttribute( portType, resolver, monitor );
        wsdlInterface.setPortType(portType);
        wsdlInterface.setCallbackInterface(callback);
        wsdlInterface.getOperations().addAll(introspectOperations(portType, wsdlDefinition, resolver, monitor));
    }

    public static Operation getOperation(javax.wsdl.Operation wsdlOp,
                                         WSDLDefinition wsdlDefinition,
                                         ModelResolver resolver,
                                         XSDFactory xsdFactory,
                                         Monitor monitor) throws InvalidWSDLException {
        WSDLOperationIntrospectorImpl op = new WSDLOperationIntrospectorImpl(xsdFactory, wsdlOp, wsdlDefinition, null, resolver, monitor);
        return op.getOperation();
    }
    
    /**
     * Process an extension @callback attribute on a WSDL portType declaration
     * - the callback attribute must contain the QName of another portType
     * @param portType the portType
     * @return
     */
    private WSDLInterface processCallbackAttribute( PortType portType, ModelResolver resolver, Monitor monitor ) {
        Object o =  portType.getExtensionAttribute(CALLBACK_ATTRIBUTE);
        if(o != null && o instanceof QName) { 
        	WSDLInterface wsdlInterface = wsdlFactory.createWSDLInterface();
        	wsdlInterface.setUnresolved(true);
        	wsdlInterface.setName( (QName)o );
        	wsdlInterface = WSDLInterfaceProcessor.resolveWSDLInterface( wsdlInterface, resolver, monitor, wsdlFactory );
        	
        	return wsdlInterface;
        } else {
        	return null;
        } // end if 
    } // end method processCallbackAttribute
    
    private void processIntents(WSDLInterface wsdlInterface, PortType portType) {
        
        // process @requires attribute
        Object o;
        try {
            o =  portType.getExtensionAttribute(POLICY_REQUIRES);
        } catch (NoSuchMethodError e) {
            // That method does not exist on older WSDL4J levels
            o = null;
        }
        if(o != null && o instanceof Vector) {
            Vector<QName> policyAttributes = (Vector<QName>) o;
            
            Enumeration<QName> policyItents = policyAttributes.elements();
            while (policyItents.hasMoreElements()) {
                QName intentName = policyItents.nextElement();

                // Add each intent to the list
                Intent intent = policyFactory.createIntent();
                intent.setName(intentName);

                wsdlInterface.getRequiredIntents().add(intent);
            }
        }
        
        // process <sca:requires/> element
        for(Object object : portType.getExtensibilityElements()){
            ExtensibilityElement element = (ExtensibilityElement)object;
            
            if (element.getElementType().equals(POLICY_REQUIRES)){
                RequiresExt requires = ((RequiresExt)element);
                
                for (QName intentName : requires.getIntents()){
                    Intent intent = policyFactory.createIntent();
                    intent.setName(intentName);

                    wsdlInterface.getRequiredIntents().add(intent);
                }
            }
        }
    }
    
}
