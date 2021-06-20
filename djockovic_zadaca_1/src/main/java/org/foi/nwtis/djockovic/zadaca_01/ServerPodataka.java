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
import java.io.OutputStreamWriter;
import java.io.Writer;
import static java.lang.String.valueOf;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.djockovic.vjezba_03.konfiguracije.Konfiguracija;
import org.foi.nwtis.djockovic.vjezba_03.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.djockovic.vjezba_03.konfiguracije.NeispravnaKonfiguracija;

/**
 * Klasa koja služi za rad s korisnicima i sa drugim serverima Obrađivanje upita korisnika i
 * ažuriranje podataka servera udaljenosti
 *
 * @author Denis Jocković
 */
public class ServerPodataka {

    protected int port;
    protected int maksCekaca;
    protected int maksDretvi;
    protected int maksCekanje;
    protected int sinkronizacijaTrajanje;
    protected String nazivDatotekeAerodroma;
    protected String nazivDatotekeDnevnika;
    protected String provjeraAdresa;
    protected int provjeraPort;
    protected String udaljenostAdresa;
    protected int udaljenostPort;
    protected HashMap<String, Aerodrom> aerodromi = new HashMap<>();
    private int brojDretve = 0;
    public int trenutnoDretvi = 0;
    final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    /**
     * Provjeravanje ispravnosti konfiguracijske datoteke, u slučaju neispravnosti gasi se server
     * Pokretanje servera
     *
     * @param args Naziv konfiguracijske datoteke
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("ERROR 49: Krivi broj argumenata! Molimo unos naziv datoteke");
            return;
        }
        String[] vrstaDatoteke = args[0].split("\\.");
        if (vrstaDatoteke.length == 0) {
            System.out.println("ERROR 49: Kriva vrsta datoteke! Potrebno: .txt, .bin, .xml, .json");
            return;
        }
        String vd = vrstaDatoteke[vrstaDatoteke.length - 1];
        if (!"txt".equals(vd) && !"xml".equals(vd) && !"json".equals(vd) && !"bin".equals(vd)) {
            System.out.println("ERROR 49: Kriva vrsta datoteke! Potrebno: .txt, .bin, .xml, .json");
            return;
        }
        ServerPodataka server = new ServerPodataka();
        if (!server.pripremiKonfiguracijskePodatke(args[0])) {
            return;
        }
        server.pokreniServer(server);
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
                System.out.println("ERROR 49: Nisu dostupni svi podaci potrebni za konfiguraciju!");
                return false;
            }
            if (!provjeriDostupnostResursa()) {
                return false;
            }
        } catch (NeispravnaKonfiguracija ex) {
            System.out.println("ERROR 49: Ne postoji konfiguracijska datoteka!");
        }
        return true;
    }

    /**
     * Čitanje podataka o aerodromu iz pripadne datoteke Provjeravanje valjanosti zapisa datoteke
     * Upisivanje podataka u hashmapu aerodromi
     *
     * @param nazivDatoteke Datoteka u kojoj se nalaze pdoaci o korisniku
     */
    public void ucitavanjePodatakaAerodroma(String nazivDatoteke) {
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        try ( CSVReader reader = new CSVReaderBuilder(new FileReader(nazivDatoteke))
                .withCSVParser(parser)
                .build()) {
            String[] zapis;
            while ((zapis = reader.readNext()) != null) {
                if (zapis.length == 4) {
                    Aerodrom a = izradiAerodrom(zapis);
                    aerodromi.put(a.icao, a);
                } else {
                    System.out.println("ERROR 49: Zapis nije u redu!");
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR 49: Nije moguće pronaći datoteku aerodroma");
        } catch (IOException ex) {
            System.out.println("ERROR 49: Nije moguće pročitati datoteku aerodroma");
        }
    }

    public Aerodrom izradiAerodrom(String[] zapis) {
        Aerodrom aerodrom = new Aerodrom(zapis[0], zapis[1], zapis[2], zapis[3]);
        return aerodrom;
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
            return false;
        }
        try {
            this.maksCekaca = Integer.parseInt(konf.dajPostavku("maks.cekaca"));
        } catch (NumberFormatException ex) {
            return false;
        }
        try {
            this.maksCekanje = Integer.parseInt(konf.dajPostavku("maks.cekanje"));
        } catch (NumberFormatException ex) {
            return false;
        }
        try {
            this.maksDretvi = Integer.parseInt(konf.dajPostavku("maks.dretvi"));
        } catch (NumberFormatException ex) {
            return false;
        }
        if (provjeraPovezanosti(konf)) {
            return false;
        }
        try {
            this.sinkronizacijaTrajanje
                    = Integer.parseInt(konf.dajPostavku("sinkronizacija.trajanje"));
        } catch (NumberFormatException ex) {
            return false;
        }
        try {
            this.nazivDatotekeDnevnika = konf.dajPostavku("datoteka.dnevnika");
        } catch (NumberFormatException ex) {
            return false;
        }
        try {
            this.nazivDatotekeAerodroma = konf.dajPostavku("datoteka.aerodroma");
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    public boolean provjeraPovezanosti(Konfiguracija konf) {
        try {
            this.provjeraAdresa = konf.dajPostavku("server.provjera.adresa");
        } catch (NumberFormatException ex) {
            return true;
        }
        try {
            this.provjeraPort = Integer.parseInt(konf.dajPostavku("server.provjera.port"));
        } catch (NumberFormatException ex) {
            return true;
        }
        try {
            this.udaljenostAdresa = konf.dajPostavku("server.udaljenosti.adresa");
        } catch (NumberFormatException ex) {
            return true;
        }
        try {
            this.udaljenostPort = Integer.parseInt(konf.dajPostavku("server.udaljenosti.port"));
        } catch (NumberFormatException ex) {
            return true;
        }
        return false;
    }

    /**
     * Provjeravanje postoji li csv datoteka za učitavanje korisnika i je li port za server dostupan
     *
     * @return Ako je port dostupan i datoteka prikladna za rad vraća true, inače false
     */
    public boolean provjeriDostupnostResursa() {

        try ( ServerSocket ss = new ServerSocket(port, maksCekaca)) {

        } catch (IOException ex) {
            System.out.println("ERROR 49: Taj port nije dostupan");
            return false;
        }

        String[] vrstaDatoteke = nazivDatotekeAerodroma.split("\\.");
        if (!"csv".equals(vrstaDatoteke[vrstaDatoteke.length - 1])) {
            System.out.println("ERROR 49: Kriva vrsta datoteke! Potreban vrsta: .csv");
            return false;
        }
        
        try {
            File file = new File(nazivDatotekeAerodroma);
            if (file.exists() && file.isFile()) {
                ucitavanjePodatakaAerodroma(nazivDatotekeAerodroma);
            } else {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    System.out.println("ERROR 49: Nemoguće stvoriti datoteku!");
                    return false;
                }
            }
        } catch (NullPointerException ex) {
            System.out.println("ERROR 49: Nemoguće stvoriti datoteku!");
            return false;
        }

        try {
            File f = new File(nazivDatotekeDnevnika);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException ex) {
                    System.out.println("ERROR 49: Nemoguće stvoriti datoteku!");
                    return false;
                }
            }
        } catch (NullPointerException ex) {
            System.out.println("ERROR 49: Nemoguće stvoriti datoteku!");
            return false;
        }

        return true;
    }

