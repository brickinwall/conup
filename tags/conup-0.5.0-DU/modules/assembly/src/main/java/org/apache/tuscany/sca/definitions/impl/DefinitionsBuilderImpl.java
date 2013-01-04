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
package org.apache.tuscany.sca.definitions.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.definitions.Definitions;
import org.apache.tuscany.sca.definitions.DefinitionsBuilder;
import org.apache.tuscany.sca.definitions.DefinitionsBuilderException;
import org.apache.tuscany.sca.policy.BindingType;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.ImplementationType;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.IntentMap;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.Qualifier;

/**
 * Provides a concrete implementation for a SCADefinitionsBuilder
 *
 * @version $Rev: 967109 $ $Date: 2010-07-23 15:30:46 +0100 (Fri, 23 Jul 2010) $
 */
public class DefinitionsBuilderImpl implements DefinitionsBuilder {

    public void build(Definitions scaDefns) throws DefinitionsBuilderException {
        Map<QName, Intent> definedIntents = new HashMap<QName, Intent>();
        for (Intent intent : scaDefns.getIntents()) {
            definedIntents.put(intent.getName(), intent);
        }

        Map<QName, PolicySet> definedPolicySets = new HashMap<QName, PolicySet>();
        for (PolicySet policySet : scaDefns.getPolicySets()) {
            definedPolicySets.put(policySet.getName(), policySet);
        }

        Map<QName, BindingType> definedBindingTypes = new HashMap<QName, BindingType>();
        for (BindingType bindingType : scaDefns.getBindingTypes()) {
            definedBindingTypes.put(bindingType.getType(), bindingType);
        }

        Map<QName, ImplementationType> definedImplTypes = new HashMap<QName, ImplementationType>();
        for (ImplementationType implType : scaDefns.getImplementationTypes()) {
            definedImplTypes.put(implType.getType(), implType);
        }

        //filling up the maps removes all duplicate entries... so fill this unique lists
        //into the scaDefns.
        scaDefns.getIntents().clear();
        scaDefns.getPolicySets().clear();
        scaDefns.getBindingTypes().clear();
        scaDefns.getImplementationTypes().clear();

        scaDefns.getIntents().addAll(definedIntents.values());
        scaDefns.getPolicySets().addAll(definedPolicySets.values());
        scaDefns.getBindingTypes().addAll(definedBindingTypes.values());
        scaDefns.getImplementationTypes().addAll(definedImplTypes.values());

        buildPolicyIntents(scaDefns, definedIntents);
        buildPolicySets(scaDefns, definedPolicySets, definedIntents);
        buildBindingTypes(scaDefns, definedBindingTypes, definedIntents);
        buildImplementationTypes(scaDefns, definedImplTypes, definedIntents);
    }

    private void buildBindingTypes(Definitions scaDefns,
                                   Map<QName, BindingType> definedBindingTypes,
                                   Map<QName, Intent> definedIntents) throws DefinitionsBuilderException {
        for (BindingType bindingType : scaDefns.getBindingTypes()) {
            buildAlwaysProvidedIntents(bindingType, definedIntents);
            buildMayProvideIntents(bindingType, definedIntents);
        }

    }

    private void buildImplementationTypes(Definitions scaDefns,
                                          Map<QName, ImplementationType> definedImplTypes,
                                          Map<QName, Intent> definedIntents) throws DefinitionsBuilderException {
        for (ImplementationType implType : scaDefns.getImplementationTypes()) {
            buildAlwaysProvidedIntents(implType, definedIntents);
            buildMayProvideIntents(implType, definedIntents);
        }
    }

    private void buildPolicyIntents(Definitions scaDefns, Map<QName, Intent> definedIntents)
        throws DefinitionsBuilderException {
        for (Intent policyIntent : scaDefns.getIntents()) {
            if (!policyIntent.getRequiredIntents().isEmpty()) {
                buildProfileIntent(policyIntent, definedIntents);
            }

            if (!policyIntent.getQualifiedIntents().isEmpty()) {
                buildQualifiedIntent(policyIntent, definedIntents);
            }
        }
    }

