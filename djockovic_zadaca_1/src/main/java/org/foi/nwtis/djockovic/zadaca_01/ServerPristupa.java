package org.foi.nwtis.djockovic.zadaca_01;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.String.valueOf;
import static java.lang.Thread.sleep;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.djockovic.vjezba_03.konfiguracije.Konfiguracija;
import org.foi.nwtis.djockovic.vjezba_03.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.djockovic.vjezba_03.konfiguracije.NeispravnaKonfiguracija;

/**
 * Klasa koja služi za autoriziranje korisnika i dodijeljivanje te produljivanje sesija korisnika
 *
 * @author Denis Jocković
 */
public class ServerPristupa {

    protected int port;
    protected int maksCekaca;
    protected int sjednicaTrajanje;
    protected String nazivDatotekeKorisnika;
    protected String nazivDatotekeDnevnika;
    protected String provjeraDozvoljeni;
    protected HashMap<String, Korisnik> korisnici = new HashMap<>();
    protected HashMap<String, DozvoljeneAdrese> dozvoljeneIPadrese = new HashMap<>();
    protected Vector<Sjednica> sjednica = new Vector<>();

    /**
     * Provjerava se je li ime datoteke dobro napisano Instancira se objekt klase ServerPristupa
     * Uzimaju se svi potrebni podaci iz datoteke navedene u argumentu i pokrece se server
     *
     * @param args zapisano ime datoteke iz koje će se uzimati postavke za rad servera
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("ERROR 19: Kriv broj argumenata! Potrebna konfiguracijska datoteka");
            return;
        }
        String[] vrstaDatoteke = args[0].split("\\.");
        if (vrstaDatoteke.length == 0) {
            System.out.println("ERROR 19: Kriva vrsta datoteke! Potrebno: .txt, .bin, .xml, .json");
            return;
        }

        String vd = vrstaDatoteke[vrstaDatoteke.length - 1];
        if (!"txt".equals(vd) && !"xml".equals(vd) && !"json".equals(vd) && !"bin".equals(vd)) {
            System.out.println("ERROR 19: Kriva vrsta datoteke! Potrebno: .txt, .bin, .xml, .json");
            return;
        }
        ServerPristupa server = new ServerPristupa();
        if (!server.pripremiKonfiguracijskePodatke(args[0])) {
            return;
        }
        server.pokreniServer();
    }

    /**
     * Pozivanje funkcija za učitavanje podataka iz konfiguracijske datoteke i povezanih datoteka
     *
     * @param nazivDatoteke datoteka koja sadrži konfiguracije podatke u txt, xml, json ili bin
     * formatu
     * @return U slučaju da su podaci ispravno pročitani i unešeni vraća true, inače false
     */
    public boolean pripremiKonfiguracijskePodatke(String nazivDatoteke) {
        try {
            Konfiguracija konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
            if (!ucitajParametre(konf)) {
                return false;
            }
            if (!provjeriDostupnostResursa()) {
                return false;
            }
            ucitajAdrese();
        } catch (NeispravnaKonfiguracija ex) {
            System.out.println("ERROR 19: Ne postoji konfiguracijska datoteka!");
            return false;
        }
        return true;
    }

    /**
     * Učitava podatke iz konfiguracijske datoteke u varijable
     *
     * @param konf Sadrži sve parametre konfiguracije potrebne za rad servera
     * @return Ako neke od postavki potrebnih za rad servera nema u konf vraća false, inače true
     */
    public boolean ucitajParametre(Konfiguracija konf) {
        try {
            this.port = Integer.parseInt(konf.dajPostavku("port"));
        } catch (NumberFormatException ex) {
            System.out.println("ERROR 19: Datoteka nema ispravno postavljene parametre!");
            return false;
        }
        try {
            this.maksCekaca = Integer.parseInt(konf.dajPostavku("maks.cekaca"));
        } catch (NumberFormatException ex) {
            System.out.println("ERROR 19: Datoteka nema ispravno postavljene parametre!");
            return false;
        }
        try {
            this.nazivDatotekeKorisnika = konf.dajPostavku("datoteka.korisnika");
        } catch (NumberFormatException ex) {
            System.out.println("ERROR 19: Datoteka nema ispravno postavljene parametre!");
            return false;
        }
        try {
            this.provjeraDozvoljeni = konf.dajPostavku("server.provjera.dozvoljeni");
        } catch (NumberFormatException ex) {
            System.out.println("ERROR 19: Datoteka nema ispravno postavljene parametre!");
            return false;
        }
        try {
            this.sjednicaTrajanje = Integer.parseInt(konf.dajPostavku("sjednica.trajanje"));
        } catch (NumberFormatException ex) {
            System.out.println("ERROR 19: Datoteka nema ispravno postavljene parametre!");
            return false;
        }
        try {
            this.nazivDatotekeDnevnika = konf.dajPostavku("datoteka.dnevnika");
        } catch (NumberFormatException ex) {
            System.out.println("ERROR 19: Datoteka nema ispravno postavljene parametre!");
            return false;
        }
        return true;
    }

