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
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.builder.BuilderContext;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilder;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilderException;
import org.apache.tuscany.sca.assembly.builder.PolicyBuilder;
import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicyExpression;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySubject;
import org.apache.tuscany.sca.policy.util.PolicyHelper;

/**
 * A composite builder that computes policy sets based on attached intents and policy sets.
 * Useful if you want to build the model without making any runtime decisions such as
 * reference/services matching
 *
 * @version $Rev: 1236688 $ $Date: 2012-01-27 14:05:03 +0000 (Fri, 27 Jan 2012) $
 */
public class CompositePolicyBuilderImpl extends ComponentPolicyBuilderImpl implements CompositeBuilder {
    private final static QName NOLISTENER_INTENT = new QName(Base.SCA11_NS, "noListener");
    private CompositeBuilder policyAppliesToBuilder = null;
    
    public CompositePolicyBuilderImpl(ExtensionPointRegistry registry) {
        super(registry);
        
        policyAppliesToBuilder = new PolicyAppliesToBuilderImpl(registry);
    }

    public String getID() {
        return "org.apache.tuscany.sca.assembly.builder.CompositePolicyBuilder";
    }

    public Composite build(Composite composite, BuilderContext context) throws CompositeBuilderException {
        computePolicies(composite, context);
        checkPolicies(composite, context);
        buildPolicies(composite, context);
        return composite;
    }

