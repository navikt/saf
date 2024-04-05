package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.BrukerTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.DokumentStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.MottaksKanalCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivBruker;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivDokumentinfo;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivFildetaljer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivRelevanteDatoer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSak;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSaksrelasjon;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Datotype;
import no.nav.saf.domain.kode.Dokumentstatus;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Kanal;
import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.kode.Variantformat;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.visningsmodell.AvsenderMottaker;
import no.nav.saf.domain.visningsmodell.Bruker;
import no.nav.saf.domain.visningsmodell.BrukerIdType;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Dokumentvariant;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.LogiskVedlegg;
import no.nav.saf.domain.visningsmodell.RelevantDato;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.domain.visningsmodell.Tilleggsopplysning;
import no.nav.saf.domain.visningsmodell.Utsendingsinfo;
import no.nav.saf.tilgangskontroll.RequestCache;
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static no.nav.saf.anticorruptionlayer.joark.ArkivAvsenderMottakerMapper.mapArkivAvsenderMottaker;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoMapper.mapArkivUtsendingsInfo;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode.U;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode.mapToJournalpostType;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep2d;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep5;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep6d;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep7d;
import static no.nav.saf.domain.DomainConstants.TIDSSONE_NORGE;
import static no.nav.saf.domain.visningsmodell.RelevantDato.INVALID_DATE;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

public class ArkivJournalpostMapper {
	static final String FILTYPE_PDF = "PDF";
	static final String FILTYPE_PDFA = "PDFA";
	public static final String ARKIVJOURNALPOST_OVERSTYRTINNSYN_STANDARD = "BRUK_STANDARDREGLER";

	public static Journalpost mapJournalpost(ArkivJournalpost arkivJournalpost, RequestCache requestCache) {
		if (arkivJournalpost == null) {
			return null;
		}
		final String journalpostId = arkivJournalpost.journalpostId().toString();
		Kanal kanal = mapKanal(arkivJournalpost);
		Tema tema = mapTema(arkivJournalpost, requestCache);
		AvsenderMottaker avsenderMottaker = mapArkivAvsenderMottaker(arkivJournalpost);

		Journalpost journalpost = Journalpost.builder()
				.journalpostId(journalpostId)
				.tittel(arkivJournalpost.innhold())
				.journalposttype(mapToJournalpostType(arkivJournalpost.type()))
				.journalstatus(mapJournalstatus(arkivJournalpost))
				.tema(tema)
				.temanavn(tema == null ? null : tema.getTemanavn())
				.behandlingstema(arkivJournalpost.behandlingstema())
				.behandlingstemanavn(arkivJournalpost.behandlingstemanavn())
				.sak(mapSak(arkivJournalpost.saksrelasjon(), requestCache))
				.bruker(mapBruker(arkivJournalpost.bruker(), arkivJournalpost.saksrelasjon(), requestCache))
				.avsenderMottaker(avsenderMottaker)
				.avsenderMottakerId(avsenderMottaker == null ? null : avsenderMottaker.getId())
				.avsenderMottakerNavn(avsenderMottaker == null ? null : avsenderMottaker.getNavn())
				.avsenderMottakerLand(avsenderMottaker == null ? null : avsenderMottaker.getLand())
				.journalforendeEnhet(arkivJournalpost.journalfoerendeEnhet())
				.journalfoerendeEnhet(arkivJournalpost.journalfoerendeEnhet())
				.journalfortAvNavn(arkivJournalpost.journalfoertAvNavn())
				.opprettetAvNavn(arkivJournalpost.opprettetAvNavn())
				.kanal(kanal)
				.kanalnavn(kanal == null ? null : kanal.getKanalnavn())
				.skjerming(mapSkjerming(arkivJournalpost.skjerming()))
				.datoOpprettet(mapDatoOpprettet(arkivJournalpost.relevanteDatoer()))
				.relevanteDatoer(mapRelevanteDatoer(arkivJournalpost))
				.tilleggsopplysninger(mapTilleggsopplysninger(arkivJournalpost))
				.antallRetur(mapAntallRetur(arkivJournalpost))
				.overstyrinnsynsregler(arkivJournalpost.innsyn() == null ? ARKIVJOURNALPOST_OVERSTYRTINNSYN_STANDARD : arkivJournalpost.innsyn())
				.overstyrinnsynsreglerNavn(arkivJournalpost.innsynsbeskrivelse())
				.eksternReferanseId(arkivJournalpost.kanalreferanseId())
				.utsendingsinfo(mapUtsendingsInfo(arkivJournalpost))
				.build();

		journalpost.getDokumenter().addAll(mapDokumenter(journalpost, arkivJournalpost, requestCache));
		return journalpost;
	}

