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

/**
 * TUSCANY-3786
 * 
 * The request side of an Interface to describe an invocation where 
 * the request processing can be performed independently of the 
 * response processing. 
 */
public interface InvokerAsyncRequest {
    
    /**
     * Process the request message and pass it down the chain
     *
     * @param msg The request Message
     * @return the processed message
     * @throws Throwable 
     * 
     */
    void invokeAsyncRequest(Message msg) throws Throwable;   

}
