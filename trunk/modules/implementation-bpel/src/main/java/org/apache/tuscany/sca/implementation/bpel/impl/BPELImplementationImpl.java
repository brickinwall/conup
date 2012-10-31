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
package org.apache.tuscany.sca.implementation.bpel.impl;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.assembly.impl.ImplementationImpl;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.implementation.bpel.BPELImplementation;
import org.apache.tuscany.sca.implementation.bpel.BPELProcessDefinition;
import org.apache.tuscany.sca.implementation.bpel.xml.BPELPartnerLinkElement;

/**
 * The model representing a BPEL implementation in an SCA assembly model.
 *
 * @version $Rev: 1186226 $ $Date: 2011-10-19 15:03:07 +0100 (Wed, 19 Oct 2011) $
 */
class BPELImplementationImpl extends ImplementationImpl implements BPELImplementation {

    private QName processName;
    private BPELProcessDefinition processDefinition;
    private ComponentType componentType;
    private ModelResolver modelResolver;

    protected BPELImplementationImpl() {
        super(TYPE);
    }

    public QName getProcess() {
        return processName;
    }

    public void setProcess(QName processName) {
        this.processName = processName;
    }

    public BPELProcessDefinition getProcessDefinition() {
        return this.processDefinition;
    }

    public void setProcessDefinition(BPELProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    @Override
    public String getURI() {
        // The BPEL implementation does not have a URI
        return null;
    }

    @Override
    public void setURI(String uri) {
        // The BPEL implementation does not have a URI
    }

    @Override
    public List<Property> getProperties() {
        return componentType.getProperties();
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public ModelResolver getModelResolver() {
        return modelResolver;
    }

    public void setModelResolver(ModelResolver modelResolver) {
        this.modelResolver = modelResolver;
    }

    @Override
    public List<Service> getServices() {
        if (componentType != null){
            return componentType.getServices();
        } else {
            return null;
        }
    }

    @Override
    public List<Reference> getReferences() {
        if (componentType != null){
            return componentType.getReferences();
        } else {
            return null;
        }        
    }

    @Override
    public int hashCode() {
        return String.valueOf(this.getProcess()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BPELImplementation) {
            if (getProcess() != null) {
                return getProcess().equals(((BPELImplementation)obj).getProcess());
            } else {
                return ((BPELImplementation)obj).getProcess() == null;
            }
        } else {
            return false;
        }
    }

	public String getReferencePartnerlinkName(String referenceName) {
		if( referenceName == null ) return null;
		// Find the partnerLink which has its SCAName set to the supplied name
		List<BPELPartnerLinkElement> partnerLinks = processDefinition.getPartnerLinks();
		for( BPELPartnerLinkElement partnerLink : partnerLinks ) {
			if( referenceName.equals(partnerLink.getSCAName()) ) {
				return partnerLink.getName();
			} // end if
		} // end for
		return null;
	} // end method getReferencePartnerlinkName

	public String getServicePartnerlinkName(String serviceName) {
		if( serviceName == null ) return null;
		// Find the partnerLink which has its SCAName set to the supplied name
		List<BPELPartnerLinkElement> partnerLinks = processDefinition.getPartnerLinks();
		for( BPELPartnerLinkElement partnerLink : partnerLinks ) {
			if( serviceName.equals(partnerLink.getSCAName()) ) {
				return partnerLink.getName();
			} // end if
		} // end for		
		return null;
	} // end method getServicePartnerlinkName
}
