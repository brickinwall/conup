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

package org.apache.tuscany.sca.host.http.extensibility;

import org.apache.tuscany.sca.host.http.HttpScheme;

/**
 * Allows runtime to query the Http Port to use for a particular Http Scheme (http, https)
 *
 * @version $Rev: 1095775 $ $Date: 2011-04-21 17:56:48 +0100 (Thu, 21 Apr 2011) $
 */
public interface HttpPortAllocator {
    int DEFAULT_HTTP_PORT = 8080;
    int DEFAULT_HTTPS_PORT = 8443;
    /**
     * Get default port for a given http scheme
     * @param scheme the http scheme in use (http/https)
     * @return the default port to use
     */
    int getDefaultPort(HttpScheme scheme);
}
