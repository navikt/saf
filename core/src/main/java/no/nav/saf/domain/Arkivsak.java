package no.nav.saf.domain;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

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
	private final String fagsaksnummer;
	private final String fagsaksystem;
	private final Tema tema;
	private final LocalDateTime datoOpprettet;

	public String getKey() {
		return arkivsaksnummer + arkivsaksystem;
	}

	public boolean isBrukerPerson() {
		return orgnummer == null;
	}
}
