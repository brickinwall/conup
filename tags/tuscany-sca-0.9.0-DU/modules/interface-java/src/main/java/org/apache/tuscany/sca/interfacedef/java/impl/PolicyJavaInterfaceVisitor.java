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
package org.apache.tuscany.sca.interfacedef.java.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaOperation;
import org.apache.tuscany.sca.interfacedef.java.introspect.JavaInterfaceVisitor;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySubject;
import org.oasisopen.sca.annotation.PolicySets;
import org.oasisopen.sca.annotation.Qualifier;
import org.oasisopen.sca.annotation.Requires;

/**
 * Processes an {@link org.oasisopen.sca.annotation.Requires} annotation
 *
 * @version $Rev: 986433 $ $Date: 2010-08-17 19:19:18 +0100 (Tue, 17 Aug 2010) $
 */
public class PolicyJavaInterfaceVisitor implements JavaInterfaceVisitor {
    private PolicyFactory policyFactory;

    public PolicyJavaInterfaceVisitor(ExtensionPointRegistry registry) {
        super();
        this.policyFactory = registry.getExtensionPoint(FactoryExtensionPoint.class).getFactory(PolicyFactory.class);
    }

    private QName getQName(String intentName) {
        QName qname;
        if (intentName.startsWith("{")) {
            int i = intentName.indexOf('}');
            if (i != -1) {
                qname = new QName(intentName.substring(1, i), intentName.substring(i + 1));
            } else {
                qname = new QName("", intentName);
            }
        } else {
            qname = new QName("", intentName);
        }
        return qname;
    }

    /**
     * Read policy intents on the given interface or class 
     * @param clazz
     * @param requiredIntents
     */
    private void readIntentsAndPolicySets(Class<?> clazz, PolicySubject subject) {
        Requires intentAnnotation = clazz.getAnnotation(Requires.class);
        if (intentAnnotation != null) {
            String[] intentNames = intentAnnotation.value();
            if (intentNames.length != 0) {
                for (String intentName : intentNames) {

                    // Add each intent to the list
                    Intent intent = policyFactory.createIntent();
                    intent.setName(getQName(intentName));
                    subject.getRequiredIntents().add(intent);
                }
            }
        }

        readSpecificIntents(clazz.getAnnotations(), subject.getRequiredIntents());
        
        PolicySets policySetAnnotation = clazz.getAnnotation(PolicySets.class);
        if (policySetAnnotation != null) {
            String[] policySetNames = policySetAnnotation.value();
            if (policySetNames.length != 0) {
                for (String policySetName : policySetNames) {

                    // Add each intent to the list
                    PolicySet policySet = policyFactory.createPolicySet();
                    policySet.setName(getQName(policySetName));
                    subject.getPolicySets().add(policySet);
                }
            }
        }
        
        if ( clazz.isAnnotationPresent(SOAPBinding.class) ) {
        	// add soap intent        	
            Intent intent = policyFactory.createIntent();
            intent.setName(Constants.SOAP_INTENT);
            subject.getRequiredIntents().add(intent);
        }
        
       
    }

    private void readIntents(Requires intentAnnotation, List<Intent> requiredIntents) {
        //Requires intentAnnotation = method.getAnnotation(Requires.class);
        if (intentAnnotation != null) {
            String[] intentNames = intentAnnotation.value();
            if (intentNames.length != 0) {
                //Operation operation = assemblyFactory.createOperation();
                //operation.setName(method.getName());
                //operation.setUnresolved(true);
                for (String intentName : intentNames) {

                    // Add each intent to the list, associated with the
                    // operation corresponding to the annotated method
                    Intent intent = policyFactory.createIntent();
                    intent.setName(getQName(intentName));
                    //intent.getOperations().add(operation);
                    requiredIntents.add(intent);
                }
            }
        }
    }

    private void readPolicySets(PolicySets policySetAnnotation, List<PolicySet> policySets) {
        if (policySetAnnotation != null) {
            String[] policySetNames = policySetAnnotation.value();
            if (policySetNames.length != 0) {
                //Operation operation = assemblyFactory.createOperation();
                //operation.setName(method.getName());
                //operation.setUnresolved(true);
                for (String policySetName : policySetNames) {
                    // Add each intent to the list, associated with the
                    // operation corresponding to the annotated method
                    PolicySet policySet = policyFactory.createPolicySet();
                    policySet.setName(getQName(policySetName));
                    //intent.getOperations().add(operation);
                    policySets.add(policySet);
                }
            }
        }
    }

