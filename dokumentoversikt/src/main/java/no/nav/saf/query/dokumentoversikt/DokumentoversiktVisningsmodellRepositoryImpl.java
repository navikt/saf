package no.nav.saf.query.dokumentoversikt;

import no.nav.saf.anticorruptionlayer.joark.domain.Rjoark900JournalpostDtoMapper;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class DokumentoversiktVisningsmodellRepositoryImpl implements DokumentoversiktVisningsmodellRepository {
	private final Rjoark900JournalpostDtoMapper rjoark900JournalpostDtoMapper;

	@Inject
	public DokumentoversiktVisningsmodellRepositoryImpl(Rjoark900JournalpostDtoMapper rjoark900JournalpostDtoMapper) {
		this.rjoark900JournalpostDtoMapper = rjoark900JournalpostDtoMapper;
	}

	@Override
	public List<Journalpost> findJournalposter(final List<String> journalpostIds,
											   final SafRequestContext safRequestContext) {
		return journalpostIds.stream().map(journalpostId -> {
			JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(journalpostId);
			return rjoark900JournalpostDtoMapper.mapJournalpostDto(journalpostDto, safRequestContext.getRequestCache());
		}).filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
}
