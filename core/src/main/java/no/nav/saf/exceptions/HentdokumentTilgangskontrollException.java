package no.nav.saf.exceptions;

import lombok.Getter;
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;
import no.nav.saf.tilgangskontroll.pep.reasons.AbacDenyReason;
import no.nav.saf.tilgangskontroll.pep.reasons.TemaReason;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class HentdokumentTilgangskontrollException extends SafFunctionalException {
	public static final String REASON_CODE = "reason_code";
	public static final String REASON_MESSAGE = "reason_message";

	private static final String MAA_HA_EGEN_ANSATT = " Arbeidet må i stedet utføres av en medarbeider med egen-ansatt-tilgang.";
	private static final String MAA_HA_FORTROLIG_TILGANG = " Arbeidet må i stedet utføres av en medarbeider med tilgang til brukere med fortrolig adresse.";
	private static final String VIKAFOSSEN = " Arbeidet må i stedet utføres av NAV Vikafossen (2103).";
	private static final String MAA_HA_GEOGRAFI = " Arbeidet må i stedet utføres av en medarbeider med tilgang til brukeren.";
	private static final String FAGPOST = " Arbeidet må i stedet utføres av NAV Fagpost (2950).";

	private final String denyReason;
	private final AbacDenyReason abacDenyReason;

	public HentdokumentTilgangskontrollException(String message, AbacAnswer abacAnswer) {
		super("Avvist av SAF tilgangskontroll: " + message);
		this.denyReason = abacAnswer.getDenyReasonSporing();
		this.abacDenyReason = abacAnswer.getAbacDenyReason();
	}

	@Override
	public Map<String, Object> getExtensions() {
		var map = new HashMap<>(super.getExtensions());
		map.put(REASON_CODE, abacDenyReason.getAbacDenyReasonCode().code);
		map.put(REASON_MESSAGE, getDenyReasonHumanReadable(abacDenyReason));
		return map;
	}

	public static String getDenyReasonHumanReadable(AbacDenyReason abacDenyReason) {
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

}
