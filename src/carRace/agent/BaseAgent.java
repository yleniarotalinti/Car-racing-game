package carRace.agent;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import telemedicina.*;

/**
 *
 * @author Cri
 */
public abstract class BaseAgent implements Runnable{
    protected Connection conn;
    protected boolean debug;
//    protected String idAgent;
    
    //==================================================
    /* Le Activity eseguite dagli Agenti. Vengono lette dal file
     *  di properties per disaccoppiarle in ogni singola installazione.
     */
    protected String SUP_ACCELERATION;
    protected String SUP_BRAKING;
    protected String SUP_STEERING;
    protected String SUP_GO_ON;
    protected String SUP_CAR_BIRTH;
    protected String SUP_NOTIFY_INITIAL_VIEW;

    protected String CAR_UPDATE_POSITION_VIEW;
    protected String CAR_INITIAL_POSITION_VIEW;
    protected String CAR_START_RACE;
    protected String CAR_END_GAME;
    
    
    public BaseAgent( Connection conn, boolean debug) {
        super();
        this.conn = conn;
        this.debug = debug;
//        this.idAgent = idAgent;
        loadActivity();
        
    }
    
   
    /*
    Carica le activity dal file di property e le controlla
    */
    private void loadActivity(){
        SUP_ACCELERATION = conn.getProperty("SUP_activity1");
        SUP_BRAKING = conn.getProperty("SUP_activity2");
        SUP_STEERING = conn.getProperty("SUP_activity3");
        SUP_GO_ON = conn.getProperty("SUP_activity4");
        SUP_CAR_BIRTH = conn.getProperty("SUP_activity5");
        SUP_NOTIFY_INITIAL_VIEW = conn.getProperty("SUP_activity6");

        CAR_UPDATE_POSITION_VIEW = conn.getProperty("CAR_activity1");
        CAR_INITIAL_POSITION_VIEW = conn.getProperty("CAR_activity2");
        CAR_START_RACE = conn.getProperty("CAR_activity3");
        CAR_END_GAME = conn.getProperty("CAR_activity4");
        
        checkActivity();
    }

    private void checkActivity(){
        
        if(SUP_ACCELERATION == null ||
            SUP_BRAKING == null ||
            SUP_STEERING == null ||
            SUP_GO_ON == null ||
            CAR_UPDATE_POSITION_VIEW == null || 
            SUP_NOTIFY_INITIAL_VIEW ==  null ||
            SUP_CAR_BIRTH == null ||
            CAR_INITIAL_POSITION_VIEW == null ||
            CAR_START_RACE == null ||
            CAR_END_GAME == null
            )
          quitVM("Una delle Activity richieste non e' definita " +
                 "nel file di property");
        
    }

    public boolean isDebug() {
        return debug;
    }

    public String getLogin() {
        return conn.getLogin();
    }
    
