package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.domain.kode.Journalstatus;

import java.util.Arrays;
import java.util.List;

public enum JournalStatusCode {
	/**
	 * journalført
	 */
	J(Journalstatus.JOURNALFOERT),
	/**
	 * midl journalført
	 */
	M(Journalstatus.MOTTATT),
	/**
	 * Utgår før tilknytn til sak
	 */
	U(Journalstatus.UTGAAR),
	/**
	 * Dokument under produksjon
	 */
	D(Journalstatus.UNDER_ARBEID),
	/**
	 * Reservert dokument
	 */
	R(Journalstatus.RESERVERT),
	/**
	 * Ferdig og sentral print
	 */
	FS(Journalstatus.FERDIGSTILT),
	/**
	 * Ferdig og lokal print
	 */
	FL(Journalstatus.FERDIGSTILT),
	/**
	 * Ekspedert
	 */
	E(Journalstatus.EKSPEDERT),
	/**
	 * Avbrutt
	 */
	A(Journalstatus.AVBRUTT),
	/**
	 * Mottatt
	 */
	MO(Journalstatus.MOTTATT),
	/**
	 * Ukjent bruker
	 */
	UB(Journalstatus.UKJENT_BRUKER),
	/**
	 * Opplasting dokument
	 */
	OD(Journalstatus.OPPLASTING_DOKUMENT);

	private final Journalstatus safJournalstatus;

	JournalStatusCode(Journalstatus safJournalstatus) {
		this.safJournalstatus = safJournalstatus;
	}

	public Journalstatus toSafJournalstatus() {
		return safJournalstatus;
	}

	public static List<JournalStatusCode> asList() {
		return Arrays.asList(values());
	}

	public static JournalStatusCode from(Journalstatus safJournalstatus) {
		return switch (safJournalstatus) {
			case MOTTATT -> M;
			case JOURNALFOERT -> J;
			case EKSPEDERT -> E;
			case UNDER_ARBEID -> D;
			case UTGAAR -> U;
			case AVBRUTT -> A;
			case UKJENT_BRUKER -> UB;
			case RESERVERT -> R;
			case OPPLASTING_DOKUMENT -> OD;
			case UKJENT -> U;
			case FERDIGSTILT ->
					throw new IllegalArgumentException("Det finnes ingen entydig mapping fra SAF journalstatus FERDIGSTILT til JournalStatusCode");
			case FEILREGISTRERT ->
					throw new IllegalArgumentException("Det finnes ingen mapping fra SAF journalstatus FEILREGISTRERT til JournalStatusCode");
		};
	}
}
