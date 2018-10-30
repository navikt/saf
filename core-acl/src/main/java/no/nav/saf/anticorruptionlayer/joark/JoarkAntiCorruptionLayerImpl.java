package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.domain.DokumentInfoTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalposterRequest;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalposterResponse;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.DokumentStatus;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.Dokumentkategori;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.JournalpostStatus;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.JournalpostType;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.Mottakskanal;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.Utsendingskanal;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
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
						.avsenderID(journalpostTo.getAvsenderMottakerId())
						.avsenderNavn(journalpostTo.getAvsenderMottaker())
						.journalpostID(journalpostTo.getJournalpostId().toString())
						.journalposttittel(journalpostTo.getInnhold())
						.journalposttype(JournalpostType.fromJoark(journalpostTo.getJournalposttype()))
						.journalstatus(JournalpostStatus.fromJoark(journalpostTo.getJournalstatus()))
						.mottakskanal(Mottakskanal.fromJoark(journalpostTo.getMottakskanal()))
						.opprettet(journalpostTo.getDatoOpprettet() == null ? null : journalpostTo.getDatoOpprettet().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
						.utsendingskanal(Utsendingskanal.fromJoark(journalpostTo.getUtsendingskanal()))
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
