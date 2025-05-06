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
			case DENY_REASON_IKKE_AVSENDER_MOTTAKER ->
					"Avsender / mottakers fødselsnummer er ikke lik brukerens fødselsnummer";
			case DENY_REASON_FOER_INNSYNSDATO -> "Journalposten er opprettet eller journalført før 4. juni 2016";
			case DENY_REASON_UGYLDIG_JOURNALSTATUS -> "Journalposten har ikke status mottatt eller ferdigstilt";
			case DENY_REASON_FEILREGISTRERT -> "Journalposten er feilregistrert";
			case DENY_REASON_TEMAER_UNNTATT_INNSYN ->
					"Journalpost har et tema som ikke skal være synlig for bruker på nav.no";
			case DENY_REASON_POL_GDPR -> "Journalposten er skjermet etter personopplysningsloven / GDPR";
			case DENY_REASON_NOTAT -> "Journalposten er et internt notat";
			case DENY_REASON_SKJULT_INNSYN -> "Journalposten er skjult for bruker";
			case DENY_REASON_SKANNET_DOKUMENT -> "Dokumentet er skannet";
			case DENY_REASON_TEKNISK_DOKUMENT -> "Dokumentet er mottatt fra/sendt til en teknisk kanal";
			case DENY_REASON_UGYLDIG_VARIANTFORMAT -> "Dokumentet har et variantformat som ikke vises på nav.no";
			case DENY_REASON_KASSERT -> "Dokumentet er kassert";
		};
	}
}
