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

package org.apache.tuscany.sca.xsd.xml;

import java.net.URI;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;

import org.apache.tuscany.sca.common.xml.stax.StAXHelper;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.apache.tuscany.sca.xsd.XSDFactory;
import org.apache.tuscany.sca.xsd.XSDefinition;

/**
 * An ArtifactProcessor for XSD documents.
 * 
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class XSDDocumentProcessor implements URLArtifactProcessor<XSDefinition> {
    private StAXHelper helper;
    private XSDFactory factory;
    private XMLInputFactory inputFactory;
    

    public XSDDocumentProcessor(ExtensionPointRegistry registry, StAXArtifactProcessor processor) {
        FactoryExtensionPoint modelFactories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        this.factory = modelFactories.getFactory(XSDFactory.class);
        this.inputFactory = modelFactories.getFactory(XMLInputFactory.class);
        this.helper = StAXHelper.getInstance(registry);
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
            Problem problem = monitor.createProblem(this.getClass().getName(), "xsd-xml-validation-messages", Severity.ERROR, model, message, ex);
            monitor.problem(problem);
        }        
    }

    public XSDefinition read(URL contributionURL, URI artifactURI, URL artifactURL, ProcessorContext context) throws ContributionReadException {
        try {
            return indexRead(artifactURL);
        } catch (Exception e) {
            ContributionReadException ce = new ContributionReadException(e);
            error(context.getMonitor(), "ContributionReadException", artifactURL, ce);
            throw ce;
        }
    }

    public void resolve(XSDefinition model, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
    }

    public String getArtifactType() {
        return ".xsd";
    }

    public Class<XSDefinition> getModelType() {
        return XSDefinition.class;
    }

    public static final QName XSD = new QName("http://www.w3.org/2001/XMLSchema", "schema");

    protected XSDefinition indexRead(URL doc) throws Exception {
        XSDefinition xsd = factory.createXSDefinition();
        xsd.setUnresolved(true);
        xsd.setNamespace(helper.readAttribute(doc, XSD, "targetNamespace"));
        xsd.setLocation(doc.toURI());
        xsd.setUnresolved(false);
        return xsd;
    }
}
