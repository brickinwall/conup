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
package org.apache.tuscany.sca.implementation.java.invocation;

import org.apache.tuscany.sca.implementation.java.injection.InjectionRuntimeException;

/**
 * Denotes an error when invoking an event on an object
 *
 * @version $Rev: 567313 $ $Date: 2007-08-18 19:38:44 +0100 (Sat, 18 Aug 2007) $
 */
public class EventInvocationException extends InjectionRuntimeException {
    private static final long serialVersionUID = 1480018831708211581L;

    public EventInvocationException() {
        super();
    }

    public EventInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventInvocationException(String message) {
        super(message);
    }

    public EventInvocationException(Throwable cause) {
        super(cause);
    }

}
