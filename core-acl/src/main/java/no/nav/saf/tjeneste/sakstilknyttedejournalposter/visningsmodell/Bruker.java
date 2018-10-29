package no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell;

import lombok.Builder;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Bruker {
	private final String aktoerId;
	@Builder.Default
	private final Set<Tema> temaer = new HashSet<>();
}
