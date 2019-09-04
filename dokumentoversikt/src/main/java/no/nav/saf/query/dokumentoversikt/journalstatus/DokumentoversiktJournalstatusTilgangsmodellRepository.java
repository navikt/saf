package no.nav.saf.query.dokumentoversikt.journalstatus;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoToArkivSakMapper;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class DokumentoversiktJournalstatusTilgangsmodellRepository {
	private final JournalpostDtoToArkivSakMapper journalpostDtoToArkivSakMapper;

	public DokumentoversiktJournalstatusTilgangsmodellRepository(JournalpostDtoToArkivSakMapper journalpostDtoToArkivSakMapper) {
		this.journalpostDtoToArkivSakMapper = journalpostDtoToArkivSakMapper;
	}

	public void mapOgCacheArkivsaker(final List<TilgangJournalpost> filteredTilgangJournalpostList, final SafRequestContext safRequestContext) {
		List<JournalpostDto> journalposterMedSaksrelasjon = filteredTilgangJournalpostList.stream()
				.map(tj -> (JournalpostDto)safRequestContext.getRequestCache().getObject(tj.getJournalpostId()))
				.filter(jp -> jp.getSaksrelasjon() != null)
				.collect(Collectors.toList());

		journalposterMedSaksrelasjon.stream()
				.map(journalpostDtoToArkivSakMapper::map)
				.forEach(arkivsak -> safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak));
	}
}
