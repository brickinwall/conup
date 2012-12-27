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

/**
 * PullTransformer transforms data from one binding format to the other one which can be directly consumed
 * 
 * @param <S> The source data type
 * @param <R> the target data type
 *
 * @version $Rev: 938572 $ $Date: 2010-04-27 18:14:08 +0100 (Tue, 27 Apr 2010) $
 * @tuscany.spi.extension.inheritfrom
 */
public interface PullTransformer<S, R> extends Transformer {
    /**
     * Transform source data into the result type.
     * 
     * @param source The source data
     * @param context The context for the transformation
     * @return The transformed result
     */
    R transform(S source, TransformationContext context);
}
