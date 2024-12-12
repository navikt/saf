package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.domain.visningsmodell.BrukerTilgangAvvistBegrunnelse;
import no.nav.safselvbetjening.tilgang.TilgangDenyReason;

import java.util.List;

public class TilgangAvvistMapper {
	public static List<BrukerTilgangAvvistBegrunnelse> mapbrukerTilgangAvvistBegrunnelser(List<TilgangDenyReason> tilgangDenyReasons) {
		return tilgangDenyReasons.stream()
				.map(denyReason -> new BrukerTilgangAvvistBegrunnelse(
						denyReason.reason,
						mapTilgangAvvistHumanReadable(denyReason)
				))
				.toList();
	}

	private static String mapTilgangAvvistHumanReadable(TilgangDenyReason denyReason) {
		return switch (denyReason) {
			case DENY_REASON_ANNEN_PART ->
					"Brukeren kan ikke se dokumentet fordi dokumentet er sendt til/fra andre parter enn bruker.";
			case DENY_REASON_INNSYNSDATO ->
					"Brukeren kan ikke se journalposten fordi journalposten er opprettet før tidligste innsynsdato (04.06.2016).";
			case DENY_REASON_UGYLDIG_JOURNALSTATUS ->
					"Brukeren kan ikke se journalposten fordi journalposten ikke har status ferdigstilt eller midlertidig.";
			case DENY_REASON_FEILREGISTRERT ->
					"Brukeren kan ikke se journalposten fordi journalposten er feilregistrert.";
			case DENY_REASON_TEMAER_UNNTATT_INNSYN ->
					"Brukeren kan ikke se journalposten fordi journalposten er markert som kontrollsak eller farskapssak.";
			case DENY_REASON_GDPR -> "Brukeren kan ikke se journalposten ihht. GDPR.";
			case DENY_REASON_FORVALTNINGSNOTAT ->
					"Brukeren kan ikke se journalposten fordi journalposten er et notat, men hoveddokumentet er ikke et forvaltningsnotat.";
			case DENY_REASON_SKJULT_INNSYN -> "Brukeren kan ikke se journalposten fordi journalposten er skjult.";
			case DENY_REASON_SKANNET_DOKUMENT -> "Brukeren kan ikke se dokumentet fordi dokumentet er skannet.";
			case DENY_REASON_UGYLDIG_VARIANTFORMAT ->
					"Brukeren kan ikke se dokumentet fordi bruker kun kan se dokument med variantformat enten SLADDET eller ARKIV.";
			case DENY_REASON_KASSERT -> "Brukeren kan ikke se dokumentet fordi dokumentet er kassert.";
		};
	}
}
