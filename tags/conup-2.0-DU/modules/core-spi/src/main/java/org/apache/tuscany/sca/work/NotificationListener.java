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
package org.apache.tuscany.sca.work;

/**
 * A callback interface that can be optionally used for registering 
 * interest in status of asynchronously scheduled unit of work.
 *
 * @version $Rev: 655921 $ $Date: 2008-05-13 16:45:54 +0100 (Tue, 13 May 2008) $
 */
public interface NotificationListener<T extends Runnable> {
    
    /**
     * Callback method when the unit of work is accepted.
     * 
     * @param work Work that was accepted.
     */
    void workAccepted(T work);
    
    /**
     * Callback method when the unit of work is successfully completed.
     * 
     * @param work Work that was successfully completed.
     */
    void workCompleted(T work);
    
    /**
     * Callback when the unit of work is started.
     * 
     * @param work Unit of work that was started.
     */
    void workStarted(T work);
    
    /**
     * Callback when the unit of work is rejected.
     * 
     * @param work Unit of work that was rejected.
     */
    void workRejected(T work);
    
    /**
     * Callback when the unit of work fails to complete.
     * 
     * @param work Unit of work that failed to complete.
     * @param error Error that caused the unit of work to fail.
     */
    void workFailed(T work, Throwable error);
    
    

}
