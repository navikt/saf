package no.nav.saf.anticorruptionlayer.joark.domain;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.BrukerTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.BrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.DokumentInfoDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.VariantDto;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Datotype;
import no.nav.saf.domain.kode.Dokumentstatus;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Kanal;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.visningsmodell.Bruker;
import no.nav.saf.domain.visningsmodell.BrukerIdType;
import no.nav.saf.domain.visningsmodell.BrukerTilgangAvvistBegrunnelse;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Dokumentvariant;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.LogiskVedlegg;
import no.nav.saf.domain.visningsmodell.RelevantDato;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.domain.visningsmodell.Tilleggsopplysning;
import no.nav.saf.domain.visningsmodell.Utsendingsinfo;
import no.nav.saf.tilgangskontroll.RequestCache;
import no.nav.saf.tilgangskontroll.pep.PepAnswer;
import no.nav.safselvbetjening.tilgang.Ident;
import no.nav.safselvbetjening.tilgang.TilgangDokument;
import no.nav.safselvbetjening.tilgang.TilgangJournalpost;
import no.nav.safselvbetjening.tilgang.TilgangSak;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.valueOf;
import static java.util.Objects.nonNull;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper.ARKIVJOURNALPOST_OVERSTYRTINNSYN_STANDARD;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper.ARKIVJOURNALPOST_OVERSTYRTINNSYN_STANDARD_BESKRIVELSE;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper.SKJULT_TITTEL;
import static no.nav.saf.anticorruptionlayer.joark.TilgangAvvistMapper.mapbrukerTilgangAvvistBegrunnelser;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode.U;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep2d;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep5;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep6d;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep7d;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep8d;
import static no.nav.saf.domain.DomainConstants.TIDSSONE_NORGE;
import static no.nav.saf.domain.kode.Journalstatus.MOTTATT;
import static no.nav.saf.domain.visningsmodell.RelevantDato.INVALID_DATE;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
@Component
public class JournalpostDtoMapper {
	private static final AvsenderMottakerMapper avsenderMottakerMapper = new AvsenderMottakerMapper();
	private static final UtledTilgangService utledTilgangService = new UtledTilgangService(true);
	static final String FILTYPE_PDF = "PDF";
	static final String FILTYPE_PDFA = "PDFA";

