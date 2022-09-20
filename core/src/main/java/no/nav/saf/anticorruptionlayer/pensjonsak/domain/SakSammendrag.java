package no.nav.saf.anticorruptionlayer.pensjonsak.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SakSammendrag(
		String sakId,
		String sakstype,
		String saksstatus,
		String arkivtema,
		Periode saksperiode,
		String enhetId
) {


	@JsonCreator
	public SakSammendrag(
			@JsonProperty("sakId") String sakId,
			@JsonProperty("sakType") String sakstype,
			@JsonProperty("sakStatus") String saksstatus,
			@JsonProperty("arkivtema") String arkivtema,
			@JsonProperty("fomDato") String fomDato,
			@JsonProperty("tomDato") String tomDato,
			@JsonProperty("enhetId") String enhetId
	) {
		this(sakId, sakstype, saksstatus, arkivtema, Periode.of(fomDato, tomDato), enhetId);
	}
}