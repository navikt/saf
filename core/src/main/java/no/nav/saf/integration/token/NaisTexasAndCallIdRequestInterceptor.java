package no.nav.saf.integration.token;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

import static no.nav.saf.headers.NavHeaders.NAV_CALLID;
import static no.nav.saf.util.MDCUtility.getCallId;

public class NaisTexasAndCallIdRequestInterceptor implements ClientHttpRequestInterceptor {

	public static final String TARGET_SCOPE = "targetScope";
	public static final String TOKEN_TO_EXCHANGE = "tokenForExchange";

	private final NaisTexasConsumer naisTexasConsumer;

	public NaisTexasAndCallIdRequestInterceptor(NaisTexasConsumer naisTexasConsumer) {
		this.naisTexasConsumer = naisTexasConsumer;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		Map<String, Object> attributes = request.getAttributes();
		if (attributes.containsKey(TARGET_SCOPE)) {
			String targetScope = (String) attributes.get(TARGET_SCOPE);
			if (attributes.containsKey(TOKEN_TO_EXCHANGE)) {
				String accessToken = (String) attributes.get(TOKEN_TO_EXCHANGE);
				request.getHeaders().setBearerAuth(naisTexasConsumer.exchangeForOboToken(accessToken, targetScope));
			} else {
				request.getHeaders().setBearerAuth(naisTexasConsumer.getSystemToken(targetScope));
			}
		}
		request.getHeaders().add(NAV_CALLID, getCallId());
		return execution.execute(request, body);
	}
}
