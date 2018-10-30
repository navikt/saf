package no.nav.saf.tjeneste.dokumentoversikt;

import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokumentoversiktVisningsmodellRepositoryImpl implements DokumentoversiktVisningsmodellRepository {

	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final JoarkAntiCorruptionLayer joarkAntiCorruptionLayer;

	@Inject
	public DokumentoversiktVisningsmodellRepositoryImpl(GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
														JoarkAntiCorruptionLayer joarkAntiCorruptionLayer) {
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.joarkAntiCorruptionLayer = joarkAntiCorruptionLayer;
	}

	@Override
	public List<Journalpost> findJournalposterByAktoerId(String aktoerId) {
		List<Sak> sakerByAktoerId = gsakAntiCorruptionLayer.findSakerByAktoerId(aktoerId, new ArrayList<>());
		List<Journalpost> journalposter = joarkAntiCorruptionLayer.hentJournalpostListeByArkivsaker(sakerByAktoerId);
		return journalposter;
	}
}
