package no.nav.saf.domain;

import lombok.Builder;
import lombok.Data;
import no.nav.saf.domain.kode.SakType;
import no.nav.saf.domain.kode.Sakssystem;
import no.nav.saf.domain.kode.Temakode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class Sak {
	private final SakType sakType;
	private final String saksreferanse;
	private final Sakssystem sakssystem;
	private final String fagsaksreferanse;
	private final Fagsystem fagsystem;
	private final Temakode tema;
	private final LocalDateTime datoOpprettet;

	@Builder.Default
	private final List<Journalpost> journalposter = new ArrayList<>();
}
