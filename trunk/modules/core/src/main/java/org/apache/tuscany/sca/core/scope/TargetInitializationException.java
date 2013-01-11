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
package org.apache.tuscany.sca.core.scope;

/**
 * Denotes an error initializing a target
 *
 * @version $Rev: 937310 $ $Date: 2010-04-23 22:27:50 +0800 (周五, 23 四月 2010) $
 * @tuscany.spi.extension.asclient
 */
public class TargetInitializationException extends TargetResolutionException {
    private static final long serialVersionUID = -6228778208649752698L;

    public TargetInitializationException() {
        super();
    }

    public TargetInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TargetInitializationException(String message) {
        super(message);
    }

    public TargetInitializationException(Throwable cause) {
        super(cause);
    }
}