    private void buildPolicySets(Definitions scaDefns,
                                 Map<QName, PolicySet> definedPolicySets,
                                 Map<QName, Intent> definedIntents) throws DefinitionsBuilderException {

        for (PolicySet policySet : scaDefns.getPolicySets()) {
            buildProvidedIntents(policySet, definedIntents);
            buildIntentsInMappedPolicies(policySet, definedIntents);
            buildReferredPolicySets(policySet, definedPolicySets);
        }

        for (PolicySet policySet : scaDefns.getPolicySets()) {
            for (PolicySet referredPolicySet : policySet.getReferencedPolicySets()) {
                includeReferredPolicySets(policySet, referredPolicySet);
            }
        }
    }

    private void buildProfileIntent(Intent policyIntent, Map<QName, Intent> definedIntents)
        throws DefinitionsBuilderException {
        //FIXME: Need to check for cyclic references first i.e an A requiring B and then B requiring A... 
        if (policyIntent != null) {
            //resolve all required intents
            List<Intent> requiredIntents = new ArrayList<Intent>();
            for (Intent requiredIntent : policyIntent.getRequiredIntents()) {
                if (requiredIntent.isUnresolved()) {
                    Intent resolvedRequiredIntent = definedIntents.get(requiredIntent.getName());
                    if (resolvedRequiredIntent != null) {
                        requiredIntents.add(resolvedRequiredIntent);
                    } else {
                        throw new DefinitionsBuilderException("Required Intent - " + requiredIntent
                            + " not found for ProfileIntent "
                            + policyIntent);

                    }
                } else {
                    requiredIntents.add(requiredIntent);
                }
            }
            policyIntent.getRequiredIntents().clear();
            policyIntent.getRequiredIntents().addAll(requiredIntents);
        }
    }

    private void buildQualifiedIntent(Intent policyIntent, Map<QName, Intent> definedIntents)
        throws DefinitionsBuilderException {
        /*
        if (policyIntent != null) {
            //resolve the qualifiable intent
            Intent qualifiableIntent = policyIntent.getQualifiableIntent();
            if (qualifiableIntent.isUnresolved()) {
                Intent resolvedQualifiableIntent = definedIntents.get(qualifiableIntent.getName());

                if (resolvedQualifiableIntent != null) {
                    policyIntent.setQualifiableIntent(resolvedQualifiableIntent);
                } else {
                    throw new DefinitionsBuilderException("Qualifiable Intent - " + qualifiableIntent
                        + " not found for QualifiedIntent "
                        + policyIntent);
                }

            }
        }
        */
    }

    private void buildAlwaysProvidedIntents(ExtensionType extensionType, Map<QName, Intent> definedIntents)
        throws DefinitionsBuilderException {
        if (extensionType != null) {
            // resolve all provided intents
            List<Intent> alwaysProvided = new ArrayList<Intent>();
            for (Intent providedIntent : extensionType.getAlwaysProvidedIntents()) {
                if (providedIntent.isUnresolved()) {
                    Intent resolvedProvidedIntent = definedIntents.get(providedIntent.getName());
                    if (resolvedProvidedIntent != null) {
                        alwaysProvided.add(resolvedProvidedIntent);
                    } else {
                        throw new DefinitionsBuilderException("Always Provided Intent - " + providedIntent
                            + " not found for ExtensionType "
                            + extensionType);

                    }
                } else {
                    alwaysProvided.add(providedIntent);
                }
            }
            extensionType.getAlwaysProvidedIntents().clear();
            extensionType.getAlwaysProvidedIntents().addAll(alwaysProvided);
        }
    }

