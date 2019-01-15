package no.nav.saf.anticorruptionlayer.joark;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.FS22;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.PEN;
import static no.nav.saf.domain.DomainConstants.ORGANISASJON;
import static no.nav.saf.domain.DomainConstants.PERSON;
import static no.nav.saf.domain.DomainConstants.TILGANG_JOURNALPOST_DTO;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.SafToJoarkJournalstatusMapper;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.FinnJournalposterRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.FinnJournalposterResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.HentTilgangJournalpostResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangBrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangDokumentInfoDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangJournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark920.HentDokumentResponseTo;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.HentDokument;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.exceptions.UgyldigArkivsaksystemException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
class JoarkAntiCorruptionLayerImpl implements JoarkAntiCorruptionLayer {
	private final HentJournalsakinfo hentJournalsakinfo;
	private final SafToJoarkJournalstatusMapper safToJoarkJournalstatusMapper;

	@Inject
	public JoarkAntiCorruptionLayerImpl(HentJournalsakinfo hentJournalsakinfo) {
		this.hentJournalsakinfo = hentJournalsakinfo;
		this.safToJoarkJournalstatusMapper = new SafToJoarkJournalstatusMapper();
	}

	@Override
	public List<JournalpostDto> finnJournalposter(List<String> alleIdenter,
												  List<TilgangSak> tilgangSakList,
												  LocalDate fraDato,
												  List<Tema> inkluderTema,
												  List<Journalposttype> inkluderJournalposttyper,
												  List<Journalstatus> inkluderJournalstatuses,
												  Integer foerste, String etterPeker, Integer siste, String foerPeker) {
		FinnJournalposterResponseTo responseTo = hentJournalsakinfo.finnJournalposter(FinnJournalposterRequestTo.builder()
				.alleIdenter(alleIdenter)
				.inkluderJournalpostType(inkluderJournalposttyper.stream()
						.map(jt -> JournalpostTypeCode.valueOf(jt.name()))
						.collect(Collectors.toList()))
				.inkluderTema(inkluderTema.stream()
						.map(FagomradeCode::fromTema)
						.filter(Objects::nonNull)
						.collect(Collectors.toList()))
				.inkluderJournalStatus(safToJoarkJournalstatusMapper.map(inkluderJournalstatuses))
				.visFeilregistrerte(inkluderJournalstatuses.contains(Journalstatus.FEILREGISTRERT))
				.fraDato(fraDato.toString())
				.gsakSakIds(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.GSAK.name().equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()))
				.psakSakIds(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.PSAK.name().equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()))
				.foerste(foerste)
				.etterPeker(etterPeker)
				.siste(siste)
				.foerPeker(foerPeker)
				.build());

		return responseTo.getTilgangJournalposter();
	}

	@Override
	public TilgangJournalpost hentTilgangJournalpostFromSafRequestContext(SafRequestContext safRequestContext, TilgangSak tilgangSak) {
		TilgangJournalpostDto tilgangJournalpostDto = safRequestContext.getRequestCache().getObject(TILGANG_JOURNALPOST_DTO);
		if (tilgangJournalpostDto == null) {
			return null;
		} else {
			return mapTilgangJournalpost(tilgangJournalpostDto, tilgangSak);
		}
	}

	@Override
	public TilgangSak hentTilgangSakFromSafRequestContext(SafRequestContext safRequestContext, TilgangBruker tilgangBruker) {
		final TilgangJournalpostDto tilgangJournalpostDto = safRequestContext.getRequestCache()
				.getObject(TILGANG_JOURNALPOST_DTO);
		if (tilgangJournalpostDto == null || tilgangJournalpostDto.getSak() == null || tilgangJournalpostDto.getBruker() == null
				|| tilgangJournalpostDto.getBruker().getBrukerId() == null) {
			return null;
		} else {
			return TilgangSak.builder()
					.foedselsnummer(tilgangBruker.getFoedselsnr())
					.arkivsaksnummer(tilgangJournalpostDto.getSak().getSakId())
					.arkivsaksystem(mapJoarkFagsystemToArkivsakssystemString(tilgangJournalpostDto.getSak()
							.getFagsystem(), tilgangJournalpostDto.getJournalpostId()))
					.tema(tilgangJournalpostDto.getTema())
					.build();
		}
	}

	@Override
	public TilgangBruker hentTilgangBruker(SafRequestContext safRequestContext) {
		final TilgangJournalpostDto tilgangJournalpostDto = safRequestContext.getRequestCache()
				.getObject(TILGANG_JOURNALPOST_DTO);

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
						.getJournalpostId());
				return null;
		}
	}

	@Override
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
				.build();
	}

	@Override
	public Arkivsak hentArkivsakAndCacheJournalpostDto(String journalpostId, String dokumentInfoId, String variantFormat, SafRequestContext safRequestContex) {
		HentTilgangJournalpostResponseTo hentTilgangJournalpostResponseTo;
		try {
			hentTilgangJournalpostResponseTo = hentJournalsakinfo.hentTilgangJournalpost(journalpostId, dokumentInfoId, variantFormat);
		} catch (Exception e) {
			log.warn("Kunne ikke hente tilgangJournalpost. journalpostId={}, dokumentInfoId={}, variantFormat={}", journalpostId, dokumentInfoId, variantFormat, e);
			return null;
		}
		safRequestContex.getRequestCache()
				.putObject(TILGANG_JOURNALPOST_DTO, hentTilgangJournalpostResponseTo.getTilgangJournalpostDto());
		return Arkivsak.builder()
				.arkivsaksnummer(hentTilgangJournalpostResponseTo.getTilgangJournalpostDto().getSak().getSakId())
				.arkivsaksystem(mapJoarkFagsystemToArkivsakssystemCode(hentTilgangJournalpostResponseTo.getTilgangJournalpostDto()
						.getSak().getFagsystem(), hentTilgangJournalpostResponseTo.getTilgangJournalpostDto()
						.getJournalpostId()))
				.build();
	}

	private TilgangJournalpost mapTilgangJournalpost(TilgangJournalpostDto dto, TilgangSak tilgangSak) {
		final TilgangDokumentInfoDto tilgangDokumentInfoDto = dto.getDokument();
		return TilgangJournalpost.builder()
				.journalpostId(dto.getJournalpostId())
				.journalStatus(dto.getJournalStatus())
				.journalpostType(dto.getJournalpostType())
				.tema(tilgangSak.getTema())
				.arkivsaksystem(mapJoarkFagsystemToArkivsakssystemString(dto.getSak() == null ? null : dto.getSak()
						.getFagsystem(), dto.getJournalpostId()))
				.arkivsaksnummer(dto.getSak() == null ? null : dto.getSak().getSakId())
				.datoOpprettet(dto.getDatoOpprettet().toLocalDate())
				.mottakskanal(dto.getMottakskanal())
				.avsenderMottakerId(dto.getAvsenderMottakerId())
				.dokumenter(Arrays.asList(TilgangDokumentInfo.builder()
						.dokumentInfoId(tilgangDokumentInfoDto.getDokumentinfoId())
						.dokumentstatus(tilgangDokumentInfoDto.getDokumentstatus())
						.brevkode(tilgangDokumentInfoDto.getBrevkode())
						.variantFormat(tilgangDokumentInfoDto.getVariantFormat())
						.build()))
				.build();

	}

	private String mapJoarkFagsystemToArkivsakssystemString(String joarkFagsystem, String journalpostId) {
		if (FS22.name().equals(joarkFagsystem)) {
			return Arkivsakssystem.GSAK.name();
		} else if (PEN.name().equals(joarkFagsystem)) {
			return Arkivsakssystem.PSAK.name();
		} else if (joarkFagsystem == null || joarkFagsystem.isEmpty()) {
			return null;
		} else {
			throw new UgyldigArkivsaksystemException(String.format("Arkivsaksystem må være GSAK (FS22), PSAK (PEN) eller NULL (midlertidig journalpost). Journalpost med journalpostId=%s har Arkivsakssystem=%s", journalpostId, joarkFagsystem));
		}
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