	private static Sak mapSak(ArkivSaksrelasjon saksrelasjon, RequestCache requestCache) {
		if (saksrelasjon == null) {
			return null;
		}
		if (saksrelasjon.isPensjonsak()) {
			Arkivsak arkivsak = requestCache.getArkivsak(saksrelasjon);
			if (arkivsak == null) {
				return Sak.builder()
						.arkivsaksnummer(String.valueOf(saksrelasjon.sakId()))
						.arkivsaksystem(FagsystemCode.toSafArkivsaksystem(saksrelasjon.fagsystem()))
						.build();
			}
			return Sak.builder()
					.arkivsaksnummer(String.valueOf(saksrelasjon.sakId()))
					.arkivsaksystem(arkivsak.getArkivsaksystem())
					.fagsakId(arkivsak.getFagsakId())
					.fagsaksystem(arkivsak.getFagsaksystem())
					.datoOpprettet(arkivsak.getDatoOpprettet())
					.sakstype(Sakstype.fromFagsaksystem(arkivsak.getFagsaksystem()))
					.tema(arkivsak.getTema())
					.build();
		} else {
			ArkivSak arkivSak = saksrelasjon.sak();
			if (arkivSak == null) {
				return null;
			}
			return Sak.builder()
					.arkivsaksnummer(String.valueOf(saksrelasjon.sakId()))
					.arkivsaksystem(FagsystemCode.toSafArkivsaksystem(saksrelasjon.fagsystem()))
					.fagsakId(arkivSak.fagsakNr())
					.fagsaksystem(arkivSak.applikasjon())
					.datoOpprettet(arkivSak.opprettetTid())
					.sakstype(Sakstype.fromFagsaksystem(arkivSak.applikasjon()))
					.tema(FagomradeCode.toSafTema(arkivSak.tema()))
					.build();
		}
	}

	private static Bruker mapBruker(ArkivBruker arkivBruker, ArkivSaksrelasjon saksrelasjon, RequestCache requestCache) {
		if (saksrelasjon == null || saksrelasjon.sakId() == null) {
			return mapBrukerFromArkivBruker(arkivBruker);
		}

		Bruker bruker = mapBrukerFromArkivsakCache(saksrelasjon, requestCache);
		if (bruker != null) {
			return bruker;
		} else {
			return mapBrukerFromTilgangBrukerCache(requestCache);
		}
	}

	private static Bruker mapBrukerFromArkivBruker(ArkivBruker arkivBruker) {
		if (arkivBruker == null) {
			return null;
		}

		final String brukerId = arkivBruker.id();
		final String brukerType = arkivBruker.type();
		if (isBlank(trim(brukerId)) || isBlank(brukerType)) {
			return null;
		}

		if (brukerType.equals(BrukerTypeCode.PERSON)) {
			return new Bruker(brukerId, BrukerIdType.FNR);
		} else if (brukerType.equals(BrukerTypeCode.ORGANISASJON)) {
			return new Bruker(trim(brukerId), BrukerIdType.ORGNR);
		} else {
			return null;
		}
	}

