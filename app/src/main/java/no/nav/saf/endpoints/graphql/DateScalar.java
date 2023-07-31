package no.nav.saf.endpoints.graphql;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

final class DateScalar {
	static final GraphQLScalarType DATE = GraphQLScalarType.newScalar()
			.name("Date")
			.description("Identifikasjon av et døgn i kalenderen etter ISO-8601 standarden.")
			.coercing(new Coercing<>() {
				@Override
				public @Nullable Object serialize(@NotNull Object dataFetcherResult, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingSerializeException {
					if (dataFetcherResult instanceof LocalDate) {
						return dataFetcherResult.toString();
					}
					throw new CoercingSerializeException("Serialisering av " + dataFetcherResult.getClass() + " til " + DATE.getName() + " er ikke implementert.");
				}

				@Override
				public @Nullable Object parseValue(@NotNull Object input, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseValueException {
					return createLocalDateFromString(input.toString());
				}

				@Override
				public @Nullable Object parseLiteral(@NotNull Value<?> input, @NotNull CoercedVariables variables, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseLiteralException {
					if (input instanceof StringValue inputString) {
						return createLocalDateFromString(inputString.getValue());
					}
					throw new CoercingParseLiteralException("Verdi er ikke en gyldig Date: " + input);
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
