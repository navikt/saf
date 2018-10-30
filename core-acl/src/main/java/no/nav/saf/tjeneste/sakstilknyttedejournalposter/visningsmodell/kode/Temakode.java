package no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import io.leangen.graphql.annotations.types.GraphQLType;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@GraphQLType(description = "Tema")
public enum Temakode {
	@GraphQLEnumValue(description = "Bidrag")
	BID("Bidrag"),
	@GraphQLEnumValue(description = "Pensjon")
	PEN("Pensjon"),
	@GraphQLEnumValue(description = "Øvrig")
	OVR("Øvrig"),
	@GraphQLEnumValue(description = "Skanning")
	MOT("Skanning"),
	@GraphQLEnumValue(description = "Okonomi")
	OKO("Okonomi"),
	@GraphQLEnumValue(description = "Bidrag innkreving")
	BII("Bidrag innkreving"),
	@GraphQLEnumValue(description = "FS22")
	FS22("FS22"),
	@GraphQLEnumValue(description = "Bil")
	BIL("Bil"),
	@GraphQLEnumValue(description = "Hjelpemidler")
	HJE("Hjelpemidler"),
	@GraphQLEnumValue(description = "Barnetrygd")
	BAR("Barnetrygd"),
	@GraphQLEnumValue(description = "Foreldre- og svangerskapspenger")
	FOR("Foreldre- og svangerskapspenger"),
	@GraphQLEnumValue(description = "Gravferdsstønad")
	GRA("Gravferdsstønad"),
	@GraphQLEnumValue(description = "Grunn- og hjelpestønad")
	GRU("Grunn- og hjelpestønad"),
	@GraphQLEnumValue(description = "Kontantstøtte")
	KON("Kontantstøtte"),
	@GraphQLEnumValue(description = "Omsorgspenger, Pleiepenger og opplæringspenger")
	OMS("Omsorgspenger, Pleiepenger og opplæringspenger"),
	@GraphQLEnumValue(description = "Supplerende stønad")
	SUP("Supplerende stønad"),
	@GraphQLEnumValue(description = "Yrkesskade / Menerstatning")
	YRK("Yrkesskade / Menerstatning"),
	@GraphQLEnumValue(description = "Enslig forsørger")
	ENF("Enslig forsørger"),
	@GraphQLEnumValue(description = "Stønadsregnskap")
	STO("Stønadsregnskap"),
	@GraphQLEnumValue(description = "Forsikring")
	FOS("Forsikring"),
	@GraphQLEnumValue(description = "Erstatning")
	ERS("Erstatning"),
	@GraphQLEnumValue(description = "Saksomkostning")
	SAK("Saksomkostning"),
	@GraphQLEnumValue(description = "Dagpenger")
	DAG("Dagpenger"),
	@GraphQLEnumValue(description = "Individstønad")
	IND("Individstønad"),
	@GraphQLEnumValue(description = "Mob.stønad")
	MOB("Mob.stønad"),
	@GraphQLEnumValue(description = "Oppfølging")
	OPP("Oppfølging"),
	@GraphQLEnumValue(description = "Ventelønn")
	VEN("Ventelønn"),
	@GraphQLEnumValue(description = "Yrkesrettet attføring")
	YRA("Yrkesrettet attføring"),
	@GraphQLEnumValue(description = "Rehabilitering")
	REH("Rehabilitering"),
	@GraphQLEnumValue(description = "Uføreytelser")
	UFO("Uføreytelser"),
	@GraphQLEnumValue(description = "Sykepenger")
	SYK("Sykepenger"),
	@GraphQLEnumValue(description = "Sykemelding")
	SYM("Sykemelding"),
	@GraphQLEnumValue(description = "Feilutbetaling (Arenaytelser)")
	FEI("Feilutbetaling (Arenaytelser)"),
	@GraphQLEnumValue(description = "Generell")
	GEN("Generell"),
	@GraphQLEnumValue(description = "Arbeidsavklaringspenger")
	AAP("Arbeidsavklaringspenger"),
	@GraphQLEnumValue(description = "Fullmakt")
	FUL("Fullmakt"),
	@GraphQLEnumValue(description = "Helsetjenester og ort. Hjelpemidler")
	HEL("Helsetjenester og ort. Hjelpemidler"),
	@GraphQLEnumValue(description = "Condictio indebiti")
	CON("Condictio indebiti"),
	@GraphQLEnumValue(description = "Medlemskap")
	MED("Medlemskap"),
	@GraphQLEnumValue(description = "Ukjent")
	UKJ("Ukjent"),
	@GraphQLEnumValue(description = "Tiltak")
	TIL("Tiltak"),
	@GraphQLEnumValue(description = "Rekruttering og Stilling")
	REK("Rekruttering og Stilling"),
	@GraphQLEnumValue(description = "Inkluderende Arbeidsliv")
	IAR("Inkluderende Arbeidsliv"),
	@GraphQLEnumValue(description = "Ajourhold - Grunnopplysninger")
	AGR("Ajourhold - Grunnopplysninger"),
	@GraphQLEnumValue(description = "Trekk")
	TRK("Trekk"),
	@GraphQLEnumValue(description = "Kontroll")
	KTR("Kontroll"),
	@GraphQLEnumValue(description = "Permittering og masseoppsigelser")
	PER("Permittering og masseoppsigelser"),
	@GraphQLEnumValue(description = "AA-registeret")
	AAR("AA-registeret"),
	@GraphQLEnumValue(description = "Trygdeavgift")
	TRY("Trygdeavgift"),
	@GraphQLEnumValue(description = "Sanksjon - Arbeidsgiver")
	SAA("Sanksjon - Arbeidsgiver"),
	@GraphQLEnumValue(description = "Sanksjon - Person")
	SAP("Sanksjon - Person"),
	@GraphQLEnumValue(description = "Oppfølging")
	OPA("Oppfølging"),
	@GraphQLEnumValue(description = "Serviceklager")
	SER("Serviceklager"),
	@GraphQLEnumValue(description = "Sikkerhetstiltak")
	SIK("Sikkerhetstiltak"),
	@GraphQLEnumValue(description = "Unntak fra medlemskap")
	UFM("Unntak fra medlemskap"),
	@GraphQLEnumValue(description = "Tilleggsstønad arbeidsøkere")
	TSR("Tilleggsstønad arbeidsøkere"),
	@GraphQLEnumValue(description = "Tilleggsstønad")
	TSO("Tilleggsstønad"),
	@GraphQLEnumValue(description = "Rettferdsvederlag")
	RVE("Rettferdsvederlag"),
	@GraphQLEnumValue(description = "Retting av personopplysninger")
	RPO("Retting av personopplysninger"),
	@GraphQLEnumValue(description = "Farskap")
	FAR("Farskap");

	private final String temanavn;

	Temakode(final String temanavn) {
		this.temanavn = temanavn;
	}

	public String getTemanavn() {
		return temanavn;
	}
}
