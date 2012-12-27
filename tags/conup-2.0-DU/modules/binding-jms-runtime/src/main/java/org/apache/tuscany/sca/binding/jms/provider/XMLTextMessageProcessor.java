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

import java.io.IOException;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.tuscany.sca.binding.jms.JMSBinding;
import org.apache.tuscany.sca.binding.jms.JMSBindingConstants;
import org.apache.tuscany.sca.binding.jms.JMSBindingException;
import org.apache.tuscany.sca.binding.jms.provider.xml.XMLHelper;
import org.apache.tuscany.sca.binding.jms.provider.xml.XMLHelperFactory;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.util.FaultException;

/**
 * MessageProcessor for sending/receiving XML javax.jms.TextMessage with the JMSBinding.
 * 
 * @version $Rev: 1199797 $ $Date: 2011-11-09 15:31:53 +0000 (Wed, 09 Nov 2011) $
 */
public class XMLTextMessageProcessor extends AbstractMessageProcessor {
    private static final Logger logger = Logger.getLogger(XMLTextMessageProcessor.class.getName());

    private XMLHelper xmlHelper;

    public XMLTextMessageProcessor(JMSBinding jmsBinding, ExtensionPointRegistry registry) {
        super(jmsBinding);
        this.xmlHelper = XMLHelperFactory.createXMLHelper(registry);
    }

    @Override
    protected Object extractPayload(Message msg) {
        try {

            String xml = ((TextMessage)msg).getText();
            Object os;
            if (xml != null) {
                os = xmlHelper.load(xml);
            } else {
                os = null;
            }
            return os;

        } catch (IOException e) {
            throw new JMSBindingException(e);
        } catch (JMSException e) {
            throw new JMSBindingException(e);
        }
    }

    @Override
    public Object extractPayloadFromJMSMessage(Message msg) {
        if (msg instanceof TextMessage) {
            return extractPayload(msg);
        } else {
            return super.extractPayloadFromJMSMessage(msg);
        }
    }

    @Override
    protected Message createJMSMessage(Session session, Object o) {
        if (session == null) {
            logger.fine("no response session to create message: " + String.valueOf(o));
            return null;
        }
        try {

            TextMessage message = session.createTextMessage();

            if (o instanceof Object[]) {
                message.setText(xmlHelper.saveAsString(((Object[])o)[0]));
            } else {
                message.setText(xmlHelper.saveAsString(o));
            }

            return message;

        } catch (JMSException e) {
            throw new JMSBindingException(e);
        }
    }
    
    @Override
    public Message createFaultMessage(Session session, Throwable o) {
        if (session == null) {
            logger.fine("no response session to create fault message: " + String.valueOf(o));
            return null;
        }
        if (o instanceof FaultException) {
            try {

                TextMessage message = session.createTextMessage();
                message.setText(xmlHelper.saveAsString(((FaultException)o).getFaultInfo()));
                message.setBooleanProperty(JMSBindingConstants.FAULT_PROPERTY, true);
                return message;

            } catch (JMSException e) {
                throw new JMSBindingException(e);
            }
        } else {
            return super.createFaultMessage(session, o);
        }
    }

}
