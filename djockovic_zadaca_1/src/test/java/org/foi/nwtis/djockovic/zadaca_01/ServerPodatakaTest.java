/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.djockovic.zadaca_01;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.djockovic.vjezba_03.konfiguracije.Konfiguracija;
import org.foi.nwtis.djockovic.vjezba_03.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.djockovic.vjezba_03.konfiguracije.NeispravnaKonfiguracija;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;

/**
 *
 * @author NWTiS_1
 */
public class ServerPodatakaTest {
    Konfiguracija konf = null;
    String nazivDatoteke = "NWTiS_djockovic_test.txt";
    
    public ServerPodatakaTest() {
        
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        try {
            konf = KonfiguracijaApstraktna.kreirajKonfiguraciju(nazivDatoteke);
            konf.spremiPostavku("port", "9000");
            konf.spremiPostavku("maks.cekaca", "10");
            konf.spremiPostavku("maks.cekanje", "10");
            konf.spremiPostavku("maks.dretvi", "10");
            konf.spremiPostavku("sinkronizacija.trajanje", "10");
            konf.spremiPostavku("datoteka.aerodroma", "aerodromitest.csv");
            konf.spremiPostavku("server.provjera.adresa", "127.0.0.1");
            konf.spremiPostavku("server.provjera.port", "8000");
            konf.spremiPostavku("server.udaljenosti.adresa", "127.0.0.1");
            konf.spremiPostavku("server.udaljenosti.port", "8040");
            konf.spremiPostavku("datoteka.dnevnika", "dnevniktest.csv");
            konf.spremiKonfiguraciju();
        } catch (NeispravnaKonfiguracija ex) {
            fail("Test je pao problem s učitavanjem konfiguracije");
        }
        
        File file = new File(konf.dajPostavku("datoteka.aerodroma"));
        try {
            file.createNewFile();
        } catch (IOException ex) {
            fail("Ne može se stvoriti file");
        }
        List<String[]> unos = new ArrayList<String[]>();
        String[] string1 = {"LDZA","Zagreb Airport","45.7429008484","16.0687999725"};
        unos.add(string1);
        String[] string2 = {"LDVA","Varaždin Airport","46.2946472","16.382932662963867"};
        unos.add(string2);
        String[] string3 = {"LDSP","Split Airport","43.53889846","16.29800033569336"};
        unos.add(string3);
        try ( CSVWriter writer = new CSVWriter(new FileWriter(konf.dajPostavku("datoteka.aerodroma")),
                    ';')) {
                writer.writeAll(unos);
                writer.flush();
            } catch (FileNotFoundException ex) {
                fail("Ne može se pronaći file");
            } catch (IOException ex) {
                fail("Ne može se čitati i pisati u file");
            }
    }
    
    @AfterEach
    public void tearDown() {
        File f = new File(nazivDatoteke);
        if (f.exists()) {
            f.delete();
        }
        File f1 = new File(konf.dajPostavku("datoteka.aerodroma"));
        if (f1.exists()) {
            f1.delete();
        }
        File f2 = new File(konf.dajPostavku("datoteka.dnevnika"));
        if (f2.exists()) {
            f2.delete();
        }
        File f3 = new File("nepostoji.csv");
        if (f3.exists()) {
            f3.delete();
        }
    }

    /**
     * Test of main method, of class ServerPodataka.
     */
    @Test
    @Disabled
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        ServerPodataka.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pripremiKonfiguracijskePodatke method, of class ServerPodataka.
     */
    @Test
    public void testPripremiKonfiguracijskePodatke() {
        System.out.println("pripremiKonfiguracijskePodatke");
        
        ServerPodataka instance = new ServerPodataka();
        instance.pripremiKonfiguracijskePodatke(nazivDatoteke);
        
        assertEquals(Integer.parseInt(konf.dajPostavku("port")), instance.port);
        assertEquals(Integer.parseInt(konf.dajPostavku("maks.cekaca")), instance.maksCekaca);
        assertEquals(konf.dajPostavku("datoteka.aerodroma"), instance.nazivDatotekeAerodroma);
        
        HashMap<String, Aerodrom> aerodromi = new HashMap<>();
        Aerodrom a1 = new Aerodrom("LDZA","Zagreb Airport","45.7429008484","16.0687999725");
        aerodromi.put(a1.icao, a1);
        Aerodrom a2 = new Aerodrom("LDVA","Varaždin Airport","46.2946472","16.382932662963867");
        aerodromi.put(a2.icao, a2);
        Aerodrom a3 = new Aerodrom("LDSP","Split Airport","43.53889846","16.29800033569336");
        aerodromi.put(a3.icao, a3);
        assertEquals(aerodromi,instance.aerodromi);
    }

    /**
     * Test of ucitavanjePodatakaAerodroma method, of class ServerPodataka.
     */
    @Test
    public void testUcitavanjePodatakaAerodroma() {
        System.out.println("ucitavanjePodatakaAerodroma");
        ServerPodataka instance = new ServerPodataka();
        instance.ucitavanjePodatakaAerodroma(konf.dajPostavku("datoteka.aerodroma"));
        
        Aerodrom a1 = new Aerodrom("LDZA","Zagreb Airport","45.7429008484","16.0687999725");
        assertEquals(a1, instance.aerodromi.get(a1.icao));
        
        Aerodrom a2 = new Aerodrom("LDVA","Varaždin Airport","46.2946472","16.382932662963867");
        assertEquals(a2, instance.aerodromi.get(a2.icao));
        
        Aerodrom a3 = new Aerodrom("LDVA","Varaždin Airport","46.2946472","16.382932662963867");
        assertEquals(a3, instance.aerodromi.get(a3.icao));
         
        Aerodrom a4 = new Aerodrom("NEMA","Nepostojeći Airport","1.000","1.00");
        assertNotEquals(a4, instance.aerodromi.get(a4.icao));
    }

