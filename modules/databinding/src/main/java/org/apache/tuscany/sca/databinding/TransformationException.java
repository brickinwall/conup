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
 * Reports problems during data transformation
 *
 * @version $Rev: 938572 $ $Date: 2010-04-27 18:14:08 +0100 (Tue, 27 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public class TransformationException extends RuntimeException {

    private static final long serialVersionUID = 7662385613693006428L;
    private String sourceDataBinding;
    private String targetDataBinding;

    public TransformationException() {
        super();
    }

    public TransformationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransformationException(String message) {
        super(message);
    }

    public TransformationException(Throwable cause) {
        super(cause);
    }

    public String getSourceDataBinding() {
        return sourceDataBinding;
    }

    public void setSourceDataBinding(String sourceDataBinding) {
        this.sourceDataBinding = sourceDataBinding;
    }

    public String getTargetDataBinding() {
        return targetDataBinding;
    }

    public void setTargetDataBinding(String targetDataBinding) {
        this.targetDataBinding = targetDataBinding;
    }

}
