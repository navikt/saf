package no.nav.saf.endpoints;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.NavBrukertype;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.dokumentoversikt.DokumentoversiktDomainCoordinator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStreamReader;
import java.util.Map;

/**
 * GraphQL endepunktet til applikasjonen.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RestController
@Slf4j
public class GraphQLController {

	private final GraphQLSchema graphQLSchema;

	public GraphQLController(DokumentoversiktDomainCoordinator dokumentoversiktDomainCoordinator) {
		SchemaParser schemaParser = new SchemaParser();
		InputStreamReader schema = new InputStreamReader(ClassLoader.getSystemResourceAsStream("schemas/saf.graphql"));
		RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
				.type("Query", typeWiring -> typeWiring.dataFetcher("dokumentOversiktFor", environment -> {
					final String aktoerId = environment.getArgument("aktoerId");
					final SafRequestContext safRequestContext = environment.getContext();
					safRequestContext.setAktoerId(aktoerId);
					return dokumentoversiktDomainCoordinator.findBrukerByAktoerId(aktoerId, safRequestContext);
				}))
				.type("Bruker", typeWiring -> typeWiring.dataFetcher("journalposter", environment -> {
					final SafRequestContext safRequestContext = environment.getContext();
					return dokumentoversiktDomainCoordinator.findJournalposterByAktoerId(safRequestContext.getAktoerId(), safRequestContext);
				}))
				.type("Journalpost", typeWiring -> typeWiring.dataFetcher("dokumenter", environment -> {
					return null; //TODO
				}))
				.build();
		TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
		SchemaGenerator schemaGenerator = new SchemaGenerator();
		graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
	}

	@PostMapping(value = "/graphql", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public Map<String, Object> graphQLRequest(@RequestBody GraphQLRequest request) {
		ExecutionResult executionResult = GraphQL.newGraphQL(graphQLSchema)
				.build()
				.execute(ExecutionInput.newExecutionInput()
						.query(request.getQuery())
						.operationName(request.getOperationName())
						.variables(request.getVariables())
						.context(SafRequestContext.builder().oidcToken("TODO").navBrukertype(NavBrukertype.SAKSBEHANDLER).build())
						.build());
		return executionResult.toSpecification();
	}
}
