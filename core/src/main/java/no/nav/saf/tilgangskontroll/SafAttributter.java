package no.nav.saf.tilgangskontroll;

/**
 * Attributter brukes for å angi nødvendige parametre i en forespørsel mot ABAC.
 * Mer info på Confluence: https://confluence.adeo.no/display/ABAC/ABAC+Attributtkatalog+Mk2
 *
 * SAF attributter: https://github.com/navikt/abac-saf/blob/master/abac-policies-alfa/src/main/alfa/saf-attributter.alfa
 * Felles NAV attributter: https://github.com/navikt/abac-attribute-constants/blob/master/src/main/java/no/nav/abac/xacml/NavAttributter.java
 */
public class SafAttributter {

	public static final String RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE = "no.nav.abac.attributter.resource.felles.person.aktoerId_resource";
	public static final String RESOURCE_FELLES_PERSON_FNR = "no.nav.abac.attributter.resource.felles.person.fnr";
	public static final String RESOURCE_FELLES_RESOURCE_TYPE = "no.nav.abac.attributter.resource.felles.resource_type";
	public static final String RESOURCE_FELLES_TEMA = "no.nav.abac.attributter.resource.felles.tema";
	public static final String RESOURCE_FELLES_DOMENE = "no.nav.abac.attributter.resource.felles.domene";

	public static final String ENVIRONMENT_FELLES_PEP_ID = "no.nav.abac.attributter.environment.felles.pep_id";
	public static final String ENVIRONMENT_FELLES_AZURE_JWT_TOKEN_BODY = "no.nav.abac.attributter.environment.felles.azure_jwt_token_body";
	public static final String ENVIRONMENT_FELLES_OIDC_TOKEN_BODY = "no.nav.abac.attributter.environment.felles.oidc_token_body";

	public static final String XACML_1_0_ACTION_ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";

	public static final String RESOURCE_SAF_DOKUMENT_FIL = "no.nav.abac.attributter.resource.saf.dokument_fil";
	public static final String RESOURCE_SAF_DOKUMENT_METADATA = "no.nav.abac.attributter.resource.saf.dokument_metadata";
	public static final String RESOURCE_SAF_JOURNAL_METADATA = "no.nav.abac.attributter.resource.saf.journal_metadata";
	public static final String RESOURCE_SAF_JOURNALSTATUS = "no.nav.abac.attributter.resource.saf.journalstatus";
	public static final String RESOURCE_SAF_PARAGRAF19 = "no.nav.abac.attributter.resource.saf.paragraf19";
	public static final String RESOURCE_SAF_PERSON = "no.nav.abac.attributter.resource.saf.person";
	public static final String RESOURCE_SAF_SAK_DOKUMENT = "no.nav.abac.attributter.resource.saf.sak_dokument";
	public static final String RESOURCE_SAF_SAK_JP_METADATA = "no.nav.abac.attributter.resource.saf.sak_jp_metadata";
	public static final String RESOURCE_SAF_SKJERMING = "no.nav.abac.attributter.resource.saf.skjerming";
	public static final String RESOURCE_SAF_TREDJEPART = "no.nav.abac.attributter.resource.saf.tredjepart";

	public SafAttributter() {
	}
}
