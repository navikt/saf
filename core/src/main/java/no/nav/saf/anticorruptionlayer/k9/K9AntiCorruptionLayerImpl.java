package no.nav.saf.anticorruptionlayer.k9;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.k9.hentrelevanteparter.K9Consumer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Tema;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_K9;
import static no.nav.saf.domain.kode.Tema.FRI;
import static no.nav.saf.domain.kode.Tema.OMS;

@Slf4j
@Component
public class K9AntiCorruptionLayerImpl implements K9AntiCorruptionLayer {

	private final K9Consumer k9Consumer;

	@Inject
	public K9AntiCorruptionLayerImpl(K9Consumer k9Consumer) {
		this.k9Consumer = k9Consumer;
	}

	private final List<Tema> temaTyper = Arrays.asList(FRI, OMS);

	@Override
	public List<String> hentRelevanteParter(Arkivsak arkivsak) {
		if (temaTyper.contains(arkivsak.getTema()) && FAGSAKSYSTEM_K9.equals(arkivsak.getFagsaksystem())) {
			try {
				return k9Consumer.hentAktoerForSak(arkivsak.getFagsakId());
			} catch (Exception e) {
				log.warn("Kunne ikke hente relevante parter fra K9-sak for sakId={}", arkivsak.getFagsakId(), e);
				return Collections.emptyList();
			}
		}
		return new ArrayList<>();
	}
}
