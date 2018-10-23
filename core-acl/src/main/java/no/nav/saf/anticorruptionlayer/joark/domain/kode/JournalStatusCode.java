package no.nav.saf.anticorruptionlayer.joark.domain.kode;

public enum JournalStatusCode {
	/**
	 * journalført
	 */
	J,
	/**
	 * midl journalført
	 */
	M,
	/**
	 * Utgår før tilknytn til sak
	 */
	U,
	/**
	 * Dokument under produksjon
	 */
	D,
	/**
	 * Reservert dokument
	 */
	R,
	/**
	 * Ferdig og sentral print
	 */
	FS,
	/**
	 * Ferdig og lokal print
	 */
	FL,
	/**
	 * Ekspedert
	 */
	E,
	/**
	 * Avbrutt
	 */
	A,
	/**
	 * Mottatt   
	 */
	MO,
	/**
	 * Ukjent bruker 
	 */
	UB,
	/** 
	 * Opplasting dokument 
	 */
	OD;
}
