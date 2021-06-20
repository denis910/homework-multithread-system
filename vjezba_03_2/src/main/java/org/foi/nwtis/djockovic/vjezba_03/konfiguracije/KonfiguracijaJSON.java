package org.foi.nwtis.djockovic.vjezba_03.konfiguracije;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KonfiguracijaJSON extends KonfiguracijaApstraktna {

    public KonfiguracijaJSON(String nazivDatoteke) {
        super(nazivDatoteke);
    }

    @Override
    public void ucitajKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija {
        this.obrisiSvePostavke();

        if (nazivDatoteke == null || nazivDatoteke.length() == 0) {
            throw new NeispravnaKonfiguracija("Naziv datoteke nije specificiran");
        }

        File file = new File(nazivDatoteke);

        if (file.exists() && file.isFile()) {
            Gson gson = new Gson();
            try ( JsonReader reader = new JsonReader(new FileReader(file))) {
                this.postavke = gson.fromJson(reader, Properties.class);
            } catch (IOException ex) {
                throw new NeispravnaKonfiguracija("Datoteka: '" + nazivDatoteke + "' nije tipa Properties!");
            }
        } else {
            throw new NeispravnaKonfiguracija("Datoteka pod nazivom: '" + nazivDatoteke + "' ne postoji!");
        }
    }

    @Override
    public void spremiKonfiguraciju(String datoteka) throws NeispravnaKonfiguracija {
        if (datoteka == null || datoteka.length() == 0) {
            throw new NeispravnaKonfiguracija("Naziv datoteke nije specificiran");
        }

        File file = new File(datoteka);
        
        if (!file.exists() || file.exists() && file.isFile()) {
            try ( Writer writer = new FileWriter(file)) {
                Gson gson = new Gson();
                gson.toJson(this.postavke, writer);
            } catch (IOException ex) {
                throw new NeispravnaKonfiguracija("Problem kod spremanja datoteke: '" + datoteka + "'!");
            }
        } else {
            throw new NeispravnaKonfiguracija("Datoteke: '" + datoteka + "' ne postoji!");
        }
    }

}
