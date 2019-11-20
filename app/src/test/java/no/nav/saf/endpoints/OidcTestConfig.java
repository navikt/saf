package no.nav.saf.endpoints;

import com.nimbusds.jose.util.IOUtils;
import no.nav.security.token.support.test.JwkGenerator;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.jose4j.http.Response;
import org.jose4j.http.SimpleGet;
import org.jose4j.http.SimpleResponse;
import org.springframework.context.annotation.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

@Configuration
@Profile("itest")
@Import(TokenGeneratorConfiguration.class)
public class OidcTestConfig {
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