    /**
     * Stvaranje server socketa Osluškivanje dolazećih zahtjeva Stvaranje dretve za sinhronizaciju
     * servera udaljenosti sa serverom podataka
     *
     * @param server objekt u kojem se nalazi referenca na trenutan server
     */
    public void pokreniServer(ServerPodataka server) {
        try {
            ServerSocket ss = new ServerSocket(port, maksCekaca);
            pokreniSinkronizaciju(server);
            while (true) {
                Socket uticnica = ss.accept();
                if (trenutnoDretvi < maksDretvi) {
                    trenutnoDretvi++;
                    pokreniDretvu(uticnica, server);
                }
            }
        } catch (IOException ex) {
            System.out.println("ERROR 49: Nije moguće povezati se na socket");
        }
    }

    public void pokreniDretvu(Socket uticnica, ServerPodataka server) {
        DretvaZahtjeva dz = new DretvaZahtjeva(uticnica, brojDretve++, server);
        dz.start();
    }

    public void pokreniSinkronizaciju(ServerPodataka server) {
        DretvaSinkronizacije dz = new DretvaSinkronizacije(server);
        dz.start();
    }

    /**
     * Dretva u kojoj se odvija obrada zahtjeva korisnika
     */
    public static class DretvaZahtjeva extends Thread {

