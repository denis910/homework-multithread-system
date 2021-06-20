package org.foi.nwtis.djockovic.vjezba_03.konfiguracije;

import java.util.Enumeration;
import java.util.Properties;

public abstract class KonfiguracijaApstraktna implements Konfiguracija {

    private String nazivDatoteke;
    protected Properties postavke = new Properties();

    public KonfiguracijaApstraktna(String nazivDatoteke) {
        this.nazivDatoteke = nazivDatoteke;
    }

    @Override
    public void azurirajKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dodajKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dodajKonfiguraciju(Properties postavke) {
        for(Enumeration e = postavke.keys(); e.hasMoreElements();){
            String kljuc = (String) e.nextElement();
            String vrijednost = postavke.getProperty(kljuc);
            this.postavke.setProperty(kljuc, vrijednost);
        }
    }

    @Override
    public void kopirajKonfiguraciju(Properties postavke) {
        this.postavke = postavke;
    }

    @Override
    public Properties dajSvePostavke() {
        return this.postavke;
    }

    @Override
    public boolean obrisiSvePostavke() {
        if(this.postavke.isEmpty())
            return false;
        this.postavke.clear();
        return true;
    }

    @Override
    public String dajPostavku(String kljuc) {
        return this.postavke.getProperty(kljuc);
    }

    @Override
    public boolean spremiPostavku(String kljuc, String vrijednost) {
        if(this.postavke.containsKey(kljuc)){
            return false;
        }else{
            this.postavke.setProperty(kljuc, vrijednost);
            return true;
        }
    }

    @Override
    public boolean azurirajPostavku(String kljuc, String vrijednost) {
        if(!this.postavke.containsKey(kljuc)){
            return false;
        }else{
            this.postavke.setProperty(kljuc, vrijednost);
            return true;
        }
    }

    @Override
    public boolean postojiPostavka(String kljuc) {
        return this.postavke.containsKey(kljuc);
    }

    @Override
    public boolean obrisiPostavku(String kljuc) {
        if(!this.postavke.containsKey(kljuc)){
            return false;
        }else{
            this.postavke.remove(kljuc);
            return true;
        }
    }
    
    @Override
    public void ucitajKonfiguraciju() throws NeispravnaKonfiguracija {
        this.ucitajKonfiguraciju(nazivDatoteke);
    }

    @Override
    public void spremiKonfiguraciju() throws NeispravnaKonfiguracija {
        this.spremiKonfiguraciju(nazivDatoteke);
    }
    
    public static Konfiguracija kreirajKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija{
        Konfiguracija konf = dajKonfiguraciju(nazivDatoteke);
        konf.spremiKonfiguraciju();
        return konf;
    }
    
    public static Konfiguracija preuzmiKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija{
        Konfiguracija konf = dajKonfiguraciju(nazivDatoteke);
        konf.ucitajKonfiguraciju();
        return konf;
    }

    protected static Konfiguracija dajKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija {
        String[] dijelovi = nazivDatoteke.split("\\.");
        String ekstenzija = dijelovi[dijelovi.length - 1];
        switch (ekstenzija.toLowerCase()) {
            case "txt":
                return new KonfiguracijaTXT(nazivDatoteke);
            case "xml":
                return new KonfiguracijaXML(nazivDatoteke);
            case "bin":
                return new KonfiguracijaBIN(nazivDatoteke);
            case "json":
                return new KonfiguracijaJSON(nazivDatoteke);
            default:
                throw new NeispravnaKonfiguracija("Datoteka '" + nazivDatoteke + "' sa ekstenzijom '" + ekstenzija + "' nije podržana!");
        }
    }
}
