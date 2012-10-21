package cn.edu.nju.moon.conup.buffer;

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

public class BufferPolicyProcessor implements StAXArtifactProcessor<BufferPolicy> {
	static final QName BUFFER_POLICY_QNAME = new QName(BufferPolicy.SCA11_NS, "buffer");

	public void resolve(BufferPolicy model, ModelResolver resolver,
			ProcessorContext context) throws ContributionResolveException {
		// TODO Auto-generated method stub

	}

	public Class<BufferPolicy> getModelType() {
		return BufferPolicy.class;
	}

	public BufferPolicy read(XMLStreamReader reader, ProcessorContext context)
			throws ContributionReadException, XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

	public void write(BufferPolicy model, XMLStreamWriter writer,
			ProcessorContext context) throws ContributionWriteException,
			XMLStreamException {
		// TODO Auto-generated method stub

	}

	public QName getArtifactType() {
		return BUFFER_POLICY_QNAME;
	}

}
