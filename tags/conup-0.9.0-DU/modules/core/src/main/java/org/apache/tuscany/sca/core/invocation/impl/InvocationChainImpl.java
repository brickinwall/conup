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
package org.apache.tuscany.sca.core.invocation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.tuscany.sca.core.invocation.InterceptorAsyncImpl;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.DataExchangeSemantics;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.InterceptorAsync;
import org.apache.tuscany.sca.invocation.InvocationChain;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.InvokerAsyncRequest;
import org.apache.tuscany.sca.invocation.InvokerAsyncResponse;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;

/**
 * Default implementation of an invocation chain
 * 
 * @version $Rev: 1057648 $ $Date: 2011-01-11 22:14:06 +0800 (周二, 11 一月 2011) $
 */
public class InvocationChainImpl implements InvocationChain {
    private Operation sourceOperation;
    private Operation targetOperation;
    private List<Node> nodes = new ArrayList<Node>();

    private final PhaseManager phaseManager;
    private boolean forReference;
    private boolean allowsPassByReference;
    private boolean isAsyncInvocation;

    public InvocationChainImpl(Operation sourceOperation, Operation targetOperation, boolean forReference, PhaseManager phaseManager, boolean isAsyncInvocation) {
        this.targetOperation = targetOperation;
        this.sourceOperation = sourceOperation;
        this.forReference = forReference;
        this.phaseManager = phaseManager;
        this.isAsyncInvocation = isAsyncInvocation;
    }

    public Operation getTargetOperation() {
        return targetOperation;
    }

    public void setTargetOperation(Operation operation) {
        this.targetOperation = operation;
    }

    public void addInterceptor(Interceptor interceptor) {
        if (interceptor instanceof PhasedInterceptor) {
            PhasedInterceptor pi = (PhasedInterceptor)interceptor;
            if (pi.getPhase() != null) {
                addInvoker(pi.getPhase(), pi);
                return;
            }
        }
        String phase = forReference ? Phase.REFERENCE : Phase.SERVICE;
        addInterceptor(phase, interceptor);
    } // end method addInterceptor

    public void addInvoker(Invoker invoker) {
        if (invoker instanceof PhasedInterceptor) {
            PhasedInterceptor pi = (PhasedInterceptor)invoker;
            if (pi.getPhase() != null) {
                addInvoker(pi.getPhase(), pi);
                return;
            }
        }
        String phase = forReference ? Phase.REFERENCE_BINDING : Phase.IMPLEMENTATION;
        addInvoker(phase, invoker);
    }

    public Invoker getHeadInvoker() {
        return nodes.isEmpty() ? null : nodes.get(0).getInvoker();
    }
    
    public Invoker getTailInvoker() {
    	int nodeCount = nodes.size();
    	if( nodeCount > 0 ) {
    		return nodes.get( nodeCount - 1).getInvoker();
    	} // end if
    	
        return null;
    } // end method getTailInvoker
    
