package org.foi.nwtis.djockovic.zadaca_01;

import java.util.Objects;

/**
 * Klasa pomoću koje se stvaraju objekti u koje se pohranjuju informacije o aerodromima
 * @author Denis Jocković
 */
class Aerodrom {

    String icao;
    String naziv;
    String gs;
    String gd;

    Aerodrom(String icao, String naziv, String gs, String gd) {
        this.icao = icao;
        this.naziv = naziv;
        this.gs = gs;
        this.gd = gd;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.icao);
        hash = 59 * hash + Objects.hashCode(this.naziv);
        hash = 59 * hash + Objects.hashCode(this.gs);
        hash = 59 * hash + Objects.hashCode(this.gd);
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
        final Aerodrom other = (Aerodrom) obj;
        if (!Objects.equals(this.icao, other.icao)) {
            return false;
        }
        if (!Objects.equals(this.naziv, other.naziv)) {
            return false;
        }
        if (!Objects.equals(this.gs, other.gs)) {
            return false;
        }
        if (!Objects.equals(this.gd, other.gd)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Aerodrom{" + "icao=" + icao + ", naziv=" + naziv + ", gs=" + gs + ", gd=" + gd + '}';
    }
    
}
