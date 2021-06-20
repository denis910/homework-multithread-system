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
import static java.lang.Math.toRadians;
import static java.lang.String.valueOf;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.djockovic.vjezba_03.konfiguracije.Konfiguracija;
import org.foi.nwtis.djockovic.vjezba_03.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.djockovic.vjezba_03.konfiguracije.NeispravnaKonfiguracija;

/**
 * Klasa koja služi za obavljanje određenih operacija koje vraćaju informacije o aerodromima
 *
 * @author Denis Jocković
 */
public class ServerUdaljenosti {

    protected int port;
    protected int maksCekaca;
    protected String nazivDatotekeDnevnika;
    protected String udaljenostDozvoljeni;
    protected HashMap<String, DozvoljeneAdrese> dozvoljeneIPadrese = new HashMap<>();
    protected HashMap<String, Aerodrom> aerodromi = new HashMap<>();

    /**
     * Provjeravanje ispravnosti konfiguracijske datoteke, u slučaju neispravnosti gasi se server
     * Pokretanje servera
     *
     * @param args Naziv konfiguracijske datoteke
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("ERROR 39: Kriv broj argumenata! Molimo unos naziva datoteke");
            return;
        }
        String[] vrstaDatoteke = args[0].split("\\.");
        if (vrstaDatoteke.length == 0) {
            System.out.println("ERROR 39: Kriva vrsta datoteke! Potrebno: .txt, .bin, .xml, .json");
            return;
        }

        String vd = vrstaDatoteke[vrstaDatoteke.length - 1];
        if (!"txt".equals(vd) && !"xml".equals(vd) && !"json".equals(vd) && !"bin".equals(vd)) {
            System.out.println("ERROR 39: Kriva vrsta datoteke! Potrebno: .txt, .bin, .xml, .json");
            return;
        }

        ServerUdaljenosti server = new ServerUdaljenosti();
        if (!server.pripremiKonfiguracijskePodatke(args[0])) {
            return;
        }
        server.pokreniServer();
    }

    /**
     * Pozivanje funkcija za provjeravanje ispravnosti parametara u konfiguracijskoj datoteci
     * Upisivanje parametara u varijable
     *
     * @param nazivDatoteke Naziv konfiguracijske datoteke
     * @return Ako je su svi parametri i port dostupni vraća se true, inače false
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
            //TODO provjeri da li konf datoteka postoji
            System.out.println("ERROR 39: Ne postoji konfiguracijska datoteka!");
            return false;
        }
        return true;
    }

    /**
     * Provjerava se dostupnost određene postavke Zapisivanje dostupne postavke u prikladnu
     * varijablu
     *
     * @param konf objekt konfiguracije u kojem se nalaze postavke
     * @return vraća false ako neka od postavki nedostaje, inače true
     */
    public boolean ucitajParametre(Konfiguracija konf) {
        try {
            this.port = Integer.parseInt(konf.dajPostavku("port"));
        } catch (NumberFormatException ex) {
            System.out.println("ERROR 39: Datoteka nema ispravno postavljene parametre!");
            return false;
        }
        try {
            this.maksCekaca = Integer.parseInt(konf.dajPostavku("maks.cekaca"));
        } catch (NumberFormatException ex) {
            System.out.println("ERROR 39: Datoteka nema ispravno postavljene parametre!");
            return false;
        }
        try {
            this.nazivDatotekeDnevnika = konf.dajPostavku("datoteka.dnevnika");
        } catch (NumberFormatException ex) {
            System.out.println("ERROR 39: Datoteka nema ispravno postavljene parametre!");
            return false;
        }
        try {
            this.udaljenostDozvoljeni = konf.dajPostavku("server.udaljenosti.dozvoljeni");
        } catch (NumberFormatException ex) {
            System.out.println("ERROR 39: Datoteka nema ispravno postavljene parametre!");
            return false;
        }
        return true;
    }

