package no.nav.saf.anticorruptionlayer.pensjonsak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.pensjonsak.hentbrukerforsak.PensjonSakRestConsumer;
import no.nav.saf.anticorruptionlayer.pensjonsak.hentsaksammendragliste.PensjonSakWsConsumer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PensjonSakAntiCorruptionLayerImpl implements PensjonSakAntiCorruptionLayer {
	public static final String PSAK_FAGSYSTEM = "PP01";
	private final PensjonSakWsConsumer pensjonSakWsConsumer;
	private final PensjonSakRestConsumer pensjonSakRestConsumer;

	@Inject
	public PensjonSakAntiCorruptionLayerImpl(PensjonSakWsConsumer pensjonSakWsConsumer, PensjonSakRestConsumer pensjonSakRestConsumer) {
		this.pensjonSakWsConsumer = pensjonSakWsConsumer;
		this.pensjonSakRestConsumer = pensjonSakRestConsumer;
	}

	@Override
	public List<Arkivsak> findArkivsaker(final TilgangBruker tilgangBruker, final List<Tema> tema) {
		try {
			if (tema.isEmpty()) {
				return new ArrayList<>();
			} else {
				List<Arkivsak> arkivsakList = new ArrayList<>();
					for(String foedselnr : tilgangBruker.hentAlleFodselsnummer()) {
						arkivsakList.addAll(pensjonSakWsConsumer.hentSakSammendragListe(foedselnr).stream()
								.filter(psak -> tema.contains(mapToTema(psak.getTema())))
								.map(psak -> Arkivsak.builder()
										.aktoerId(tilgangBruker.getAktoerId())
										.arkivsaksnummer(psak.getSakNr())
										.arkivsaksystem(Arkivsakssystem.PSAK)
										.fagsakId(psak.getSakNr())
										.fagsaksystem(PSAK_FAGSYSTEM)
										.tema(Tema.valueOf(psak.getTema()))
										.datoOpprettet(psak.getDatoOpprettet())
										.build())
								.collect(Collectors.toList()));
					}
				return arkivsakList;
			}
		} catch (Exception e) {
			log.warn("Klarte ikke hente pensjonssaker for fødselsnummer={}", "*****", e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<Arkivsak> findArkivsaker(final TilgangBruker tilgangBruker) {
		try {
			if (tilgangBruker.getFoedselsnr() == null) {
				return new ArrayList<>();
			} else {
				return pensjonSakWsConsumer.hentSakSammendragListe(tilgangBruker.getFoedselsnr()).stream()
						.map(psak -> Arkivsak.builder()
								.aktoerId(tilgangBruker.getAktoerId())
								.arkivsaksnummer(psak.getSakNr())
								.arkivsaksystem(Arkivsakssystem.PSAK)
								.fagsakId(psak.getSakNr())
								.fagsaksystem(PSAK_FAGSYSTEM)
								.tema(Tema.valueOf(psak.getTema()))
								.datoOpprettet(psak.getDatoOpprettet())
								.build())
						.collect(Collectors.toList());
			}
		} catch (Exception e) {
			log.warn("Klarte ikke hente pensjonssaker for fødselsnummer={}", "*****", e);
			return new ArrayList<>();
		}
	}

	@Override
	public String findFoedselsnummerBySakId(String sakId) {
		try {
			return pensjonSakRestConsumer.hentBrukerForSak(sakId).getFnr();
		} catch (Exception e) {
			log.warn("Klarte ikke å hente brukerId (fødselsnummer) for sakId={}", sakId, e);
			return null;
		}
	}

	private Tema mapToTema(String tema) {
		try {
			return Tema.valueOf(tema);
		} catch (Exception e) {
			return null;
		}
	}
}
