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

package org.apache.tuscany.sca.implementation.python.provider;

import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

/**
 * An invoker for Python components.
 * 
 * @version $Rev$ $Date$
 */
class PythonInvoker implements Invoker {
    final PythonInterpreter python;
    final PyObject callable;
    final Operation operation;

    PythonInvoker(final PythonInterpreter py, final PyObject c, final Operation op) {
        python = py;
        callable = c;
        operation = op;
    }

    String apply(final String req) {
        PyObject r = callable.__call__(new PyString(req));
        return r.toString();
    }

    public Message invoke(final Message msg) {
        try {
            msg.setBody(apply((String)((Object[])msg.getBody())[0]));
        } catch(Exception e) {
            e.printStackTrace();
            msg.setFaultBody(e.getCause());
        }
        return msg;
    }
}
