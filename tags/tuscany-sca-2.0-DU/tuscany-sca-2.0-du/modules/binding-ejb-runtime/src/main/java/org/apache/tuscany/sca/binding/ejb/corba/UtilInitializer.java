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
package org.apache.tuscany.sca.binding.ejb.corba;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * The sole purpose of this initializer is to register a non-singleton ORB
 * with the class <code>Util</code>.
 *
 * @version $Revision: 665774 $ $Date: 2008-06-09 18:05:18 +0100 (Mon, 09 Jun 2008) $
 * @see Java2IDLUtil
 */
public class UtilInitializer extends LocalObject implements ORBInitializer {

    private static final long serialVersionUID = 4901857563505370955L;

    /**
     * Called during ORB initialization.  If it is expected that initial
     * services registered by an interceptor will be used by other
     * interceptors, then those initial services shall be registered at
     * this point via calls to
     * <code>ORBInitInfo.register_initial_reference</code>.
     *
     * @param info provides initialization attributes and operations by
     *             which Interceptors can be registered.
     */
    public void pre_init(ORBInitInfo info) {
    }

    /**
     * Called during ORB initialization. If a service must resolve initial
     * references as part of its initialization, it can assume that all
     * initial references will be available at this point.
     * <p/>
     * Calling the <code>post_init</code> operations is not the final
     * task of ORB initialization. The final task, following the
     * <code>post_init</code> calls, is attaching the lists of registered
     * interceptors to the ORB. Therefore, the ORB does not contain the
     * interceptors during calls to <code>post_init</code>. If an
     * ORB-mediated call is made from within <code>post_init</code>, no
     * request interceptors will be invoked on that call.
     * Likewise, if an operation is performed which causes an IOR to be
     * created, no IOR interceptors will be invoked.
     *
     * @param info provides initialization attributes and
     *             operations by which Interceptors can be registered.
     */
    public void post_init(ORBInitInfo info) {
    }
}
