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

package helloworld;

/**
 * @version $Rev: 736166 $ $Date: 2009-01-20 23:19:46 +0000 (Tue, 20 Jan 2009) $
 */
public class HelloException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 2570611055132507470L;

    /**
     * 
     */
    public HelloException() {
    }

    /**
     * @param message
     */
    public HelloException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public HelloException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public HelloException(String message, Throwable cause) {
        super(message, cause);
    }

}
