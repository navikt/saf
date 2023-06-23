package no.nav.saf.tilgangskontroll;

import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

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
	void shouldMaskereFnr() {
		final String maskertLogg = abacLogger.mapRequest(buildAbacRequest(List.of(
				Pair.of(RESOURCE_FELLES_DOMENE, "saf"),
				Pair.of(RESOURCE_FELLES_RESOURCE_TYPE, "no.nav.abac.attributter.resource.saf.person"),
				Pair.of(RESOURCE_FELLES_PERSON_FNR, "11111111111")
		)));

		assertThat(maskertLogg)
				.isEqualTo("Authorization Request: domene=saf, resource_type=no.nav.abac.attributter.resource.saf.person, fnr=***********");
	}

	@Test
	void shouldMaskereAktoerId() {
		final String maskertLogg = abacLogger.mapRequest(buildAbacRequest(List.of(
				Pair.of(RESOURCE_FELLES_DOMENE, "saf"),
				Pair.of(RESOURCE_FELLES_RESOURCE_TYPE, "no.nav.abac.attributter.resource.saf.person"),
				Pair.of(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, "3333333333333")
		)));

		assertThat(maskertLogg)
				.isEqualTo("Authorization Request: domene=saf, resource_type=no.nav.abac.attributter.resource.saf.person, aktoerId_resource=*************");
	}

	private XacmlRequest buildAbacRequest(List<Pair<String, String>> resources) {
		XacmlRequest xacmlRequest = new XacmlRequest();
		resources.forEach(p -> xacmlRequest.resource(p.getKey(), p.getValue()));
		return xacmlRequest;
	}
//
//	@Test
//	void shouldSanitizeAktoerId() {
//		final String scrubbed = abacLogger.sanitizeFnr("Authorization Request: domene=saf, resource_type=no.nav.abac.attributter.resource.saf.person, aktoerId_resource=2222222222222.  Authorization Response: decision=Deny, cause=cause-0002-ikketilgangtilNAVbrukersenhet, deny_policy=fp4_geografi, deny_rule=ingen_tilgang_enhet;");
//		assertEquals("Authorization Request: domene=saf, resource_type=no.nav.abac.attributter.resource.saf.person, aktoerId_resource=*************.  Authorization Response: decision=Deny, cause=cause-0002-ikketilgangtilNAVbrukersenhet, deny_policy=fp4_geografi, deny_rule=ingen_tilgang_enhet;", scrubbed);
//	}
//
//	@Test
//	void shouldNotSanitizeStringNotContainingFnrMatch() {
//		final String message = "Authorization Request: domene=saf, resource_type=no.nav.abac.attributter.resource.saf.organisasjon, orgnr=999999999.  Authorization Response: decision=Deny, cause=cause-0002-ikketilgangtilNAVbrukersenhet, deny_policy=fp4_geografi, deny_rule=ingen_tilgang_enhet;";
//		final String scrubbed = abacLogger.sanitizeFnr(message);
//		assertEquals(message, scrubbed);
//	}

	//  Authorization Response: decision=Deny, cause=cause-0002-ikketilgangtilNAVbrukersenhet, deny_policy=fp4_geografi, deny_rule=ingen_tilgang_enhet;
}