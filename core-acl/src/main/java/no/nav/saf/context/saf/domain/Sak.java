package no.nav.saf.context.saf.domain;

import lombok.Builder;
import lombok.Data;
import no.nav.saf.context.saf.domain.kode.Fagsystem;
import no.nav.saf.context.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.context.saf.domain.kode.Temakode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class Sak {
	private final String arkivsaksnummer;
	private final Arkivsakssystem arkivsakssystem;
	private final String fagsaksnummer;
	private final Fagsystem fagsystem;
	private final LocalDateTime datoOpprettet;
	private final Temakode temakode;

	@Builder.Default
	private final List<Journalpost> journalposter = new ArrayList<>();
}
