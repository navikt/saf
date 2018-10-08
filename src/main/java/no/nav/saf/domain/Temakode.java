package no.nav.saf.domain;

import io.leangen.graphql.annotations.GraphQLEnumValue;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public enum Temakode {
	@GraphQLEnumValue(description = "Bidrag")
	BID,
	@GraphQLEnumValue(description = "Dagpenger")
	DAG,
	@GraphQLEnumValue(description = "Pensjon")
	PEN,
	@GraphQLEnumValue(description = "Foreldrepenger og engangsstønad")
	FOR
}
