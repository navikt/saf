package no.nav.saf.cache;

import static no.nav.saf.cache.AbstractKeyGeneratorCaching.DOKUMENTINFO_ID;
import static no.nav.saf.cache.AbstractKeyGeneratorCaching.JOURNALPOST_ID;
import static no.nav.saf.cache.AbstractKeyGeneratorCaching.RESSURSTYPE;
import static no.nav.saf.cache.AbstractKeyGeneratorCaching.RESSURSTYPE_DOKUMENT_FIL;
import static no.nav.saf.cache.AbstractKeyGeneratorCaching.RESSURSTYPE_SAK_DOKUMENT;
import static no.nav.saf.cache.AbstractKeyGeneratorCaching.SAKSBEHANDLER_ID;
import static no.nav.saf.cache.AbstractKeyGeneratorCaching.SKJERMING;
import static no.nav.saf.cache.AbstractKeyGeneratorCaching.TEMA;
import static no.nav.saf.cache.AbstractKeyGeneratorCaching.VARIANTFORMAT;
import static no.nav.saf.cache.KeyGeneratorDistributedCaching.getKeyForPep2dDistributedCaching;
import static no.nav.saf.cache.KeyGeneratorDistributedCaching.getKeyForPep6dDistributedCaching;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
class KeyGeneratorDistributedCachingTest extends AbstractKeyGeneratorCachingTest {

	@Test
	void checkPep2dKeyGenerator() {
		String key = getKeyForPep2dDistributedCaching(SAKSBEHANDLER_ID_TEST, TEMA_TEST);
		assertThat(key, is(String.format("%s:%s;%s:%s;%s:%s", RESSURSTYPE, RESSURSTYPE_SAK_DOKUMENT, SAKSBEHANDLER_ID, SAKSBEHANDLER_ID_TEST, TEMA, TEMA_TEST)));
	}

	@Test
	void checkPep6dKeyGenerator() {
		String key = getKeyForPep6dDistributedCaching(SAKSBEHANDLER_ID_TEST, JOURNALPOST_ID_TEST, DOKUMENTINFO_ID_TEST, VARIANT_FORMAT_TEST, SKJERMING_TEST);
		assertThat(key, is(String.format("%s:%s;%s:%s;%s:%s;%s:%s;%s:%s;%s:%s", RESSURSTYPE, RESSURSTYPE_DOKUMENT_FIL, SAKSBEHANDLER_ID, SAKSBEHANDLER_ID_TEST, JOURNALPOST_ID,
				JOURNALPOST_ID_TEST, DOKUMENTINFO_ID, DOKUMENTINFO_ID_TEST, VARIANTFORMAT, VARIANT_FORMAT_TEST, SKJERMING, SKJERMING_TEST)));
	}
}