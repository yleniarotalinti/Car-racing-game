/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carRace.threads;

import carRace.agent.Supervisor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CalculateCarPosition.
 *
 * @author barbaradraghi 
 * 
 * E' uno dei thread che ha il supervisor. In particolare,
 * questo thread effettua un ciclo ogni 0.1 secondi, in cui per ogni
 * CarPerception (idealmente Car) contenuta in esso, calcola la nuova velocità e
 * la nuova posizione, applicando le leggi della fisica, in termini vettoriali.
 */

public class CalculateCarPosition extends Thread {

    Supervisor sup;
    static long previousTime;
    private final long thresholdTime;
    boolean interrupt;
    private long timeSleepInterrupt;

    public CalculateCarPosition(Supervisor sup) {
        this.sup = sup;
        thresholdTime = 100;
        timeSleepInterrupt = 100; 
    }

    static public void setPreviousTime(long time) {
        previousTime = time;
    }
    

    /**
     * Calcolo la posizione di ciascuna macchina ogni 0.1 secondi. Questo thread
     * ha il compito di calcolare ogni 0.1 secondi la nuova posizione di
     * ciascuna macchina. In questo modo, il supervisor potrà comunicare le
     * posizioni alla pista ogni 0.5 secondi, in modo da rendere più fluida e
     * realistica possibile la gara stessa. Quello che accade nel run di questo
     * thread è che - se non sono ancora trascorsi 0.1 secondi, il thread dorme
     * per il tempo rimanente per raggiungere 0.1. - se sono trascorsi 0.1
     * secondi il thread si sveglia, e richiama il metodo che applica le leggi
     * della fisica del Supervisor.
     */
    @Override
    public void run() {
        while (true) {
            //System.out.println("Interrupt: " + interrupt);
            if(interrupt){
                //System.out.println(" CalculateCarPosition sono interrotto");
                try {
                    CalculateCarPosition.sleep(timeSleepInterrupt);
                } catch (InterruptedException ex) {
                    //System.out.println("Sono stato svegliato");
                }
                previousTime += timeSleepInterrupt;
                
            }else{
               // System.out.println("Tempo corrente:" + System.currentTimeMillis() + "PreviousTime: " + previousTime);
                if ((System.currentTimeMillis() - previousTime) < thresholdTime) {

                    try {
                        CalculateCarPosition.sleep(thresholdTime - (System.currentTimeMillis() - previousTime));
                        
                    } catch (InterruptedException ex) {
                        //System.out.println("Sono stato svegliato e passo allo stato interrotto");
                    }
                    previousTime += thresholdTime;
                    sup.physicalLaws();
                }

                
           
            }

        }
    }
    public void interruptThread(){
        interrupt = true;
    }
    
    public void exitFromStateInterrupt(){
        interrupt = false;
    }

    public boolean isInterrupt() {
        return interrupt;
    }
    
}
