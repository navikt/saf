package no.nav.saf.anticorruptionlayer.fpsak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.fpsak.hentrelevanteparter.FpsakConsumer;
import no.nav.saf.domain.Arkivsak;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_FORELDREPENGELOSNING;
import static no.nav.saf.domain.kode.Tema.FOR;

@Slf4j
@Component
public class FpsakAntiCorruptionLayerImpl implements FpsakAntiCorruptionLayer {

	private final FpsakConsumer fpsakConsumer;

	@Inject
	public FpsakAntiCorruptionLayerImpl(FpsakConsumer fpsakConsumer) {
		this.fpsakConsumer = fpsakConsumer;
	}

	@Override
	public List<String> hentRelevanteParter(Arkivsak arkivsak) {
		if (FOR.equals(arkivsak.getTema()) && FAGSAKSYSTEM_FORELDREPENGELOSNING.equals(arkivsak.getFagsaksystem())) {
			try {
				return fpsakConsumer.hentAktoerForSak(arkivsak.getFagsakId());
			} catch (Exception e) {
				log.warn("Kunne ikke hente relevante parter fra fpsak for sakId={}", arkivsak.getFagsakId(), e);
			}
		}
		return new ArrayList<>();
	}
}
