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
package org.apache.tuscany.sca.invocation;

import java.util.Map;

import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.interfacedef.Operation;

/**
 * Represents a request, response, or exception flowing through a wire
 *
 * @version $Rev $Date: 2011-09-19 23:55:37 +0100 (Mon, 19 Sep 2011) $
 * @tuscany.spi.extension.asclient
 */
public interface Message {

    /**
     * Returns the body of the message, which will be the payload or parameters associated with the wire
     * @return The body of the message
     */
    <T> T getBody();

    /**
     * Sets the body of the message.
     * @param body The body of the message
     */
    <T> void setBody(T body);

    /**
     * Get the end point reference of the source reference
     * @return The end point reference of the reference originating the message
     */
    EndpointReference getFrom();

    /**
     * Set the end point reference of the reference originating the message
     * @param from The end point reference of the reference originating the message
     */
    void setFrom(EndpointReference from);

    /**
     * Get the end point reference of target service
     * @return The end point reference of the service that the message targets
     */
    Endpoint getTo();

    /**
     * Set the end point reference of target service
     * @param to The end point reference of the service that the message targets
     */
    void setTo(Endpoint to);
    
    /**
     * Returns the id of the message
     * @return The message Id
     */
    Object getMessageID();

    /**
     * Sets the id of the message
     * @param messageId The message ID
     */
    void setMessageID(Object messageId);

    /**
     * Determines if the message represents a fault/exception
     *
     * @return true If the message body is a fault object, false if the body is a normal payload
     */
    boolean isFault();

    /**
     * Set the message body with a fault object. After this method is called, isFault() returns true.
     *
     * @param fault The fault object represents an exception
     */
    <T> void setFaultBody(T fault);

    /**
     * Returns the operation that created the message.
     *
     * @return The operation that created the message
     */
    Operation getOperation();

    /**
     * Sets the operation that created the message.
     *
     * @param op The operation that created the message
     */
    void setOperation(Operation op);
    
    /** 
     * Returns a list of objects that are contained in the message header
     * 
     * @return
     */
    Map<String, Object> getHeaders();

    /**
     * Returns the binding context in force for this message
     */
    <T> T getBindingContext();

    /**
     * Set the binding context that is in force for this message
     * @param bindingContext
     */
    <T> void setBindingContext(T bindingContext);
}