	public Journalpost mapJournalpostDto(JournalpostDto journalpostDto, Set<Ident> brukerIdenter, RequestCache requestCache) {
		if (journalpostDto == null) {
			return null;
		}
		final Kanal kanal = mapKanal(journalpostDto);
		final String journalpostId = journalpostDto.getJournalpostId().toString();

		Tema tema = mapTema(journalpostDto, requestCache);
		Journalstatus journalstatus = mapJournalstatus(journalpostDto);
		TilgangSak tilgangSak = mapTilgangSak(journalpostDto.getSaksrelasjon(), requestCache);
		TilgangJournalpost tilgangJournalpost = journalpostDto.getJournalpostTilgang(tilgangSak);
		List<BrukerTilgangAvvistBegrunnelse> brukerTilgangAvvistBegrunnelser = mapbrukerTilgangAvvistBegrunnelser(utledTilgangService.utledTilgangJournalpost(tilgangJournalpost, brukerIdenter));
		Sak sak = mapSak(journalpostDto.getSaksrelasjon(), requestCache);
		Journalpost journalpost = Journalpost.builder()
				.journalpostId(journalpostId)
				.tittel(mapTittel(journalpostDto.getInnhold(), tema, journalstatus, sak, requestCache))
				.journalposttype(JournalpostTypeCode.mapToJournalpostType(journalpostDto.getJournalposttype()))
				.journalstatus(journalstatus)
				.tema(tema)
				.temanavn(tema.getTemanavn())
				.behandlingstema(journalpostDto.getBehandlingstema())
				.behandlingstemanavn(journalpostDto.getBehandlingstemanavn())
				.sak(sak)
				.bruker(mapBruker(journalpostDto.getBruker(), journalpostDto.getSaksrelasjon(), requestCache))
				.avsenderMottaker(avsenderMottakerMapper.map(journalpostDto))
				.avsenderMottakerId(journalpostDto.getAvsenderMottakerId())
				.avsenderMottakerNavn(journalpostDto.getAvsenderMottakerNavn())
				.avsenderMottakerLand(journalpostDto.getAvsenderMottakerLand())
				.journalforendeEnhet(journalpostDto.getJournalforendeEnhet())
				.journalfoerendeEnhet(journalpostDto.getJournalforendeEnhet())
				.journalfortAvNavn(journalpostDto.getJournalfortAvNavn())
				.opprettetAvNavn(journalpostDto.getOpprettetAvNavn())
				.kanal(kanal)
				.kanalnavn(kanal == null ? null : kanal.getKanalnavn())
				.skjerming(journalpostDto.getSkjerming() == null ? null : journalpostDto.getSkjerming()
						.getSafSkjerming())
				.datoOpprettet(journalpostDto.getDatoOpprettet() == null ? INVALID_DATE : LocalDateTime.from(journalpostDto.getDatoOpprettet()
						.toInstant()
						.atZone(TIDSSONE_NORGE)))
				.relevanteDatoer(mapRelevanteDatoer(journalpostDto))
				.tilleggsopplysninger(mapTilleggsopplysninger(journalpostDto))
				.antallRetur(mapAntallRetur(journalpostDto))
				.eksternReferanseId(journalpostDto.getKanalReferanseId())
				.innsynsregel(journalpostDto.getInnsyn() == null ? ARKIVJOURNALPOST_OVERSTYRTINNSYN_STANDARD : journalpostDto.getInnsyn())
				.innsynsregelBeskrivelse(journalpostDto.getInnsynbeskrivelse() == null ? ARKIVJOURNALPOST_OVERSTYRTINNSYN_STANDARD_BESKRIVELSE : journalpostDto.getInnsynbeskrivelse())
				.utsendingsinfo(getUtgaaendeJournalpostUtsendingsInfo(journalpostDto))
				.brukerTilgangAvvistBegrunnelser(brukerTilgangAvvistBegrunnelser)
				.brukerHarTilgang(brukerTilgangAvvistBegrunnelser.isEmpty())
				.build();
		List<DokumentInfo> dokumenter = journalpostDto.getDokumenter().stream()
				.filter(dokumentInfoDto -> shouldMapDokumentInfo(journalpostId, dokumentInfoDto.getDokumentInfoId(), requestCache))
				.map(dokumentInfoDto -> DokumentInfo.builder()
						.parent(journalpost)
						.dokumentInfoId(dokumentInfoDto.getDokumentInfoId())
						.tittel(mapTittel(dokumentInfoDto.getTittel(), tema, journalstatus, sak, requestCache))
						.brevkode(mapBrevkode(journalpostDto, dokumentInfoDto))
						.dokumentstatus(mapDokumentstatus(dokumentInfoDto))
						.datoFerdigstilt(dokumentInfoDto.getDatoFerdigstilt() == null ? null :
								LocalDateTime.from(dokumentInfoDto.getDatoFerdigstilt()
										.toInstant()
										.atZone(TIDSSONE_NORGE)))
						.originalJournalpostId(dokumentInfoDto.getOrigJournalpostId() == null ? null : dokumentInfoDto.getOrigJournalpostId()
								.toString())
						.skjerming(dokumentInfoDto.getSkjerming() == null ? null : dokumentInfoDto.getSkjerming()
								.getSafSkjerming()
						)
						.dokumentvarianter(dokumentInfoDto.getVarianter().stream()
								.map(variantDto -> {
									TilgangDokument tilgangDokument = tilgangJournalpost.getDokumenter().stream().filter(dok -> dok.id() == Long.parseLong(dokumentInfoDto.getDokumentInfoId())).findFirst().orElse(null);
									List<BrukerTilgangAvvistBegrunnelse> variantBrukerTilgangAvvistBegrunnelser = mapbrukerTilgangAvvistBegrunnelser(utledTilgangService.utledTilgangDokument(tilgangJournalpost, tilgangDokument, variantDto.getTilgangVariant(), brukerIdenter));
									return Dokumentvariant.builder()
											.saksbehandlerHarTilgang(determineSaksbehandlerTilgang(journalpost, dokumentInfoDto, variantDto, requestCache))
											.variantformat(variantDto.getVariantf().getSafVariantformat())
											.filnavn(variantDto.getFilnavn())
											.filuuid(variantDto.getFiluuid())
											.filtype(mapFiltype(variantDto))
											.skjerming(variantDto.getSkjerming() == null ? null : variantDto.getSkjerming()
													.getSafSkjerming())
											.filstoerrelse(isBlank(variantDto.getFilstorrelse()) ? 0 : valueOf(variantDto.getFilstorrelse()))
											.brukerTilgangAvvistBegrunnelser(variantBrukerTilgangAvvistBegrunnelser)
											.brukerHarTilgang(variantBrukerTilgangAvvistBegrunnelser.isEmpty())
											.build();
								})
								.collect(Collectors.toList()))
						.logiskeVedlegg(dokumentInfoDto.getLogiske().stream()
								.map(logiskVedleggDto -> new LogiskVedlegg(logiskVedleggDto.getVedleggId(), mapTittel(logiskVedleggDto.getTittel(), tema, journalstatus, sak, requestCache)))
								.collect(Collectors.toList()))
						.build()).toList();
		journalpost.getDokumenter().addAll(dokumenter);
		return journalpost;
	}

