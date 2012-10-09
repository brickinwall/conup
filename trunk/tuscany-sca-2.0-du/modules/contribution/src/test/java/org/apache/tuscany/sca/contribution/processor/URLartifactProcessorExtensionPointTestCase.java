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

package org.apache.tuscany.sca.contribution.processor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.net.URL;

import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * URL Artifact Processor Extension Point test case
 * Verifies the right registration and lookup for processors that handle filename and file types
 *
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class URLartifactProcessorExtensionPointTestCase {

    private static URLArtifactProcessorExtensionPoint artifactProcessors;

    @BeforeClass
    public static void setUp() throws Exception {
        ExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        artifactProcessors = new DefaultURLArtifactProcessorExtensionPoint(extensionPoints);
        artifactProcessors.addArtifactProcessor(new FileTypeArtifactProcessor());
        artifactProcessors.addArtifactProcessor(new FileNameArtifactProcessor());
    }

    @Test
    public final void testFileTypeProcessor() {
        assertNotNull(artifactProcessors.getProcessor("dir1/file1.m1"));
        assertNotNull(artifactProcessors.getProcessor("file1.m1"));
    }

    @Test
    public final void testFileNameProcessor() {
        assertNotNull(artifactProcessors.getProcessor("file.m2"));
        assertNotNull(artifactProcessors.getProcessor("dir1/file.m2"));
        assertNull(artifactProcessors.getProcessor("onefile.m2"));
    }

    /**
     * Internal mock classes
     *
     */

    private class M1 {
    }

    private class M2 {
    }

    private static class FileTypeArtifactProcessor implements URLArtifactProcessor<M1> {
        public FileTypeArtifactProcessor() {
        }

        public M1 read(URL contributionURL, URI uri, URL url, ProcessorContext context) throws ContributionReadException {
            return null;
        }

        public void resolve(M1 m1, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        }

        public String getArtifactType() {
            return ".m1";
        }

        public Class<M1> getModelType() {
            return M1.class;
        }
    }

    private static class FileNameArtifactProcessor implements URLArtifactProcessor<M2> {
        public FileNameArtifactProcessor() {
        }

        public M2 read(URL contributionURL, URI uri, URL url, ProcessorContext context) throws ContributionReadException {
            return null;
        }

        public void resolve(M2 m2, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        }

        public String getArtifactType() {
            return "file.m2";
        }

        public Class<M2> getModelType() {
            return M2.class;
        }
    }

}
