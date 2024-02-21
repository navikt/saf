package no.nav.saf.endpoints.graphql;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GraphQLResponse {
	private Map<String, Object> data;
	private List<Error> errors;

	@Data
	public static class Error {
		private String message;
		private Extensions extensions;
	}

	@Data
	public static class Extensions {
		private String code;
		@JsonProperty("reason_code")
		private String reasonCode;
		@JsonProperty("reason_message")
		private String reasonMessage;
	}
}
