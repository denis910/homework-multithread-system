package org.foi.nwtis.djockovic.vjezba_03.konfiguracije;

import java.util.Properties;

public interface Konfiguracija {
    void ucitajKonfiguraciju() throws NeispravnaKonfiguracija;
    void ucitajKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija;
    void spremiKonfiguraciju() throws NeispravnaKonfiguracija;
    void spremiKonfiguraciju(String datoteka) throws NeispravnaKonfiguracija;
    void azurirajKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija;
    void dodajKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija;
    void dodajKonfiguraciju(Properties postavke);
    void kopirajKonfiguraciju(Properties postavke);
    Properties dajSvePostavke();
    boolean obrisiSvePostavke();
    String dajPostavku(String kljuc);
    boolean spremiPostavku(String kljuc, String vrijednost);
    boolean azurirajPostavku(String kljuc, String vrijednost);
    boolean postojiPostavka(String kljuc);
    boolean obrisiPostavku(String kljuc);
}
