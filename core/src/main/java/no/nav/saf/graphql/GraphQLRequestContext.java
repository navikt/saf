package no.nav.saf.graphql;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import no.nav.security.token.support.core.context.TokenValidationContext;

@Value
@Builder
public class GraphQLRequestContext {
	private final String navCallId;
	@ToString.Exclude
	private final TokenValidationContext tokenValidationContext;
}
