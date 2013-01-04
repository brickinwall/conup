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
package org.apache.tuscany.sca.policy;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpression;

/**
 * Represents a policy set. See the Policy Framework specification for a
 * description of this element.
 *
 * @version $Rev: 982129 $ $Date: 2010-08-04 07:12:29 +0100 (Wed, 04 Aug 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface PolicySet {

    /**
     * Returns the intent name.
     * 
     * @return the intent name
     */
    QName getName();

    /**
     * Sets the intent name
     * 
     * @param name the intent name
     */
    void setName(QName name);

    /**
     * Returns the list of
     * 
     * @return
     */
    List<PolicySet> getReferencedPolicySets();

    /**
     * Returns the list of provided intents
     * 
     * @return
     */
    List<Intent> getProvidedIntents();

    /**
     * Returns the list of concrete policies, either WS-Policy policy
     * attachments, policy references, or policies expressed in another policy
     * language.
     * 
     * @return the list of concrete policies
     */
    List<PolicyExpression> getPolicies();

    /**
     * Returns true if the model element is unresolved.
     * 
     * @return true if the model element is unresolved.
     */
    boolean isUnresolved();

    /**
     * Sets whether the model element is unresolved.
     * 
     * @param unresolved whether the model element is unresolved
     */
    void setUnresolved(boolean unresolved);

    /**
     * Returns the XPath expression that is to be used to evaluate
     * if this PolicySet applies to specific policy subject
     * 
     * @return the XPath expression
     */
    String getAppliesTo();

    /**
     * Sets the XPath expression that is to be used to evaluate
     * if this PolicySet applies to specific policy subject
     * 
     */
    void setAppliesTo(String xpath);
    
    /**
     * A string which is an XPath 1.0 expression identifying one or more
     * elements (policy subject) in the Domain. It is used to declare which 
     * set of elements the policySet is actually attached to.
     * 
     * @return The attachTo XPath
     */
    String getAttachTo();
    
    /**
     * Set the attachTo XPath
     * @param xpath
     */
    void setAttachTo(String xpath);
    
    /**
     * Get the resolved XPathExpression for attachTo
     * @return
     */
    XPathExpression getAttachToXPathExpression();
    
    /**
     * Set the resolved XPathExpression for attachTo
     * @param expression
     */
    void setAttachToXPathExpression(XPathExpression expression);

    /**
     * Returns the policies / policy attachments provided thro intent maps
     * 
     * @return
     */
    List<IntentMap> getIntentMaps();
    
    /**
     * Get the XPath expression for the appliesTo attribute
     * @return the XPath expression for the appliesTo attribute
     */
    XPathExpression getAppliesToXPathExpression();

    /**
     * Set the XPath expression for the appliesTo attribute
     * @param xpathExpression the XPath expression for the appliesTo attribute
     */
    void setAppliesToXPathExpression(XPathExpression xpathExpression);

    /**
     * Returns true if this PolicySet was attached via external attachment
     * For unattached and directly attached PolicySets, returns false
     * @return
     */
    boolean isExternalAttachment();
    
    /**
     * Sets whether this PolicySet has been attached via external attachment or not
     * @param value
     */
    void setIsExternalAttachment(boolean value);
}
