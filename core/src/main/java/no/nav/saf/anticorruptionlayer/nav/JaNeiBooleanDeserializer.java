package no.nav.saf.anticorruptionlayer.nav;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class JaNeiBooleanDeserializer extends StdDeserializer<Boolean> {
	static final String TRUE_JA = "ja";

	public JaNeiBooleanDeserializer() {
		super(Boolean.class);
	}

	@Override
	public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
		String value = jsonParser.readValueAs(String.class);
		return value.equalsIgnoreCase(TRUE_JA);
	}
}