package no.nav.saf.endpoints;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.endpoints.wiring.DokumentoversiktWiring;
import no.nav.saf.exceptions.OidcTokenNotFoundException;
import no.nav.saf.tilgangskontroll.NavBrukertype;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GraphQL endepunktet til applikasjonen.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RestController
@Slf4j
public class GraphQLController {

	private final GraphQLSchema graphQLSchema;

	public GraphQLController(DokumentoversiktWiring dokumentoversiktWiring) {
		SchemaParser schemaParser = new SchemaParser();
		InputStreamReader schema = new InputStreamReader(ClassLoader.getSystemResourceAsStream("schemas/saf.graphql"));

		TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
		SchemaGenerator schemaGenerator = new SchemaGenerator();
		graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, dokumentoversiktWiring.createRuntimeWiring());
	}

	@PostMapping(value = "/graphql", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public Map<String, Object> graphQLRequest(@RequestBody GraphQLRequest request, @RequestHeader HttpHeaders httpHeaders) {

		String oidcToken = getIdToken(httpHeaders);
		if (oidcToken == null) {
			throw new OidcTokenNotFoundException("Authorization field did not contain oidc token.", HttpStatus.UNAUTHORIZED);
		}

		try {
			JWT.decode(oidcToken).getPayload();
		} catch (JWTDecodeException e) {
			throw new OidcTokenNotFoundException(e.getMessage(), HttpStatus.UNAUTHORIZED);
		}

		ExecutionResult executionResult = GraphQL.newGraphQL(graphQLSchema)
				.build()
				.execute(ExecutionInput.newExecutionInput()
						.query(request.getQuery())
						.operationName(request.getOperationName())
						.variables(request.getVariables())
						.context(SafRequestContext.builder().oidcToken(oidcToken).navBrukertype(NavBrukertype.SAKSBEHANDLER).build())
						.build());

		return executionResult.toSpecification();

	}

	private String getIdToken(HttpHeaders httpHeaders) {
		return Optional.ofNullable(getAuthorizationList(httpHeaders)).filter(e -> {
			return e.get(0).startsWith("Bearer ");
		}).map(e -> {
			return e.get(0).replaceFirst("Bearer ", "");
		}).orElse(null);
	}

	private List<String> getAuthorizationList(HttpHeaders httpHeaders) throws OidcTokenNotFoundException {
		return Optional.ofNullable(httpHeaders.get("Authorization"))
				.orElseThrow(() -> new OidcTokenNotFoundException("No Authorization field was found in GraphQLRequest.", HttpStatus.UNAUTHORIZED));
	}

}
