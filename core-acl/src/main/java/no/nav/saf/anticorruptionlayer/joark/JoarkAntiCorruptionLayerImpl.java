package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.domain.DokumentInfoTo;
import no.nav.saf.anticorruptionlayer.joark.domain.JournalpostTo;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalposterRequest;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalposterResponse;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.HentJournalpostBulkRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.HentJournalpostBulkResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.HentTilgangJournalpostResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangBrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangDokumentInfoDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangJournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.TilgangSakDto;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.hentdokument.HentDokument;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalStatus;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalpostType;
import no.nav.saf.tjeneste.visningsmodell.kode.Mottakskanal;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;
import no.nav.saf.tjeneste.visningsmodell.kode.VariantFormatkode;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class JoarkAntiCorruptionLayerImpl implements JoarkAntiCorruptionLayer {

	private final HentJournalsakinfo hentJournalsakinfo;
	private final Cache journalpostCache;

	@Inject
	public JoarkAntiCorruptionLayerImpl(HentJournalsakinfo hentJournalsakinfo, CacheManager cacheManager) {
		this.hentJournalsakinfo = hentJournalsakinfo;
		this.journalpostCache = cacheManager.getCache(LokalCacheConfig.JOURNALPOST_CACHE);
	}

	@Override
	public List<Journalpost> hentJournalpostListeByArkivsaker(List<Sak> saker) {
		HentJournalposterResponse hentJournalposterResponse = hentJournalsakinfo.hentJournalposter(HentJournalposterRequest.builder()
				.gsakSakIdList(saker.stream()
						.filter(sak -> Arkivsakssystem.GSAK.equals(sak.getArkivsaksystem()))
						.map(Sak::getArkivsaksnummer).collect(Collectors.toList()))
				.psakSakIdList(saker.stream()
						.filter(tilgangSak -> Arkivsakssystem.PSAK.equals(tilgangSak.getArkivsaksystem()))
						.map(Sak::getArkivsaksnummer).collect(Collectors.toList()))
				.build());

		Map<String, Sak> sakMap = saker.stream().collect(Collectors.toMap(Sak::getArkivsaksnummer, sak -> sak));

		return Stream.concat(hentJournalposterResponse.getGsakJournalpostList()
				.stream(), hentJournalposterResponse.getPsakJournalpostList().stream())
				.map(journalpostTo -> mapJournalpostTo(sakMap, journalpostTo))
				.collect(Collectors.toList());
	}

	private Journalpost mapJournalpostTo(Map<String, Sak> sakMap, JournalpostTo journalpostTo) {
		return Journalpost.builder()
				.journalpostId(journalpostTo.getJournalpostId().toString())
				.beskrivelse(journalpostTo.getInnhold())
				.journalposttype(JournalpostType.fromJoark(journalpostTo.getJournalposttype()))
				.journalstatus(journalpostTo.getJournalstatus().toSafJournalStatus())
				.tema(FagomradeCode.toSafJournalStatus(journalpostTo.getFagomrade()))
				.temanavn(FagomradeCode.toSafJournalStatus(journalpostTo.getFagomrade()).getTemanavn())
				.mottakskanal(Mottakskanal.fromJoark(journalpostTo.getMottakskanal()))
				.opprettet(journalpostTo.getDatoOpprettet() == null ? null : LocalDateTime.from(journalpostTo.getDatoOpprettet()
						.toInstant()
						.atZone(ZoneId.systemDefault())))
				.sak(journalpostTo.getSaksrelasjon() == null ? null : sakMap.get(journalpostTo.getSaksrelasjon()
						.getSakId()))
				.dokumenter(journalpostTo.getJournalpostDokumentInfoRelasjoner().stream()
						.map(jr -> {
							final DokumentInfoTo dokumentInfoTo = jr.getDokumentInfo();
							return DokumentInfo.builder()
									.dokumentId(dokumentInfoTo.getDokumentInfoId().toString())
									.tittel(dokumentInfoTo.getTittel())
									.variantFormat(VariantFormatkode.ARKIV)
									.saksbehandlerHarTilgang(false)
									.innbyggerHarDigitaltInnsyn(false)
									.build();
						}).collect(Collectors.toList())).build();
	}

	@Override
	public List<TilgangJournalpost> hentTilgangJournalpostListByArkivsaker(List<TilgangSak> tilgangSakList) {
		HentJournalposterResponse hentJournalposterResponse = hentJournalsakinfo.hentJournalposter(HentJournalposterRequest.builder()
				.gsakSakIdList(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.GSAK.name().equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()))
				.psakSakIdList(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.PSAK.name().equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()))
				.build());

		return hentJournalposterResponse
				.getGsakJournalpostList().stream().map(journalpostTo -> TilgangJournalpost.builder()
						.journalpostId(journalpostTo.getJournalpostId().toString())
						.journalStatus(journalpostTo.getJournalstatus() == null ? null : journalpostTo.getJournalstatus()
								.name())
						.dokumenter(journalpostTo.getJournalpostDokumentInfoRelasjoner().stream()
								.map(relasjon -> TilgangDokumentInfo.builder()
										.dokumentInfoId(relasjon.getDokumentInfo().getDokumentInfoId().toString())
										.dokumentstatus(relasjon.getDokumentInfo().getDokumentstatus() == null ? null : relasjon
												.getDokumentInfo().getDokumentstatus().name())
										.variantFormat("ARKIV") //TODO Endre hentJournalsakInfo til å også returnere variantformat
										.build())
								.collect(Collectors.toList()))
						.build())
				.collect(Collectors.toList());
	}

	@Override
	public List<TilgangJournalpost> hentTilgangJournalpostListByArkivsaker(TilgangBruker tilgangBruker,
																		   List<TilgangSak> tilgangSakList,
																		   LocalDate fraDato,
																		   Collection<Temakode> inkluderTema,
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
				.inkluderTema(inkluderTema.stream().map(t -> FagomradeCode.valueOf(t.name())).collect(Collectors.toList()))
				.visFeilregistrerte(inkluderJournalstatus.contains(JournalStatus.FEILREGISTRERT))
				.fraDato(fraDato.toString())
				.gsakSakIds(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.GSAK.name().equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()))
				.psakSakIds(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.PSAK.name().equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()))
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
		HentTilgangJournalpostResponseTo hentTilgangJournalpostResponseTo = hentJournalsakinfo.hentTilgangJournalpost(journalpostId, dokumentId, variantFormat);
		final TilgangSakDto tilgangSakDto = hentTilgangJournalpostResponseTo.getTilgangJournalpostDto().getSak();
		return TilgangSak.builder()
				.arkivsaksnummer(tilgangSakDto.getSakId())
				.arkivsaksystem(tilgangSakDto.getFagsystem())
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
		return hentJournalsakinfo.hentDokument(dokumentId, variantFormat);
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
				.avsenderMottakerId(dto.getAvsenderMottakerId())
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
		return Journalpost.builder()
				.journalpostId(journalpostDto.getJournalpostId().toString())
				.beskrivelse(journalpostDto.getInnhold())
				.journalposttype(JournalpostType.fromJoark(journalpostDto.getJournalposttype()))
				.journalstatus(journalpostDto.getJournalstatus().toSafJournalStatus())
				.tema(FagomradeCode.toSafJournalStatus(journalpostDto.getFagomrade()))
				.temanavn(FagomradeCode.toSafJournalStatus(journalpostDto.getFagomrade()).getTemanavn())
				.mottakskanal(Mottakskanal.fromJoark(journalpostDto.getMottakskanal()))
				.opprettet(journalpostDto.getDatoOpprettet() == null ? null : LocalDateTime.from(journalpostDto.getDatoOpprettet()
						.toInstant()
						.atZone(ZoneId.systemDefault())))
				.sak(journalpostDto.getSaksrelasjon() == null ? null : sakMap.get(journalpostDto.getSaksrelasjon()
						.getSakId()))
				.dokumenter(journalpostDto.getDokumenter().stream()
						.map(dokumentInfoDto -> {
							return DokumentInfo.builder()
									.dokumentId(dokumentInfoDto.getDokumentInfoId())
									.tittel(dokumentInfoDto.getTittel())
									.variantFormat(VariantFormatkode.ARKIV)
									.saksbehandlerHarTilgang(false)
									.innbyggerHarDigitaltInnsyn(false)
									.build();
						}).collect(Collectors.toList())).build();
	}
}
