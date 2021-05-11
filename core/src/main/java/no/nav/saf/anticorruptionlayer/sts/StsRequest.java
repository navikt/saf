package no.nav.saf.anticorruptionlayer.sts;

import lombok.Getter;

@Getter
public class StsRequest {
    private final String grant_type = "client_credentials";
    private final String scope = "openid";
}