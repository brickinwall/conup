/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.tuscany.sca.databinding.jaxb;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.oasisopen.sca.ServiceRuntimeException;

/**
 * A PropertyDescriptor provides access to a bean property.  Values can be queried/changed using the
 * read and writer methods of the PropertyDescriptor.
 * <p/>
 * A PropertyDescriptorPlus object wraps a PropertyDescriptor and supplies enhanced set/get methods
 * that match JAXB semantics.
 * <p/>
 * For example, the set(..) method is smart enough to add lists, arrays and atomic values on JAXB
 * beans.
 * <p/>
 * The PropertyDescriptorPlus object also stores the xmlName of the property.
 *
 * @See XMLRootElementUtil.createPropertyDescriptorMap , which creates the PropertyDescriptorPlus
 * objects
 */
public class JAXBPropertyDescriptor implements Comparable<JAXBPropertyDescriptor> {
    PropertyDescriptor descriptor;
    QName xmlName = null;
    int index;

    /**
     * Package protected constructor.  Only created by XMLRootElementUtil.createPropertyDescriptorMap
     * @param descriptor
     * @param index TODO
     * @param propertyName
     *
     * @see XMLRootElementUtil.createPropertyDescriptorMap
     */
    JAXBPropertyDescriptor(PropertyDescriptor descriptor, QName xmlName, int index) {
        super();
        this.descriptor = descriptor;
        this.xmlName = xmlName;
    }

    /**
     * Package protected constructor.  Only created by XMLRootElementUtil.createPropertyDescriptorMap
     * @param descriptor
     * @param index TODO
     * @param propertyName
     *
     * @see XMLRootElementUtil.createPropertyDescriptorMap
     */
    JAXBPropertyDescriptor(PropertyDescriptor descriptor, String xmlName, int index) {
        super();
        this.descriptor = descriptor;
        this.xmlName = new QName("", xmlName);
    }

    public int compareTo(JAXBPropertyDescriptor o) {
        return index - o.index;
    }

    /** @return xmlname */
    public String getXmlName() {
        return xmlName.getLocalPart();
    }

    public QName getXmlQName() {
        return xmlName;
    }

    /** @return property type */
    public Class<?> getPropertyType() {
        return descriptor.getPropertyType();
    }

    /** @return property name */
    public String getPropertyName() {
        return descriptor.getName();
    }

    /**
     * Get the object
     *
     * @param targetBean
     * @return Object for this property or null
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object get(Object targetBean) throws InvocationTargetException, IllegalAccessException {
        Method method = descriptor.getReadMethod();
        if (method == null && descriptor.getPropertyType() == Boolean.class) {
            String propertyName = descriptor.getName();
            if (propertyName != null) {
                String methodName = "is";
                methodName =
                    methodName + ((propertyName.length() > 0) ? propertyName.substring(0, 1).toUpperCase() : "");
                methodName = methodName + ((propertyName.length() > 1) ? propertyName.substring(1) : "");
                try {
                    method = targetBean.getClass().getMethod(methodName);
                } catch (NoSuchMethodException e) {
                }
            }
        }
        if (method == null) {
            throw new ServiceRuntimeException("No getter is found");
        }
        Object ret = method.invoke(targetBean);
        if (method.getReturnType() == JAXBElement.class) {
            ret = ((JAXBElement<?>)ret).getValue();
        }
        return ret;
    }

    /**
     * Set the object
     *
     * @param targetBean
     * @param propValue
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws JAXBWrapperException
     */
    public void set(Object targetBean, Object propValue) throws InvocationTargetException, IllegalAccessException,
        JAXBWrapperException {

        Method writeMethod = null;
        try {
            // No set occurs if the value is null
            if (propValue == null) {
                return;
            }

            // There are 3 different types of setters that can occur.
            // 1) Normal Atomic Setter : setFoo(type)
            // 2) Indexed Array Setter : setFoo(type[])
            // 3) No Setter case if the property is a List<T>.

            writeMethod = descriptor.getWriteMethod();
            if (descriptor instanceof IndexedPropertyDescriptor) {
                // Set for indexed  T[]
                setIndexedArray(targetBean, propValue, writeMethod);
            } else if (writeMethod == null) {
                // Set for List<T>
                setList(targetBean, propValue);
            } else if (descriptor.getPropertyType() == JAXBElement.class) {
                if (propValue != null) {
                    Class<?> clazz = propValue.getClass();
                    JAXBElement<?> element = new JAXBElement(xmlName, clazz, propValue);
                    setAtomic(targetBean, element, writeMethod);
                }
            } else {
                // Normal case
                setAtomic(targetBean, propValue, writeMethod);
            }
        } catch (RuntimeException e) {
            throw e;
        }
    }

