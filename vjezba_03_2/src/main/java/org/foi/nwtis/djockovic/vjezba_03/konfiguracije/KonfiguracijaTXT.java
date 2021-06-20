package org.foi.nwtis.djockovic.vjezba_03.konfiguracije;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class KonfiguracijaTXT extends KonfiguracijaApstraktna {

    public KonfiguracijaTXT(String nazivDatoteke) {
        super(nazivDatoteke);
    }

    @Override
    public void ucitajKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija {
        this.obrisiSvePostavke();
        
        if(nazivDatoteke == null || nazivDatoteke.length() == 0){
            throw new NeispravnaKonfiguracija("Naziv datoteke nije specificiran");
        }
        
        File file = new File(nazivDatoteke);
        if(file.exists() && file.isFile()){
            try {
                this.postavke.load(new FileInputStream(file));
            } catch (IOException ex) {
                throw new NeispravnaKonfiguracija("Problem kod učitavanja datoteke: '" + nazivDatoteke + "'!");
            }
        }else{
            throw new NeispravnaKonfiguracija("Datoteka pod nazivom: '" + nazivDatoteke + "' ne postoji!");
        }
    }

    @Override
    public void spremiKonfiguraciju(String datoteka) throws NeispravnaKonfiguracija {
        if(datoteka == null || datoteka.length() == 0){
            throw new NeispravnaKonfiguracija("Naziv datoteke nije specificiran");
        }
        
        File file = new File(datoteka);
        if(!file.exists() || file.exists() && file.isFile()){
            try {
                this.postavke.store(new FileOutputStream(file), "NWTiS 2021, Denis Jocković");
            } catch (IOException ex) {
                throw new NeispravnaKonfiguracija("Problem kod spremanja datoteke: '" + datoteka + "'!");
            }
        }else{
            throw new NeispravnaKonfiguracija("Datoteka pod nazivom: '" + datoteka + "' ne postoji!");
        }
    }
    
}
