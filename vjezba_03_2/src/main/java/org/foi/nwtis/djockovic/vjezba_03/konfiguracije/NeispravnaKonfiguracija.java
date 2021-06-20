/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.djockovic.vjezba_03.konfiguracije;

/**
 *
 * @author NWTiS_1
 */
public class NeispravnaKonfiguracija extends Exception {

    /**
     * Creates a new instance of <code>NeispravnaKonfiguracija</code> without
     * detail message.
     */
    public NeispravnaKonfiguracija() {
    }

    /**
     * Constructs an instance of <code>NeispravnaKonfiguracija</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NeispravnaKonfiguracija(String msg) {
        super(msg);
    }
}
