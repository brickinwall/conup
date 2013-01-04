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
package org.apache.tuscany.sca.contribution.processor;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.Extension;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;

/**
 * Implementation of an extensible StAX attribute processor.
 *
 * Takes a StAXAttributeProcessorExtensionPoint and delegates to the proper
 * StAXAttributeProcessor by attribute QName
 *
 * @version $Rev: 1021805 $ $Date: 2010-10-12 16:50:33 +0100 (Tue, 12 Oct 2010) $
 */
public class ExtensibleStAXAttributeProcessor implements StAXAttributeProcessor<Object> {

    private static final Logger logger = Logger.getLogger(ExtensibleStAXAttributeProcessor.class.getName());
    private static final String XMLSCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
    public static final QName ANY_ATTRIBUTE = new QName(XMLSCHEMA_NS, "anyAttribute");

    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;
    private StAXAttributeProcessorExtensionPoint processors;
    /**
     * Constructs a new ExtensibleStAXArtifactProcessor.
     * @param processors
     * @param inputFactory
     * @param outputFactory
     */
    public ExtensibleStAXAttributeProcessor(StAXAttributeProcessorExtensionPoint processors,
                                            XMLInputFactory inputFactory,
                                            XMLOutputFactory outputFactory) {
        super();
        this.processors = processors;
        this.inputFactory = inputFactory;
        this.outputFactory = outputFactory;
        if (this.outputFactory != null) {
            this.outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        }
    }

    /**
     * Report a warning.
     *
     * @param problems
     * @param message
     * @param model
     */
    private void warning(Monitor monitor, String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      "contribution-validation-messages",
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
                                      "contribution-validation-messages",
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
                                      "contribution-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      ex);
            monitor.problem(problem);
        }
    }

    public Object read(QName attributeName, XMLStreamReader source, ProcessorContext context) throws ContributionReadException,
        XMLStreamException {
        // Delegate to the processor associated with the attribute QName
        int event = source.getEventType();
        if (event == XMLStreamConstants.START_DOCUMENT) {
            while (source.next() != XMLStreamReader.START_ELEMENT);
        }

        StAXAttributeProcessor<?> processor = null;

        //lookup for registered attribute processors
        processor = (StAXAttributeProcessor<?>)processors.getProcessor(attributeName);
        if (processor == null) {
            Location location = source.getLocation();
            // Skip xsi:type, xsi:nil and xsi:schemaLocation etc
            if (!W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(attributeName.getNamespaceURI())) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning("Attribute " + attributeName + " cannot be processed. (" + location + ")");
                }
                warning(context.getMonitor(), "AttributeCannotBeProcessed", processors, attributeName, location);
            }
        } else {
            return processor.read(attributeName, source, context);
        }

        //handle extension attributes without processors
        processor = (StAXAttributeProcessor<?>)processors.getProcessor(ANY_ATTRIBUTE);
        if (processor == null) {
            Location location = source.getLocation();
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Could not find Default Attribute processor !");
            }
            warning(context.getMonitor(), "DefaultAttributeProcessorNotAvailable", processors, ANY_ATTRIBUTE, location);
        }

        return processor == null ? null : processor.read(attributeName, source, context);
    }

    @SuppressWarnings("unchecked")
    public void write(Object model, XMLStreamWriter outputSource, ProcessorContext context) throws ContributionWriteException, XMLStreamException {

        if (model == null) {
            return;
        }

        // Delegate to the processor associated with the model type
        StAXAttributeProcessor processor = processors.getProcessor(model.getClass());
        if (processor == null) {
            if (!Extension.class.isInstance(model)) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning("No StAX processor is configured to handle " + model.getClass());
                }
                warning(context.getMonitor(), "NoStaxProcessor", processors, model.getClass());
            }
        } else {
            processor.write(model, outputSource, context);
            return;
        }

        //handle extension attributes without processors
        processor = (StAXAttributeProcessor<?>)processors.getProcessor(ANY_ATTRIBUTE);
        if (processor == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("No Default StAX processor is configured to handle " + model.getClass());
            }
            warning(context.getMonitor(), "NoDefaultStaxProcessor", processors, model.getClass());
        } else {
            processor.write(model, outputSource, context);
            return;
        }
    }

    @SuppressWarnings("unchecked")
    public void resolve(Object model, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {

        // Delegate to the processor associated with the model type
        if (model != null) {
            StAXAttributeProcessor processor = processors.getProcessor(model.getClass());
            if (processor != null) {
                processor.resolve(model, resolver, context);
            }
        }
    }

    /**
     * Read a model from an InputStream.
     * @param is The artifact InputStream
     * @param type Model type
     * @return The model
     * @throws ContributionReadException
     */
    public <M> M read(InputStream is, Class<M> type) throws ContributionReadException {
        return null;
    }

    /**
     * Write a model to an OutputStream.
     * @param model
     * @param os
     * @throws ContributionWriteException
     */
    public void write(Object model, OutputStream os, ProcessorContext context) throws ContributionWriteException {
        try {
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(os);
            write(model, writer, context);
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            ContributionWriteException cw = new ContributionWriteException(e);
            error(context.getMonitor(), "ContributionWriteException", outputFactory, cw);
            throw cw;
        }
    }

    public QName getArtifactType() {
        return null;
    }

    public Class<Object> getModelType() {
        return Object.class;
    }
}
