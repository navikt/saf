package no.nav.saf.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.HentTilgangJournalpostResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangBrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangDokumentInfoDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangJournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangSakDto;
import no.nav.saf.anticorruptionlayer.joark.safintern.DokarkivConsumer;
import no.nav.saf.anticorruptionlayer.joark.safintern.hentdokument.HentDokumentResponseTo;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.HentDokument;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.UgyldigArkivsaksystemException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.FS22;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.PEN;
import static no.nav.saf.domain.DomainConstants.ORGANISASJON;
import static no.nav.saf.domain.DomainConstants.PERSON;
import static no.nav.saf.domain.DomainConstants.RJOARK901_TILGANG_JOURNALPOST_DTO;
import static no.nav.saf.util.MimetypeFileextensionMapper.toFileextension;

@Slf4j
@Component
public class HentDokumentAntiCorruptionLayer {
	public static final Set<String> HENTDOKUMENT_TILGANG_FIELDS = Set.of("journalpostId", "fagomraade", "status", "skjerming", "bruker", "saksrelasjon", "dokumenter.skjerming", "dokumenter.fildetaljer");

	private final HentJournalsakinfo hentJournalsakinfo;
	private final DokarkivConsumer dokarkivConsumer;

	@Autowired
	public HentDokumentAntiCorruptionLayer(HentJournalsakinfo hentJournalsakinfo,
										   DokarkivConsumer dokarkivConsumer) {
		this.hentJournalsakinfo = hentJournalsakinfo;
		this.dokarkivConsumer = dokarkivConsumer;
	}

	public ArkivJournalpost hentDokumentTilgang(String journalpostId, String dokumentInfoId) {
		return dokarkivConsumer.hentJournalpost(journalpostId, dokumentInfoId, HENTDOKUMENT_TILGANG_FIELDS);
	}

	public TilgangJournalpost hentTilgangJournalpostFromSafRequestContext(SafRequestContext safRequestContext) {
		TilgangJournalpostDto tilgangJournalpostDto = safRequestContext.getRequestCache().getObject(RJOARK901_TILGANG_JOURNALPOST_DTO);
		if (tilgangJournalpostDto == null) {
			return null;
		} else {
			return mapTilgangJournalpost(tilgangJournalpostDto);
		}
	}

	public TilgangSak hentTilgangSakFromSafRequestContext(SafRequestContext safRequestContext, TilgangBruker tilgangBruker) {
		final TilgangJournalpostDto tilgangJournalpostDto = safRequestContext.getRequestCache()
				.getObject(RJOARK901_TILGANG_JOURNALPOST_DTO);
		if (tilgangJournalpostDto == null) {
			throw new IllegalStateException("journalpost metadata for tilgangskontroll ligger ikke i requestCache. Dette er en ugyldig tilstand og en teknisk feil");
		} else if (tilgangJournalpostDto.isTilknyttetSak() && tilgangBruker != null) {
			return TilgangSak.builder()
					.foedselsnummer(tilgangBruker.getFoedselsnr())
					.arkivsaksnummer(tilgangJournalpostDto.getSak().getSakId())
					.arkivsaksystem(mapJoarkFagsystemToArkivsakssystemCode(tilgangJournalpostDto.getSak()
							.getFagsystem(), tilgangJournalpostDto.getJournalpostId()))
					.tema(FagomradeCode.toSafTema(tilgangJournalpostDto.getFagomrade()))
					.relevanteTredjeparter(new ArrayList<>())
					.build();
		} else {
			log.info("Dokumentet har ingen sakstilknytning. journalpostId={}", tilgangJournalpostDto.getJournalpostId());
			return TilgangSak.builder()
					.tema(FagomradeCode.toSafTema(tilgangJournalpostDto.getFagomrade()))
					.build();
		}
	}

