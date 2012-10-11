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
package org.apache.tuscany.sca.policy.security;

import java.security.Principal;

import javax.security.auth.Subject;

import org.apache.tuscany.sca.invocation.Message;


/**
 * Some utilities for dealing with security information in a Tuscany message
 * 
 * @version $Rev: 954414 $ $Date: 2010-06-14 12:35:36 +0100 (Mon, 14 Jun 2010) $
 * @tuscany.spi.extension.asclient
 */
public class SecurityUtil {
    
    public static String SubjectString = "Subject";
    public static String PrincipalString = "Principal";
    
    public static Subject getSubject(Message msg){
        
        Subject subject = (Subject)msg.getHeaders().get(SubjectString);
        
        if (subject == null){
            subject = new Subject(); 
            msg.getHeaders().put(SubjectString, subject); 
        }
        
        return subject;
    }
    
    public static <T> T getPrincipal(Subject subject, Class<T> clazz){
        for (Principal msgPrincipal : subject.getPrincipals() ){
            if (clazz.isInstance(msgPrincipal)){
                return clazz.cast(msgPrincipal);
            }
        }
        
        return null;
    }
    
    public static Principal getPrincipal(Message msg){
        
        return (Principal)msg.getHeaders().get(PrincipalString);
    }    
}
