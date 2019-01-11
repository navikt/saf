package no.nav.saf.anticorruptionlayer.joark.domain;

import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static no.nav.saf.tjeneste.visningsmodell.RelevantDato.INVALID_DATE;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.SaksrelasjonDto;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.RequestCache;
import no.nav.saf.tilgangskontroll.SafSecurityContext;
import no.nav.saf.tjeneste.visningsmodell.Bruker;
import no.nav.saf.tjeneste.visningsmodell.BrukerIdType;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Dokumentvariant;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.LogiskVedlegg;
import no.nav.saf.tjeneste.visningsmodell.RelevantDato;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.Datotype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Kanal;
import no.nav.saf.tjeneste.visningsmodell.kode.Variantformat;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class JournalpostDtoMapper {

	public Journalpost mapJournalpostDto(final JournalpostDto journalpostDto, final RequestCache requestCache, final SafSecurityContext safSecurityContext) {
		if (journalpostDto == null) {
			return null;
		}
		final Kanal kanal = mapKanal(journalpostDto);
		final String journalpostId = journalpostDto.getJournalpostId().toString();
		Journalpost journalpost = Journalpost.builder()
				.journalpostId(journalpostId)
				.tittel(journalpostDto.getInnhold())
				.journalposttype(JournalpostTypeCode.mapToJournalpostType(journalpostDto.getJournalposttype()))
				.journalstatus(mapJournalstatus(journalpostDto))
				.tema(FagomradeCode.toSafJournalstatus(journalpostDto.getFagomrade()))
				.temanavn(FagomradeCode.toSafJournalstatus(journalpostDto.getFagomrade()).getTemanavn())
				.sak(mapSak(journalpostDto.getSaksrelasjon(), requestCache))
				.bruker(mapBruker(journalpostDto.getSaksrelasjon(), requestCache))
				.avsenderMottakerNavn(journalpostDto.getAvsenderMottakerNavn())
				.journalfortAvNavn(journalpostDto.getJournalfortAvNavn())
				.kanal(kanal)
				.kanalnavn(kanal == null ? null : kanal.getKanalnavn())
				.datoOpprettet(journalpostDto.getDatoOpprettet() == null ? INVALID_DATE : LocalDateTime.from(journalpostDto.getDatoOpprettet()
						.toInstant()
						.atZone(ZoneId.systemDefault())))
				.relevanteDatoer(mapRelevanteDatoer(journalpostDto))
				.build();
		List<DokumentInfo> dokumenter = journalpostDto.getDokumenter().stream()
				.map(dokumentInfoDto -> DokumentInfo.builder()
						.parent(journalpost)
						.dokumentInfoId(dokumentInfoDto.getDokumentInfoId())
						.tittel(dokumentInfoDto.getTittel())
						.brevkode(dokumentInfoDto.getBrevkode())
						.dokumentvarianter(Collections.singletonList(Dokumentvariant.builder()
								.saksbehandlerHarTilgang(findSaksbehandlerHarTilgang(journalpost, requestCache, safSecurityContext))
								.variantformat(Variantformat.valueOf(dokumentInfoDto.getVariantFormat().name()))
								.build()))
						.logiskeVedlegg(dokumentInfoDto.getLogiske().stream()
								.map(logiskVedleggDto -> new LogiskVedlegg(logiskVedleggDto.getTittel()))
								.collect(Collectors.toList()))
						.build()).collect(Collectors.toList());
		journalpost.getDokumenter().addAll(dokumenter);
		return journalpost;
	}

	private Bruker mapBruker(SaksrelasjonDto saksrelasjon, RequestCache requestCache) {
		if (saksrelasjon == null) {
			return null;
		}

		Bruker bruker = getBrukerFromArkivsakCache(saksrelasjon, requestCache);
		if (bruker != null) {
			return bruker;
		} else {
			return getBrukerFromTilgangBrukerCache(requestCache);
		}
	}

	private Sak mapSak(SaksrelasjonDto saksrelasjon, RequestCache requestCache) {
		if (saksrelasjon == null) {
			return null;
		} else {
			Arkivsak arkivsak = requestCache.getObject(saksrelasjon.getSakId() + mapJoarkFagsystem(saksrelasjon.getFagsystem()));
			if (arkivsak == null) {
				return null;
			}
			return Sak.builder()
					.arkivsaksnummer(arkivsak.getArkivsaksnummer())
					.arkivsaksystem(arkivsak.getArkivsaksystem())
					.fagsaksnummer(arkivsak.getFagsaksnummer())
					.fagsaksystem(arkivsak.getFagsaksystem())
					.tema(arkivsak.getTema())
					.datoOpprettet(arkivsak.getDatoOpprettet())
					.build();
		}
	}

	private Journalstatus mapJournalstatus(JournalpostDto journalpostDto) {
		SaksrelasjonDto saksrelasjon = journalpostDto.getSaksrelasjon();
		if (saksrelasjon != null && saksrelasjon.getFeilregistrert() != null && saksrelasjon.getFeilregistrert()) {
			return Journalstatus.FEILREGISTRERT;
		} else {
			return journalpostDto.getJournalstatus().toSafJournalStatus();
		}
	}

	private List<RelevantDato> mapRelevanteDatoer(JournalpostDto journalpostDto) {
		List<RelevantDato> relevanteDatoer = new ArrayList<>();
		if (journalpostDto.getDokumentDato() != null) {
			relevanteDatoer.add(new RelevantDato(journalpostDto.getDokumentDato(), Datotype.DATO_DOKUMENT));
		}
		switch (journalpostDto.getJournalposttype()) {
			case I:
				if (journalpostDto.getMottattDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getMottattDato(), Datotype.DATO_REGISTRERT));
				}
				if (journalpostDto.getJournalDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getJournalDato(), Datotype.DATO_JOURNALFOERT));
				}
				break;
			case U:
				if (journalpostDto.getSendtPrintDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getSendtPrintDato(), Datotype.DATO_SENDT_PRINT));
				}
				if (journalpostDto.getEkspedertDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getEkspedertDato(), Datotype.DATO_EKSPEDERT));
				}
				if (journalpostDto.getAvsReturDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getAvsReturDato(), Datotype.DATO_AVS_RETUR));
				}
				break;
			default:
				return relevanteDatoer;
		}
		return relevanteDatoer;
	}

	private Kanal mapKanal(JournalpostDto journalpostDto) {
		switch (journalpostDto.getJournalposttype()) {
			case I:
				if (journalpostDto.getMottakskanal() == null) {
					return mapManglendeMottakskanal(journalpostDto);
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

	// TODO skal lages en Kanal.UKJENT istedet, fikses etter test er fikset.
	private Kanal mapManglendeMottakskanal(JournalpostDto journalpostDto) {
		return null;
	}

	private Kanal mapManglendeUtsendingskanal(JournalpostDto journalpostDto) {
		switch (journalpostDto.getJournalstatus()) {
			case FL:
				return Kanal.LOKAL_UTSKRIFT;
			case FS:
				return Kanal.SENTRAL_UTSKRIFT;
			case E:
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

	//journalposten er endelig journalført
	private Bruker getBrukerFromArkivsakCache(SaksrelasjonDto saksrelasjon, RequestCache requestCache) {
		Arkivsak arkivsak = requestCache.getObject(saksrelasjon.getSakId() + mapJoarkFagsystem(saksrelasjon.getFagsystem()));
		if (arkivsak == null || arkivsak.isBrukerInfoMissing()) {
			return null;
		}
		if (arkivsak.isBrukerPerson()) {
			return new Bruker(arkivsak.getAktoerId(), BrukerIdType.AKTOERID);
		} else {
			return new Bruker(arkivsak.getOrgnummer(), BrukerIdType.ORGNR);
		}
	}

	//journalposten er midlertidig journalført
	private Bruker getBrukerFromTilgangBrukerCache(RequestCache requestCache) {
		TilgangBruker tilgangBruker = requestCache.getObject(TILGANG_BRUKER);
		if (tilgangBruker == null) {
			return null;
		}
		if (tilgangBruker.isBrukerPerson()) {
			return new Bruker(tilgangBruker.getAktoerId(), BrukerIdType.AKTOERID);
		} else {
			return new Bruker(tilgangBruker.getOrgnummer(), BrukerIdType.ORGNR);
		}
	}

	private boolean findSaksbehandlerHarTilgang(Journalpost journalpost, RequestCache requestCache, SafSecurityContext safSecurityContext) {
		try {
			String tilgangKey = "tilgang:" + safSecurityContext
					.getSaksbehandlerId() + ":tema=" + journalpost.getTema();
			return requestCache.getObject(tilgangKey);
		} catch (NullPointerException e) {
			return false;
		}
	}
}
