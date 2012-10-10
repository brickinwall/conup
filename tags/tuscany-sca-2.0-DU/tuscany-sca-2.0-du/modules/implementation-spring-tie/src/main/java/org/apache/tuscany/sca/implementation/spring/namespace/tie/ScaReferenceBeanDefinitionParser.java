/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.tuscany.sca.implementation.spring.namespace.tie;

import static org.apache.tuscany.sca.implementation.spring.namespace.tie.ScaNamespaceHandler.getAttribute;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.implementation.spring.context.tie.SCAGenericApplicationContext;
import org.apache.tuscany.sca.implementation.spring.elements.tie.SpringSCAReferenceElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for the &lt;sca:reference&gt; element
 *
 * @version $Rev: 988752 $ $Date: 2010-08-24 23:50:49 +0100 (Tue, 24 Aug 2010) $
 */
public class ScaReferenceBeanDefinitionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionRegistry registry = parserContext.getRegistry();
        if (registry instanceof SCAGenericApplicationContext) {
            SCAGenericApplicationContext context = (SCAGenericApplicationContext)registry;
            SpringSCAReferenceElement referenceElement =
                new SpringSCAReferenceElement(getAttribute(element, "name"), getAttribute(element, "type"));
            referenceElement.setDefaultBean(getAttribute(element, "default"));

            String requires = getAttribute(element, "requires");
            if (requires != null) {
                List<QName> qnames = ScaNamespaceHandler.resolve(element, requires);
                referenceElement.getIntentNames().addAll(qnames);
            }

            String policySets = getAttribute(element, "policySets");
            if (policySets != null) {
                List<QName> qnames = ScaNamespaceHandler.resolve(element, policySets);
                referenceElement.getPolicySetNames().addAll(qnames);
            }

            context.addSCAReferenceElement(referenceElement);
        }

        // do nothing, this is handled by Tuscany
        return null;
    }

}