	// journalposten er endelig journalført
	private static Bruker mapBrukerFromArkivsakCache(ArkivSaksrelasjon saksrelasjon, RequestCache requestCache) {
		Arkivsak arkivsak = requestCache.getArkivsak(saksrelasjon);
		if (arkivsak == null || arkivsak.isBrukerInfoMissing()) {
			return null;
		}
		if (arkivsak.isBrukerPerson()) {
			return new Bruker(arkivsak.getAktoerId(), BrukerIdType.AKTOERID);
		} else if (arkivsak.isBrukerOrganisasjon()) {
			return new Bruker(arkivsak.getOrgnummer(), BrukerIdType.ORGNR);
		} else {
			return null;
		}
	}

	// journalposten er midlertidig journalført
	private static Bruker mapBrukerFromTilgangBrukerCache(RequestCache requestCache) {
		TilgangBruker tilgangBruker = requestCache.getTilgangBruker();
		if (tilgangBruker == null) {
			return null;
		}
		if (tilgangBruker.isPerson()) {
			if (tilgangBruker.getAktoerId() == null) {
				return new Bruker(tilgangBruker.getFoedselsnr(), BrukerIdType.FNR);
			} else {
				return new Bruker(tilgangBruker.getAktoerId(), BrukerIdType.AKTOERID);
			}
		} else if (tilgangBruker.isOrganisasjon()) {
			return new Bruker(tilgangBruker.getOrgnummer(), BrukerIdType.ORGNR);
		} else {
			return null;
		}
	}

	private static Journalstatus mapJournalstatus(ArkivJournalpost arkivJournalpost) {
		try {
			ArkivSaksrelasjon saksrelasjon = arkivJournalpost.saksrelasjon();
			Journalstatus journalstatus = JournalStatusCode.valueOf(arkivJournalpost.status()).toSafJournalstatus();
			if (saksrelasjon != null && saksrelasjon.feilregistrert() != null && saksrelasjon.feilregistrert()
				&& !(Journalstatus.UTGAAR == journalstatus)) {
				return Journalstatus.FEILREGISTRERT;
			} else {
				return journalstatus;
			}
		} catch (IllegalArgumentException e) {
			return Journalstatus.UKJENT;
		}
	}

	private static Tema mapTema(ArkivJournalpost arkivJournalpost, RequestCache requestCache) {
		if (arkivJournalpost.isTilknyttetSak()) {
			ArkivSaksrelasjon saksrelasjon = arkivJournalpost.saksrelasjon();
			if (saksrelasjon.isPensjonsak()) {
				Arkivsak arkivsak = requestCache.getArkivsak(saksrelasjon);
				if (arkivsak == null) {
					return FagomradeCode.toSafTema(arkivJournalpost.fagomraade());
				}
				return arkivsak.getTema();
			} else {
				ArkivSak arkivSak = saksrelasjon.sak();
				if (arkivSak == null || arkivSak.tema() == null) {
					return FagomradeCode.toSafTema(arkivJournalpost.fagomraade());
				}
				return FagomradeCode.toSafTema(arkivSak.tema());
			}
		} else {
			return FagomradeCode.toSafTema(arkivJournalpost.fagomraade());
		}
	}

	private static LocalDateTime mapDatoOpprettet(ArkivRelevanteDatoer arkivRelevanteDatoer) {
		return arkivRelevanteDatoer.opprettet() == null ? INVALID_DATE : arkivRelevanteDatoer.opprettet().atZoneSameInstant(TIDSSONE_NORGE).toLocalDateTime();
	}

