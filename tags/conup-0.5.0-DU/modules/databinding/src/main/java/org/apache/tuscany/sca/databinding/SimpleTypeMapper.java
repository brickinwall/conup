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

package org.apache.tuscany.sca.databinding;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.interfacedef.util.TypeInfo;

/**
 * Type Mapper between XML schema simple data types and java objects
 *
 * @version $Rev: 937310 $ $Date: 2010-04-23 15:27:50 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface SimpleTypeMapper {
    /**
     * Parse the XML lexical representation into a java object 
     * @param simpleType The XSD simple type
     * @param value the XML lexical representation
     * @param context The context of the transformation
     * @return A java object for the XML value
     */
    Object toJavaObject(QName simpleType, String value, TransformationContext context);
    /**
     * Create the XML lexical representation for a java object
     * @param simpleType The XSD simple type
     * @param obj The java object
     * @param context The context of the transformation
     * @return The XML lexical representation
     */
    String toXMLLiteral(QName simpleType, Object obj, TransformationContext context);

    /**
     * Get the java type for a simple xml type
     * @param xmlType
     * @return
     */
    Class<?> getJavaType(QName xmlType);

    /**
     * Get the xml type for a java type
     * @param javaType
     * @return
     */
    TypeInfo getXMLType(Class<?> javaType);

    /**
     * Check if an xml type is a simple type
     * @param xmlType
     * @return
     */
    boolean isSimpleXSDType(QName xmlType);
}