    protected void computePolicies(Composite composite, BuilderContext context) {
        Monitor monitor = context.getMonitor();
        monitor.pushContext("Composite: " + composite.getName().toString());

        try {
            resolveAndCheck(composite, context);

            // compute policies recursively
            for (Component component : composite.getComponents()) {
                monitor.pushContext("Component: " + component.getName());

                //resolve component level
                resolveAndCheck(component, context);
                
                try {
                    Implementation implementation = component.getImplementation();
                    
                    for (ComponentService componentService : component.getServices()) {
                        monitor.pushContext("Service: " + componentService.getName());

                        try {
                            resolveAndCheck(componentService, context);

                            if (componentService.getInterfaceContract() != null) {
                                resolveAndCheck(componentService.getInterfaceContract().getInterface(), context);

                                resolveAndCheck(componentService.getInterfaceContract().getCallbackInterface(), context);

                            }

                            for (Endpoint ep : componentService.getEndpoints()) {
                              
                                
                                // Inherit from binding
                                inherit(ep, null, true, ep.getBinding());
                                
                                // Inherit from composite/component/service
                                inherit(ep, null, true, ep.getService(), ep.getComponent(), composite );
                                
                                if (componentService.getInterfaceContract() != null) {
                                    // Inherit from the component.service.interface
                                	// Do not check mutual exclusion here.. interfaces do not follow normal rules
                                	// of the structural hierarchy (Policy spec 4.10)
                                    inherit(ep, null, false, componentService.getInterfaceContract().getInterface());
                                }
                                
                                // add in default binding mayProvides intents
                                addDefaultIntents(ep, ep.getBinding(), context);

                                // Replace profile intents with their required intents
                                // Replace unqualified intents if there is a qualified intent in the list
                                // Replace qualifiable intents with the default qualied intent
                                resolveAndNormalize(ep, context);
                                
                                // Replace qualifiable intents with their default qualifier
                                expandDefaultIntents(ep, context);
                                
                                // Remove the intents whose @contraints do not include the current element
                                removeConstrainedIntents(ep, context);
                                
                                // Remove any direct policy sets if an external one has been applied
                                removeDirectPolicySetsIfExternalExists(ep, context);
                                
                                // Validate that noListener is not specified on a service endpoint
                                checkForNoListenerIntent(ep, context);
                                
                                // check that all intents are resolved
                                checkIntentsResolved(ep, context);

                                // check that the resulting endpoint has no mutually exclusive intents
                                checkMutualExclusion(ep, context);
                            }
                        } finally {
                            monitor.popContext();
                        }
                    }

                    for (ComponentReference componentReference : component.getReferences()) {
                        monitor.pushContext("Reference: " + componentReference.getName().toString());

                        try {

                            if (componentReference.getInterfaceContract() != null) {
                                resolveAndCheck(componentReference.getInterfaceContract().getInterface(), context);

                                resolveAndCheck(componentReference.getInterfaceContract().getCallbackInterface(),
                                                context);
                            }

                            for (EndpointReference epr : componentReference.getEndpointReferences()) {
                                                             
                                // Inherit from binding
                                inherit(epr, null, true, epr.getBinding());

                                // Inherit from composite/component/reference
                                inherit(epr, null, true,  epr.getReference(), epr.getComponent(),  composite);
                                
                                // Inherit from the component.reference.interface
                                if (componentReference.getInterfaceContract() != null) {
                                	// Do not check mutual exclusion here.. interfaces do not follow normal rules
                                	// of the structural hierarchy (Policy spec 4.10)
                                    inherit(epr, null, true, componentReference.getInterfaceContract().getInterface());
                                }  
                                
                                // add in default binding mayProvides intents
                                addDefaultIntents(epr, epr.getBinding(), context);

                                // Replace profile intents with their required intents
                                // Replace unqualified intents if there is a qualified intent in the list
                                // Replace qualifiable intents with the default qualified intent
                                resolveAndNormalize(epr, context);
                                
                                // Replace qualifiable intents with their default qualifier
                                expandDefaultIntents(epr, context);
                                
                                // Remove the intents whose @contraints do not include the current element
                                removeConstrainedIntents(epr, context);
                                
                                removeDirectPolicySetsIfExternalExists(epr, context);
                                
                                // check that all intents are resolved
                                checkIntentsResolved(epr, context);

                                // check that the resulting endpoint reference has no mutually exclusive intents
                                checkMutualExclusion(epr, context);
                            }
                        } finally {
                            monitor.popContext();
                        }
                    }

                    if (implementation instanceof Composite) {                    	                     		
                    	resolveAndCheck(implementation, context);
                        inherit(implementation, Intent.Type.implementation, true, component, composite);                                             
                        computePolicies((Composite)implementation, context);
                        expandDefaultIntents(implementation,context);
                        checkIntentsResolved(implementation,context);
                    } else {
                        resolveAndCheck(implementation, context);
                        if (implementation != null) {
                            inherit(implementation, Intent.Type.implementation, true, component, composite);
                            
                            // Remove the intents whose @contraints do not include the current element
                            removeConstrainedIntents(implementation, context);
                            
                            removeDirectPolicySetsIfExternalExists(implementation, context);
                            
                         // Replace qualifiable intents with their default qualifier
                            expandDefaultIntents(implementation, context);
                            
                            // check that all intents are resolved
                            checkIntentsResolved(implementation, context);
                        }
                    }
                } finally {
                    monitor.popContext();
                }
            }
            removeConstrainedIntents(composite, context);
        } finally {
            monitor.popContext();
        }
    }
 