    /**
     * Set the property value onto targetBean using the writeMethod
     *
     * @param targetBean
     * @param propValue
     * @param writeMethod (set(T))
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws JAXBWrapperException
     */
    private void setAtomic(Object targetBean, Object propValue, Method writeMethod) throws InvocationTargetException,
        IllegalAccessException, JAXBWrapperException {
        // JAXB provides setters for atomic value.

        if (propValue != null) {
            // Normal case
            Object[] SINGLE_PARAM = new Object[1];
            SINGLE_PARAM[0] = propValue;
            writeMethod.invoke(targetBean, SINGLE_PARAM);
        } else {
            Class<?>[] paramTypes = writeMethod.getParameterTypes();

            if (paramTypes != null && paramTypes.length == 1) {
                Class<?> paramType = paramTypes[0];
                if (paramType.isPrimitive() && propValue == null) {
                    //Ignoring null value for primitive type, this could potentially be the way of a customer indicating to set
                    //default values defined in JAXBObject/xmlSchema.
                    return;
                }
            }
        }

    }

    /**
     * Set the property value using the indexed array setter
     *
     * @param targetBean
     * @param propValue
     * @param writeMethod set(T[])
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws JAXBWrapperException
     */
    private void setIndexedArray(Object targetBean, Object propValue, Method writeMethod)
        throws InvocationTargetException, IllegalAccessException, JAXBWrapperException {

        Class<?> paramType = writeMethod.getParameterTypes()[0];
        Object value = asArray(propValue, paramType);
        // JAXB provides setters for atomic value.
        Object[] SINGLE_PARAM = new Object[1];
        SINGLE_PARAM[0] = value;

        writeMethod.invoke(targetBean, SINGLE_PARAM);
    }

    /**
     * Set the property value for the collection case.
     *
     * @param targetBean
     * @param propValue
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws JAXBWrapperException
     */
    private void setList(Object targetBean, Object propValue) throws InvocationTargetException, IllegalAccessException,
        JAXBWrapperException {
        // For the List<T> case, there is no setter. 
        // You are supposed to use the getter to obtain access to the collection and then add the collection

        Collection value = asCollection(propValue, descriptor.getPropertyType());
        Collection collection = (Collection)get(targetBean);

        // Now add our our object to the collection
        collection.clear();
        if (propValue != null) {
            collection.addAll(value);
        }
    }

    /**
     * @param propValue
     * @param destType
     * @return propValue as a Collection
     */
    private static Collection asCollection(Object propValue, Class<?> destType) {
        // TODO Missing function
        // Convert the object into an equivalent object that is a collection
        if (DataConverter.isConvertable(propValue, destType)) {
            return (Collection)DataConverter.convert(propValue, destType);
        } else {
            String objectClass = (propValue == null) ? "null" : propValue.getClass().getName();
            throw new ServiceRuntimeException("Cannot convert " + objectClass);
        }
    }

    /**
     * @param propValue
     * @param destType  T[]
     * @return array of component type
     */
    private static Object asArray(Object propValue, Class<?> destType) {
        if (DataConverter.isConvertable(propValue, destType)) {
            return DataConverter.convert(propValue, destType);
        } else {
            String objectClass = (propValue == null) ? "null" : propValue.getClass().getName();
            throw new ServiceRuntimeException("Cannot convert " + objectClass);

        }
    }

    @Override
    public String toString() {
        String value = "PropertyDescriptorPlus[";
        value += " name=" + this.getPropertyName();
        value += " type=" + this.getPropertyType().getName();
        value += " propertyDecriptor=" + this.descriptor;
        return value + "]";
    }
}
