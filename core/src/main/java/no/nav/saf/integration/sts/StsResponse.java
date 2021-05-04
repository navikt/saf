package no.nav.saf.integration.sts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StsResponse {
	private String access_token;
	private String token_type;
	private String expires_in;
}