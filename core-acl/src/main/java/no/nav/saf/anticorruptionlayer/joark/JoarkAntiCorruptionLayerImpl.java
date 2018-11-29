package no.nav.saf.anticorruptionlayer.joark;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.FS22;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.PEN;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.SafToJoarkJournalstatusMapper;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.HentJournalpostBulkRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.HentJournalpostBulkResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.HentTilgangJournalpostResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangBrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangDokumentInfoDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangJournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangSakDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark920.HentDokumentResponseTo;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.tjeneste.hentdokument.HentDokument;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalz;
import no.nav.saf.tjeneste.visningsmodell.kode.Journazz;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
class JoarkAntiCorruptionLayerImpl implements JoarkAntiCorruptionLayer {
	// Joark bruker in query og max antall elementer er 1000 i Oracle.
	private static final int SAK_MAX_SIZE = 1000;
	private final HentJournalsakinfo hentJournalsakinfo;
	private final SafToJoarkJournalstatusMapper safToJoarkJournalstatusMapper;

	@Inject
	public JoarkAntiCorruptionLayerImpl(HentJournalsakinfo hentJournalsakinfo) {
		this.hentJournalsakinfo = hentJournalsakinfo;
		this.safToJoarkJournalstatusMapper = new SafToJoarkJournalstatusMapper();
	}

	@Override
	public Map<String, JournalpostDto> hentJournalpostBulk(TilgangBruker tilgangBruker,
														   List<TilgangSak> tilgangSakList,
														   LocalDate fraDato,
														   List<Journalz> inkluderJournalposttyper,
														   List<Journazz> inkluderJournalstatuses) {
		List<String> alleIdenter = tilgangBruker.getHistoriskeIdenter()
				.stream()
				.map(TilgangIdent::getIdentifikator)
				.collect(Collectors.toList());
		alleIdenter.add(tilgangBruker.getFoedselsnr());
		HentJournalpostBulkResponseTo responseTo = hentJournalsakinfo.hentJournalpostBulk(HentJournalpostBulkRequestTo.builder()
				.aktoerId(tilgangBruker.getAktoerId())
				.alleIdenter(alleIdenter)
				.inkluderJournalpostType(inkluderJournalposttyper.stream()
						.map(jt -> JournalpostTypeCode.valueOf(jt.name()))
						.collect(Collectors.toList()))
				.inkluderJournalStatus(safToJoarkJournalstatusMapper.map(inkluderJournalstatuses))
				.visFeilregistrerte(inkluderJournalstatuses.contains(Journazz.FEILREGISTRERT))
				.fraDato(fraDato.toString())
				.gsakSakIds(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.GSAK.name().equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).limit(SAK_MAX_SIZE).collect(Collectors.toList()))
				.psakSakIds(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.PSAK.name().equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).limit(SAK_MAX_SIZE).collect(Collectors.toList()))
				.build());

		List<JournalpostDto> tilgangJournalposter = responseTo.getTilgangJournalposter();
		return tilgangJournalposter.stream()
				.collect(Collectors.toMap(journalpostDto -> "journalpostId=" + journalpostDto.getJournalpostId().toString(),
						journalpostDto -> journalpostDto, (
								journalpostDto1, journalpostDto2) -> {
							// Ignorerer duplikate journalpostId
							return journalpostDto1;
						})
				);
	}

	@Override
	public TilgangJournalpost hentTilgangJournalpost(String journalpostId, String dokumentId, String variantFormat) {
		HentTilgangJournalpostResponseTo hentTilgangJournalpostResponseTo = hentJournalsakinfo.hentTilgangJournalpost(journalpostId, dokumentId, variantFormat);
		return mapTilgangJournalpost(hentTilgangJournalpostResponseTo.getTilgangJournalpostDto());

	}

	@Override
	public TilgangSak hentTilgangSak(String journalpostId, String dokumentId, String variantFormat) {
		final HentTilgangJournalpostResponseTo hentTilgangJournalpostResponseTo = hentJournalsakinfo.hentTilgangJournalpost(journalpostId, dokumentId, variantFormat);
		if (hentTilgangJournalpostResponseTo.getTilgangJournalpostDto() == null || hentTilgangJournalpostResponseTo.getTilgangJournalpostDto()
				.getSak() == null) {
			return null;
		}
		final TilgangSakDto tilgangSakDto = hentTilgangJournalpostResponseTo.getTilgangJournalpostDto().getSak();
		return TilgangSak.builder()
				.arkivsaksnummer(tilgangSakDto.getSakId())
				.arkivsaksystem(mapJoarkFagsystem(tilgangSakDto.getFagsystem()))
				.tema(hentTilgangJournalpostResponseTo.getTilgangJournalpostDto().getTema())
				.build();
	}

	@Override
	public TilgangBruker hentTilgangBruker(String journalpostId, String dokumentId, String variantFormat) {
		HentTilgangJournalpostResponseTo hentTilgangJournalpostResponseTo = hentJournalsakinfo.hentTilgangJournalpost(journalpostId, dokumentId, variantFormat);
		final TilgangBrukerDto tilgangBrukerDto = hentTilgangJournalpostResponseTo.getTilgangJournalpostDto().getBruker();
		return TilgangBruker.builder()
				.foedselsnr(tilgangBrukerDto.getBrukerId())
				.build();
	}

	@Override
	public HentDokument hentDokument(String dokumentId, String variantFormat) {
		HentDokumentResponseTo responseTo = hentJournalsakinfo.hentDokument(dokumentId, variantFormat);
		byte[] dokumentByteArray;
		try {
			dokumentByteArray = Base64.getDecoder().decode(responseTo.getDokument());
		} catch (Exception e) {
			throw new SafTechnicalException(String.format("Kunne ikke dekode dokument, dokumentId=%s, variantFormat=%s. Feilmelding=%s", dokumentId, variantFormat, e
					.getMessage()), e);
		}

		return HentDokument.builder()
				.dokument(dokumentByteArray)
				.mediaType(responseTo.getMediaType())
				.build();
	}

	private TilgangJournalpost mapTilgangJournalpost(TilgangJournalpostDto dto) {
		final TilgangDokumentInfoDto tilgangDokumentInfoDto = dto.getDokument();
		return TilgangJournalpost.builder()
				.journalpostId(dto.getJournalpostId())
				.journalStatus(dto.getJournalStatus())
				.journalpostType(dto.getJournalpostType())
				.tema(dto.getTema())
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

	private String mapJoarkFagsystem(String joarkFagsystem) {
		if (FS22.name().equals(joarkFagsystem)) {
			return Arkivsakssystem.GSAK.name();
		} else if (PEN.name().equals(joarkFagsystem)) {
			return Arkivsakssystem.PSAK.name();
		} else {
			throw new SafFunctionalException(String.format("Arkivsaksystem må være GSAK (FS22) eller PSAK (PEN). Fikk: %s i oppslag mot hentTilgangJournalpost", joarkFagsystem));
		}
	}
}
