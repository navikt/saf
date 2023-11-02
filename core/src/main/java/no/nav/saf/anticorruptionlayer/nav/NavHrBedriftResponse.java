package no.nav.saf.anticorruptionlayer.nav;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public record NavHrBedriftResponse(@JsonProperty("HR_NAV_BEDRIFT") HrNavBedrift hrNavBedrift) {

	public record HrNavBedrift(@JsonProperty("BEDRIFTNR") String bedriftnr,
							   @JsonProperty("ER_NAV_BEDRIFT") @JsonDeserialize(using = JaNeiBooleanDeserializer.class) boolean erNavBedrift) {
	}

	public static NavHrBedriftResponse ja(String organisasjonsnummer) {
		return new NavHrBedriftResponse(new HrNavBedrift(organisasjonsnummer, true));
	}

	public static NavHrBedriftResponse nei(String organisasjonsnummer) {
		return new NavHrBedriftResponse(new HrNavBedrift(organisasjonsnummer, false));
	}
}
