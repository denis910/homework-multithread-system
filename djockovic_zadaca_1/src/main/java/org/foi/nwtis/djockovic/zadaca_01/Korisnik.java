package org.foi.nwtis.djockovic.zadaca_01;

import java.util.Objects;

/**
 * Klasa pomoću koje se stvaraju objekti u koje se zapisuju podaci o dozvoljenim korisnicima
 * @author Denis Jocković
 */
class Korisnik {
    public String ime;
    public String prezime;
    public String korisnickoIme;
    public String lozinka;
    
    public Korisnik() {
    }

    public Korisnik(String ime, String prezime, String korisnickoIme, String lozinka) {
        this.ime = ime;
        this.prezime = prezime;
        this.korisnickoIme = korisnickoIme;
        this.lozinka = lozinka;
    }

    @Override
    public String toString() {
        return "Korisnik{" + "ime=" + ime + ", prezime=" + prezime + ", korisnickoIme=" + korisnickoIme + ", lozinka=" + lozinka + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.ime);
        hash = 53 * hash + Objects.hashCode(this.prezime);
        hash = 53 * hash + Objects.hashCode(this.korisnickoIme);
        hash = 53 * hash + Objects.hashCode(this.lozinka);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        final Korisnik other = (Korisnik) obj;
        if (Objects.equals(this.korisnickoIme, other.korisnickoIme) && Objects.equals(this.lozinka, other.lozinka)) {
            return true;
        }
        if (!Objects.equals(this.ime, other.ime)) {
            return false;
        }
        if (!Objects.equals(this.prezime, other.prezime)) {
            return false;
        }
        if (!Objects.equals(this.korisnickoIme, other.korisnickoIme)) {
            return false;
        }
        if (!Objects.equals(this.lozinka, other.lozinka)) {
            return false;
        }
        return true;
    }
    
}
