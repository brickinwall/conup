package cn.edu.nju.moon.conup.trace;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Base;

public class TracePolicy {
    static final String SCA11_NS = Base.SCA11_NS;
//    static final String SCA11_TUSCANY_NS = Base.SCA11_TUSCANY_NS;
    static final QName TRACE_POLICY_QNAME = new QName(SCA11_NS, "trace");
    
    
}
