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
package org.apache.tuscany.sca.implementation.java.introspect;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.oasisopen.sca.ServiceReference;

/**
 * Implements various reflection-related operations
 *
 * @version $Rev: 1242718 $ $Date: 2012-02-10 09:43:24 +0000 (Fri, 10 Feb 2012) $
 */
public final class JavaIntrospectionHelper {
    private static final Logger logger = Logger.getLogger(JavaIntrospectionHelper.class.getName());
    private static final Class<?>[] EMPTY_CLASS_ARRY = new Class[0];

    /**
     * Hide the constructor
     */
    private JavaIntrospectionHelper() {
    }

    /**
     * Returns a collection of public, and protected fields declared by a class
     * or one of its supertypes
     */
    public static Set<Field> getAllPublicAndProtectedFields(Class<?> clazz, boolean validating) {
        return getAllPublicAndProtectedFields(clazz, new HashSet<Field>(), validating);
    }

    private static void checkInvalidAnnotations(AnnotatedElement element) {
        for (Annotation a : element.getAnnotations()) {
            if (a.annotationType().getName().startsWith("org.oasisopen.sca.annotation.")) {
                logger.warning("Invalid annotation " + a + " is found on " + element);
            }
        }
    }

    /**
     * Recursively evaluates the type hierarchy to return all fields that are
     * public or protected
     */
    private static Set<Field> getAllPublicAndProtectedFields(Class<?> clazz, Set<Field> fields, boolean validating) {
        if (clazz == null || clazz.isArray() || Object.class.equals(clazz)) {
            return fields;
        }
        fields = getAllPublicAndProtectedFields(clazz.getSuperclass(), fields, validating);

        Field[] declaredFields = null;

        try {
        	declaredFields = clazz.getDeclaredFields();
        } catch(Throwable t) {
        	//TUSCANY-3667 - clazz.getDeclaredFields might fail in GAE environment (log and ignore)
        	logger.log(Level.WARNING, "Error retrieving declared fields from class : " + t.getMessage());
        }

        if( declaredFields != null ) {
            for (final Field field : declaredFields) {
                int modifiers = field.getModifiers();
                // The field should be non-final and non-static
                if ((Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) && !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
                    // Allow privileged access to set accessibility. Requires ReflectPermission
                    // in security policy.
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            field.setAccessible(true); // ignore Java accessibility
                            return null;
                        }
                    });
                    fields.add(field);
                } /*else {
                    if (validating) {
                        checkInvalidAnnotations(field);
                    }
                }*/
            }
        }
        return fields;
    }

    /**
     * Returns a collection of injectable fields declared by a class
     * or one of its supertypes
     *
     * For now we will include final or static fields so that validation problems can be reported
     */
    public static Set<Field> getInjectableFields(Class<?> clazz, boolean validating) {
        return getInjectableFields(clazz, new HashSet<Field>(), validating);
    }

    /**
     * Recursively evaluates the type hierarchy to return all fields
     */
    private static Set<Field> getInjectableFields(Class<?> clazz, Set<Field> fields, boolean validating) {
        if (clazz == null || clazz.isArray() || Object.class.equals(clazz)) {
            return fields;
        }

        fields = getInjectableFields(clazz.getSuperclass(), fields, validating);

        Field[] declaredFields = null;

        try {
        	declaredFields = clazz.getDeclaredFields();
        } catch(Throwable t) {
        	//TUSCANY-3667 - clazz.getDeclaredFields might fail in GAE environment (log and ignore)
        	logger.log(Level.WARNING, "Error retrieving declared fields from class : " + t.getMessage());
        }

        if( declaredFields != null ) {
            for (final Field field : declaredFields) {
                int modifiers = field.getModifiers();
                // The field should be non-final and non-static
                if (!Modifier.isStatic(modifiers)
                    // && !Modifier.isFinal(modifiers)
                    ) {

                    // Allow privileged access to set accessibility. Requires ReflectPermission
                    // in security policy.
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            field.setAccessible(true); // ignore Java accessibility
                            return null;
                        }
                    });
                    fields.add(field);
                } else {
                    if (validating) {
                        checkInvalidAnnotations(field);
                    }
                }
            }
        }


        return fields;
    }

    /**
     * Returns a collection of public and protected methods declared by a class
     * or one of its supertypes. Note that overridden methods will not be
     * returned in the collection (i.e. only the method override will be). <p/>
     * This method can potentially be expensive as reflection information is not
     * cached. It is assumed that this method will be used during a
     * configuration phase.
     */
    public static Set<Method> getAllUniquePublicProtectedMethods(Class<?> clazz, boolean validating) {
        return getAllUniqueMethods(clazz, new HashSet<Method>(), validating);
    }

    /**
     * Recursively evaluates the type hierarchy to return all unique methods
     */
    private static Set<Method> getAllUniqueMethods(Class<?> pClass, Set<Method> methods, boolean validating) {
        if (pClass == null || pClass.isArray() || Object.class.equals(pClass)) {
            return methods;
        }
        // we first evaluate methods of the subclass and then move to the parent
        Method[] declaredMethods = pClass.getDeclaredMethods();
        for (final Method declaredMethod : declaredMethods) {
            int modifiers = declaredMethod.getModifiers();
            if ((!Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers)) || Modifier.isStatic(modifiers)) {
                if (validating) {
                    checkInvalidAnnotations(declaredMethod);
                }
                continue;
            }
            if (methods.size() == 0) {
                methods.add(declaredMethod);
            } else {
                List<Method> temp = new ArrayList<Method>();
                boolean matched = false;
                for (Method method : methods) {
                    // only add if not already in the set from a superclass (i.e.
                    // the method is not overridden)
                    if (exactMethodMatch(declaredMethod, method)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    // Allow privileged access to set accessibility. Requires ReflectPermission
                    // in security policy.
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            declaredMethod.setAccessible(true);
                            return null;
                        }
                    });
                    temp.add(declaredMethod);
                }
                methods.addAll(temp);
                temp.clear();
            }
        }
        // evaluate class hierarchy - this is done last to track inherited
        // methods
        methods = getAllUniqueMethods(pClass.getSuperclass(), methods, validating);
        return methods;
    }

    /**
     * Finds the closest matching field with the given name, that is, a field of
     * the exact specified type or, alternately, of a supertype.
     *
     * @param name the name of the field
     * @param type the field type
     * @param fields the collection of fields to search
     * @return the matching field or null if not found
     */
    public static Field findClosestMatchingField(String name, Class type, Set<Field> fields) {
        Field candidate = null;
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                if (field.getType().equals(type)) {
                    return field; // exact match
                } else if (field.getType().isAssignableFrom(type) || (field.getType().isPrimitive() && primitiveAssignable(field
                                                                                                                               .getType(),
                                                                                                                           type))) {
                    // We could have the situation where a field parameter is a
                    // primitive and the demarshalled value is
                    // an object counterpart (e.g. Integer and int)
                    // @spec issue
                    // either an interface or super class, so keep a reference
                    // until
                    // we know there are no closer types
                    candidate = field;
                }
            }
        }
        if (candidate != null) {
            return candidate;
        } else {
            return null;
        }
    }

    /**
     * Finds the closest matching method with the given name, that is, a method
     * taking the exact parameter types or, alternately, parameter supertypes.
     *
     * @param name the name of the method
     * @param types the method parameter types
     * @param methods the collection of methods to search
     * @return the matching method or null if not found
     */
    public static Method findClosestMatchingMethod(String name, Class[] types, Set<Method> methods) {
        if (types == null) {
            types = EMPTY_CLASS_ARRY;
        }
        Method candidate = null;
        for (Method method : methods) {
            if (method.getName().equals(name) && method.getParameterTypes().length == types.length) {
                Class<?>[] params = method.getParameterTypes();
                boolean disqualify = false;
                boolean exactMatch = true;
                for (int i = 0; i < params.length; i++) {
                    if (!params[i].equals(types[i]) && !params[i].isAssignableFrom(types[i])) {
                        // no match
                        disqualify = true;
                        exactMatch = false;
                        break;
                    } else if (!params[i].equals(types[i]) && params[i].isAssignableFrom(types[i])) {
                        // not exact match
                        exactMatch = false;
                    }
                }
                if (disqualify) {
                    continue;
                } else if (exactMatch) {
                    return method;
                } else {
                    candidate = method;
                }
            }
        }
        if (candidate != null) {
            return candidate;
        } else {
            return null;
        }
    }

    /**
     * Determines if two methods "match" - that is, they have the same method
     * names and exact parameter types (one is not a supertype of the other)
     */
    public static boolean exactMethodMatch(Method method1, Method method2) {
        if (!method1.getName().equals(method2.getName())) {
            return false;
        }
        Class<?>[] types1 = method1.getParameterTypes();
        Class<?>[] types2 = method2.getParameterTypes();
        if (types1.length != types2.length) {
            return false;
        }
        boolean matched = true;
        for (int i = 0; i < types1.length; i++) {
            if (types1[i] != types2[i]) {
                matched = false;
                break;
            }
        }
        return matched;
    }

    public static <T> Constructor<T> getDefaultConstructor(Class<T> clazz) throws NoSuchMethodException {
        return clazz.getConstructor((Class[])null);
    }

    /**
     * Returns the simple name of a class - i.e. the class name devoid of its
     * package qualifier
     *
     * @param implClass the implementation class
     */
    public static String getBaseName(Class<?> implClass) {
        return implClass.getSimpleName();
    }

    public static boolean isImmutable(Class<?> clazz) {
        return String.class == clazz || clazz.isPrimitive()
            || Number.class.isAssignableFrom(clazz)
            || Boolean.class.isAssignableFrom(clazz)
            || Character.class.isAssignableFrom(clazz)
            || Byte.class.isAssignableFrom(clazz);
    }

    /**
     * Takes a property name and converts it to a getter method name according
     * to JavaBean conventions. For example, property
     * <code>foo<code> is returned as <code>getFoo</code>
     */
    public static String toGetter(String name) {
        return "get" + name.toUpperCase().substring(0, 1) + name.substring(1);
    }

    /**
     * Takes a setter or getter method name and converts it to a property name
     * according to JavaBean conventions. For example, <code>setFoo(var)</code>
     * is returned as property <code>foo<code>
     */
    public static String toPropertyName(String name) {
        if (!name.startsWith("set")) {
            return name;
        }
        return Introspector.decapitalize(name.substring(3));
    }

    public static Class<?> getErasure(Type type) {
        if (type instanceof Class) {
            return (Class<?>)type;
        } else if (type instanceof GenericArrayType) {
            // FIXME: How to deal with the []?
            GenericArrayType arrayType = (GenericArrayType)type;
            return getErasure(arrayType.getGenericComponentType());
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType)type;
            return getErasure(pType.getRawType());
        } else if (type instanceof WildcardType) {
            WildcardType wType = (WildcardType)type;
            Type[] types = wType.getUpperBounds();
            return getErasure(types[0]);
        } else if (type instanceof TypeVariable) {
            TypeVariable var = (TypeVariable)type;
            Type[] types = var.getBounds();
            return getErasure(types[0]);
        }
        return null;
    }

    public static Class<?> getBaseType(Class<?> cls, Type genericType) {
        if (cls.isArray()) {
            return cls.getComponentType();
        } else if (Collection.class.isAssignableFrom(cls)) {
            if (genericType instanceof ParameterizedType) {
                // Collection<BaseType>
                ParameterizedType parameterizedType = (ParameterizedType)genericType;
                Type baseType = parameterizedType.getActualTypeArguments()[0];
                if (baseType instanceof GenericArrayType) {
                    // Base is array
                    return cls;
                } else {
                    return getErasure(baseType);
                }
            } else {
                return cls;
            }
        } else {
            return cls;
        }
    }

    public static Type getParameterType(Type type) {
        if (type instanceof ParameterizedType) {
            // Collection<BaseType>
            ParameterizedType parameterizedType = (ParameterizedType)type;
            Type baseType = parameterizedType.getActualTypeArguments()[0];
            return baseType;
        } else {
            return Object.class;
        }
    }

    public static Class<?> getBusinessInterface(Class<?> cls, Type callableReferenceType) {
        if (ServiceReference.class.isAssignableFrom(cls) && callableReferenceType instanceof ParameterizedType) {
            // Collection<BaseType>
            ParameterizedType parameterizedType = (ParameterizedType)callableReferenceType;
            Type baseType = parameterizedType.getActualTypeArguments()[0];
            if (baseType instanceof GenericArrayType) {
                // Base is array
                return cls;
            } else {
                return getErasure(baseType);
            }
        }
        return Object.class;
    }

    /**
     * Takes a property name and converts it to a setter method name according
     * to JavaBean conventions. For example, the property
     * <code>foo<code> is returned as <code>setFoo(var)</code>
     */
    public static String toSetter(String name) {
        return "set" + name.toUpperCase().substring(0, 1) + name.substring(1);
    }

    /**
     * Compares a two types, assuming one is a primitive, to determine if the
     * other is its object counterpart
     */
    private static boolean primitiveAssignable(Class<?> memberType, Class<?> param) {
        if (memberType == Integer.class) {
            return param == Integer.TYPE;
        } else if (memberType == Double.class) {
            return param == Double.TYPE;
        } else if (memberType == Float.class) {
            return param == Float.TYPE;
        } else if (memberType == Short.class) {
            return param == Short.TYPE;
        } else if (memberType == Character.class) {
            return param == Character.TYPE;
        } else if (memberType == Boolean.class) {
            return param == Boolean.TYPE;
        } else if (memberType == Byte.class) {
            return param == Byte.TYPE;
        } else if (param == Integer.class) {
            return memberType == Integer.TYPE;
        } else if (param == Double.class) {
            return memberType == Double.TYPE;
        } else if (param == Float.class) {
            return memberType == Float.TYPE;
        } else if (param == Short.class) {
            return memberType == Short.TYPE;
        } else if (param == Character.class) {
            return memberType == Character.TYPE;
        } else if (param == Boolean.class) {
            return memberType == Boolean.TYPE;
        } else if (param == Byte.class) {
            return memberType == Byte.TYPE;
        } else {
            return false;
        }
    }

    /**
     * Returns the generic types represented in the given type. Usage as
     * follows: <code>
     * JavaIntrospectionHelper.getGenerics(field.getGenericType());
     * <p/>
     * JavaIntrospectionHelper.getGenerics(m.getGenericParameterTypes()[0];); </code>
     *
     * @return the generic types in order of declaration or an empty array if
     *         the type is not genericized
     */
    public static List<? extends Type> getGenerics(Type genericType) {
        List<Type> classes = new ArrayList<Type>();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType)genericType;
            // get the type arguments
            Type[] targs = ptype.getActualTypeArguments();
            for (Type targ : targs) {
                classes.add(targ);
            }
        }
        return classes;
    }

    /**
     * Returns the generic type specified by the class at the given position as
     * in: <p/> <code> public class Foo<Bar,Baz>{ //.. }
     * <p/>
     * JavaIntrospectionHelper.introspectGeneric(Foo.class,1); <code>
     * <p/>
     * will return Baz.
     */
    public static Class introspectGeneric(Class<?> clazz, int pos) {
        assert clazz != null : "No class specified";
        Type type = clazz.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType)type).getActualTypeArguments();
            if (args.length <= pos) {
                throw new IllegalArgumentException("Invalid index value for generic class " + clazz.getName());
            }
            return (Class)((ParameterizedType)type).getActualTypeArguments()[pos];
        } else {
            Type[] interfaces = clazz.getGenericInterfaces();
            for (Type itype : interfaces) {
                if (!(itype instanceof ParameterizedType)) {
                    continue;
                }
                ParameterizedType interfaceType = (ParameterizedType)itype;
                return (Class)interfaceType.getActualTypeArguments()[0];
            }
        }
        return null;
    }

    /**
     * Returns the set of interfaces implemented by the given class and its
     * ancestors or a blank set if none
     */
    public static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> implemented = new HashSet<Class<?>>();
        getAllInterfaces(clazz, implemented);
        return implemented;
    }

    private static void getAllInterfaces(Class<?> clazz, Set<Class<?>> implemented) {
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> interfaze : interfaces) {
            implemented.add(interfaze);
        }
        Class<?> superClass = clazz.getSuperclass();
        // Object has no superclass so check for null
        if (superClass != null && !superClass.equals(Object.class)) {
            getAllInterfaces(superClass, implemented);
        }
    }

    public static boolean isSetter(Method method) {
        return (void.class == method.getReturnType() && method.getParameterTypes().length == 1 && method.getName()
            .startsWith("set"));
    }

    public static boolean isGetter(Method method) {
        return (void.class != method.getReturnType() && method.getParameterTypes().length == 0 && method.getName()
            .startsWith("get"));
    }

    private final static Map<Class<?>, String> signatures = new HashMap<Class<?>, String>();
    static {
        signatures.put(boolean.class, "Z");
        signatures.put(byte.class, "B");
        signatures.put(char.class, "C");
        signatures.put(short.class, "S");
        signatures.put(int.class, "I");
        signatures.put(long.class, "J");
        signatures.put(float.class, "F");
        signatures.put(double.class, "D");
    };

    public static String getSignature(Class<?> cls) {
        if (cls.isPrimitive()) {
            return signatures.get(cls);
        }
        if (cls.isArray()) {
            return "[" + getSignature(cls.getComponentType());
        }
        return "L" + cls.getName().replace('.', '/') + ";";
    }

    public static Class<?> getArrayType(Class<?> componentType, int dims) throws ClassNotFoundException {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < dims; i++) {
            buf.append('[');
        }
        buf.append(getSignature(componentType));
        return Class.forName(buf.toString(), false, componentType.getClassLoader());
    }

    public static Set<Method> getPrivateMethods(Class<?> clazz) {
        Set<Method> methods = new HashSet<Method>();
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (final Method declaredMethod : declaredMethods) {
            int modifiers = declaredMethod.getModifiers();
            if(Modifier.isPrivate(modifiers)) {
                methods.add(declaredMethod);
            }
        }

        return methods;
    }

    public static Set<Field> getPrivateFields(Class<?> clazz) {
        Set<Field> fields = new HashSet<Field>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (final Field declaredField : declaredFields) {
            int modifiers = declaredField.getModifiers();
            if(Modifier.isPrivate(modifiers)) {
                fields.add(declaredField);
            }
        }

        return fields;
    }
}
