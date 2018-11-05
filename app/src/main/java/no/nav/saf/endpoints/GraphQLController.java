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
import no.nav.saf.exceptions.OidcAuthentificationException;
import no.nav.saf.tilgangskontroll.NavBrukertype;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.http.HttpHeaders;
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

	private static final String TOKEN_TYPE = "Bearer";
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
		String oidcToken = Optional.ofNullable(getAuthorizationList(httpHeaders))
				.filter(e -> e.startsWith(TOKEN_TYPE + " "))
				.map(e -> e.replaceFirst(TOKEN_TYPE + " ", ""))
				.orElseThrow(() -> new OidcAuthentificationException("Autorization sitt OIDC token mangler token_type " + TOKEN_TYPE + " foran oidc token."));

		try {
			return JWT.decode(oidcToken).getPayload();
		} catch (JWTDecodeException e) {
			throw new OidcAuthentificationException("Dekoding av oidcToken feilet, " + e.getMessage());
		}
	}

	private String getAuthorizationList(HttpHeaders httpHeaders) {

		List<String> authorization = Optional.ofNullable(
				httpHeaders.get(HttpHeaders.AUTHORIZATION))
				.orElseThrow(() -> new OidcAuthentificationException("GraphQLRequest inneholder ikke et Autorization felt."));

		if (authorization.size() != 1) {
			throw new OidcAuthentificationException("GraphQLRequest skal ha kun et Autorization felt.");
		}

		return authorization.get(0);

	}

}