        private Socket uticnica;
        private int brojDretve;
        private ServerPodataka server;

        public DretvaZahtjeva(Socket uticnica, int brojDretve, ServerPodataka server) {
            this.uticnica = uticnica;
            this.brojDretve = brojDretve;
            this.server = server;
        }

        @Override
        public void run() {
            obradiZahtjev();
        }

        /**
         * Obrada nadolazećeg zahtjeva Pretvaranje nadolazećih bajtova u stringove Pozivanje
         * funkcije za obradu zahtjeva Pozivanje funkcije za upis u dnevnik Ispisivanje rezultata
         * obrade Slanje rezultata obrade klijentu
         */
        public void obradiZahtjev() {
            try ( InputStream is = uticnica.getInputStream();  OutputStream os = uticnica.getOutputStream();) {
                System.out.println("Broj dretve: " + brojDretve + " Veza uspostavljena: "
                        + uticnica.getInetAddress().getHostAddress());
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
                String odgovor = odrediZahtjev(tekst.toString());
                upisiDnevnik(adresa,
                        uticnica.getInetAddress().getHostName(), tekst.toString(), odgovor);
                //TODO provjera sintakse primljenog zahtjeva i generiranje odgovora
                //sleep(10000);
                os.write(odgovor.getBytes());
                os.flush();
                uticnica.shutdownOutput();
                uticnica.close();
            } catch (IOException ex) {
                System.out.println("ERROR 49: Nije moguće obraditi zahtjev");
            }
            server.trenutnoDretvi--;
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
            String[] zapis = {v.toString(),
                valueOf(server.port), adresa, hostName, zahtjev, odgovor};
            upisiZapis(zapis);
        }

        /**
         * Upisivanje zapisa na kraj dokumenta dnevnika
         *
         * @param zapis zapis koji će se upisati u dnevnik
         */
        public void upisiZapis(String[] zapis) {
            try ( CSVWriter writer
                    = new CSVWriter(new FileWriter(server.nazivDatotekeDnevnika, true), ';')) {
                writer.writeNext(zapis);
                writer.flush();
            } catch (FileNotFoundException ex) {
                System.out.println("ERROR 49: Nije moguće pronaći datoteku dnevnika");
            } catch (IOException ex) {
                System.out.println("ERROR 49: Nije moguće pročitati datoteku dnevnika");
            }
        }

        /**
         * Odabir načina obrade zahtjeva
         *
         * @param zahtjev Zahtjev korisnika pomoću kojeg se određuje koja će se funkcija pozvati
         * @return Zapis obrade koji će se vratiti klijentu i zapisati u dnevnik
         */
        public String odrediZahtjev(String zahtjev) {
            String[] argumenti = zahtjev.split(" ");
            odrediSpavanje(argumenti);
            String komanda = " ";
            for (int i = 0; i < argumenti.length; i++) {
                komanda = obradiInfo(zahtjev, argumenti);
                if (!" ".equals(komanda)) {
                    break;
                }
                komanda = pronadiKomandu(argumenti[i], argumenti, i);
                if (!" ".equals(komanda)) {
                    break;
                }
            }
            return komanda;
        }

