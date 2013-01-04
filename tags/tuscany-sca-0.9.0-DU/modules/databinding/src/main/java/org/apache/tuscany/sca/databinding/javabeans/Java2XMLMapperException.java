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

package org.apache.tuscany.sca.databinding.javabeans;

import javax.xml.namespace.QName;

/**
 * This exception is used to encapsulate and rethrow exceptions that arise out
 * of converting JavaBean objects to XML
 *
 * @version $Rev: 796166 $ $Date: 2009-07-21 08:03:47 +0100 (Tue, 21 Jul 2009) $
 */
public class Java2XMLMapperException extends RuntimeException {
    private static final long serialVersionUID = 6811924384399578686L;

    private QName xmlElementName;
    private String javaFieldName;
    private Class javaType;
    
    public Java2XMLMapperException(String message) {
        super(message);
    }

    public Java2XMLMapperException(Throwable cause) {
        super(cause);
    }

    public String getJavaFieldName() {
        return javaFieldName;
    }

    public void setJavaFieldName(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    public Class getJavaType() {
        return javaType;
    }

    public void setJavaType(Class javaType) {
        this.javaType = javaType;
    }

    public QName getXmlElementName() {
        return xmlElementName;
    }

    public void setXmlElementName(QName xmlElementName) {
        this.xmlElementName = xmlElementName;
    }

}
