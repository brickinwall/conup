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

package org.apache.tuscany.sca.databinding.sdo;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.databinding.SimpleTypeMapper;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sdo.api.SDOUtil;
import org.apache.tuscany.sca.interfacedef.util.TypeInfo;

import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.TypeHelper;

/**
 * SDO Java/XML mapping for simple XSD types
 *
 * @version $Rev: 909092 $ $Date: 2010-02-11 17:44:00 +0000 (Thu, 11 Feb 2010) $
 */
public class SDOSimpleTypeMapper implements SimpleTypeMapper {
    public static final String URI_2001_SCHEMA_XSD = "http://www.w3.org/2001/XMLSchema";

    public SDOSimpleTypeMapper() {
        super();
    }

    public Object toJavaObject(QName typeName, String value, TransformationContext context) {
        Type type = null;
        if (URI_2001_SCHEMA_XSD.equals(typeName.getNamespaceURI())) {
            type = SDOUtil.getXSDSDOType(typeName.getLocalPart());
        } else {
            HelperContext helperContext = SDOContextHelper.getHelperContext(context, false);
            TypeHelper typeHelper = helperContext.getTypeHelper();
            type = typeHelper.getType(typeName.getNamespaceURI(), typeName.getLocalPart());
        }
        return SDOUtil.createFromString(type, value);
    }

    public String toXMLLiteral(QName typeName, Object obj, TransformationContext context) {
        Type type = null;
        if (URI_2001_SCHEMA_XSD.equals(typeName.getNamespaceURI())) {
            type = SDOUtil.getXSDSDOType(typeName.getLocalPart());
        } else {
            HelperContext helperContext = SDOContextHelper.getHelperContext(context, true);
            TypeHelper typeHelper = helperContext.getTypeHelper();
            type = typeHelper.getType(typeName.getNamespaceURI(), typeName.getLocalPart());
        }
        return SDOUtil.convertToString(type, obj);
    }
	
	public Class<?> getJavaType(QName xmlType) {
	   
	   return null;
	}
	   
	public TypeInfo getXMLType(Class<?> javaType) {
	    return null;
	  }
	  
	public boolean isSimpleXSDType(QName xmlType) {
	  return false;
	  }

}
