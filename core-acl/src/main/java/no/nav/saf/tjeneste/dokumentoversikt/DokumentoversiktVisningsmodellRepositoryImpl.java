package no.nav.saf.tjeneste.dokumentoversikt;

import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokumentoversiktVisningsmodellRepositoryImpl implements DokumentoversiktVisningsmodellRepository {

	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final JoarkAntiCorruptionLayer joarkAntiCorruptionLayer;

	@Inject
	public DokumentoversiktVisningsmodellRepositoryImpl(GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
														PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
														JoarkAntiCorruptionLayer joarkAntiCorruptionLayer) {
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.joarkAntiCorruptionLayer = joarkAntiCorruptionLayer;
	}

	@Override
	public List<Journalpost> findJournalposterByAktoerId(String aktoerId, String foedselsnummer, List<Temakode> temaer) {
		List<Sak> sakerByAktoerId = gsakAntiCorruptionLayer.findSakerByAktoerId(aktoerId, temaer);
		sakerByAktoerId.addAll(pensjonSakAntiCorruptionLayer.hentSakerByFoedselsnummer(foedselsnummer));
		List<Journalpost> journalposter = joarkAntiCorruptionLayer.hentJournalpostListeByArkivsaker(sakerByAktoerId);
		return journalposter;
	}
}
