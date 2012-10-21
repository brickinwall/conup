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
package org.apache.tuscany.sca.implementation.script;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.assembly.Implementation;

/**
 * Represents a Script implementation.
 *
 * @version $Rev: 924051 $ $Date: 2010-03-16 23:34:25 +0000 (Tue, 16 Mar 2010) $
 */
public interface ScriptImplementation extends Implementation {
    QName TYPE = new QName(Base.SCA11_TUSCANY_NS, "implementation.script");

    String getScript();

    void setScript(String script);

    String getLocation();

    void setLocation(String location);

    void setLanguage(String language);

    String getLanguage();

}
