package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.AbacDenyReason;
import no.nav.saf.tilgangskontroll.pep.reasons.TemaReason;

import static no.nav.saf.tilgangskontroll.pep.Pep1gImpl.ORGANISASJON_ER_NAV_STAT_KREVER_EGEN_ANSATT_TILGANG;

/**
 * Menneskelesbare grunner til deny fra PEP.
 */
public final class DenyReasonFactory {
	private static final String DENY_PREFIX = "Tilgang til ressurs (journalpost/dokument) ble avvist. ";
	private static final String CONTACT_US_SUFFIX = " Hvis dette ikke fungerer; kontakt oss på #team_dokumentløsninger";
	public static final String PEP1G_DENY_REASON =
			" har ikke tilgang til ressurs tilhørende bruker som har kode 6/7 (strengt fortrolig/fortrolig adressesperre), egen ansatt eller utenfor tillatt geografisk område.";

	private static final String PEP1G_DENY_SAKSBEHANDLER_INFO =
			" Saksbehandler må ha tilgang til Enhet som brukeren er tilknyttet i AXSYS.";
	public static final String PEP2_DENY_REASON =
			" har ikke tilgang til tema ressursen tilhører eller på grunn av Forvaltningsloven § 19.";

	private static final String PEP2D_DENY_SYSTEM_INFO = "System har ikke tilgang til tema ressursen tilhører. " +
														 "\"tema_%s\" må ligge i feltet \"roles\" i Azure IAC-konfigurasjonen for konsumenten i saf sin nais-konfigurasjon.";
	private static final String PEP2D_DENY_SAKSBEHANDLER_INFO = "Saksbehandler har ikke tilgang til tema ressursen tilhører eller geografisk område. " +
																"Saksbehandler må ha tilgang til Enhet som brukeren er tilknyttet, med Fagområde=%s i AXSYS.";

	public static final String PEP3_DENY_REASON =
			" har ikke tilgang til ressurs der en av partene i bidragssaken har kode 6/7 (strengt fortrolig/fortrolig adressesperre) eller egen ansatt.";
	public static final String PEP4_DENY_REASON =
			" har ikke tilgang til ressurs på grunn av journalposten sin status.";
	public static final String PEP5_DENY_REASON =
			" har ikke tilgang til ressurs som er skjermet eller begrenset.";
	public static final String PEP6D_DENY_REASON =
			" har ikke tilgang til ressurs som er skjermet, begrenset eller logisk kassert.";
	public static final String PEP7D_DENY_REASON =
			" har ikke tilgang til ressurs der relevante parter på sak har kode 6/7 (strengt fortrolig/fortrolig adressesperre).";

	private DenyReasonFactory() {
		// noop
	}

	private static String saksbehandlerEllerSystem(boolean isSystem) {
		return isSystem ? "System" : "Saksbehandler";
	}

	public static String createPep1gDenyReasonDokumentoversikt(SafRequestContext safRequestContext, AbacAnswer abacAnswer) {
		boolean isSystem = safRequestContext.getSecurityContext().isSystem();
		String consumerType = saksbehandlerEllerSystem(isSystem);
		return switch (abacAnswer.getDenyReasonSporing()) {
			case "saf_info=" + ORGANISASJON_ER_NAV_STAT_KREVER_EGEN_ANSATT_TILGANG ->
					"Tilgang til dokumentoversikt for organisasjon ble avvist. " +
					"Organisasjonen er underlagt NAV og det krever egen ansatt behandling for oppslag på denne. " +
					"NAV ansatt må være medlem av gruppen 0000-GA-Egne_ansatte";
			default -> "Tilgang til dokumentoversikt ble avvist. " + consumerType + PEP1G_DENY_REASON +
					   (isSystem ? "" : PEP1G_DENY_SAKSBEHANDLER_INFO) + CONTACT_US_SUFFIX;
		};
	}

	public static String createPep1gDenyReason(SafRequestContext safRequestContext, AbacAnswer abacAnswer) {
		boolean isSystem = safRequestContext.getSecurityContext().isSystem();
		if (abacAnswer.getAbacDenyReason().getAbacDenyReasonCode() == AbacAnswer.AbacDenyReasonCode.ORGNR_NAV_STAT) {
			return DENY_PREFIX +
					"Journalpost/dokument er knyttet til organisasjon underlagt NAV og det krever egen ansatt behandling for oppslag på denne. " +
					"NAV ansatt må være medlem av gruppen 0000-GA-Egne_ansatte";
		}
		return DENY_PREFIX + saksbehandlerEllerSystem(isSystem) + PEP1G_DENY_REASON +
				(isSystem ? "" : PEP1G_DENY_SAKSBEHANDLER_INFO) + CONTACT_US_SUFFIX;
	}

