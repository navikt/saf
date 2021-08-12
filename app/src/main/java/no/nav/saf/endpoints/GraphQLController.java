package no.nav.saf.endpoints;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.AsyncSerialExecutionStrategy;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.azure.SafProperties;
import no.nav.saf.endpoints.wiring.DokumentoversiktWiring;
import no.nav.saf.exceptionhandler.GraphQLExceptionHandler;
import no.nav.saf.metrics.AudienceCounter;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static no.nav.saf.headers.NavHeaders.NAV_CALLID;
import static no.nav.saf.headers.NavHeaders.NAV_CONSUMER_ID;
import static no.nav.saf.headers.NavHeaders.X_CORRELATION_ID;

/**
 * GraphQL endepunktet til applikasjonen.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RestController
@Slf4j
@Protected
public class GraphQLController extends AbstractSafController {
	private final GraphQLSchema graphQLSchema;
	private final GraphQLExceptionHandler graphQLExceptionHandler;
	private final Set<String> azureIssuers;
	private final TokenValidationContextHolder tokenValidationContextHolder;
	private final AudienceCounter audienceCounter;
	private final Map<String, Boolean> privilegiedServiceusers;

	@Inject
	public GraphQLController(@Named("azureIssuers") Set<String> azureIssuers,
							 @Named("privilegiedServiceusers") Map<String, Boolean> privilegiedServiceusers,
							 DokumentoversiktWiring dokumentoversiktWiring,
							 GraphQLExceptionHandler graphQLExceptionHandler,
							 AudienceCounter audienceCounter,
							 TokenValidationContextHolder tokenValidationContextHolder,
							 SafProperties safProperties) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
		this.graphQLExceptionHandler = graphQLExceptionHandler;
		this.azureIssuers = azureIssuers;
		SchemaParser schemaParser = new SchemaParser();
		InputStreamReader schema = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("schemas/saf.graphqls")));

		TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
		SchemaGenerator schemaGenerator = new SchemaGenerator();
		this.graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, dokumentoversiktWiring.createRuntimeWiring());
		this.audienceCounter = audienceCounter;
		this.privilegiedServiceusers = privilegiedServiceusers;
    }

	@PostMapping(value = "/graphql", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> graphQLRequest(@RequestHeader(value = X_CORRELATION_ID, required = false) String xCorrelationId,
											  @RequestHeader(value = NAV_CALLID, required = false) String navCallid,
											  @RequestHeader(value = NAV_CONSUMER_ID, required = false) String navConsumerId,
											  @RequestBody GraphQLRequest request) {

		SafRequestContext safRequestContext = new SafRequestContext(
				this.azureIssuers,
				createNavCallid(navCallid, xCorrelationId),
				navConsumerId,
				tokenValidationContextHolder.getTokenValidationContext(),
				privilegiedServiceusers
		);

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
						.context(safRequestContext)
						.build());

		return executionResult.toSpecification();
	}
}
