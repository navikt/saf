package no.nav.saf.sakstilknyttedejournalposter;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.metadata.strategy.query.AnnotatedResolverBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RestController
@Slf4j
public class GraphQLController {

	private final GraphQL graphQL;

	@Inject
	public GraphQLController(SakstilknyttedeJournalposter brukerQuery, TemaQuery temaQuery, SakQuery sakQuery, JournalpostQuery journalpostQuery) {
		//Schema generated from query classes
		GraphQLSchemaGenerator schemaGenerator = new GraphQLSchemaGenerator()
				.withResolverBuilders(new AnnotatedResolverBuilder());

		schemaGenerator = schemaGenerator
				.withOperationsFromSingleton(brukerQuery)
				.withOperationsFromSingleton(temaQuery)
				.withOperationsFromSingleton(sakQuery)
				.withOperationsFromSingleton(journalpostQuery);

		GraphQLSchema schema = schemaGenerator.generate();
		graphQL = GraphQL.newGraphQL(schema)
				.build();
	}

	@PostMapping(value = "/graphql", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public Map<String, Object> graphQLRequest(@RequestBody GraphQLRequest request, HttpServletRequest raw) {
		ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
				.query(request.getQuery())
				.operationName(request.getOperationName())
				.variables(request.getVariables())
				.context(raw)
				.build());
		return executionResult.toSpecification();
	}
}
