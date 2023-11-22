package no.nav.saf.anticorruptionlayer.joark;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.SafToJoarkJournalstatusMapper;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.FinnJournalposterRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.FinnJournalposterResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark904.FinnJournalposterStatusRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark904.FinnJournalposterStatusResponseTo;
import no.nav.saf.anticorruptionlayer.joark.safintern.DokarkivConsumer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Component
@Slf4j
class JoarkAntiCorruptionLayerImpl implements JoarkAntiCorruptionLayer {
	private static final Set<String> SAFINTERN_FETCHPATHS_UTEN_DOKUMENTER = Set.of(
			"journalpostId",
			"fagomraade",
			"fagomraadenavn",
			"status",
			"type",
			"kanalreferanseId",
			"mottakskanal",
			"utsendingskanal",
			"behandlingstema",
			"behandlingstemanavn",
			"innhold",
			"journalfoerendeEnhet",
			"journalfoertAvNavn",
			"opprettetAvNavn",
			"antallRetur",
			"innsyn",
			"skjerming",
			"relevanteDatoer",
			"avsenderMottaker",
			"saksrelasjon",
			"bruker",
			"utsendingsInfo",
			"tilleggsopplysninger"
	);
	private static final Set<String> SAFINTERN_FETCHPATHS_DOKUMENTER = Set.of(
			"dokumenter",
			"dokumenter.dokumentInfoId",
			"dokumenter.tilknyttetSom",
			"dokumenter.kassert",
			"dokumenter.kategori",
			"dokumenter.skjerming",
			"dokumenter.fildetaljer"
	);
	private static final Set<String> SAFINTERN_FETCHPATHS_ALLE = concat(SAFINTERN_FETCHPATHS_UTEN_DOKUMENTER.stream(), SAFINTERN_FETCHPATHS_DOKUMENTER.stream()).collect(toSet());

	private final HentJournalsakinfo hentJournalsakinfo;
	private final DokarkivConsumer dokarkivConsumer;
	private final SafToJoarkJournalstatusMapper safToJoarkJournalstatusMapper;

	@Autowired
	public JoarkAntiCorruptionLayerImpl(HentJournalsakinfo hentJournalsakinfo,
										DokarkivConsumer dokarkivConsumer) {
		this.hentJournalsakinfo = hentJournalsakinfo;
		this.dokarkivConsumer = dokarkivConsumer;
		this.safToJoarkJournalstatusMapper = new SafToJoarkJournalstatusMapper();
	}

	@Override
	public List<JournalpostDto> finnJournalposter(List<String> alleIdenter,
												  List<TilgangSak> tilgangSakList,
												  LocalDate fraDato,
												  LocalDate tilDato,
												  List<Journalposttype> inkluderJournalposttyper,
												  List<Journalstatus> inkluderJournalstatuses,
												  Integer foerste, String etterPeker) {
		FinnJournalposterResponseTo responseTo = hentJournalsakinfo.finnJournalposter(FinnJournalposterRequestTo.builder()
				.alleIdenter(alleIdenter)
				.inkluderJournalpostType(inkluderJournalposttyper.stream()
						.map(jt -> JournalpostTypeCode.valueOf(jt.name()))
						.collect(Collectors.toList()))
				.inkluderJournalStatus(safToJoarkJournalstatusMapper.map(inkluderJournalstatuses))
				.visFeilregistrerte(inkluderJournalstatuses.contains(Journalstatus.FEILREGISTRERT))
				.fraDato(fraDato.toString())
				.tilDato(tilDato == null ? null : tilDato.toString())
				.gsakSakIds(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.GSAK.equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()))
				.psakSakIds(tilgangSakList.stream()
						.filter(tilgangSak -> Arkivsakssystem.PSAK.equals(tilgangSak.getArkivsaksystem()))
						.map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()))
				.foerste(foerste)
				.etterPeker(etterPeker)
				.build());

		return responseTo.getTilgangJournalposter();
	}

	@Override
	public List<JournalpostDto> finnJournalposterStatus(LocalDate fraDato,
														List<Journalposttype> inkluderJournalposttyper,
														Journalstatus journalstatus,
														Integer foerste, String etterPeker) {

		// tidligere validering skal sikre at dette er trygt
		JournalStatusCode journalStatusCode = safToJoarkJournalstatusMapper.map(Collections.singletonList(journalstatus)).get(0);

		FinnJournalposterStatusResponseTo responseTo = hentJournalsakinfo.finnJournalposterStatus(FinnJournalposterStatusRequestTo
				.builder()
				.journalposttyper(inkluderJournalposttyper.stream()
						.map(jt -> JournalpostTypeCode.valueOf(jt.name()))
						.collect(Collectors.toList()))
				.journalstatus(journalStatusCode)
				.fraDato(fraDato.toString())
				.foerste(foerste)
				.etterPeker(etterPeker)
				.build());

		return responseTo.getTilgangJournalposter();
	}

	@Override
	public ArkivJournalpost hentJournalpostById(String journalpostId, Set<String> fields) {
		if (!SAFINTERN_FETCHPATHS_ALLE.containsAll(fields)) {
			throw new SafTechnicalException("fields inneholder et felt som ikke er gyldig. fields=" + fields, INTERNAL_SERVER_ERROR);
		}
		return dokarkivConsumer.journalpostById(journalpostId, fields);
	}

	@Override
	public ArkivJournalpost hentJournalpostByEksternReferanseId(String eksternReferanseId, Set<String> fields) {
		if (!SAFINTERN_FETCHPATHS_ALLE.containsAll(fields)) {
			throw new SafTechnicalException("fields inneholder et felt som ikke er gyldig. fields=" + fields, INTERNAL_SERVER_ERROR);
		}
		return dokarkivConsumer.journalpostByEksternReferanseId(eksternReferanseId, fields);
	}
}
