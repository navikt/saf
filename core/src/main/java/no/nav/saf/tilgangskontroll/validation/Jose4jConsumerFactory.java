package no.nav.saf.tilgangskontroll.validation;

import static java.lang.String.format;

import no.nav.saf.exceptions.OidcAuthorizationException;
import org.jose4j.http.SimpleGet;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Jose4jConsumerFactory {
	static final String MISSING_VERIFICATION_KEY_RESOLVER = "Failed to find verification key resolver for issuer: %s";

	private final Map<String, VerificationKeyResolver> keyResolverMap = new ConcurrentHashMap<>();
	private final SimpleGet httpGet;

	public Jose4jConsumerFactory(SimpleGet simpleGet, List<Idp> idps) {

		this.httpGet = simpleGet;

		for (Idp idp : idps) {
			String jwks = idp.getJwksUrl();
			HttpsJwks httpsJwks;

			httpsJwks = new HttpsJwks(jwks);

			httpsJwks.setSimpleHttpGet(httpGet);
			keyResolverMap.put(idp.getIssuerUrl(), new HttpsJwksVerificationKeyResolver(httpsJwks));
		}
	}

	JwtConsumer create(String issuer) {
		VerificationKeyResolver resolver = keyResolverMap.get(issuer);
		if (resolver == null) {
			throw new OidcAuthorizationException(format(MISSING_VERIFICATION_KEY_RESOLVER, issuer));
		}

		return new JwtConsumerBuilder()
				.setRequireExpirationTime()
				.setRequireIssuedAt()
				.setAllowedClockSkewInSeconds(30)
				.setRequireSubject()
				.setSkipDefaultAudienceValidation()
				.setExpectedIssuer(issuer)
				.setVerificationKeyResolver(resolver)
				.build();
	}
}