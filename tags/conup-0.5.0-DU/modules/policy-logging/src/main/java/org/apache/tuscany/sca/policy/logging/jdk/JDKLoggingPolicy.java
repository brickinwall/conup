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
package org.apache.tuscany.sca.policy.logging.jdk;

import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Base;

/**
 * Implementation for policies that could be injected as parameter
 * into the axis2config.
 *
 * @version $Rev: 824551 $ $Date: 2009-10-13 01:21:22 +0100 (Tue, 13 Oct 2009) $
 */
public class JDKLoggingPolicy {
    static final String SCA11_NS = Base.SCA11_NS;
    static final String SCA11_TUSCANY_NS = Base.SCA11_TUSCANY_NS;
    static final QName JDK_LOGGING_POLICY_QNAME = new QName(SCA11_TUSCANY_NS, "jdkLogger");

    private String loggerName;
    private String resourceBundleName;
    private Level logLevel;
    private boolean useParentHandlers = false;

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    public String getResourceBundleName() {
        return resourceBundleName;
    }

    public void setResourceBundleName(String resourceBundleName) {
        this.resourceBundleName = resourceBundleName;
    }

    public boolean isUseParentHandlers() {
        return useParentHandlers;
    }

    public void setUseParentHandlers(boolean useParentHandlers) {
        this.useParentHandlers = useParentHandlers;
    }

    public QName getSchemaName() {
        return JDK_LOGGING_POLICY_QNAME;
    }

    public boolean isUnresolved() {
        return false;
    }

    public void setUnresolved(boolean unresolved) {
    }

    @Override
    public String toString() {
        return "JDKLoggingPolicy [logLevel=" + logLevel
            + ", loggerName="
            + loggerName
            + ", resourceBundleName="
            + resourceBundleName
            + ", useParentHandlers="
            + useParentHandlers
            + "]";
    }
}
