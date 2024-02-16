package no.nav.saf.endpoints.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.AsyncSerialExecutionStrategy;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.graphql.GraphQLExceptionHandler;
import no.nav.saf.metrics.AudienceCounter;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static no.nav.saf.endpoints.HeaderUtils.createNavCallid;
import static no.nav.saf.headers.NavHeaders.NAV_CALLID;
import static no.nav.saf.headers.NavHeaders.NAV_USER_ID;
import static no.nav.saf.headers.NavHeaders.X_CORRELATION_ID;
import static no.nav.saf.tilgangskontroll.SafRequestContext.KEY;
import static no.nav.saf.util.MDCUtility.addMdcData;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Hidden
@RestController
@Slf4j
@Protected
public class GraphQLController {
	private final GraphQLSchema graphQLSchema;
	private final GraphQLExceptionHandler graphQLExceptionHandler;
	private final TokenValidationContextHolder tokenValidationContextHolder;
	private final AudienceCounter audienceCounter;
	private final Map<String, Boolean> privilegiedServiceusers;

	public GraphQLController(@Qualifier("privilegiedServiceusers") Map<String, Boolean> privilegiedServiceusers,
							 GraphQLWiring graphQLWiring,
							 GraphQLExceptionHandler graphQLExceptionHandler,
							 AudienceCounter audienceCounter,
							 TokenValidationContextHolder tokenValidationContextHolder) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
		this.graphQLExceptionHandler = graphQLExceptionHandler;
		SchemaParser schemaParser = new SchemaParser();
		InputStreamReader schema = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("schemas/saf.graphqls")));

		TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
		SchemaGenerator schemaGenerator = new SchemaGenerator();
		this.graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, graphQLWiring.createRuntimeWiring());
		this.audienceCounter = audienceCounter;
		this.privilegiedServiceusers = privilegiedServiceusers;
	}

	@PostMapping(value = {"/graphql", "/graphql/"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public Map<String, Object> graphQLRequest(@RequestHeader(value = X_CORRELATION_ID, required = false) String xCorrelationId,
											  @RequestHeader(value = NAV_CALLID, required = false) String navCallid,
											  @RequestHeader(value = NAV_USER_ID, required = false) String navUserId,
											  @RequestBody GraphQLRequest request) {
		final SafRequestContext safRequestContext = new SafRequestContext(
				createNavCallid(navCallid, xCorrelationId),
				navUserId,
				tokenValidationContextHolder.getTokenValidationContext(),
				privilegiedServiceusers
		);
		addMdcData(safRequestContext);

		audienceCounter.increment(
				safRequestContext.getSecurityContext().getIssuer(),
				safRequestContext.getSecurityContext().getAudience()
		);
		ExecutionResult executionResult =
				GraphQL.newGraphQL(graphQLSchema)
						.mutationExecutionStrategy(new AsyncSerialExecutionStrategy(graphQLExceptionHandler))
						.queryExecutionStrategy(new AsyncExecutionStrategy(graphQLExceptionHandler))
						.build().execute(ExecutionInput.newExecutionInput()
								.query(request.getQuery())
								.operationName(request.getOperationName())
								.variables(request.getVariables() == null ? Collections.emptyMap() : request.getVariables())
								.graphQLContext((c) -> c.put(KEY, safRequestContext))
								.build());
		return executionResult.toSpecification();
	}
}
