package no.nav.saf.domain;

import java.time.ZoneId;

public final class DomainConstants {
	// abac
	public static final String SAF = "saf";
	public static final String ABAC_JOURNALSTATUS_UTGAAR = "U";
	// lokale caches
	public static final String TILGANG_BRUKER = "tilgangBruker";
	public static final String AKTOER_ID_LIST = "aktoerIdList";
	public static final String ORGNR_LIST = "orgnrList";
	public static final String RJOARK902_JOURNALPOST_DTO = "journalpostDto";

	public static final String FAGSAKSYSTEM_BISYS = "BISYS";
	public static final String FAGSAKSYSTEM_FORELDREPENGELOSNING = "FS36";
	public static final String FAGSAKSYSTEM_K9 = "K9";

	public static final String SAK_STATUS_AAPEN = "AAPEN";

	public static final ZoneId TIDSSONE_NORGE = ZoneId.of("Europe/Oslo");

	// PEP funksjonelt navn. Se https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll
	public static final String PEP1G = "pep1g";
	public static final String PEP2 = "pep2";
	public static final String PEP2D = "pep2d";
	public static final String PEP3 = "pep3";
	public static final String PEP4 = "pep4";
	public static final String PEP5 = "pep5";
	public static final String PEP6D = "pep6d";
	public static final String PEP7D = "pep7d";
	public static final String PEP8D = "pep8d";

	private DomainConstants() {
	}

}
