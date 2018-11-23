package no.nav.saf.tilgangskontroll.abstraction;

import com.google.common.collect.Lists;
import lombok.Getter;
import no.nav.dok.saf.domain.secmodel.Bruker;
import no.nav.dok.saf.domain.secmodel.Journalpost;
import no.nav.dok.saf.domain.secmodel.Sak;

import java.util.List;

@Getter
public class SecModelWorld {

    List<Bruker> brukere;
    List<Sak> saker;
    List<Journalpost> journalposter;

     public <T> void put(List<T> entiteter) {
         if (!entiteter.isEmpty()) {
             T checkType = entiteter.get(0);
             if (checkType instanceof Bruker) {
                 this.brukere = (List<Bruker>)entiteter;
             } else if (checkType instanceof Sak) {
                 this.saker = (List<Sak>)entiteter;
             } else if (checkType instanceof Journalpost) {
                 this.journalposter = (List<Journalpost>)entiteter;
             }

         }
    }

    public void add(Bruker bruker) {
        if (brukere == null) {
            brukere = Lists.newArrayList();
        }
        if (bruker != null) {
            brukere.add(bruker);
        }
     }

     public void add(Sak sak) {
         if (saker == null) {
             saker = Lists.newArrayList();
         }
         if (sak != null) {
             saker.add(sak);
         }
     }

     public void add(Journalpost journalpost) {
         if (journalposter == null) {
             journalposter = Lists.newArrayList();
         }
         if (journalpost != null) {
             journalposter.add(journalpost);
         }
     }

}
