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
package org.apache.tuscany.sca.databinding;

import java.util.List;

/**
 * An extension point for data transformer extensions.
 *
 * @version $Rev: 656146 $ $Date: 2008-05-14 09:22:08 +0100 (Wed, 14 May 2008) $
 */
public interface TransformerExtensionPoint {

    /**
     * Register a transformer
     * 
     * @param sourceDataBinding
     * @param targetDataBinding
     * @param weight
     * @param transformer
     * @param publicTransformer TODO
     */
    void addTransformer(String sourceDataBinding,
                        String targetDataBinding,
                        int weight,
                        Transformer transformer,
                        boolean publicTransformer);

    /**
     * Register a transformer
     * 
     * @param transformer
     * @param publicTransformer TODO
     */
    void addTransformer(Transformer transformer, boolean publicTransformer);

    /**
     * Unregister a transformer
     * 
     * @param sourceDataBinding
     * @param targetDataBinding
     * @return
     */
    boolean removeTransformer(String sourceDataBinding, String targetDataBinding);

    /**
     * Get the direct Transformer which can transform data from source type to
     * result type
     * 
     * @param sourceDataBinding
     * @param targetDataBinding
     * @return
     */
    Transformer getTransformer(String sourceDataBinding, String targetDataBinding);

    /**
     * Get the a chain of Transformers which can transform data from source type
     * to result type
     * 
     * @param sourceDataBinding
     * @param targetDataBinding
     * @return
     */
    List<Transformer> getTransformerChain(String sourceDataBinding, String targetDataBinding);
}
