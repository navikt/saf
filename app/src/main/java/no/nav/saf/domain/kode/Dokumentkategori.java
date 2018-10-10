package no.nav.saf.domain.kode;

import io.leangen.graphql.annotations.GraphQLEnumValue;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public enum Dokumentkategori {
	@GraphQLEnumValue(description = "Utgående brev")
	BREV,
	@GraphQLEnumValue(description = "Inngående søknad")
	SOKNAD,
	@GraphQLEnumValue(description = "Utgående klage og ankebrev.")
	KLAGE_ANKE
}
