package org.foi.nwtis.djockovic.zadaca_01;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Klasa koja služi za primanje i ispitivanje valjanosti korisničkih upita
 * @author Denis Jocković
 */
public class KorisnikServera {

    protected String adresaPristup;
    protected String adresaPodatak;
    protected int portPristup;
    protected int portPodatak;
    protected String udaljenost1;
    protected String udaljenost2;
    protected String aerodrom;
    protected String korisnickoIme;
    protected String lozinka;
    protected String dodajAerodrom;
    protected int spavanje;
    private int odabirAkcije;
    private int narediSpavanje = 0;
    private int brojSjednice;

    /**
     * Stvara se objekt klase KorisnikServera
     * Pozivanje funkcije za provjeravanje unesenih podataka
     * Pozivanje funkcije za slanje upita
     * @param args Argumenti sadrže podatke o korisniku, serverima za priključivanje i željenom upitu
     */
    public static void main(String[] args) {
        KorisnikServera ks = new KorisnikServera();
        if (!ks.provjeravanjePodataka(args)) {
            return;
        }
        ks.dobavljanjePodataka(args);
        //String odgovor = ks.izvrsiKomandu(komanda);
        //System.out.println("ODGOVOR: '" + odgovor + "'");
        /*
        for (int i = 0; i < 100; i++) {
            DretvaTest dt = new DretvaTest(adresaPristup, portPristup);
            dt.start();
        }
         */
    }

    /**
     * Stvara se dretva preko koje se šalje upit za autorizaciju te se čeka njezin završetak
     * Ako je odgovor dretve pozitivan poziva se funkcija za odabir daljnje akcije, inače se završava rad
     * @param args Argumenti sadrže podatke o korisniku, serverima za priključivanje i željenom upitu
     */
    private void dobavljanjePodataka(String[] args) {
        DretvaTest dt = new DretvaTest(adresaPristup, portPristup, 
                "AUTH " + this.korisnickoIme + " " + this.lozinka);
        dt.start();
        try {
            dt.join();
        } catch (InterruptedException ex) {
            System.out.println("Izvođenje programa je prekinuto!");
        }
        String[] odgovor = dt.odgovor.split(" ");
        if (!"OK".equals(odgovor[0])) {
            return;
        }
        this.brojSjednice = Integer.parseInt(odgovor[1]);
        odaberiAkciju(args);
    }

    /**
     * Provjerava se stvaraju li upisani argumenti smislen upit
     * Prva regularna provjera služi provjeravanju valjanosti upita kao jedne cjeline
     * Nakon toga se poziva funkcija koja služi za provjeravanje pojedinačnih djelova upita
     * Ako je upit u redu odabiru se akcije koje je potrebno poslati serveru na obradu, inače es završava rad
     * @param args Argumenti sadrže podatke o korisniku, serverima za priključivanje i željenom upitu
     * @return vraća se true ako je upit dobro napisa, inače false
     */
    private boolean provjeravanjePodataka(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String p = sb.toString().trim();
        String uzorakPocetni = "^-k [a-žA-Ž\\d_-]+ -l [a-žA-Ž\\d_#!-]+ -s1 [a-zA-Z\\d\\-_\\.!*'\"%$&+,\\/:;=?@]+ -p1 [0-9]+ -s2 [a-zA-Z\\d\\-_\\.!*'\"%$&+,\\/:;=?@]+ -p2 [0-9]+( --list| --sync| --dist [a-žA-Ž\\d:\\. ]+| --clear| --info [a-žA-Ž]+| --add [a-žA-Ž\\d:\\. \"]+)?( --sleep [0-9]+)?$";
        boolean pocetnaProvjera = regexProvjera(uzorakPocetni, p);
        if (!pocetnaProvjera) {
            System.out.println("Niste dobro upisali argumente");
            return false;
        }
        String[] argumenti = {"-k", "-l", "-s1", "-p1", "-s2", "-p2"};
        String[] uzorci = {"^[a-žA-Ž\\d_-]{3,10}$", "^[a-žA-Ž\\d_#!-]{3,10}$", "^(((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|[a-zA-Z][a-zA-Z\\d\\-_.!*'\"%$&+,\\/:;=?@]+)$", "^(8[0-9][0-9][0-9]|9000)$", "^(((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|[a-zA-Z][a-zA-Z\\d\\-_.!*'\"%$&+,\\/:;=?@]+)$", "^(8[0-9][0-9][0-9]|9000)$"};
        List<String> popisArgumenata = popisArgumenata(args, argumenti, uzorci);
        if (popisArgumenata.size() != 6) {
            return false;
        }
        if (!dodatniArgumenti(args)) {
            return false;
        }
        if (!provjeriSpavanje(args)) {
            return false;
        }
        upisPodataka(popisArgumenata);
        return true;
    }

