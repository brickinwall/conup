package cn.edu.nju.moon.conup.trace;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;

public class TracePolicyProcessor implements StAXArtifactProcessor<TracePolicy> {
	static final QName TRACE_POLICY_QNAME = new QName(TracePolicy.SCA11_NS, "trace");

	public void resolve(TracePolicy model, ModelResolver resolver,
			ProcessorContext context) throws ContributionResolveException {
		// TODO Auto-generated method stub

	}

	public Class<TracePolicy> getModelType() {
		return TracePolicy.class;
	}

	public TracePolicy read(XMLStreamReader reader, ProcessorContext context)
			throws ContributionReadException, XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

	public void write(TracePolicy model, XMLStreamWriter writer,
			ProcessorContext context) throws ContributionWriteException,
			XMLStreamException {
		// TODO Auto-generated method stub

	}

	public QName getArtifactType() {
		return TRACE_POLICY_QNAME;
	}

}
