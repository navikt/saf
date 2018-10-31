package no.nav.saf.endpoints.wiring;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class DateScalar {
	static final GraphQLScalarType DATE = new GraphQLScalarType("Date", "Identifikasjon av et døgn i kalenderen etter ISO-8601 standarden.", new Coercing() {
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
	});

	private static Object serializeDato(Object datafetcherResult) {
		if(datafetcherResult instanceof LocalDate) {
			return datafetcherResult.toString();
		}
		throw new CoercingSerializeException("Serialisering av " + datafetcherResult.getClass() + " til " + DATE.getName() + " er ikke implementert.");
	}

	private static Object parseDatoFromValue(Object input) {
		throw new CoercingParseValueException("Parsing av query variabel " + input.getClass() + " til " + DATE.getName() + " er ikke implementert.");
	}

	private static Object parseDatoFromAstLiteral(Object input) {
		if(input instanceof StringValue) {
			try {
				return LocalDate.parse(((StringValue) input).getValue());
			} catch(DateTimeParseException e) {
				throw new CoercingParseLiteralException("Verdi er ikke en gyldig Date: " + input.toString());
			}
		}
		throw new CoercingParseLiteralException("Verdi er ikke en gyldig Date: " + input.toString());
	}
}
