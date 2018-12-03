package no.nav.saf.tjeneste.hentdokument;

import no.nav.saf.domain.DokumentRepository;
import no.nav.saf.exceptions.TilgangskontrollException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abstraction.ParameterContext;
import no.nav.saf.tilgangskontroll.abstraction.SecModelWorld;
import no.nav.saf.tilgangskontroll.model.HentDokumentTilgangsmodell;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */

@Component("HentDokumentDomainCoordinatorImplPoc")
public class HentDokumentDomainCoordinatorImplPoc implements HentDokumentDomainCoordinator {

	private final DokumentRepository dokumentRepository;
	private final HentDokumentTilgangsmodell hentDokumentTilgangsmodell;
	private final SecModelWorld secModelWorld = new SecModelWorld();
	private final ParameterContext parameterContext = new ParameterContext();

	@Inject
	public HentDokumentDomainCoordinatorImplPoc(DokumentRepository dokumentRepository, HentDokumentTilgangsmodell hentDokumentTilgangsmodell) {
		this.dokumentRepository = dokumentRepository;
		this.hentDokumentTilgangsmodell = hentDokumentTilgangsmodell;
	}

	@Override
	public HentDokument hentDokument(final String journalpostId, final String dokumentId, final String variantFormat, final SafRequestContext safRequestContext) {
		parameterContext.putParameter("journalpostId", journalpostId);
		parameterContext.putParameter("dokumentInfoId", dokumentId);
		parameterContext.putParameter("variantformat", variantFormat);

		if (hentDokumentTilgangsmodell.checkTilgangDokumentInfo(safRequestContext, parameterContext, secModelWorld) != null) {
			return dokumentRepository.findDokument(dokumentId, variantFormat);
		} else {
			throw new TilgangskontrollException();

		}
	}
}
