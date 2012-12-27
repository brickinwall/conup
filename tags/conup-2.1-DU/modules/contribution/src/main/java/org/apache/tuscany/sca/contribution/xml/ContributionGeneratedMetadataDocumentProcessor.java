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
package org.apache.tuscany.sca.contribution.xml;

import javax.xml.stream.XMLInputFactory;

import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.monitor.Monitor;

/**
 * URLArtifactProcessor that handles sca-contribution-generated.xml files.
 *
 * @version $Rev: 826363 $ $Date: 2009-10-18 07:19:06 +0100 (Sun, 18 Oct 2009) $
 */
public class ContributionGeneratedMetadataDocumentProcessor extends ContributionMetadataDocumentProcessor {

    public ContributionGeneratedMetadataDocumentProcessor(XMLInputFactory inputFactory,
                                                          StAXArtifactProcessor staxProcessor) {
        super(inputFactory, staxProcessor);
    }

    public ContributionGeneratedMetadataDocumentProcessor(FactoryExtensionPoint modelFactories,
                                                          StAXArtifactProcessor staxProcessor) {
        super(modelFactories, staxProcessor);
    }

    @Override
    public String getArtifactType() {
        return "/META-INF/sca-contribution-generated.xml";
    }
}
