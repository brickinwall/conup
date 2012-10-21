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

package org.apache.tuscany.sca.contribution.impl;

import org.apache.tuscany.sca.contribution.Artifact;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.ContributionMetadata;
import org.apache.tuscany.sca.contribution.DefaultExport;
import org.apache.tuscany.sca.contribution.DefaultImport;


/**
 * Default implementation of a contribution model factory.
 *
 * @version $Rev: 763837 $ $Date: 2009-04-10 01:11:48 +0100 (Fri, 10 Apr 2009) $
 */
public class ContributionFactoryImpl implements ContributionFactory {

    protected ContributionFactoryImpl() {
    }

    public Contribution createContribution() {
        return new ContributionImpl();
    }

    public ContributionMetadata createContributionMetadata() {
        return new ContributionMetadataImpl();
    }

    public Artifact createArtifact() {
        return new ArtifactImpl();
    }

    public DefaultExport createDefaultExport() {
        return new DefaultExportImpl();
    }

    public DefaultImport createDefaultImport() {
        return new DefaultImportImpl();
    }

}
