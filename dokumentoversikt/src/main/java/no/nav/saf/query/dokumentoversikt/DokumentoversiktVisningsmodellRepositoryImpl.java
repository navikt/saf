package no.nav.saf.query.dokumentoversikt;

import no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoMapper;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
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
	private final JournalpostDtoMapper journalpostDtoMapper;

	@Inject
	public DokumentoversiktVisningsmodellRepositoryImpl(JournalpostDtoMapper journalpostDtoMapper) {
		this.journalpostDtoMapper = journalpostDtoMapper;
	}

	@Override
	public List<Journalpost> findJournalposter(final List<String> journalpostIds,
											   final SafRequestContext safRequestContext) {
		return journalpostIds.stream().map(journalpostId -> {
					JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(journalpostId);
					return journalpostDtoMapper.mapJournalpostDto(journalpostDto, safRequestContext.getRequestCache());
				}).filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
}
