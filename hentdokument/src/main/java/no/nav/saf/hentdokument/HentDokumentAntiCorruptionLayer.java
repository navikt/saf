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
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark920.HentDokumentResponseTo;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.HentDokument;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.exceptions.UgyldigArkivsaksystemException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.FS22;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.PEN;
import static no.nav.saf.domain.DomainConstants.ORGANISASJON;
import static no.nav.saf.domain.DomainConstants.PERSON;
import static no.nav.saf.domain.DomainConstants.RJOARK901_TILGANG_JOURNALPOST_DTO;
import static no.nav.saf.util.MimetypeFileextensionMapper.toFileextension;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class HentDokumentAntiCorruptionLayer {
	private final HentJournalsakinfo hentJournalsakinfo;

	@Inject
	public HentDokumentAntiCorruptionLayer(HentJournalsakinfo hentJournalsakinfo) {
		this.hentJournalsakinfo = hentJournalsakinfo;
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
		if (tilgangJournalpostDto == null || tilgangJournalpostDto.getSak() == null || tilgangJournalpostDto.getBruker() == null
				|| tilgangJournalpostDto.getBruker().getBrukerId() == null) {
			log.info("hentTilgangSakFromSafRequestContext feilet, da påkrevde felter for TilgangSak mangler på det cachede TilgangJournalpostDto-objektet. JournalpostId={}",
					tilgangJournalpostDto == null ? null : tilgangJournalpostDto.getJournalpostId());
			return null;
		} else {
			return TilgangSak.builder()
					.foedselsnummer(tilgangBruker.getFoedselsnr())
					.arkivsaksnummer(tilgangJournalpostDto.getSak().getSakId())
					.arkivsaksystem(mapJoarkFagsystemToArkivsakssystemCode(tilgangJournalpostDto.getSak()
							.getFagsystem(), tilgangJournalpostDto.getJournalpostId()))
					.tema(FagomradeCode.toSafTema(tilgangJournalpostDto.getFagomrade()))
					.relevanteTredjeparter(new ArrayList<>())
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
		HentDokumentResponseTo responseTo = hentJournalsakinfo.hentDokument(dokumentInfoId, variantFormat);
		byte[] dokumentByteArray;
		try {
			dokumentByteArray = Base64.getDecoder().decode(responseTo.getDokument());
		} catch (Exception e) {
			throw new SafTechnicalException(String.format("Kunne ikke dekode dokument, dokumentInfoId=%s, variantFormat=%s. Feilmelding=%s", dokumentInfoId, variantFormat, e
					.getMessage()), e);
		}

		return HentDokument.builder()
				.dokument(dokumentByteArray)
				.mediaType(responseTo.getMediaType())
				.extension(toFileextension(responseTo.getMediaType()))
				.build();
	}

	public Arkivsak hentArkivsakAndCacheJournalpostDto(String journalpostId, String dokumentInfoId, String variantFormat, SafRequestContext safRequestContex) {
		HentTilgangJournalpostResponseTo hentTilgangJournalpostResponseTo;
		try {
			hentTilgangJournalpostResponseTo = hentJournalsakinfo.hentTilgangJournalpost(journalpostId, dokumentInfoId, variantFormat);
		} catch (Exception e) {
			log.warn("Kunne ikke hente tilgangJournalpost. journalpostId={}, dokumentInfoId={}, variantFormat={}", journalpostId, dokumentInfoId, variantFormat, e);
			return null;
		}
		safRequestContex.getRequestCache()
				.putObject(RJOARK901_TILGANG_JOURNALPOST_DTO, hentTilgangJournalpostResponseTo.getTilgangJournalpostDto());
		return Optional.ofNullable(hentTilgangJournalpostResponseTo.getTilgangJournalpostDto().getSak())
				.map(sak -> Arkivsak.builder()
						.arkivsaksnummer(sak.getSakId())
						.arkivsaksystem(mapJoarkFagsystemToArkivsakssystemCode(
								sak.getFagsystem(),
								hentTilgangJournalpostResponseTo.getTilgangJournalpostDto().getJournalpostId()))
						.fagsakId(sak.getFagsakNr())
						.fagsaksystem(sak.getApplikasjon())
						.orgnummer(sak.getOrgnr())
						.aktoerId(sak.getAktoerId())
						.tema(Arkivsak.mapTema(sak.getTema()))
						.datoOpprettet(Optional.ofNullable(sak.getOpprettetTidspunkt())
								.map(ZonedDateTime::toLocalDateTime)
								.orElse(null))
						.build())
				.orElse(null);
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
