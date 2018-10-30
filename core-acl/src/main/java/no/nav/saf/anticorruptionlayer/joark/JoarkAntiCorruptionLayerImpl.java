package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.domain.DokumentInfoTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalposterRequest;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalposterResponse;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.DokumentStatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Dokumentkategori;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalpostType;
import no.nav.saf.tjeneste.visningsmodell.kode.Mottakskanal;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.ZoneId;
import java.util.Collections;
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
	public List<Journalpost> hentJournalpostListeByArkivsaksnummer(final String arkivsaksnummer) {
		HentJournalposterResponse hentJournalposterResponse = hentJournalsakinfo.hentJournalposter(HentJournalposterRequest.builder().gsakSakIdList(Collections.singletonList(arkivsaksnummer)).build());
		return hentJournalposterResponse
				.getGsakJournalpostList().stream().map(journalpostTo -> Journalpost.builder()
						.journalpostID(journalpostTo.getJournalpostId().toString())
						.beskrivelse(journalpostTo.getInnhold())
						.journalposttype(JournalpostType.fromJoark(journalpostTo.getJournalposttype()))
						.journalstatus(journalpostTo.getJournalstatus().toSafJournalStatus())
						.mottakskanal(Mottakskanal.fromJoark(journalpostTo.getMottakskanal()))
						.opprettet(journalpostTo.getDatoOpprettet() == null ? null : journalpostTo.getDatoOpprettet().toInstant().atZone(ZoneId.systemDefault()).toString())
						.build()).collect(Collectors.toList());
	}

	@Override
	public List<Journalpost> hentJournalpostListeByArkivsaker(List<Sak> saker) {
		HentJournalposterResponse hentJournalposterResponse = hentJournalsakinfo.hentJournalposter(HentJournalposterRequest.builder()
				.gsakSakIdList(saker.stream().map(Sak::getArkivsaksnummer).collect(Collectors.toList()))
				.build());
		Map<String, Sak> sakMap = saker.stream().collect(Collectors.toMap(Sak::getArkivsaksnummer, sak -> sak));
		return hentJournalposterResponse
				.getGsakJournalpostList().stream().map(journalpostTo -> Journalpost.builder()
						.journalpostID(journalpostTo.getJournalpostId().toString())
						.beskrivelse(journalpostTo.getInnhold())
						.journalposttype(JournalpostType.fromJoark(journalpostTo.getJournalposttype()))
						.journalstatus(journalpostTo.getJournalstatus().toSafJournalStatus())
						.mottakskanal(Mottakskanal.fromJoark(journalpostTo.getMottakskanal()))
						.opprettet(journalpostTo.getDatoOpprettet() == null ? null : journalpostTo.getDatoOpprettet().toInstant().atZone(ZoneId.systemDefault()).toString())
						.sak(journalpostTo.getSaksrelasjon() == null ? null : sakMap.get(journalpostTo.getSaksrelasjon().getSakId()))
						.build()).collect(Collectors.toList());
	}

	@Override
	public List<DokumentInfo> hentDokumentInfoListeByJournalpostIdAndArkivsak(String journalpostId, String arkivsaksnummer) {
		HentJournalposterResponse hentJournalposterResponse = hentJournalsakinfo.hentJournalposter(HentJournalposterRequest.builder().gsakSakIdList(Collections.singletonList(arkivsaksnummer)).build());
		return hentJournalposterResponse
				.getGsakJournalpostList().stream()
				.filter(j -> journalpostId.equals(j.getJournalpostId().toString()))
				.flatMap(j -> j.getJournalpostDokumentInfoRelasjoner().stream())
				.map(jr -> {
					final DokumentInfoTo dokumentInfoTo = jr.getDokumentInfo();
					return DokumentInfo.builder()
							.dokumenttypeID(dokumentInfoTo.getDokumenttypeId())
							.dokumentkategori(Dokumentkategori.fromJoark(dokumentInfoTo.getKategori()))
							.dokumentStatus(DokumentStatus.fromJoark(dokumentInfoTo.getDokumentstatus()))
							.dokumentID(dokumentInfoTo.getDokumentInfoId().toString())
							.navSkjemaID(dokumentInfoTo.getBrevkode())
							.tittel(dokumentInfoTo.getTittel())
							.build();
				}).collect(Collectors.toList());
	}
}
