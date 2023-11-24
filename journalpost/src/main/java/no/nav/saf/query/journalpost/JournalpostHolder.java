package no.nav.saf.query.journalpost;

import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;

public record JournalpostHolder(ArkivJournalpost arkivJournalpost, JournalpostTilgang journalpostTilgang) {
}