        /**
         * Stvaranje socketa za slanje zahtjeva Slanje zahtjeva Primanje odgovora Ispisivanje
         * odgovora
         *
         * @param komanda Upit koji se želi poslati nekom drugom serveru
         * @param adresa IP adresa na koju se upit želi poslati
         * @param port Port na kojeg se upit želi poslati
         * @return Odgovor drugog servera na upit
         */
        public String klijent(String komanda, String adresa, int port) {
            try ( Socket uticnica = new Socket(adresa, port);  InputStream ist = uticnica.getInputStream();  OutputStream ost = uticnica.getOutputStream();  OutputStreamWriter ow = new OutputStreamWriter(ost)) {

                ow.write(komanda);
                ow.flush();
                uticnica.shutdownOutput();

                StringBuilder tekst = new StringBuilder();

                while (true) {
                    int i = ist.read();
                    if (i == -1) {
                        break;
                    }
                    tekst.append((char) i);
                }
                uticnica.shutdownInput();
                String[] argumenti = komanda.split(" ");
                if (!"SYNC".equals(argumenti[0])) {
                    System.out.println("ODGOVOR: '" + tekst.toString() + "'");
                }
                uticnica.close();
                return tekst.toString();
            } catch (IOException ex) {
                if (port == server.provjeraPort) {
                    System.out.println("ERROR 49: Nije moguće doći do servera pristupa");
                } else {
                    System.out.println("ERROR 48: Nije moguće doći do servera udaljenosti");
                }
            }
            if (port == server.provjeraPort) {
                return "ERROR 49: Nije moguće doći do servera pristupa";
            } else {
                return "ERROR 48: Nije moguće doći do servera udaljenosti";
            }
        }

        /**
         * Odabir načina obrade zahtjeva
         *
         * @param argument Argument po kojem se gleda koju je akciju potrebno izvršiti
         * @param argumenti Podaci potrebni za izvršavanje akcije
         * @param i Broj pomoću kojeg se određuje na kojem je mjestu u polju argumenti određeni
         * podatak
         * @return Vraćanje odgovora nakon obrade akcije
         */
        public String pronadiKomandu(String argument, String[] argumenti, int i) {
            switch (argument) {
                case ("LIST"):
                    return obradiList(argumenti);
                case ("SYNC"):
                    return obradiSync(argumenti);
                case ("DIST"):
                    return obradiDist(argumenti, i);
                case ("CLEAR"):
                    return obradiClear(argumenti);
                case ("ADD"):
                    return obradiAdd(argumenti);
            }
            return " ";
        }

        /**
         * Provjeravanje postojanja važeće sjednice za korisnika koji je poslao zahtjev Stavljanje
         * podataka o svakom aerodromu u listu Stvaranje odgovora za korisnika
         *
         * @param argumenti Korisničko ime i lozinka koji služe za provjeru sjednice
         * @return String u kojem je zapisan odgovor za korisnika
         */
        public String obradiList(String[] argumenti) {
            if (!provjeriSjednicu(argumenti)) {
                return "ERROR 42: Ne postoji važeća sjednica";
            }

            List<String> popisAerodroma = new ArrayList<String>();
            int brojac = 0;
            server.rwl.readLock().lock();
            try {
                Iterator it = server.aerodromi.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    String vrijednost = (String) pair.getKey();
                    popisAerodroma.add(vrijednost);
                }
            } finally {
                server.rwl.readLock().unlock();
            }
            String odgovor = "OK ";
            for (int i = 0; i < popisAerodroma.size(); i++) {
                odgovor = odgovor.concat(popisAerodroma.get(i));
                if (i + 1 != popisAerodroma.size()) {
                    odgovor = odgovor.concat(" ");
                }
            }
            return odgovor;
        }