    /**
     * Test of izradiAerodrom method, of class ServerPodataka.
     */
    @Test
    public void testIzradiAerodrom() {
        System.out.println("izradiAerodrom");
        String[] zapis = {"AERO", "Testni aerodrom", "34.234", "34.53453"};
        ServerPodataka instance = new ServerPodataka();
        Aerodrom result = instance.izradiAerodrom(zapis);
        
        Aerodrom a = new Aerodrom("AERO", "Testni aerodrom", "34.234", "34.53453");
        assertEquals(a, result);
    }

    /**
     * Test of ucitajParametre method, of class ServerPodataka.
     */
    @Test
    public void testUcitajParametre() {
        System.out.println("ucitajParametre");
        ServerPodataka instance = new ServerPodataka();
        //Ako su u konfiguracijskoj datoteci sve potrebne postavke vraća se true
        boolean result = instance.ucitajParametre(konf);
        assertEquals(result, true);

        //Ako u konfiguracijskoj datoteci nisu upisani sve potrebne postavke vraća se false
        Konfiguracija konf2;
        try {
            konf2 = KonfiguracijaApstraktna.kreirajKonfiguraciju("nesto.txt");
            konf2.spremiPostavku("port", "9000");
            boolean result2 = instance.ucitajParametre(konf2);
            assertEquals(result2, false);
            File f = new File("nesto.txt");
            if (f.exists()) {
                f.delete();
            }
        } catch (NeispravnaKonfiguracija ex) {
            fail("Test je pao problem s učitavanjem konfiguracije");
        }
    }
    
    /**
     * Test of provjeraPovezanosti method, of class ServerPodataka.
     */
    @Test
    public void testprovjeraPovezanosti() {
        System.out.println("ucitajParametre");
        ServerPodataka instance = new ServerPodataka();
        //Ako su u konfiguracijskoj datoteci sve potrebne postavke vraća se true
        boolean result = instance.provjeraPovezanosti(konf);
        assertEquals(result, false);

        //Ako u konfiguracijskoj datoteci nisu upisani sve potrebne postavke vraća se false
        Konfiguracija konf2;
        try {
            konf2 = KonfiguracijaApstraktna.kreirajKonfiguraciju("nesto.txt");
            konf2.spremiPostavku("port", "9000");
            boolean result2 = instance.provjeraPovezanosti(konf2);
            assertEquals(result2, true);
            File f = new File("nesto.txt");
            if (f.exists()) {
                f.delete();
            }
        } catch (NeispravnaKonfiguracija ex) {
            fail("Test je pao problem s učitavanjem konfiguracije");
        }
        Korisnik k;
        ServerPristupa s;
    }

    /**
     * Test of provjeriDostupnostResursa method, of class ServerPodataka.
     */
    @Test
    public void testProvjeriDostupnostResursa() {
        System.out.println("provjeriDostupnostResursa");
        ServerPodataka instance = new ServerPodataka();
        System.out.println("provjeriDostupnostResursa");
        
        //pozivanje datoteke koja ne postoji
        instance.nazivDatotekeAerodroma = "nepostoji.csv";
        boolean result = instance.provjeriDostupnostResursa();
        assertEquals(false, result);
        
        //pozivanje datoteke koja postoji ali nije csv
        File file = new File("necsv.csvf");
        try {
            file.createNewFile();
        } catch (IOException ex) {
            fail("Ne može se stvoriti file");
        }
        
        instance.nazivDatotekeAerodroma = "necsv.csvf";
        boolean result2 = instance.provjeriDostupnostResursa();
        if(file.exists())
            file.delete();
        assertEquals(false, result2);
        
        //ako datoteka postoji i csv je
        instance.nazivDatotekeAerodroma = konf.dajPostavku("datoteka.aerodroma");
        instance.nazivDatotekeDnevnika = konf.dajPostavku("datoteka.dnevnika");
        boolean result3 = instance.provjeriDostupnostResursa();
        assertEquals(true, result3);
    }

    /**
     * Test of pokreniServer method, of class ServerPodataka.
     */
    @Test
    @Disabled
    public void testPokreniServer() {
        System.out.println("pokreniServer");
        ServerPodataka server = null;
        ServerPodataka instance = new ServerPodataka();
        instance.pokreniServer(server);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pokreniDretvu method, of class ServerPodataka.
     */
    @Test
    @Disabled
    public void testPokreniDretvu() {
        System.out.println("pokreniDretvu");
        Socket uticnica = null;
        ServerPodataka server = null;
        ServerPodataka instance = new ServerPodataka();
        instance.pokreniDretvu(uticnica, server);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pokreniSinkronizaciju method, of class ServerPodataka.
     */
    @Test
    @Disabled
    public void testPokreniSinkronizaciju() {
        System.out.println("pokreniSinkronizaciju");
        ServerPodataka server = null;
        ServerPodataka instance = new ServerPodataka();
        instance.pokreniSinkronizaciju(server);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
