package no.nav.saf.integration.azure;

/**
 * "Borrowed" from safselvbetjening, thanks Joakim!
 */
public interface TokenConsumer {

	TokenResponse getClientCredentialToken(String scope);

}
