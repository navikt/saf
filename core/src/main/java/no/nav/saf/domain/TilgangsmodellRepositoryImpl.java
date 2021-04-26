package no.nav.saf.domain;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
@Slf4j
public class TilgangsmodellRepositoryImpl implements TilgangsmodellRepository {
	private static final int MAX_ARKIVSAKER_LOGG = 1000;

	private final JoarkAntiCorruptionLayer joarkAntiCorruptionLayer;

	@Inject
	public TilgangsmodellRepositoryImpl(JoarkAntiCorruptionLayer joarkAntiCorruptionLayer) {
		this.joarkAntiCorruptionLayer = joarkAntiCorruptionLayer;
	}

	@Override
	public List<TilgangJournalpost> findTilgangJournalposter(List<TilgangBruker> tilgangBrukere,
															 List<TilgangSak> tilgangSakList,
															 LocalDate fraDato,
															 LocalDate tilDato,
															 List<Journalposttype> inkluderJournalposttyper,
															 List<Journalstatus> inkluderJournalstatuses,
															 Integer foerste, String etterPeker,
															 SafRequestContext safRequestContext) {
		try {
			List<String> identer = tilgangBrukere.stream()
					.flatMap(t -> t.getAlleIdenter().stream())
					.collect(Collectors.toList());
			List<JournalpostDto> journalposter = joarkAntiCorruptionLayer.finnJournalposter(identer,
					tilgangSakList, fraDato, tilDato, inkluderJournalposttyper, inkluderJournalstatuses, foerste, etterPeker);
			return journalposter.stream()
					.map(journalpostDto -> {
						safRequestContext.getRequestCache()
								.putObject(journalpostDto.getJournalpostId().toString(), journalpostDto);
						return mapTilgangJournalpost(journalpostDto);
					})
					.collect(Collectors.toList());
		} catch (Exception e) {
			if (tilgangSakList.size() < MAX_ARKIVSAKER_LOGG) {
				List<String> arkivsaksId = tilgangSakList.stream()
						.map(TilgangSak::getArkivsaksnummer)
						.collect(Collectors.toList());
				log.warn("finnJournalposter feilet ved henting av journalposter på arkivsaker={}.", arkivsaksId, e);
			} else {
				log.warn("finnJournalposter feilet ved henting av journalposter på arkivsaker. Det var flere enn 1000 arkivsaker. Disse logges ikke da så lange logglinjer ikke støttes i logstash.", e);
			}
			return new ArrayList<>();
		}
	}

	@Override
	public List<TilgangJournalpost> findTilgangJournalposterStatus(LocalDate fraDato,
																   List<Journalposttype> inkluderJournalposttyper,
																   Journalstatus journalstatus,
																   Integer foerste, String etterPeker,
																   SafRequestContext safRequestContext) {
		try {
			List<JournalpostDto> journalposter = joarkAntiCorruptionLayer.finnJournalposterStatus(fraDato,
					inkluderJournalposttyper, journalstatus, foerste, etterPeker);
			return journalposter.stream()
					.map(journalpostDto -> {
						safRequestContext.getRequestCache()
								.putObject(journalpostDto.getJournalpostId().toString(), journalpostDto);
						return mapTilgangJournalpost(journalpostDto);
					})
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.warn(String.format("finnJournalposterStatus feilet ved henting av journalposter med fraDato=%s, journalstatus=%s, inkluderJournalposttyper=%s.",
					fraDato, journalstatus, inkluderJournalposttyper), e);
			return new ArrayList<>();
		}
	}

	private TilgangJournalpost mapTilgangJournalpost(JournalpostDto dto) {
		return TilgangJournalpost.builder()
				.journalpostId(dto.getJournalpostId().toString())
				.journalstatus(dto.getJournalstatus().toSafJournalstatus())
				.skjerming(SkjermingTypeCode.toSafSkjerming(dto.getSkjerming()))
				.tilgangSak(mapSak(dto.getSaksrelasjon()))
				.dokumenter(dto.getDokumenter().stream().map(dokdto -> TilgangDokumentInfo.builder()
						.journalpostId(dto.getJournalpostId().toString())
						.dokumentInfoId(dokdto.getDokumentInfoId())
						.skjerming(SkjermingTypeCode.toSafSkjerming(dokdto.getSkjerming()))
						.tilgangDokumentvarianter(dokdto.getVarianter().stream()
								.map(variantDto -> TilgangDokumentvariant.builder()
										.skjerming(SkjermingTypeCode.toSafSkjerming(variantDto.getSkjerming()))
										.variantformat(VariantFormatCode.toSafVariantformat(variantDto.getVariantf()))
										.journalpostId(dto.getJournalpostId().toString())
										.dokumentInfoId(dokdto.getDokumentInfoId())
										.build())
								.collect(Collectors.toList())
						)
						.build()).collect(Collectors.toList()))
				.build();
	}

	private TilgangSak mapSak(SaksrelasjonDto saksrelasjonDto) {
		return TilgangSak.builder()
				.applikasjon(saksrelasjonDto.getApplikasjon())
				.sakId(saksrelasjonDto.getSakId())
				.build();
	}

}
