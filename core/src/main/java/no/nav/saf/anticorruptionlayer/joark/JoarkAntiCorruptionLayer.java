package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface JoarkAntiCorruptionLayer {

	List<JournalpostDto> finnJournalposter(List<String> identer,
										   List<TilgangSak> tilgangSakList,
										   LocalDate fraDato,
										   LocalDate tilDato,
										   List<Journalposttype> inkluderJournalposttyper,
										   List<Journalstatus> inkluderJournalstatuses,
										   Integer foerste, String etterPeker);

	List<JournalpostDto> finnJournalposterStatus(LocalDate fraDato,
												 List<Journalposttype> inkluderJournalposttyper,
												 Journalstatus journalstatus,
												 Integer foerste, String etterPeker);

	/**
	 * Henter en journalpost fra joark basert på enten journalpostId
	 *
	 * @param journalpostId journalpostId
	 * @param fields        felt som skal hentes fra joark
	 * @return Journalposten fra joark
	 * @throws no.nav.saf.exceptions.JournalpostIkkeFunnetException
	 */
	ArkivJournalpost hentJournalpostById(String journalpostId, Set<String> fields);

	/**
	 * Henter en journalpost fra joark basert på eksternReferanseId
	 *
	 * @param eksternReferanseId eksternReferanseId
	 * @param fields             felt som skal hentes fra joark
	 * @return Journalposten fra joark
	 * @throws no.nav.saf.exceptions.JournalpostIkkeFunnetException
	 */
	ArkivJournalpost hentJournalpostByEksternReferanseId(String eksternReferanseId, Set<String> fields);
}