    //========================================
    /** Mostra in output un messaggio informativo.
     *
     * Per distinguere fra messaggi sempre presenti e messaggi stampati solo
     *  quando l'Agente opera in modalita' debug viene usato un apposito flag.
     *
     *  Un altro flag consente di indicare che si opera in modo asincrono nel caso
     *  in cui la stampa del messaggio viene richiesta da un gestore di Activity
     *  anziche' da un gestore delle routine di interfaccia. Cio' e' utile per
     *  evitare che il messaggio che viene inviato si confonda con il prompt.
     *
     * @param debugp     Flag che indica se il messaggio e' di debug o meno.
     * @param redisplayp Flag usato dagli Agenti di Interfaccia quando la stampa
     *                   avviene in modo asincrono (Activity INFO o altre).
     * @param message    Il messaggio da mostrare.
     */
    protected void showMsg (String message, boolean debugp, boolean redisplayp) {
      if (debugp && !debug) return;

        if (redisplayp) { // Si mandano 2 newline per lasciare una riga vuota.
            System.out.println();
            System.out.println();
        }
        if (debugp) System.out.println("=====<agent-debug>=====");
        else        System.out.println("=====<agent-info>=====");
        System.out.println(message);
        if (debugp) System.out.println("=====</agent-debug>=====");
        else        System.out.println("====</agent-info>======");
    }
    //========================================
    /**  Mostra un messaggio informativo. La versione senza alcun flag assume
     *   che debug sia false e redisplayp sia true.
     *
     * @param message    Il messaggio da mostrare.
     */
    protected void showMsg (String message) {
        showMsg(message, false, true);
    }
    //========================================
    /**  Mostra un messaggio informativo. La versione con solo flag di debug assume
     *   che redisplayp sia true.
     *
     * @param debugp     Flag che indica se il messaggio e' di debug o meno.
     * @param message    Il messaggio da mostrare.
     */
    protected void showMsg (String message, boolean debugp) {
        showMsg(message, debugp, true);
    }
    //========================================
    /** Controlla lo Status della transazione. Se Status Code risulta essere KO
     *  il metodo mostra SEMPRE gli attributi "message" ed "exception" e quindi
     *  forza una uscita immediata dalla VM. Se invece lo Status Code non e' KO
     *  il metodo non mostra nulla a meno che l'agente non sia stato attivato in
     *  modalita' debug. Un flag apposito permette di indicare nella chiamata al
     *  metodo se mostrare anche la stringa XML ricevuta.
     *
     * @param msg     AbstractMessage che e' stato inviato.
     * @param showxml Flag che richiede di mostrare l'intera reply in XML.
     */
    protected void checkStatus (AbstractMessage msg, boolean showxml) {
        XMLInterface status = msg.getStatus();
        String code = status.getProperty("code");

        // XML Reply (when requested) is always output as a debug message.
        if (showxml) 
          showMsg("XML Reply ricevuta dal Server:\n" + msg.getXMLReply(),
                  true);
          
        // Reply status is always output as a debug message.
        showMsg("Lo Status della Reply per " + msg.getClass().getName() + " e': " + code,
                true);

        // KO is output as an informational message so that it cannot be blocked
        if ("KO".equals(code)) {
          quitVM("... abbiamo dei problemi !! \n" +
                  "Message: "   + status.getProperty("message") + "\n" +
                  "Exception: " + status.getProperty("exception") + "\n");
        }
    }
    //========================================
    /** Mostra un messaggio ed esce dalla VM.
     *
     * @param message Il messaggio da mostrare.
     */
    private void quitVM (String message) {
        System.out.println();
        System.out.println("*****<alert>*****");
        System.out.println("Exiting VM");
        System.out.println(message);
        System.out.println("*****</alert>*****");
        System.exit(0);
    }
    
    //========================================
    /** Effettua la (de)registrazione recuperando le informazioni sulle Activities
     *  che rappresentano le Capabilities dell'Agente dalle properties della
     *  connessione, una volta che queste siano state lette dal relativo file.
     *
     *  Le Activities vengono ricercate in funzione del particolare ident che
     *  definisce la personalita' dell'Agente. In tal modo, e' possibile variare
     *  le Activities per cui l'Agente si registra semplicemente variando
     *  gli argomenti sulla riga di comando, ottenendo una maggior flessibilita'
     *  per l'esempio.
     *
     * @param ident Serve per recuperare le Activity con cui registrarsi variandole
     *              in funzione di un determinato prefisso. La ricerca avviene su
     *              ident_activity[xx] dove xx va da 1 a 10.
     */
     public void register(String ident) {
        SubscribeMessage submsg = new SubscribeMessage();
        int numActivity = 10;
        if (ident != null) {
            submsg.addActivity("startup");
            //Sto registrando le capabilities
            for( int i = 1; i < numActivity; i++){
                String activity = conn.getProperty( ident+"_activity"+i );
                if(activity != null){
                    submsg.addActivity(activity);
                    System.out.println(ident + " mi sono registrato a:  " + activity);
                }else{
                    break;
                }
            }
        }
        conn.send(submsg);
         System.out.println("Sono in register");   
        checkStatus(submsg, true);
    }
    //========================================
    /**
     * Richiede una nuova Activity al Server, che viene restituita come
     * risultato.
     *
     * @return La XMLInterface che rappresenta il descrittore per la Next
     * Activity.
     */
    protected XMLInterface nextActivity() {
        //====================
        // Il messaggio e' inviato in Blocking mode. Questo consente una
        // automatica re-trasmissione dello stesso messaggio se la Reply ha uno
        // status code == RETRY.
      
        EngageMessage engmsg = new EngageMessage();
        conn.send(engmsg, true);
        checkStatus(engmsg, true);
        return engmsg.getTask();
    }
    
    
    
    
    
}

