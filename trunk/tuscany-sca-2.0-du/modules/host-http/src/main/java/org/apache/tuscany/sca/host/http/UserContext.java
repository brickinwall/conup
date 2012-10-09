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

package org.apache.tuscany.sca.host.http;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class used to define list of users and it's roles
 * 
 * These info is used to configure authentication/authorization 
 * in embedded http servers 
 *
 * @version $Rev: 916308 $ $Date: 2010-02-25 15:02:17 +0000 (Thu, 25 Feb 2010) $
 */
public class UserContext {
    private String username;
    private String password;
    private List<String> roles = new ArrayList<String>();
    
    
    public UserContext() {
        
    }
    
    public UserContext(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public List<String> getRoles() {
        return this.roles;
    }
    
}
