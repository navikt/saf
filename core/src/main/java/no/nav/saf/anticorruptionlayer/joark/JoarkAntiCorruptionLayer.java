package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;

import java.time.LocalDate;
import java.util.List;

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
	 * Henter en journalpost fra joark basert på enten journalpostId eller eksternReferanseId
	 *
	 * @param journalpostId Hvis angitt, henter
	 * @param eksternReferanseId Hentes kun hvis journalpostId ikke er angitt
	 * @return Journalposten fra joark
	 * @throws no.nav.saf.exceptions.JournalpostIkkeFunnetException
	 */
	ArkivJournalpost hentJournalpost(String journalpostId, String eksternReferanseId);
}