    /**
     * Provjerava se valjanost pojedinačnih dijelova upita
     * @param args Sadrži pojedine argumente koji čine upit
     * @param svojstva Sadrži ključne riječi koje identificiraju pojedini dio upita, poput -k za korisničko ime
     * @param uzorci Sadrži regularne izraze pomoću kojih se provjerava ispravnost napisanih dijelova upita
     * @return vraća se null i ispisuje greška ako regex provjera ne valja, inače se vraća popis argumenata za daljnju obradu
     */
    private List<String> popisArgumenata(String[] args, String[] svojstva, String[] uzorci) {
        int brojSvojstva = 0;
        List<String> vrijednostiArgumenata = new ArrayList<String>();
        for (int i = 0; i < 12; i++) {
            if (svojstva[brojSvojstva].equals(args[i])) {
                if (regexProvjera(uzorci[brojSvojstva], args[i + 1])) {
                    vrijednostiArgumenata.add(args[i + 1]);
                    if (brojSvojstva != 5) {
                        brojSvojstva++;
                    }
                } else {
                    prijavaGreske(brojSvojstva);
                    return null;
                }
            }
        }
        return vrijednostiArgumenata;
    }

    /**
     * Funkcija koja služi za izvršavanje regex provjere
     * @param uzorak sadrži regularni izraz pomoću kojeg se ispituje valjanost
     * @param upisano sadrži string čija se valjanost ispituje
     * @return true ako string prođe regex provjeru, inače false
     */
    private boolean regexProvjera(String uzorak, String upisano) {
        Pattern pattern = Pattern.compile(uzorak);
        Matcher m = pattern.matcher(upisano);
        boolean status = m.matches();
        if (!status) {
            return false;
        }
        return true;
    }

    /**
     * Funkcija služi za ispisivanje razloga zbog kojeg se pojavila greška
     * @param brojSvojstva služi za identificiranje točnog stringa za ispis
     * @return vraća se false
     */
    private boolean prijavaGreske(int brojSvojstva) {
        switch (brojSvojstva) {
            case 0:
                System.out.println("Krivo ste upisali korisničko ime!");
                return false;
            case 1:
                System.out.println("Krivo ste upisali lozinku!");
                return false;
            case 2,4:
                System.out.println("Krivo ste upisali adresu!");
                return false;
            case 3,5:
                System.out.println("Krivo ste upisali port!");
                return false;
            case 6:
                System.out.println("Krivo ste upisali ICAO-ve!");
                return false;
            case 7:
                System.out.println("Krivo ste upisali ICAO");
                return false;
            case 8:
                System.out.println("Krivo ste upisali podatke za unos aerodroma!");
                return false;
            case 9:
                System.out.println("Krivo ste upisali podatke za spavanje!");
                return false;
        }
        return false;
    }

