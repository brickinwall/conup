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

package org.apache.tuscany.sca.monitor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;

/**
 * A monitor for the watching for validation problems
 *
 * @version $Rev: 1295144 $ $Date: 2012-02-29 15:06:57 +0000 (Wed, 29 Feb 2012) $
 */
public class MonitorImpl extends Monitor {
    private static final Logger logger = Logger.getLogger(MonitorImpl.class.getName());
    
    // stack of context information that is printed alongside each problem
    private Stack<Object> contextStack = new Stack<Object>();

    // Cache all the problem reported to monitor for further analysis
    private List<Problem> problemCache = new ArrayList<Problem>();

    // Name of an artifact for which problems are being Monitored
    private String artifactName = null;

    public void problem(Problem problem) {

        Logger problemLogger = Logger.getLogger(problem.getSourceClassName(), problem.getResourceBundleName());

        if (problemLogger == null) {
            logger.severe("Can't get logger " + problem.getSourceClassName()
                + " with bundle "
                + problem.getResourceBundleName());
        }

        if (problem.getSeverity() == Severity.INFO) {
            problemCache.add(problem);
            problemLogger.logp(Level.INFO, problem.getSourceClassName(), problem.getContext(), problem.getMessageId(), problem
                .getMessageParams());
        } else if (problem.getSeverity() == Severity.WARNING) {
            problemCache.add(problem);
            problemLogger.logp(Level.WARNING, problem.getSourceClassName(), problem.getContext(), problem.getMessageId(), problem
                .getMessageParams());
        } else if (problem.getSeverity() == Severity.ERROR) {
            if (problem.getCause() != null) {
                problemCache.add(problem);
                problemLogger.logp(Level.SEVERE, problem.getSourceClassName(), problem.getContext(), problem.getMessageId(), problem
                    .getCause());

            } else {
                problemCache.add(problem);
                problemLogger.logp(Level.SEVERE, problem.getSourceClassName(), problem.getContext(), problem.getMessageId(), problem
                    .getMessageParams());
            }
        }
    }

    public List<Problem> getProblems() {
        return problemCache;
    }

    public Problem getLastProblem() {
        if (problemCache.isEmpty()) {
            return null;
        }
        return problemCache.get(problemCache.size() - 1);
    }

    public Problem createProblem(String sourceClassName,
                                 String bundleName,
                                 Severity severity,
                                 Object problemObject,
                                 String messageId,
                                 Throwable cause) {
        return new ProblemImpl(sourceClassName, bundleName, severity, contextStack.toString(), problemObject, messageId, cause);
    }

    public Problem createProblem(String sourceClassName,
                                 String bundleName,
                                 Severity severity,
                                 Object problemObject,
                                 String messageId,
                                 Object... messageParams) {
        return new ProblemImpl(sourceClassName, bundleName, severity, contextStack.toString(), problemObject, messageId, messageParams);
    }
    
    public Problem createProblem(String sourceClassName,
                                 String bundleName,
                                 Severity severity,
                                 Object problemObject,
                                 String messageId,
                                 Throwable cause,
                                 Object... messageParams) {
        return new ProblemImpl(sourceClassName, bundleName, severity, contextStack.toString(), problemObject, messageId, cause, messageParams);
    }

    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }
    
    @Override
    public void pushContext(Object context) {
        contextStack.push(context);
    }
    
    @Override
    public Object popContext() {
        return contextStack.pop();
    }
    
    @Override
    public void reset() {
        contextStack.clear();
        problemCache.clear();
        artifactName = null;
    }
    
    @Override
    public String getMessageString(String loggerName, String messageBundleName, String messageID){
        Logger problemLogger = Logger.getLogger(loggerName, messageBundleName);
        return problemLogger.getResourceBundle().getString(messageID);
    }
}