	private String mapTittel(String originalTittel, Tema tema, Journalstatus journalstatus, Sak sak, RequestCache requestCache) {
		if (requestCache.isSystem() || journalstatus == MOTTATT || getDecisionFromPep2d(tema, requestCache) && (sak == null || getDecisionFromPep8d(sak, requestCache))) {
			return originalTittel;
		}
		return SKJULT_TITTEL;
	}

	private String mapFiltype(VariantDto variantDto) {
		if (FILTYPE_PDFA.equals(variantDto.getFiltype())) {
			return FILTYPE_PDF;
		}
		return variantDto.getFiltype();
	}

	private String mapAntallRetur(JournalpostDto journalpostDto) {
		if (U.equals(journalpostDto.getJournalposttype())) {
			return journalpostDto.getAntallRetur();
		} else {
			return null;
		}
	}

	private String mapBrevkode(JournalpostDto journalpostDto, DokumentInfoDto dokumentInfoDto) {
		return switch (journalpostDto.getJournalposttype()) {
			case U ->
					isBlank(dokumentInfoDto.getDokumenttypeId()) ? dokumentInfoDto.getBrevkode() : dokumentInfoDto.getDokumenttypeId();
			case I, N -> dokumentInfoDto.getBrevkode();
		};
	}

	private Tema mapTema(JournalpostDto journalpostDto, RequestCache requestCache) {
		if (journalpostDto.isTilknyttetSak()) {
			SaksrelasjonDto saksrelasjon = journalpostDto.getSaksrelasjon();
			Arkivsak arkivsak = requestCache.getArkivsak(saksrelasjon);
			if (arkivsak == null) {
				// For journalposter som mangler saksrelasjon, er gjeldende tema lik Journalpost.fagomrade.
				return FagomradeCode.toSafTema(journalpostDto.getFagomrade());
			}
			if (arkivsak.getTema() == null) {
				// For journalposter som ikke har tema på sak (f.eks ugyldig sakId), så er gjeldende tema lik Journalpost.fagomrade.
				return FagomradeCode.toSafTema(journalpostDto.getFagomrade());
			} else {
				// For sakstilknyttede journalposter hentes tema fra arkivsaken (GSAK eller PSAK sak), altså ikke fra joark.
				return arkivsak.getTema();
			}
		} else {
			// For journalposter som mangler saksrelasjon, er gjeldende tema lik Journalpost.fagomrade.
			return FagomradeCode.toSafTema(journalpostDto.getFagomrade());
		}
	}

