package no.nav.saf.query.tilknyttedejournalposter;

import static no.nav.saf.domain.DomainConstants.PEP1G;
import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.DomainConstants.PEP2D;
import static no.nav.saf.domain.DomainConstants.PEP3;
import static no.nav.saf.domain.DomainConstants.PEP4;
import static no.nav.saf.domain.DomainConstants.PEP5;
import static no.nav.saf.domain.DomainConstants.PEP6D;

import no.nav.saf.domain.kode.Tilknytning;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.stereotype.Component;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class TilknyttedeJournalposterCoordinator {
	private final TilknyttedeJournalposterTilgangRepository tilknyttedeJournalposterTilgangRepository;
	private final Pep<TilgangBruker> pep1;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;

	public TilknyttedeJournalposterCoordinator(TilknyttedeJournalposterTilgangRepository tilknyttedeJournalposterTilgangRepository,
									  @Named(PEP1G) Pep<TilgangBruker> pep1,
									  @Named(PEP2) Pep<TilgangSak> pep2,
									  @Named(PEP2D) Pep<TilgangSak> pep2d,
									  @Named(PEP3) Pep<TilgangSak> pep3,
									  @Named(PEP4) Pep<TilgangJournalpost> pep4,
									  @Named(PEP5) Pep<TilgangDokumentInfo> pep5,
									  @Named(PEP6D) Pep<TilgangDokumentvariant> pep6d) {
		this.tilknyttedeJournalposterTilgangRepository = tilknyttedeJournalposterTilgangRepository;
		this.pep1 = pep1;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
	}


	public List<Journalpost> hentTilknyttedeJournalposter(String dokumentInfoId, Tilknytning tilknytning, SafRequestContext safRequestContext) {



		return new ArrayList<>();
	}
}