	private static Skjerming mapSkjerming(String skjerming) {
		try {
			return skjerming == null ? null : SkjermingTypeCode.valueOf(skjerming).getSafSkjerming();
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static List<RelevantDato> mapRelevanteDatoer(ArkivJournalpost arkivJournalpost) {
		List<RelevantDato> relevanteDatoer = new ArrayList<>();
		ArkivRelevanteDatoer arkivRelevanteDatoer = arkivJournalpost.relevanteDatoer();
		if (arkivRelevanteDatoer.hoveddokument() != null) {
			relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.hoveddokument(), Datotype.DATO_DOKUMENT));
		}
		if (arkivRelevanteDatoer.journalfoert() != null) {
			relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.journalfoert(), Datotype.DATO_JOURNALFOERT));
		}
		switch (JournalpostTypeCode.valueOf(arkivJournalpost.type())) {
			case I:
				if (arkivRelevanteDatoer.forsendelseMottatt() != null) {
					relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.forsendelseMottatt(), Datotype.DATO_REGISTRERT));
				}
				break;
			case U:
				if (arkivRelevanteDatoer.sendtPrint() != null) {
					relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.sendtPrint(), Datotype.DATO_SENDT_PRINT));
				}
				if (arkivRelevanteDatoer.ekspedert() != null) {
					relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.ekspedert(), Datotype.DATO_EKSPEDERT));
				}
				if (arkivRelevanteDatoer.retur() != null) {
					relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.retur(), Datotype.DATO_AVS_RETUR));
				}
				if (arkivRelevanteDatoer.lest() != null) {
					relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.lest(), Datotype.DATO_LEST));
				}
				break;
			default:
				return relevanteDatoer;
		}
		return relevanteDatoer;
	}

	private static List<Tilleggsopplysning> mapTilleggsopplysninger(ArkivJournalpost arkivJournalpost) {
		Map<String, String> tilleggsopplysninger = arkivJournalpost.tilleggsopplysninger();
		if (tilleggsopplysninger == null || tilleggsopplysninger.isEmpty()) {
			return new ArrayList<>();
		}
		return tilleggsopplysninger.entrySet()
				.stream()
				.map(entry -> new Tilleggsopplysning(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
	}

	private static Kanal mapKanal(ArkivJournalpost arkivJournalpost) {
		return switch (JournalpostTypeCode.valueOf(arkivJournalpost.type())) {
			case I:
				if (arkivJournalpost.mottakskanal() == null) {
					yield Kanal.UKJENT;
				}
				try {
					yield MottaksKanalCode.valueOf(arkivJournalpost.mottakskanal()).getSafKanal();
				} catch (IllegalArgumentException e) {
					yield Kanal.UKJENT;
				}
			case U:
				if (arkivJournalpost.utsendingskanal() == null) {
					yield mapManglendeUtsendingskanal(arkivJournalpost);
				}
				try {
					yield UtsendingsKanalCode.valueOf(arkivJournalpost.utsendingskanal()).getSafKanal();
				} catch (IllegalArgumentException e) {
					yield Kanal.UKJENT;
				}
			case N:
				yield Kanal.INGEN_DISTRIBUSJON;
		};
	}

	private static Kanal mapManglendeUtsendingskanal(ArkivJournalpost arkivJournalpost) {
		try {
			return switch (JournalStatusCode.valueOf(arkivJournalpost.status())) {
				case FL -> Kanal.LOKAL_UTSKRIFT;
				case FS, E -> Kanal.SENTRAL_UTSKRIFT;
				default -> Kanal.UKJENT;
			};
		} catch (IllegalArgumentException e) {
			return Kanal.UKJENT;
		}
	}

	private static String mapAntallRetur(ArkivJournalpost arkivJournalpost) {
		if (U.name().equals(arkivJournalpost.type())) {
			return arkivJournalpost.antallRetur() == null ? null : arkivJournalpost.antallRetur().toString();
		} else {
			return null;
		}
	}

	private static Utsendingsinfo mapUtsendingsInfo(ArkivJournalpost arkivJournalpost) {
		if (U.name().equals(arkivJournalpost.type()) && arkivJournalpost.utsendingskanal() != null) {
			try {
				return mapArkivUtsendingsInfo(arkivJournalpost.utsendingsInfo(), UtsendingsKanalCode.valueOf(arkivJournalpost.utsendingskanal()))
						.orElse(null);
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		return null;
	}

	private static List<DokumentInfo> mapDokumenter(Journalpost journalpost, ArkivJournalpost arkivJournalpost, RequestCache requestCache) {
		if(arkivJournalpost.dokumenter() == null) {
			return List.of();
		}
		return arkivJournalpost.dokumenter().stream()
				.filter(dokumentinfo -> shouldMapDokumentInfo(arkivJournalpost.journalpostId().toString(), dokumentinfo.dokumentInfoId().toString(), requestCache))
				.map(dokumentinfo -> DokumentInfo.builder()
						.dokumentInfoId(dokumentinfo.dokumentInfoId().toString())
						.tittel(dokumentinfo.tittel())
						.brevkode(mapBrevkode(arkivJournalpost, dokumentinfo))
						.dokumentstatus(mapDokumentstatus(dokumentinfo))
						.datoFerdigstilt(mapDokumentFerdigstilt(dokumentinfo))
						.originalJournalpostId(dokumentinfo.originalJournalpostId() == null ? null : dokumentinfo.originalJournalpostId().toString())
						.skjerming(mapSkjerming(dokumentinfo.skjerming()))
						.dokumentvarianter(dokumentinfo.fildetaljer().stream()
								.map(fildetaljer -> mapDokumentvariant(journalpost, requestCache, dokumentinfo, fildetaljer))
								.filter(Objects::nonNull)
								.collect(Collectors.toList()))
						.logiskeVedlegg(mapLogiskeVedlegg(dokumentinfo))
						.build()).toList();
	}

	private static Dokumentvariant mapDokumentvariant(Journalpost journalpost, RequestCache requestCache, ArkivDokumentinfo dokumentinfo, ArkivFildetaljer fildetaljer) {
		Variantformat variantformat = mapVariantformat(fildetaljer);
		if(variantformat == null) {
			return null;
		}
		return Dokumentvariant.builder()
				.saksbehandlerHarTilgang(determineSaksbehandlerTilgang(journalpost, dokumentinfo, fildetaljer, requestCache))
				.variantformat(variantformat)
				.filnavn(fildetaljer.navn())
				.filuuid(fildetaljer.uuid())
				.filtype(mapFiltype(fildetaljer))
				.skjerming(mapSkjerming(fildetaljer.skjerming()))
				.filstoerrelse(mapFilstoerrelse(fildetaljer))
				.build();
	}

	private static List<LogiskVedlegg> mapLogiskeVedlegg(ArkivDokumentinfo dokumentinfo) {
		return dokumentinfo.logiskVedlegg().stream()
				.map(logiskVedlegg -> new LogiskVedlegg(logiskVedlegg.vedleggId().toString(), logiskVedlegg.tittel()))
				.collect(Collectors.toList());
	}

	private static Integer mapFilstoerrelse(ArkivFildetaljer fildetaljer) {
		return isBlank(fildetaljer.stoerrelse()) ? 0 : Integer.parseInt(fildetaljer.stoerrelse());
	}

	private static Variantformat mapVariantformat(ArkivFildetaljer fildetaljer) {
		return VariantFormatCode.toSafVariantformat(fildetaljer.format());
	}

	private static Dokumentstatus mapDokumentstatus(ArkivDokumentinfo dokumentinfo) {
		if (isTrue(dokumentinfo.kassert())) {
			return Dokumentstatus.KASSERT;
		}
		try {
			return dokumentinfo.status() == null ? null : DokumentStatusCode.valueOf(dokumentinfo.status()).toSafDokumentstatus();
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static String mapBrevkode(ArkivJournalpost arkivJournalpost, ArkivDokumentinfo arkivDokumentinfo) {
		Journalposttype journalposttype = mapToJournalpostType(arkivJournalpost.type());
		if (journalposttype == null) {
			return arkivDokumentinfo.brevkode();
		}
		return switch (journalposttype) {
			case U ->
					isBlank(arkivDokumentinfo.dokumenttypeId()) ? arkivDokumentinfo.brevkode() : arkivDokumentinfo.dokumenttypeId();
			case I, N -> arkivDokumentinfo.brevkode();
		};
	}

	private static LocalDateTime mapDokumentFerdigstilt(ArkivDokumentinfo dokumentinfo) {
		return dokumentinfo.ferdigDato() == null ? null : dokumentinfo.ferdigDato().atZoneSameInstant(TIDSSONE_NORGE).toLocalDateTime();
	}

	private static String mapFiltype(ArkivFildetaljer arkivFildetaljer) {
		if (FILTYPE_PDFA.equals(arkivFildetaljer.type())) {
			return FILTYPE_PDF;
		}
		return arkivFildetaljer.type();
	}


	private static boolean determineSaksbehandlerTilgang(Journalpost journalpost, ArkivDokumentinfo arkivDokumentinfo, ArkivFildetaljer arkivFildetaljer, RequestCache requestCache) {
		if (journalpost.getJournalstatus() == Journalstatus.MOTTATT || journalpost.getSak() == null) {
			// Midlertidige journalposter skal ikke ha tilgangskontroll på tema. Her skal saksbehandler ha tilgang uansett.
			// https://jira.adeo.no/browse/MMA-2494
			return getDecisionFromPep6d(journalpost.getJournalpostId(), String.valueOf(arkivDokumentinfo.dokumentInfoId()), arkivFildetaljer, requestCache);
		} else if (journalpost.getTema() == Tema.UKJ) {
			// Når tema=UKJ skal saksbehandler ha tilgang. Kun Pep6d skal bestemme saksbehandlerHarTilgang.
			// https://jira.adeo.no/browse/MMA-3992
			return getDecisionFromPep6d(journalpost.getJournalpostId(), String.valueOf(arkivDokumentinfo.dokumentInfoId()), arkivFildetaljer, requestCache);
		} else {
			return getDecisionFromPep2d(journalpost.getTema(), requestCache) &&
				   getDecisionFromPep6d(journalpost.getJournalpostId(), String.valueOf(arkivDokumentinfo.dokumentInfoId()), arkivFildetaljer, requestCache) &&
				   getDecisionFromPep7d(journalpost.getSak().getArkivsaksystem(), journalpost.getSak().getArkivsaksnummer(), requestCache);
		}
	}

	private static boolean getDecisionFromPep2d(Tema tema, RequestCache requestCache) {
		String tilgangKeyPep2dLocalCaching = getKeyForPep2d(tema);
		return getCachedDecision(requestCache, tilgangKeyPep2dLocalCaching);
	}

	private static boolean getDecisionFromPep6d(String journalpostId, String dokumentInfoId, ArkivFildetaljer arkivFildetaljer, RequestCache requestCache) {
		String variantFormat = arkivFildetaljer.format() == null ? null : arkivFildetaljer.format();
		String skjerming = arkivFildetaljer.skjerming() == null ? null : arkivFildetaljer.skjerming();
		String tilgangKeyPep6dLocalCaching = getKeyForPep6d(journalpostId, dokumentInfoId, variantFormat, skjerming);
		return getCachedDecision(requestCache, tilgangKeyPep6dLocalCaching);
	}

	private static boolean getDecisionFromPep7d(Arkivsakssystem arkivsakssystem, String arkivsaksnummer, RequestCache requestCache) {
		String tilgangKeyPep7dLocalCaching = getKeyForPep7d(arkivsakssystem, arkivsaksnummer);
		return getCachedDecision(requestCache, tilgangKeyPep7dLocalCaching);
	}


	private static boolean shouldMapDokumentInfo(String journalpostId, String dokumentInfoId, RequestCache requestCache) {
		String tilgangKeyPep5LocalCaching = getKeyForPep5(journalpostId, dokumentInfoId);
		return getCachedDecision(requestCache, tilgangKeyPep5LocalCaching);
	}

	private static boolean getCachedDecision(RequestCache requestCache, String tilgangKey) {
		AbacAnswer abacAnswer = requestCache.getCachedDecision(tilgangKey);
		return abacAnswer != null && abacAnswer.isPermit();
	}
}
