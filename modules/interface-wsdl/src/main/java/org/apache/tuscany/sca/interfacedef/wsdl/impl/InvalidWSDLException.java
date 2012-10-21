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

package org.apache.tuscany.sca.interfacedef.wsdl.impl;

import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;

/**
 * An exception to indicate the WSDL definition is invalid
 *
 * @version $Rev: 563061 $ $Date: 2007-08-06 09:19:58 +0100 (Mon, 06 Aug 2007) $
 */
public class InvalidWSDLException extends InvalidInterfaceException {
    private static final long serialVersionUID = 3742887584293256519L;

    public InvalidWSDLException(String message) {
        super(message);
    }
}
