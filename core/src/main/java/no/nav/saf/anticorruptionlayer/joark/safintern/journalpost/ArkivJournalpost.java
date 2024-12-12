package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import lombok.Builder;
import no.nav.safselvbetjening.tilgang.Ident;
import no.nav.safselvbetjening.tilgang.TilgangBruker;
import no.nav.safselvbetjening.tilgang.TilgangInnsyn;
import no.nav.safselvbetjening.tilgang.TilgangJournalpost;
import no.nav.safselvbetjening.tilgang.TilgangJournalposttype;
import no.nav.safselvbetjening.tilgang.TilgangJournalstatus;
import no.nav.safselvbetjening.tilgang.TilgangMottakskanal;
import no.nav.safselvbetjening.tilgang.TilgangSak;
import no.nav.safselvbetjening.tilgang.TilgangSkjermingType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

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
		String innsynsbeskrivelse,
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

	public TilgangJournalpost getJournalpostTilgang(TilgangSak tilgangSak) {
		return TilgangJournalpost.builder()
				.journalstatus(TilgangJournalstatus.from(status))
				.journalposttype(TilgangJournalposttype.from(type))
				.mottakskanal(mottakskanal == null ? TilgangMottakskanal.IKKE_SKANNING : TilgangMottakskanal.from(mottakskanal))
				.tema(fagomraade)
				.avsenderMottakerId(mapAvsenderMottakerId())
				.datoOpprettet(relevanteDatoer == null ? LocalDateTime.MIN : relevanteDatoer.opprettet().toLocalDateTime())
				.journalfoertDato(mapJournalfoert())
				.skjerming(mapSkjermingType())
				.dokumenter(dokumenter == null ? emptyList() : dokumenter.stream().map(ArkivDokumentinfo::getTilgangDokument).toList())
				.tilgangBruker(mapTilgangBruker())
				.tilgangSak(tilgangSak)
				.innsyn(TilgangInnsyn.from(innsyn))
				.build();
	}

	private LocalDateTime mapJournalfoert() {
		if (relevanteDatoer == null || relevanteDatoer.journalfoert() == null) {
			return null;
		}
		return relevanteDatoer.journalfoert().toLocalDateTime();
	}

	private Ident mapAvsenderMottakerId() {
		if (avsenderMottaker == null || avsenderMottaker.id() == null) {
			return null;
		}
		return Ident.ofNullable(avsenderMottaker.id());
	}

	private TilgangBruker mapTilgangBruker() {
		if (bruker == null || bruker.id() == null) {
			return null;
		}

		return new TilgangBruker(Ident.of(bruker.id()));
	}

	private TilgangSkjermingType mapSkjermingType() {
		return TilgangSkjermingType.from(skjerming);
	}
}
