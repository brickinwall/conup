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

package org.apache.tuscany.sca.monitor;

import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Loads a monitor and adds some problems to it.
 *
 * @version $Rev: 827745 $ $Date: 2009-10-20 18:53:37 +0100 (Tue, 20 Oct 2009) $
 */
public class MonitorTestCase {

    private static final String MESSAGES = "org.apache.tuscany.sca.monitor.tuscany-monitor-test-messages";
    private static MonitorFactory monitorFactory;

    @BeforeClass
    public static void init() throws Exception {
        monitorFactory = new DefaultMonitorFactory();
    }

    @AfterClass
    public static void destroy() throws Exception {
        monitorFactory = null;
    }

    @Test
    public void testCreateProblem() throws Exception {
        String dummyModelObject = "DUMMY MODEL OBJECT";

        Monitor monitor = monitorFactory.createMonitor();

        Problem problem = null;

        problem =
            monitor.createProblem(this.getClass().getName(),
                                  MESSAGES,
                                  Severity.WARNING,
                                  dummyModelObject,
                                  "MESSAGE1");
        monitor.problem(problem);

        String param = "Some Parameter";

        problem =
            monitor.createProblem(this.getClass().getName(),
                                  MESSAGES,
                                  Severity.WARNING,
                                  dummyModelObject,
                                  "MESSAGE2",
                                  param);
        monitor.problem(problem);

        problem =
            monitor.createProblem(this.getClass().getName(),
                                  MESSAGES,
                                  Severity.WARNING,
                                  dummyModelObject,
                                  "MESSAGE3",
                                  8,
                                  9,
                                  4);
        monitor.problem(problem);

        Exception ex = new IllegalStateException("TEST_MESSAGE");

        problem =
            monitor.createProblem(this.getClass().getName(),
                                  MESSAGES,
                                  Severity.ERROR,
                                  dummyModelObject,
                                  "MESSAGE4",
                                  ex);
        monitor.problem(problem);

    }

}