	private Bruker mapBruker(BrukerDto brukerDto, SaksrelasjonDto saksrelasjon, RequestCache requestCache) {
		if (saksrelasjon == null) {
			// Fall tilbake på bruker på Journalpost
			return mapBrukerDtoToBruker(brukerDto);
		}

		Bruker bruker = getBrukerFromArkivsakCache(saksrelasjon, requestCache);
		if (bruker != null) {
			return bruker;
		} else {
			return getBrukerFromTilgangBrukerCache(requestCache);
		}
	}

	private Bruker mapBrukerDtoToBruker(BrukerDto brukerDto) {
		if (brukerDto == null) {
			return null;
		}

		final String brukerId = brukerDto.getBrukerId();
		final String brukerType = brukerDto.getBrukerIdType();
		if (isBlank(trim(brukerDto.getBrukerId())) || isBlank(brukerType)) {
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

	private Sak mapSak(SaksrelasjonDto saksrelasjon, RequestCache requestCache) {
		if (saksrelasjon == null) {
			return null;
		} else {
			Arkivsak arkivsak = requestCache.getArkivsak(saksrelasjon);
			if (arkivsak == null) {
				return null;
			}
			return Sak.builder()
					.arkivsaksnummer(arkivsak.getArkivsaksnummer())
					.arkivsaksystem(arkivsak.getArkivsaksystem())
					.fagsakId(arkivsak.getFagsakId())
					.fagsaksystem(arkivsak.getFagsaksystem())
					.datoOpprettet(arkivsak.getDatoOpprettet())
					.sakstype(Sakstype.fromFagsaksystem(arkivsak.getFagsaksystem()))
					.tema(arkivsak.getTema())
					.build();
		}
	}

	public static TilgangSak mapTilgangSak(SaksrelasjonDto saksrelasjon, RequestCache requestCache) {
		if (saksrelasjon == null) {
			return null;
		}

		if (saksrelasjon.getFagsystem() == FagsystemCode.PEN) {
			Arkivsak arkivsak = requestCache.getArkivsak(saksrelasjon);
			if (arkivsak == null) {
				return null;
			}
			return TilgangSak.builder()
					.feilregistrert(saksrelasjon.getFeilregistrert() != null && saksrelasjon.getFeilregistrert())
					.tema(arkivsak.getTema() != null ? arkivsak.getTema().name() : null)
					.ident(Ident.of(arkivsak.getAktoerId()))
					.build();
		} else {
			Arkivsak arkivsak = requestCache.getArkivsak(saksrelasjon);
			if (arkivsak == null) {
				return TilgangSak.builder()
						.feilregistrert(saksrelasjon.getFeilregistrert() != null && saksrelasjon.getFeilregistrert())
						.tema(saksrelasjon.getTema())
						.ident(Ident.ofNullable(saksrelasjon.getAktoerId()))
						.build();
			} else {
				return TilgangSak.builder()
						.feilregistrert(saksrelasjon.getFeilregistrert() != null && saksrelasjon.getFeilregistrert())
						.tema(arkivsak.getTema() != null ? arkivsak.getTema().name() : saksrelasjon.getTema())
						.ident(Ident.ofNullable(findFirstNonNull(arkivsak.getAktoerId(), saksrelasjon.getAktoerId())))
						.build();
			}
		}
	}

	private Journalstatus mapJournalstatus(JournalpostDto journalpostDto) {
		SaksrelasjonDto saksrelasjon = journalpostDto.getSaksrelasjon();
		if (saksrelasjon != null && saksrelasjon.getFeilregistrert() != null && saksrelasjon.getFeilregistrert()
				&& !Journalstatus.UTGAAR.equals(journalpostDto.getJournalstatus().toSafJournalstatus())) {
			return Journalstatus.FEILREGISTRERT;
		} else {
			return journalpostDto.getJournalstatus().toSafJournalstatus();
		}
	}

	private Dokumentstatus mapDokumentstatus(DokumentInfoDto dokumentInfoDto) {
		if (isTrue(dokumentInfoDto.getKassert())) {
			return Dokumentstatus.KASSERT;
		}

		return dokumentInfoDto.getDokumentstatus() == null ? null : dokumentInfoDto.getDokumentstatus().toSafDokumentstatus();
	}

	private List<Tilleggsopplysning> mapTilleggsopplysninger(JournalpostDto journalpostDto) {
		if (journalpostDto.getTilleggsopplysninger() == null || journalpostDto.getTilleggsopplysninger().isEmpty()) {
			return new ArrayList<>();
		}
		return journalpostDto.getTilleggsopplysninger().stream()
				.map(dto -> new Tilleggsopplysning(dto.getNokkel(), dto.getVerdi()))
				.collect(Collectors.toList());
	}

	private List<RelevantDato> mapRelevanteDatoer(JournalpostDto journalpostDto) {
		List<RelevantDato> relevanteDatoer = new ArrayList<>();
		if (journalpostDto.getDokumentDato() != null) {
			relevanteDatoer.add(new RelevantDato(journalpostDto.getDokumentDato(), Datotype.DATO_DOKUMENT));
		}
		if (journalpostDto.getJournalDato() != null) {
			relevanteDatoer.add(new RelevantDato(journalpostDto.getJournalDato(), Datotype.DATO_JOURNALFOERT));
		}
		switch (journalpostDto.getJournalposttype()) {
			case I:
				if (journalpostDto.getMottattDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getMottattDato(), Datotype.DATO_REGISTRERT));
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
				if (journalpostDto.getLestDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getLestDato(), Datotype.DATO_LEST));
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
					return Kanal.UKJENT;
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
		return switch (journalpostDto.getJournalstatus()) {
			case FL -> Kanal.LOKAL_UTSKRIFT;
			case FS, E -> Kanal.SENTRAL_UTSKRIFT;
			default -> null;
		};
	}

	// journalposten er endelig journalført
	private Bruker getBrukerFromArkivsakCache(SaksrelasjonDto saksrelasjon, RequestCache requestCache) {
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
	private Bruker getBrukerFromTilgangBrukerCache(RequestCache requestCache) {
		return requestCache.getTilgangBruker()
				.map(tilgangBruker -> {
					if (tilgangBruker.isPerson()) {
						return new Bruker(tilgangBruker.getAktoerId(), BrukerIdType.AKTOERID);
					} else if (tilgangBruker.isOrganisasjon()) {
						return new Bruker(tilgangBruker.getOrgnummer(), BrukerIdType.ORGNR);
					} else {
						return null;
					}
				}).orElse(null);
	}

	private Utsendingsinfo getUtgaaendeJournalpostUtsendingsInfo(JournalpostDto journalpostDto) {
		if (U.equals(journalpostDto.getJournalposttype()) && nonNull(journalpostDto.getUtsendingskanal())) {
			return UtsendingsInfoMapper.mapUtsendingsInfo(journalpostDto.getUtsendingsInfo(), journalpostDto.getUtsendingskanal())
					.orElse(null);
		}
		return null;
	}

	private boolean determineSaksbehandlerTilgang(Journalpost journalpost, DokumentInfoDto dokumentInfoDto, VariantDto variantDto, RequestCache requestCache) {
		if (journalpost.getSak() == null) {
			// Midlertidige journalposter skal ikke ha tilgangskontroll på tema. Her skal saksbehandler ha tilgang uansett.
			// https://jira.adeo.no/browse/MMA-2494
			return getDecisionFromPep6d(journalpost.getJournalpostId(), dokumentInfoDto.getDokumentInfoId(), variantDto, requestCache);
		} else if (journalpost.getJournalstatus() == MOTTATT) {
			// Midlertidige journalposter skal ikke ha tilgangskontroll på tema. Her skal saksbehandler ha tilgang uansett.
			// https://jira.adeo.no/browse/MMA-2494
			return getDecisionFromPep6d(journalpost.getJournalpostId(), dokumentInfoDto.getDokumentInfoId(), variantDto, requestCache) &&
					getDecisionFromPep8d(journalpost.getSak(), requestCache);
		} else {
			if (journalpost.getTema() == Tema.UKJ) {
				// Når tema=UKJ og sak er åpen skal saksbehandler ha tilgang. Kun Pep6d og Pep8d skal bestemme saksbehandlerHarTilgang.
				// https://jira.adeo.no/browse/MMA-3992
				return getDecisionFromPep6d(journalpost.getJournalpostId(), dokumentInfoDto.getDokumentInfoId(), variantDto, requestCache) &&
						getDecisionFromPep8d(journalpost.getSak(), requestCache);
			} else {
				return getDecisionFromPep2d(journalpost.getTema(), requestCache) &&
						getDecisionFromPep6d(journalpost.getJournalpostId(), dokumentInfoDto.getDokumentInfoId(), variantDto, requestCache) &&
						getDecisionFromPep7d(journalpost.getSak().getArkivsaksystem(), journalpost.getSak().getArkivsaksnummer(), requestCache) &&
						getDecisionFromPep8d(journalpost.getSak(), requestCache);
			}
		}
	}

	private static boolean getDecisionFromPep2d(Tema tema, RequestCache requestCache) {
		String tilgangKeyPep2dLocalCaching = getKeyForPep2d(tema);
		return getCachedDecision(requestCache, tilgangKeyPep2dLocalCaching);
	}

	private static boolean getDecisionFromPep6d(String journalpostId, String dokumentInfoId, VariantDto variantDto, RequestCache requestCache) {
		String tilgangKeyPep6dLocalCaching = getKeyForPep6d(
				journalpostId, dokumentInfoId, variantDto.getVariantf() == null ? null : variantDto.getVariantf()
						.getSafVariantformat().name(), variantDto.getSkjerming() == null ? null : variantDto.getSkjerming()
						.getSafSkjerming().name());
		return getCachedDecision(requestCache, tilgangKeyPep6dLocalCaching);
	}

	private static boolean getDecisionFromPep7d(Arkivsakssystem arkivsakssystem, String arkivsaksnummer, RequestCache requestCache) {
		String tilgangKeyPep7dLocalCaching = getKeyForPep7d(arkivsakssystem, arkivsaksnummer);
		return getCachedDecision(requestCache, tilgangKeyPep7dLocalCaching);
	}

	private static boolean getDecisionFromPep8d(Sak sak, RequestCache requestCache) {
		String tilgangKeyPep8dLocalCaching = getKeyForPep8d(sak.getArkivsaksystem(), sak.getArkivsaksnummer());
		return getCachedDecision(requestCache, tilgangKeyPep8dLocalCaching);
	}

	private static boolean shouldMapDokumentInfo(String journalpostId, String dokumentInfoId, RequestCache requestCache) {
		String tilgangKeyPep5LocalCaching = getKeyForPep5(journalpostId, dokumentInfoId);
		return getCachedDecision(requestCache, tilgangKeyPep5LocalCaching);
	}

	private static boolean getCachedDecision(RequestCache requestCache, String tilgangKey) {
		PepAnswer pepAnswer = requestCache.getCachedDecision(tilgangKey);
		return pepAnswer != null && pepAnswer.isPermit();
	}

	private static String findFirstNonNull(String... str) {
		return Stream.of(str).filter(Objects::nonNull).findFirst().orElse(null);
	}
}
