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

package org.apache.tuscany.sca.core.databinding.transformers;

import java.util.List;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.databinding.DataBinding;
import org.apache.tuscany.sca.databinding.Mediator;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.WrapperHandler;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;
import static org.apache.tuscany.sca.interfacedef.Operation.IDL_INPUT;
import org.apache.tuscany.sca.interfacedef.util.ElementInfo;
import org.apache.tuscany.sca.interfacedef.util.WrapperInfo;
import org.apache.tuscany.sca.interfacedef.util.XMLType;

/**
 * This is a special transformer to transform the input from one IDL to the
 * other one
 *
 * @version $Rev: 1236129 $ $Date: 2012-01-26 10:24:43 +0000 (Thu, 26 Jan 2012) $
 */
public class Input2InputTransformer extends BaseTransformer<Object[], Object[]> implements
    PullTransformer<Object[], Object[]> {
    protected Mediator mediator;

    public Input2InputTransformer(ExtensionPointRegistry registry) {
        super();
        this.mediator = registry.getExtensionPoint(UtilityExtensionPoint.class).getUtility(Mediator.class);
    }

    @Override
    public String getSourceDataBinding() {
        return IDL_INPUT;
    }

    @Override
    public String getTargetDataBinding() {
        return IDL_INPUT;
    }

    /**
     * @see org.apache.tuscany.sca.databinding.BaseTransformer#getSourceType()
     */
    @Override
    protected Class<Object[]> getSourceType() {
        return Object[].class;
    }

    /**
     * @see org.apache.tuscany.sca.databinding.BaseTransformer#getTargetType()
     */
    @Override
    protected Class<Object[]> getTargetType() {
        return Object[].class;
    }

    /**
     * @see org.apache.tuscany.sca.databinding.Transformer#getWeight()
     */
    @Override
    public int getWeight() {
        return 10000;
    }

    /**
     * Match the structure of the wrapper element. If it matches, then we can do
     * wrapper to wrapper transformation. Otherwise, we do child to child.
     * @param w1
     * @param w2
     * @return
     */
    private boolean matches(WrapperInfo w1, WrapperInfo w2) {
        if (w1 == null || w2 == null) {
            return false;
        }
        if (!w1.getWrapperElement().equals(w2.getWrapperElement())) {
            return false;
        }

        // Compare the child elements
        List<ElementInfo> list1 = w1.getChildElements();
        List<ElementInfo> list2 = w2.getChildElements();
        if (list1.size() != list2.size()) {
            return false;
        }
        // FXIME: [rfeng] At this point, the J2W generates local elments under the namespace
        // of the interface instead of "". We only compare the local parts only to work around
        // the namespace mismatch
        for (int i = 0; i < list1.size(); i++) {
            String n1 = list1.get(i).getQName().getLocalPart();
            String n2 = list2.get(i).getQName().getLocalPart();
            // TUSCANY-3298: In the following situation:
            //  1. The child is a java.util.Map type
            //  2. The child's name is a Java keyword (e.g., return)
            //  3. Tuscany is using a generated JAXB wrapper class for WSDL generation
            // the Java to WSDL generation process results in the WSDL element name
            // having a leading underscore added to the actual element name.  This is
            // because of a known JAXB issue that prevents the @XmlElement annotation
            // being used on a java.util.Map type property field in the wrapper bean
            // (see https://jaxb.dev.java.net/issues/show_bug.cgi?id=268).
            // To prevent the compatibility match from failing in this situation,
            // we strip any leading underscore before doing the comparison.
            if (!stripLeadingUnderscore(n1).equals(stripLeadingUnderscore(n2))) {
                return false;
            }
        }
        return true;
    }

    private static String stripLeadingUnderscore(String name) {
        return name.startsWith("_") ? name.substring(1) : name;
    }
    
    @SuppressWarnings("unchecked")
    public Object[] transform(Object[] source, TransformationContext context) {
        // Check if the source operation is wrapped
        DataType<List<DataType>> sourceType = context.getSourceDataType();
        Operation sourceOp = context.getSourceOperation();
        boolean sourceWrapped = sourceOp != null && sourceOp.isInputWrapperStyle() && sourceOp.getInputWrapper() != null;
        boolean sourceNotSubjectToWrapping = sourceOp != null && sourceOp.isNotSubjectToWrapping();

        // Find the wrapper handler for source data
        WrapperHandler sourceWrapperHandler = null;
        String sourceDataBinding = getDataBinding(sourceOp);
        sourceWrapperHandler = getWrapperHandler(sourceDataBinding, sourceWrapped);

        // Check if the target operation is wrapped
        DataType<List<DataType>> targetType = context.getTargetDataType();
        Operation targetOp = (Operation)context.getTargetOperation();
        boolean targetWrapped = targetOp != null && targetOp.isInputWrapperStyle() && targetOp.getInputWrapper() != null;
        boolean targetNotSubjectToWrapping = targetOp != null && targetOp.isNotSubjectToWrapping();

        // Find the wrapper handler for target data
        WrapperHandler targetWrapperHandler = null;
        String targetDataBinding = getDataBinding(targetOp);
        targetWrapperHandler = getWrapperHandler(targetDataBinding, targetWrapped);

        if ((!sourceWrapped && !sourceNotSubjectToWrapping) && targetWrapped) {
            // Unwrapped --> Wrapped
            WrapperInfo wrapper = targetOp.getInputWrapper();
            // ElementInfo wrapperElement = wrapper.getInputWrapperElement();

            // Class<?> targetWrapperClass = wrapper != null ? wrapper.getInputWrapperClass() : null;

            if (source == null) {
                // Empty child elements
                Object targetWrapper = targetWrapperHandler.create(targetOp, true);
                return new Object[] {targetWrapper};
            }

            // If the source can be wrapped, wrapped it first
            if (sourceWrapperHandler != null) {
                WrapperInfo sourceWrapperInfo = sourceOp.getInputWrapper();
                DataType sourceWrapperType = sourceWrapperInfo != null ? sourceWrapperInfo.getWrapperType() : null;

                // We only do wrapper to wrapper transformation if the source has a wrapper and both sides
                // match by XML structure
                if (sourceWrapperType != null && matches(sourceOp.getInputWrapper(), targetOp.getInputWrapper())) {
                    Class<?> sourceWrapperClass = sourceWrapperType.getPhysical();

                    // Create the source wrapper
                    Object sourceWrapper = sourceWrapperHandler.create(sourceOp, true);

                    // Populate the source wrapper
                    if (sourceWrapper != null) {
                        sourceWrapperHandler.setChildren(sourceWrapper,
                                                         source,
                                                         sourceOp,
                                                         true);

                        // Transform the data from source wrapper to target wrapper
                        Object targetWrapper =
                            mediator.mediate(sourceWrapper, sourceWrapperType, targetType.getLogical().get(0), context
                                .getMetadata());
                        return new Object[] {targetWrapper};
                    }
                }
            }
            // Fall back to child by child transformation
            Object targetWrapper = targetWrapperHandler.create(targetOp, true);
            List<DataType> argTypes = wrapper.getUnwrappedType().getLogical();
            Object[] targetChildren = new Object[source.length];
            for (int i = 0; i < source.length; i++) {
                // ElementInfo argElement = wrapper.getInputChildElements().get(i);
                DataType<XMLType> argType = argTypes.get(i);
                targetChildren[i] =
                    mediator.mediate(source[i], sourceType.getLogical().get(i), argType, context.getMetadata());
            }
            targetWrapperHandler.setChildren(targetWrapper,
                                             targetChildren,
                                             targetOp,
                                             true);
            return new Object[] {targetWrapper};

        } else if (sourceWrapped && (!targetWrapped && !targetNotSubjectToWrapping)) {
            // Wrapped to Unwrapped
            Object sourceWrapper = source[0];
            Object[] target = null;

            // List<ElementInfo> childElements = sourceOp.getWrapper().getInputChildElements();
            if (targetWrapperHandler != null) {
                // ElementInfo wrapperElement = sourceOp.getWrapper().getInputWrapperElement();
                // FIXME: This is a workaround for the wsdless support as it passes in child elements
                // under the wrapper that only matches by position
                if (sourceWrapperHandler.isInstance(sourceWrapper, sourceOp, true)) {

                    WrapperInfo targetWrapperInfo = targetOp.getInputWrapper();
                    DataType targetWrapperType =
                        targetWrapperInfo != null ? targetWrapperInfo.getWrapperType() : null;
                    if (targetWrapperType != null && matches(sourceOp.getInputWrapper(), targetOp.getInputWrapper())) {
                        Object targetWrapper =
                            mediator.mediate(sourceWrapper, sourceType.getLogical().get(0), targetWrapperType, context
                                .getMetadata());
                        target = targetWrapperHandler.getChildren(targetWrapper, targetOp, true).toArray();
                        return target;
                    }
                }
            }
            Object[] sourceChildren = sourceWrapperHandler.getChildren(sourceWrapper, sourceOp, true).toArray();
            target = new Object[sourceChildren.length];
            for (int i = 0; i < sourceChildren.length; i++) {
                DataType<XMLType> childType = sourceOp.getInputWrapper().getUnwrappedType().getLogical().get(i);
                target[i] =
                    mediator.mediate(sourceChildren[i], childType, targetType.getLogical().get(i), context
                        .getMetadata());
            }
            return target;
        } else {
            // Assuming wrapper to wrapper conversion can be handled here as well
            Object[] newArgs = new Object[source.length];
            for (int i = 0; i < source.length; i++) {
                Object child =
                    mediator.mediate(source[i], sourceType.getLogical().get(i), targetType.getLogical().get(i), context
                        .getMetadata());
                newArgs[i] = child;
            }
            return newArgs;
        }
    }

    private WrapperHandler getWrapperHandler(String dataBindingId, boolean required) {
        WrapperHandler wrapperHandler = null;
        if (dataBindingId != null) {
            DataBinding dataBinding = mediator.getDataBindings().getDataBinding(dataBindingId);
            wrapperHandler = dataBinding == null ? null : dataBinding.getWrapperHandler();
        }
        if (wrapperHandler == null && required) {
            throw new TransformationException("No wrapper handler is provided for databinding: " + dataBindingId);
        }
        return wrapperHandler;
    }

    private String getDataBinding(Operation operation) {
        WrapperInfo wrapper = operation.getInputWrapper();
        if (wrapper != null) {
            return wrapper.getDataBinding();
        } else {
            return null;
        }
    }

}
