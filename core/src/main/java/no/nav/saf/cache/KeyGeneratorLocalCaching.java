package no.nav.saf.cache;

import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;

/**
 * Nøklene genereres med følgende format: key = "ressurs:*ressurs*" + "unik_id_for_lokal_caching"
 */
public final class KeyGeneratorLocalCaching extends AbstractKeyGeneratorCaching {

	private KeyGeneratorLocalCaching() {
	}

	public static String getKeyForPep2d(Tema tema) {
		return createIdentifierPair(RESSURS, RESSURS_SAK_DOKUMENT) + createUniqueIdentifier(
				createIdentifierPair(TEMA, tema.name())
		);
	}

	public static String getKeyForPep5(String journalpostId, String dokumentInfoId) {
		return createIdentifierPair(RESSURS, RESSURS_DOKUMENT_METADATA) + createUniqueIdentifier(
				createIdentifierPair(JOURNALPOST_ID, journalpostId),
				createIdentifierPair(DOKUMENTINFO_ID, dokumentInfoId)
		);
	}

	public static String getKeyForPep6d(String journalpostId, String dokumentInfoId, String variantFormat, String skjerming) {
		return createIdentifierPair(RESSURS, RESSURS_DOKUMENT_FIL) + createUniqueIdentifier(
				createIdentifierPair(JOURNALPOST_ID, journalpostId),
				createIdentifierPair(DOKUMENTINFO_ID, dokumentInfoId),
				createIdentifierPair(VARIANTFORMAT, variantFormat),
				createIdentifierPair(SKJERMING, skjerming)
		);
	}

	public static String getKeyForPep7d(Arkivsakssystem arkivsaksystem, String arkivsaksnummer) {
		return createIdentifierPair(RESSURS, RESSURS_SAK_DOKUMENT) + createUniqueIdentifier(
				createIdentifierPair(ARKIVSAKSSYSTEM, arkivsaksystem.name()),
				createIdentifierPair(ARKIVSAKSNUMMER, arkivsaksnummer)
		);
	}
}
