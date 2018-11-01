package no.nav.saf.tjeneste.visningsmodell.kode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public enum Temakode {
	BID("Bidrag"),
	PEN("Pensjon"),
	OVR("Øvrig"),
	MOT("Skanning"),
	OKO("Okonomi"),
	BII("Bidrag innkreving"),
	FS22("FS22"),
	BIL("Bil"),
	HJE("Hjelpemidler"),
	BAR("Barnetrygd"),
	FOR("Foreldre- og svangerskapspenger"),
	GRA("Gravferdsstønad"),
	GRU("Grunn- og hjelpestønad"),
	KON("Kontantstøtte"),
	OMS("Omsorgspenger, Pleiepenger og opplæringspenger"),
	SUP("Supplerende stønad"),
	YRK("Yrkesskade / Menerstatning"),
	ENF("Enslig forsørger"),
	STO("Stønadsregnskap"),
	FOS("Forsikring"),
	ERS("Erstatning"),
	SAK("Saksomkostning"),
	DAG("Dagpenger"),
	IND("Individstønad"),
	MOB("Mob.stønad"),
	OPP("Oppfølging"),
	VEN("Ventelønn"),
	YRA("Yrkesrettet attføring"),
	REH("Rehabilitering"),
	UFO("Uføreytelser"),
	SYK("Sykepenger"),
	SYM("Sykemelding"),
	FEI("Feilutbetaling (Arenaytelser)"),
	GEN("Generell"),
	AAP("Arbeidsavklaringspenger"),
	FUL("Fullmakt"),
	HEL("Helsetjenester og ort. Hjelpemidler"),
	CON("Condictio indebiti"),
	MED("Medlemskap"),
	UKJ("Ukjent"),
	TIL("Tiltak"),
	REK("Rekruttering og Stilling"),
	IAR("Inkluderende Arbeidsliv"),
	AGR("Ajourhold - Grunnopplysninger"),
	TRK("Trekk"),
	KTR("Kontroll"),
	PER("Permittering og masseoppsigelser"),
	AAR("AA-registeret"),
	TRY("Trygdeavgift"),
	SAA("Sanksjon - Arbeidsgiver"),
	SAP("Sanksjon - Person"),
	OPA("Oppfølging"),
	SER("Serviceklager"),
	SIK("Sikkerhetstiltak"),
	UFM("Unntak fra medlemskap"),
	TSR("Tilleggsstønad arbeidsøkere"),
	TSO("Tilleggsstønad"),
	RVE("Rettferdsvederlag"),
	RPO("Retting av personopplysninger"),
	FAR("Farskap");

	private final String temanavn;

	Temakode(final String temanavn) {
		this.temanavn = temanavn;
	}

	public String getTemanavn() {
		return temanavn;
	}
}