    public Invoker getHeadInvoker(String phase) {
        int index = phaseManager.getAllPhases().indexOf(phase);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid phase name: " + phase);
        }
        for (Node node : nodes) {
            if (index <= node.getPhaseIndex()) {
                return node.getInvoker();
            }
        }
        return null;
    }

    /**
     * @return the sourceOperation
     */
    public Operation getSourceOperation() {
        return sourceOperation;
    }

    /**
     * @param sourceOperation the sourceOperation to set
     */
    public void setSourceOperation(Operation sourceOperation) {
        this.sourceOperation = sourceOperation;
    }

    public void addInterceptor(String phase, Interceptor interceptor) {
        addInvoker(phase, interceptor);
    }

    private void addInvoker(String phase, Invoker invoker) {
        if (isAsyncInvocation &&
            !(invoker instanceof InvokerAsyncRequest) &&
            !(invoker instanceof InvokerAsyncResponse) ){
            // TODO - should raise an error but don't want to break
            //        the existing non-native async support
/*            
            throw new IllegalArgumentException("Trying to add synchronous invoker " +
                                               invoker.getClass().getName() +
                                               " to asynchronous chain");
*/                                             
        }
        
        int index = phaseManager.getAllPhases().indexOf(phase);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid phase name: " + phase);
        }
        Node node = new Node(index, invoker);
        ListIterator<Node> li = nodes.listIterator();
        Node before = null, after = null;
        boolean found = false;
        while (li.hasNext()) {
            before = after;
            after = li.next();
            if (after.getPhaseIndex() > index) {
                // Move back
                li.previous();
                li.add(node);
                found = true;
                break;
            }
        }
        if (!found) {
            // Add to the end
            nodes.add(node);
            before = after;
            after = null;
        }

        // Relink the interceptors
        if (before != null) {
            if (before.getInvoker() instanceof Interceptor) {
                ((Interceptor)before.getInvoker()).setNext(invoker);
                if ((invoker instanceof InterceptorAsync) &&
                    (before.getInvoker() instanceof InvokerAsyncResponse)) {
                    ((InterceptorAsync) invoker).setPrevious((InvokerAsyncResponse)before.getInvoker());
                }
            }
        }
        if (after != null) {
            if (invoker instanceof Interceptor) {
                ((Interceptor)invoker).setNext(after.getInvoker());
                if ((after.getInvoker() instanceof InterceptorAsync) &&
                    (invoker instanceof InvokerAsyncResponse)){
                    ((InterceptorAsync) after.getInvoker()).setPrevious((InvokerAsyncResponse)invoker);
                }
            }
        }

    }

    public boolean allowsPassByReference() {
        if (allowsPassByReference) {
            // No need to check the invokers
            return true;
        }
        // Check if any of the invokers allows pass-by-reference
        boolean allowsPBR = false;
        for (Node i : nodes) {
            if (i.getInvoker() instanceof DataExchangeSemantics) {
                if (((DataExchangeSemantics)i.getInvoker()).allowsPassByReference()) {
                    allowsPBR = true;
                    break;
                }
            }
        }
        return allowsPBR;
    }

    public void setAllowsPassByReference(boolean allowsPBR) {
        this.allowsPassByReference = allowsPBR;
    }

    private static class Node {
        private int phaseIndex;
        private Invoker invoker;

        public Node(int phaseIndex, Invoker invoker) {
            super();
            this.phaseIndex = phaseIndex;
            this.invoker = invoker;
        }

        public int getPhaseIndex() {
            return phaseIndex;
        }

        public Invoker getInvoker() {
            return invoker;
        }

        @Override
        public String toString() {
            return "(" + phaseIndex + ")" + invoker;
        }
    }
    
    public boolean isAsyncInvocation() {
        return isAsyncInvocation;
    }

	public void addHeadInterceptor(Interceptor interceptor) {
		String phase = forReference ? Phase.REFERENCE : Phase.SERVICE_BINDING;
        if (interceptor instanceof PhasedInterceptor) {
            PhasedInterceptor pi = (PhasedInterceptor)interceptor;
            if (pi.getPhase() != null) {
            	phase = pi.getPhase();
            } // end if
        } // end if
       
        addHeadInterceptor(phase, interceptor);
	} // end method addHeadInterceptor

	public void addHeadInterceptor(String phase, Interceptor interceptor) {
		// TODO Auto-generated method stub
		Invoker invoker = (Invoker)interceptor;
		
        int index = phaseManager.getAllPhases().indexOf(phase);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid phase name: " + phase);
        } // end if 
        Node node = new Node(index, invoker);
        
        ListIterator<Node> li = nodes.listIterator();
        Node before = null, after = null;
        boolean found = false;
        while (li.hasNext()) {
            before = after;
            after = li.next();
            // Look for the first node with a phase index equal to or greater than the one provided
            if (after.getPhaseIndex() >= index) {
                // Move back
                li.previous();
                li.add(node);
                found = true;
                break;
            }
        }
        if (!found) {
            // Add to the end
            nodes.add(node);
            before = after;
            after = null;
        }

        // Relink the interceptors
        if (before != null) {
            if (before.getInvoker() instanceof Interceptor) {
                ((Interceptor)before.getInvoker()).setNext(invoker);
                if ((invoker instanceof InterceptorAsync) &&
                    (before.getInvoker() instanceof InvokerAsyncResponse)) {
                    ((InterceptorAsync) invoker).setPrevious((InvokerAsyncResponse)before.getInvoker());
                }
            }
        }
        if (after != null) {
            if (invoker instanceof Interceptor) {
                ((Interceptor)invoker).setNext(after.getInvoker());
                if ((after.getInvoker() instanceof InterceptorAsync) &&
                    (invoker instanceof InvokerAsyncResponse)){
                    ((InterceptorAsync) after.getInvoker()).setPrevious((InvokerAsyncResponse)invoker);
                }
            }
        }

	} // end method addHeadInterceptor

}
