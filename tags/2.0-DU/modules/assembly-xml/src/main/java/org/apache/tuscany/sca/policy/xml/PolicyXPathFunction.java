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

package org.apache.tuscany.sca.policy.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The SCA-defined XPath function
 */
public class PolicyXPathFunction implements XPathFunction {
    private static Logger logger = Logger.getLogger(PolicyXPathFunction.class.getName());

    static final QName InterfaceRef = new QName(PolicyConstants.SCA11_NS, "InterfaceRef");
    static final QName OperationRef = new QName(PolicyConstants.SCA11_NS, "OperationRef");
    static final QName MessageRef = new QName(PolicyConstants.SCA11_NS, "MessageRef");
    static final QName IntentRefs = new QName(PolicyConstants.SCA11_NS, "IntentRefs");
    static final QName URIRef = new QName(PolicyConstants.SCA11_NS, "URIRef");

    static final Set<QName> functions =
        new HashSet<QName>(Arrays.asList(InterfaceRef, OperationRef, MessageRef, IntentRefs, URIRef));

    private NamespaceContext namespaceContext;
    private final QName functionName;

    public PolicyXPathFunction(NamespaceContext namespaceContext, QName functionName) {
        super();
        this.namespaceContext = namespaceContext;
        this.functionName = functionName;
    }

    private Node getContextNode(List args) {
        if (args.size() >= 2) {
            NodeList nodeList = (NodeList)args.get(1);
            if (nodeList.getLength() > 0) {
                return nodeList.item(0);
            }
        }
        return null;
    }

