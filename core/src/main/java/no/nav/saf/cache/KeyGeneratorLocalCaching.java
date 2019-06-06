package no.nav.saf.cache;

/**
 * @author Sigurd Midttun, Visma Consulting.
 * <p>
 * Nøklene genereres med følgende format: key = "ressurstype:*ressurstype*" + "unik_id_for_lokal_caching"
 */
public final class KeyGeneratorLocalCaching extends AbstractKeyGeneratorCaching {

	private KeyGeneratorLocalCaching() {
		super();
	}

	public static String getKeyForPep2d(String tema) {
		return createIdentifierPair(RESSURS, RESSURS_SAK_DOKUMENT) + createUniqueIdentifier(createIdentifierPair(TEMA, tema));
	}

	public static String getKeyForPep5(String journalpostId, String dokumentInfoId) {
		return createIdentifierPair(RESSURS, RESSURS_DOKUMENT_METADATA) + createUniqueIdentifier(createIdentifierPair(JOURNALPOST_ID, journalpostId),
				createIdentifierPair(DOKUMENTINFO_ID, dokumentInfoId));
	}

	public static String getKeyForPep6d(String journalpostId, String dokumentInfoId, String variantFormat, String skjerming) {
		return createIdentifierPair(RESSURS, RESSURS_DOKUMENT_FIL) + createUniqueIdentifier(createIdentifierPair(JOURNALPOST_ID, journalpostId), createIdentifierPair(DOKUMENTINFO_ID, dokumentInfoId), createIdentifierPair(VARIANTFORMAT, variantFormat),
				createIdentifierPair(SKJERMING, skjerming));
	}
}
