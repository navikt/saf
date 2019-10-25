package no.nav.saf.tilgangskontroll.validation;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.validation.registry.IdpException;
import no.nav.saf.tilgangskontroll.validation.registry.IdpRegistry;
import org.jose4j.http.Get;
import org.jose4j.http.SimpleGet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Configuration
public class Jose4jValidationConfig {

	@Bean
	Jose4jConsumerFactory jose4jConsumerFactory(SimpleGetResolver simpleGetResolver, IdpRegistry idpRegistry) {
		return new Jose4jConsumerFactory(simpleGetResolver, idpRegistry.getAll());
	}

	@Bean
	SimpleGet simpleGet() {
		return new Get();
	}

	@Bean
	SimpleGetResolver simpleGetResolver(SimpleGet defaultGet) {
		return (issuerUrl, proxyAddress) -> {
			if (proxyAddress != null) {
				Get simpleGet = new Get();
				try {
					URI uri = new URI("proxy://" + proxyAddress);
					if (uri.getHost() == null || uri.getPort() == -1) {
						throw new IdpException("proxyAddress must have both host and port");
					}
					simpleGet.setHttpProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(uri.getHost(), uri.getPort()));
				} catch (URISyntaxException e) {
					throw new IdpException(e.getMessage());
				}

				return simpleGet;
			}
			return defaultGet;
		};
	}
}