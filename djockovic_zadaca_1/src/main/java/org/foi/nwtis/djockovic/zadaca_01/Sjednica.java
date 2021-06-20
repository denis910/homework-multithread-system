package org.foi.nwtis.djockovic.zadaca_01;

import java.util.Objects;

/**
 * Klasa objekata u koje se zapisuju podaci o sjednicama korisnika
 * @author Denis Jocković
 */
class Sjednica {

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(String korisnik) {
        this.korisnik = korisnik;
    }

    public long getVrijemeKreiranja() {
        return vrijemeKreiranja;
    }

    public void setVrijemeKreiranja(long vrijemeKreiranja) {
        this.vrijemeKreiranja = vrijemeKreiranja;
    }

    public long getVrijemeDoKadaVrijedi() {
        return vrijemeDoKadaVrijedi;
    }

    public void setVrijemeDoKadaVrijedi(long vrijemeDoKadaVrijedi) {
        this.vrijemeDoKadaVrijedi = vrijemeDoKadaVrijedi;
    }

    public StatusSjednice getStatus() {
        return status;
    }

    public void setStatus(StatusSjednice status) {
        this.status = status;
    }
    
    static public enum StatusSjednice{
        Aktivna, Neaktivna;
    }
    
    private int id;
    private String korisnik;
    private long vrijemeKreiranja;
    private long vrijemeDoKadaVrijedi;
    private StatusSjednice status;

    public Sjednica(int id, String korisnik, long vrijemeKreiranja, long vrijemeDoKadaVrijedi, StatusSjednice status) {
        this.id = id;
        this.korisnik = korisnik;
        this.vrijemeKreiranja = vrijemeKreiranja;
        this.vrijemeDoKadaVrijedi = vrijemeDoKadaVrijedi;
        this.status = status;
    }
}
