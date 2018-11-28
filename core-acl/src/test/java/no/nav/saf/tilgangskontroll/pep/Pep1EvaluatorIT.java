package no.nav.saf.tilgangskontroll.pep;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Pep1EvaluatorIT extends AbstractPepIT {

	@Inject
	@Named("pep1")
	private Pep<TilgangBruker> pep1;

	@Test
	public void pep1HappyPath() throws IOException {
		abacPermit();

		boolean hasAccess = pep1.hasAccess(TilgangBruker.builder()
				.aktoerId(AKTOER_ID)
				.foedselsnr(IDENTIFIKATOR)
				.historiskeIdenter(Arrays.asList(TilgangIdent.builder().identifikator(IDENTIFIKATOR).build()))
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST));

		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("pep1/pep1-happy.json"),
				getOidcTokenBody(OIDC_TOKEN_PERSON_USER_TEST.replace("Bearer ", ""))))));
		assertEquals(Boolean.TRUE, hasAccess);
	}

	@Test
	public void pep1Deny() throws IOException {
		abacDeny();

		boolean hasAccess = pep1.hasAccess(TilgangBruker.builder()
				.aktoerId(AKTOER_ID)
				.foedselsnr(IDENTIFIKATOR)
				.historiskeIdenter(Collections.singletonList(TilgangIdent.builder().identifikator(IDENTIFIKATOR).build()))
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST));

		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(format(stringFromClasspath("pep1/pep1-happy.json"),
				getOidcTokenBody(OIDC_TOKEN_PERSON_USER_TEST.replace("Bearer ", ""))))));
		assertEquals(Boolean.FALSE, hasAccess);
	}
}
