package no.nav.saf.endpoints.hentDokument;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

import jdk.nashorn.internal.ir.annotations.Ignore;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Base64;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class HentDokumentIT extends AbstractEndpointEvaluatorIT {

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
		byte[] testFile = "TestThis".getBytes();

		stubFor(get("/hentdokument/" + dokumentId + "/" + variantFormat).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(testFile))));

		String uri = "/rest/hentdokument/" + journalpostId + "/" + dokumentId + "/" + variantFormat.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHeaders(), String.class);

		assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
		assertEquals(responseEntity.getBody(), new String(testFile));
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
			ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHeaders(), String.class);

		assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
	}

	private HttpEntity createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
		return new HttpEntity(headers);
	}
//
//	public byte[] testpdfToBytearray(String path) {
//		InputStream inputStream = null;
//
//	}
}
