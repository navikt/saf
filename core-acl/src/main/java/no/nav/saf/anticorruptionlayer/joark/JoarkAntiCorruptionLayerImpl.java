package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.domain.DokumentInfoTo;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalposterRequest;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalposterResponse;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalpostType;
import no.nav.saf.tjeneste.visningsmodell.kode.Mottakskanal;
import no.nav.saf.tjeneste.visningsmodell.kode.VariantFormatkode;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class JoarkAntiCorruptionLayerImpl implements JoarkAntiCorruptionLayer {

	private final HentJournalsakinfo hentJournalsakinfo;

	@Inject
	public JoarkAntiCorruptionLayerImpl(HentJournalsakinfo hentJournalsakinfo) {
		this.hentJournalsakinfo = hentJournalsakinfo;
	}

	@Override
	public List<Journalpost> hentJournalpostListeByArkivsaker(List<Sak> saker) {
		HentJournalposterResponse hentJournalposterResponse = hentJournalsakinfo.hentJournalposter(HentJournalposterRequest.builder()
				.gsakSakIdList(saker.stream()
						.filter(sak -> Arkivsakssystem.GSAK.equals(sak.getArkivsaksystem()))
						.map(Sak::getArkivsaksnummer).collect(Collectors.toList()))
				.psakSakIdList(saker.stream()
						.filter(tilgangSak -> Arkivsakssystem.PSAK.equals(tilgangSak.getArkivsaksystem()))
						.map(Sak::getArkivsaksnummer).collect(Collectors.toList()))
				.build());

		Map<String, Sak> sakMap = saker.stream().collect(Collectors.toMap(Sak::getArkivsaksnummer, sak -> sak));

		return hentJournalposterResponse
				.getGsakJournalpostList().stream().map(journalpostTo -> Journalpost.builder()
						.journalpostId(journalpostTo.getJournalpostId().toString())
						.beskrivelse(journalpostTo.getInnhold())
						.journalposttype(JournalpostType.fromJoark(journalpostTo.getJournalposttype()))
						.journalstatus(journalpostTo.getJournalstatus().toSafJournalStatus())
						.tema(FagomradeCode.toSafJournalStatus(journalpostTo.getFagomrade()))
						.temanavn(FagomradeCode.toSafJournalStatus(journalpostTo.getFagomrade()).getTemanavn())
						.mottakskanal(Mottakskanal.fromJoark(journalpostTo.getMottakskanal()))
						.opprettet(journalpostTo.getDatoOpprettet() == null ? null : LocalDateTime.from(journalpostTo.getDatoOpprettet()
								.toInstant()
								.atZone(ZoneId.systemDefault())))
						.sak(journalpostTo.getSaksrelasjon() == null ? null : sakMap.get(journalpostTo.getSaksrelasjon()
								.getSakId()))
						.dokumenter(journalpostTo.getJournalpostDokumentInfoRelasjoner().stream()
								.map(jr -> {
									final DokumentInfoTo dokumentInfoTo = jr.getDokumentInfo();
									return DokumentInfo.builder()
											.dokumentId(dokumentInfoTo.getDokumentInfoId().toString())
											.tittel(dokumentInfoTo.getTittel())
											.variantFormat(VariantFormatkode.ARKIV)
											.saksbehandlerHarTilgang(false)
											.innbyggerHarDigitaltInnsyn(false)
											.build();
								}).collect(Collectors.toList()))
						.build()).collect(Collectors.toList());
	}

	@Override
	public List<TilgangJournalpost> hentTilgangJournalpostListByArkivsaker(List<TilgangSak> tilgangSakList) {
		HentJournalposterResponse hentJournalposterResponse = hentJournalsakinfo.hentJournalposter(HentJournalposterRequest.builder()
				.gsakSakIdList(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.GSAK.name().equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()))
				.psakSakIdList(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.PSAK.name().equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()))
				.build());

		return hentJournalposterResponse
				.getGsakJournalpostList().stream().map(journalpostTo -> TilgangJournalpost.builder()
						.journalpostId(journalpostTo.getJournalpostId().toString())
						.journalstatus(journalpostTo.getJournalstatus() == null ? null : journalpostTo.getJournalstatus()
								.name())
						.dokumenter(journalpostTo.getJournalpostDokumentInfoRelasjoner().stream()
								.map(relasjon -> TilgangDokumentInfo.builder()
										.dokumentInfoId(relasjon.getDokumentInfo().getDokumentInfoId().toString())
										.dokumentstatus(relasjon.getDokumentInfo().getDokumentstatus() == null ? null : relasjon
												.getDokumentInfo().getDokumentstatus().name())
										.variantFormat("ARKIV") //TODO Endre hentJournalsakInfo til å også returnere variantformat
										.build())
								.collect(Collectors.toList()))
						.build())
				.collect(Collectors.toList());
	}

}
