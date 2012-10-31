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

package org.apache.tuscany.sca.assembly.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.common.java.io.IOHelper;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ValidatingXMLInputFactory;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.monitor.Monitor;

/**
 * A componentType processor.
 * 
 * @version $Rev: 828119 $ $Date: 2009-10-21 19:00:07 +0100 (Wed, 21 Oct 2009) $
 */
public class ComponentTypeDocumentProcessor extends BaseAssemblyProcessor implements URLArtifactProcessor<ComponentType> {
    private ValidatingXMLInputFactory inputFactory;
    
    /**
     * Constructs a new componentType processor.
     * @param modelFactories
     * @param staxProcessor
     */
    public ComponentTypeDocumentProcessor(FactoryExtensionPoint modelFactories,
                                          StAXArtifactProcessor staxProcessor) {
        super(modelFactories, staxProcessor);
        this.inputFactory = modelFactories.getFactory(ValidatingXMLInputFactory.class);
    }
    
    public ComponentType read(URL contributionURL, URI uri, URL url, ProcessorContext context) throws ContributionReadException {
        InputStream urlStream = null;
        Monitor monitor = context.getMonitor();
        try {
            
            // Create a stream reader
            urlStream = IOHelper.openStream(url);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(url.toString(), urlStream);
            ValidatingXMLInputFactory.setMonitor(reader, monitor);
            reader.nextTag();
            
            // Reader the componentType model 
            ComponentType componentType = (ComponentType)extensionProcessor.read(reader, context);
            if (componentType != null) {
                componentType.setURI(uri.toString());
            }

            // For debugging purposes, write it back to XML
//            if (componentType != null) {
//                try {
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
//                    outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
//                    extensionProcessor.write(componentType, outputFactory.createXMLStreamWriter(bos));
//                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(bos.toByteArray()));
//                    OutputFormat format = new OutputFormat();
//                    format.setIndenting(true);
//                    format.setIndent(2);
//                    XMLSerializer serializer = new XMLSerializer(System.out, format);
//                    serializer.serialize(document);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            
            return componentType;
            
        } catch (XMLStreamException e) {
        	ContributionReadException ce = new ContributionReadException(e);
        	error(monitor, "ContributionReadException", inputFactory, ce);
            throw ce;
        } catch (IOException e) {
        	ContributionReadException ce = new ContributionReadException(e);
        	error(monitor, "ContributionReadException", inputFactory, ce);
            throw ce;
        } finally {
            try {
                if (urlStream != null) {
                    urlStream.close();
                    urlStream = null;
                }
            } catch (IOException ioe) {
                //ignore
            }
        }
    }
    
    public void resolve(ComponentType componentType, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        extensionProcessor.resolve(componentType, resolver, context);
    }
    
    public String getArtifactType() {
        return ".componentType";
    }
    
    public Class<ComponentType> getModelType() {
        return ComponentType.class;
    }
}
