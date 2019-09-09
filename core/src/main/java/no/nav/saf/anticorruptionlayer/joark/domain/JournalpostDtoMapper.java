package no.nav.saf.anticorruptionlayer.joark.domain;

import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep2d;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep5;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep6d;
import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static no.nav.saf.domain.visningsmodell.RelevantDato.INVALID_DATE;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.AvsenderMottakerIdTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.BrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.DokumentInfoDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.VariantDto;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.DomainConstants;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Datotype;
import no.nav.saf.domain.kode.Dokumentstatus;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Kanal;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.visningsmodell.AvsenderMottaker;
import no.nav.saf.domain.visningsmodell.AvsenderMottakerIdType;
import no.nav.saf.domain.visningsmodell.Bruker;
import no.nav.saf.domain.visningsmodell.BrukerIdType;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Dokumentvariant;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.LogiskVedlegg;
import no.nav.saf.domain.visningsmodell.RelevantDato;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.domain.visningsmodell.Tilleggsopplysning;
import no.nav.saf.tilgangskontroll.RequestCache;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class JournalpostDtoMapper {

	public Journalpost mapJournalpostDto(final JournalpostDto journalpostDto, final RequestCache requestCache) {
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
				.tema(mapTema(journalpostDto, requestCache))
				.temanavn(mapTema(journalpostDto, requestCache).getTemanavn())
				.behandlingstema(journalpostDto.getBehandlingstema())
				.behandlingstemanavn(journalpostDto.getBehandlingstemanavn())
				.sak(mapSak(journalpostDto.getSaksrelasjon(), requestCache))
				.bruker(mapBruker(journalpostDto.getBruker(), journalpostDto.getSaksrelasjon(), requestCache))
				.avsenderMottaker(mapAvsenderMottaker(journalpostDto))
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
						.atZone(ZoneId.systemDefault())))
				.relevanteDatoer(mapRelevanteDatoer(journalpostDto))
				.tilleggsopplysninger(mapTilleggsopplysninger(journalpostDto))
				.antallRetur(mapAntallRetur(journalpostDto))
				.build();
		List<DokumentInfo> dokumenter = journalpostDto.getDokumenter().stream()
				.filter(dokumentInfoDto -> shouldMapDokumentInfo(journalpostId, dokumentInfoDto.getDokumentInfoId(), requestCache))
				.map(dokumentInfoDto -> DokumentInfo.builder()
						.parent(journalpost)
						.dokumentInfoId(dokumentInfoDto.getDokumentInfoId())
						.tittel(dokumentInfoDto.getTittel())
						.brevkode(mapBrevkode(journalpostDto, dokumentInfoDto))
						.dokumentstatus(mapDokumentstatus(dokumentInfoDto))
						.datoFerdigstilt(dokumentInfoDto.getDatoFerdigstilt() == null ? null :
								LocalDateTime.from(dokumentInfoDto.getDatoFerdigstilt()
										.toInstant()
										.atZone(ZoneId.systemDefault())))
						.originalJournalpostId(dokumentInfoDto.getOrigJournalpostId() == null ? null : dokumentInfoDto.getOrigJournalpostId()
								.toString())
						.skjerming(dokumentInfoDto.getSkjerming() == null ? null : dokumentInfoDto.getSkjerming()
								.getSafSkjerming()
						)
						.dokumentvarianter(dokumentInfoDto.getVarianter().stream()
								.map(variantDto -> Dokumentvariant.builder()
										.saksbehandlerHarTilgang(determineSaksbehandlerTilgang(journalpost, dokumentInfoDto, variantDto, requestCache))
										.variantformat(variantDto.getVariantf().getSafVariantformat())
										.filnavn(variantDto.getFilnavn())
										.filuuid(variantDto.getFiluuid())
										.filtype(variantDto.getFiltype())
										.skjerming(variantDto.getSkjerming() == null ? null : variantDto.getSkjerming()
												.getSafSkjerming())
										.build())
								.collect(Collectors.toList()))
						.logiskeVedlegg(dokumentInfoDto.getLogiske().stream()
								.map(logiskVedleggDto -> new LogiskVedlegg(logiskVedleggDto.getVedleggId(), logiskVedleggDto.getTittel()))
								.collect(Collectors.toList()))
						.build()).collect(Collectors.toList());
		journalpost.getDokumenter().addAll(dokumenter);
		return journalpost;
	}

	private String mapAntallRetur(JournalpostDto journalpostDto) {
		if (JournalpostTypeCode.U.equals(journalpostDto.getJournalposttype())) {
			return journalpostDto.getAntallRetur();
		} else {
			return null;
		}
	}

	private String mapBrevkode(JournalpostDto journalpostDto, DokumentInfoDto dokumentInfoDto) {
		switch (journalpostDto.getJournalposttype()) {
			case U:
				return isBlank(dokumentInfoDto.getDokumenttypeId()) ? dokumentInfoDto.getBrevkode() : dokumentInfoDto.getDokumenttypeId();
			case I:
			case N:
			default:
				return dokumentInfoDto.getBrevkode();
		}
	}

	private Tema mapTema(JournalpostDto journalpostDto, RequestCache requestCache) {
		if (journalpostDto.isTilknyttetSak()) {
			SaksrelasjonDto saksrelasjon = journalpostDto.getSaksrelasjon();
			Arkivsak arkivsak = requestCache.getObject(saksrelasjon.getSakId() + mapJoarkFagsystem(saksrelasjon.getFagsystem()));
			if (arkivsak == null) {
				// For journalposter som mangler saksrelasjon, er gjeldende tema lik Journalpost.fagomrade.
				return FagomradeCode.toSafTema(journalpostDto.getFagomrade());
			}
			// For sakstilknyttede journalposter hentes tema fra arkivsaken (GSAK eller PSAK sak), altså ikke fra joark.
			return arkivsak.getTema();
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

		if (brukerType.equals(DomainConstants.PERSON)) {
			return new Bruker(brukerId, BrukerIdType.FNR);
		} else if (brukerType.equals(DomainConstants.ORGANISASJON)) {
			return new Bruker(brukerId, BrukerIdType.ORGNR);
		} else {
			return null;
		}
	}

	private AvsenderMottaker mapAvsenderMottaker(JournalpostDto journalpostDto) {
		return AvsenderMottaker.builder()
				.id(journalpostDto.getAvsenderMottakerId())
				.type(mapAvsenderMottakerIdType(journalpostDto.getAvsenderMottakerId(), journalpostDto.getAvsenderMottakerIdType()))
				.navn(journalpostDto.getAvsenderMottakerNavn())
				.land(journalpostDto.getAvsenderMottakerLand())
				.erLikBruker(mapErLikBruker(journalpostDto.getAvsenderMottakerId(), journalpostDto.getBruker()))
				.build();
	}

	private AvsenderMottakerIdType mapAvsenderMottakerIdType(String avsenderMottakerId, AvsenderMottakerIdTypeCode avsenderMottakerIdTypeCode) {
		AvsenderMottakerIdType avsenderMottakerIdType;
		if (avsenderMottakerIdTypeCode != null) {
			switch (avsenderMottakerIdTypeCode) {
				case FNR:
					avsenderMottakerIdType = AvsenderMottakerIdType.FNR;
					break;
				case ORGNR:
					avsenderMottakerIdType = AvsenderMottakerIdType.ORGNR;
					break;
				case HPRNR:
					avsenderMottakerIdType = AvsenderMottakerIdType.HPRNR;
					break;
				case UTL_ORG:
					avsenderMottakerIdType = AvsenderMottakerIdType.UTL_ORG;
					break;
				default:
					avsenderMottakerIdType = AvsenderMottakerIdType.UKJENT;
					break;
			}

		} else {
			if (avsenderMottakerId == null) {
				return AvsenderMottakerIdType.NULL;
			} else {
				switch (avsenderMottakerId.length()) {
					case 11:
						avsenderMottakerIdType = AvsenderMottakerIdType.FNR;
						break;
					case 9:
						avsenderMottakerIdType = AvsenderMottakerIdType.ORGNR;
						break;
					default:
						avsenderMottakerIdType = AvsenderMottakerIdType.UKJENT;
						break;
				}
			}
		}
		return avsenderMottakerIdType;

	}

	private boolean mapErLikBruker(String avsenderMottakerId, BrukerDto brukerDto) {
		if (avsenderMottakerId == null || brukerDto == null) {
			return false;
		}
		return avsenderMottakerId.equals(brukerDto.getBrukerId());
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
					.fagsakId(arkivsak.getFagsakId())
					.fagsaksystem(arkivsak.getFagsaksystem())
					.datoOpprettet(arkivsak.getDatoOpprettet())
					.build();
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
		if (joarkFagsystem == null) {
			return null;
		}
		switch (joarkFagsystem) {
			case PEN:
				return Arkivsakssystem.PSAK.name();
			case FS22:
				return Arkivsakssystem.GSAK.name();
			default:
				log.warn("Forventet joarkFagsystem er (FS22) GSAK eller (PEN) PSAK");
				return null;
		}
	}

	// journalposten er endelig journalført
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

	// journalposten er midlertidig journalført
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

	private boolean determineSaksbehandlerTilgang(Journalpost journalpost, DokumentInfoDto dokumentInfoDto, VariantDto variantDto, RequestCache requestCache) {
		if (journalpost.getJournalstatus() == Journalstatus.MOTTATT) {
			// Midlertidige journalposter skal ikke ha tilgangskontroll på tema. Her skal saksbehandler ha tilgang uansett.
			// https://jira.adeo.no/browse/MMA-2494
			return getDecisionFromPep6d(journalpost.getJournalpostId(), dokumentInfoDto.getDokumentInfoId(), variantDto, requestCache);
		} else {
			return getDecisionFromPep2d(journalpost.getTema(), requestCache) &&
					getDecisionFromPep6d(journalpost.getJournalpostId(), dokumentInfoDto.getDokumentInfoId(), variantDto, requestCache);
		}
	}

	private boolean getDecisionFromPep2d(Tema tema, RequestCache requestCache) {
		String tilgangKeyPep2dLocalCaching = getKeyForPep2d(tema.name());
		return getCachedDecision(requestCache, tilgangKeyPep2dLocalCaching);
	}

	private boolean getDecisionFromPep6d(String journalpostId, String dokumentInfoId, VariantDto variantDto, RequestCache requestCache) {
		String tilgangKeyPep6dLocalCaching = getKeyForPep6d(
				journalpostId, dokumentInfoId, variantDto.getVariantf() == null ? null : variantDto.getVariantf()
						.getSafVariantformat().name(), variantDto.getSkjerming() == null ? null : variantDto.getSkjerming()
						.getSafSkjerming().name());
		return getCachedDecision(requestCache, tilgangKeyPep6dLocalCaching);
	}

	private boolean shouldMapDokumentInfo(String journalpostId, String dokumentInfoId, RequestCache requestCache) {
		String tilgangKeyPep5LocalCaching = getKeyForPep5(journalpostId, dokumentInfoId);
		return getCachedDecision(requestCache, tilgangKeyPep5LocalCaching);
	}

	private boolean getCachedDecision(RequestCache requestCache, String tilgangKey) {
		return requestCache.getObject(tilgangKey) == null ? false : requestCache.getObject(tilgangKey);
	}
}
