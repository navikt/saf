package no.nav.saf.query.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.BrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.DokumentInfoDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.VariantDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark902.HentJournalpostResponseTo;
import no.nav.saf.anticorruptionlayer.joark.safintern.DokarkivConsumer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.UgyldigArkivsaksystemException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.FS22;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.PEN;
import static no.nav.saf.domain.DomainConstants.ORGANISASJON;
import static no.nav.saf.domain.DomainConstants.PERSON;
import static no.nav.saf.domain.DomainConstants.RJOARK902_JOURNALPOST_DTO;
import static no.nav.saf.domain.DomainConstants.TIDSSONE_NORGE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
@Component
class JournalpostAntiCorruptionLayer {

	private final HentJournalsakinfo hentJournalsakinfo;
	private final DokarkivConsumer dokarkivConsumer;

	JournalpostAntiCorruptionLayer(HentJournalsakinfo hentJournalsakinfo,
								   DokarkivConsumer dokarkivConsumer) {
		this.hentJournalsakinfo = hentJournalsakinfo;
		this.dokarkivConsumer = dokarkivConsumer;
	}

	public ArkivJournalpost hentJournalpost(String journalpostId, String eksternReferanseId) {
		if(isNotBlank(journalpostId)) {
			return dokarkivConsumer.journalpostById(journalpostId, Set.of());
		} else {
			return dokarkivConsumer.journalpostByEksternReferanseId(eksternReferanseId, Set.of());
		}
	}

	public TilgangJournalpost hentTilgangJournalpostFromSafRequestContext(SafRequestContext safRequestContext) {
		JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(RJOARK902_JOURNALPOST_DTO);
		if (journalpostDto == null) {
			return null;
		} else {
			return mapTilgangJournalpost(journalpostDto);
		}
	}

	public TilgangSak hentTilgangSakFromSafRequestContext(SafRequestContext safRequestContext, TilgangBruker tilgangBruker) {
		final JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(RJOARK902_JOURNALPOST_DTO);
		if (journalpostDto == null) {
			throw new IllegalStateException("journalpost ligger ikke i requestCache. Dette er en ugyldig tilstand og en teknisk feil");
		} else if (journalpostDto.isTilknyttetSak() && tilgangBruker != null) {
			return TilgangSak.builder()
					.foedselsnummer(tilgangBruker.getFoedselsnr())
					.arkivsaksnummer(journalpostDto.getSaksrelasjon().getSakId())
					.arkivsaksystem(mapJoarkFagsystemToArkivsakssystemCode(journalpostDto.getSaksrelasjon()
							.getFagsystem(), journalpostDto.getJournalpostId()))
					.tema(FagomradeCode.toSafTema(journalpostDto.getFagomrade()))
					.relevanteTredjeparter(new ArrayList<>())
					.build();
		} else {
			log.info("Journalposten har ingen sakstilknytning. journalpostId={}", journalpostDto.getJournalpostId());
			return TilgangSak.builder()
					.tema(FagomradeCode.toSafTema(journalpostDto.getFagomrade()))
					.build();
		}
	}

	public TilgangBruker hentTilgangBruker(SafRequestContext safRequestContext) {
		final JournalpostDto tilgangJournalpostDto = safRequestContext.getRequestCache()
				.getObject(RJOARK902_JOURNALPOST_DTO);

		if (tilgangJournalpostDto == null || tilgangJournalpostDto.getBruker() == null
				|| tilgangJournalpostDto.getBruker().getBrukerIdType() == null) {
			return null;
		}
		final BrukerDto tilgangBruker = tilgangJournalpostDto.getBruker();
		switch (tilgangBruker.getBrukerIdType()) {
			case PERSON:
				return TilgangBruker.builder()
						.foedselsnr(tilgangBruker.getBrukerId())
						.build();
			case ORGANISASJON:
				return TilgangBruker.builder()
						.orgnummer(trim(tilgangBruker.getBrukerId()))
						.build();
			default:
				log.warn("Forventet brukerType=(PERSON, ORGANISASJON) for midlertidig journalpost med journalpostId={}. Fikk brukerType={}", tilgangJournalpostDto
						.getJournalpostId(), tilgangBruker.getBrukerIdType());
				return null;
		}
	}

