package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.tjeneste.visningsmodell.kode.JournalStatus;

public enum JournalStatusCode {
	/**
	 * journalført
	 */
	J(JournalStatus.JOURNALFOERT),
	/**
	 * midl journalført
	 */
	M(JournalStatus.MIDLERTIDIG),
	/**
	 * Utgår før tilknytn til sak
	 */
	U(JournalStatus.UTGAAR),
	/**
	 * Dokument under produksjon
	 */
	D(JournalStatus.UNDER_ARBEID),
	/**
	 * Reservert dokument
	 */
	R(JournalStatus.RESERVERT),
	/**
	 * Ferdig og sentral print
	 */
	FS(JournalStatus.FERDIGSTILT),
	/**
	 * Ferdig og lokal print
	 */
	FL(JournalStatus.FERDIGSTILT),
	/**
	 * Ekspedert
	 */
	E(JournalStatus.EKSPEDERT),
	/**
	 * Avbrutt
	 */
	A(JournalStatus.AVBRUTT),
	/**
	 * Mottatt   
	 */
	MO(JournalStatus.MIDLERTIDIG),
	/**
	 * Ukjent bruker 
	 */
	UB(JournalStatus.UKJENT_BRUKER),
	/**
	 * Opplasting dokument 
	 */
	OD(JournalStatus.OPPLASTING_DOKUMENT);

	private final JournalStatus safJournalStatus;

	JournalStatusCode(JournalStatus safJournalStatus) {
		this.safJournalStatus = safJournalStatus;
	}

	public JournalStatus toSafJournalStatus() {
		return safJournalStatus;
	}
}
