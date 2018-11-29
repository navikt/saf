package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.tjeneste.visningsmodell.kode.Journazz;

import java.util.Arrays;
import java.util.List;

public enum JournalStatusCode {
	/**
	 * journalført
	 */
	J(Journazz.JOURNALFOERT),
	/**
	 * midl journalført
	 */
	M(Journazz.MOTTATT),
	/**
	 * Utgår før tilknytn til sak
	 */
	U(Journazz.UTGAAR),
	/**
	 * Dokument under produksjon
	 */
	D(Journazz.UNDER_ARBEID),
	/**
	 * Reservert dokument
	 */
	R(Journazz.RESERVERT),
	/**
	 * Ferdig og sentral print
	 */
	FS(Journazz.FERDIGSTILT),
	/**
	 * Ferdig og lokal print
	 */
	FL(Journazz.FERDIGSTILT),
	/**
	 * Ekspedert
	 */
	E(Journazz.EKSPEDERT),
	/**
	 * Avbrutt
	 */
	A(Journazz.AVBRUTT),
	/**
	 * Mottatt   
	 */
	MO(Journazz.MOTTATT),
	/**
	 * Ukjent bruker 
	 */
	UB(Journazz.UKJENT_BRUKER),
	/**
	 * Opplasting dokument 
	 */
	OD(Journazz.OPPLASTING_DOKUMENT);

	private final Journazz safJournazz;

	JournalStatusCode(Journazz safJournazz) {
		this.safJournazz = safJournazz;
	}

	public Journazz toSafJournalStatus() {
		return safJournazz;
	}

	public static List<JournalStatusCode> asList() {
		return Arrays.asList(values());
	}
}