    /**
     * Provjeravanje postoji li csv datoteka za učitavanje korisnika i je li port za server dostupan
     *
     * @return Ako je port dostupan i datoteka prikladna za rad vraća true, inače false
     */
    public boolean provjeriDostupnostResursa() {

        try ( ServerSocket ss = new ServerSocket(port, maksCekaca)) {
        } catch (IOException ex) {
            System.out.println("ERROR 19: Taj port nije dostupan");
            return false;
        }

        String[] vrstaDatoteke = nazivDatotekeKorisnika.split("\\.");
        if (!"csv".equals(vrstaDatoteke[vrstaDatoteke.length - 1])) {
            System.out.println("ERROR 19: Kriva vrsta datoteke! Potreban vrsta: .csv");
            return false;
        }

        try {
            File file = new File(nazivDatotekeKorisnika);
            if (file.exists() && file.isFile()) {
                ucitavanjePodatakaKorisnika(nazivDatotekeKorisnika);
            } else {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    System.out.println("ERROR 19: Nemoguće stvoriti datoteku!");
                    return false;
                }
            }
        } catch (NullPointerException ex) {
            System.out.println("ERROR 19: Nemoguće stvoriti datoteku!");
            return false;
        }

        try {
            File f = new File(nazivDatotekeDnevnika);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException ex) {
                    System.out.println("ERROR 19: Nemoguće stvoriti datoteku!");
                    return false;
                }
            }
        } catch (NullPointerException ex) {
            System.out.println("ERROR 19: Nemoguće stvoriti datoteku!");
            return false;
        }

        return true;
    }

    /**
     * Stvaranje server socketa Osluškivanje dolazećih zahtjeva
     */
    public void pokreniServer() {
        try {
            ServerSocket ss = new ServerSocket(port, maksCekaca);
            while (true) {
                Socket uticnica = ss.accept();
                obradiZahtjev(uticnica);
            }
        } catch (IOException ex) {
            System.out.println("ERROR 19: Nije moguće povezati se na socket");
        }
    }

    /**
     * Obrada nadolazećeg zahtjeva Pretvaranje nadolazećih bajtova u stringove Pozivanje funkcije za
     * obradu zahtjeva Pozivanje funkcije za upis u dnevnik Ispisivanje rezultata obrade Slanje
     * rezultata obrade klijentu
     *
     * @param uticnica Socket na koji su primljeni podaci
     */
    public void obradiZahtjev(Socket uticnica) {
        try ( InputStream is = uticnica.getInputStream();  OutputStream os = uticnica.getOutputStream();) {
            System.out.println("Veza uspostavljena: " + uticnica.getInetAddress().getHostAddress());
            //TODO provjeri da li je dozvoljena IP adresa

            StringBuilder tekst = new StringBuilder();

            while (true) {
                int i = is.read();
                if (i == -1) {
                    break;
                }
                tekst.append((char) i);
            }
            uticnica.shutdownInput();
            System.out.println("ZAHTJEV: '" + tekst.toString() + "'");
            String adresa = uticnica.getInetAddress().getHostAddress();
            String odgovor = odrediZahtjev(tekst.toString(), adresa);
            upisiDnevnik(adresa,
                    uticnica.getInetAddress().getHostName(), tekst.toString(), odgovor);
            //sleep(10000);
            os.write(odgovor.getBytes());
            os.flush();
            uticnica.shutdownOutput();
            uticnica.close();
        } catch (IOException ex) {
            System.out.println("ERROR 19: Nije moguće obraditi zahtjev");
        }
    }

    /**
     * Stvaranje zapisa za dnevnik Pozivanje funkcija za upis zapisa u datoteku dnevnika
     *
     * @param adresa IP adresa računala korisnika
     * @param hostName Opisna adresa računala korisnika
     * @param zahtjev Zahtjev kojeg je korisnik poslao
     * @param odgovor Rezultat obrade zahtjeva
     */
    public void upisiDnevnik(String adresa, String hostName, String zahtjev, String odgovor) {
        Timestamp v = new Timestamp(System.currentTimeMillis());
        String[] zapis = {v.toString(), valueOf(this.port), adresa, hostName, zahtjev, odgovor};
        upisiZapis(zapis);
    }

    /**
     * Upisivanje zapisa na kraj dokumenta dnevnika
     *
     * @param zapis zapis koji će se upisati u dnevnik
     */
    public void upisiZapis(String[] zapis) {
        try ( CSVWriter writer = new CSVWriter(new FileWriter(nazivDatotekeDnevnika, true),
                ';')) {
            writer.writeNext(zapis);
            writer.flush();
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR 19: Nije moguće pronaći datoteku dnevnika");
        } catch (IOException ex) {
            System.out.println("ERROR 19: Nije moguće pročitati datoteku dnevnika");
        }
    }

    /**
     * Čitanje podataka o korisniku iz pripadne datoteke Provjeravanje valjanosti zapisa datoteke
     * Upisivanje podataka u hashmapu korisnici
     *
     * @param nazivDatoteke Datoteka u kojoj se nalaze poodaci o korisniku
     */
    public void ucitavanjePodatakaKorisnika(String nazivDatoteke) {
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        try ( CSVReader reader = new CSVReaderBuilder(new FileReader(nazivDatoteke))
                .withCSVParser(parser)
                .build()) {
            String[] zapis;
            while ((zapis = reader.readNext()) != null) {
                if (zapis.length == 4) {
                    Korisnik k = izradiKorisnika(zapis);
                    korisnici.put(k.korisnickoIme, k);
                } else {
                    System.out.println("ERROR 19: Zapis nije u redu!");
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR 19: Nije moguće pronaći datoteku: '" + nazivDatoteke + "'");
        } catch (IOException ex) {
            System.out.println("ERROR 19: Nije moguće pročitati datoteku: '" + nazivDatoteke + "'");
        }
    }

    public Korisnik izradiKorisnika(String[] zapis) {
        Korisnik korisnik = new Korisnik(zapis[1], zapis[0], zapis[2], zapis[3]);
        return korisnik;
    }

    /**
     * Učitavanje adresa s kojih mogu dolaziti zahtjevi u hashmapu dozvoljeneIPadresa Pronalaženje
     * IP adresa od dobivenih opisnih adresa
     */
    public void ucitajAdrese() {
        String[] listaAdresa = provjeraDozvoljeni.split(",");
        for (int i = 0; i < listaAdresa.length; i++) {
            try {
                InetAddress host = InetAddress.getByName(listaAdresa[i]);
                String IPAdresa = host.getHostAddress();
                DozvoljeneAdrese a = new DozvoljeneAdrese(listaAdresa[i], IPAdresa);
                dozvoljeneIPadrese.put(a.naziv, a);
            } catch (UnknownHostException ex) {
                System.out.println("ERROR 19: Nije moguće pronaći IP adresu dobivene opisne!");
            }
        }
    }

    /**
     * Odabir načina obrade zahtjeva
     *
     * @param zahtjev Zahtjev korisnika pomoću kojeg se određuje koja će se funkcija pozvati
     * @param adresa Adresa s koje je poslan zahtjev; Potrebno za testiranje dozvoljenosti IP adrese
     * @return Zapis obrade koji će se vratiti klijentu i zapisati u dnevnik
     */
    public String odrediZahtjev(String zahtjev, String adresa) {
        String[] argumentiZahtjeva = zahtjev.split(" ");
        switch (argumentiZahtjeva[0]) {
            case "AUTH":
                return obradiAuth(argumentiZahtjeva);
            case "TEST":
                return obradiTest(argumentiZahtjeva, adresa);
            default:
                return "ERROR 10: Prvi argument niste dobro unijeli!";
        }
    }

    /**
     * Obrađivanje zahtjeva za autentifikacijom Provjeravanje ispravnosti zahtjeva Provjeravanje
     * korisničkog imena i lozinke sa zapisima u hashmapi korisnici U poklapanja korisničkog imena i
     * lozinke, pozivanje funkcije za provjeru sjednice
     *
     * @param argumentiZahtjeva Polje u kojem su zapisane, između ostalog, vrijednosti korisničkog
     * imena i lozinke
     * @return Zapis obrade autentifikacije ili je pozitivno ili se vraća error
     */
    public String obradiAuth(String[] argumentiZahtjeva) {
        if (argumentiZahtjeva.length != 3) {
            return "ERROR 10: Niste unijeli dobar broj argumenata!";
        }

        String korisnickoIme = argumentiZahtjeva[1];
        String lozinka = argumentiZahtjeva[2];

        if (korisnici.containsKey(korisnickoIme)) {
            Korisnik korisnik = korisnici.get(korisnickoIme);
            if (korisnik.korisnickoIme.equals(korisnickoIme) && korisnik.lozinka.equals(lozinka)) {
                return provjeraSjednice(korisnik);
            } else {
                return "ERROR 11: Ne slažu se korisničko ime i lozinke";
            }
        } else {
            return "ERROR 11: Ne postoji korisnik s takvim korisničkim imenom";
        }
    }

    /**
     * Provjeravanje postojanja sjednice s korisnikom Produljenje sjednice u slučaju postojanja
     * odgovarajuće, a u suprotnom Stvaranje sjednice s prosljeđenim korisnikom
     *
     * @param korisnik Korisnik za kojeg je potrebno stvoriti sjednicu
     * @return Potvrdan odgovor u slučaju uspješnog stvaranja ili produljenja sjednice
     */
    public String provjeraSjednice(Korisnik korisnik) {
        Sjednica sjednicaKorisnik = null;
        for (int i = 0; i < sjednica.size(); i++) {
            Sjednica s = sjednica.get(i);
            if (s.getKorisnik().equals(korisnik.korisnickoIme)) {
                if (s.getStatus() == Sjednica.StatusSjednice.Aktivna) {
                    sjednicaKorisnik = sjednica.get(i);
                    break;
                }
            }
        }
        long vrijemeSjednica = sjednicaTrajanje + System.currentTimeMillis();
        if (sjednicaKorisnik != null) {
            sjednicaKorisnik.setVrijemeDoKadaVrijedi(vrijemeSjednica);
        } else {
            sjednicaKorisnik = new Sjednica(sjednica.size(), korisnik.korisnickoIme,
                    System.currentTimeMillis(), vrijemeSjednica, Sjednica.StatusSjednice.Aktivna);
            sjednica.add(sjednicaKorisnik);
        }
        return "OK " + sjednicaKorisnik.getId() + " " + sjednicaKorisnik.getVrijemeDoKadaVrijedi();
    }

    /**
     * Obrađivanje zahtjeva za potvrdom sjednice određenog korisnika Traženje IP adrese u hashmapi
     * dozvoljeneIPadrese Provjeravanje dostupnosti sjednice za određenog korisnika u slučaju
     * potvrde IP adrese
     *
     * @param argumentiZahtjeva Polje u kojem su zapisane, između ostalog, vrijednosti korisničkog
     * imena i lozinke
     * @param adresa IP adresa korisnika pomoću koje se provjerava je li ta adresa dozvoljena
     * @return Error ako adresa nije dozvoljena i ako ne postoji važeća sjednica, inače potvrdna
     * poruka
     */
    public String obradiTest(String[] argumentiZahtjeva, String adresa) {
        boolean adresaOK = false;
        if (argumentiZahtjeva.length != 3) {
            return "ERROR 10: Niste unijeli dobar broj argumenata!";
        }
        Iterator it = dozvoljeneIPadrese.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DozvoljeneAdrese vrijednost = (DozvoljeneAdrese) pair.getValue();
            if (vrijednost.ipAdresa.equals(adresa)) {
                adresaOK = true;
                break;
            }
        }
        if (adresaOK) {
            for (int i = 0; i < sjednica.size(); i++) {
                Sjednica s = sjednica.get(i);
                if (s.getKorisnik().equals(argumentiZahtjeva[1])) {
                    if (s.getStatus() == Sjednica.StatusSjednice.Aktivna) {
                        return "OK";
                    }
                }
            }
        } else {
            return "ERROR 12: Zahtjev nije stigao s dozvoljene IP adresa";
        }
        return "ERROR 13: Korisnik '" + argumentiZahtjeva[1] + "' nema aktivnu sjednicu";
    }
}
