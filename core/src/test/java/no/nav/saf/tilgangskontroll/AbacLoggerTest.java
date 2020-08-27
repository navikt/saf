package no.nav.saf.tilgangskontroll;

import static org.junit.jupiter.api.Assertions.assertEquals;

import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.junit.jupiter.api.Test;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class AbacLoggerTest {

	private final AbacLogger abacLogger = new AbacLogger() {
		@Override
		public void logAbacDeny(XacmlRequest xacmlRequest, XacmlResponse xacmlResponse) {

		}

		@Override
		public void logAbacPermit(XacmlRequest xacmlRequest, XacmlResponse xacmlResponse) {

		}
	};

	@Test
	void shouldSanitizeFnr() {
		final String scrubbed = abacLogger.sanitizeFnr("Authorization Request: domene=saf, resource_type=no.nav.abac.attributter.resource.saf.person, fnr=11111111111.  Authorization Response: decision=Deny, cause=cause-0002-ikketilgangtilNAVbrukersenhet, deny_policy=fp4_geografi, deny_rule=ingen_tilgang_enhet;");
		assertEquals("Authorization Request: domene=saf, resource_type=no.nav.abac.attributter.resource.saf.person, fnr=****.  Authorization Response: decision=Deny, cause=cause-0002-ikketilgangtilNAVbrukersenhet, deny_policy=fp4_geografi, deny_rule=ingen_tilgang_enhet;", scrubbed);
	}

	@Test
	void shouldNotSanitizeStringNotContainingFnrMatch() {
		final String message = "Authorization Request: domene=saf, resource_type=no.nav.abac.attributter.resource.saf.organisasjon, orgnr=999999999.  Authorization Response: decision=Deny, cause=cause-0002-ikketilgangtilNAVbrukersenhet, deny_policy=fp4_geografi, deny_rule=ingen_tilgang_enhet;";
		final String scrubbed = abacLogger.sanitizeFnr(message);
		assertEquals(message, scrubbed);
	}
}