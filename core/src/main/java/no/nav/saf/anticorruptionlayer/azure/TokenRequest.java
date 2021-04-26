package no.nav.saf.anticorruptionlayer.azure;

import lombok.Getter;

@Getter
public class TokenRequest {
    private final String grant_type = "client_credentials";
    private final String scope = "openid";
}