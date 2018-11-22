package no.nav.saf.anticorruptionlayer.joark;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.FS22;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.PEN;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
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
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.tjeneste.hentdokument.HentDokument;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalStatus;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalpostType;
import no.nav.saf.tjeneste.visningsmodell.kode.Kanal;
import no.nav.saf.tjeneste.visningsmodell.kode.VariantFormat;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	private final Cache journalpostCache;

	@Inject
	public JoarkAntiCorruptionLayerImpl(HentJournalsakinfo hentJournalsakinfo, CacheManager cacheManager) {
		this.hentJournalsakinfo = hentJournalsakinfo;
		this.journalpostCache = cacheManager.getCache(LokalCacheConfig.JOURNALPOST_CACHE);
	}

	@Override
	public List<TilgangJournalpost> hentTilgangJournalpostListByArkivsaker(TilgangBruker tilgangBruker,
																		   List<TilgangSak> tilgangSakList,
																		   LocalDate fraDato,
																		   List<JournalpostType> inkluderJournalposttyper,
																		   List<JournalStatus> inkluderJournalstatus) {
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
				.inkluderJournalStatus(JournalStatusCode.asList())
				.visFeilregistrerte(inkluderJournalstatus.contains(JournalStatus.FEILREGISTRERT))
				.fraDato(fraDato.toString())
				.gsakSakIds(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.GSAK.name().equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).limit(SAK_MAX_SIZE).collect(Collectors.toList()))
				.psakSakIds(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.PSAK.name().equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).limit(SAK_MAX_SIZE).collect(Collectors.toList()))
				.build());

		return responseTo.getTilgangJournalposter().stream().map(this::mapTilgangJournalpost).collect(Collectors.toList());
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

	private TilgangJournalpost mapTilgangJournalpost(JournalpostDto dto) {
		journalpostCache.put(dto.getJournalpostId().toString(), dto);
		return TilgangJournalpost.builder()
				.journalpostId(dto.getJournalpostId().toString())
				.journalStatus(dto.getJournalstatus().toString())
				.journalpostType(dto.getJournalposttype().toString())
				.tema(dto.getFagomrade() == null ? null : dto.getFagomrade().toString())
				.datoOpprettet(dto.getDatoOpprettet().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
				.mottakskanal(dto.getMottakskanal() == null ? null : dto.getMottakskanal().toString())
				.avsenderMottakerId(dto.getAvsenderMottakerNavn())
				.dokumenter(dto.getDokumenter().stream().map(dokdto -> TilgangDokumentInfo.builder()
						.dokumentInfoId(dokdto.getDokumentInfoId())
						.dokumentstatus(dokdto.getDokumentstatus() == null ? null : dokdto.getDokumentstatus().toString())
						.brevkode(dokdto.getBrevkode())
						.variantFormat(dokdto.getVariantFormat() == null ? null : dokdto.getVariantFormat().toString())
						.build()).collect(Collectors.toList()))
				.build();
	}

	@Override
	public List<Journalpost> hentVisningJournalposter(Map<String, Sak> sakMap, List<String> journalpostIds) {
		return journalpostIds.stream()
				.map(journalpostId -> {
					JournalpostDto journalpostDto = journalpostCache.get(journalpostId, JournalpostDto.class);
					if (journalpostDto == null) {
						return null;
					}
					return mapJournalpostDto(sakMap, journalpostDto);
				}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	private Journalpost mapJournalpostDto(Map<String, Sak> sakMap, JournalpostDto journalpostDto) {
		final Kanal kanal = mapKanal(journalpostDto);
		return Journalpost.builder()
				.journalpostId(journalpostDto.getJournalpostId().toString())
				.tittel(journalpostDto.getInnhold())
				.journalposttype(JournalpostType.fromJoark(journalpostDto.getJournalposttype()))
				.journalstatus(journalpostDto.getJournalstatus().toSafJournalStatus())
				.tema(FagomradeCode.toSafJournalStatus(journalpostDto.getFagomrade()))
				.temanavn(FagomradeCode.toSafJournalStatus(journalpostDto.getFagomrade()).getTemanavn())
				.sak(journalpostDto.getSaksrelasjon() == null ? null : sakMap.get(journalpostDto.getSaksrelasjon()
						.getSakId()))
				.avsenderMottakerNavn(journalpostDto.getAvsenderMottakerNavn())
				.journalfortAvNavn(journalpostDto.getJournalfortAvNavn())
				.kanal(kanal)
				.kanalnavn(kanal == null ? null : kanal.getKanalnavn())
				.opprettet(journalpostDto.getDatoOpprettet() == null ? null : LocalDateTime.from(journalpostDto.getDatoOpprettet()
						.toInstant()
						.atZone(ZoneId.systemDefault())))
				.dokumenter(journalpostDto.getDokumenter().stream()
						.map(dokumentInfoDto -> DokumentInfo.builder()
								.dokumentId(dokumentInfoDto.getDokumentInfoId())
								.tittel(dokumentInfoDto.getTittel())
								.variantFormat(VariantFormat.ARKIV)
								.saksbehandlerHarTilgang(false)
								.innbyggerHarDigitaltInnsyn(false)
								.build()).collect(Collectors.toList())).build();
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
			default:
				if (journalpostDto.getUtsendingskanal() == null) {
					return mapManglendeUtsendingskanal(journalpostDto);
				}
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
