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
package org.apache.tuscany.sca.implementation.java.introspect.impl;

import java.lang.reflect.Member;

import org.apache.tuscany.sca.implementation.java.IntrospectionException;

/**
 * Denotes an invalid usage of {@link @org.apache.tuscany.api.annotation.Resource}
 *
 * @version $Rev: 563061 $ $Date: 2007-08-06 09:19:58 +0100 (Mon, 06 Aug 2007) $
 */
public class InvalidResourceException extends IntrospectionException {
    private static final long serialVersionUID = 511728001735534934L;

    public InvalidResourceException(String message) {
        super(message);
    }
    
    public InvalidResourceException(String message, Member member) {
        super(message, member);
    }    
}