    private void validateTransactionIntents(Composite composite, BuilderContext context) {    	    		       	   
    	 
    	for ( Component component : composite.getComponents() ) {    	
    		if ( component.getImplementation() != null ) {
    			if ( component.getImplementation() instanceof Composite ) 
    				validateTransactionIntents((Composite) component.getImplementation(), context);    		   
    			
    			for ( Intent implIntent : component.getImplementation().getRequiredIntents() ) {
    				if ( Constants.MANAGED_TRANSACTION_LOCAL_INTENT.equals(implIntent.getName() ) ) {
    					for ( ComponentReference reference : component.getReferences() ) {
    						for ( EndpointReference epr : reference.getEndpointReferences() ) {
    							for ( Intent eprIntent : epr.getRequiredIntents() ) {
    								if ( Constants.TRANSACTED_ONE_WAY_INTENT.equals(eprIntent.getName())) {
    									error(context.getMonitor(), 
    										"TransactedOneWayWithManagedTransactionLocal", 
    		    			    			this,
    		    			    			"reference",
    		    			    			epr.getComponent().getName(),
    		    			    			epr.getReference().getName());
    								} else if ( Constants.PROPAGATES_TRANSACTION_INTENT.equals(eprIntent.getName())) {
    									error(context.getMonitor(),
    											"PropagatesTransactionWithLocalTran",
    											this,
    											"reference",
    											epr.getComponent().getName(),
    											epr.getReference().getName());
    								}
    							}
    						}
    					}    	
    					for ( ComponentService service : component.getServices() ) {
    						for ( Endpoint ep : service.getEndpoints() ) {
    							for ( Intent epIntent : ep.getRequiredIntents() ) {
    								if ( Constants.TRANSACTED_ONE_WAY_INTENT.equals(epIntent.getName())) {
    									error(context.getMonitor(),
    											"TransactedOneWayWithManagedTransactionLocal",
    											this,
    											"service",
    											ep.getComponent().getName(),
    											ep.getService().getName());
    								} else if ( Constants.PROPAGATES_TRANSACTION_INTENT.equals(epIntent.getName())) {
    									error(context.getMonitor(),
    											"PropagatesTransactionWithLocalTran",
    											this,
    											"service",
    											ep.getComponent().getName(),
    											ep.getService().getName());
    								}
    							}
    						}
    					}
    				} else if ( Constants.NO_MANAGED_TRANSACTION_INTENT.equals(implIntent.getName())) {
    					for ( ComponentService service : component.getServices() ) {
    						for ( Endpoint ep : service.getEndpoints() ) {
    							for ( Intent epIntent : ep.getRequiredIntents() ) {
    								if ( Constants.PROPAGATES_TRANSACTION_INTENT.equals(epIntent.getName())) {
    									error(context.getMonitor(), 
    										"PropagatesTransactionWithNoManagedTran", 
    		    			    			this,
    		    			    			"service",
    		    			    			ep.getComponent().getName(),
    		    			    			ep.getService().getName());
    								}
    							}
    						}
    					}
    					for ( ComponentReference reference : component.getReferences() ) {
    						for ( EndpointReference epr : reference.getEndpointReferences() ) {
    							for ( Intent eprIntent : epr.getRequiredIntents() ) {
    								if ( Constants.PROPAGATES_TRANSACTION_INTENT.equals(eprIntent.getName())) {
    									error(context.getMonitor(),
    											"PropagatesTransactionWithNoManagedTran",
    											this,
    											"reference",
    											epr.getComponent().getName(),
    											epr.getReference().getName());
    								}
    							}
    						}
    					}
    				}
    			}
    			     		
/*  TUSCANY-4006 - This is allowed; transactedOneWay applies only to the one-way methods when an interface also has two-way operations
 */			
       			for ( ComponentReference reference : component.getReferences()) {
    				for ( EndpointReference epr : reference.getEndpointReferences() ) {
    					for ( Intent eprIntent : epr.getRequiredIntents() ) {
    						if ( Constants.TRANSACTED_ONE_WAY_INTENT.equals(eprIntent.getName()) ) {
    							for ( Operation o : epr.getComponentReferenceInterfaceContract().getInterface().getOperations() ) {
    								if ( !o.isNonBlocking() ) {
    									error(context.getMonitor(),
    											"TransactedOneWayWithTwoWayOp",
    											this,
    											reference.getName(),
    											o.getName());
    								}
    									
    							}
    						} else if ( Constants.IMMEDIATE_ONE_WAY_INTENT.equals(eprIntent.getName())) {
    							for ( Operation o : epr.getComponentReferenceInterfaceContract().getInterface().getOperations() ) {
    								if ( !o.isNonBlocking() ) {
    									error(context.getMonitor(),
    											"ImmediateOneWayWithTwoWayOp",
    											this,
    											reference.getName(),
    											o.getName());
    								}
    									
    							}
    						}
    					}    					
    				}   				
    			}
/*
*/    			
    		}   
    	}
    }
    	        					 	
   

