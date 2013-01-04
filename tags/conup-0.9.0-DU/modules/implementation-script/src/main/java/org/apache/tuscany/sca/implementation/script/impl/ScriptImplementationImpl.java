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
package org.apache.tuscany.sca.implementation.script.impl;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.assembly.impl.ImplementationImpl;
import org.apache.tuscany.sca.implementation.script.ScriptImplementation;

/**
 * Represents a Script implementation.
 *
 * @version $Rev: 924051 $ $Date: 2010-03-16 23:34:25 +0000 (Tue, 16 Mar 2010) $
 */
public class ScriptImplementationImpl extends ImplementationImpl implements ScriptImplementation {
    public static final QName TYPE = new QName(Base.SCA11_TUSCANY_NS, "implementation.script");

    private String script; // Relative URI to the script
    private String language; // Scripting lang
    private String location; // Resolved location of the script
    
    public ScriptImplementationImpl() {
        super(TYPE);
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return "Script : " + getScript(); 
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((language == null) ? 0 : language.hashCode());
        result = prime * result + ((script == null) ? 0 : script.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScriptImplementationImpl other = (ScriptImplementationImpl)obj;
        if (language == null) {
            if (other.language != null)
                return false;
        } else if (!language.equals(other.language))
            return false;
        if (script == null) {
            if (other.script != null)
                return false;
        } else if (!script.equals(other.script))
            return false;
        return true;
    }
}