	public static String createPep2DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP2_DENY_REASON;
	}

	public static String createPep2dDenyReason(SafRequestContext safRequestContext, TilgangSak tilgangSak) {
		if (tilgangSak == null) {
			return DENY_PREFIX + (safRequestContext.getSecurityContext().isSystem() ? PEP2D_DENY_SYSTEM_INFO.formatted("ukjent") : PEP2D_DENY_SAKSBEHANDLER_INFO.formatted("UKJENT")) + CONTACT_US_SUFFIX;
		}
		Tema tema = tilgangSak.getTema();
		return DENY_PREFIX + (safRequestContext.getSecurityContext().isSystem() ? PEP2D_DENY_SYSTEM_INFO.formatted(tema.name().toLowerCase()) : PEP2D_DENY_SAKSBEHANDLER_INFO.formatted(tema.name())) + CONTACT_US_SUFFIX;
	}

	public static String createPep3DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP3_DENY_REASON;
	}

	public static String createPep4DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP4_DENY_REASON;
	}

	public static String createPep5DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP5_DENY_REASON;
	}

	public static String createPep6dDenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP6D_DENY_REASON;
	}

	public static String createPep7dDenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP7D_DENY_REASON;
	}

	public static String getDenyReasonHumanReadable(SafRequestContext safRequestContext, AbacDenyReason abacDenyReason) {
		// I fremtiden (når vi bytter til java 21) kan denne erstattes med pattern matching for AbacDenyReason (som er et sett med sealed classes)
		return switch (abacDenyReason.getAbacDenyReasonCode()) {
			case EGEN_ANSATT -> "Du har ikke tilgang til brukeren fordi vedkommende er NAV-ansatt, eller er i nær familie med en NAV-ansatt." + MAA_HA_EGEN_ANSATT;
			case EGEN_ANSATT_PART -> "Du har ikke tilgang til journalpost / dokument fordi den er journalført mot en fagsak der en av partene er NAV-ansatt." + MAA_HA_EGEN_ANSATT;
			case ORGNR_NAV_STAT -> "Du har ikke tilgang fordi organisasjonsnummeret tilhører NAV. " + MAA_HA_EGEN_ANSATT;
			case FORTROLIG_ADRESSE -> "Du har ikke tilgang til brukeren fordi han / hun har fortrolig adresse." + MAA_HA_FORTROLIG_TILGANG;
			case FORTROLIG_ADRESSE_PART -> "Du har ikke tilgang til journalpost / dokument fordi den er journalført mot en fagsak der en av partene har fortrolig adresse." + MAA_HA_FORTROLIG_TILGANG;
			case GEOGRAFI -> "Du har ikke tilgang til brukeren fordi han / hun er folkeregistrert i et geografisk område du ikke har tilgang til." + MAA_HA_GEOGRAFI;
			case JOURNALSTATUS -> "Du har ikke tilgang til journalpost / dokument fordi den har status Utgår eller Ukjent Bruker." + FAGPOST;
			case SKJERMING -> "Du har ikke tilgang til journalpost / dokument fordi den er skjermet eller kassert." + FAGPOST;
			case STRENGT_FORTROLIG_ADRESSE -> "Du har ikke tilgang til brukeren fordi han / hun har strengt fortrolig adresse." + VIKAFOSSEN;
			case STRENGT_FORTROLIG_ADRESSE_UTLAND -> "Du har ikke tilgang til brukeren fordi han / hun har strengt fortrolig adresse utland." + VIKAFOSSEN;
			case STRENGT_FORTROLIG_ADRESSE_PART -> "Du har ikke tilgang til journalpost / dokument fordi den er journalført mot en fagsak der en av partene har strengt fortrolig adresse." + VIKAFOSSEN;
			case STRENGT_FORTROLIG_ADRESSE_UTLAND_PART -> "Du har ikke tilgang til journalpost / dokument fordi den er journalført mot en fagsak der en av partene har strengt fortrolig adresse utland." + VIKAFOSSEN;
			case TEMA -> "Du har ikke tilgang til journalpost / dokument fordi du mangler tilgang til tema " + ((TemaReason)abacDenyReason).getTemaForHumanDisplay() +
					". Arbeidet må i stedet utføres av en medarbeider med tilgang til temaet.";
			case UKJENT -> "Du har blitt nektet tilgang av en ukjent grunn, eller på grunn av teknisk feil. " +
					"Prøv på nytt om litt. Om du fortsatt ikke får tilgang må du melde inn en sak til brukerstøtte i Porten.";
		};
	}

	private static final String MAA_HA_EGEN_ANSATT = " Arbeidet må i stedet utføres av en medarbeider med egen-ansatt-tilgang.";
	private static final String MAA_HA_FORTROLIG_TILGANG = " Arbeidet må i stedet utføres av en medarbeider med tilgang til brukere med fortrolig adresse.";
	private static final String VIKAFOSSEN = " Arbeidet må i stedet utføres av NAV Vikafossen (2103).";
	private static final String MAA_HA_GEOGRAFI = " Arbeidet må i stedet utføres av en medarbeider med tilgang til brukeren.";
	private static final String FAGPOST = " Arbeidet må i stedet utføres av NAV Fagpost (2950).";
}