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

/**
 * An extension point for StAX artifact processors.
 *
 * @version $Rev: 761053 $ $Date: 2009-04-01 21:46:33 +0100 (Wed, 01 Apr 2009) $
 */
public interface StAXAttributeProcessorExtensionPoint {
    /**
     * Add an artifact processor.
     *
     * @param artifactProcessor The artifact processor to add
     */
    void addArtifactProcessor(StAXAttributeProcessor<?> artifactProcessor);

    /**
     * Remove an artifact processor.
     *
     * @param artifactProcessor The artifact processor to remove
     */
    void removeArtifactProcessor(StAXAttributeProcessor<?> artifactProcessor);

    /**
     * Returns the processor associated with the given artifact type.
     *
     * @param artifactType An artifact type
     * @return The processor associated with the given artifact type
     */
    <T> StAXAttributeProcessor<T> getProcessor(Object artifactType);

    /**
     * Returns the processor associated with the given model type.
     *
     * @param modelType A model type
     * @return The processor associated with the given model type
     */
    <T> StAXAttributeProcessor<T> getProcessor(Class<T> modelType);
}
