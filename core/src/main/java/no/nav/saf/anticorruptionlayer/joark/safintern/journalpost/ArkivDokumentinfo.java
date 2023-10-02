package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import java.util.List;

public record ArkivDokumentinfo(Long dokumentInfoId, String skjerming, List<ArkivFildetaljer> fildetaljer) {
}
