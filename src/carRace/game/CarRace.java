
package carRace.game;

import carRace.agent.BaseAgent;
import carRace.agent.Car;
import carRace.agent.Supervisor;
import carRace.gui.*;
import carRace.gui.GUI;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import telemedicina.Connection;


/**
 * Classe principale dell'applicazione da cui viene invocato il main().
 * Da qui è possibile lanciare i diversi agenti: per prima cosa è necessario invocare
 * il supervisor. Dopodichè possono essere invocate più auto.
 * @author barbaradraghi
 */



public class CarRace {
    static GUI gui;
    static CarRacePanel panel;
    
    public static void main(String[] args) {
  
        /** Flag di Debug per la Connessione.
         *  Se la connessione viene usata in modalita' debug manda fuori dei messaggi (in rosso)
         *  relativi a tutto cio' che passa sulla rete.
         */
        boolean debugconn   = false;

        /** Flag di Debug per il singolo Agente.
         *  Lo useremo dentro l'agente per decidere se mandare fuori messaggi base o per debug "avanzato"
         *     e differenziare i due modi.
         */
        boolean debugagent  = false;  

        
        // Blocco di controllo della stringa di argomento.
        try {
            if (args.length < 2 || args.length > 3){
            	//throw new Exception ();
                 System.err.println("L'AGENTE " + args[0] + " HA POCHI/TROPPI ARGOMENTI NEL CUSTOMIZE");
                 System.exit(0);
            }

            if (args.length >= 3) {
                debugagent = "agent".equalsIgnoreCase(args[2]) || "both".equalsIgnoreCase(args[2]);
                debugconn  = "conn".equalsIgnoreCase(args[2])  || "both".equalsIgnoreCase(args[2]);
            }
        }
        catch (Exception ex) {
            System.out.println("Uso: Main [id] [personality] [debug]");
            System.out.println("     id =:: [ id1 | id2 | id3 | id4 | id5 ]");
            System.out.println("     personality =:: [ SUP | CAR ]");
            System.out.println("     debug =:: [none | conn | agent | both ]");
            System.exit(0);
        }

        //======================================================================
        // Viene aperta la connessione indicando se deve agire in modalita' debug
        // ed utilizzando la Stringa di ID per la scelta di username/password.
        // Questo consente di renderle parametriche in funzione di quanto viene
        // passato sulla riga di comando, aumentando la flessibilita' dell' esempio.
        Connection conn = new Connection("sample.properties", args[0], debugconn);

        System.out.println("Inizio dell'Attivita' dell'Agente in qualita' di: " +
                            args[1] + 
                            " con utenza: " + 
                            conn.getLogin());

        // Crea l'istanza del gestore in funzione della particolare personalita' assunta.
        BaseAgent ba = null;
        
        
      
        if (null != args[1]) {
            switch (args[1]) {
                
                case "SUP":
                     Supervisor sup = new Supervisor(conn, debugagent);
                   try {
                            java.awt.EventQueue.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    gui = new GUI();
                                    gui.setVisible(true);
                                }
                            });
                            RaceTrack raceTrack = gui.getPanel().getRacetrack();
                            sup.addRaceTrack(raceTrack);
                            gui.addObservers(sup);
                            //GUI gui = new GUI();
                            //gui.setVisible(true);

                            java.awt.EventQueue.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {

                                    panel = gui.getPanel();
                                }

                            });
                            gui.addObservers(sup);
                            raceTrack.addObservers(panel);
                            ba = (BaseAgent) sup;

                        } catch (InterruptedException | InvocationTargetException ex) {
                            Logger.getLogger(CarRace.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    
                    break;
                    
                case "CAR":
                    ba = new Car(conn, debugagent);
                    break;
                    
                default:
                    System.err.println("Identità non riconosciuta");
                    
            }
            
        } 

          
        // Cerca innanzitutto di deregistrarsi, nel caso sia rimasta qualcosa
        //   nel database del server a seguito di una precedente registrazione
        //   e di un malfunzionamento dell'Agente che ha impedito di deregistrarsi
        //   prima di terminare il proprio ciclo di vita.
        ba.register(null);

        // Questa invece invoca la registrazione dell'agente in funzione della
        //   personalita'. L'agente registrera' tutte le activities che corrispondono
        //   all'ID che viene passato come argomento (guardarsi il codice).
        ba.register(args[1]);

        // Lancia l'esecuzione degli Handler delle Activity in background ...
        new Thread(ba).start();
        
        // ... ed esegue quella di interfaccia in foreground se il metodo e' stato
        //    overridden in una sottoclasse. Altrimenti sfrutta il metodo
        //     di default presente nella superclasse.
        //ba.interfaceMenu();

        // Quando l'interfaccia termina, si deregistra l'agente e quindi esce.
//        ba.register(null);
//        System.out.println("Sessione Terminata");
//        System.exit(0);
    
    }
    
//    public void initialize() {
//    }
 }

