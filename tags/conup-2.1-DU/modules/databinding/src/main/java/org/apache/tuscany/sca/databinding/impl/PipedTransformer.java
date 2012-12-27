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
package org.apache.tuscany.sca.databinding.impl;

import org.apache.tuscany.sca.databinding.DataPipe;
import org.apache.tuscany.sca.databinding.DataPipeTransformer;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.PushTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;

/**
 * A utility class to connect PushTransformer and DataPipe to create a
 * PullTransformer
 * 
 * @param <S> Source type
 * @param <I> Intermediate type
 * @param <R> Result type
 *
 * @version $Rev: 656146 $ $Date: 2008-05-14 09:22:08 +0100 (Wed, 14 May 2008) $
 */
public class PipedTransformer<S, I, R> implements PullTransformer<S, R> {
    private PushTransformer<S, I> pusher;

    private DataPipeTransformer<I, R> pipe;

    /**
     * @param pumper
     * @param pipe
     */
    public PipedTransformer(PushTransformer<S, I> pumper, DataPipeTransformer<I, R> pipe) {
        super();
        this.pusher = pumper;
        this.pipe = pipe;
    }

    public R transform(S source, TransformationContext context) {
        DataPipe<I, R> dataPipe = pipe.newInstance();
        pusher.transform(source, dataPipe.getSink(), context);
        return dataPipe.getResult();
    }

    public String getSourceDataBinding() {
        return pusher.getSourceDataBinding();
    }

    public String getTargetDataBinding() {
        return pipe.getTargetDataBinding();
    }

    public int getWeight() {
        return pusher.getWeight() + pipe.getWeight();
    }

}
