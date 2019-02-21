package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Arkivsakssystem;

import java.time.LocalDateTime;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Sak {
	private final String arkivsaksnummer;
	private final Arkivsakssystem arkivsaksystem;
	private final String fagsakId;
	private final String fagsaksystem;
	private final LocalDateTime datoOpprettet;
}
