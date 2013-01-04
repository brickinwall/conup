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
package org.apache.tuscany.sca.databinding.jaxb.axiom;

import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.tuscany.sca.databinding.jaxb.JAXBContextHelper;

/**
 *
 * @version $Rev: 831163 $ $Date: 2009-10-30 00:04:47 +0000 (Fri, 30 Oct 2009) $
 */
public class JAXBDataSource implements OMDataSource {
    private JAXBContext context;
    private Object element;
    private JAXBContextHelper contextHelper; 

    public JAXBDataSource(Object element, JAXBContext context, JAXBContextHelper contextHelper) {
        this.element = element;
        this.context = context;
        this.contextHelper = contextHelper;
    }

    public XMLStreamReader getReader() throws XMLStreamException {
        // FIXME: [rfeng] This is a quick and dirty implementation
        // We could use the fastinfoset to optimize the roundtrip
        StringWriter writer = new StringWriter();
        serialize(writer, new OMOutputFormat());
        StringReader reader = new StringReader(writer.toString());
        // FIXME: We need to use Tuscany extension point to create the reader
        return StAXUtils.createXMLStreamReader(reader);
    }

    public void serialize(final XMLStreamWriter xmlWriter) throws XMLStreamException {
        try {
            // marshaller.setProperty(Marshaller.JAXB_ENCODING, format.getCharSetEncoding());
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    Marshaller marshaller = null;
                    try {
                        marshaller = contextHelper.getMarshaller(context);
                        marshaller.marshal(element, xmlWriter);
                    } finally {
                        contextHelper.releaseJAXBMarshaller(context, marshaller);
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new XMLStreamException(e.getException());
        }
    }

    public void serialize(final OutputStream output, OMOutputFormat format) throws XMLStreamException {
        try {
            // marshaller.setProperty(Marshaller.JAXB_ENCODING, format.getCharSetEncoding());
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    Marshaller marshaller = null;
                    try {
                        marshaller = contextHelper.getMarshaller(context);
                        marshaller.marshal(element, output);
                    } finally {
                        contextHelper.releaseJAXBMarshaller(context, marshaller);
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new XMLStreamException(e.getException());
        }
    }

    public void serialize(final Writer writer, OMOutputFormat format) throws XMLStreamException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    Marshaller marshaller = null;
                    try {
                        marshaller = contextHelper.getMarshaller(context);
                        marshaller.marshal(element, writer);
                    } finally {
                        contextHelper.releaseJAXBMarshaller(context, marshaller);
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new XMLStreamException(e.getException());
        }
    }

    public Object getObject() {
        return element;
    }

}
