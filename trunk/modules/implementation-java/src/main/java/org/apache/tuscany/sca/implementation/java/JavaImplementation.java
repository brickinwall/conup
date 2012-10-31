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

package org.apache.tuscany.sca.implementation.java;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Represents a Java implementation.
 *
 * @version $Rev: 950295 $ $Date: 2010-06-01 23:57:55 +0100 (Tue, 01 Jun 2010) $
 */
public interface JavaImplementation extends BaseJavaImplementation {
    QName TYPE = new QName(SCA11_NS, "implementation.java");
    /**
     * Returns the constructor used to instantiate implementation instances.
     *
     * @return the constructor used to instantiate implementation instances
     */
    JavaConstructorImpl<?> getConstructor();

    /**
     * Sets the constructor used to instantiate implementation instances
     *
     * @param definition the constructor used to instantiate implementation instances
     */
    void setConstructor(JavaConstructorImpl<?> definition);

    /**
     * Returns the component initializer method.
     *
     * @return the component initializer method
     */
    Method getInitMethod();

    /**
     * Sets the component initializer method.
     *
     * @param initMethod the component initializer method
     */
    void setInitMethod(Method initMethod);

    /**
     * Returns the component destructor method.
     *
     * @return the component destructor method
     */
    Method getDestroyMethod();

    /**
     * Sets the component destructor method.
     *
     * @param destroyMethod the component destructor method
     */
    void setDestroyMethod(Method destroyMethod);

    /**
     * Returns the resources injected into this implementation.
     *
     * @return
     */
    Map<String, JavaResourceImpl> getResources();

    /**
     * Returns the Java member used to inject a conversation ID.
     *
     * @return
     */
    List<Member> getConversationIDMembers();

    /**
     * Sets the Java member used to inject a conversation ID.
     *
     * @param conversationIDMember
     */
    void addConversationIDMember(Member conversationIDMember);

    /**
     * Returns true if AllowsPassReference is set.
     *
     * @return true if AllowsPassByReference is set
     */
    boolean isAllowsPassByReference();

    /**
     * @param allowsPassByReference the allowsPassByReference to set
     */
    void setAllowsPassByReference(boolean allowsPassByReference);

    /**
     * @return the allowsPassByReferenceMethods
     */
    List<Method> getAllowsPassByReferenceMethods();

    /**
     * @param method
     * @return
     */
    boolean isAllowsPassByReference(Method method);

    /**
     * @return the constructors
     */
    Map<Constructor, JavaConstructorImpl> getConstructors();

    /**
     * @return the eagerInit
     */
    boolean isEagerInit();

    /**
     * @param eagerInit the eagerInit to set
     */
    void setEagerInit(boolean eagerInit);

    /**
     * @return the callbacks
     */
    Map<String, Collection<JavaElementImpl>> getCallbackMembers();

    /**
     * @return the properties
     */
    Map<String, JavaElementImpl> getPropertyMembers();

    /**
     * @return the references
     */
    Map<String, JavaElementImpl> getReferenceMembers();

    /**
     * @return the scope
     */
    JavaScopeImpl getJavaScope();

    /**
     * @param scope the scope to set
     */
    void setJavaScope(JavaScopeImpl scope);

}