    private void buildMayProvideIntents(ExtensionType extensionType, Map<QName, Intent> definedIntents)
        throws DefinitionsBuilderException {
        if (extensionType != null) {
            // resolve all provided intents
            List<Intent> mayProvide = new ArrayList<Intent>();
            for (Intent providedIntent : extensionType.getMayProvidedIntents()) {
                if (providedIntent.isUnresolved()) {
                    Intent resolvedProvidedIntent = definedIntents.get(providedIntent.getName());
                    if (resolvedProvidedIntent != null) {
                        mayProvide.add(resolvedProvidedIntent);
                    } else {
                        throw new DefinitionsBuilderException("May Provide Intent - " + providedIntent
                            + " not found for ExtensionType "
                            + extensionType);

                    }
                } else {
                    mayProvide.add(providedIntent);
                }
            }
            extensionType.getMayProvidedIntents().clear();
            extensionType.getMayProvidedIntents().addAll(mayProvide);
        }
    }

    private void buildProvidedIntents(PolicySet policySet, Map<QName, Intent> definedIntents)
        throws DefinitionsBuilderException {
        if (policySet != null) {
            //resolve all provided intents
            List<Intent> providedIntents = new ArrayList<Intent>();
            for (Intent providedIntent : policySet.getProvidedIntents()) {
                if (providedIntent.isUnresolved()) {
                    Intent resolvedProvidedIntent = definedIntents.get(providedIntent.getName());
                    if (resolvedProvidedIntent != null) {
                        providedIntents.add(resolvedProvidedIntent);
                    } else {
                        throw new DefinitionsBuilderException("Provided Intent - " + providedIntent
                            + " not found for PolicySet "
                            + policySet);

                    }
                } else {
                    providedIntents.add(providedIntent);
                }
            }
            policySet.getProvidedIntents().clear();
            policySet.getProvidedIntents().addAll(providedIntents);
        }
    }

    private void buildIntentsInMappedPolicies(PolicySet policySet, Map<QName, Intent> definedIntents)
        throws DefinitionsBuilderException {
        for (IntentMap intentMap : policySet.getIntentMaps()) {
            for (Qualifier qualifier : intentMap.getQualifiers()) {
                Intent mappedIntent = qualifier.getIntent();
                if (mappedIntent.isUnresolved()) {
                    Intent resolvedMappedIntent = definedIntents.get(mappedIntent.getName());

                    if (resolvedMappedIntent != null) {
                        qualifier.setIntent(resolvedMappedIntent);
                    } else {
                        throw new DefinitionsBuilderException("Mapped Intent - " + mappedIntent
                            + " not found for PolicySet "
                            + policySet);

                    }
                }
            }
        }
    }

    private void buildReferredPolicySets(PolicySet policySet, Map<QName, PolicySet> definedPolicySets)
        throws DefinitionsBuilderException {

        List<PolicySet> referredPolicySets = new ArrayList<PolicySet>();
        for (PolicySet referredPolicySet : policySet.getReferencedPolicySets()) {
            if (referredPolicySet.isUnresolved()) {
                PolicySet resolvedReferredPolicySet = definedPolicySets.get(referredPolicySet.getName());
                if (resolvedReferredPolicySet != null) {
                    referredPolicySets.add(resolvedReferredPolicySet);
                } else {
                    throw new DefinitionsBuilderException("Referred PolicySet - " + referredPolicySet
                        + "not found for PolicySet - "
                        + policySet);
                }
            } else {
                referredPolicySets.add(referredPolicySet);
            }
        }
        policySet.getReferencedPolicySets().clear();
        policySet.getReferencedPolicySets().addAll(referredPolicySets);
    }

    private void includeReferredPolicySets(PolicySet policySet, PolicySet referredPolicySet) {
        for (PolicySet furtherReferredPolicySet : referredPolicySet.getReferencedPolicySets()) {
            includeReferredPolicySets(referredPolicySet, furtherReferredPolicySet);
        }
        policySet.getPolicies().addAll(referredPolicySet.getPolicies());
        policySet.getIntentMaps().addAll(referredPolicySet.getIntentMaps());
    }
}
