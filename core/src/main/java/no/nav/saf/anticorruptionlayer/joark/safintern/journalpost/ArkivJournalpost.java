package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import java.util.List;

public record ArkivJournalpost(Long journalpostId, String fagomraade, String status, String skjerming,
							   ArkivBruker bruker,
							   ArkivSaksrelasjon saksrelasjon,
							   List<ArkivDokumentinfo> dokumenter) {
	public boolean isTilknyttetSak() {
		return saksrelasjon != null;
	}
}
