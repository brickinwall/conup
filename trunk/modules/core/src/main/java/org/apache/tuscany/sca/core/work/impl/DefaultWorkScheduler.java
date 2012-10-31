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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.LifeCycleListener;
import org.apache.tuscany.sca.work.NotificationListener;
import org.apache.tuscany.sca.work.WorkScheduler;
import org.apache.tuscany.sca.work.WorkSchedulerException;

/**
 * A work scheduler implementation based on a JSR 237 work manager.
 * <p/>
 * <p/>
 * This needs a JSR 237 work manager implementation available for scheduling work. Instances can be configured with a
 * work manager implementation that is injected in. It is the responsibility of the runtime environment to make a work
 * manager implementation available. For example, if the managed environment supports work manager the runtime can use
 * the appropriate lookup mechanism to inject the work manager implementation. </p>
 *
 * @version $Rev: 998309 $ $Date: 2010-09-17 21:37:49 +0100 (Fri, 17 Sep 2010) $
 */
public class DefaultWorkScheduler implements WorkScheduler, LifeCycleListener {

    /**
     * Underlying JSR-237 work manager
     */
    private ThreadPoolWorkManager jsr237WorkManager;
    private int maxThreads = 0;

    /**
     * Initializes the JSR 237 work manager.
     *
     * @param jsr237WorkManager JSR 237 work manager.
     */
    public DefaultWorkScheduler(ExtensionPointRegistry registry, Map<String, String> attributes) {
        if (attributes != null) {
            String value = attributes.get("maxThreads");
            if (value != null) {
                maxThreads = Integer.parseInt(value.trim());
            }
        }
    }

    private synchronized ThreadPoolWorkManager getWorkManager() {
        if (jsr237WorkManager != null) {
            return jsr237WorkManager;
        }
//        try {
//            InitialContext ctx = new InitialContext();
//            jsr237WorkManager = (ThreadPoolWorkManager)ctx.lookup("java:comp/env/wm/TuscanyWorkManager");
//        } catch (Throwable e) {
//            // ignore
//        }
        if (jsr237WorkManager == null) {
            jsr237WorkManager = new ThreadPoolWorkManager(maxThreads);
        }
        return jsr237WorkManager;
    }

    /**
     * Schedules a unit of work for future execution. The notification listener is used to register interest in
     * callbacks regarding the status of the work.
     *
     * @param work The unit of work that needs to be asynchronously executed.
     */
    public <T extends Runnable> void scheduleWork(T work) {
        scheduleWork(work, null);
    }

    /**
     * Schedules a unit of work for future execution. The notification listener is used to register interest in
     * callbacks regarding the status of the work.
     *
     * @param work     The unit of work that needs to be asynchronously executed.
     * @param listener Notification listener for callbacks.
     */
    public <T extends Runnable> void scheduleWork(T work, NotificationListener<T> listener) {

        if (work == null) {
            throw new IllegalArgumentException("Work cannot be null");
        }

        Work<T> jsr237Work = new Work<T>(work);
        try {
            if (listener == null) {
                getWorkManager().schedule(jsr237Work);
            } else {
                Jsr237WorkListener<T> jsr237WorkListener = new Jsr237WorkListener<T>(listener, work);
                getWorkManager().schedule(jsr237Work, jsr237WorkListener);
            }
        } catch (IllegalArgumentException ex) {
            if (listener != null) {
                listener.workRejected(work);
            } else {
                throw new WorkSchedulerException(ex);
            }
        } catch (Exception ex) {
            throw new WorkSchedulerException(ex);
        }

    }

    public void start() {
    }

    public void stop() {
        if (jsr237WorkManager instanceof ThreadPoolWorkManager) {
            // Allow privileged access to modify threads. Requires RuntimePermission in security
            // policy.
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    ((ThreadPoolWorkManager)jsr237WorkManager).destroy();
                    return null;
                }
            });
        }
    }

    /*
     * WorkListener for keeping track of work status callbacks.
     *
     */
    private class Jsr237WorkListener<T extends Runnable> implements WorkListener {

        // Notification listener
        private NotificationListener<T> listener;

        // Work
        private T work;

        /*
        * Initializes the notification listener.
        */
        public Jsr237WorkListener(NotificationListener<T> listener, T work) {
            this.listener = listener;
            this.work = work;
        }

        /*
         * Callback when the work is accepted.
         */
        public void workAccepted(WorkEvent workEvent) {
            T work = getWork();
            listener.workAccepted(work);
        }

        /*
         * Callback when the work is rejected.
         */
        public void workRejected(WorkEvent workEvent) {
            T work = getWork();
            listener.workRejected(work);
        }

        /*
         * Callback when the work is started.
         */
        public void workStarted(WorkEvent workEvent) {
            T work = getWork();
            listener.workStarted(work);
        }

        /*
         * Callback when the work is completed.
         */
        public void workCompleted(WorkEvent workEvent) {
            T work = getWork();
            Exception exception = workEvent.getException();
            if (exception != null) {
                listener.workFailed(work, exception);
            } else {
                listener.workCompleted(work);
            }
        }

        /*
        * Gets the underlying work from the work event.
        */
        private T getWork() {
            return work;
        }

    }

    @Override
    public ExecutorService getExecutorService() {
        return getWorkManager().getExecutorService();
    }
}
