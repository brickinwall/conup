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

package org.apache.tuscany.sca.implementation.bpel.ode.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.implementation.bpel.ode.EmbeddedODEServer;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implements a target invoker for BPEL component implementations.
 *
 * The target invoker is responsible for dispatching invocations to the particular
 * component implementation logic. In this example we are simply delegating the
 * CRUD operation invocations to the corresponding methods on our fake
 * resource manager.
 *
 * @version $Rev: 910275 $ $Date: 2010-02-15 17:34:56 +0000 (Mon, 15 Feb 2010) $
 */
public class BPELInvoker implements Invoker {
    private final static long TIME_OUT = 10000L;
	
    protected final Log __log = LogFactory.getLog(getClass());

    private EmbeddedODEServer odeServer;
    private TransactionManager txMgr;

    private RuntimeComponentService service;
    private Operation 				operation;
    private QName 					bpelServiceName;
    private String 					bpelOperationName;
    private Part 					bpelOperationInputPart;
    private Part 					bpelOperationOutputPart;
    private RuntimeComponent 		component;
    // Marks if this service has a callback interface
    private Boolean					isCallback = false;
    private EndpointReference 		callbackEPR;

    public BPELInvoker(RuntimeComponent component, RuntimeComponentService service, Operation operation, 
    		           EmbeddedODEServer odeServer, TransactionManager txMgr) {
        this.service = service;
        this.component = component;
        this.operation = operation;
        this.bpelOperationName = operation.getName();
        this.odeServer = odeServer;
        this.txMgr = txMgr;
        this.isCallback = serviceHasCallback( service );

        initializeInvocation();
    } // end method BPELInvoker
    
    private boolean serviceHasCallback( RuntimeComponentService service ) {
    	if(service.getInterfaceContract().getCallbackInterface() != null) return true;
    	return false;
    } // end method serviceHasCallback

    private void initializeInvocation() {

        __log.debug("Initializing BPELInvoker");

        Interface interfaze = operation.getInterface();
        if(interfaze instanceof WSDLInterface){
            WSDLInterface wsdlInterface = null;
            wsdlInterface = (WSDLInterface) interfaze;

            // Fetch the service name from the service object - including the componentURI guarantees a unique service name
            String componentURI = component.getURI();
            bpelServiceName = new QName( Base.SCA11_TUSCANY_NS, componentURI + service.getName() );

            bpelOperationInputPart = (Part) wsdlInterface.getPortType().getOperation(bpelOperationName,null,null).getInput().getMessage().getParts().values().iterator().next();
            bpelOperationOutputPart = (Part) wsdlInterface.getPortType().getOperation(bpelOperationName,null,null).getOutput().getMessage().getParts().values().iterator().next();
        }
    } // end method initializeInvocation

    public Message invoke(Message msg) {
        try {
            if( isCallback ) {
                // Extract the callback endpoint metadata
                callbackEPR = msg.getFrom();
            } // end if
            Object[] args = msg.getBody();
            Object resp = doTheWork(args);
            msg.setBody(resp);
        } catch (InvocationTargetException e) {
            msg.setFaultBody(e.getCause());
        }
        return msg;
    }

