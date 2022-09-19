package no.nav.saf.anticorruptionlayer.pensjonsak.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SakSammendrag  {

	public final String sakId;
	public final String sakstype;
	public final String arkivtema;
	public final String saksstatus;
	private final String fomDato;
	private final String tomDato;
	public final String enhetId;

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
		this.sakId = sakId;
		this.sakstype = sakstype;
		this.saksstatus = saksstatus;
		this.arkivtema = arkivtema;
		this.fomDato = fomDato;
		this.tomDato = tomDato;
		this.enhetId = enhetId;
	}

	public Periode saksperiode() {
		return Periode.of(fomDato, tomDato);
	}
}