	public void readWebServicesAnnotations(Method m, Class<?> clazz, List<Intent> requiredIntents) {
		
		WebResult webResultAnnotation = m.getAnnotation(WebResult.class);
		if (webResultAnnotation != null) {
			if (webResultAnnotation.header()) {
				// Add SOAP intent
				Intent intent = policyFactory.createIntent();
				intent.setName(Constants.SOAP_INTENT);
				requiredIntents.add(intent);
				return;
			}
		}
		
		Annotation[][] parameterAnnotations = m.getParameterAnnotations();
		for ( int i=0; i < parameterAnnotations.length; i++ ) {
			for ( int j=0; j < parameterAnnotations[i].length; j++) {
				if ( parameterAnnotations[i][j] instanceof WebParam ) {
					WebParam webParam = (WebParam)parameterAnnotations[i][j];
					if ( webParam.header() ) {
						// Add SOAP intent
						Intent intent = policyFactory.createIntent();
						intent.setName(Constants.SOAP_INTENT);
						requiredIntents.add(intent);
						return;
					}
				}
			}
		}

	}
    public void visitInterface(JavaInterface javaInterface) throws InvalidInterfaceException {

        if (javaInterface.getJavaClass() != null) {
        	readIntentsAndPolicySets(javaInterface.getJavaClass(), javaInterface);

            // Read intents on the service interface methods 
            List<Operation> operations = javaInterface.getOperations();
            for (Operation op : operations) {
                JavaOperation operation = (JavaOperation)op;
                Method method = operation.getJavaMethod();
              
                readIntents(method.getAnnotation(Requires.class), op.getRequiredIntents());
                readSpecificIntents(method.getAnnotations(), op.getRequiredIntents());
                readPolicySets(method.getAnnotation(PolicySets.class), op.getPolicySets());
                readWebServicesAnnotations(method, javaInterface.getJavaClass(), javaInterface.getRequiredIntents());
                inherit(javaInterface, op);
            }
        }
        
        
     
       
    }

    private void inherit(JavaInterface javaInterface, Operation op) {
    	List<Intent> interfaceIntents = new ArrayList<Intent>(javaInterface.getRequiredIntents());
		for ( Intent intent : javaInterface.getRequiredIntents() ) {
			
			for ( Intent operationIntent : op.getRequiredIntents() ) {
				if ( intent.getExcludedIntents().contains(operationIntent) || 
						operationIntent.getExcludedIntents().contains(intent) ) {
					interfaceIntents.remove(intent);
					continue;
				}
			}
		}
		op.getRequiredIntents().addAll(interfaceIntents);
		
		op.getPolicySets().addAll(javaInterface.getPolicySets());
	}

	private void readSpecificIntents(Annotation[] annotations, List<Intent> requiredIntents) {
        for (Annotation a : annotations) {
            org.oasisopen.sca.annotation.Intent intentAnnotation =
                a.annotationType().getAnnotation(org.oasisopen.sca.annotation.Intent.class);
            if (intentAnnotation == null) {
                continue;
            }
            QName qname = null;
            String value = intentAnnotation.value();
            if (!value.equals("")) {
                qname = getQName(value);
            } else {
                qname = new QName(intentAnnotation.targetNamespace(), intentAnnotation.localPart());
            }
            Set<String> qualifiers = new HashSet<String>();
            for(Method m: a.annotationType().getMethods()) {
                Qualifier qualifier = m.getAnnotation(Qualifier.class);
                if (qualifier != null && m.getReturnType() == String[].class) {
                    try {
                        qualifiers.addAll(Arrays.asList((String[]) m.invoke(a)));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    } 
                }
            }
            qualifiers.remove("");
            if (qualifiers.isEmpty()) {
                Intent intent = policyFactory.createIntent();
                intent.setUnresolved(true);
                intent.setName(qname);
                requiredIntents.add(intent);
            } else {
                for (String q : qualifiers) {
                    Intent intent = policyFactory.createIntent();
                    intent.setUnresolved(true);
                    qname = new QName(qname.getNamespaceURI(), qname.getLocalPart() + "." + q);
                    intent.setName(qname);
                    requiredIntents.add(intent);
                }
            }
        }
    }

}
