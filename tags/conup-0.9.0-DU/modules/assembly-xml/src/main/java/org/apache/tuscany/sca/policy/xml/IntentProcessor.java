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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.contribution.processor.BaseStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.policy.Intent.Type;

/**
 * Processor for handling XML models of PolicyIntent definitions
 *
 * @version $Rev: 983640 $ $Date: 2010-08-09 14:37:45 +0100 (Mon, 09 Aug 2010) $
 */
public class IntentProcessor extends BaseStAXArtifactProcessor implements StAXArtifactProcessor<Intent>,
    PolicyConstants {

    private PolicyFactory policyFactory;
    

    public IntentProcessor(FactoryExtensionPoint modelFactories) {
        this.policyFactory = modelFactories.getFactory(PolicyFactory.class);
    }

    public IntentProcessor(PolicyFactory policyFactory) {
        this.policyFactory = policyFactory;
    }

    /**
     * Report a error.
     * 
     * @param problems
     * @param message
     * @param model
     */
    private void error(Monitor monitor, String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      Messages.RESOURCE_BUNDLE,
                                      Severity.ERROR,
                                      model,
                                      message,
                                      messageParameters);
            monitor.problem(problem);
        }
    }

    private void warn(Monitor monitor, String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      Messages.RESOURCE_BUNDLE,
                                      Severity.WARNING,
                                      model,
                                      message,
                                      messageParameters);
            monitor.problem(problem);
        }
    }

    public Intent read(XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {
        Intent intent = null;
        String intentLocalName = reader.getAttributeValue(null, NAME);
        if (intentLocalName == null) {
            error(context.getMonitor(), "IntentNameMissing", reader);
            return null;
        }

        String intentType = reader.getAttributeValue(null, INTENT_TYPE);
        if (intentType == null) {
            intentType = Intent.Type.interaction.name();
        }

        intent = policyFactory.createIntent();

        // [rfeng] the target namespace is not available, set the local part for now
        // This will be changed in the definitions processor
        intent.setName(new QName(intentLocalName));
        intent.setType(Type.valueOf(intentType));

        readRequiredIntents(intent, reader, context);
        readExcludedIntents(intent, reader);

        readConstrainedTypes(intent, reader);
        
        String mutuallyExclusiveString = reader.getAttributeValue(null, MUTUALLY_EXCLUSIVE);
        if (mutuallyExclusiveString != null &&
            mutuallyExclusiveString.equals("true")){
            intent.setMutuallyExclusive(true);
        } else {
            intent.setMutuallyExclusive(false);
        }

        Intent current = intent;
        int event = reader.getEventType();
        QName name = null;
        while (reader.hasNext()) {
            event = reader.getEventType();
            switch (event) {
                case START_ELEMENT: {
                    name = reader.getName();
                    if (DESCRIPTION_QNAME.equals(name)) {
                        String text = reader.getElementText();
                        if (text != null) {
                            text = text.trim();
                        }
                        current.setDescription(text);
                    } else if (INTENT_QUALIFIER_QNAME.equals(name)) {
                        String qualifierName = reader.getAttributeValue(null, NAME);
                        String defaultQ = reader.getAttributeValue(null, DEFAULT);
                        boolean isDefault = defaultQ == null ? false : Boolean.parseBoolean(defaultQ);
                        String qualifiedIntentName = intentLocalName + QUALIFIER + qualifierName;
                        Intent qualified = policyFactory.createIntent();
                        qualified.setUnresolved(false);
                        qualified.setType(intent.getType());
                        qualified.setName(new QName(qualifiedIntentName));
                        if (isDefault) {
                            if (intent.getDefaultQualifiedIntent() == null){
                                intent.setDefaultQualifiedIntent(qualified);
                            } else {
                                Monitor.error(context.getMonitor(), 
                                              this, 
                                              Messages.RESOURCE_BUNDLE, 
                                              "MultipleDefaultQualifiers", 
                                              intent.getName().toString());
                            }
                        }
                        
                        // check that the qualifier is unique
                        if ( !intent.getQualifiedIntents().contains(qualified)){
                            intent.getQualifiedIntents().add(qualified);
                        } else {
                            Monitor.error(context.getMonitor(), 
                                          this, 
                                          Messages.RESOURCE_BUNDLE, 
                                          "QualifierIsNotUnique", 
                                          intent.getName().toString(),
                                          qualifierName);
                        }
                        
                        qualified.setQualifiableIntent(intent);
                        current = qualified;
                    }
                    break;
                }
                case END_ELEMENT: {
                    name = reader.getName();
                    if (INTENT_QUALIFIER_QNAME.equals(name)) {
                        current = intent;
                    }
                    break;
                }
            }
            if (event == END_ELEMENT && POLICY_INTENT_QNAME.equals(reader.getName())) {
                break;
            }

            //Read the next element
            if (reader.hasNext()) {
                reader.next();
            }
        }
        // REVIEW: [rfeng] What's going to happen if there is only one qualified intent
        if (intent.getQualifiedIntents().size() == 1) {
            intent.setDefaultQualifiedIntent(intent.getQualifiedIntents().get(0));
        }
        
        // set all qualified intents as excluding one another if the qualifiable
        // intent is set to be mutually exclusive
        if (intent.isMutuallyExclusive()){
            for (Intent qualifiedIntent : intent.getQualifiedIntents()){
                for (Intent excludedIntent : intent.getQualifiedIntents()){
                    if (qualifiedIntent != excludedIntent){
                        qualifiedIntent.getExcludedIntents().add(excludedIntent);
                   }
                }
            }
        }
        
        return intent;
    }

    public void write(Intent intent, XMLStreamWriter writer, ProcessorContext context) throws ContributionWriteException, XMLStreamException {
        // Write an <sca:intent>
        writer.writeStartElement(PolicyConstants.SCA11_NS, INTENT);
        writer.writeNamespace(intent.getName().getPrefix(), intent.getName().getNamespaceURI());
        writer.writeAttribute(PolicyConstants.NAME, intent.getName().getPrefix() + COLON
            + intent.getName().getLocalPart());
        if (intent.getRequiredIntents() != null && intent.getRequiredIntents().size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (Intent requiredIntents : intent.getRequiredIntents()) {
                sb.append(requiredIntents.getName());
                sb.append(" ");
            }
            writer.writeAttribute(PolicyConstants.REQUIRES, sb.toString());
        }

        if (intent.getExcludedIntents() != null && intent.getExcludedIntents().size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (Intent excludedIntents : intent.getExcludedIntents()) {
                sb.append(excludedIntents.getName());
                sb.append(" ");
            }
            writer.writeAttribute(PolicyConstants.EXCLUDES, sb.toString());
        }

        if (intent.getConstrainedTypes() != null && intent.getConstrainedTypes().size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (ExtensionType contrainedArtifact : intent.getConstrainedTypes()) {
                sb.append(contrainedArtifact.getType().getPrefix());
                sb.append(':').append(contrainedArtifact.getType().getLocalPart());
                sb.append(" ");
            }
            writer.writeAttribute(CONSTRAINS, sb.toString());
        }

        if (intent.getDescription() != null && intent.getDescription().length() > 0) {
            writer.writeStartElement(PolicyConstants.SCA11_NS, DESCRIPTION);
            writer.writeCData(intent.getDescription());
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void resolveContrainedTypes(Intent intent, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        Collection<ExtensionType> resolvedTypes = new HashSet<ExtensionType>();
        for (ExtensionType extensionType : intent.getConstrainedTypes()) {
            if (ExtensionType.BINDING_BASE.equals(extensionType.getType()) || ExtensionType.IMPLEMENTATION_BASE
                .equals(extensionType.getType())) {
                // HACK: Mark sca:binding and sca:implementation as resolved
                extensionType.setUnresolved(false);
                resolvedTypes.add(extensionType);
            } else {
                ExtensionType resolved = resolver.resolveModel(ExtensionType.class, extensionType, context);
                if (!resolved.isUnresolved() || resolved != extensionType) {
                    resolvedTypes.add(resolved);
                } else {
                    warn(context.getMonitor(), "ConstrainedTypeNotFound", intent, extensionType, intent);
                }
            }
        }
        intent.getConstrainedTypes().clear();
        intent.getConstrainedTypes().addAll(resolvedTypes);
    }

    private void resolveProfileIntent(Intent intent, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        Monitor monitor = context.getMonitor();
        // FIXME: Need to check for cyclic references first i.e an A requiring B
        // and then B requiring A...
        if (intent != null && !intent.getRequiredIntents().isEmpty()) {
            // resolve all required intents
            List<Intent> requiredIntents = new ArrayList<Intent>();
            for (Intent required : intent.getRequiredIntents()) {
                if (required.isUnresolved()) {
                    Intent resolved = resolver.resolveModel(Intent.class, required, context);
                    // At this point, when the required intent is not resolved, it does not mean 
                    // its undeclared, chances are that their dependency are not resolved yet. 
                    // Lets try to resolve them first.
                    if (resolved.isUnresolved()) {
                        if (((resolved).getRequiredIntents()).contains(intent)) {
                            error(monitor, "CyclicReferenceFound", resolver, required, intent);
                            return;
                        }
                    }

                    if (!resolved.isUnresolved() || resolved != required) {
                        requiredIntents.add(resolved);
                    } else {
                        error(monitor, "RequiredIntentNotFound", resolver, required, intent);
                        return;
                        //throw new ContributionResolveException("Required Intent - " + requiredIntent
                        //+ " not found for Intent " + policyIntent);
                    }
                } else {
                    requiredIntents.add(required);
                }
            }
            intent.getRequiredIntents().clear();
            intent.getRequiredIntents().addAll(requiredIntents);
        }
    }

    private void resolveQualifiedIntent(Intent qualifed, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        if (qualifed != null) {
            //resolve the qualifiable intent
            Intent parent = qualifed.getQualifiableIntent();
            if (parent == null) {
                return;
            }
            if (parent.isUnresolved()) {
                Intent resolved = resolver.resolveModel(Intent.class, parent, context);
                // At this point, when the qualifiable intent is not resolved, it does not mean 
                // its undeclared, chances are that their dependency are not resolved yet. 
                // Lets try to resolve them first.

                if (!resolved.isUnresolved() || resolved != qualifed) {
                    qualifed.setQualifiableIntent(resolved);
                } else {
                    error(context.getMonitor(), "QualifiableIntentNotFound", resolver, parent, qualifed);
                    //throw new ContributionResolveException("Qualifiable Intent - " + qualifiableIntent
                    //+ " not found for Intent " + policyIntent);
                }
            }
        }
    }

    public void resolve(Intent intent, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        if (intent != null && intent.isUnresolved()) {
            resolveProfileIntent(intent, resolver, context);
            resolveExcludedIntents(intent, resolver, context);
            resolveQualifiedIntent(intent, resolver, context);
            resolveContrainedTypes(intent, resolver, context);
            intent.setUnresolved(false);
        }
    }

    public QName getArtifactType() {
        return POLICY_INTENT_QNAME;
    }

    private void readConstrainedTypes(Intent policyIntent, XMLStreamReader reader) throws ContributionReadException {
        String value = reader.getAttributeValue(null, CONSTRAINS);
        if (value != null) {
            List<ExtensionType> constrainedTypes = policyIntent.getConstrainedTypes();
            for (StringTokenizer tokens = new StringTokenizer(value); tokens.hasMoreTokens();) {
                QName qname = getQNameValue(reader, tokens.nextToken());
                ExtensionType extensionType = policyFactory.createExtensionType();
                extensionType.setType(qname);
                constrainedTypes.add(extensionType);
            }
        }
    }

    private void readRequiredIntents(Intent intent, XMLStreamReader reader, ProcessorContext context) {
        String value = reader.getAttributeValue(null, REQUIRES);
        if (value != null) {
            List<Intent> requiredIntents = intent.getRequiredIntents();
            for (StringTokenizer tokens = new StringTokenizer(value); tokens.hasMoreTokens();) {
                QName qname = getQNameValue(reader, tokens.nextToken());
                Intent required = policyFactory.createIntent();
                required.setName(qname);
                required.setUnresolved(true);
                requiredIntents.add(required);
            }
            
            // Check that a profile intent does not have "." in its name 
            if (requiredIntents.size() > 0) {
                if (intent.getName().getLocalPart().contains(".")){
                    Monitor.error(context.getMonitor(), 
                                  this, 
                                  Messages.RESOURCE_BUNDLE, 
                                  "ProfileIntentNameWithPeriod", 
                                  intent.getName().toString());
                }
            }
        }
    }

    private void readExcludedIntents(Intent intent, XMLStreamReader reader) {
        String value = reader.getAttributeValue(null, EXCLUDES);
        if (value != null) {
            List<Intent> excludedIntents = intent.getExcludedIntents();
            for (StringTokenizer tokens = new StringTokenizer(value); tokens.hasMoreTokens();) {
                QName qname = getQNameValue(reader, tokens.nextToken());
                Intent excluded = policyFactory.createIntent();
                excluded.setName(qname);
                excluded.setUnresolved(true);
                excludedIntents.add(excluded);
            }
        }
    }

    private void resolveExcludedIntents(Intent policyIntent, ModelResolver resolver, ProcessorContext context)
        throws ContributionResolveException {
        if (policyIntent != null) {
            // resolve all excluded intents
            List<Intent> excludedIntents = new ArrayList<Intent>();
            for (Intent excludedIntent : policyIntent.getExcludedIntents()) {
                if (excludedIntent.isUnresolved()) {
                    Intent resolvedExcludedIntent = resolver.resolveModel(Intent.class, excludedIntent, context);
                    if (!resolvedExcludedIntent.isUnresolved() || resolvedExcludedIntent != excludedIntent) {
                        excludedIntents.add(resolvedExcludedIntent);
                    } else {
                        error(context.getMonitor(), "ExcludedIntentNotFound", resolver, excludedIntent, policyIntent);
                        return;
                        //throw new ContributionResolveException("Excluded Intent " + excludedIntent
                        //+ " not found for intent " + policyIntent);
                    }
                } else {
                    excludedIntents.add(excludedIntent);
                }
            }
            policyIntent.getExcludedIntents().clear();
            policyIntent.getExcludedIntents().addAll(excludedIntents);
        }
    }

    public Class<Intent> getModelType() {
        return Intent.class;
    }

}
