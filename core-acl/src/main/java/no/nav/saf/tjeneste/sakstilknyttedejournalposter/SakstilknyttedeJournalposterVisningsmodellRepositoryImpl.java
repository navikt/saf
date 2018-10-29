package no.nav.saf.tjeneste.sakstilknyttedejournalposter;

import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class SakstilknyttedeJournalposterVisningsmodellRepositoryImpl implements SakstilknyttedeJournalposterVisningsmodellRepository {
	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final JoarkAntiCorruptionLayer joarkAntiCorruptionLayer;

	@Inject
	public SakstilknyttedeJournalposterVisningsmodellRepositoryImpl(GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
																	JoarkAntiCorruptionLayer joarkAntiCorruptionLayer) {
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.joarkAntiCorruptionLayer = joarkAntiCorruptionLayer;
	}
}
