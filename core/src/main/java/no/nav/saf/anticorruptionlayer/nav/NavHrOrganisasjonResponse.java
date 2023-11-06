package no.nav.saf.anticorruptionlayer.nav;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public record NavHrOrganisasjonResponse(
		@JsonProperty("ER_NAV_ORGNUMMER") ErNavOrgnummer erNavOrgnummer) {

	public record ErNavOrgnummer(@JsonProperty("ORGNUMMER") String orgnummer,
								 @JsonProperty("ER_NAV_ORGANISASJON") @JsonDeserialize(using = JaNeiBooleanDeserializer.class) boolean erNavOrganisasjon) {
	}

	public static NavHrOrganisasjonResponse ja(String organisasjonsnummer) {
		return new NavHrOrganisasjonResponse(new ErNavOrgnummer(organisasjonsnummer, true));
	}

	public static NavHrOrganisasjonResponse nei(String organisasjonsnummer) {
		return new NavHrOrganisasjonResponse(new ErNavOrgnummer(organisasjonsnummer, false));
	}
}
