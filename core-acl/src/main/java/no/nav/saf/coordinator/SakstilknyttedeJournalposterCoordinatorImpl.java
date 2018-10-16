package no.nav.saf.coordinator;

import no.nav.saf.context.gsak.GsakAcl;
import no.nav.saf.context.joark.JoarkAcl;
import no.nav.saf.context.saf.domain.DokumentInfo;
import no.nav.saf.context.saf.domain.Journalpost;
import no.nav.saf.context.saf.domain.Sak;
import no.nav.saf.context.saf.domain.Tema;
import no.nav.saf.context.saf.domain.kode.Temakode;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class SakstilknyttedeJournalposterCoordinatorImpl implements SakstilknyttedeJournalposterCoordinator {

	private final GsakAcl gsakAcl;
	private final JoarkAcl joarkAcl;

	@Inject
	SakstilknyttedeJournalposterCoordinatorImpl(GsakAcl gsakAcl, JoarkAcl joarkAcl) {
		this.gsakAcl = gsakAcl;
		this.joarkAcl = joarkAcl;
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
