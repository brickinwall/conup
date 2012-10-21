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

package org.apache.tuscany.sca.interfacedef.java.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceClient;

import org.apache.tuscany.sca.assembly.xml.Messages;
import org.apache.tuscany.sca.assembly.xml.PolicySubjectProcessor;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ClassReference;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.interfacedef.Compatibility;
import org.apache.tuscany.sca.interfacedef.IncompatibleInterfaceContractException;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceContract;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLFactory;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterfaceContract;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.oasisopen.sca.annotation.AllowsPassByReference;
import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.ComponentName;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Intent;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Qualifier;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;
import org.oasisopen.sca.annotation.Service;

/**
 *
 * @version $Rev: 1295144 $ $Date: 2012-02-29 15:06:57 +0000 (Wed, 29 Feb 2012) $
 */
public class JavaInterfaceProcessor implements StAXArtifactProcessor<JavaInterfaceContract>, JavaConstants {
    private static final String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200912";
    
    private JavaInterfaceFactory javaFactory;
    private ExtensionPointRegistry extensionPoints;
    private PolicyFactory policyFactory;
    private PolicySubjectProcessor policyProcessor;
    private WSDLFactory wsdlFactory;
    private StAXArtifactProcessor<Object> extensionProcessor;
    private transient InterfaceContractMapper interfaceContractMapper;

    public JavaInterfaceProcessor(ExtensionPointRegistry extensionPoints,  StAXArtifactProcessor<?> staxProcessor) {
        this.extensionPoints = extensionPoints;
        FactoryExtensionPoint modelFactories = extensionPoints.getExtensionPoint(FactoryExtensionPoint.class);
        this.policyFactory = modelFactories.getFactory(PolicyFactory.class);
        this.policyProcessor = new PolicySubjectProcessor(policyFactory);
        this.javaFactory = modelFactories.getFactory(JavaInterfaceFactory.class);
        this.wsdlFactory = modelFactories.getFactory(WSDLFactory.class);
        this.extensionProcessor = (StAXArtifactProcessor<Object>)staxProcessor;
        UtilityExtensionPoint utilities = extensionPoints.getExtensionPoint(UtilityExtensionPoint.class);
        this.interfaceContractMapper = utilities.getUtility(InterfaceContractMapper.class);
    }
    
