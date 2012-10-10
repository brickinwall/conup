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

package org.apache.tuscany.sca.implementation.script.provider;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.axiom.om.OMElement;
import org.apache.bsf.xml.XMLHelper;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;

/**
 * Perform the actual script invocation
 *
 * @version $Rev: 951651 $ $Date: 2010-06-05 06:17:47 +0100 (Sat, 05 Jun 2010) $
 */
public class ScriptInvoker implements Invoker {

    private ScriptEngine scriptEngine;
    private XMLHelper xmlHelper;
    private Operation operation;

    public ScriptInvoker(ScriptEngine scriptEngine, XMLHelper xmlHelper, Operation operation) {
        this.scriptEngine = scriptEngine;
        this.xmlHelper = xmlHelper;
        this.operation = operation;
    }

    private Object doInvoke(Object[] objects, Operation op) throws ScriptException {
        if (xmlHelper != null) {
            objects[0] = xmlHelper.toScriptXML((OMElement)objects[0]);
        }

        Operation oper = operation;  // static setting
        if (oper.getName() == null) {  // if no static setting
            oper = op;  // use dynamic setting
        }
        Object response;
        try {
            response = ((Invocable)scriptEngine).invokeFunction(oper.getName(), objects);
        } catch (ScriptException e) {
            throw e;
        } catch (Exception e) {
            throw new ScriptException(e);
        }

        if (xmlHelper != null) {
            response = xmlHelper.toOMElement(response);
        }

        return response;
    }

    public Message invoke(Message msg) {
        try {
            Object resp = doInvoke((Object[])msg.getBody(), msg.getOperation());
            msg.setBody(resp);
        } catch (ScriptException e) {
            msg.setFaultBody(e.getCause());
        }
        return msg;
    }

}
