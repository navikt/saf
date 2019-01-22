package no.nav.saf.tilgangskontroll.validation;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.OidcAuthorizationException;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Slf4j
@Component
public class OidcValidatorTool {
	private static final String OIDC_TOKEN_PREFIX = "Bearer ";
	private static final String VALIDATION_FAILED = "Validering av OIDC tokenet gitt av konsumer feilet.";
	private static final String OIDC_NULL = "Validering av OIDC tokenet gitt av konsumer feilet.";
	private final Jose4jConsumerFactory consumerFactory;


	@Inject
	OidcValidatorTool(Jose4jConsumerFactory consumerFactory) {
		this.consumerFactory = consumerFactory;
	}

	public boolean validate(String authorizationHeader) {

		try {
			String oidcToken = authorizationHeader.split(OIDC_TOKEN_PREFIX)[1];

			if (oidcToken == null) {
				throw new OidcAuthorizationException(OIDC_NULL);
			}

			String issuer = new JwtConsumerBuilder().setSkipSignatureVerification().setSkipAllValidators().build().processToClaims(oidcToken).getIssuer();

			JwtConsumer consumer = consumerFactory.create(issuer);

			consumer.processToClaims(oidcToken);

			return true;

		} catch (InvalidJwtException | MalformedClaimException e) {
			log.error(VALIDATION_FAILED, e);
			throw new OidcAuthorizationException(VALIDATION_FAILED, e);
		}
	}
}
