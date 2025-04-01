package no.nav.saf.cache;

import no.nav.saf.domain.kode.Tema;

/**
 * Nøklene genereres med følgende format: key = "ressurs:*ressurs*" + "unik_id_for_distribuert_caching"
 */
public final class KeyGeneratorDistributedCaching extends AbstractKeyGeneratorCaching {

	private KeyGeneratorDistributedCaching() {}

	public static String getKeyForPep2d(String saksbehandlerId, Tema tema) {
		return createIdentifierPair(RESSURS, RESSURS_SAK_DOKUMENT) + createUniqueIdentifier(createIdentifierPair(SAKSBEHANDLER_ID, saksbehandlerId), createIdentifierPair(TEMA, tema.name()));
	}

	public static String getKeyForPep6d(String saksbehandlerId, String journalpostId, String dokumentInfoId, String variantFormat, String skjerming) {
		return createIdentifierPair(RESSURS, RESSURS_DOKUMENT_FIL) + createUniqueIdentifier(createIdentifierPair(SAKSBEHANDLER_ID, saksbehandlerId),
				createIdentifierPair(JOURNALPOST_ID, journalpostId), createIdentifierPair(DOKUMENTINFO_ID, dokumentInfoId), createIdentifierPair(VARIANTFORMAT, variantFormat),
				createIdentifierPair(SKJERMING, skjerming));
	}

	public static String getKeyForSaksbehandlerGroupMembership(String saksbehandlerId) {
		return createIdentifierPair(SAKSBEHANDLER_ID, saksbehandlerId);
	}
}
