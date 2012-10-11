package cn.edu.nju.moon.conup.buffer;

import java.util.ArrayList;
import java.util.List;

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


public class BufferPolicyBuilder implements PolicyBuilder<BufferPolicy> {

	public QName getPolicyType() {
		return BufferPolicy.BUFFER_POLICY_QNAME;
	}

	public List<QName> getSupportedBindings() {
		return null;
	}

	public boolean build(Endpoint endpoint, BuilderContext context) {
		System.out.println("BufferPolicyBuilder.build(...)");
        List<BufferPolicy> polices = getPolicies(endpoint);
        System.out.println(endpoint + ": " + polices);
        return true;
	}

	public boolean build(EndpointReference endpointReference,
			BuilderContext context) {
		System.out.println("BufferPolicyBuilder.build(...)");
        List<BufferPolicy> polices = getPolicies(endpointReference);
        System.out.println(endpointReference + ": " + polices);
        return true;
	}

	public boolean build(Component component, Implementation implementation,
			BuilderContext context) {
		System.out.println("BufferPolicyBuilder.build(...)");
        List<BufferPolicy> polices = getPolicies(implementation);
        System.out.println(implementation + ": " + polices);
        return true;
	}

	public boolean build(EndpointReference endpointReference,
			Endpoint endpoint, BuilderContext context) {
		System.out.println("BufferPolicyBuilder.build(...)");
		return true;
	}
	
    private List<BufferPolicy> getPolicies(PolicySubject subject) {
    	System.out.println("BufferPolicyBuilder.getPolicies(...)");
        List<BufferPolicy> polices = new ArrayList<BufferPolicy>();
        for (PolicySet ps : subject.getPolicySets()) {
            for (PolicyExpression exp : ps.getPolicies()) {
                if (getPolicyType().equals(exp.getName())) {
                    polices.add((BufferPolicy)exp.getPolicy());
                }
            }
        }
        return polices;
    }

}
