package no.nav.saf.query.sak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.tilgangskontroll.RequestCache;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SakMapper {

	public Sak mapSak(TilgangSak sak, RequestCache requestCache) {
		Arkivsak arkivsak = requestCache.getObject(sak.getArkivsaksnummer() + sak.getArkivsaksystem());
		if (arkivsak == null) {
			return null;
		}
		return Sak.builder()
				.arkivsaksnummer(arkivsak.getArkivsaksnummer())
				.arkivsaksystem(arkivsak.getArkivsaksystem())
				.fagsakId(arkivsak.getFagsakId())
				.fagsaksystem(arkivsak.getFagsaksystem())
				.datoOpprettet(arkivsak.getDatoOpprettet())
				.sakstype(Sakstype.fromFagsaksystem(arkivsak.getFagsaksystem()))
				.tema(arkivsak.getTema())
				.build();

	}


}
