package no.nav.saf.endpoints.hentDokument;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

import jdk.nashorn.internal.ir.annotations.Ignore;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.endpoints.AbstractItest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class HentDokumentIT extends AbstractItest {

	public HentDokumentIT() {
	}

	// Flytt STSConfig etc til app, senere bør
	// PepEvaluatorITX konvertertes til å bruke mockito istedenfor wiremock.

	// all endpoints along the way must be stubbed
	// and restTemplate.getForEntity should return a sensible document

//		we're supposed to use wiremock, not lombok.
//		difference is that mockito stubs the function, while wiremock mocks the external dependencies
//		Example of this is in dokprod qdok001IT
//	  	in order to get the json responseentity from hentDocument, use postman and copy the format
//		then place the contents in __files


	@Test
	public void hentDokumentHappyPath() {
		String dokumentId = "441360260";
		String journalpostId = "123";
		VariantFormatCode variantFormat = VariantFormatCode.ARKIV;

		stubFor(get("/hentdokument/" + dokumentId + "/" + variantFormat).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder()
						.encode(new String("test").getBytes(StandardCharsets.UTF_8)))));//"/hentDokument/hentdokument-happy.json")));

		String uri = "/rest/hentdokument/" + journalpostId + "/" + dokumentId + "/" + variantFormat.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);


		// finally check the integrity of the dokument with various asserts.
		//			new String(Base64.decode(response.body), StandardCharsets.UTF_8)
		assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
//		assertEquals(responseEntity.getHeaders().getContentType(),"yay");
		assertEquals(responseEntity.getBody(), "hentDokument/hentdokument-happy.json");
	}

	@Ignore
	@Test
	public void hentDokumentNotFound() {
		String dokumentId = "123456";
		String journalpostId = "123";
		VariantFormatCode variantFormat = VariantFormatCode.ARKIV;

		stubFor(get("/hentdokument/" + dokumentId + "/" + variantFormat).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody("hentDokument/hentdokument-happy.json")));

		String uri = "/rest/hentdokument/" + journalpostId + "/" + dokumentId + "/" + variantFormat.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
	}

}
