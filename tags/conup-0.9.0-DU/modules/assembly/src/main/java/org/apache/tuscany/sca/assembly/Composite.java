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
package org.apache.tuscany.sca.assembly;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.policy.PolicySubject;

/**
 * Represents a composite.
 *
 * @version $Rev: 1137220 $ $Date: 2011-06-18 19:03:56 +0100 (Sat, 18 Jun 2011) $
 */
public interface Composite extends Implementation, Extensible, Cloneable, PolicySubject {
    /**
     * Special name for the domain composite
     */
    QName DOMAIN_COMPOSITE = new QName(SCA11_NS, "");
    QName TYPE = new QName(SCA11_NS, "implementation.composite");

    /**
     * Returns the spec version of the composite used to build this model
     *
     * @return the spec version used to build this model
     */
    String getSpecVersion();

    /**
     * Sets the spec version of the composite used to build this model
     *
     * @param specVersion the spec version used to build this model
     */
    void setSpecVersion(String specVersion);

    String getContributionURI();
    void setContributionURI(String contributionURI);
    
    /**
     * Returns the name of the composite.
     *
     * @return the name of the composite
     */
    QName getName();

    /**
     * Sets the name of the composite.
     *
     * @param name the name of the composite
     */
    void setName(QName name);

    /**
     * Returns a list of composites included in this composite.
     *
     * @return a list of composites included in this composite.
     */
    List<Composite> getIncludes();

    public List<Composite> getFusedIncludes();

    /**
     * Returns a list of components contained in this composite.
     *
     * @return a list of components contained in this composite
     */
    List<Component> getComponents();
    
    /**
     * Returns a component by name
     */
    Component getComponent(String name);
    
    /**
     * Returns a list of wires contained in this composite.
     *
     * @return a list of wires contained in this composite
     */
    List<Wire> getWires();

    /**
     * Returns true if all the components within the composite must run in the
     * same process.
     *
     * @return true if all the components within the composite must run in the
     *         same process
     */
    boolean isLocal();

    /**
     * Sets whether all the components within the composite must run in the same
     * process.
     *
     * @param local whether all the components within the composite must run in
     *            the same process
     */
    void setLocal(boolean local);

    /**
     * Return the Boolean value of autowire
     * @return null/TRUE/FALSE
     */
    Boolean getAutowire();

    /**
     * Sets whether component references should be autowired.
     *
     * @param autowire whether component references should be autowired
     */
    void setAutowire(Boolean autowire);

    /**
     * Returns a clone of the component type.
     *
     * @return a clone of the component type
     * @throws CloneNotSupportedException
     */
    Object clone() throws CloneNotSupportedException;

}
