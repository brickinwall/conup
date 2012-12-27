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

package org.apache.tuscany.sca.policy.xml;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.ImplementationType;
import org.apache.tuscany.sca.policy.PolicyFactory;

/**
 * Processor for handling XML models of ImplementationType meta data definitions
 *
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class ImplementationTypeProcessor extends ExtensionTypeProcessor {

    public ImplementationTypeProcessor(PolicyFactory policyFactory,
                                       StAXArtifactProcessor<Object> extensionProcessor) {
        super(policyFactory, extensionProcessor);
    }

    public ImplementationTypeProcessor(FactoryExtensionPoint modelFactories,
                                       StAXArtifactProcessor<Object> extensionProcessor) {
        super(modelFactories.getFactory(PolicyFactory.class), extensionProcessor);
    }

    public QName getArtifactType() {
        return IMPLEMENTATION_TYPE_QNAME;
    }

    @Override
    protected ExtensionType resolveExtensionType(ExtensionType extnType, ModelResolver resolver, ProcessorContext context)
        throws ContributionResolveException {
        if (extnType instanceof ImplementationType) {
            ImplementationType implType = (ImplementationType)extnType;
            return resolver.resolveModel(ImplementationType.class, implType, context);
        } else {
            return extnType;
        }

    }
}
