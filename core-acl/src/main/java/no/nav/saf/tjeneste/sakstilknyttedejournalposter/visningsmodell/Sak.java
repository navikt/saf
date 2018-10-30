package no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.Temakode;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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

	@Builder.Default
	private final List<Journalpost> journalposter = new ArrayList<>();
}