	public TilgangBruker hentTilgangBruker(SafRequestContext safRequestContext) {
		final TilgangJournalpostDto tilgangJournalpostDto = safRequestContext.getRequestCache()
				.getObject(RJOARK901_TILGANG_JOURNALPOST_DTO);

		if (tilgangJournalpostDto == null || tilgangJournalpostDto.getBruker() == null
			|| tilgangJournalpostDto.getBruker().getBrukerType() == null) {
			return null;
		}
		final TilgangBrukerDto tilgangBruker = tilgangJournalpostDto.getBruker();
		switch (tilgangBruker.getBrukerType()) {
			case PERSON:
				return TilgangBruker.builder()
						.foedselsnr(tilgangBruker.getBrukerId())
						.build();
			case ORGANISASJON:
				return TilgangBruker.builder()
						.orgnummer(tilgangBruker.getBrukerId())
						.build();
			default:
				log.warn("Forventet brukerType=(PERSON, ORGANISASJON) for midlertidig journalpost med journalpostId={}. Fikk brukerType={}", tilgangJournalpostDto
						.getJournalpostId(), tilgangBruker.getBrukerType());
				return null;
		}
	}

	public HentDokument hentDokument(String dokumentInfoId, String variantFormat) {
		HentDokumentResponseTo responseTo = dokarkivConsumer.hentDokument(dokumentInfoId, variantFormat);

		return HentDokument.builder()
				.dokument(responseTo.dokument())
				.mediaType(responseTo.mediaType())
				.extension(toFileextension(responseTo.mediaType()))
				.build();
	}

	public Arkivsak hentArkivsakAndCacheJournalpostDto(String journalpostId, String dokumentInfoId, String variantFormat, SafRequestContext safRequestContex) {
		HentTilgangJournalpostResponseTo hentTilgangJournalpostResponseTo;
		hentTilgangJournalpostResponseTo = hentJournalsakinfo.hentTilgangJournalpost(journalpostId, dokumentInfoId, variantFormat);
		TilgangJournalpostDto tilgangJournalpostDto = hentTilgangJournalpostResponseTo.getTilgangJournalpostDto();
		safRequestContex.getRequestCache().putObject(RJOARK901_TILGANG_JOURNALPOST_DTO, tilgangJournalpostDto);
		if (tilgangJournalpostDto.isTilknyttetSak()) {
			TilgangSakDto sak = tilgangJournalpostDto.getSak();
			return Arkivsak.builder()
					.arkivsaksnummer(sak.getSakId())
					.arkivsaksystem(mapJoarkFagsystemToArkivsakssystemCode(
							sak.getFagsystem(),
							tilgangJournalpostDto.getJournalpostId()))
					.fagsakId(sak.getFagsakNr())
					.fagsaksystem(sak.getApplikasjon())
					.orgnummer(sak.getOrgnr())
					.aktoerId(sak.getAktoerId())
					.tema(Arkivsak.mapTema(sak.getTema()))
					.datoOpprettet(Optional.ofNullable(sak.getOpprettetTidspunkt())
							.map(ZonedDateTime::toLocalDateTime)
							.orElse(null))
					.build();
		} else {
			return Arkivsak.builder()
					.tema(FagomradeCode.toSafTema(tilgangJournalpostDto.getFagomrade()))
					.build();
		}
	}

	private TilgangJournalpost mapTilgangJournalpost(TilgangJournalpostDto dto) {
		final TilgangDokumentInfoDto tilgangDokumentInfoDto = dto.getDokument();
		return TilgangJournalpost.builder()
				.journalpostId(dto.getJournalpostId())
				.journalstatus(dto.getJournalStatus().toSafJournalstatus())
				.skjerming(SkjermingTypeCode.toSafSkjerming(dto.getSkjerming()))
				.dokumenter(Collections.singletonList(TilgangDokumentInfo.builder()
						.skjerming(SkjermingTypeCode.toSafSkjerming(tilgangDokumentInfoDto.getSkjerming()))
						.tilgangDokumentvarianter(Collections.singletonList(TilgangDokumentvariant.builder()
								.skjerming(SkjermingTypeCode.toSafSkjerming(tilgangDokumentInfoDto.getVariant().getSkjerming()))
								.variantformat(VariantFormatCode.toSafVariantformat(tilgangDokumentInfoDto.getVariant()
										.getVariantFormat()))
								.journalpostId(dto.getJournalpostId())
								.dokumentInfoId(dto.getDokument().getDokumentinfoId())
								.build()))
						.build()))
				.build();

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
}
