package no.nav.saf.query.tilknyttedejournalposter;

import lombok.Value;
import no.nav.saf.domain.kode.Arkivsakssystem;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class Sakstilknytning {
	private final String arkivsakId;
	private final Arkivsakssystem arkivsakssystem;
}
