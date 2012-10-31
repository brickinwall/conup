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
package org.apache.tuscany.sca.core.work.impl;



/**
 * Simple Work item that will throw an exception
 * 
 * @version $Rev: 967109 $ $Date: 2010-07-23 15:30:46 +0100 (Fri, 23 Jul 2010) $
 */
public class FailingWork extends Work {

    public FailingWork() {
        super(null);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDaemon() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void release() {
    }

    /**
     * Throws an IllegalArgumentException
     */
    public void run() {
        System.out.println("Starting " + this + " and throwing an Exception");
        throw new IllegalArgumentException("Sample exception from " + this);
    }
}
