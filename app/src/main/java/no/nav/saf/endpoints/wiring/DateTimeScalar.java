package no.nav.saf.endpoints.wiring;

import static java.time.temporal.ChronoUnit.SECONDS;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.time.LocalDateTime;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class DateTimeScalar {
	static final GraphQLScalarType DATE_TIME = GraphQLScalarType.newScalar()
			.name("DateTime")
			.description("Identifikasjon av dato og tidspunkt etter ISO-8601 standarden.")
			.coercing(new Coercing() {
				@Override
				public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
					return serializeDato(dataFetcherResult);
				}

				@Override
				public Object parseValue(Object input) throws CoercingParseValueException {
					return parseDatoFromValue(input);
				}

				@Override
				public Object parseLiteral(Object input) throws CoercingParseLiteralException {
					return parseDatoFromAstLiteral(input);
				}
			})
			.build();

	private static Object serializeDato(Object datafetcherResult) {
		if (datafetcherResult instanceof LocalDateTime) {
			return ((LocalDateTime) datafetcherResult).truncatedTo(SECONDS).toString();
		}
		throw new CoercingSerializeException("Serialisering av " + datafetcherResult.getClass() + " til " + DATE_TIME.getName() + " er ikke implementert.");
	}

	private static Object parseDatoFromValue(Object input) {
		throw new CoercingParseValueException("Parsing av query variabel " + input.getClass() + " til " + DATE_TIME.getName() + " er ikke implementert.");
	}

	private static Object parseDatoFromAstLiteral(Object input) {
		throw new CoercingParseLiteralException("Parsing av literal " + input.getClass() + " til " + DATE_TIME.getName() + " er ikke implementert.");
	}
}