    public Object doTheWork(Object[] args) throws InvocationTargetException {
        Element response = null;

        if(! (operation.getInterface() instanceof WSDLInterface)) {
            throw new InvocationTargetException(null,"Unsupported service contract");
        }

        org.apache.ode.bpel.iapi.MyRoleMessageExchange mex = null;
        Future<?> onhold = null;

        //Process the BPEL process invocation
        Long processID = 0L;
        try {
            txMgr.begin();
            mex = odeServer.getBpelServer().getEngine().createMessageExchange(new GUID().toString(),
                                                                              bpelServiceName,
                                                                              bpelOperationName);
            //TODO - this will not be true for OneWay operations - need to handle those
            mex.setProperty("isTwoWay", "true");
            onhold = mex.invoke(createInvocationMessage(mex, args));
            
            txMgr.commit();
            // Deal with callback cases - store the callback metadata by process instance ID
            if( isCallback ) {
            	processID = odeServer.getProcessIDFromMex(mex.getMessageExchangeId());
                // Store the callback metadata for this invocation
            	odeServer.saveCallbackMetadata( processID, service.getName(), callbackEPR );
            } // end if
        } catch (Exception e) {
            try {
                txMgr.rollback();
            } catch (SystemException se) {

            }
            throw new InvocationTargetException(e, "Error invoking BPEL process : " + e.getMessage());
        } // end try

        // Waiting until the reply is ready in case the engine needs to continue in a different thread
        if (onhold != null) {
            try {
            	//add timeout to avoid blocking when there is a exception/failure
            	onhold.get(TIME_OUT, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new InvocationTargetException(e,"Error invoking BPEL process : " + e.getMessage());
            } // end try
        } // end if

        //Process the BPEL invocation response
        try {
            txMgr.begin();
            // Reloading the mex in the current transaction, otherwise we can't
            // be sure we have the "freshest" one.
            mex = (MyRoleMessageExchange)odeServer.getBpelServer().getEngine().getMessageExchange(mex.getMessageExchangeId());

            Status status = mex.getStatus();

            switch (status) {
            case FAULT:
                if (__log.isDebugEnabled())
                    __log.debug("Fault response message: " + mex.getFault());
                    throw new ODEInvocationException("FAULT received from BPEL process : " + mex.getFault()
                        + " "
                        + mex.getFaultExplanation());
            case ASYNC:
            case RESPONSE:
                //process the method invocation result
                response = processResponse(mex.getResponse().getMessage());
                if (__log.isDebugEnabled())
                    __log.debug("Response message " + response);
                break;
            case FAILURE:
                if (__log.isDebugEnabled())
                    __log.debug("Failure response message: " + mex.getFault());
                break;
            default:
                throw new ODEInvocationException("FAILURE received from BPEL process : " + mex.getStatus() + " - " + mex.getFault());
            } // end switch

            txMgr.commit();
            // end of transaction two
        } catch (Exception e) {
            try {
                txMgr.rollback();
            } catch (SystemException se) {

            }
            throw new InvocationTargetException(e, "Error retrieving BPEL process invocation status : " + e.getMessage());
        } // end try

        // Cleanup the ODE MessageExchange object
        //mex.release();

        return response;
    }

    /**
     * Create BPEL Invocation message
     *
     *  BPEL invocation message like :
     *  <message>
     *     <TestPart>
     *        <hello xmlns="http://tuscany.apache.org/implementation/bpel/example/helloworld.wsdl">Hello</hello>
     *     </TestPart>
     *   </message>
     * @param args
     * @return
     */
    private org.apache.ode.bpel.iapi.Message createInvocationMessage(org.apache.ode.bpel.iapi.MyRoleMessageExchange mex, Object[] args) {
        Document dom = DOMUtils.newDocument();

        Element contentMessage = dom.createElement("message");
        Element contentPart = dom.createElement(bpelOperationInputPart.getName());
        Element payload = null;

        // TODO handle WSDL input messages with multiple Parts...
        //TUSCANY-2321 - Properly handling Document or Element types
        if(args[0] instanceof Document) {
            payload = (Element) ((Document) args[0]).getFirstChild();
        } else {
            payload = (Element) args[0];
        }

        contentPart.appendChild(dom.importNode(payload, true));
        contentMessage.appendChild(contentPart);
        dom.appendChild(contentMessage);

        if (__log.isDebugEnabled()) {
            __log.debug("Creating invocation message:");
            __log.debug(">> args.....: " + DOMUtils.domToString(payload));
            __log.debug(">> message..:" + DOMUtils.domToString(dom.getDocumentElement()));
        }

        org.apache.ode.bpel.iapi.Message request = mex.createMessage(new QName("", ""));
        request.setMessage(dom.getDocumentElement());

        return request;
    }

    /**
     * Process BPEL response
     *
     *  <message>
     *     <TestPart>
     *        <hello xmlns="http://tuscany.apache.org/implementation/bpel/example/helloworld.wsdl">World</hello>
     *     </TestPart>
     *   </message>
     *
     * @param response
     * @return
     */
    private Element processResponse(Element response) {
    	return (Element) DOMUtils.findChildByName(response, new QName("",bpelOperationOutputPart.getName())).getFirstChild();

    	// MJE, 12/06/2009 - changed to return the message without the PART wrapper element, since this element is not
    	// transmitted in the SOAP messages on the wire
    } // end method processResponse
} // end class BPELInvoker
