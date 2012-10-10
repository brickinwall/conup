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
package org.apache.tuscany.sca.node.configuration.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.common.xml.stax.StAXHelper;
import org.apache.tuscany.sca.contribution.processor.BaseStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.node.configuration.BindingConfiguration;
import org.apache.tuscany.sca.node.configuration.ContributionConfiguration;
import org.apache.tuscany.sca.node.configuration.DeploymentComposite;
import org.apache.tuscany.sca.node.configuration.NodeConfiguration;
import org.apache.tuscany.sca.node.configuration.NodeConfigurationFactory;

/**
 * Implements a StAX artifact processor for node implementations.
 *
 * @version $Rev: 1209634 $ $Date: 2011-12-02 18:55:36 +0000 (Fri, 02 Dec 2011) $
 */
public class NodeConfigurationProcessor extends BaseStAXArtifactProcessor implements
    StAXArtifactProcessor<NodeConfiguration> {
    private static final String SCA11_TUSCANY_NS = "http://tuscany.apache.org/xmlns/sca/1.1";
    private static final QName NODE = new QName(SCA11_TUSCANY_NS, "node");
    private static final QName CONTRIBUTION = new QName(SCA11_TUSCANY_NS, "contribution");
    private static final QName BINDING = new QName(SCA11_TUSCANY_NS, "binding");
    private static final QName BASE_URI = new QName(SCA11_TUSCANY_NS, "baseURI");
    private static final QName DEPLOYMENT_COMPOSITE = new QName(SCA11_TUSCANY_NS, "deploymentComposite");

    private static final String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200912";
    private static final QName COMPOSITE = new QName(SCA11_NS, "composite");

    private StAXArtifactProcessor processor;
    private NodeConfigurationFactory nodeConfigurationFactory;
    private StAXHelper helper;

    public NodeConfigurationProcessor(ExtensionPointRegistry registry,
                                      StAXArtifactProcessor processor) {
        FactoryExtensionPoint modelFactories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        this.nodeConfigurationFactory = modelFactories.getFactory(NodeConfigurationFactory.class);
        this.processor = processor;
        this.helper = StAXHelper.getInstance(registry);
    }

    public QName getArtifactType() {
        // Returns the QName of the XML element processed by this processor
        return NODE;
    }

    public Class<NodeConfiguration> getModelType() {
        // Returns the type of model processed by this processor
        return NodeConfiguration.class;
    }

    public NodeConfiguration read(XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {

        NodeConfiguration node = null;
        ContributionConfiguration contribution = null;
        DeploymentComposite composite = null;
        BindingConfiguration binding = null;

        // Skip to end element
        while (true) {
            int event = reader.getEventType();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    QName name = reader.getName();
                    if (NODE.equals(name)) {
                        node = nodeConfigurationFactory.createNodeConfiguration();
                        node.setURI(reader.getAttributeValue(null, "uri"));
                        node.setDomainURI(reader.getAttributeValue(null, "domain"));
                        node.setDomainRegistryURI(reader.getAttributeValue(null, "domainRegistry"));
                    } else if (CONTRIBUTION.equals(name)) {
                        contribution = nodeConfigurationFactory.createContributionConfiguration();
                        contribution.setURI(reader.getAttributeValue(null, "uri"));
                        contribution.setLocation(reader.getAttributeValue(null, "location"));
                        contribution.setMetaDataURL(reader.getAttributeValue(null, "metaDataURL"));
                        String startDeployables = reader.getAttributeValue(null, "startDeployables");
                        if (startDeployables != null) {
                            contribution.setStartDeployables(Boolean.parseBoolean(startDeployables));
                        }
                        String dependentURIs = reader.getAttributeValue(null, "dependentURIs");
                        if (dependentURIs != null) {
                            contribution.setDependentContributionURIs(Arrays.asList(dependentURIs.split(",")));
                        }
                        node.getContributions().add(contribution);
                    } else if (BINDING.equals(name)) {
                        binding = nodeConfigurationFactory.createBindingConfiguration();
                        binding.setBindingType(getQName(reader, "name"));
                        String baseURIs = reader.getAttributeValue(null, "baseURIs");
                        if (baseURIs != null) {
                            StringTokenizer tokenizer = new StringTokenizer(baseURIs);
                            while (tokenizer.hasMoreTokens()) {
                                binding.getBaseURIs().add(tokenizer.nextToken());
                            }
                        }
                        node.getBindings().add(binding);
                    } else if (DEPLOYMENT_COMPOSITE.equals(name)) {
                        composite = nodeConfigurationFactory.createDeploymentComposite();
                        composite.setLocation(reader.getAttributeValue(null, "location"));
                        if (contribution != null) {
                            contribution.getDeploymentComposites().add(composite);
                        }
                    } else if(BASE_URI.equals(name)) {
                        // We also support <baseURI> element
                        String baseURI = reader.getElementText();
                        if (baseURI != null && binding != null) {
                            baseURI = baseURI.trim();
                            binding.addBaseURI(baseURI);
                        }
                        // getElementText() moves the event to END_ELEMENT
                        continue;
                    } else if (COMPOSITE.equals(name)) {
                        /*
                        Object model = processor.read(reader);
                        if (model instanceof Composite) {
                            // FIXME: We need to capture the text here
                            // composite.setComposite((Composite)model);
                        }
                        */
                        StringWriter sw = new StringWriter();
                        XMLStreamWriter writer = helper.createXMLStreamWriter(sw);
                        helper.save(reader, writer);
                        composite.setContent(sw.toString());
                    } else {
                        Object ext = processor.read(reader, context);
                        if (ext instanceof Endpoint) {
                            node.getEndpointDescriptions().add((Endpoint)ext);
                        } else {
                            node.getExtensions().add(ext);
                        }
                    }
                    break;

                case END_ELEMENT:
                    name = reader.getName();
                    if (NODE.equals(name)) {
                        return node;
                    } else if (CONTRIBUTION.equals(name)) {
                        contribution = null;
                    } else if (DEPLOYMENT_COMPOSITE.equals(name)) {
                        composite = null;
                    } else if (BINDING.equals(name)) {
                        binding = null;
                    }
            }
            if (reader.hasNext()) {
                reader.next();
            } else {
                break;
            }
        }

        return node;
    }

    public void resolve(NodeConfiguration node, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
    }

    public void write(NodeConfiguration node, XMLStreamWriter writer, ProcessorContext context) throws ContributionWriteException,
        XMLStreamException {

        writeStart(writer,
                   NODE.getNamespaceURI(),
                   NODE.getLocalPart(),
                   new XAttr("uri", node.getURI()),
                   new XAttr("domainRegistry", node.getDomainRegistryURI()),
                   new XAttr("domain", node.getDomainURI()));

        for (ContributionConfiguration c : node.getContributions()) {
            writeStart(writer,
                       CONTRIBUTION.getNamespaceURI(),
                       CONTRIBUTION.getLocalPart(),
                       new XAttr("uri", c.getURI()),
                       new XAttr("location", c.getLocation()));
            for (DeploymentComposite dc : c.getDeploymentComposites()) {
                writeStart(writer,
                           DEPLOYMENT_COMPOSITE.getNamespaceURI(),
                           DEPLOYMENT_COMPOSITE.getLocalPart(),
                           new XAttr("location", dc.getLocation()),
                           new XAttr("contribution", dc.getContributionURI()));
                if (dc.getContent() != null) {
                    XMLStreamReader reader = helper.createXMLStreamReader(new StringReader(dc.getContent()));
                    reader.nextTag(); // Move to the first element
                    helper.save(reader, writer);
                    reader.close();
                }
                writeEnd(writer);
            }
            writeEnd(writer);
        }

        for (BindingConfiguration b : node.getBindings()) {
            StringBuffer uris = new StringBuffer();
            for (String uri : b.getBaseURIs()) {
                uris.append(uri).append(' ');
            }
            if (uris.length() > 0) {
                uris.deleteCharAt(uris.length() - 1); // Remove the trailing space
            } else {
                uris = null;
            }
            String baseURIs = (uris == null) ? null : uris.toString();
            writeStart(writer,
                       BINDING.getNamespaceURI(),
                       BINDING.getLocalPart(),
                       new XAttr("name", b.getBindingType()),
                       new XAttr("baseURIs", baseURIs));
            writeEnd(writer);
        }
        
        // FIXME: The composite processor assumes that composite is root element
//        for (Endpoint o : node.getEndpointDescriptions()) {
//            processor.write(o, writer, context);
//        }
        
        for(Object o: node.getExtensions()) {
            processor.write(o, writer, context);
        }

        writeEnd(writer);
    }
}
