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
package org.apache.tuscany.sca.implementation.web.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.contribution.processor.BaseStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.implementation.web.WebImplementation;
import org.apache.tuscany.sca.implementation.web.WebImplementationFactory;
import org.apache.tuscany.sca.implementation.web.impl.WebImplementationFactoryImpl;

/**
 * Implements a StAX artifact processor for Web implementations.
 */
public class WebImplementationProcessor extends BaseStAXArtifactProcessor implements StAXArtifactProcessor<WebImplementation> {
    private static final QName IMPLEMENTATION_WEB = new QName(Constants.SCA11_NS, "implementation.web");
    
    private AssemblyFactory assemblyFactory;
    private WebImplementationFactory implementationFactory;
    
    public WebImplementationProcessor(ExtensionPointRegistry extensionPoints) {
        FactoryExtensionPoint modelFactories = extensionPoints.getExtensionPoint(FactoryExtensionPoint.class);
        this.assemblyFactory = modelFactories.getFactory(AssemblyFactory.class);
        this.implementationFactory = new WebImplementationFactoryImpl();
    }

    public QName getArtifactType() {
        // Returns the QName of the XML element processed by this processor
        return IMPLEMENTATION_WEB;
    }

    public Class<WebImplementation> getModelType() {
        // Returns the type of model processed by this processor
        return WebImplementation.class;
    }

    public WebImplementation read(XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {
        
        // Read an <implementation.web> element
        WebImplementation implementation = implementationFactory.createWebImplementation();
        implementation.setUnresolved(true);

        // Read the webapp uri attribute
        String webURI = getString(reader, "web-uri");
        if (webURI != null) {
            implementation.setWebURI(webURI);

            // Set the URI of the component type 
            implementation.setURI(webURI);
        }

        String jsClient = reader.getAttributeValue(Constants.SCA11_TUSCANY_NS, "jsClient");
        if (jsClient != null) {
            implementation.setJSClient(Boolean.parseBoolean(jsClient));
        }

        // Skip to end element
        while (reader.hasNext()) {
            if (reader.next() == END_ELEMENT && IMPLEMENTATION_WEB.equals(reader.getName())) {
                break;
            }
        }
        
        return implementation;
    }

    public void resolve(WebImplementation implementation, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        
        // Resolve the component type
        String uri = implementation.getURI();
        if (uri != null) {
            ComponentType componentType = assemblyFactory.createComponentType();
            componentType.setURI("web.componentType");
            componentType = resolver.resolveModel(ComponentType.class, componentType, context);
            if (!componentType.isUnresolved()) {
                
                // Initialize the implementation's services, references and properties
                implementation.getServices().addAll(componentType.getServices());
                implementation.getReferences().addAll(componentType.getReferences());
                implementation.getProperties().addAll(componentType.getProperties());
            }
        }
        implementation.setUnresolved(false);
    }

    public void write(WebImplementation implementation, XMLStreamWriter writer, ProcessorContext context) throws ContributionWriteException, XMLStreamException {
        
        // Write <implementation.web>
        writeStart(writer, IMPLEMENTATION_WEB.getNamespaceURI(), IMPLEMENTATION_WEB.getLocalPart(),
                   new XAttr("web-uri", implementation.getWebURI()));
        
        writeEnd(writer);
    }
}
