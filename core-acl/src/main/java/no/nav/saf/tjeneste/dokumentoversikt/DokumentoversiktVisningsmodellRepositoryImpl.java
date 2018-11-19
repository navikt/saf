package no.nav.saf.tjeneste.dokumentoversikt;

import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	public List<Journalpost> findJournalposter(String aktoerId, String foedselsnummer, List<String> journalpostIds) {
		List<Sak> sakerByAktoerId = gsakAntiCorruptionLayer.findSakerByAktoerId(aktoerId);
		sakerByAktoerId.addAll(pensjonSakAntiCorruptionLayer.hentSakerByFoedselsnummer(foedselsnummer));
		Map<String, Sak> sakMap = sakerByAktoerId.stream().collect(Collectors.toMap(Sak::getArkivsaksnummer, sak -> sak));
		return joarkAntiCorruptionLayer.hentVisningJournalposter(sakMap, journalpostIds);
	}
}
