package no.nav.saf.anticorruptionlayer.joark;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.SafToJoarkJournalstatusMapper;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.FinnJournalposterRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.FinnJournalposterResponseTo;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
class JoarkAntiCorruptionLayerImpl implements JoarkAntiCorruptionLayer {
	private final HentJournalsakinfo hentJournalsakinfo;
	private final SafToJoarkJournalstatusMapper safToJoarkJournalstatusMapper;

	@Inject
	public JoarkAntiCorruptionLayerImpl(HentJournalsakinfo hentJournalsakinfo) {
		this.hentJournalsakinfo = hentJournalsakinfo;
		this.safToJoarkJournalstatusMapper = new SafToJoarkJournalstatusMapper();
	}

	@Override
	public List<JournalpostDto> finnJournalposter(List<String> alleIdenter,
												  List<TilgangSak> tilgangSakList,
												  LocalDate fraDato,
												  List<Journalposttype> inkluderJournalposttyper,
												  List<Journalstatus> inkluderJournalstatuses,
												  Integer foerste, String etterPeker, Integer siste, String foerPeker) {
		FinnJournalposterResponseTo responseTo = hentJournalsakinfo.finnJournalposter(FinnJournalposterRequestTo.builder()
				.alleIdenter(alleIdenter)
				.inkluderJournalpostType(inkluderJournalposttyper.stream()
						.map(jt -> JournalpostTypeCode.valueOf(jt.name()))
						.collect(Collectors.toList()))
				.inkluderJournalStatus(safToJoarkJournalstatusMapper.map(inkluderJournalstatuses))
				.visFeilregistrerte(inkluderJournalstatuses.contains(Journalstatus.FEILREGISTRERT))
				.fraDato(fraDato.toString())
				.gsakSakIds(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.GSAK.equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()))
				.psakSakIds(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.PSAK.equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()))
				.foerste(foerste)
				.etterPeker(etterPeker)
				.siste(siste)
				.foerPeker(foerPeker)
				.build());

		return responseTo.getTilgangJournalposter();
	}
}
