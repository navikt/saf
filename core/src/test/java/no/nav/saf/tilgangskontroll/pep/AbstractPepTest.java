package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import no.nav.saf.tilgangskontroll.validation.OidcValidatorTool;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(MockitoExtension.class)
public abstract class AbstractPepTest {

	protected static final String OIDC_TOKEN_PERSON_USER_TEST = "Bearer " + "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJvNFUwMVhKNmlnRmw0VGYwdFRkYjR3IiwgInN1YiI6ICJaOTkwNDI0IiwgImF1ZGl0VHJhY2tpbmdJZCI6ICJlYTdmNWUxMi1jYjZjLTQ1ZjUtYmViMi0wYjVkYmI5ZDQ3YTItMTMzNzkzNCIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJpZGEtdCIsICJjX2hhc2giOiAiRnJwNzhwdlJZU0VPMExjUktPUFdWdyIsICJvcmcuZm9yZ2Vyb2NrLm9wZW5pZGNvbm5lY3Qub3BzIjogIjJjYjQ2OGU4LThmMjItNGY1NS1hYTQ4LWM1NWExYjA4YmQ1ZiIsICJhenAiOiAiaWRhLXQiLCAiYXV0aF90aW1lIjogMTU0MzU3Nzk3MiwgInJlYWxtIjogIi8iLCAiZXhwIjogMTU0MzU4MTU3MiwgInRva2VuVHlwZSI6ICJKV1RUb2tlbiIsICJpYXQiOiAxNTQzNTc3OTcyIH0.NRgKaZhZ7qbBbJMUj_l9kzGOv7yOJVRVZDqmK0-G9lxzZs4jW1AtvFWqJRO9dd_djlIOGXz93UnuMNpWYWuoUd_S9gVc53yUjquzrys1IK8Zjd89smEl_9QP3ya8z7ISv48DciJORxdB2XT8rr2qpltYjKrCE2QmmK2ctAhy9QuFwEoZnctrR8IDKhUJCGd8LXPXddNRNEDL4-A47KwkF0UcfoDzPXznyZ2cbV4IkT3zvGqqwO3hovdrpadBdf204hClcmETYN3frRh1qHuTUqrBL7ualfqs-eDa4FKd77Mwu02LqPQGVpt8Ebebtv3OlS28YDchx8ng_P05okSjZg";
	protected static String AKTOER_ID = "1234";
	protected static String FNR = "***gammelt_fnr***";
	protected static String FNR2 = "***gammelt_fnr***";
	protected static Tema TEMA_BID = Tema.BID;
	protected static Skjerming SKJERMING_POL = Skjerming.POL;

	protected AbacService abacService;
	protected OidcValidatorTool oidcValidatorTool;

	public AbstractPepTest() {
		abacService = Mockito.mock(AbacService.class);
		oidcValidatorTool = Mockito.mock(OidcValidatorTool.class);
	}


}
