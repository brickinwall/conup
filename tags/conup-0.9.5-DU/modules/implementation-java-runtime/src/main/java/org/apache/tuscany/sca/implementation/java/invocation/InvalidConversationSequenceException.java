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


/**
 * Denotes an unknown operation sequence in a conversation
 *
 * @version $Rev: 723218 $ $Date: 2008-12-04 06:05:21 +0000 (Thu, 04 Dec 2008) $
 */
public class InvalidConversationSequenceException extends Exception {
    private static final long serialVersionUID = -5744028391493899147L;

    public InvalidConversationSequenceException() {
        super();
    }

    public InvalidConversationSequenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidConversationSequenceException(String message) {
        super(message);
    }

    public InvalidConversationSequenceException(Throwable cause) {
        super(cause);
    }
}
