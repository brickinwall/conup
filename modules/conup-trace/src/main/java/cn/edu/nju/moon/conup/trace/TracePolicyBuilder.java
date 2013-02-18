package cn.edu.nju.moon.conup.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.builder.BuilderContext;
import org.apache.tuscany.sca.assembly.builder.PolicyBuilder;
import org.apache.tuscany.sca.policy.PolicyExpression;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySubject;

import cn.edu.nju.moon.conup.spi.utils.DepRecorder;


public class TracePolicyBuilder implements PolicyBuilder<TracePolicy> {
	private static Logger LOGGER = Logger.getLogger(TracePolicyBuilder.class.getName());
	public QName getPolicyType() {
		return TracePolicy.TRACE_POLICY_QNAME;
	}

	public List<QName> getSupportedBindings() {
		return null;
	}

	public boolean build(Endpoint endpoint, BuilderContext context) {
		LOGGER.fine("TracePolicyBuilder.build(...)");
        List<TracePolicy> polices = getPolicies(endpoint);
        LOGGER.fine(endpoint + ": " + polices);
        return true;
	}

	public boolean build(EndpointReference endpointReference,
			BuilderContext context) {
		LOGGER.fine("TracePolicyBuilder.build(...)");
        List<TracePolicy> polices = getPolicies(endpointReference);
        LOGGER.fine(endpointReference + ": " + polices);
        return true;
	}

	public boolean build(Component component, Implementation implementation,
			BuilderContext context) {
		LOGGER.fine("TracePolicyBuilder.build(...)");
        List<TracePolicy> polices = getPolicies(implementation);
        LOGGER.fine(implementation + ": " + polices);
        return true;
	}

	public boolean build(EndpointReference endpointReference,
			Endpoint endpoint, BuilderContext context) {
		LOGGER.fine("TracePolicyBuilder.build(...)");
		return true;
	}
	
    private List<TracePolicy> getPolicies(PolicySubject subject) {
    	LOGGER.fine("TracePolicyBuilder.getPolicies(...)");
        List<TracePolicy> polices = new ArrayList<TracePolicy>();
        for (PolicySet ps : subject.getPolicySets()) {
            for (PolicyExpression exp : ps.getPolicies()) {
                if (getPolicyType().equals(exp.getName())) {
                    polices.add((TracePolicy)exp.getPolicy());
                }
            }
        }
        return polices;
    }

}