	public Arkivsak hentArkivsakAndCacheJournalpostDto(String journalpostId, String eksternReferanseId, SafRequestContext safRequestContex) {
		HentJournalpostResponseTo hentJournalpostResponseTo;
		if (isBlank(journalpostId) && isNotBlank(eksternReferanseId)) {
			hentJournalpostResponseTo = hentJournalsakinfo.hentJournalpostByEksternReferanseId(eksternReferanseId);
		} else {
			hentJournalpostResponseTo = hentJournalsakinfo.hentJournalpost(Long.valueOf(journalpostId));
		}

		JournalpostDto hentJournalpostDto = hentJournalpostResponseTo.getHentJournalpostDto();
		safRequestContex.getRequestCache().putObject(RJOARK902_JOURNALPOST_DTO, hentJournalpostDto);
		SaksrelasjonDto saksrelasjon = hentJournalpostDto.getSaksrelasjon();
		// Journalpost sannsynligvis midlertidig uten saksrelasjon
		if (hentJournalpostDto.isTilknyttetSak()) {
			return Arkivsak.builder()
					.arkivsaksnummer(saksrelasjon.getSakId())
					.arkivsaksystem(mapJoarkFagsystemToArkivsakssystemCode(saksrelasjon.getFagsystem(), hentJournalpostDto
							.getJournalpostId()))
					.fagsaksystem(saksrelasjon.getApplikasjon())
					.fagsakId(saksrelasjon.getFagsakNr())
					.orgnummer(saksrelasjon.getOrgnr())
					.aktoerId(saksrelasjon.getAktoerId())
					.tema(Arkivsak.mapTema(saksrelasjon.getTema()))
					.datoOpprettet(Optional.ofNullable(saksrelasjon.getOpprettetTidspunkt())
							.map(o -> o.atZoneSameInstant(TIDSSONE_NORGE))
							.map(ZonedDateTime::toLocalDateTime)
							.orElse(null))
					.build();
		} else {
			return Arkivsak.builder()
					.tema(FagomradeCode.toSafTema(hentJournalpostDto.getFagomrade()))
					.build();
		}

	}

	private Arkivsakssystem mapJoarkFagsystemToArkivsakssystemCode(FagsystemCode joarkFagsystem, Long journalpostId) {
		if (joarkFagsystem == null || journalpostId == null) {
			return null;
		}
		return mapJoarkFagsystemToArkivsakssystemCode(joarkFagsystem.name(), journalpostId.toString());
	}

	private Arkivsakssystem mapJoarkFagsystemToArkivsakssystemCode(String joarkFagsystem, String journalpostId) {
		if (FS22.name().equals(joarkFagsystem)) {
			return Arkivsakssystem.GSAK;
		} else if (PEN.name().equals(joarkFagsystem)) {
			return Arkivsakssystem.PSAK;
		} else if (joarkFagsystem == null || joarkFagsystem.isEmpty()) {
			return null;
		} else {
			throw new UgyldigArkivsaksystemException(String.format("Arkivsaksystem må være GSAK (FS22), PSAK (PEN) eller NULL (midlertidig journalpost). Journalpost med journalpostId=%s har Arkivsakssystem=%s", journalpostId, joarkFagsystem));
		}
	}

	private TilgangJournalpost mapTilgangJournalpost(final JournalpostDto dto) {
		return TilgangJournalpost.builder()
				.journalpostId(dto.getJournalpostId().toString())
				.journalstatus(dto.getJournalstatus().toSafJournalstatus())
				.skjerming(SkjermingTypeCode.toSafSkjerming(dto.getSkjerming()))
				.dokumenter(dto.getDokumenter().stream()
						.map(dokumentInfoDto -> mapTilgangDokumentInfo(dto, dokumentInfoDto))
						.collect(Collectors.toList()))
				.build();

	}

	private TilgangDokumentInfo mapTilgangDokumentInfo(final JournalpostDto dto, final DokumentInfoDto dokumentInfoDto) {
		return TilgangDokumentInfo.builder()
				.skjerming(SkjermingTypeCode.toSafSkjerming(dokumentInfoDto.getSkjerming()))
				.journalpostId(dto.getJournalpostId().toString())
				.dokumentInfoId(dokumentInfoDto.getDokumentInfoId())
				.tilgangDokumentvarianter(dokumentInfoDto.getVarianter().stream()
						.map(variantDto -> mapDokumentvarianter(dto, dokumentInfoDto, variantDto))
						.collect(Collectors.toList())).build();
	}

	private TilgangDokumentvariant mapDokumentvarianter(final JournalpostDto dto, final DokumentInfoDto dokumentInfoDto, final VariantDto variantDto) {
		return TilgangDokumentvariant.builder()
				.skjerming(SkjermingTypeCode.toSafSkjerming(variantDto.getSkjerming()))
				.variantformat(VariantFormatCode.toSafVariantformat(variantDto.getVariantf()))
				.journalpostId(dto.getJournalpostId().toString())
				.dokumentInfoId(dokumentInfoDto.getDokumentInfoId())
				.build();
	}
}
