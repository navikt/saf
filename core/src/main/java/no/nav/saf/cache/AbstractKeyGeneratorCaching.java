package no.nav.saf.cache;

import java.util.Arrays;
import java.util.stream.Collectors;

abstract class AbstractKeyGeneratorCaching {
	static final String RESSURS = "ressurs";
	static final String RESSURS_SAK_DOKUMENT = "sak_dokument";
	static final String RESSURS_DOKUMENT_METADATA = "dokument_metadata";
	static final String RESSURS_DOKUMENT_FIL = "dokument_fil";

	static final String NAV_IDENT_TEMA = "navIdentTema";
	static final String SAKSBEHANDLER_ID = "saksbehandlerId";
	static final String TEMA = "tema";
	static final String JOURNALPOST_ID = "journalpostId";
	static final String DOKUMENTINFO_ID = "dokumentInfoId";
	static final String VARIANTFORMAT = "variantFormat";
	static final String SKJERMING = "skjerming";
	static final String AVSLUTTET = "avsluttet";

	static final String ARKIVSAKSSYSTEM = "arkivsakssystem";
	static final String ARKIVSAKSNUMMER = "arkivsaksnummer";

	AbstractKeyGeneratorCaching() {
	}

	static String createUniqueIdentifier(String... uniqueIdentifiers) {
		return Arrays.stream(uniqueIdentifiers).map(identifier -> ";" + identifier).collect(Collectors.joining());
	}

	static String createIdentifierPair(String s1, String s2) {
		return String.format("%s:%s", s1, s2);
	}
}
