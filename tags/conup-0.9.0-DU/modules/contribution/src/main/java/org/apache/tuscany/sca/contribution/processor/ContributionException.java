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
 * Base class for exceptions raised by contribution services.
 *
 * @version $Rev: 938567 $ $Date: 2010-04-27 17:56:52 +0100 (Tue, 27 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public class ContributionException extends Exception {

    private static final long serialVersionUID = 4432880414927652578L;

    protected ContributionException() {
        super();
    }

    protected ContributionException(String message) {
        super(message);
    }

    protected ContributionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContributionException(Throwable cause) {
        super(cause);
    }
}
