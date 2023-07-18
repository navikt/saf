package no.nav.saf.domain.kode;

import java.util.Arrays;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public enum Tema {
	AAP("Arbeidsavklaringspenger"),
	AAR("Aa-registeret"),
	AGR("Ajourhold – grunnopplysninger"),
	ARP("Arbeidsrådgivning – psykologtester"),
	ARS("Arbeidsrådgivning – skjermet"),
	BAR("Barnetrygd"),
	BID("Bidrag"),
	BIL("Bil"),
	DAG("Dagpenger"),
	ENF("Enslig mor eller far"),
	ERS("Erstatning"),
	EYB("Barnepensjon"),
	EYO("Omstillingsstønad"),
	FAR("Foreldreskap"),
	FEI("Feilutbetaling"),
	FIP("Fiskerpensjon"),
	FOR("Foreldre- og svangerskapspenger"),
	FOS("Forsikring"),
	FRI("Kompensasjon for selvstendig næringsdrivende/frilansere"),
	FUL("Fullmakt"),
	GEN("Generell"),
	GRA("Gravferdsstønad"),
	GRU("Grunn- og hjelpestønad"),
	HEL("Helsetjenester og ortopediske hjelpemidler"),
	HJE("Hjelpemidler"),
	IAR("Inkluderende arbeidsliv"),
	IND("Tiltakspenger"),
	KON("Kontantstøtte"),
	KLL("Klage – lønnsgaranti"),
	KTA("Kontroll – anmeldelse"),
	KTR("Kontroll"),
	MED("Medlemskap"),
	MOB("Mobilitetsfremmende stønad"),
	OMS("Omsorgspenger, pleiepenger og opplæringspenger"),
	OPA("Oppfølging – arbeidsgiver"),
	OPP("Oppfølging"),
	PEN("Pensjon"),
	PER("Permittering og masseoppsigelser"),
	REH("Rehabiliteringspenger"),
	REK("Rekruttering"),
	RPO("Retting av personopplysninger"),
	RVE("Rettferdsvederlag"),
	SAA("Sanksjon - Arbeidsgiver"),
	SAK("Sakskostnader"),
	SAP("Sanksjon – person"),
	SER("Serviceklager"),
	SIK("Sikkerhetstiltak"),
	STO("Regnskap/utbetaling"),
	SUP("Supplerende stønad"),
	SYK("Sykepenger"),
	SYM("Sykmeldinger"),
	TIL("Tiltak"),
	TRK("Trekkhåndtering"),
	TRY("Trygdeavgift"),
	TSO("Tilleggsstønad"),
	TSR("Tilleggsstønad – arbeidssøkere"),
	UFM("Unntak fra medlemskap"),
	UFO("Uføretrygd"),
	UKJ("Ukjent"),
	VEN("Ventelønn"),
	YRA("Yrkesrettet attføring"),
	YRK("Yrkesskade og menerstatning");

	private final String temanavn;

	Tema(final String temanavn) {
		this.temanavn = temanavn;
	}

	public String getTemanavn() {
		return temanavn;
	}

	public static List<Tema> asList() {
		return Arrays.asList(values());
	}
}