    /**
     * Provjeravanje svojstava koja nisu provjerena u popisArgumenata i postavljanje varijable odabir akcije
     * @param args Sadrži pojedine argumente koji čine upit
     * @return vraća istinu ako sve regex provjere prođu, inače false
     */
    private boolean dodatniArgumenti(String[] args) {
        for (int i = 12; i < args.length; i++) {
            switch (args[i]) {
                case ("--list"):
                    this.odabirAkcije = 0;
                    break;
                case ("--sync"):
                    this.odabirAkcije = 1;
                    break;
                case ("--dist"):
                    String u = "^[A-Z]{4}$";
                    if (args.length < 15) {
                        return prijavaGreske(6);
                    }
                    if (regexProvjera(u, args[i + 1]) && regexProvjera(u, args[i + 2])) {
                        this.odabirAkcije = 2;
                    } else {
                        return prijavaGreske(6);
                    }
                    break;
                case ("--clear"):
                    this.odabirAkcije = 3;
                    break;
                case ("--info"):
                    if (regexProvjera("^[A-Z]{4}$", args[i + 1])) {
                        this.odabirAkcije = 4;
                    } else {
                        return prijavaGreske(7);
                    }
                    break;
                case ("--add"):
                    String uzorak = "^[A-Z]{4}:[A-Ža-ž ]+:((90|[1-8][0-9]|[1-9])\\.[\\d]{1,9}|(90|[1-8][0-9]|[1-9])):((190|1[0-8][0-9]|[1-9][0-9]|[1-9])\\.[\\d]{1,9}|(190|1[0-8][0-9]|[1-9][0-9]|[1-9]))$";
                    if (regexProvjera(uzorak, args[i + 1])) {
                        this.odabirAkcije = 5;
                    } else {
                        return prijavaGreske(8);
                    }
                    break;
            }
        }
        return true;
    }

    /**
     * Provjerava treba li poslati zahtjev za spavanjem i ako treba postavlja varijablu narediSpavanje na tu vrijednost
     * @param args Sadrži pojedine argumente koji čine upit
     * @return vraća istinu ako regex provjera prođe, inače laž
     */
    private boolean provjeriSpavanje(String[] args) {
        if (args[args.length - 2].equals("--sleep")) {
            if (regexProvjera("^(300|[1-2][0-9][0-9]|[1-9][0-9]|[1-9])$", args[args.length - 1])) {
                this.narediSpavanje = Integer.parseInt(args[args.length - 1]);
            } else {
                return prijavaGreske(9);
            }
        }
        return true;
    }

    /**
     * Popunjavanje varijabli potrebnih za slanje upita i pozivanje funkcije za provjeru adrese servera
     * @param popisArgumenata Lista stringova u kojima se nalazi potrebni podaci
     */
    private void upisPodataka(List<String> popisArgumenata) {
        this.korisnickoIme = popisArgumenata.get(0);
        this.lozinka = popisArgumenata.get(1);
        odrediAdresu(popisArgumenata.get(2), "pristup");
        this.portPristup = Integer.parseInt(popisArgumenata.get(3));
        odrediAdresu(popisArgumenata.get(4), "podatak");
        this.portPodatak = Integer.parseInt(popisArgumenata.get(5));
    }

    /**
     * Provjeravanje ispravnosti zapisa adrese
     * @param adresa adresa čija se ispravnost provjerava
     * @param vrsta "pristup" ako je adresa za ServerPristupa, inače "podatak"
     */
    private void odrediAdresu(String adresa, String vrsta) {
        String uzorak = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        if (regexProvjera(uzorak, adresa)) {
            if ("pristup".equals(vrsta)) {
                this.adresaPristup = adresa;
            } else {
                this.adresaPodatak = adresa;
            }
        } else {
            odrediIPadrese(adresa, vrsta);
        }
    }

    /**
     * Određivanje IP adrese od opisne adrese
     * @param adresa Opisna adresa čija se IP adresa treba odrediti
     * @param vrsta  "pristup" ako je adresa za ServerPristupa, inače "podatak"
     */
    private void odrediIPadrese(String adresa, String vrsta) {
        try {
            InetAddress IPadresa = InetAddress.getByName(adresa);
            if (vrsta != "pristup") {
                this.adresaPristup = IPadresa.getHostAddress();
            } else {
                this.adresaPodatak = IPadresa.getHostAddress();
            }
        } catch (UnknownHostException ex) {
            System.out.println("Nije moguće pronaći IP adresu od te opisne adrese!");
        }
    }

    /**
     * Funkcija u kojoj se odabire funkcija prema akciji koju korisnik želi izvršiti
     * @param args Podaci potrebni za izvršavanje određenih akcija
     */
    private void odaberiAkciju(String[] args) {
        switch (this.odabirAkcije) {
            case (0):
                akcijaList();
                break;
            case (1):
                akcijaSync();
                break;
            case (2):
                akcijaDist(args);
                break;
            case (3):
                akcijaClear();
                break;
            case (4):
                akcijaInfo(args);
                break;
            case (5):
                akcijaAdd(args);
                break;
        }
    }