	private void checkForNoListenerIntent(Endpoint ep, BuilderContext context) {
		PolicyHelper helper = new PolicyHelper();
		if ( helper.getIntent(ep, NOLISTENER_INTENT) != null ) {
			  error(context.getMonitor(), 
                      "NoListenerIntentSpecifiedOnService", 
                      this,
                      ep.toString());
		} 				
		
	}

	private void removeDirectPolicySetsIfExternalExists(PolicySubject subject,
			BuilderContext context) {
    	boolean foundExternalPolicySet = false;
		for (PolicySet ps : subject.getPolicySets() ) {
			if ( ps.isExternalAttachment() ) {
				foundExternalPolicySet = true;
				break;
			}
		}
		
		if ( foundExternalPolicySet ) {
			List<PolicySet> copy = new ArrayList<PolicySet>(subject.getPolicySets());
			for ( PolicySet ps : copy ) {
				if ( !ps.isExternalAttachment() ) {
					subject.getPolicySets().remove(ps);
				}
			}
		}
		
	} 

	/**
     * This is mainly about removing policies that don't "applyTo" the element where
     * they have ended up after all the attachment and inheritance processing
     * 
     * @param composite
     * @param context
     */
    protected void checkPolicies(Composite composite, BuilderContext context) throws CompositeBuilderException{
        policyAppliesToBuilder.build(composite, context);
        validateTransactionIntents(composite, context);
    }

    protected void buildPolicies(Composite composite, BuilderContext context) {

        // build policies recursively
        for (Component component : composite.getComponents()) {
            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
                buildPolicies((Composite)implementation, context);
            }
        }

        for (Component component : composite.getComponents()) {

            for (ComponentService componentService : component.getServices()) {
                for (Endpoint ep : componentService.getEndpoints()) {
                    Set<String> policyNames = getPolicyNames(ep);
                    
                    // check that only one policy language is present in the endpoint's policy sets
                    if (policyNames.size() > 1){
                        error(context.getMonitor(), 
                              "MultiplePolicyLanguagesInEP", 
                              this,
                              ep.toString(), 
                              policyNames.toString());
                    } else {
                        for (PolicySet ps : ep.getPolicySets()) {
                            for (PolicyExpression exp : ps.getPolicies()) {
                                PolicyBuilder builder = builders.getPolicyBuilder(exp.getName());
                                if (builder != null) {
                                    builder.build(ep, context);
                                }                
                            }
                        }                        
                    }
                }
            }

            for (ComponentReference componentReference : component.getReferences()) {
                for (EndpointReference epr : componentReference.getEndpointReferences()) {
                    Set<String> policyNames = getPolicyNames(epr);
                    
                    // check that only one policy language is present in the endpoint references's policy sets
                    if (policyNames.size() > 1){
                        error(context.getMonitor(),  
                              "MultiplePolicyLanguagesInEPR",
                              this,
                              epr.toString(), 
                              policyNames.toString());
                    } else {                    
                        for (PolicySet ps : epr.getPolicySets()) {
                            for (PolicyExpression exp : ps.getPolicies()) {
                                PolicyBuilder builder = builders.getPolicyBuilder(exp.getName());
                                if (builder != null) {
                                    builder.build(epr, context);
                                }
                            }
                        }
                    }
                }
            }

            Implementation implementation = component.getImplementation();
            if (implementation != null) {
                Set<String> policyNames = getPolicyNames(implementation);
                
                // check that only one policy language is present in the implementations's policy sets
                if (policyNames.size() > 1){
                    error(context.getMonitor(), 
                          "MultiplePolicyLanguagesInImplementation", 
                          this,
                          component.toString(), 
                          policyNames.toString());
                } else {
                    for (PolicySet ps : implementation.getPolicySets()) {
                        for (PolicyExpression exp : ps.getPolicies()) {                    
                            PolicyBuilder builder = builders.getPolicyBuilder(exp.getName());
                            if (builder != null) {
                                builder.build(component, implementation, context);
                            }
                        }
                    }
                }
            }
        }
    }
}
