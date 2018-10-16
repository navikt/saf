package no.nav.saf.context.gsak;

import no.nav.saf.context.gsak.domain.GsakSakerTo;
import no.nav.saf.context.gsak.hentgsaksaker.GsakConsumer;
import no.nav.saf.context.saf.domain.Sak;
import no.nav.saf.context.saf.domain.Tema;
import no.nav.saf.context.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.context.saf.domain.kode.Temakode;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class GsakAclImpl implements GsakAcl {
	private final GsakConsumer gsakConsumer;

	@Inject
	public GsakAclImpl(GsakConsumer gsakConsumer) {
		this.gsakConsumer = gsakConsumer;
	}

	@Override
	public Set<Tema> findTemaByAktoerIdAndFilterTemakode(final String aktoerId, final List<Temakode> temakoder) {
		List<GsakSakerTo> gsakSakerTo = gsakConsumer.hentSakerByAktoerId(aktoerId);
		return gsakSakerTo.stream().map(gsak -> Tema.fromTemakode(Temakode.valueOf(gsak.getTema())))
				.filter(t -> temakoder.isEmpty() || temakoder.contains(t.getTema()))
				.collect(Collectors.toSet());
	}

	@Override
	public List<Sak> findSakByAktoerIdAndTemakode(final String aktoerId, final Temakode temakode) {
		List<GsakSakerTo> gsakSakerTo = gsakConsumer.hentSakerByAktoerId(aktoerId);
		return gsakSakerTo.stream()
				.filter(gsak -> Temakode.valueOf(gsak.getTema()) == temakode)
				.map(gsak -> Sak.builder()
						.arkivsaksnummer(gsak.getId().toString())
						.arkivsakssystem(Arkivsakssystem.GSAK)
						.fagsaksnummer(gsak.getFagsakNr())
						.fagsystem(gsak.getApplikasjon())
						.temakode(Temakode.valueOf(gsak.getTema()))
						.datoOpprettet(gsak.getOpprettetTidspunkt())
						.build())
				.collect(Collectors.toList());
	}
}