    /**
     * Slanje upita za dobavljanje liste aerodroma s podacima
     */
    private void akcijaList() {
        if (this.narediSpavanje == 0) {
            DretvaTest dt = new DretvaTest(adresaPodatak, portPodatak, 
                    "USER " + this.korisnickoIme + " " + this.brojSjednice + " AIRPORT LIST");
            dt.start();
            try {
                dt.join();
            } catch (InterruptedException ex) {
                System.out.println("Izvođenje programa je prekinuto!");
            }
        } else {
            DretvaTest dt = new DretvaTest(adresaPodatak, portPodatak, 
                    "USER " + this.korisnickoIme + " " + this.brojSjednice + " AIRPORT LIST SLEEP " 
                            + narediSpavanje);
            dt.start();
            try {
                dt.join();
            } catch (InterruptedException ex) {
                System.out.println("Izvođenje programa je prekinuto!");
            }
        }
    }
    
    /**
     * Slanje upita za izjednačavanje podataka o aerodromu u serveru podataka i udaljenosti
     */
    private void akcijaSync() {
        if (this.narediSpavanje == 0) {
            DretvaTest dt = new DretvaTest(adresaPodatak, portPodatak, 
                    "USER " + this.korisnickoIme + " " + this.brojSjednice + " AIRPORT SYNC");
            dt.start();
            try {
                dt.join();
            } catch (InterruptedException ex) {
                System.out.println("Izvođenje programa je prekinuto!");
            }
        } else {
            DretvaTest dt = new DretvaTest(adresaPodatak, portPodatak, 
                    "USER " + this.korisnickoIme + " " + this.brojSjednice + " AIRPORT SYNC SLEEP " 
                            + narediSpavanje);
            dt.start();
            try {
                dt.join();
            } catch (InterruptedException ex) {
                System.out.println("Izvođenje programa je prekinuto!");
            }
        }
    }
    
    /**
     * Slanje podataka za upit kojim se dobavlja udaljenost neka dva aerodroma
     * @param args podaci u kojima se nalazi popis aerodroma čija se udaljenost želi dobaviti
     */
    private void akcijaDist(String[] args) {
        if (this.narediSpavanje == 0) {
            DretvaTest dt = new DretvaTest(adresaPodatak, portPodatak, 
                    "USER " + this.korisnickoIme + " " + this.brojSjednice 
                            + " AIRPORT DIST " + args[13] + " " + args[14]);
            dt.start();
            try {
                dt.join();
            } catch (InterruptedException ex) {
                System.out.println("Izvođenje programa je prekinuto!");
            }
        } else {
            DretvaTest dt = new DretvaTest(adresaPodatak, portPodatak, 
                    "USER " + this.korisnickoIme + " " + this.brojSjednice + " AIRPORT DIST " 
                            + args[13] + " " + args[14] + " SLEEP " + narediSpavanje);
            dt.start();
            try {
                dt.join();
            } catch (InterruptedException ex) {
                System.out.println("Izvođenje programa je prekinuto!");
            }
        }
    }
    
    /**
     * Slanje upita kojim se briše sve što se nalazi u serveru udaljenosti
     */
    private void akcijaClear() {
        if (this.narediSpavanje == 0) {
            DretvaTest dt = new DretvaTest(adresaPodatak, portPodatak, 
                    "USER " + this.korisnickoIme + " " + this.brojSjednice + " AIRPORT CLEAR");
            dt.start();
            try {
                dt.join();
            } catch (InterruptedException ex) {
                System.out.println("Izvođenje programa je prekinuto!");
            }
        } else {
            DretvaTest dt = new DretvaTest(adresaPodatak, portPodatak, 
                    "USER " + this.korisnickoIme + " " + this.brojSjednice + " AIRPORT CLEAR SLEEP " 
                            + narediSpavanje);
            dt.start();
            try {
                dt.join();
            } catch (InterruptedException ex) {
                System.out.println("Izvođenje programa je prekinuto!");
            }
        }
    }
    
