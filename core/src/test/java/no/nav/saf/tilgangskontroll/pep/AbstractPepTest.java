package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(MockitoExtension.class)
public abstract class AbstractPepTest {

	@Mock
	protected AbacService abacService;
}
