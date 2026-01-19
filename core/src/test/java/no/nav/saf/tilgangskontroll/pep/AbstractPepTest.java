package no.nav.saf.tilgangskontroll.pep;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.SneakyThrows;
import no.nav.saf.anticorruptionlayer.nav.NavUserGroupMembershipService;
import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(SpringExtension.class)
abstract class AbstractPepTest {

	protected static final String NAV_CALLID = "navCallId";
	private static final String NAVIDENT = "Z990424";
	static String AKTOER_ID = "1234";
	static String FNR = "11111111111";
	static Skjerming SKJERMING_POL = Skjerming.POL;

	@Mock
	protected NavUserGroupMembershipService navUserGroupMembershipService;

	@Mock
	protected JwtToken mockJwtToken;

	@BeforeEach
	@SneakyThrows
	void setUp() {
		openMocks(this);
		when(mockJwtToken.getEncodedToken()).thenReturn(createDummySignedJwt());
		when(mockJwtToken.getJwtTokenClaims()).thenReturn(getJwtTokenClaims());
	}

	SafRequestContext createSafRequestContext() {
		Map<String, Boolean> privilegiedServiceusers = Map.of(
				"srvsaf", true,
				"srvdokopp", true
		);
		Map<String, JwtToken> issuerValidated = Map.of("azurev2", mockJwtToken);
		return new SafRequestContext(NAV_CALLID, NAVIDENT, new TokenValidationContext(issuerValidated), privilegiedServiceusers);
	}

	@SneakyThrows
	private static String createDummySignedJwt() {
		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), getJwtClaimsSet());
		signedJWT.sign(new MACSigner(new byte[32]));

		return signedJWT.serialize();
	}

	private static JwtTokenClaims getJwtTokenClaims() {
		return new JwtTokenClaims(getJwtClaimsSet());
	}

	private static JWTClaimsSet getJwtClaimsSet() {
		return new JWTClaimsSet.Builder().build();
	}
}
