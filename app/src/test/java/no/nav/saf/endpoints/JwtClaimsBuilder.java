package no.nav.saf.endpoints;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(fluent = true)
public class JwtClaimsBuilder {
	private String subject;
	private String issuer;
	private String audience;
	private LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
	private LocalDateTime validFrom = LocalDateTime.now().minusMinutes(1);
	private LocalDateTime issuedAt = LocalDateTime.now();
	private Map<String, Object> customClaims = new HashMap<>();

	public JwtClaimsBuilder customClaim(String key, Object value) {
		customClaims.put(key, value);
		return this;
	}

	public JwtClaimsBuilder azp(String azp) {
		return customClaim("azp", azp);
	}

	public JwtClaimsBuilder clearCustomClaims() {
		customClaims.clear();
		return this;
	}

	public JwtClaims build() {
		JwtClaims claims = new JwtClaims();
		claims.setGeneratedJwtId();
		claims.setIssuedAtToNow();
		claims.setIssuedAt(NumericDate.fromMilliseconds(issuedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
		claims.setSubject(subject);
		claims.setIssuer(issuer);
		claims.setAudience(audience);
		claims.setExpirationTime(NumericDate.fromMilliseconds(expiry.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
		claims.setNotBefore(NumericDate.fromMilliseconds(validFrom.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

		for (Map.Entry<String, Object> entry : customClaims.entrySet()) {
			claims.setClaim(entry.getKey(), entry.getValue());
		}

		return claims;
	}
}