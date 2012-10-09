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

package org.apache.tuscany.sca.implementation.script.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.core.factory.ObjectCreationException;
import org.apache.tuscany.sca.core.factory.ObjectFactory;
import org.apache.tuscany.sca.databinding.Mediator;
import org.apache.tuscany.sca.databinding.SimpleTypeMapper;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A property factory for script properties.
 *
 * @version $Rev: 951651 $ $Date: 2010-06-05 06:17:47 +0100 (Sat, 05 Jun 2010) $
 */
public class ScriptPropertyFactory {
    private Mediator mediator = null;
    private SimpleTypeMapper simpleTypeMapper;
    boolean isSimpleType;
    
    public ScriptPropertyFactory(Mediator mediator, SimpleTypeMapper simpleTypeMapper) {
        this.mediator = mediator;
        this.simpleTypeMapper = simpleTypeMapper;
    }
    
    public ObjectFactory createValueFactory(Property property) {
        isSimpleType = isSimpleType(property);
        Document doc = (Document)property.getValue();
        Element rootElement = doc.getDocumentElement();
        
        //FIXME : since scripts use dynamic types we need to generate a dynamic java type using the 
        //XML structure of the property value.  Should this be done in the JavaBeansDataBinding... 
        Class javaType = null;
        
        if (property.isMany()) {
            if (isSimpleType) {
                String value = "";
                if (rootElement.getChildNodes().getLength() > 0) {
                    value = rootElement.getChildNodes().item(0).getTextContent();
                }
                List<String> values = 
                    getSimplePropertyValues(value, javaType);
                return new ListObjectFactoryImpl(property, 
                                                 values,
                                                 isSimpleType,
                                                 javaType);
            } else {
                return new ListObjectFactoryImpl(property,
                                                 getComplexPropertyValues(doc),
                                                 isSimpleType,
                                                 javaType);
            }
        } else {
            if (isSimpleType) {
                String value = "";
                if (rootElement.getChildNodes().getLength() > 0) {
                    value = rootElement.getChildNodes().item(0).getTextContent();
                }
                return new ObjectFactoryImpl(property,
                                             value,
                                             isSimpleType,
                                             javaType);
            } else {
                Object value = getComplexPropertyValues(doc).get(0);
                return new ObjectFactoryImpl(property,
                                             value,
                                             isSimpleType,
                                             javaType);
            }
            
        }
    }
    
    private boolean isSimpleType(Property property) {
        if (property.getXSDType() != null) {
            return simpleTypeMapper.isSimpleXSDType(property.getXSDType());
        } else {
            if (property instanceof Document) {
                Document doc = (Document)property;
                Element element = doc.getDocumentElement(); 
                if (element.getChildNodes().getLength() == 1 && 
                    element.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private List<String> getSimplePropertyValues(String concatenatedValue, Class<?> javaType) {
        List<String> propValues = new ArrayList<String>();
        StringTokenizer st = null;
        if ( javaType.getName().equals("java.lang.String")) {
            st = new StringTokenizer(concatenatedValue, "\"");
        } else {
            st = new StringTokenizer(concatenatedValue);
        }
        String aToken = null;
        while (st.hasMoreTokens()) {
            aToken = st.nextToken();
            if (aToken.trim().length() > 0) {
                propValues.add(aToken);
            }
        }
        return propValues;
    }
    
    private List<Node> getComplexPropertyValues(Document document) {
        Element rootElement = document.getDocumentElement();
        List<Node> propValues = new ArrayList<Node>();
        for (int count = 0 ; count < rootElement.getChildNodes().getLength() ; ++count) {
            if (rootElement.getChildNodes().item(count).getNodeType() == Node.ELEMENT_NODE) {
                propValues.add(rootElement.getChildNodes().item(count));
            }
        }
        return propValues;
    }
    
    public abstract class ObjectFactoryImplBase  implements ObjectFactory {
        protected Property property;
        protected Object propertyValue;
        protected Class<?> javaType;
        protected DataType<XMLType> sourceDataType;
        protected DataType<?> targetDataType;
        boolean isSimpleType;

        public ObjectFactoryImplBase(Property property, Object propertyValue, boolean isSimpleType, Class<?> javaType)  {
            
            this.isSimpleType = isSimpleType;
            this.property = property;
            this.propertyValue = propertyValue;
            this.javaType = javaType;

            //FIXME : fix this when we have managed to generate dynamic java types
            
            /*sourceDataType =
                new DataTypeImpl<XMLType>(DOMDataBinding.NAME, Node.class, 
                    new XMLType(null, this.property.getXSDType()));
            TypeInfo typeInfo = null;
            if (this.property.getXSDType() != null) {
                if (SimpleTypeMapperExtension.isSimpleXSDType(this.property.getXSDType())) {
                    typeInfo = new TypeInfo(property.getXSDType(), true, null);
                } else {
                    typeInfo = new TypeInfo(property.getXSDType(), false, null);
                }
            } else {
                typeInfo = new TypeInfo(property.getXSDType(), false, null);
            }

            XMLType xmlType = new XMLType(typeInfo);
            String dataBinding = null; //(String)property.getExtensions().get(DataBinding.class.getName());
            if (dataBinding != null) {
                targetDataType = new DataTypeImpl<XMLType>(dataBinding, javaType, xmlType);
            } else {
                targetDataType = new DataTypeImpl<XMLType>(dataBinding, javaType, xmlType);
                mediator.getDataBindingRegistry().introspectType(targetDataType, null);  
            }*/
        }
    }
    
    public class ObjectFactoryImpl extends ObjectFactoryImplBase {
        public ObjectFactoryImpl(Property property, Object propertyValue, boolean isSimpleType, Class<?> javaType) {
            super(property, propertyValue, isSimpleType, javaType);
        }

        public Object getInstance() throws ObjectCreationException {
            if (isSimpleType) {
                return simpleTypeMapper.toJavaObject(property.getXSDType(), (String)propertyValue, null);
            } else {
                return mediator.mediate(propertyValue, sourceDataType, targetDataType, null);
            }
        }
    }

    public class ListObjectFactoryImpl extends ObjectFactoryImplBase  {
        public ListObjectFactoryImpl(Property property, List<?>propertyValues, boolean isSimpleType, Class<?> javaType) {
            super(property, propertyValues, isSimpleType, javaType);
        }

        public List<?> getInstance() throws ObjectCreationException {
            if (isSimpleType) {
                List<Object> values = new ArrayList<Object>();
                for (String aValue : (List<String>)propertyValue) {
                    values.add(simpleTypeMapper.toJavaObject(property.getXSDType(), aValue, null));
                }
                return values;
            } else {
                List<Object> instances = new ArrayList<Object>();
                for (Node aValue : (List<Node>)propertyValue) {
                    instances.add(mediator.mediate(aValue,
                                                      sourceDataType,
                                                      targetDataType,
                                                      null));
                }
                return instances;
            }
        }
    }
}
