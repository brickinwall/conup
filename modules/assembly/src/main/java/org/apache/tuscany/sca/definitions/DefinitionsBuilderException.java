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
package org.apache.tuscany.sca.definitions;

/**
 * Builder Exception
 *
 * @version $Rev: 967109 $ $Date: 2010-07-23 15:30:46 +0100 (Fri, 23 Jul 2010) $
 */
public class DefinitionsBuilderException extends Exception {
    private static final long serialVersionUID = 2513219325230252783L;

    public DefinitionsBuilderException() {
    }

    public DefinitionsBuilderException(String message) {
        super(message);
    }

    public DefinitionsBuilderException(Throwable cause) {
        super(cause);
    }

    public DefinitionsBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
