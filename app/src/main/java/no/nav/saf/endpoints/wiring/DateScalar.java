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
final class DateScalar {
	static final GraphQLScalarType DATE = GraphQLScalarType.newScalar()
			.name("Date")
			.description("Identifikasjon av et døgn i kalenderen etter ISO-8601 standarden.")
			.coercing(new Coercing() {
				@Override
				public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
					if (dataFetcherResult instanceof LocalDate) {
						return dataFetcherResult.toString();
					}
					throw new CoercingSerializeException("Serialisering av " + dataFetcherResult.getClass() + " til " + DATE.getName() + " er ikke implementert.");
				}

				@Override
				public Object parseValue(Object input) throws CoercingParseValueException {
					return createLocalDateFromString(input.toString());
				}

				@Override
				public Object parseLiteral(Object input) throws CoercingParseLiteralException {
					if (input instanceof StringValue) {
						return createLocalDateFromString(((StringValue) input).getValue());
					}
					throw new CoercingParseLiteralException("Verdi er ikke en gyldig Date: " + input.toString());
				}
			})
			.build();

	private static LocalDate createLocalDateFromString(String value) {
		try {
			return LocalDate.parse(value);
		} catch (DateTimeParseException e) {
			throw new CoercingParseLiteralException("Verdi er ikke en gyldig Date: " + value);
		}

	}

	private DateScalar() {
		// ingen instansiering
	}
}