    /**
     * Provjeravanje dostupnosti socketa
     *
     * @return Ako je socket dostupan vraća true, inače false
     */
    public boolean provjeriDostupnostResursa() {
        try ( ServerSocket ss = new ServerSocket(port, maksCekaca)) {

        } catch (IOException ex) {
            System.out.println("ERROR 39: Taj port nije dostupan");
            return false;
        }
        try {
            File f = new File(nazivDatotekeDnevnika);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException ex) {
                    System.out.println("ERROR 39: Nemoguće stvoriti datoteku!");
                    return false;
                }
            }
        } catch (NullPointerException ex) {
            System.out.println("ERROR 39: Nemoguće stvoriti datoteku!");
            return false;
        }
        return true;
    }

    /**
     * Popunjavanje hashmape dozvoljeneIPadrese s adresama s kojima ServerUdaljenosti smije
     * komunicirati
     */
    public void ucitajAdrese() {
        String[] listaAdresa = udaljenostDozvoljeni.split(",");
        for (int i = 0; i < listaAdresa.length; i++) {
            try {
                InetAddress host = InetAddress.getByName(listaAdresa[i]);
                String IPAdresa = host.getHostAddress();
                DozvoljeneAdrese a = new DozvoljeneAdrese(listaAdresa[i], IPAdresa);
                dozvoljeneIPadrese.put(a.naziv, a);
            } catch (UnknownHostException ex) {
                System.out.println("ERROR 39: Nije moguće pronaći IP adresu tražene opisne");
            }
        }
    }

    /**
     * Pokretanje servera i čekanje na upite
     */
    public void pokreniServer() {
        try {
            ServerSocket ss = new ServerSocket(port, maksCekaca);
            while (true) {
                Socket uticnica = ss.accept();
                obradiZahtjev(uticnica);
            }
        } catch (IOException ex) {
            System.out.println("ERROR 39: Nije moguće povezati se na socket");
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

            StringBuilder tekst = new StringBuilder();

            while (true) {
                int i = is.read();
                if (i == -1) {
                    break;
                }
                tekst.append((char) i);
            }
            uticnica.shutdownInput();
            String[] argumentiZahtjeva = tekst.toString().split(" ");
            String adresa = uticnica.getInetAddress().getHostAddress();
            String odgovor = odrediZahtjev(tekst.toString(), adresa);
            if (!"SINKRONIZACIJA".equals(argumentiZahtjeva[argumentiZahtjeva.length - 1])) {
                System.out.println("Prisutna veza: " + uticnica.getInetAddress().getHostAddress());
                System.out.println("ZAHTJEV: '" + tekst.toString() + "'");
                upisiDnevnik(adresa, uticnica.getInetAddress().getHostName(),
                        tekst.toString(), odgovor);
            }
            //TODO provjera sintakse primljenog zahtjeva i generiranje odgovora
            //sleep(10000);
            os.write(odgovor.getBytes());
            os.flush();
            uticnica.shutdownOutput();
            uticnica.close();
        } catch (IOException ex) {
            System.out.println("ERROR 39: Nije moguće obraditi zahtjev");
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
            System.out.println("ERROR 39: Nije moguće pronaći datoteku dnevnika");
        } catch (IOException ex) {
            System.out.println("ERROR 39: Nije moguće pročitati datoteku dnevnika");
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
            case "SYNC":
                return obradiSync(argumentiZahtjeva, adresa);
            case "DIST":
                return obradiDist(argumentiZahtjeva, adresa);
            case "CLEAR":
                return obradiClear(argumentiZahtjeva, adresa);
            default:
                return "ERROR 31: Prvi argument niste dobro unijeli!";
        }
    }

    /**
     * Provjeravanje ispravnosti IP adrese i upita Ažuriranje hashmape aerodromi
     *
     * @param argumentiZahtjeva Polje u kojem su zapisane vrijednosti potrebne za izvršavanje upita
     * @param adresa String u kojem se nalazi adresa s koje je poslan upit
     * @return Odgovor na upit
     */
    public String obradiSync(String[] argumentiZahtjeva, String adresa) {
        if (!provjeriAdresu(adresa)) {
            return "ERROR 32: To nije važeća adresa";
        }
        if (argumentiZahtjeva.length < 4) {
            return "ERROR 39: Potrebna su četiri argumenta";
        }
        if (!azurirajAerodrom(argumentiZahtjeva)) {
            Aerodrom a = izradiAerodrom(argumentiZahtjeva);
            aerodromi.put(a.icao, a);
        }
        return "OK";
    }

    /**
     * Provjeravanje ispravnosti IP adrese i upita Pozivanje funkcije za izračunavanje udaljenosti
     * dva aerodroma
     *
     * @param argumentiZahtjeva Polje u kojem su zapisane vrijednosti potrebne za izvršavanje upita
     * @param adresa String u kojem se nalazi adresa s koje je poslan upit
     * @return Odgovor na upit
     */
    public String obradiDist(String[] argumentiZahtjeva, String adresa) {
        if (!provjeriAdresu(adresa)) {
            return "ERROR 32: To nije važeća adresa";
        }
        if (argumentiZahtjeva.length != 3) {
            return "ERROR 39: Potrebna su tri argumenta";
        }
        List<Double> koordinate = dohvatiKoordinate(argumentiZahtjeva);
        return izracunajUdaljenost(koordinate);
    }

    /**
     * Dobavljanje podataka o aerodromima za izračun udaljenosti
     *
     * @param argumenti ICAO aerodroma čija se međusobna udaljenost želi izračunati
     * @return lista koordinata za aerodrome
     */
    public List<Double> dohvatiKoordinate(String[] argumenti) {
        List<Double> koordinate = new ArrayList<Double>();
        Iterator it = aerodromi.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Aerodrom vrijednost = (Aerodrom) pair.getValue();
            if (argumenti[1].equals(vrijednost.icao)) {
                koordinate.add(Double.parseDouble(vrijednost.gd));
                koordinate.add(Double.parseDouble(vrijednost.gs));
            }
            if (argumenti[2].equals(vrijednost.icao)) {
                koordinate.add(Double.parseDouble(vrijednost.gd));
                koordinate.add(Double.parseDouble(vrijednost.gs));
            }
        }
        return koordinate;
    }

    /**
     * Izračunavanje udaljenosti aerodroma
     *
     * @param koordinate geografska širina i duljina dva aerodroma
     * @return poruka o rješenju upita
     */
    public String izracunajUdaljenost(List<Double> koordinate) {
        if (koordinate.size() < 3 && koordinate.size() > 0) {
            return "ERROR 33: Ne postoje dva aerodroma s tim kodovima!";
        }
        if (koordinate.size() == 0) {
            return "ERROR 33: Ne postoje aerodromi s tim kodovima!";
        }
        koordinate = preracunajRadijani(koordinate);
        int R = 6371;
        Double gs = koordinate.get(1) - koordinate.get(3);
        Double gd = koordinate.get(0) - koordinate.get(2);
        Double a = Math.sin(gs / 2) * Math.sin(gs / 2)
                + Math.sin(gd / 2) * Math.sin(gd / 2)
                * Math.sin(koordinate.get(1)) * Math.sin(koordinate.get(3));
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        int rjesenje = (int) (R * c);
        return "OK " + valueOf(rjesenje);
    }

    public List<Double> preracunajRadijani(List<Double> koordinate) {
        for (int i = 0; i < koordinate.size(); i++) {
            koordinate.set(i, toRadians(koordinate.get(i)));
        }
        return koordinate;
    }

    public String obradiClear(String[] argumentiZahtjeva, String adresa) {
        if (!provjeriAdresu(adresa)) {
            return "ERROR 32: To nije važeća adresa";
        }
        aerodromi.clear();
        return "OK";
    }

    public Aerodrom izradiAerodrom(String[] zapis) {
        Aerodrom aerodrom = new Aerodrom(zapis[1], " ", zapis[2], zapis[3]);
        return aerodrom;
    }

    /**
     * Ažuriranje podataka o već upisano aerodromu
     *
     * @param zapis podaci o aerodromu
     * @return true ako je pronađen aerodrom i ažuriran, inače false
     */
    public boolean azurirajAerodrom(String[] zapis) {
        Iterator it = aerodromi.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Aerodrom vrijednost = (Aerodrom) pair.getValue();
            if (zapis[1].equals(vrijednost.icao)) {
                vrijednost.gs = zapis[2];
                vrijednost.gd = zapis[3];
                return true;
            }
        }
        return false;
    }

    /**
     * Provjeravanje dostupnosti IP adrese klijenta
     *
     * @param adresa IP adresa za provjeru
     * @return true ako je adresa dostupna, inače false
     */
    public boolean provjeriAdresu(String adresa) {
        Iterator it = dozvoljeneIPadrese.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DozvoljeneAdrese vrijednost = (DozvoljeneAdrese) pair.getValue();
            if (vrijednost.ipAdresa.equals(adresa)) {
                return true;
            }
        }
        return false;
    }
}
