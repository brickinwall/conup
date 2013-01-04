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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.contribution.processor.BaseStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXAttributeProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;

/**
 * A Policy Processor used for testing.
 *
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class TestAttributeProcessor extends BaseStAXArtifactProcessor implements StAXAttributeProcessor<String> {
	private static final QName ATTRIBUTE = new QName("http://test", "customAttribute");
	
    public QName getArtifactType() {
        return ATTRIBUTE;
    }

    public String read(QName attributeName, XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {
        return reader.getAttributeValue(attributeName.getNamespaceURI(), attributeName.getLocalPart());
    }

    public void write(String value, XMLStreamWriter writer, ProcessorContext context) throws ContributionWriteException, XMLStreamException {
    	writer.setPrefix(ATTRIBUTE.getPrefix(), ATTRIBUTE.getNamespaceURI());
    	writer.writeAttribute(ATTRIBUTE.getLocalPart(), value);
    } 

    public Class<String> getModelType() {
        return String.class;
    }

    public void resolve(String arg0, ModelResolver arg1, ProcessorContext context) throws ContributionResolveException {
    	
    }
}
