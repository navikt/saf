package no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode;

import io.leangen.graphql.annotations.GraphQLEnumValue;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public enum AvsenderType {
	@GraphQLEnumValue(description = "Person.")
	PERSON,
	@GraphQLEnumValue(description = "Juridisk person.")
	ORGANISASJON,
	@GraphQLEnumValue(description = "Lege eller behandler.")
	SAMHANDLER
}
