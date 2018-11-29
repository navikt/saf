package no.nav.saf.tjeneste.dokumentoversiktbruker;

import static no.nav.saf.domain.TilgangsmodellRepositoryImpl.PENSJON;

import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoMapper;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokumentoversiktBrukerVisningsmodellRepositoryImpl implements DokumentoversiktBrukerVisningsmodellRepository {
	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final JournalpostDtoMapper journalpostDtoMapper;

	@Inject
	public DokumentoversiktBrukerVisningsmodellRepositoryImpl(GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
															  PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
															  JournalpostDtoMapper journalpostDtoMapper) {
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.journalpostDtoMapper = journalpostDtoMapper;
	}

	@Override
	public List<Journalpost> findJournalposter(SafRequestContext safRequestContext,
											   List<Tema> tema,
											   String aktoerId,
											   String foedselsnummer,
											   List<String> journalpostIds) {
		List<Sak> sakerByAktoerId = gsakAntiCorruptionLayer.findSakerByAktoerId(aktoerId);
		if(!Collections.disjoint(tema, PENSJON)) {
			sakerByAktoerId.addAll(pensjonSakAntiCorruptionLayer.hentSakerByFoedselsnummer(foedselsnummer));
		}
		Map<String, Sak> sakMap = sakerByAktoerId.stream().collect(Collectors.toMap(Sak::getArkivsaksnummer, sak -> sak));
		return journalpostIds.stream().map(journalpostId -> {
			JournalpostDto journalpostDto = safRequestContext.getParameterContext().getParameter("journalpostId=" + journalpostId);
			return journalpostDtoMapper.mapJournalpostDto(sakMap, journalpostDto);
		}).collect(Collectors.toList());
	}
}
