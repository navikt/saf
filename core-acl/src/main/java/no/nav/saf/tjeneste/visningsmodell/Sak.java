package no.nav.saf.tjeneste.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;

import java.time.OffsetDateTime;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Sak {
	private final String arkivsaksnummer;
	private final Arkivsakssystem arkivsakssystem;
	private final String fagsaksnummer;
	private final String fagsystem;
	private final OffsetDateTime datoOpprettet;
	private final Temakode temakode;
}
