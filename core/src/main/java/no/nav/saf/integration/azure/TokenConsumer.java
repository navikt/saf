package no.nav.saf.integration.azure;

public interface TokenConsumer {

	TokenResponse getClientCredentialToken(String scope);

}
