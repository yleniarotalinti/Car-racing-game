/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carRace.threads;

import carRace.agent.Supervisor;
import java.util.logging.Level;
import java.util.logging.Logger;
import telemedicina.NotifyMessage;

/**
 *
 * @author barbaradraghi
 */
public class RaceTrackUpdate extends Thread{

    static long previousTime;
    long timeSleep;
    long threshold;
    Supervisor sup;
    
    public static void setPreviousTime(long time) {
        previousTime = time;
    }
    
/**
 * Ho distinto il tempo in cui il thread dorme dalla soglia, perchè potremmo volerli diversi
 * @param s 
 */
    public RaceTrackUpdate(Supervisor s) {
        sup = s;
        timeSleep = 100;
        threshold = 100;
    }
    
    @Override
    public void run(){
         while(true){
            if( (System.currentTimeMillis() - previousTime) < threshold)
            {
                try {
                    RaceTrackUpdate.sleep(threshold - (System.currentTimeMillis() - previousTime));
                } catch (InterruptedException ex) {
                    Logger.getLogger(Supervisor.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
                
                sup.updateRaceTrack();
            }
           
            previousTime += threshold; 
        }
    }

}
