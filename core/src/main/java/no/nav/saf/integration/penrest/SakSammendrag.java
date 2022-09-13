package no.nav.saf.integration.penrest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SakSammendrag  (
		String sakId,
		String sakstype,
		Arkivtema arkivtema,
		String saksstatus,
		Periode saksperiode,
		String enhetId
){
	@JsonCreator
	public SakSammendrag (
			@JsonProperty("sakId") String sakId,
			@JsonProperty("sakType") String sakstype,
			@JsonProperty("sakStatus") String saksstatus,
			@JsonProperty("fomDato") String fomDato,
			@JsonProperty("tomDato") String tomDato,
			@JsonProperty("enhetId") String enhetId
	) {
		this(sakId, sakstype, Arkivtema.hardcoded(), saksstatus, Periode.of(fomDato, tomDato), enhetId);
	}

}