    /**
     * Report a exception.
     * 
     * @param problems
     * @param message
     * @param model
     */
    private void error(Monitor monitor, String message, Object model, Exception ex) {
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      "interface-javaxml-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      ex);
            monitor.problem(problem);
        }
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
                                      "interface-javaxml-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      (Object[])messageParameters);
            monitor.problem(problem);
        }
    }
    
    private JavaInterface createJavaInterface(String interfaceName) {
        JavaInterface javaInterface = javaFactory.createJavaInterface();
        javaInterface.setUnresolved(true);
        javaInterface.setName(interfaceName);
        return javaInterface;
    }

    public JavaInterfaceContract read(XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {
        
        // Read an <interface.java>
        JavaInterfaceContract javaInterfaceContract = javaFactory.createJavaInterfaceContract();
        String interfaceName = reader.getAttributeValue(null, INTERFACE);
        if (interfaceName != null) {
            JavaInterface javaInterface = createJavaInterface(interfaceName);
            javaInterfaceContract.setInterface(javaInterface);
        }

        String callbackInterfaceName = reader.getAttributeValue(null, CALLBACK_INTERFACE);
        if (callbackInterfaceName != null) {
            JavaInterface javaCallbackInterface = createJavaInterface(callbackInterfaceName);
            javaInterfaceContract.setCallbackInterface(javaCallbackInterface);
        }

        String remotable = reader.getAttributeValue(null, REMOTABLE);
        if (remotable != null) {
            javaInterfaceContract.getInterface().setRemotable(Boolean.parseBoolean(remotable));
            javaInterfaceContract.getInterface().setRemotableSetFromSCDL();
        }
        
        // Read intents and policy sets
        policyProcessor.readPolicies(javaInterfaceContract.getInterface(), reader);

        // Skip to end element
        while (reader.hasNext()) {
            if (reader.next() == END_ELEMENT && INTERFACE_JAVA_QNAME.equals(reader.getName())) {
                break;
            }
        }
        return javaInterfaceContract;
    }
    
    public void write(JavaInterfaceContract javaInterfaceContract, XMLStreamWriter writer, ProcessorContext context) throws ContributionWriteException, XMLStreamException {
        
        // Write an <interface.java>
        writer.writeStartElement(SCA11_NS, INTERFACE_JAVA);
        JavaInterface javaInterface = (JavaInterface)javaInterfaceContract.getInterface();
        
        if (javaInterface != null && javaInterface.getName() != null) {
            writer.writeAttribute(INTERFACE, javaInterface.getName());
        }
        
        if(javaInterface != null && javaInterface.isRemotableSetFromSCDL()) {
            writer.writeAttribute(REMOTABLE, String.valueOf(javaInterface.isRemotable()));
        }
        
        JavaInterface javaCallbackInterface = (JavaInterface)javaInterfaceContract.getCallbackInterface();
        if (javaCallbackInterface != null && javaCallbackInterface.getName() != null) {
            writer.writeAttribute(CALLBACK_INTERFACE, javaCallbackInterface.getName());
        }
        
        policyProcessor.writePolicyAttributes(javaInterface, writer);
        
        writer.writeEndElement();
    }
    
    private JavaInterface resolveJavaInterface(JavaInterface javaInterface, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        
        if (javaInterface != null && javaInterface.isUnresolved()) {
            Monitor monitor = context.getMonitor();
            // Resolve the Java interface
            javaInterface = resolver.resolveModel(JavaInterface.class, javaInterface, context);
            if (javaInterface.isUnresolved()) {

                // If the Java interface has never been resolved yet, do it now
                ClassReference classReference = new ClassReference(javaInterface.getName());
                classReference = resolver.resolveModel(ClassReference.class, classReference, context);
                Class<?> javaClass = classReference.getJavaClass();
                if (javaClass == null) {
                    error(monitor, "ClassNotFoundException", resolver, javaInterface.getName());
                    return javaInterface;
                    //throw new ContributionResolveException(new ClassNotFoundException(javaInterface.getName()));
                }
                
                try {                        
                    // Introspect the Java interface and populate the interface and
                    // operations
                    javaFactory.createJavaInterface(javaInterface, javaClass);
                    
                    // cache the contribution that was used to resolve the Java interface
                    // in case we need it to reolve a referenced WSDL file
                    javaInterface.setContributionContainingClass(classReference.getContributionContainingClass());
                
                } catch (InvalidInterfaceException e) {
                	ContributionResolveException ce = new ContributionResolveException(context.getMonitor().getMessageString(JavaInterfaceProcessor.class.getName(),
                	                                                                                                         "interface-javaxml-validation-messages", 
                                                                                                                             "ResolvingInterface") + 
                                                                                       javaInterface.getName(), e);
                	error(monitor, "InvalidInterfaceException", javaFactory, e);
                    return javaInterface;
                } catch ( Exception e ) {
                	throw new ContributionResolveException(context.getMonitor().getMessageString(JavaInterfaceProcessor.class.getName(),
                	                                                                             "interface-javaxml-validation-messages", 
                                                                                                 "ResolvingInterface") +
                                                           " " +
                                                           javaInterface.getName(), e );
                } // end try

                // Cache the resolved interface
                javaInterface.setUnresolved(false);
                resolver.addModel(javaInterface, context);
            }
        }
        return javaInterface;
    }

    public void resolve(JavaInterfaceContract javaInterfaceContract, ModelResolver resolver, ProcessorContext context)
        throws ContributionResolveException {
        try {
            Monitor monitor = context.getMonitor();
            // Resolve the interface and callback interface
            JavaInterface javaInterface =
                resolveJavaInterface((JavaInterface)javaInterfaceContract.getInterface(), resolver, context);
            javaInterfaceContract.setInterface(javaInterface);

            JavaInterface javaCallbackInterface =
                resolveJavaInterface((JavaInterface)javaInterfaceContract.getCallbackInterface(), resolver, context);
            javaInterfaceContract.setCallbackInterface(javaCallbackInterface);
            
            postJAXWSProcessorResolve(javaInterfaceContract, resolver, context);
	        
	        checkForbiddenAnnotations(monitor, javaInterfaceContract);
	        
        } catch (Exception e) {
            throw new ContributionResolveException(context.getMonitor().getMessageString(JavaInterfaceProcessor.class.getName(),
                                                                                         "interface-javaxml-validation-messages",
                                                                                         "ResolvingInterface") + 
                                                   " " +
                                                   javaInterfaceContract.getInterface().toString(), e);
        } // end try
    }
    
    private static List<Class<?>> JCA30006_ANNOTATIONS =
        Arrays.asList(new Class<?>[] {AllowsPassByReference.class, ComponentName.class, Constructor.class, Context.class,
                                   Destroy.class, EagerInit.class, Init.class, Intent.class, Property.class, Qualifier.class,
                                   Reference.class, Scope.class, Service.class});
    private static List<Class<?>> JCA30007_ANNOTATIONS =
        Arrays.asList(new Class<?>[] {AllowsPassByReference.class, Callback.class, ComponentName.class, Constructor.class,
                                   Context.class, Destroy.class, EagerInit.class, Init.class, Intent.class,
                                   Property.class, Qualifier.class, Reference.class, Scope.class, Service.class}); 
    private static List<Class<?>> JCA30008_ANNOTATIONS = Arrays.asList(new Class<?>[] {Intent.class, Qualifier.class}); 
    
    private void checkForbiddenAnnotations(Monitor monitor, JavaInterfaceContract javaInterfaceContract) {
        if (javaInterfaceContract.getInterface() == null) {
            return;
        }
        Class<?> ifc = ((JavaInterface) javaInterfaceContract.getInterface()).getJavaClass();
        if (ifc == null) {
            return;
        }
        for (Annotation a : ifc.getAnnotations()) {
            if (ifc.isInterface()) {
                if (JCA30006_ANNOTATIONS.contains(a.annotationType())) {
                    error(monitor, "ForbiddenAnnotationJCA30006", javaInterfaceContract, a.annotationType(), ifc.getName());
                }
            } else {
                if (JCA30008_ANNOTATIONS.contains(a.annotationType())) {
                    error(monitor, "ForbiddenAnnotationJCA30008", javaInterfaceContract, a.annotationType(), ifc.getName());
                }
            }
            
            if ( a.annotationType().equals(WebServiceClient.class) ) {
            	error(monitor, "ForbiddenAnnotationJCA100018", javaInterfaceContract, a.annotationType(), ifc.getName());
            }
            	
        }
        for (Method m : ifc.getMethods()) {
            for (Annotation a : m.getAnnotations()) {
                if (ifc.isInterface()) {
                    if (JCA30006_ANNOTATIONS.contains(a.annotationType())) {
                        error(monitor, "ForbiddenAnnotationJCA30006", javaInterfaceContract, a.annotationType(), ifc.getName());
                    }
                } else {
                    if (JCA30008_ANNOTATIONS.contains(a.annotationType())) {
                        error(monitor, "ForbiddenAnnotationJCA30008", javaInterfaceContract, a.annotationType(), ifc.getName());
                    }
                }
            }
        }
        for (Field f : ifc.getFields()) {
            for (Annotation a : f.getAnnotations()) {
                if (ifc.isInterface()) {
                    if (JCA30006_ANNOTATIONS.contains(a.annotationType())) {
                        error(monitor, "ForbiddenAnnotationJCA30006", javaInterfaceContract, a.annotationType(), ifc.getName());
                    }
                } else {
                    if (JCA30008_ANNOTATIONS.contains(a.annotationType())) {
                        error(monitor, "ForbiddenAnnotationJCA30008", javaInterfaceContract, a.annotationType(), ifc.getName());
                    }
                }
            }
        }
        
        if (javaInterfaceContract.getCallbackInterface() == null) {
            return;
        }
        ifc = ((JavaInterface) javaInterfaceContract.getCallbackInterface()).getJavaClass();
        if (ifc == null) {
            return;
        }

        for (Annotation a : ifc.getAnnotations()) {
            if (ifc.isInterface()) {
                if (JCA30007_ANNOTATIONS.contains(a.annotationType())) {
                    error(monitor, "ForbiddenAnnotationJCA30007", javaInterfaceContract, a.annotationType(), ifc.getName());
                }
            } else {
                if (JCA30008_ANNOTATIONS.contains(a.annotationType())) {
                    error(monitor, "ForbiddenAnnotationJCA30008", javaInterfaceContract, a.annotationType(), ifc.getName());
                }
            }
        }
        for (Method m : ifc.getMethods()) {
            for (Annotation a : m.getAnnotations()) {
                if (ifc.isInterface()) {
                    if (JCA30007_ANNOTATIONS.contains(a.annotationType())) {
                        error(monitor, "ForbiddenAnnotationJCA30007", javaInterfaceContract, a.annotationType(), ifc.getName());
                    }
                } else {
                    if (JCA30008_ANNOTATIONS.contains(a.annotationType())) {
                        error(monitor, "ForbiddenAnnotationJCA30008", javaInterfaceContract, a.annotationType(), ifc.getName());
                    }
                }
            }
        }
        for (Field f : ifc.getFields()) {
            for (Annotation a : f.getAnnotations()) {
                if (ifc.isInterface()) {
                    if (JCA30007_ANNOTATIONS.contains(a.annotationType())) {
                        error(monitor, "ForbiddenAnnotationJCA30007", javaInterfaceContract, a.annotationType(), ifc.getName());
                    }
                } else {
                    if (JCA30008_ANNOTATIONS.contains(a.annotationType())) {
                        error(monitor, "ForbiddenAnnotationJCA30008", javaInterfaceContract, a.annotationType(), ifc.getName());
                    }
                }
            }
        }
    }

    public QName getArtifactType() {
        return INTERFACE_JAVA_QNAME;
    }
    
    public Class<JavaInterfaceContract> getModelType() {
        return JavaInterfaceContract.class;
    }
    
    private void postJAXWSProcessorResolve(JavaInterfaceContract javaInterfaceContract, ModelResolver resolver, ProcessorContext context)
        throws ContributionResolveException, IncompatibleInterfaceContractException {
        
        JavaInterface javaInterface = (JavaInterface)javaInterfaceContract.getInterface();
        
        // the Java interface may now be marked as unresolved due to a new Java interface 
        // name retrieved from JAXWS annotations. Resolve it again if it is.
        if (javaInterface != null && javaInterface.isUnresolved()){
            javaInterface = resolveJavaInterface(javaInterface, resolver, context);
            javaInterfaceContract.setInterface(javaInterface);
        }
        
        JavaInterface javaCallbackInterface = (JavaInterface)javaInterfaceContract.getCallbackInterface();
        // the Java callback interface may now be marked as unresolved due to a new Java interface 
        // name retrieved from JAXWS annotations. Resolve it again if it is.
        if (javaCallbackInterface != null && javaCallbackInterface.isUnresolved()){
            javaCallbackInterface = resolveJavaInterface(javaCallbackInterface, resolver, context);
            javaInterfaceContract.setCallbackInterface(javaCallbackInterface);
        }
        
        // the Java interface may be replaced by a WSDL contract picked up from JAXWS annotation
        // if so we need to fluff up a WSDL contract and set it to be the normalized contract
        // for the Java interface so it's used during contract mapping
        if (javaInterface != null && javaInterface.getJAXWSWSDLLocation() != null){
            WSDLInterface wsdlInterface = wsdlFactory.createWSDLInterface();
            wsdlInterface.setUnresolved(true);
            wsdlInterface.setRemotable(true);
            
            WSDLInterfaceContract wsdlInterfaceContract = wsdlFactory.createWSDLInterfaceContract();
            wsdlInterfaceContract.setInterface(wsdlInterface);
            wsdlInterfaceContract.setLocation(javaInterface.getJAXWSWSDLLocation());
            javaInterfaceContract.setNormalizedWSDLContract(wsdlInterfaceContract);
            
            ProcessorContext wsdlContext = new ProcessorContext(javaInterface.getContributionContainingClass(), 
                                                                context.getMonitor());
            extensionProcessor.resolve(wsdlInterfaceContract, resolver, wsdlContext);
            
            // check that the Java and WSDL contracts are compatible
            interfaceContractMapper.checkCompatibility(wsdlInterfaceContract,
                                                       javaInterfaceContract, 
                                                       Compatibility.SUBSET, 
                                                       false, 
                                                       false);
            
            // copy policy from the WSDL interface to the Java interface
            javaInterface.getPolicySets().addAll(wsdlInterface.getPolicySets());
            javaInterface.getRequiredIntents().addAll(wsdlInterface.getRequiredIntents());
            
           // TODO - is there anything else to be copied from the user specified WSDL?
            
        }
        
        // TODO - how to handle callbacks as the location is stored at the contract level?        
    }      
}
