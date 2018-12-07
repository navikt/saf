package no.nav.saf.tjeneste.dokumentoversiktbruker;

import no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoMapper;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokumentoversiktBrukerVisningsmodellRepositoryImpl implements DokumentoversiktBrukerVisningsmodellRepository {
	private final JournalpostDtoMapper journalpostDtoMapper;

	@Inject
	public DokumentoversiktBrukerVisningsmodellRepositoryImpl(JournalpostDtoMapper journalpostDtoMapper) {
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
