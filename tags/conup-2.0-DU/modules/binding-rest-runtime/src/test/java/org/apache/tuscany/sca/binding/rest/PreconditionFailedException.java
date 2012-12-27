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

package org.apache.tuscany.sca.binding.rest;

/**
 * Indicates that a resource was not modified.
 * 
 * @version $Rev: 937843 $ $Date: 2010-04-25 18:51:29 +0100 (Sun, 25 Apr 2010) $
 */
public class PreconditionFailedException extends Exception {
    private static final long serialVersionUID = -5046027674128627383L;

    public PreconditionFailedException() {
    }

    public PreconditionFailedException(String message) {
        super(message);
    }

    public PreconditionFailedException(Throwable cause) {
        super(cause);
    }

    public PreconditionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
