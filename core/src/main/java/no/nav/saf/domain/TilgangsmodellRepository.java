package no.nav.saf.domain;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.safintern.DokarkivConsumer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.PaginatedArkivJournalpost;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

@Repository
@Slf4j
public class TilgangsmodellRepository {
	private static final int MAX_ARKIVSAKER_LOGG = 1000;

	private final JoarkAntiCorruptionLayer joarkAntiCorruptionLayer;
	private final DokarkivConsumer dokarkivConsumer;

	@Autowired
	public TilgangsmodellRepository(JoarkAntiCorruptionLayer joarkAntiCorruptionLayer, DokarkivConsumer dokarkivConsumer) {
		this.joarkAntiCorruptionLayer = joarkAntiCorruptionLayer;
		this.dokarkivConsumer = dokarkivConsumer;
	}

	public Map<Long, JournalpostDto> findJournalposter(List<TilgangBruker> tilgangBrukere,
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
					.toList();
			return joarkAntiCorruptionLayer.finnJournalposter(identer,
							tilgangSakList, fraDato, tilDato, inkluderJournalposttyper, inkluderJournalstatuses, foerste, etterPeker)
					.stream()
					.collect(Collectors.toMap(JournalpostDto::getJournalpostId, journalpost -> journalpost));
		} catch (Exception e) {
			if (tilgangSakList.size() < MAX_ARKIVSAKER_LOGG) {
				List<String> arkivsaksId = tilgangSakList.stream()
						.map(TilgangSak::getArkivsaksnummer)
						.toList();
				log.error("finnJournalposter feilet ved henting av journalposter på arkivsaker={}.", arkivsaksId, e);
			} else {
				log.error("finnJournalposter feilet ved henting av journalposter på arkivsaker. Det var flere enn 1000 arkivsaker. Disse logges ikke da så lange logglinjer ikke støttes i logstash.", e);
			}
			return emptyMap();
		}
	}

	public Optional<PaginatedArkivJournalpost> findTilgangJournalposterStatus(LocalDate fraDato,
																			  List<Journalposttype> inkluderJournalposttyper,
																			  JournalStatusCode journalstatus,
																			  Integer foerste, String etterPeker) {
		try {
			return Optional.of(dokarkivConsumer.finnJournalposterStatus(journalstatus,
					inkluderJournalposttyper, fraDato, foerste, etterPeker, emptySet()));
		} catch (Exception e) {
			log.warn(String.format("finnJournalposterStatus feilet ved henting av journalposter med fraDato=%s, journalstatus=%s, inkluderJournalposttyper=%s.",
					fraDato, journalstatus, inkluderJournalposttyper), e);
			return Optional.empty();
		}
	}

	public static TilgangJournalpost mapTilgangJournalpost(JournalpostDto dto) {
		return TilgangJournalpost.builder()
				.journalpostId(dto.getJournalpostId().toString())
				.journalstatus(dto.getJournalstatus().toSafJournalstatus())
				.skjerming(SkjermingTypeCode.toSafSkjerming(dto.getSkjerming()))
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
								.toList()
						)
						.build()).toList())
				.build();
	}
}
