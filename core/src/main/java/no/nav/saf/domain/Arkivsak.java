package no.nav.saf.domain;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;

import java.time.LocalDateTime;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Arkivsak {
	private final String aktoerId;
	private final String orgnummer;
	private final String arkivsaksnummer;
	private final Arkivsakssystem arkivsaksystem;
	private final String fagsakId;
	private final String fagsaksystem;
	private final Tema tema;
	private final LocalDateTime datoOpprettet;

	public String getKey() {
		return arkivsaksnummer + arkivsaksystem;
	}

	public boolean isBrukerInfoMissing(){
		return aktoerId == null && orgnummer == null;
	}

	public boolean isBrukerPerson() {
		return orgnummer == null;
	}
}