        /**
         * Provjeravanje postojanja važeće sjednice za korisnika koji je poslao zahtjev Slanje
         * podataka o aerodromima serveru udaljenosti
         *
         * @param argumenti Korisničko ime i lozinka koji služe za provjeru sjednice
         * @return String u kojem je zapisan odgovor za korisnika
         */
        public String obradiSync(String[] argumenti) {
            if (!provjeriSjednicu(argumenti)) {
                return "ERROR 42: Ne postoji važeća sjednica";
            }
            List<String> popisOdgovora = new ArrayList<String>();
            server.rwl.readLock().lock();
            try {
                Iterator it = server.aerodromi.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    Aerodrom vrijednost = (Aerodrom) pair.getValue();
                    popisOdgovora.add(klijent("SYNC " + vrijednost.icao + " " + vrijednost.gs + " "
                            + vrijednost.gd, server.udaljenostAdresa, server.udaljenostPort));
                }
            } finally {
                server.rwl.readLock().unlock();
            }

            return provjeriSync(popisOdgovora);
        }

        /**
         * Slanje zahtjeva serveru udaljenosti za izračunavanjem udaljenosti između aerodroma
         *
         * @param argumenti Sadrži ICAO aerodroma čiju se udaljenost želi izračunati
         * @param i Mjesta u polju na kojima se nalaze podaci
         * @return Odgovor servera udaljenosti ili poruka greške ako ne postoji sjednica
         */
        public String obradiDist(String[] argumenti, int i) {
            if (!provjeriSjednicu(argumenti)) {
                return "ERROR 42: Ne postoji važeća sjednica";
            }
            return klijent("DIST " + argumenti[i + 1] + " " + argumenti[i + 2],
                    server.udaljenostAdresa, server.udaljenostPort);
        }

        /**
         * Provjeravanje postojanja važeće sjednice za korisnika koji je poslao zahtjev Slanje
         * zahtjeva serveru udaljenosti za brisanjem svih podataka o aerodromima
         *
         * @param argumenti Korisničko ime i lozinka koji služe za provjeru sjednice
         * @return Odgovor servera udaljenosti ili poruka greške ako ne postoji sjednica
         */
        public String obradiClear(String[] argumenti) {
            if (!provjeriSjednicu(argumenti)) {
                return "ERROR 42: Ne postoji važeća sjednica";
            }
            return klijent("CLEAR", server.udaljenostAdresa, server.udaljenostPort);
        }

        /**
         * Provjeravanje je li zahtjev stvarno zahtjev za informacijama Pozivanje funkcije za
         * dobavljanje informacija
         *
         * @param zahtjev String u kojem je zapisan zahtjev korisnika prema serveru podataka
         * @param argumenti Polje stringova u kojem se nalazi ICAO aerodroma za kojeg se žele
         * vidjeti informacije
         * @return String s podacima o aerodromu, greška ako aerodrom ne postoji ili prazan string
         */
        public String obradiInfo(String zahtjev, String[] argumenti) {
            String uzorak = "^USER [a-žA-Ž\\d_-]{3,10} [0-9]{1,} AIRPORT [A-Z]{4}( SLEEP (300|[1-2][0-9][0-9]|[1-9][0-9]|[1-9]))?$";
            String[] a = {argumenti[argumenti.length - 1], argumenti[argumenti.length - 3]};
            if (!"SYNC".equals(a[0]) && !"SYNC".equals(a[1]) && !"LIST".equals(a[0])
                    && !"LIST".equals(a[1]) && regexProvjera(uzorak, zahtjev)) {
                return obradiInfo(argumenti);
            } else {
                return " ";
            }
        }

        /**
         * Dobavljanje informacija o određenom aerodromu
         *
         * @param argumenti Polje stringova u kojem se nalazi ICAO aerodroma za kojeg se žele
         * vidjeti informacije
         * @return String s podacima o aerodromu, greška ako aerodrom ne postoji
         */
        public String obradiInfo(String[] argumenti) {
            if (!provjeriSjednicu(argumenti)) {
                return "ERROR 42: Ne postoji važeća sjednica";
            }
            server.rwl.readLock().lock();
            try {
                Iterator it = server.aerodromi.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    Aerodrom v = (Aerodrom) pair.getValue();
                    if (v.icao.equals(argumenti[4])) {
                        return "OK " + v.icao + " \"" + v.naziv + "\" " + v.gs + " " + v.gd;
                    }
                }
            } finally {
                server.rwl.readLock().unlock();
            }
            return "ERROR 43: Ne postoji aerodrom s vrijednosti ICAO '" + argumenti[4];
        }

        /**
         * Provjeravanje postojanja važeće sjednice za korisnika koji je poslao zahtjev Pronalazak
         * aerodroma u hashmapi sa ICAO u argumenti polju i ažuriranje tog aerodroma U slučaju ne
         * pronalaska dodavanje navedenog aerodroma
         *
         * @param argumenti Polje stringova u kojem se nalaze podaci o aerodromu kojeg se želi
         * upisati u hashmapu i datoteku
         * @return Informacija o odrađenosti zadatka
         */
        public String obradiAdd(String[] argumenti) {
            if (!provjeriSjednicu(argumenti)) {
                return "ERROR 42: Ne postoji važeća sjednica";
            }
            boolean ulazak = false;
            int indeksNaziva = vratiNaziv(argumenti);
            String naziv = vratiNaziv(argumenti, indeksNaziva);
            server.rwl.readLock().lock();
            try {
                Iterator it = server.aerodromi.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    Aerodrom v = (Aerodrom) pair.getValue();
                    if (v.icao.equals(argumenti[5])) {
                        ulazak = true;
                        server.rwl.readLock().unlock();
                        server.rwl.writeLock().lock();
                        v.naziv = naziv;
                        v.gs = argumenti[indeksNaziva + 1];
                        v.gd = argumenti[indeksNaziva + 2];
                        pronadiCSV(v);
                    }
                }
            } finally {
                if (ulazak) {
                    server.rwl.writeLock().unlock();
                    return "OK";
                } else {
                    server.rwl.readLock().unlock();
                }
            }
            server.rwl.writeLock().lock();
            try {
                Aerodrom a = new Aerodrom(argumenti[5],
                        naziv, argumenti[indeksNaziva + 1], argumenti[indeksNaziva + 2]);
                server.aerodromi.put(a.icao, a);
                pronadiCSV(a);
            } finally {
                server.rwl.writeLock().unlock();
            }
            return "OK";
        }

        /**
         * Pronalazak zapisa o ažuriranom aerodromu u csv datoteci
         *
         * @param a
         */
        public void pronadiCSV(Aerodrom a) {
            String[] zapisCSV = {a.icao, a.naziv, a.gs, a.gd};
            int redZapisa = -1;
            CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
            try ( CSVReader reader
                    = new CSVReaderBuilder(new FileReader(server.nazivDatotekeAerodroma))
                            .withCSVParser(parser)
                            .build()) {
                        String[] zapis;
                        while ((zapis = reader.readNext()) != null) {
                            redZapisa++;
                            if (zapis[0].equals(zapisCSV[0])) {
                                break;
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        System.out.println("ERROR 49: Nije moguće pronaći datoteku: '"
                                + server.nazivDatotekeDnevnika + "'");
                    } catch (IOException ex) {
                        System.out.println("ERROR 49: Nije moguće pročitati datoteku: '"
                                + server.nazivDatotekeDnevnika + "'");
                    }
                    postaviCSV(zapisCSV, redZapisa);
        }

        /**
         * Postavljanje novog zapisa o aerodromu na prikladno mjestu u zapisima csv datoteke
         *
         * @param zapisCSV Zapis koji se želi postaviti u datoteku
         * @param redZapisa Mjesto na koje se ažurirani zapis želi postaviti
         */
        public void postaviCSV(String[] zapisCSV, int redZapisa) {
            CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
            try ( CSVReader reader
                    = new CSVReaderBuilder(new FileReader(server.nazivDatotekeAerodroma))
                            .withCSVParser(parser)
                            .build()) {
                        List<String[]> csvBody = reader.readAll();
                        if (csvBody.size() - 1 == redZapisa) {
                            csvBody.add(zapisCSV);
                        } else {
                            csvBody.set(redZapisa, zapisCSV);
                        }
                        unesiCSV(csvBody);
                    } catch (FileNotFoundException ex) {
                        System.out.println("ERROR 49: Nije moguće pronaći datoteku: '"
                                + server.nazivDatotekeDnevnika + "'");
                    } catch (IOException ex) {
                        System.out.println("ERROR 49: Nije moguće pročitati datoteku: '"
                                + server.nazivDatotekeDnevnika + "'");
                    }
        }

        /**
         * Upisivanje novih vrijednosti u csv datoteku
         *
         * @param csvBody Lista zapisa koja će se upisati u datoteku
         */
        public void unesiCSV(List<String[]> csvBody) {
            try ( CSVWriter writer = new CSVWriter(new FileWriter(server.nazivDatotekeAerodroma),
                    ';')) {
                writer.writeAll(csvBody);
                writer.flush();
            } catch (FileNotFoundException ex) {
                System.out.println("ERROR 49: Nije moguće pronaći datoteku dnevnika");
            } catch (IOException ex) {
                System.out.println("ERROR 49: Nije moguće pročitati datoteku dnevnika");
            }
        }

        /**
         * Pronalaženje zadnjeg elementa polja argumenti u kojem se nalazi dio naziva aerodroma
         *
         * @param argumenti Polje u kojem se nalaze podaci za ažuriranje/dodavanje novog aerodroma
         * @return Indeks na kojem se nalazi zadnji dio naziva aerodroma
         */
        public int vratiNaziv(String[] argumenti) {
            int indeksKraja;
            for (int i = 6; i < argumenti.length; i++) {
                String uzorak = "^((90|[1-8][0-9])\\.[\\d]{1,9}|(90|[1-8][0-9]))$";
                if (regexProvjera(uzorak, argumenti[i])) {
                    return indeksKraja = i - 1;
                }
            }
            return -1;
        }

        /**
         * Spajanje elemenata polja argumenti u kojem se nalazi naziv aerodroma
         *
         * @param argumenti Polje u kojem se nalaze dijelovi naziva aerodroma
         * @param indeksNaziva Indeks na kojem se nalazi zadnji dio naziva aerodroma
         * @return String u kojem je zapisan naziv polja
         */
        public String vratiNaziv(String[] argumenti, int indeksNaziva) {
            String naziv = argumenti[6];
            for (int i = 7; i < indeksNaziva + 1; i++) {
                naziv = naziv.concat(" ");
                naziv = naziv.concat(argumenti[i]);
            }
            naziv = naziv.substring(1, naziv.length() - 1);
            return naziv;
        }

        /**
         * Provjeravanje jesu li svi zapisi o aerodromima uspješno sinhronizirani
         *
         * @param popisOdgovora Popis odgovora od strane servera udaljenosti
         * @return Ako je sve u redu vraća potvrdan odgovor, inače vraća poruku greške
         */
        public String provjeriSync(List<String> popisOdgovora) {
            for (int i = 0; i < popisOdgovora.size(); i++) {
                if (popisOdgovora.get(i) != "OK") {
                    return popisOdgovora.get(i);
                }
            }
            return "OK";
        }

        /**
         * Šalje se upit na server pristupa kako bi se vidjelo imaju li korisnici pristup serveru
         *
         * @param argumenti Polje u kojem se nalaze podaci o korisničkom imenu i lozinci
         * @return True ako je vraćen potvrdan odgovor na upit, false inače
         */
        public boolean provjeriSjednicu(String[] argumenti) {
            String test = klijent("TEST " + argumenti[1] + " " + argumenti[2],
                    server.provjeraAdresa, server.provjeraPort);
            if (!"OK".equals(test)) {
                return false;
            }
            return true;
        }

        /**
         * Funkcija koja služi za izvršavanje regex provjere
         *
         * @param uzorak sadrži regularni izraz pomoću kojeg se ispituje valjanost
         * @param upisano sadrži string čija se valjanost ispituje
         * @return true ako string prođe regex provjeru, inače false
         */
        public boolean regexProvjera(String uzorak, String upisano) {
            Pattern pattern = Pattern.compile(uzorak);
            Matcher m = pattern.matcher(upisano);
            boolean status = m.matches();
            if (!status) {
                return false;
            }
            return true;
        }

        /**
         * Ako je u polju argumenti element s vrijednošću SLEEP određuje se koliko će server
         * odspavati
         *
         * @param argumenti Polje u kojem se može nalaziti informacija o duljini spavanja
         */
        public void odrediSpavanje(String[] argumenti) {

            if ("SLEEP".equals(argumenti[argumenti.length - 2])) {
                try {
                    TimeUnit.SECONDS.sleep(Integer.parseInt(argumenti[argumenti.length - 1]));
                } catch (InterruptedException ex) {
                    System.out.println("ERROR 49: Nije moguće odraditi spavanje");
                }
            }
        }
    }

    /**
     * Dretva pomoću koje se sinhroniziraju podaci sa servera podataka s podacima na serveru
     * udaljenosti
     */
    public static class DretvaSinkronizacije extends Thread {

        private ServerPodataka server;

        public DretvaSinkronizacije(ServerPodataka server) {
            this.server = server;
        }

        /**
         * Dobavljanje zapisa o aerodromima koji će se poslati serveru udaljenosti Slanje upita
         * serveru udaljenosti Čekanje na novi ciklus slanja
         */
        @Override
        public void run() {
            long startTime;
            while (true) {
                try {
                    startTime = System.currentTimeMillis();
                    List<String> popisOdgovora = new ArrayList<String>();
                    server.rwl.readLock().lock();
                    try {
                        Iterator it = server.aerodromi.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            Aerodrom vrijednost = (Aerodrom) pair.getValue();
                            klijent("SYNC " + vrijednost.icao + " "
                                    + vrijednost.gs + " " + vrijednost.gd + " SINKRONIZACIJA");
                        }
                    } finally {
                        server.rwl.readLock().unlock();
                    }
                    if (server.sinkronizacijaTrajanje
                            - (System.currentTimeMillis() - startTime) > 0) {
                        wait(server.sinkronizacijaTrajanje
                                - (System.currentTimeMillis() - startTime));
                    }
                } catch (InterruptedException ex) {

                } catch (IllegalMonitorStateException ex) {

                }
            }
        }

        public String klijent(String komanda) {
            try ( Socket uticnica = new Socket(server.udaljenostAdresa, server.udaljenostPort);  InputStream ist = uticnica.getInputStream();  OutputStream ost = uticnica.getOutputStream();  OutputStreamWriter ow = new OutputStreamWriter(ost)) {
                ow.write(komanda);
                ow.flush();
                uticnica.shutdownOutput();

                StringBuilder tekst = new StringBuilder();

                while (true) {
                    int i = ist.read();
                    if (i == -1) {
                        break;
                    }
                    tekst.append((char) i);
                }
                uticnica.shutdownInput();
                uticnica.close();
                return tekst.toString();
            } catch (IOException ex) {
            }
            return "ERROR 48: Nije moguće doći do servera udaljenosti";
        }
    }

}
