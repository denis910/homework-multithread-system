package org.foi.nwtis.djockovic.zadaca_01;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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

public class ServerPristupaTest {

    Konfiguracija konf = null;
    String nazivDatoteke = "NWTiS_djockovic_test.txt";

    public ServerPristupaTest() {
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
            konf.spremiPostavku("datoteka.korisnika", "korisnicitest.csv");
            konf.spremiPostavku("sjednica.trajanje", "500000");
            konf.spremiPostavku("datoteka.dnevnika", "dnevniktest.csv");
            konf.spremiPostavku("server.provjera.dozvoljeni", "localhost,127.0.0.1,barok.foi.hr");
            konf.spremiKonfiguraciju();
        } catch (NeispravnaKonfiguracija ex) {
            fail("Test je pao problem s učitavanjem konfiguracije");
        }
        
        File file = new File(konf.dajPostavku("datoteka.korisnika"));
        try {
            file.createNewFile();
        } catch (IOException ex) {
            fail("Ne može se stvoriti file");
        }
        List<String[]> unos = new ArrayList<String[]>();
        String[] string = {"Pero", "Kos", "pkos", "123456"};
        unos.add(string);
        try ( CSVWriter writer = new CSVWriter(new FileWriter(konf.dajPostavku("datoteka.korisnika")),
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
        File f1 = new File(konf.dajPostavku("datoteka.korisnika"));
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
     * Test of main method, of class ServerPristupa.
     */
    @Test
    @Disabled
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        ServerPristupa.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pripremiKonfiguracijskePodatke method, of class ServerPristupa.
     */
    @Test
    public void testPripremiKonfiguracijskePodatke() {
        System.out.println("pripremiKonfiguracijskePodatke");

        ServerPristupa instance = new ServerPristupa();
        instance.pripremiKonfiguracijskePodatke(nazivDatoteke);

        //ako datoteka postoji i završava s mogućim nastavcima i sadrži sve postavke vraća se true
        assertEquals(instance.pripremiKonfiguracijskePodatke(nazivDatoteke), true);

        //Ako ime datoteke ne završava s txt ili json ili bin ili xml ili ako ne postoji vraća se false 
        assertEquals(instance.pripremiKonfiguracijskePodatke("nesto"), false);
    }

    @Test
    public void testucitavanjePodatakaKorisnika() {
        System.out.println("pripremiKonfiguracijskePodatke");

        ServerPristupa instance = new ServerPristupa();
        instance.ucitavanjePodatakaKorisnika(konf.dajPostavku("datoteka.korisnika"));

        //Novi korisnik je jednak zapisanom ako se podudara sa svim atributima
        Korisnik k1 = new Korisnik("Pero", "Kos", "pkos", "123456");
        assertEquals(k1, instance.korisnici.get(k1.korisnickoIme));

        //Novi korisnik je jednak zapisanom ako se podudara korisničko ime i lozinka sa već zapisanim
        Korisnik k2 = new Korisnik("Kruno", "Peric", "pkos", "123456");
        assertEquals(k2, instance.korisnici.get(k2.korisnickoIme));

        //Inače korisnik nije jednak
        Korisnik k3 = new Korisnik("Pero", "Kos", "perok", "123456");
        assertNotEquals(k3, instance.korisnici.get(k3.korisnickoIme));
        Korisnik k4 = new Korisnik("Pero", "Kos", "pkos", "654321");
        assertNotEquals(k4, instance.korisnici.get(k4.korisnickoIme));
        Korisnik k5 = new Korisnik("Ivo", "Anić", "Ivan", "547896");
        assertNotEquals(k5, instance.korisnici.get(k5.korisnickoIme));
    }

    /**
     * Test of ucitajParametre method, of class ServerPristupa.
     */
    @Test
    public void testUcitajParametre() {
        System.out.println("ucitajParametre");
        ServerPristupa instance = new ServerPristupa();
        ServerPristupa instance2 = new ServerPristupa();
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
     * Test of provjeriDostupnostResursa method, of class ServerPristupa.
     */
    @Test
    public void testProvjeriDostupnostResursa() {
        System.out.println("provjeriDostupnostResursa");
        ServerPristupa instance = new ServerPristupa();
        
        //pozivanje datoteke koja ne postoji
        instance.nazivDatotekeKorisnika = "nepostoji.csv";
        boolean result = instance.provjeriDostupnostResursa();
        assertEquals(false, result);
        
        //pozivanje datoteke koja postoji ali nije csv
        File file = new File("necsv.csvf");
        try {
            file.createNewFile();
        } catch (IOException ex) {
            fail("Ne može se stvoriti file");
        }
        
        instance.nazivDatotekeKorisnika = "necsv.csvf";
        boolean result2 = instance.provjeriDostupnostResursa();
        if(file.exists())
            file.delete();
        assertEquals(false, result2);
        
        //ako datoteka postoji i csv je
        instance.nazivDatotekeKorisnika = konf.dajPostavku("datoteka.korisnika");
        instance.nazivDatotekeDnevnika = konf.dajPostavku("datoteka.dnevnika");
        boolean result3 = instance.provjeriDostupnostResursa();
        assertEquals(true, result3);
    }

    /**
     * Test of pokreniServer method, of class ServerPristupa.
     */
    @Test
    @Disabled
    public void testPokreniServer() {
        System.out.println("pokreniServer");
        ServerPristupa instance = new ServerPristupa();
        instance.pokreniServer();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obradiZahtjev method, of class ServerPristupa.
     */
    @Test
    @Disabled
    public void testObradiZahtjev() {
        System.out.println("obradiZahtjev");
        Socket uticnica = null;
        ServerPristupa instance = new ServerPristupa();
        instance.obradiZahtjev(uticnica);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of upisiDnevnik method, of class ServerPristupa.
     */
    @Test
    @Disabled
    public void testUpisiDnevnik() {
        System.out.println("upisiDnevnik");
        String adresa = "";
        String hostName = "";
        String zahtjev = "";
        String odgovor = "";
        ServerPristupa instance = new ServerPristupa();
        instance.upisiDnevnik(adresa, hostName, zahtjev, odgovor);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of upisiZapis method, of class ServerPristupa.
     */
    @Test
    @Disabled
    public void testUpisiZapis() {
        System.out.println("upisiZapis");
        String[] zapis = null;
        ServerPristupa instance = new ServerPristupa();
        instance.upisiZapis(zapis);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of izradiKorisnika method, of class ServerPristupa.
     */
    @Test
    public void testIzradiKorisnika() {
        System.out.println("izradiKorisnika");
        String[] zapis = {"Ime", "Prezime", "korime", "lozinka"};
        ServerPristupa instance = new ServerPristupa();
        Korisnik result = instance.izradiKorisnika(zapis);
        
        Korisnik k1 = new Korisnik("Ime", "Prezime", "korime", "lozinka");
        assertEquals(k1, result);
        
        //objekti klase Korisnik su jednaki ako su im korisnička imena i lozinke jednaki
        Korisnik k2 = new Korisnik("Name", "LastName", "korime", "lozinka");
        assertEquals(k2, result);
    }

    /**
     * Test of ucitajAdrese method, of class ServerPristupa.
     */
    @Test
    public void testUcitajAdrese() {
        System.out.println("ucitajAdrese");
        ServerPristupa instance = new ServerPristupa();
        instance.provjeraDozvoljeni = konf.dajPostavku("server.provjera.dozvoljeni");
        instance.ucitajAdrese();
        String[] listaAdresa = konf.dajPostavku("server.provjera.dozvoljeni").split(",");
        
        assertEquals(instance.dozvoljeneIPadrese.get("localhost").naziv, listaAdresa[0]);
        assertEquals(instance.dozvoljeneIPadrese.get("barok.foi.hr").naziv, listaAdresa[2]);
        assertEquals(instance.dozvoljeneIPadrese.get("127.0.0.1").ipAdresa, listaAdresa[1]);
    }

    /**
     * Test of odrediZahtjev method, of class ServerPristupa.
     */
    @Test
    public void testOdrediZahtjev() {
        System.out.println("odrediZahtjev");
        ServerPristupa instance = new ServerPristupa();
        
        //ako se ne unese pravilan upit ispisat će se greška
        String zahtjev1 = "AUTH korisnik";
        String adresa = "127.0.0.1";
        String expResult = "ERROR 10: Prvi argument niste dobro unijeli!";
        String result1 = instance.odrediZahtjev(zahtjev1, adresa);
        assertNotEquals(expResult, result1);
        
        String zahtjev2 = "KRIVO korisnik";
        String result2 = instance.odrediZahtjev(zahtjev2, adresa);
        assertEquals(expResult, result2);
    }

    /**
     * Test of obradiAuth method, of class ServerPristupa.
     */
    @Test
    public void testObradiAuth() {
        System.out.println("obradiAuth");
        ServerPristupa instance = new ServerPristupa();
        Korisnik k = new Korisnik("Pero", "Kos", "pkos", "123456");
        instance.korisnici.put(k.korisnickoIme, k);
        
        //ako se ne unese dovoljan broj argumenata ispisuje se prikladna poruka
        String[] zahtjev1 = {"AUTH", "korisnik"};
        String adresa1 = "127.0.0.1";
        String expResult1 = "ERROR 10: Niste unijeli dobar broj argumenata!";
        String result1 = instance.obradiAuth(zahtjev1);
        assertEquals(expResult1, result1);
        
        //ako se ne unese prikladno korisničko ime uz lozinku dobije se pogreška greške
        String[] zahtjev2 = {"AUTH", "pkos", "kriva"};
        String adresa2 = "127.0.0.1";
        String expResult2 = "ERROR 11: Ne slažu se korisničko ime i lozinke";
        String result2 = instance.obradiAuth(zahtjev2);
        assertEquals(expResult2, result2);
        
        String[] zahtjev3 = {"AUTH", "pko", "kriva"};
        String adresa3 = "127.0.0.1";
        String expResult3 = "ERROR 11: Ne postoji korisnik s takvim korisničkim imenom";
        String result3 = instance.obradiAuth(zahtjev3);
        assertEquals(expResult3, result3);
    }

    /**
     * Test of provjeraSjednice method, of class ServerPristupa.
     */
    @Test
    public void testProvjeraSjednice() {
        System.out.println("provjeraSjednice");
        ServerPristupa instance = new ServerPristupa();
        Korisnik k = new Korisnik("Pero", "Kos", "pkos", "123456");
        long vrijemeSjednica = System.currentTimeMillis();
        Sjednica s = new Sjednica(instance.sjednica.size(), k.korisnickoIme, vrijemeSjednica, vrijemeSjednica, Sjednica.StatusSjednice.Aktivna);
        instance.provjeraSjednice(k);
        
        assertEquals(s.getId(), instance.sjednica.get(0).getId());
    }

    /**
     * Test of obradiTest method, of class ServerPristupa.
     */
    @Test
    public void testObradiTest() {
        System.out.println("obradiTest");
        ServerPristupa instance = new ServerPristupa();
        
        //kada se ne unese dobar broj argumenata ispiše se prikladna poruka
        String[] zahtjev1 = {"TEST", "korisnik"};
        String adresa1 = "127.0.0.2";
        String expResult1 = "ERROR 10: Niste unijeli dobar broj argumenata!";
        String result1 = instance.obradiTest(zahtjev1, adresa1);
        assertEquals(expResult1, result1);
        
        //ako je nedostupna adresa ispisuje se prikladna poruka
        String[] zahtjev2 = {"TEST", "korisnik", "test"};
        String result2 = instance.obradiTest(zahtjev2, adresa1);
        String expResult2 = "ERROR 12: Zahtjev nije stigao s dozvoljene IP adresa";
        assertEquals(expResult2, result2);
        
        //ako je i IP adresa i korisničko ime i lozinka uredu ispisuje se potvrdna poruka
        instance.dozvoljeneIPadrese.put("localhost", new DozvoljeneAdrese("127.0.0.1", "127.0.0.1"));
        Korisnik k = new Korisnik("Pero", "Kos", "pkos", "123456");
        instance.provjeraSjednice(k);
        String[] zahtjev3 = {"TEST", "pkos", "123456"};
        String adresa2 = "127.0.0.1";
        String result3 = instance.obradiTest(zahtjev3, adresa2);
        String expResult3 = "OK";
        assertEquals(expResult3, result3);
        
        //ako korisničko ime ili lozinka nisu u redu ispisuje se prikladna poruka
        instance.dozvoljeneIPadrese.put("localhost", new DozvoljeneAdrese("127.0.0.1", "127.0.0.1"));
        String[] zahtjev4 = {"TEST", "pko", "123456"};
        String result4 = instance.obradiTest(zahtjev4, adresa2);
        String expResult4 = "ERROR 13: Korisnik '" + zahtjev4[1] + "' nema aktivnu sjednicu";
        assertEquals(expResult4, result4);
    }

}
