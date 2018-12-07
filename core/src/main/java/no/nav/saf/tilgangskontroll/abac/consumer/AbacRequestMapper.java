package no.nav.saf.tilgangskontroll.abac.consumer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.exception.JsonMarshallingException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class AbacRequestMapper {

	String map(XacmlRequest request) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try (JsonGenerator generator = new JsonFactory().createGenerator(stream)) {

			generator.writeStartObject();
			generator.writeFieldName("Request");
			generator.writeStartObject();

			writeXacmlAttributes(request.getResources(), "Resource", generator);
			writeXacmlAttributes(request.getAccessSubjects(), "AccessSubject", generator);
			writeXacmlAttributes(request.getActions(), "Action", generator);
			writeXacmlAttributes(request.getEnvironments(), "Environment", generator);

			generator.writeEndObject();
			generator.writeEndObject();
			generator.close();

			return new String(stream.toByteArray());
		} catch (IOException e) {
			throw new JsonMarshallingException("Failed to write request as Json", e);
		}
	}

	private void writeXacmlAttributes(List<XacmlAttribute> attributes, String group, JsonGenerator generator) throws IOException {
		if (attributes != null && !attributes.isEmpty()) {
			generator.writeFieldName(group);
			generator.writeStartObject();
			generator.writeArrayFieldStart("Attribute");

			for (XacmlAttribute attribute : attributes) {
				generator.writeStartObject();
				generator.writeObjectField("AttributeId", attribute.getAttributeId());
				generator.writeObjectField("Value", attribute.getValue());
				generator.writeEndObject();
			}

			generator.writeEndArray();
			generator.writeEndObject();
		}
	}
}