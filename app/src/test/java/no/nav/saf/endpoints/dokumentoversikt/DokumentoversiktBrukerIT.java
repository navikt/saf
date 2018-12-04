package no.nav.saf.endpoints.dokumentoversikt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.GraphQLRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class DokumentoversiktBrukerIT extends AbstractItest {

	@Test
	@Disabled("Implement me")
	public void shouldHentDokumentBruker() throws IOException, URISyntaxException {
		abacPermit();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(post("/hentjournalsakinfo")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("joark/IMPLEMENT-hentjournalpostbulk")));

		stubFor(post("/gsak")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("gsak/IMPLEMENT")));
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/IMPLEMENT")));

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/query-happy"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<Object> responseEntity = restTemplate.exchange(requestEntity, Object.class);

		//TODO: Assert call to all endponts + assert response
	}
}

