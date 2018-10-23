package no.nav.saf.coordinator;

import no.nav.saf.domain.visningsmodell.Bruker;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.domain.visningsmodell.Tema;
import no.nav.saf.domain.visningsmodell.kode.Temakode;
import no.nav.saf.legacycontext.gsak.GsakAcl;
import no.nav.saf.legacycontext.joark.JoarkAcl;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class SafDomainCoordinatorImpl implements SafDomainCoordinator {

	private final GsakAcl gsakAcl;
	private final JoarkAcl joarkAcl;

	@Inject
	SafDomainCoordinatorImpl(GsakAcl gsakAcl, JoarkAcl joarkAcl) {
		this.gsakAcl = gsakAcl;
		this.joarkAcl = joarkAcl;
	}

	@Override
	public Bruker findBrukerByAktoerId(String aktoerId) {
		return Bruker.builder().aktoerId(aktoerId).build();
	}

	@Override
	public Set<Tema> findTemaKnyttetTilAktoerIdAndFilterByTemakoder(final String aktoerId, final List<Temakode> temakoder) {
		return gsakAcl.findTemaByAktoerIdAndFilterTemakode(aktoerId, temakoder);
	}

	@Override
	public List<Sak> findSakerByAktoerIdAndTema(String aktoerId, Temakode tema) {
		return gsakAcl.findSakByAktoerIdAndTemakode(aktoerId, tema);
	}

	@Override
	public List<Journalpost> findJournalposterByArkivsak(String arkivsaksnummer) {
		return joarkAcl.hentJournalpostListeByArkivsaksnummer(arkivsaksnummer);
	}

	@Override
	public List<DokumentInfo> findDokumentInfoByJournalpostIdAndArkivsak(String journalpostId, String arkivsaksnummer) {
		return joarkAcl.hentDokumentInfoListeByJournalpostIdAndArkivsak(journalpostId, arkivsaksnummer);
	}
}
