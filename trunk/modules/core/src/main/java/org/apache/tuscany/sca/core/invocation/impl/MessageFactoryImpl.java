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

package org.apache.tuscany.sca.core.invocation.impl;

import java.io.Serializable;

import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.MessageFactory;

/**
 * Implementation of MessageFactory.
 *
 * @version $Rev: 1059919 $ $Date: 2011-01-17 13:33:14 +0000 (Mon, 17 Jan 2011) $
 */
public class MessageFactoryImpl implements MessageFactory, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2112289169275106977L;

	public Message createMessage() {
        return new MessageImpl();
    }

}
