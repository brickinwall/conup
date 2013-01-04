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
package org.apache.tuscany.sca.implementation.widget;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.binding.http.HTTPBinding;
import org.apache.tuscany.sca.binding.http.HTTPBindingFactory;
import org.apache.tuscany.sca.contribution.Artifact;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.processor.BaseStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceContract;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;

/**
 *
 * Implements a StAX artifact processor for Widget implementations.
 *
 * @version $Rev: 1350977 $ $Date: 2012-06-16 19:14:17 +0100 (Sat, 16 Jun 2012) $
 */
public class WidgetImplementationProcessor extends BaseStAXArtifactProcessor implements StAXArtifactProcessor<WidgetImplementation> {
    private ExtensionPointRegistry registry;
    private AssemblyFactory assemblyFactory;
    private ContributionFactory contributionFactory;
    private HTTPBindingFactory httpBindingFactory;
    private JavaInterfaceFactory javaInterfaceFactory;
    private WidgetImplementationFactory implementationFactory;

    public WidgetImplementationProcessor(ExtensionPointRegistry registry) {
        this.registry = registry;
        FactoryExtensionPoint modelFactories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        assemblyFactory = modelFactories.getFactory(AssemblyFactory.class);
        contributionFactory = modelFactories.getFactory(ContributionFactory.class);
        httpBindingFactory = modelFactories.getFactory(HTTPBindingFactory.class);
        javaInterfaceFactory = modelFactories.getFactory(JavaInterfaceFactory.class);
        implementationFactory = modelFactories.getFactory(WidgetImplementationFactory.class);
    }
 
    public QName getArtifactType() {
        // Returns the QName of the XML element processed by this processor
        return WidgetImplementation.TYPE;
    }

    public Class<WidgetImplementation> getModelType() {
        // Returns the type of model processed by this processor
        return WidgetImplementation.class;
    }

    public WidgetImplementation read(XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {

        // Read an <implementation.widget> element

        // Create and initialize the resource implementation model
        WidgetImplementation implementation = implementationFactory.createWidgetImplementation();
        implementation.setUnresolved(true);
        
        // Read the location attribute specifying the location of the resources
        String location = getURIString(reader, "location");
        if (location != null && location.isEmpty() == false) {
            implementation.setLocation(location);
        } else {
            error(context.getMonitor(), "LocationAttributeMissing", reader);
            //throw new ContributionReadException(MSG_LOCATION_MISSING);
        }
        
        String widgetUri = getURIString(reader, "widgetUri");
        if(widgetUri != null && widgetUri.isEmpty() == false) {
            implementation.setWidgetUri(widgetUri);
        } else {
            warning(context.getMonitor(), "UocationAttributeMissing", reader);
        }

        // Skip to end element
        while (reader.hasNext()) {
            if (reader.next() == END_ELEMENT && WidgetImplementation.TYPE.equals(reader.getName())) {
                break;
            }
        }

        return implementation;
    }

    public void resolve(WidgetImplementation implementation, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {

        if (implementation != null) {
            // Resolve the resource directory location
            Artifact artifact = contributionFactory.createArtifact();
            artifact.setURI(implementation.getLocation());
            Artifact resolved = resolver.resolveModel(Artifact.class, artifact, context);

            if(resolved.getLocation() == null) {
                URL resource = null;
                URI uri = URI.create(implementation.getLocation());
                Artifact parent = context.getArtifact();
                if (!uri.isAbsolute()) {
                    if (parent != null && parent.getURI() != null) {
                        URI base = URI.create("/" + parent.getURI());
                        uri = base.resolve(uri);
                        // Remove the leading / to make artifact resolver happy
                        if (uri.toString().startsWith("/")) {
                            uri = URI.create(uri.toString().substring(1));
                        }
                    }
                }


                // The resource can be out of scope of the contribution root
                if (parent != null && parent.getLocation() != null) {
                    try {
                        resource = new URL(new URL(parent.getLocation()), implementation.getLocation());

                        implementation.setLocationURL(resource);

                    } catch (MalformedURLException e) {
                        ContributionResolveException ce = new ContributionResolveException(e);
                        error(context.getMonitor(), "ContributionResolveException", resolver, ce);
                    }
                }
            } else {
                try {
                    implementation.setLocationURL(new URL(resolved.getLocation()));
                } catch (MalformedURLException e) {
                    ContributionResolveException ce = new ContributionResolveException(e);
                    error(context.getMonitor(), "ContributionResolveException", resolver, ce);
                }
            }

            //introspect implementation
            WidgetImplementationIntrospector widgetIntrospector = new WidgetImplementationIntrospector(registry, implementation);
            widgetIntrospector.introspectImplementation();

            implementation.setUnresolved(false);

            if(implementation.getWidgetUri() != null && implementation.getWidgetUri().isEmpty() == false) {
                addService(implementation);
            }


            if (implementation.isUnresolved()) {
                error(context.getMonitor(), "CouldNotResolveLocation", resolver, implementation.getLocation());
                //throw new ContributionResolveException("Could not resolve implementation.widget location: " + implementation.getLocation());
            }
        }
    }

    public void write(WidgetImplementation implementation, XMLStreamWriter writer, ProcessorContext context) throws ContributionWriteException, XMLStreamException {
        // Write <implementation.widget>        
        writeStart(writer, WidgetImplementation.TYPE.getNamespaceURI(), WidgetImplementation.TYPE.getLocalPart());

        if (implementation.getLocation() != null && implementation.getLocation().isEmpty() == false) {
            writer.writeAttribute("location", implementation.getLocation());
        }

        if (implementation.getWidgetUri() != null && implementation.getWidgetUri().isEmpty() == false) {
            writer.writeAttribute("widgetUri", implementation.getWidgetUri());
        }
        
        writeEnd(writer);
    }
    
    /**
     * Utility methods
     */
    
    private void addService(WidgetImplementation implementation) {
        Service widgetService = null;
        
        // Resource implementation always provide a single service exposing
        // the Resource interface, and have no references and properties
        widgetService = assemblyFactory.createService();
        widgetService.setName("Widget");
        
        // Create the Java interface contract for the Resource service
        JavaInterface javaInterface;
        try {
            javaInterface = javaInterfaceFactory.createJavaInterface(Widget.class);
        } catch (InvalidInterfaceException e) {
            throw new IllegalArgumentException(e);
        }
        JavaInterfaceContract interfaceContract = javaInterfaceFactory.createJavaInterfaceContract();
        interfaceContract.setInterface(javaInterface);
        widgetService.setInterfaceContract(interfaceContract);

        HTTPBinding binding = httpBindingFactory.createHTTPBinding();
        binding.setURI(implementation.getWidgetUri());
        widgetService.getBindings().add(binding);

        implementation.getServices().add(widgetService);
        
    }
    
    private void warning(Monitor monitor, String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      "impl-widget-validation-messages",
                                      Severity.WARNING,
                                      model,
                                      message,
                                      (Object[])messageParameters);
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
                                      "impl-widget-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      (Object[])messageParameters);
            monitor.problem(problem);
        }
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
                                      "impl-widget-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      ex);
            monitor.problem(problem);
        }
    }

}
