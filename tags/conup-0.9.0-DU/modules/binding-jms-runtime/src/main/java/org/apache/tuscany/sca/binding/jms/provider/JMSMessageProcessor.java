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
package org.apache.tuscany.sca.binding.jms.provider;

import javax.jms.Message;
import javax.jms.Session;

/**
 * Interface for a component that does operation selection and message payload processing
 * 
 * @version $Rev: 676284 $ $Date: 2008-07-13 10:02:10 +0100 (Sun, 13 Jul 2008) $
 */
public interface JMSMessageProcessor {

    /**
     * Get the operation name from a JMS Message
     */
    String getOperationName(Message message);

    /**
     * Set the operation name on a JMS Message
     */
    void setOperationName(String operationName, Message message);

    /**
     * Extracts the payload from a JMS Message
     */
    Object extractPayloadFromJMSMessage(Message msg);

    /**
     * Create a JMS Message containing the payload
     */
    Message insertPayloadIntoJMSMessage(Session session, Object payload);

    /**
     * Create a JMS Message for reporting an exception
     */
    Message createFaultMessage(Session session, Throwable responsePayload);
}
