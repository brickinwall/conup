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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.policy.PolicyExpression;

/**
 *
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class TestPolicyProcessor implements StAXArtifactProcessor<PolicyExpression> {

    public QName getArtifactType() {
        return new QName("http://schemas.xmlsoap.org/ws/2004/09/policy", "PolicyAttachment");
    }

    public PolicyExpression read(XMLStreamReader arg0, ProcessorContext context) throws ContributionReadException, XMLStreamException {
        return new MockPolicyImplOne();
    }

    public void write(PolicyExpression arg0, XMLStreamWriter arg1, ProcessorContext context) throws ContributionWriteException, XMLStreamException {
    }

    public Class<PolicyExpression> getModelType() {
        // TODO Auto-generated method stub
        return PolicyExpression.class;
    }

    public void resolve(PolicyExpression arg0, ModelResolver arg1, ProcessorContext context) throws ContributionResolveException {

    }

    public class MockPolicyImplOne implements PolicyExpression {
        public <T> T getPolicy() {
            return null;
        }

        public void setName(QName name) {
        }

        public <T> void setPolicy(T policy) {
        }

        public QName getName() {
            return new QName("http://schemas.xmlsoap.org/ws/2004/09/policy", "PolicyAttachment");
        }

        public boolean isUnresolved() {
            return false;
        }

        public void setUnresolved(boolean unresolved) {
        }

    }
}
