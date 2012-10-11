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

package org.apache.tuscany.sca.core.assembly.impl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.runtime.ReferenceParameters;

/**
 * Artifact processor for reference parameters.
 * 
 * @version $Rev: 967109 $ $Date: 2010-07-23 15:30:46 +0100 (Fri, 23 Jul 2010) $
 */
public class ReferenceParameterProcessor implements StAXArtifactProcessor<ReferenceParameters> {
    private static final QName REFERENCE_PARAMETERS =
        new QName("http://tuscany.apache.org/xmlns/sca/1.1", "referenceParameters", "tuscany");
    
    /**
     * Constructs a new processor.
     * 
     * @param modelFactories
     */
    public ReferenceParameterProcessor(FactoryExtensionPoint modelFactories) {
    }

    /**
     * @see org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor#getArtifactType()
     */
    public QName getArtifactType() {
        return REFERENCE_PARAMETERS;
    }

    /**
     * @see org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor#read(javax.xml.stream.XMLStreamReader, ProcessorContext)
     */
    public ReferenceParameters read(XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {
        ReferenceParameters parameters = new ReferenceParametersImpl();
        parameters.setCallbackID(reader.getAttributeValue(null, "callbackID"));
        return parameters;
    }

    /**
     * @see org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor#write(java.lang.Object, javax.xml.stream.XMLStreamWriter, ProcessorContext)
     */
    public void write(ReferenceParameters model, XMLStreamWriter writer, ProcessorContext context) throws ContributionWriteException,
        XMLStreamException {
        writer.writeStartElement(REFERENCE_PARAMETERS.getPrefix(),
                                 REFERENCE_PARAMETERS.getLocalPart(),
                                 REFERENCE_PARAMETERS.getNamespaceURI());
        writer.writeNamespace(REFERENCE_PARAMETERS.getPrefix(), REFERENCE_PARAMETERS.getNamespaceURI());

        if (model.getCallbackID() != null) {
            writer.writeAttribute("callbackID", model.getCallbackID().toString());
        }
        writer.writeEndElement();
    }

    /**
     * @see org.apache.tuscany.sca.contribution.processor.ArtifactProcessor#getModelType()
     */
    public Class<ReferenceParameters> getModelType() {
        return ReferenceParameters.class;
    }

    /**
     * @see org.apache.tuscany.sca.contribution.processor.ArtifactProcessor#resolve(java.lang.Object, org.apache.tuscany.sca.contribution.resolver.ModelResolver, ProcessorContext)
     */
    public void resolve(ReferenceParameters model, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
    }

}
