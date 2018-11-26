package no.nav.saf.tjeneste.visningsmodell.kode;

import java.util.Arrays;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public enum Temakode {
	AAP("Arbeidsavklaringspenger"),
	AAR("Aa-registeret"),
	AGR("Ajourhold - Grunnopplysninger"),
	BAR("Barnetrygd"),
	BID("1 - Bidrag"),
	BII("2 - Bidrag innkreving"),
	BIL("Bil"),
	DAG("Dagpenger"),
	ENF("Enslig forsørger"),
	ERS("Erstatning"),
	FAR("Farskap"),
	FEI("Feilutbetaling"),
	FOR("Foreldre- og svangerskapspenger"),
	FOS("Forsikring"),
	FUL("Fullmakt"),
	GEN("Generell"),
	GRA("Gravferdsstønad"),
	GRU("Grunn- og hjelpestønad"),
	HEL("Helsetjenester og ort. Hjelpemidler"),
	HJE("Hjelpemidler"),
	IAR("Inkluderende Arbeidsliv"),
	IND("Tiltakspenger"),
	KLA("Klage/Anke"),
	KNA("Kontakt NAV"),
	KOM("Kommunale tjenester"),
	KON("Kontantstøtte"),
	KTR("Kontroll"),
	LGA("Lønnsgaranti"),
	MED("Medlemskap"),
	MOB("Mob.stønad"),
	MOT("3 - Skanning"),
	OKO("Økonomi"),
	OMS("Omsorgspenger, Pleiepenger og opplæringspenger"),
	OPA("Oppfølging - Arbeidsgiver"),
	OPP("Oppfølging"),
	OVR("4 - Øvrig"),
	PEN("Pensjon"),
	PER("Permittering og masseoppsigelser"),
	REH("Rehabilitering"),
	REK("Rekruttering og Stilling"),
	RPO("Retting av personopplysninger"),
	RVE("Rettferdsvederlag"),
	SAA("Sanksjon - Arbeidsgiver"),
	SAK("Saksomkostning"),
	SAP("Sanksjon - Person"),
	SER("Serviceklager"),
	SIK("Sikkerhetstiltak"),
	STO("Regnskap/utbetaling"),
	SUP("Supplerende stønad"),
	SYK("Sykepenger"),
	SYM("Sykemeldinger"),
	TIL("Tiltak"),
	TRK("Trekkhåndtering"),
	TRY("Trygdeavgift"),
	TSO("Tilleggsstønad"),
	TSR("Tilleggsstønad arbeidssøkere"),
	UFM("Unntak fra medlemskap"),
	UFO("Uføretrygd"),
	UKJ("Ukjent"),
	VEN("Ventelønn"),
	YRA("Yrkesrettet attføring"),
	YRK("Yrkesskade / Menerstatning");

	private final String temanavn;

	Temakode(final String temanavn) {
		this.temanavn = temanavn;
	}

	public String getTemanavn() {
		return temanavn;
	}

	public static List<Temakode> asList() {
		return Arrays.asList(values());
	}
}
