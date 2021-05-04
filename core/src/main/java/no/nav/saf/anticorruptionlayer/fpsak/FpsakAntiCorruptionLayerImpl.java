package no.nav.saf.anticorruptionlayer.fpsak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.fpsak.hentrelevanteparter.FpsakConsumer;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class FpsakAntiCorruptionLayerImpl implements FpsakAntiCorruptionLayer {

	private final FpsakConsumer fpsakConsumer;

	@Inject
	public FpsakAntiCorruptionLayerImpl(FpsakConsumer fpsakConsumer) {
		this.fpsakConsumer = fpsakConsumer;
	}

	@Override
	public List<String> hentRelevanteParter(String sakId) {
		try {
			return fpsakConsumer.hentAktoerForSak(sakId);
		} catch (Exception e) {
			log.warn("Kunne ikke hente relevante parter fra fpsak for sakId={}", sakId, e);
			return Collections.emptyList();
		}
	}
}
