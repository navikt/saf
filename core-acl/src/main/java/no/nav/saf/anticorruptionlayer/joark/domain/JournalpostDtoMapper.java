package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Dokumentvariant;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.RelevantDato;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Datotype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalz;
import no.nav.saf.tjeneste.visningsmodell.kode.Journazz;
import no.nav.saf.tjeneste.visningsmodell.kode.Kanal;
import no.nav.saf.tjeneste.visningsmodell.kode.Variantformat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO behøver enhetstest
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class JournalpostDtoMapper {
	public Journalpost mapJournalpostDto(Map<String, Sak> sakMap, JournalpostDto journalpostDto) {
		final Kanal kanal = mapKanal(journalpostDto);
		return Journalpost.builder()
				.journalpostId(journalpostDto.getJournalpostId().toString())
				.tittel(journalpostDto.getInnhold())
				.journalposttype(Journalz.fromJoark(journalpostDto.getJournalposttype()))
				.journalstatus(mapJournalstatus(journalpostDto))
				.tema(FagomradeCode.toSafJournalStatus(journalpostDto.getFagomrade()))
				.temanavn(FagomradeCode.toSafJournalStatus(journalpostDto.getFagomrade()).getTemanavn())
				.sak(journalpostDto.getSaksrelasjon() == null ? null : sakMap.get(journalpostDto.getSaksrelasjon()
						.getSakId()))
				.avsenderMottakerNavn(journalpostDto.getAvsenderMottakerNavn())
				.journalfortAvNavn(journalpostDto.getJournalfortAvNavn())
				.kanal(kanal)
				.kanalnavn(kanal == null ? null : kanal.getKanalnavn())
				.relevanteDatoer(mapRelevanteDatoer(journalpostDto))
				.dokumenter(journalpostDto.getDokumenter().stream()
						.map(dokumentInfoDto -> DokumentInfo.builder()
								.dokumentId(dokumentInfoDto.getDokumentInfoId())
								.tittel(dokumentInfoDto.getTittel())
								.navSkjemaId(dokumentInfoDto.getBrevkode())
								.saksbehandlerHarTilgang(true) //TODO
								.dokumentvarianter(Collections.singletonList(Dokumentvariant.builder()
										.variantformat(Variantformat.valueOf(dokumentInfoDto.getVariantFormat().name()))
										.build()))
								.build()).collect(Collectors.toList())).build();
	}

	private Journazz mapJournalstatus(JournalpostDto journalpostDto) {
		if (journalpostDto.getSaksrelasjon() != null && journalpostDto.getSaksrelasjon().getFeilregistrert()) {
			return Journazz.FEILREGISTRERT;
		} else {
			return journalpostDto.getJournalstatus().toSafJournalStatus();
		}
	}

	private List<RelevantDato> mapRelevanteDatoer(JournalpostDto journalpostDto) {
		List<RelevantDato> relevanteDatoer = new ArrayList<>();
		switch (journalpostDto.getJournalposttype()) {
			case I:
				if (journalpostDto.getMottattDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getMottattDato(), Datotype.MOTTATT_DATO));
				}
			case U:
				if (journalpostDto.getEkspedertDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getEkspedertDato(), Datotype.EKSPEDERT_DATO));
				}
			default:
				if (journalpostDto.getDatoOpprettet() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getDatoOpprettet(), Datotype.OPPRETTET_DATO));
				}
				return relevanteDatoer;
		}
	}

	private Kanal mapKanal(JournalpostDto journalpostDto) {
		switch (journalpostDto.getJournalposttype()) {
			case I:
				if (journalpostDto.getMottakskanal() == null) {
					return null;
				}
				return journalpostDto.getMottakskanal().getSafKanal();
			case U:
				if (journalpostDto.getUtsendingskanal() == null) {
					return mapManglendeUtsendingskanal(journalpostDto);
				}
				return journalpostDto.getUtsendingskanal().getSafKanal();
			case N:
				return Kanal.INGEN_DISTRIBUSJON;
			default:
				return null;
		}
	}

	private Kanal mapManglendeUtsendingskanal(JournalpostDto journalpostDto) {
		switch (journalpostDto.getJournalstatus()) {
			case FL:
				return Kanal.LOKAL_UTSKRIFT;
			case FS:
				return Kanal.SENTRAL_UTSKRIFT;
			default:
				return null;
		}
	}
}
