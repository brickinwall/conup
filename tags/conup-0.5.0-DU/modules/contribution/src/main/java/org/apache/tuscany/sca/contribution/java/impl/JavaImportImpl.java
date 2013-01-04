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

package org.apache.tuscany.sca.contribution.java.impl;

import java.util.List;

import org.apache.tuscany.sca.assembly.impl.ExtensibleImpl;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.Export;
import org.apache.tuscany.sca.contribution.java.JavaExport;
import org.apache.tuscany.sca.contribution.java.JavaImport;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;

/**
 * Implementation of a Java Import model
 *
 * @version $Rev: 767701 $ $Date: 2009-04-22 23:49:55 +0100 (Wed, 22 Apr 2009) $
 */
public class JavaImportImpl extends ExtensibleImpl implements JavaImport {
    private ModelResolver modelResolver;
    private List<Contribution> contributions;
    /**
     * Java package name being imported
     */
    private String packageName;
    /**
     * Contribution URI where the artifact is imported from
     */
    private String location;

    public JavaImportImpl() {
        super();
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPackage() {
        return this.packageName;
    }

    public void setPackage(String packageName) {
        this.packageName = packageName;
    }

    public ModelResolver getModelResolver() {
        return this.modelResolver;
    }

    public void setModelResolver(ModelResolver modelResolver) {
        this.modelResolver = modelResolver;
    }
    public List<Contribution> getExportContributions() {
        return contributions;
    }

    public void setExportContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }

    /**
     * Match a JavaImport to a given JavaExport based on :
     *    location is not provided
     *    import and export packages match
     */
    public boolean match(Export export) {
        if(export instanceof JavaExport) {
            JavaExport javaExport = (JavaExport)export;
            String exportedPackage = javaExport.getPackage();
            if (packageName.equals(exportedPackage)) {
                return true;
            } else {
                if (packageName.endsWith(".*")) {
                    String prefix = packageName.substring(0, packageName.length() - 1);
                    if (exportedPackage.startsWith(prefix)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(packageName);
    }
}
