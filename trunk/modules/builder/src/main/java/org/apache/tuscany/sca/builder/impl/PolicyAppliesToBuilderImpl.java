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

package org.apache.tuscany.sca.builder.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.builder.BuilderContext;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilderException;

import org.apache.tuscany.sca.context.CompositeContext;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.definitions.Definitions;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySubject;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A builder that checks that policy sets apply to the elements to which they are attached. 
 * Any that don't are removed. It first creates a DOM model for the composite so that the xpath
 * expression can be evaluated. For each element that holds a policy set it calculates the 
 * appliesTo nodes and checks that the current element is in the set. If not the policySet is
 * removed from the element   
 *
 * @version $Rev: 1138864 $ $Date: 2011-06-23 14:05:00 +0100 (Thu, 23 Jun 2011) $
 */
public class PolicyAppliesToBuilderImpl extends PolicyAttachmentBuilderImpl {

    public PolicyAppliesToBuilderImpl(ExtensionPointRegistry registry) {
        super(registry);
    }

    public String getID() {
        return "org.apache.tuscany.sca.policy.builder.PolicyAppliesToBuilder";
    }

    // Build the whole composite
    public Composite build(Composite composite, BuilderContext context)
        throws CompositeBuilderException {
        try {

	    Definitions definitions = context.getDefinitions();
            if (definitions == null || (definitions.getPolicySets().isEmpty() && definitions.getExternalAttachments().isEmpty()) ) {
                return composite;
            }
            // create a DOM for the Domain Composite Infoset
            Document document = saveAsDOM(composite);
            
            // create a cache of evaluated node against each policy set so we don't
            // have to keep evaluating policy sets that appear in multiple places
            Map<PolicySet, List<PolicySubject>> appliesToSubjects = new HashMap<PolicySet, List<PolicySubject>>();
            
            // for all implementations, endpoint and endpoint references check that 
            // the policy sets validly apply
            return checkAppliesTo(document, appliesToSubjects, composite, context);
           
        } catch (Exception e) {
            throw new CompositeBuilderException(e);
        }
    }
    
    // Build just an endpoint reference that has just been matched against an endpoint. This
    // can often happen at runtime so the policy attachTo for a reference policy cannot
    // be assessed at build time
    // THIS DOESN'T HANDLE THE NESTED COMPOSITE CASE
    public void build(EndpointReference epr) throws CompositeBuilderException {
        try {
            
            // What we really need to be doing here is...
            //
            // - correct the composite model to add the service binding to the reference
            // - turn the composite model into a DOM
            // - apply the reference policy XPath and assess whether it appliesTo the binding
            //
            // I've had a go at making the code do that below. However there is some question
            // about what the appliesTo field means and it's hard to get here from the 
            // binder so until we sort out the appliesTo question this code isn't used
                       
            CompositeContext compositeContext = ((RuntimeComponent)epr.getComponent()).getComponentContext().getCompositeContext();
            Composite domainComposite = compositeContext.getDomainComposite();
            Definitions systemDefinitions = compositeContext.getSystemDefinitions();
            
            if (systemDefinitions == null || 
                (systemDefinitions.getPolicySets().isEmpty() && 
                 systemDefinitions.getExternalAttachments().isEmpty()) ) {
                return;
            }
            
            // temporarily add the endpoint binding to the reference model so that the
            // XPath expression can be evaluated correctly
            epr.getReference().getBindings().add(epr.getTargetEndpoint().getBinding());
            
            // create a DOM for the Domain Composite Infoset
            Document document = saveAsDOM(domainComposite);
            
            // remove the binding again to retain the untainted model 
            epr.getReference().getBindings().remove(epr.getTargetEndpoint().getBinding());
            
            // create a cache of evaluated node against each policy set so we don't
            // have to keep evaluating policy sets that appear in multiple places
            Map<PolicySet, List<PolicySubject>> appliesToSubjects = new HashMap<PolicySet, List<PolicySubject>>();
            
            // can we get the composite within which the erp is defined
            
            for (PolicySet ps : new ArrayList<PolicySet>(epr.getPolicySets()) ) {
                // Check if this PolicySet applies to the binding, component reference, component, or composite. If not,
                // remove it from the list of policy sets for this endpoint. 
                if ( epr.getBinding() instanceof PolicySubject ) {
                    if (isApplicableToSubject(document, appliesToSubjects, domainComposite, (PolicySubject)epr.getBinding(), ps))
                        continue;
                }
                if (isApplicableToSubject(document, appliesToSubjects, domainComposite, epr.getReference(), ps))
                    continue;
                else if ( (epr.getReference().getInterfaceContract() != null) && 
                          (isApplicableToSubject(document, appliesToSubjects, domainComposite, epr.getReference().getInterfaceContract().getInterface(), ps)))
                    continue;
                else if ( isApplicableToSubject(document, appliesToSubjects, domainComposite, epr.getComponent(), ps))
                    continue;
                else if ( isApplicableToSubject(document, appliesToSubjects, domainComposite, domainComposite, ps))
                    continue;
                else
                    epr.getPolicySets().remove(ps);             
            }
          
        } catch (Exception e) {
            throw new CompositeBuilderException(e);
        }
    }    
    