    public Object evaluate(List args) throws XPathFunctionException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(functionName + "(" + args + ")");
        }

        String arg = (String)args.get(0);
        Node node = getContextNode(args);
        /**
         * If the xpath expression that contains the function does not select any nodes
         * (eg IntentRefs('someIntent')), the context node passed in will be a Document. 
         * In this case we need to iterate over every Node in the Document and evaluate it. 
         * 
         * If the xpath expression does select nodes (eg //sca:component[IntentRefs('someIntent')])
         * then xpath will call evaluate for each node and we only need to return the result for that
         * node.
         */
        if ( node instanceof Document ) 
        	return evaluateDocument(arg, (Document)node);
        else
        	return evaluateNode(arg, node);
    }
    
    public Object evaluateNode(String arg, Node node) {
        if (InterfaceRef.equals(functionName)) {
            return evaluateInterface(arg, node);
        } else if (OperationRef.equals(functionName)) {
            String[] params = arg.split("/");
            if (params.length != 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String interfaceName = params[0];
            String operationName = params[1];
            return evaluateOperation(interfaceName, operationName, node);
        } else if (MessageRef.equals(functionName)) {
            String[] params = arg.split("/");
            if (params.length != 3) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String interfaceName = params[0];
            String operationName = params[1];
            String messageName = params[2];
            return evaluateMessage(interfaceName, operationName, messageName, node);
        } else if (URIRef.equals(functionName)) {
            return evaluateURI(arg, node);
        } else if (IntentRefs.equals(functionName)) {
            String[] intents = arg.split("(\\s)+");
            return evaluateIntents(intents, node);
        } else {
            return Boolean.FALSE;
        }
    }

    private class NodeListImpl implements NodeList {

    	private ArrayList<Node> list;

		public NodeListImpl() {
    		this.list = new ArrayList<Node>();
    	}
		public int getLength() {
			return this.list.size();
		}

		public Node item(int index) {			
			return this.list.get(index);
		}
		public boolean add(Node node) {
			return this.list.add(node);
			
		}
    	
    }
    private Object evaluateDocument(String arg, Document doc) {
    	NodeListImpl retList = new NodeListImpl();
    	NodeList elements = doc.getElementsByTagName("*");
		for ( int i=0; i < elements.getLength(); i++) {
			Object node = evaluateNode(arg, elements.item(i));
			if ( node != null ) 
				retList.add((Node)node);
		}
		return retList;
	}

	private Boolean evaluateInterface(String interfaceName, Node node) {
        return Boolean.FALSE;
    }

    private Boolean evaluateOperation(String interfaceName, String operationName, Node node) {
        return Boolean.FALSE;
    }

    private Boolean evaluateMessage(String interfaceName, String operationName, String messageName, Node node) {
        return Boolean.FALSE;
    }

    private Boolean evaluateURI(String uri, Node node) {
        return Boolean.FALSE;
    }

    /**
     * Evaluates a single node for the given intents. 
     * @param intents
     * @param node
     * @return
     */
    private Object evaluateIntents(String[] intents, Node node) {
    	if ( node == null ) 
    		return false;
    	    	
    	if ( node.getAttributes() != null ) {
    		for  ( int i=0; i < node.getAttributes().getLength(); i++) {
    			Node attr = node.getAttributes().item(i);
    			
    			if ( "requires".equalsIgnoreCase(attr.getNodeName())) {
    				
    				for ( int j = 0; j < intents.length; j++ ) {
    					// Check negative intents
    					if ( intents[j].startsWith("!")) {
    						if ( matchIntent(intents[j].substring(1), attr, node.getNamespaceURI()))
    							return null; 
    					} else if ( !matchIntent(intents[j], attr, node.getNamespaceURI())){
    						return null; 
    					}    					
    				}
    				return node; 
    			}
    			
    		}
    	}

        return null; 
    }

    

    /**
     * Determine whether the given intent is present in the "requires" attribute
     * @param intent
     * @param node
     * @param namespaceURI
     * @return
     */
	private boolean matchIntent(String intent, Node node, String namespaceURI) {
    	String[] requires = node.getNodeValue().split("(\\s)+");
		QName intentName = getStringAsQName(intent);
		
		
		for ( int i=0; i < requires.length; i++ ) {
			QName nodeName = null;
			int idx = requires[i].indexOf(':');
			
			// No prefix specified
			if ( idx == -1 ) {
				nodeName = new QName(namespaceURI, requires[i]);
			} else {
				String prefix = requires[i].substring(0, idx);
				String name = requires[i].substring(idx + 1);
				String ns = node.lookupNamespaceURI(prefix);
				nodeName = new QName(ns, name, prefix);
			}
			if ( intentName.equals(nodeName))
				return true;
		}
		return false;
	}


	private QName getStringAsQName(String intent) {
		int idx = intent.indexOf(':');
		if (idx == -1)
			return new QName(namespaceContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX), intent);
		
		String prefix = intent.substring(0, idx);
		intent = intent.substring(idx + 1);
		
		return new QName(namespaceContext.getNamespaceURI(prefix), intent, prefix);
		
	}


	private static Pattern FUNCTION;
    static {
        String functionPattern = "(URIRef|InterfaceRef|OperationRef|MessageRef|IntentRefs)\\s*\\(([^\\)]*)\\)";
        FUNCTION = Pattern.compile(functionPattern);
    }

    /** Adds the node as an argument to the XPath function. 
     * Required in order to have access to the NodeList within the function
     */
    public static String normalize(String attachTo, String scaPrefix) {
    	// Get rid of any whitespace
    	attachTo = attachTo.trim();
    	
        Matcher matcher = FUNCTION.matcher(attachTo);
        boolean result = matcher.find();
        if (result) {
            StringBuffer sb = new StringBuffer();
            do {
                String function = matcher.group(1);
                String args = matcher.group(2);
                if ( (matcher.start() == 0) || (attachTo.charAt( matcher.start() -1) != ':' )) {
                	function = scaPrefix + ":" + function; 
                }
                String replacement = null;
                if (args.trim().length() > 0) {
                    replacement = function + "(" + args + "," + "self::node())";
                } else {
                    replacement = function + "(self::node())";
                }
                matcher.appendReplacement(sb, replacement);
                result = matcher.find();
            } while (result);
            
            matcher.appendTail(sb);
            return sb.toString();
        }
        return attachTo;
    }

}
