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

import org.apache.tuscany.sca.core.factory.ObjectCreationException;
import org.apache.tuscany.sca.core.factory.ObjectFactory;

/**
 * @version $Rev: 567619 $ $Date: 2007-08-20 10:29:57 +0100 (Mon, 20 Aug 2007) $
 */
public class JavaComponentNameFactory implements ObjectFactory<String> {
    private final JavaComponentContextProvider componentContextProvider;


    public JavaComponentNameFactory(JavaComponentContextProvider component) {
        this.componentContextProvider = component;
    }


    public String getInstance() throws ObjectCreationException {
        String uri = componentContextProvider.getComponent().getURI();
        return uri.substring(uri.lastIndexOf('/')+1);
    }
}