    private Composite checkAppliesTo(Document document, Map<PolicySet, List<PolicySubject>> appliesToSubjects, Composite topComposite, BuilderContext context) throws Exception {
 
    	for ( Component component : topComposite.getComponents() ) {
    		if ( component.getImplementation() instanceof Composite ) {
    			Composite nested = (Composite) component.getImplementation();
    			checkAppliesTo(saveAsDOM(nested),new HashMap<PolicySet, List<PolicySubject>>(), nested, context );
    		}
    	}

    	for (Component component : topComposite.getComponents()) {

    		for (ComponentService componentService : component.getServices()) {
    	
    			for (Endpoint ep : componentService.getEndpoints()) {  
    				for ( PolicySet ps : new ArrayList<PolicySet>(ep.getPolicySets()) ) {
    					// Check if this PolicySet applies to the binding, component service, interface, component, or composite. If not,
    					// remove it from the list of policy sets for this endpoint. 
    					if ( ep.getBinding() instanceof PolicySubject ) {
    						if (isApplicableToSubject(document, appliesToSubjects, topComposite, (PolicySubject)ep.getBinding(), ps))
    							continue;
    					}
    					
    					if (isApplicableToSubject(document, appliesToSubjects, topComposite, componentService, ps))
    						continue;
    					else if ( (componentService.getInterfaceContract() != null ) && (isApplicableToSubject(document, appliesToSubjects, topComposite, componentService.getInterfaceContract().getInterface(), ps)))
    						continue;
    					else if ( isApplicableToSubject(document, appliesToSubjects, topComposite, component, ps))
    						continue;
    					else if ( isApplicableToSubject(document, appliesToSubjects, topComposite, topComposite, ps))
    						continue;
    					else
    						ep.getPolicySets().remove(ps);
    				}
    			}
    		}

    		for (ComponentReference componentReference : component.getReferences()) {    			
    			for (EndpointReference epr : componentReference.getEndpointReferences()) {
    			    // don't process the EPR yet if it's not resolved
                    if (epr.getStatus() == EndpointReference.Status.WIRED_TARGET_NOT_FOUND ||
                        epr.getBinding() == null){ 
                        continue;
                    }
                    
    				for (PolicySet ps : new ArrayList<PolicySet>(epr.getPolicySets()) ) {
                        // Check if this PolicySet applies to the binding, component reference, component, or composite. If not,
                        // remove it from the list of policy sets for this endpoint. 
    					if ( epr.getBinding() instanceof PolicySubject ) {
    						if (isApplicableToSubject(document, appliesToSubjects, topComposite, (PolicySubject)epr.getBinding(), ps))
    							continue;
    					}
    					if (isApplicableToSubject(document, appliesToSubjects, topComposite, componentReference, ps))
    						continue;
    					else if ( (componentReference.getInterfaceContract() != null) && (isApplicableToSubject(document, appliesToSubjects, topComposite, componentReference.getInterfaceContract().getInterface(), ps)))
    						continue;
    					else if ( isApplicableToSubject(document, appliesToSubjects, topComposite, component, ps))
    						continue;
    					else if ( isApplicableToSubject(document, appliesToSubjects, topComposite, topComposite, ps))
    						continue;
    					else
    						epr.getPolicySets().remove(ps);    			
    				}
    			}
    		}

    		Implementation implementation = component.getImplementation();
    		if (implementation != null && 
    				implementation instanceof PolicySubject) {
    			for ( PolicySet ps : new ArrayList<PolicySet>(implementation.getPolicySets())) {    		
    				if (!isApplicableToSubject(document, appliesToSubjects, topComposite, implementation, ps))
    					implementation.getPolicySets().remove(ps);
    			}
    		}
    		
    	}
    	return topComposite;
    }

	/**
     * Checks that the provided policy sets applies to the provided policy subject
     * 
     * @param document
     * @param appliesToSubjects
     * @param policySubject
     * @param policySet
     * @return
	 * @throws XPathExpressionException    
     */
    private boolean isApplicableToSubject(Document document, Map<PolicySet, List<PolicySubject>> appliesToSubjects, Composite composite, PolicySubject policySubject, PolicySet policySet) throws XPathExpressionException {
                  
        List<PolicySubject> subjects = appliesToSubjects.get(policySet);
            
        if (subjects == null){
        	XPathExpression appliesTo = policySet.getAppliesToXPathExpression();
        	if (appliesTo != null) {
        		NodeList nodes = (NodeList)appliesTo.evaluate(document, XPathConstants.NODESET);
                    
        		if (nodes.getLength() > 0){
        			subjects = new ArrayList<PolicySubject>();
        			appliesToSubjects.put(policySet, subjects);
        		}
                    
        		for (int i = 0; i < nodes.getLength(); i++) {
        			Node node = nodes.item(i);
        			String index = getStructuralURI(node);
        			PolicySubject subject = lookup(composite, index);
        			if ( subject != null ) 
        				subjects.add(subject);
        		}
        	}
        }
            
        if (subjects != null){
        	if (!subjects.contains(policySubject)){
        		return false;
        	} else {
        		return true;
        	}
        } 
        
        return false;
                   
    }    
}
