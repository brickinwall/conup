package cn.edu.nju.moon.conup.buffer;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Base;

public class BufferPolicy {
    static final String SCA11_NS = Base.SCA11_NS;
//    static final String SCA11_TUSCANY_NS = Base.SCA11_TUSCANY_NS;
    static final QName BUFFER_POLICY_QNAME = new QName(SCA11_NS, "buffer");
    
    
}
