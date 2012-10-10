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
package org.apache.tuscany.sca.implementation.spring.provider;

/**
 * @version $Rev: 987670 $ $Date: 2010-08-21 00:42:07 +0100 (Sat, 21 Aug 2010) $
 */
public class SpringInvocationException extends Exception {

    private static final long serialVersionUID = -1157790036638157513L;

    public SpringInvocationException(String msg) {
        super(msg);
    }

    public SpringInvocationException(Throwable e) {
        super(e);
    }

    public SpringInvocationException(String msg, Throwable e) {
        super(msg, e);
    }

}
