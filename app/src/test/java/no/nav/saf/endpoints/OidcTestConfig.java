package no.nav.saf.endpoints;

import com.nimbusds.jose.util.IOUtils;
import no.nav.saf.tilgangskontroll.validation.config.IdpProperties;
import no.nav.saf.tilgangskontroll.validation.registry.Idp;
import no.nav.saf.tilgangskontroll.validation.registry.IdpRegistry;
import no.nav.saf.tilgangskontroll.validation.registry.IdpRegistryImpl;
import no.nav.security.oidc.test.support.JwkGenerator;
import org.jose4j.http.Response;
import org.jose4j.http.SimpleGet;
import org.jose4j.http.SimpleResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

@Configuration
@Profile("itest")
public class OidcTestConfig {
	@Bean
	@Primary
	IdpRegistry idpRegistry() {
		IdpProperties idpProperties = new IdpProperties();
		idpProperties.getIdp().put("iss-localhost", new Idp("iss-localhost","http://jwks"));
		return new IdpRegistryImpl(idpProperties.getIdp());
	}

	@Bean
	@Primary
	SimpleGet simpleGetTest() {
		return new SimpleGet() {
			@Override
			public SimpleResponse get(String s) throws IOException {
				if("http://jwks".equals(s)) {
					return new Response(200, null, new HashMap<>(), IOUtils.readInputStreamToString(getClass().getResourceAsStream(JwkGenerator.DEFAULT_JWKSET_FILE),
							Charset.defaultCharset()));
				} else {
				return null;
				}
			}
		};
	}
}