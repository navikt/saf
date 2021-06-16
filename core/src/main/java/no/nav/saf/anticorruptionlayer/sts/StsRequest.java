package no.nav.saf.anticorruptionlayer.sts;

import lombok.Getter;

@Getter
public class StsRequest {
    private static final String grant_type = "client_credentials";
    private static final String scope = "openid";
}