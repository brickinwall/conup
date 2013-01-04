/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

             http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.     
 */
package org.apache.tuscany.sca.host.rmi;


/**
 * This exception relates to cases where there is a problem with the
 * Host runtime
 *
 * @version $Rev: 796166 $ $Date: 2009-07-21 08:03:47 +0100 (Tue, 21 Jul 2009) $
 */
public class RMIHostRuntimeException extends RuntimeException {
   
    private static final long serialVersionUID = -2639598547028423686L;

    public RMIHostRuntimeException() {
    }

    public RMIHostRuntimeException(String message) {
        super(message);
    }
    
    public RMIHostRuntimeException(Throwable e) {
        super(e);
    }

    public RMIHostRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
