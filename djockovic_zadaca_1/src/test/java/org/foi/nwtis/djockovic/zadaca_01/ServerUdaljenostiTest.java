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
public class ServerUdaljenostiTest {
    Konfiguracija konf = null;
    String nazivDatoteke = "NWTiS_djockovic_test.txt";
    
    public ServerUdaljenostiTest() {
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
            konf.spremiPostavku("datoteka.dnevnika", "dnevniktest.csv");
            konf.spremiPostavku("server.udaljenosti.dozvoljeni", "localhost,127.0.0.1,barok.foi.hr");
            konf.spremiKonfiguraciju();
        } catch (NeispravnaKonfiguracija ex) {
            fail("Test je pao problem s učitavanjem konfiguracije");
        }
    }
    
    @AfterEach
    public void tearDown() {
        File f = new File(nazivDatoteke);
        if (f.exists()) {
            f.delete();
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
     * Test of main method, of class ServerUdaljenosti.
     */
    @Test
    @Disabled
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        ServerUdaljenosti.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pripremiKonfiguracijskePodatke method, of class ServerUdaljenosti.
     */
    @Test
    public void testPripremiKonfiguracijskePodatke() {
        System.out.println("pripremiKonfiguracijskePodatke");
        ServerUdaljenosti instance = new ServerUdaljenosti();
        
        instance.pripremiKonfiguracijskePodatke(nazivDatoteke);

        //ako datoteka postoji i završava s mogućim nastavcima i sadrži sve postavke vraća se true
        assertEquals(instance.pripremiKonfiguracijskePodatke(nazivDatoteke), true);

        //Ako ime datoteke ne završava s txt ili json ili bin ili xml ili ako ne postoji vraća se false 
        assertEquals(instance.pripremiKonfiguracijskePodatke("nesto"), false);
    }

    /**
     * Test of ucitajParametre method, of class ServerUdaljenosti.
     */
    @Test
    public void testUcitajParametre() {
        System.out.println("ucitajParametre");
        ServerUdaljenosti instance = new ServerUdaljenosti();
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
     * Test of provjeriDostupnostResursa method, of class ServerUdaljenosti.
     */
    @Test
    public void testProvjeriDostupnostResursa() {
        System.out.println("provjeriDostupnostResursa");
        ServerUdaljenosti instance = new ServerUdaljenosti();
        instance.nazivDatotekeDnevnika = konf.dajPostavku("datoteka.dnevnika");
        assertEquals(instance.provjeriDostupnostResursa(), true);
    }
    /**
     * Test of ucitajAdrese method, of class ServerUdaljenosti.
     */
    @Test
    public void testUcitajAdrese() {
        System.out.println("ucitajAdrese");
        ServerUdaljenosti instance = new ServerUdaljenosti();
        instance.udaljenostDozvoljeni = konf.dajPostavku("server.udaljenosti.dozvoljeni");
        instance.ucitajAdrese();
        String[] listaAdresa = konf.dajPostavku("server.udaljenosti.dozvoljeni").split(",");
        
        assertEquals(instance.dozvoljeneIPadrese.get("localhost").naziv, listaAdresa[0]);
        assertEquals(instance.dozvoljeneIPadrese.get("barok.foi.hr").naziv, listaAdresa[2]);
        assertEquals(instance.dozvoljeneIPadrese.get("127.0.0.1").ipAdresa, listaAdresa[1]);
    }

    /**
     * Test of pokreniServer method, of class ServerUdaljenosti.
     */
    @Test
    @Disabled
    public void testPokreniServer() {
        System.out.println("pokreniServer");
        ServerUdaljenosti instance = new ServerUdaljenosti();
        instance.pokreniServer();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obradiZahtjev method, of class ServerUdaljenosti.
     */
    @Test
    @Disabled
    public void testObradiZahtjev() {
        System.out.println("obradiZahtjev");
        Socket uticnica = null;
        ServerUdaljenosti instance = new ServerUdaljenosti();
        instance.obradiZahtjev(uticnica);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of upisiDnevnik method, of class ServerUdaljenosti.
     */
    @Test
    @Disabled
    public void testUpisiDnevnik() {
        System.out.println("upisiDnevnik");
        String adresa = "";
        String hostName = "";
        String zahtjev = "";
        String odgovor = "";
        ServerUdaljenosti instance = new ServerUdaljenosti();
        instance.upisiDnevnik(adresa, hostName, zahtjev, odgovor);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of upisiZapis method, of class ServerUdaljenosti.
     */
    @Test
    @Disabled
    public void testUpisiZapis() {
        System.out.println("upisiZapis");
        String[] zapis = null;
        ServerUdaljenosti instance = new ServerUdaljenosti();
        instance.upisiZapis(zapis);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of odrediZahtjev method, of class ServerUdaljenosti.
     */
    @Test
    public void testOdrediZahtjev() {
        System.out.println("odrediZahtjev");
        ServerUdaljenosti instance = new ServerUdaljenosti();
        
        //ako se ne unese pravilan upit ispisat će se greška
        String zahtjev1 = "SYNC korisnik";
        String adresa = "127.0.0.1";
        String expResult = "ERROR 31: Prvi argument niste dobro unijeli!";
        String result1 = instance.odrediZahtjev(zahtjev1, adresa);
        assertNotEquals(expResult, result1);
        
        String zahtjev2 = "KRIVO korisnik";
        String result2 = instance.odrediZahtjev(zahtjev2, adresa);
        assertEquals(expResult, result2);
    }

    /**
     * Test of obradiSync method, of class ServerUdaljenosti.
     */
    @Test
    public void testObradiSync() {
        System.out.println("obradiSync");
        ServerUdaljenosti instance = new ServerUdaljenosti();
        instance.dozvoljeneIPadrese.put("127.0.0.1",new DozvoljeneAdrese("localhost", "127.0.0.1"));
        
        //ako adresa nije u redu ispisuje se prikladna poruka
        String[] zahtjev1 = {"SYNC", "ICAO"};
        String adresa1 = "127.0.0.2";
        String expResult1 = "ERROR 32: To nije važeća adresa";
        String result1 = instance.obradiSync(zahtjev1, adresa1);
        assertEquals(expResult1, result1);
        
        //ako se ne unese dovoljan broj argumenata ispisuje se prikladna poruka
        String adresa2 = "127.0.0.1";
        String expResult2 = "ERROR 39: Potrebna su četiri argumenta";
        String result2 = instance.obradiSync(zahtjev1, adresa2);
        assertEquals(expResult2, result2);
        
        //ako se ne unese dovoljan broj argumenata ispisuje se prikladna poruka
        String[] zahtjev2 = {"SYNC", "ICAO", "naziv", "13.2", "15.3"};
        String expResult3 = "OK";
        String result3 = instance.obradiSync(zahtjev2, adresa2);
        assertEquals(expResult3, result3);
    }

    /**
     * Test of obradiDist method, of class ServerUdaljenosti.
     */
    @Test
    public void testObradiDist() {
        System.out.println("obradiSync");
        ServerUdaljenosti instance = new ServerUdaljenosti();
        instance.dozvoljeneIPadrese.put("127.0.0.1",new DozvoljeneAdrese("localhost", "127.0.0.1"));
        
        //ako adresa nije u redu ispisuje se prikladna poruka
        String[] zahtjev1 = {"DIST", "ICAO"};
        String adresa1 = "127.0.0.2";
        String expResult1 = "ERROR 32: To nije važeća adresa";
        String result1 = instance.obradiDist(zahtjev1, adresa1);
        assertEquals(expResult1, result1);
        
        //ako se ne unese dovoljan broj argumenata ispisuje se prikladna poruka
        String adresa2 = "127.0.0.1";
        String expResult2 = "ERROR 39: Potrebna su tri argumenta";
        String result2 = instance.obradiDist(zahtjev1, adresa2);
        assertEquals(expResult2, result2);
        
        //ako je sve pravilno uneseno vraća poruku odgovora
        String[] zahtjev2 = {"DIST", "ICAO1", "ICAO2"};
        String result3 = instance.obradiDist(zahtjev2, adresa2);
        assertNotEquals(expResult1, result3);
        assertNotEquals(expResult2, result3);
    }

    /**
     * Test of dohvatiKoordinate method, of class ServerUdaljenosti.
     */
    @Test
    public void testDohvatiKoordinate() {
        System.out.println("dohvatiKoordinate");
        ServerUdaljenosti instance = new ServerUdaljenosti();
        String[] zahtjev = {"DIST", "ICAO1", "ICAO2"};
        instance.aerodromi.put("ICAO1", new Aerodrom("ICAO1", "Prvi aerodrom", "46.666", "12.32"));
        instance.aerodromi.put("ICAO2", new Aerodrom("ICAO2", "Drugi aerodrom", "16.666", "42.32"));
        
        //vraća se lista i to uvijek tako da se prvo upisuje geografska dužina pa onda širina
        List<Double> result = instance.dohvatiKoordinate(zahtjev);
        List<Double> lista = new ArrayList<>();
        lista.add(12.32);
        lista.add(46.666);
        lista.add(42.32);
        lista.add(16.666);
        assertEquals(result, lista);
        
        //nije moguće upisati manji broj brojeva
        List<Double> lista2 = new ArrayList<>();
        lista2.add(12.32);
        lista2.add(46.666);
        assertNotEquals(result, lista2);
        
        //vraćaju se točno iste vrijednosti kao u hashmapi
        lista2.add(42.32);
        lista2.add(16.6);
        assertNotEquals(result, lista2);
    }

    /**
     * Test of izracunajUdaljenost method, of class ServerUdaljenosti.
     */
    @Test
    public void testIzracunajUdaljenost() {
        System.out.println("izracunajUdaljenost");
        ServerUdaljenosti instance = new ServerUdaljenosti();
        
        //ukoliko nema kooridanta ispisuje se prikladna poruka
        List<Double> lista = new ArrayList<>();
        
        String expResult1 = "ERROR 33: Ne postoje aerodromi s tim kodovima!";
        String result1 = instance.izracunajUdaljenost(lista);
        assertEquals(expResult1, result1);
        
        //ukoliko su nađene dvije koordinate ispisuje se prikladna poruka
        lista.add(12.32);
        lista.add(46.666);
        String expResult2 = "ERROR 33: Ne postoje dva aerodroma s tim kodovima!";
        String result2 = instance.izracunajUdaljenost(lista);
        assertEquals(expResult2, result2);
        
        //ukoliko je dobar  broj koordinata računa se udaljenost
        lista.add(42.32);
        lista.add(16.666);
        
        String expResult3 = "OK 3676";
        String result3 = instance.izracunajUdaljenost(lista);
        assertEquals(expResult3, result3);
    }

    /**
     * Test of preracunajRadijani method, of class ServerUdaljenosti.
     */
    @Test
    public void testPreracunajRadijani() {
        System.out.println("preracunajRadijani");
        ServerUdaljenosti instance = new ServerUdaljenosti();
        
        List<Double> lista = new ArrayList<>();
        lista.add(12.32);
        lista.add(46.666);
        
        List<Double> expResult = new ArrayList<>();
        expResult.add(0.2150245638457014);
        expResult.add(0.8144753487356737);
        
        List<Double> result = instance.preracunajRadijani(lista);
        assertEquals(expResult, result);

        //u listi se vraća točno onoliko brojeva koliko je ušlo
        expResult.add(4.1);
        assertNotEquals(expResult, result);
    }

    /**
     * Test of obradiClear method, of class ServerUdaljenosti.
     */
    @Test
    public void testObradiClear() {
        System.out.println("obradiClear");
        ServerUdaljenosti instance = new ServerUdaljenosti();
        instance.dozvoljeneIPadrese.put("127.0.0.1",new DozvoljeneAdrese("localhost", "127.0.0.1"));
        instance.aerodromi.put("ICAO1", new Aerodrom("ICAO1", "Prvi aerodrom", "46.666", "12.32"));
        
        //ako adresa nije u redu ispisuje se prikladna poruka
        String[] zahtjev1 = {"CLEAR"};
        String adresa1 = "127.0.0.2";
        String expResult1 = "ERROR 32: To nije važeća adresa";
        String result1 = instance.obradiClear(zahtjev1, adresa1);
        assertEquals(expResult1, result1);
        
        //nakon poziva funkcije vise nema aerodroma u hashmapi aerodromi
        instance.obradiClear(zahtjev1, "127.0.0.1");
        HashMap<String, Aerodrom> prazno = new HashMap<>();
        assertEquals(instance.aerodromi, prazno);
        
        //vraća se odgovor OK ako je adresa u redu
        String result2 = instance.obradiClear(zahtjev1, "127.0.0.1");
        assertEquals("OK", result2);
    }

    /**
     * Test of izradiAerodrom method, of class ServerUdaljenosti.
     */
    @Test
    public void testIzradiAerodrom() {
        System.out.println("izradiAerodrom");
        String[] zapis = {"SYNC","AERO","34.234", "34.53453"};
        ServerUdaljenosti instance = new ServerUdaljenosti();
        Aerodrom result = instance.izradiAerodrom(zapis);
        
        Aerodrom a1 = new Aerodrom("AERO", " ","34.234", "34.53453");
        assertEquals(a1, result);
        
        //ime aerodroma mora biti string od jednog razmaka
        Aerodrom a2 = new Aerodrom("AERO", "","34.234", "34.53453");
        assertNotEquals(a2, result);
        
        //koordinate moraju biti točno iste kao u ulaznom argumentu
        Aerodrom a3 = new Aerodrom("AERO", " ","34.2342", "34.534532");
        assertNotEquals(a3, result);
    }

    /**
     * Test of azurirajAerodrom method, of class ServerUdaljenosti.
     */
    @Test
    public void testAzurirajAerodrom() {
        System.out.println("azurirajAerodrom");
        String[] zapis = {"SYNC","ICAO","4.234", "34.53453"};
        ServerUdaljenosti instance = new ServerUdaljenosti();
        instance.aerodromi.put("ICAO", new Aerodrom("ICAO", "Aerodrom", "46.666", "12.32"));
        
        //vraćena je vrijednost true ako je pronađen aerodrom
        Boolean result = instance.azurirajAerodrom(zapis);
        assertEquals(result, true);
        
        //aerodromu je promijenjena vrijednost geografske širine i dužine na onu u polju zapis
        assertEquals(instance.aerodromi.get("ICAO"),new Aerodrom("ICAO", "Aerodrom", "4.234", "34.53453"));
        
        //ako nije nađen aerodrom vraćeno je false
        String[] zapis2 = {"SYNC","NEMA","4.234", "34.53453"};
        Boolean result2 = instance.azurirajAerodrom(zapis2);
        assertEquals(result2, false);
    }

    /**
     * Test of provjeriAdresu method, of class ServerUdaljenosti.
     */
    @Test
    public void testProvjeriAdresu() {
        System.out.println("provjeriAdresu");
        ServerUdaljenosti instance = new ServerUdaljenosti();
        
        //ako ne postoji adresa iz polja adresa vraća se false
        String adresa = "127.0.0.1";
        boolean expResult1 = false;
        boolean result = instance.provjeriAdresu(adresa);
        assertEquals(expResult1, result);
        
        //ako ne postoji adresa iz polja adresa vraća se false
        instance.dozvoljeneIPadrese.put("localhost", new DozvoljeneAdrese("127.0.0.1", "127.0.0.1"));
        result = instance.provjeriAdresu(adresa);
        boolean expResult2 = true;
        assertEquals(expResult2, result);
    }
}