    /**
     * Slanje upita za dobavljanje informacija o određenom aerodromu
     * @param args ICAO aerodroma o kojem se žele dobaviti podaci
     */
    private void akcijaInfo(String[] args) {
        if (this.narediSpavanje == 0) {
            DretvaTest dt = new DretvaTest(adresaPodatak, portPodatak, 
                    "USER " + this.korisnickoIme + " " + this.brojSjednice 
                            + " AIRPORT " + args[13]);
            dt.start();
            try {
                dt.join();
            } catch (InterruptedException ex) {
                System.out.println("Izvođenje programa je prekinuto!");
            }
        } else {
            DretvaTest dt = new DretvaTest(adresaPodatak, portPodatak, 
                    "USER " + this.korisnickoIme + " " + this.brojSjednice + " AIRPORT " 
                            + args[13] + " SLEEP " + narediSpavanje);
            dt.start();
            try {
                dt.join();
            } catch (InterruptedException ex) {
                System.out.println("Izvođenje programa je prekinuto!");
            }
        }
    }
    
    /**
     * Slanje upita kojim se dodaje ili ažurira aerodrom
     * @param args Podaci o aerodromu kojeg se želi dodati/ažurirati
     */
    private void akcijaAdd(String[] args) {
        String[] vrijednosti = args[13].split(":");
        if (this.narediSpavanje == 0) {
            DretvaTest dt = new DretvaTest(adresaPodatak, portPodatak, 
                    "USER " + this.korisnickoIme + " " + this.brojSjednice 
                            + " AIRPORT ADD " + vrijednosti[0] + " \"" + vrijednosti[1] + "\" " 
                            + vrijednosti[2] + " " + vrijednosti[3]);
            dt.start();
            try {
                dt.join();
            } catch (InterruptedException ex) {
                System.out.println("Izvođenje programa je prekinuto!");
            }
        } else {
            DretvaTest dt = new DretvaTest(adresaPodatak, portPodatak, 
                    "USER " + this.korisnickoIme + " " + this.brojSjednice + " AIRPORT ADD " 
                            + vrijednosti[0] + " \"" + vrijednosti[1] + "\" " + vrijednosti[2] + " " 
                            + vrijednosti[3] + " SLEEP " + narediSpavanje);
            dt.start();
            try {
                dt.join();
            } catch (InterruptedException ex) {
                System.out.println("Izvođenje programa je prekinuto!");
            }
        }
    }

    /**
     * Klasa koja stvara dretvu za slanje upita
     */
    private static class DretvaTest extends Thread {

        String adresa;
        int port;
        String komanda;
        String odgovor;

        public DretvaTest(String adresa, int port, String komanda) {
            this.adresa = adresa;
            this.port = port;
            this.komanda = komanda;
        }

        @Override
        public void run() {
            odgovor = izvrsiKomandu(this.komanda);
        }

        /**
         * Slanje upita na odgovarajući server
         * Primanje i ispisivanje odgovora na upit
         * @param komanda upit koji se šalje nekom serveru
         * @return odgovor koji je vratio server
         */
        private String izvrsiKomandu(String komanda) {
            try ( Socket uticnica = new Socket(adresa, port);  
                    InputStream is = uticnica.getInputStream();  
                    OutputStream os = uticnica.getOutputStream();  
                    OutputStreamWriter osw = new OutputStreamWriter(os)) {

                osw.write(komanda);
                osw.flush();
                uticnica.shutdownOutput();

                StringBuilder tekst = new StringBuilder();

                while (true) {
                    int i = is.read();
                    if (i == -1) {
                        break;
                    }
                    tekst.append((char) i);
                }
                uticnica.shutdownInput();
                System.out.println("ODGOVOR: '" + tekst.toString() + "'");
                uticnica.close();
                return tekst.toString();
            } catch (IOException ex) {
                if(!"AUTH".equals(komanda.split(" ")[0])){
                    System.out.println("ERROR 40: Nije moguće doći do servera podataka");
                    return "ERROR 40: Nije moguće doći do servera podataka";
                }
                else{
                    System.out.println("ERROR: Nije moguće doći do servera");
                    return "ERROR: Nije moguće doći do servera";
                }
            }
        }

    }

}
