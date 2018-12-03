package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.SaksrelasjonDto;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.ParameterContext;
import no.nav.saf.tjeneste.visningsmodell.Bruker;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Dokumentvariant;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.RelevantDato;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.Brukertype;
import no.nav.saf.tjeneste.visningsmodell.kode.Datotype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Kanal;
import no.nav.saf.tjeneste.visningsmodell.kode.Variantformat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO behøver enhetstest
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class JournalpostDtoMapper {
	public Journalpost mapJournalpostDto(final JournalpostDto journalpostDto, final ParameterContext parameterContext) {
		if (journalpostDto == null) {
			return null;
		}
		final Kanal kanal = mapKanal(journalpostDto);
		return Journalpost.builder()
				.journalpostId(journalpostDto.getJournalpostId().toString())
				.tittel(journalpostDto.getInnhold())
				.journalposttype(Journalposttype.fromJoark(journalpostDto.getJournalposttype()))
				.journalstatus(mapJournalstatus(journalpostDto))
				.tema(FagomradeCode.toSafJournalstatus(journalpostDto.getFagomrade()))
				.temanavn(FagomradeCode.toSafJournalstatus(journalpostDto.getFagomrade()).getTemanavn())
				.sak(mapSak(journalpostDto, parameterContext))
				.bruker(mapBruker(parameterContext))
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

	private Bruker mapBruker(ParameterContext parameterContext) {
		TilgangBruker tilgangBruker = parameterContext.getParameter("tilgangBruker");
		if(tilgangBruker == null) {
			return null;
		}
		return new Bruker(Brukertype.PERSON, tilgangBruker.getFoedselsnr());
	}

	private Sak mapSak(JournalpostDto journalpostDto, ParameterContext parameterContext) {
		SaksrelasjonDto saksrelasjon = journalpostDto.getSaksrelasjon();
		if (saksrelasjon == null) {
			return null;
		} else {
			Arkivsak arkivsak = parameterContext.getParameter("sakId=" + saksrelasjon.getSakId() + "-" + mapJoarkFagsystem(saksrelasjon.getFagsystem()));
			if (arkivsak == null) {
				return null;
			}
			return Sak.builder()
					.arkivsaksnummer(arkivsak.getArkivsaksnummer())
					.arkivsaksystem(arkivsak.getArkivsaksystem())
					.fagsaksnummer(arkivsak.getFagsaksnummer())
					.fagsystem(arkivsak.getFagsystem())
					.tema(arkivsak.getTema())
					.datoOpprettet(arkivsak.getDatoOpprettet())
					.build();
		}
	}

	private Journalstatus mapJournalstatus(JournalpostDto journalpostDto) {
		SaksrelasjonDto saksrelasjon = journalpostDto.getSaksrelasjon();
		if (saksrelasjon != null &&  saksrelasjon.getFeilregistrert() != null && saksrelasjon.getFeilregistrert()) {
			return Journalstatus.FEILREGISTRERT;
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

	private String mapJoarkFagsystem(FagsystemCode joarkFagsystem) {
		switch (joarkFagsystem) {
			case PEN:
				return Arkivsakssystem.PSAK.name();
			case FS22:
				return Arkivsakssystem.GSAK.name();
			default:
				return "";
		}
	}
}
