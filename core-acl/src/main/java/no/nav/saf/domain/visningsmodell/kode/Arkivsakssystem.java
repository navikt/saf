package no.nav.saf.domain.visningsmodell.kode;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import io.leangen.graphql.annotations.types.GraphQLType;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@GraphQLType(description = "Arkivsakssystem")
public enum Arkivsakssystem {
	@GraphQLEnumValue(description = "GSAK")
	GSAK,
	@GraphQLEnumValue(description = "PSAK")
	PSAK
}
