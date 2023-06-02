/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carRace.threads;

import carRace.agent.Supervisor;
import java.awt.Point;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import telemedicina.NotifyMessage;

/**
 *
 * @author barbaradraghi
 */
public class SendCarPosition extends Thread {

    static long previousTime;
    protected long thresholdTime;
    Supervisor sup;
    boolean interrupt;
    private long timeSleepInterrupt;
    
    public static void setPreviousTime(long time) {
        previousTime = time;
    }

    public SendCarPosition(Supervisor s) {
        thresholdTime = 3000;
        timeSleepInterrupt = 1000;
        sup = s;
        interrupt = false;
    }

    /**
     * Quando il thread si sveglia va a prendere la posizione della macchina
     */
    
   @Override
    public void run(){

        while(true){
            if(interrupt){
                //System.out.println("SendCarPosition sono interrotto");
                try {
                    SendCarPosition.sleep(timeSleepInterrupt);
                } catch (InterruptedException ex) {
                    //System.out.println("Sono stato svegliato");
                }
                previousTime += timeSleepInterrupt;
            }else{
                if( (System.currentTimeMillis() - previousTime) < thresholdTime)
                {
                    try {
                        SendCarPosition.sleep(thresholdTime - (System.currentTimeMillis() - previousTime));
                    } catch (InterruptedException ex) {
                        //System.out.println("Sono stato svegliato");
                    }
                    sendActivity();
                    previousTime += thresholdTime;
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
    
    private void sendActivity(){
        Set<String> keySet = sup.getMyCars().keySet();
        Iterator<String> it = keySet.iterator();
        
        while(it.hasNext()){
            String key = it.next();
            sup.sendCarActivity(new NotifyMessage(), sup.doPayload(sup.getMyCars().get(key)) ,key);
        }
    }
    
    
}
