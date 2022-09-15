package no.nav.saf.integration.penrest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.saf.anticorruptionlayer.pensjonsak.hentbrukerforsak.PensjonSakRestConsumer;
import no.nav.saf.anticorruptionlayer.pensjonsak.hentsaksammendragliste.PensjonSakWsConsumer;
import no.nav.saf.config.SafProperties;
import no.nav.saf.config.ServiceuserAlias;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListeSakManglerEierenhet;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.FunctionUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;

import java.util.List;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

class PensjonSakRestIT  extends AbstractItest {

	@Resource
	private TestRestTemplate testRestTemplate;

	@Resource
	private PensjonSakRest pensjonSakRest;

	@BeforeEach
	public void setup() {
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();

		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	@Test
	public void PensjonSakSammendragRestLookUpHappyCase() throws HentSakSammendragListeSakManglerEierenhet, HentSakSammendragListePersonIkkeFunnet {
		stubFor(get(urlMatching(".*/sammendrag"))
				.willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("assorted_saks.json")));


		List<SakSammendrag> sammendragList = pensjonSakRest.hentSakSammendragListe("12345654321");

		assertAll(
				() -> assertThat(sammendragList, hasSize(5)),
				() -> assertThat(sammendragList, hasItem(where(SakSammendrag::saksstatus, equalToIgnoringCase("AVSLUTTET"))))
		);
	}


	private static <T,V> BaseMatcher<T> where(Function<T,V> function, Matcher<V> matcher) {
		return new BaseMatcher<T>() {
			@Override
			public void describeTo(Description description) {
				description.appendText(" a lambda returning ").appendDescriptionOf(matcher);
			}

			@Override
			public boolean matches(Object thing) {
				try {
					if (thing != null) {
						return matcher.matches(function.apply((T) thing));
					}
				} catch (ClassCastException e) {}
				return false;
			}
		};
	}
}