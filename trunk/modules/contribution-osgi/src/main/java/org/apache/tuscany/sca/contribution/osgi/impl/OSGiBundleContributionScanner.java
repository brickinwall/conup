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

package org.apache.tuscany.sca.contribution.osgi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.tuscany.sca.contribution.Artifact;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.PackageType;
import org.apache.tuscany.sca.contribution.processor.ContributionException;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.scanner.ContributionScanner;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * Bundle Contribution package processor.
 *
 * @version $Rev: 903542 $ $Date: 2010-01-27 04:33:47 +0000 (Wed, 27 Jan 2010) $
 */
public class OSGiBundleContributionScanner implements ContributionScanner {
    private ContributionFactory contributionFactory;

    public OSGiBundleContributionScanner(ContributionFactory contributionFactory) {
        this.contributionFactory = contributionFactory;
    }

    public String getContributionType() {
        return PackageType.BUNDLE;
    }

    public List<Artifact> scan(Contribution contribution) throws ContributionReadException {
        Bundle bundle = OSGiBundleActivator.findBundle(contribution.getLocation());

        if (bundle == null) {
            throw new IllegalArgumentException("Could not find OSGi bundle " + contribution.getLocation());
        }

        List<Artifact> artifacts = new ArrayList<Artifact>();
        Set<String> bundleClassPath = new HashSet<String>();
        String cp = (String)bundle.getHeaders().get(Constants.BUNDLE_CLASSPATH);
        if (cp != null) {
            String[] paths = cp.split(",");
            for (String path : paths) {
                bundleClassPath.add(path.trim());
            }
        }

        try {
            // Test if the bundle is an Eclipse project
            boolean devMode = (bundle.getEntry("/.project") != null);
            // FIXME: The entries can come from fragments. Do we need to have a way to differentiate the entries?
            Enumeration<?> entries = bundle.findEntries("/", "*", true);
            while (entries.hasMoreElements()) {
                URL entry = (URL)entries.nextElement();
                String entryName = entry.getPath();
                if (devMode && entryName.contains("/.svn/")
                    || entryName.startsWith("/.")
                    || entryName.startsWith("/target/")
                    || entryName.startsWith("/src/")) {
                    // Ignore .svn files
                    // Ignore .classpath, .project, src, and target
                    continue;
                }
                if (entryName.startsWith("/")) {
                    entryName = entryName.substring(1);
                }
                
                //Add artifact to list
                Artifact artifact = contributionFactory.createArtifact();
                artifact.setURI(entryName);
                artifact.setLocation(entry.toString());
                
                artifacts.add(artifact);

                //if Artifact is a JAR, add jar artifacts as well
                /*
                if (entryName.endsWith(".jar") && bundleClassPath.contains(entryName)) {
                    List<String> jarArtifactURIs = getJarArtifacts(entry, entry.openStream());
                    for( String uri : jarArtifactURIs) {
                        Artifact jarArtifact = contributionFactory.createArtifact();
                        jarArtifact.setURI(uri);
                        URL url = bundle.getResource(uri);
                        String location = null;
                        if(url!=null) {
                            location = url.toString();
                        }
                        jarArtifact.setLocation(location);
                        artifacts.add(jarArtifact);
                        
                    }
                }
                */

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        contribution.getExtensions().add(bundle);
        contribution.getTypes().add(getContributionType());
        contribution.setClassLoader(new BundleClassLoader(bundle));
        return artifacts;
    }
    
    /**
     * Retrieve a list of Artifact URIs for a given JAR
     * @param packageSourceURL
     * @param inputStream
     * @return
     * @throws ContributionException
     * @throws IOException
     */
    private List<String> getJarArtifacts(URL packageSourceURL, InputStream inputStream) throws ContributionException, IOException {
        if (packageSourceURL == null) {
            throw new IllegalArgumentException("Invalid null package source URL.");
        }

        if (inputStream == null) {
            throw new IllegalArgumentException("Invalid null source inputstream.");
        }

        // Assume the root is a jar file
        JarInputStream jar = new JarInputStream(inputStream);
        try {
            Set<String> names = new HashSet<String>();
            while (true) {
                JarEntry entry = jar.getNextJarEntry();
                if (entry == null) {
                    // EOF
                    break;
                }

                // FIXME: Maybe we should externalize the filter as a property
                String name = entry.getName();
                if (!name.startsWith(".") && !entry.isDirectory()) {

                    // Trim trailing /
                    if (name.endsWith("/")) {
                        name = name.substring(0, name.length() - 1);
                    }

                    // Add the entry name
                    if (!names.contains(name) && name.length() > 0) {
                        names.add(name);

                    }
                }
            }
            
            // Return list of URIs
            List<String> artifacts = new ArrayList<String>();
            for (String name : names) {
                artifacts.add(name);
            }
            return artifacts;

        } finally {
            jar.close();
        }
    }
    


    private static class BundleClassLoader extends ClassLoader {
        private Bundle bundle;
        public BundleClassLoader(Bundle bundle) {
            super(null);
            this.bundle = bundle;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            return bundle.loadClass(name);
        }

        @Override
        protected URL findResource(String name) {
            return bundle.getResource(name);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Enumeration<URL> findResources(String name) throws IOException {
            Enumeration<URL> urls = bundle.getResources(name);
            if (urls == null) {
                List<URL> list = Collections.emptyList();
                return Collections.enumeration(list);
            } else {
                return urls;
            }
        }
    }

}
