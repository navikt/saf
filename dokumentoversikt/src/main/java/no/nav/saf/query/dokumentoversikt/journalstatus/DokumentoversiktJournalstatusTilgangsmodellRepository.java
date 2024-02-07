package no.nav.saf.query.dokumentoversikt.journalstatus;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.ArkivsakMapper;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
class DokumentoversiktJournalstatusTilgangsmodellRepository {
	private final ArkivsakMapper arkivsakMapper;

	public DokumentoversiktJournalstatusTilgangsmodellRepository(ArkivsakMapper arkivsakMapper) {
		this.arkivsakMapper = arkivsakMapper;
	}

	public void mapOgCacheArkivsaker(final List<TilgangJournalpost> filteredTilgangJournalpostList, final SafRequestContext safRequestContext) {
		List<JournalpostDto> journalposterMedSaksrelasjon = filteredTilgangJournalpostList.stream()
				.map(tj -> safRequestContext.getRequestCache().getJournalpost(tj.getJournalpostId()))
				.filter(jp -> jp.getSaksrelasjon() != null)
				.collect(Collectors.toList());

		journalposterMedSaksrelasjon.stream()
				.map(arkivsakMapper::map)
				.forEach(arkivsak -> safRequestContext.getRequestCache().putArkivsak(arkivsak));
	}
}
