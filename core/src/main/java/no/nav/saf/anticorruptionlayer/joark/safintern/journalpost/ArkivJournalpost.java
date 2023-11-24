package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record ArkivJournalpost(
		// brukt til tilgangskontroll i hentdokument
		Long journalpostId,
		String fagomraade,
		String status,
		String skjerming,
		ArkivBruker bruker,
		ArkivSaksrelasjon saksrelasjon,
		//
		Integer antallRetur,
		ArkivAvsenderMottaker avsenderMottaker,
		String behandlingstema,
		String behandlingstemanavn,
		String fagomraadenavn,
		String innhold,
		String innsyn,
		String journalfoerendeEnhet,
		String journalfoertAvNavn,
		String kanalreferanseId,
		String mottakskanal,
		String opprettetAvNavn,
		ArkivRelevanteDatoer relevanteDatoer,
		Map<String, String> tilleggsopplysninger,
		String type,
		ArkivUtsendingsInfo utsendingsInfo,
		String utsendingskanal,
		List<ArkivDokumentinfo> dokumenter
) {
	public boolean isTilknyttetSak() {
		return saksrelasjon != null && saksrelasjon.sakId() != null;
	}
}
