package carRace.agent;
 
import carRace.game.CarView;
import carRace.game.PointMatching;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.vecmath.Vector2d;
import telemedicina.Connection;
import telemedicina.NotifyMessage;
import telemedicina.XMLInterface;


public class Car extends MoveAgent {

    private Point2D.Double position;
    private Vector2d velocity;
    private final Vector2d maxVelocity;
    private CarView carView;
    private int carHeight= 25;
    private PointMatching boundaryPoints;
    
    
    public Car(Connection conn, boolean debugagent) {
        super(conn, debugagent);
        carHeight= 25;
        maxVelocity=new Vector2d(carHeight/10,carHeight/10);
        this.carView = new CarView();
    }

    /**
     * Il metodo run gestisce le activity in arrivo alla classe Car.
     *
     * -STARTUP: quando il system invia l'activity di startup, l'agente Car
     * risponde con un Notify e inviando al supervisor l'activity di nascita (SUP_CAR_BIRTH); 
     * -CAR_INITIAL_POSITION_VIEW: l'agente Car riceve dal
     * supervisor dopo la nascita un'activity con la sua posizione iniziale,
     * velocità iniziale e primo raggio di visione (paletti); 
     * -CAR_START_RACE: una volta premuto il tasto Start, l'agente riceve questa activity a cui
     * risponde inviando al supervisor la prossima azione che ha deciso di
     * compiere (accelerazione, frenatura, ecc..); 
     * -CAR_UPDATE_POSITION_VIEW: questa activity viene inviata dal Thread del Supervisor ogni 3 secondi
     * circa per aggiornare l'agente Car della sua nuova posizione e velocità
     * raggiunta, in base alla sua ultima richiesta (ad esempio di
     * accelerazione). In questo modo può aggiornare il nuovo raggio di visione
     * e decidere la prossima azione da compiere. 
     * -CAR_END_GAME: quando viene premuto il tasto di chiusura della GUI, il supervisor manda questa
     * activity all'agente Car, così che possa deregistrarsi ed il thread
     * terminare.
     *
     */
    
    @Override
    public void run() {
        for (;;) {
            System.out.println("Sto aspettando activity");
            XMLInterface task = nextActivity();
            showMsg("CAR: "
                    + " Activity: " + task.getProperty("Activity")
                    + " richiesta da: " + task.getProperty("Scheduler"));
            System.out.println(task.getProperty("Payload"));

            if ("startup".equals(task.getProperty("Activity"))) {
                sendRequest(task, SUP_CAR_BIRTH, "");

            } else if (CAR_INITIAL_POSITION_VIEW.equals(task.getProperty("Activity"))) {
                doInitialPosition(task);

            } else if (CAR_START_RACE.equals(task.getProperty("Activity"))) {
                selectActionToDo(task);
            } else if (CAR_UPDATE_POSITION_VIEW.equals(task.getProperty("Activity"))) {
                doUpdatePosition(task);

            } else if (CAR_END_GAME.equals(task.getProperty("Activity"))) {
                doDeath(task);
            } else {
                showMsg(this.getClass().getName() + ": "
                        + "Incapace di eseguire la Activity: "
                        + task.getProperty("Activity"));
            }
        }
    }

    private void doInitialPosition(XMLInterface task) {
        sendRequest(task, SUP_NOTIFY_INITIAL_VIEW, null);
        updatePositionView(task);
    }

    private void doUpdatePosition(XMLInterface task) {
        updatePositionView(task);
        selectActionToDo(task);
    }

    private void sendOnlyNotify(XMLInterface task) {
        NotifyMessage nmsg = new NotifyMessage(task.getProperty("Activity"),
                task.getProperty("Serial"),
                "COMPLETED",
                null);
        conn.send(nmsg);
    }

    /**
     * DECISIONE PROSSIMA AZIONE DA COMPIERE
     *
     * @param
     *
     * Quando la classe Car riceve dal supervisor l'activity di inizio gara (CAR_START_RACE) 
     * oppure quando riceve il nuovo raggio di visione(CAR_UPDATE_POSITION_VIEW), 
     * richiama questo metodo per decidere quale sarà la sua prossima da compiere. 
     * -Calcola il punto target, ovvero quello a maggior distanza dalla sua posizione attuale. 
     * -Se la velocità attuale della macchina è minore di quella massima, manda un'activity al
     * supervisor (SUP_ACCELERATION), decidendo come grado di accelerazione un numero intero casuale tra 1 e 5. 
     * -Atrimenti manda un'activity (SUP_GO_ON) comunicando al supervisor che ha intenzione di proseguire ancora con la
     * stessa velocità.
     *
     */
    
    private void selectActionToDo(XMLInterface task) {

        //cerco eventuale angolo di inclinazione tra i paletti del raggio di visione
        double angle = searchSlopeAngle();
        System.out.println("----------------- ANGOLO =  " + angle);
        if (angle == 0 || angle == Math.PI) {
          doAcceleration(task);
        } 
        else {
            doSteering(task,angle);
        }

    }
    
    
    
    private double searchSlopeAngle() {

        Point2D.Double p = carView.getMapList().get(carView.getMapList().size()-1).getPout();     
        Point2D.Double boundaryP = boundaryPoints.getPout(); 

        System.err.println("Angolo" + getAngleOfLineBetweenTwoPoints(boundaryP,p));
        System.out.println("boundary  " + boundaryP + " " + p);
        return getAngleOfLineBetweenTwoPoints(boundaryP,p);
    }

    
    private  double getAngleOfLineBetweenTwoPoints(Point2D.Double p1, Point2D.Double p2)
    {
        double xDiff = p2.x - p1.x;
        double yDiff = p2.y - p1.y;
        
        if(xDiff == 0)
            return 0;
        
        return Math.atan2(yDiff, xDiff);
    }
    
    // controllo se la velocità a cui sto andando è pari alla massima
    private boolean checkVelocity() {
       return (( Math.abs(velocity.x) == Math.abs(this.maxVelocity.x) || Math.abs(velocity.y) == Math.abs(this.maxVelocity.y))
                || (Math.abs(velocity.x) == Math.abs(this.maxVelocity.x) && Math.abs(velocity.y) == Math.abs(this.maxVelocity.y)));

    }


    
    private void doAcceleration(XMLInterface task){
        //            Point2D.Double ptarget = getPointTarget();
       // System.out.println("CONDIZIONE: ");
        System.out.println((Math.abs(velocity.x) < Math.abs(maxVelocity.x)) && (Math.abs(velocity.y) < Math.abs(maxVelocity.y)));
            if ( (Math.abs(velocity.x) < Math.abs(maxVelocity.x)) && (Math.abs(velocity.y) < Math.abs(maxVelocity.y)) ) {
                double new_acceleration = calculateAcceleration();
                System.out.println(" Accelerazione " + new_acceleration);
                String payload = "" + new_acceleration;
                sendRequest(task, SUP_ACCELERATION, payload);
            } else {
                sendRequest(task, SUP_GO_ON, "");
            }

    }
    
    /**
     * Di quanto deve essere l'accelerazione?
     * Se:
     * - la velocità della macchina è minore di 1/4 vmax accelero di 1
     * - la velocità della macchina è minore di 1/3 vmax accelero di 2
     * - la velocità della macchina è minore di 1/2 vmax accelero di 3
     * - la velocità della macchina  minore di vmax ma maggiore di v/2, accelero di 4
     * Questi valori interi verranno poi letti e tradotti dal supervisor.
     * @return 
     */
    
    
    private int calculateAcceleration() {
        if ((Math.abs(velocity.x) < Math.abs(maxVelocity.x)) && (Math.abs(velocity.y) < Math.abs(maxVelocity.y))) {

            if ((Math.abs(velocity.x) < Math.abs(maxVelocity.x) / 3) && (Math.abs(velocity.y) < Math.abs(maxVelocity.y) / 3)
                    && (Math.abs(velocity.x) > Math.abs(maxVelocity.x) / 4) && (Math.abs(velocity.y) > Math.abs(maxVelocity.y) / 4)) {
                return 3;

            }

            if ((Math.abs(velocity.x) < Math.abs(maxVelocity.x) / 4) && (Math.abs(velocity.y) < Math.abs(maxVelocity.y) / 4)) {
                return 4;
            }

            if ((Math.abs(velocity.x) < Math.abs(maxVelocity.x) / 2) && (Math.abs(velocity.y) < Math.abs(maxVelocity.y) / 2)
                    && (Math.abs(velocity.x) > Math.abs(maxVelocity.x) / 3) && (Math.abs(velocity.y) > Math.abs(maxVelocity.y) / 3)) {
                return 2;

            } else {
                return 1;
            }
        }

        return 0;
    }
    
    
    
    private int calculateBraking() {

        if (((Math.abs(velocity.x) > Math.abs(maxVelocity.x) / 2) && (Math.abs(velocity.y) > Math.abs(maxVelocity.y) / 2))
                || ((Math.abs(velocity.x) > Math.abs(maxVelocity.x) / 2) || (Math.abs(velocity.y) > Math.abs(maxVelocity.y) / 2))) {
            return 2;

        }

//        if (((Math.abs(velocity.x) > Math.abs(maxVelocity.x) / 3) && (Math.abs(velocity.y) > Math.abs(maxVelocity.y) / 3))
//                || ((Math.abs(velocity.x) > Math.abs(maxVelocity.x) / 3) || (Math.abs(velocity.y) > Math.abs(maxVelocity.y) / 3))) {
//            return 3;
//
//        }
//
//        if (((Math.abs(velocity.x) > Math.abs(maxVelocity.x) / 4) && (Math.abs(velocity.y) > Math.abs(maxVelocity.y) / 4))
//                || ((Math.abs(velocity.x) > Math.abs(maxVelocity.x) / 4) || (Math.abs(velocity.y) > Math.abs(maxVelocity.y) / 4))) {
//            return 4;
//
//        }

        return 1;

    }
    
    
    
    
    private void doSteering(XMLInterface task, double angle) {
        System.out.println("Check Velocity" + checkVelocity());
        if (checkVelocity()) {
            int braking = calculateBraking();
            System.out.println(" Braking " + braking);
            
            sendRequest(task, SUP_STEERING, doPayload(angle, braking));
        } else {
            sendRequest(task, SUP_STEERING, doPayload(angle, 0));
        }

    }
    
    
    private String doPayload(double angle, int braking){
        
        String slopeAngle = angle + ";" ;
        String brak = braking + ";";
    
        return slopeAngle + brak;
    }
    
    
    
    
    /**
     * CREAZIONE DEL PUNTO TARGET
     *
     * @return
     *
     * Questo metodo itera su tutti i punti disponibili nel raggio di visione
     * della Car, restituendo quello a massima distanza dalla posizione attuale
     * della macchina.
     *
     */
    private Point2D.Double getPointTarget() {

        ArrayList<Point2D.Double> carsList = carView.getAllowedPointList();
        Iterator<Point2D.Double> it = carsList.iterator();
        Point2D.Double ptarget = new Point2D.Double();
        double distance = 0;

        while (it.hasNext()) {
            Point2D.Double point = it.next();
            if (distance < position.distance(point)) {
                distance = position.distance(point);
                ptarget = point;
            }
        }

        return ptarget;
    }

    /**
     * AGGIORNAMENTO PARAMETRI E RAGGIO DI VISIONE
     *
     * @param  
     * Questo metodo viene richiamato dall'agente Car ogni volta che il
     * supervisor manda un'activity con la nuova posizione/velocità/raggio di
     * visione. 
     * -Per prima cosa si splitta il payload usando come delimitatore ;
     * recuperando le coordinate di tutti i punti ricevuti. 
     * -Il metodo setParameters() recupera i primi due tokens resituiti e li utizza per
     * aggioranare la posizione e velocità. 
     * -I restanti tokens riguardano le coordinate dei paletti, quindi si richiama setView() per creare i
     * PointMatching.
     */
    
    private void updatePositionView(XMLInterface task) {
        String payload = task.getProperty("Payload");
        String[] split = null;
        split = splitPayload(payload, ";", 12);
        setParameters(split);
        setView(split);
    }

    /**
     * ELABORAZIONE PAYLOAD DELL'ACITIVITY RICEVUTA DAL SUPERVISOR
     *
     * @param
     * 
     * Questo metodo serve all'agente Car, per elaborare il messaggio di payload
     * ricevuto dal supervisor. Restituisce un array di stringhe con i tokens
     * recuperati, specificando il deliminatore secondo cui splittare e la
     * dimensione dell'array stesso così da poterlo istanziare con precisione.
     * Infatti la dimensione è fissa sapendo quanti paletti vengono mandati
     * all'agente Car: 
     * -quando divido usando come delimitatore ";" è string[num_ paletti + 2] 
     * (oltre i paletti viene mandata anche la posizione e la velocità) 
     * -quando divido per " " è string[2] (coordinata x e coordinata y dei punti recuperati)
     */
    
    private String[] splitPayload(String payload, String delim, int dim) {

        String split[] = new String[dim];
        StringTokenizer st = new StringTokenizer(payload, delim);
        int i = 0;

        while (st.hasMoreTokens()) {
            split[i] = st.nextToken();
            i++;
        }
        return split;
    }
    
    

    private void setParameters(String[] split) {

       
        for (int i = 0; i < 2; i++) {
            String[] values = splitPayload(split[i], " ", 2);
            if (i == 0) {
                setPosition(Double.parseDouble(values[0]), Double.parseDouble(values[1]));
            } else {
                setVelocity(Double.parseDouble(values[0]), Double.parseDouble(values[1]));
            }
        }

    }

    
    
    public void setPosition(double x, double y) {
        Point2D.Double p = new Point2D.Double(x, y);
        this.position = p;
        System.out.println("Posizione: " + position.x + " " + position.y + "\n");
    }

    
    
    
    
    public void setVelocity(double x, double y) {
        //Vector2d v = new Vector2d(x, y);
        this.velocity = new Vector2d(x, y);;
        System.out.println("Velocità: " + velocity.x + " " + velocity.y + "\n");
    }
    
    

    /**
     * Questo metodo recupera le coordinate dei paletti e le salva in una lista
     * di punti.
     */
    
    
    private void setView(String[] split) {

        int i = 0;
        ArrayList<Point2D.Double> list_point = new ArrayList<>();

        for (i = 2; i < split.length; i++) {
            String[] values = splitPayload(split[i], " ", 2);
            
            list_point.add(new Point2D.Double(Double.parseDouble(values[0]), Double.parseDouble(values[1])));
        }

        updateView(list_point);

    }

    
    /**
     * Questo metodo cicla sulla lista di punti (paletti) restituita dl metodo
     * setView(), ovvero crea i PointMatching prendendo ad ogni giro un punto ed
     * il successivo Dopo aggiorna il raggio di visione della macchina
     * (carView).
     * @param points
     */
    
    
    public void updateView(ArrayList<Point2D.Double> points) {

        
        // VENGONO PASSATI COME Pout ; Pin quindi vanno invertiti 
        
        boundaryPoints = new PointMatching(points.get(0),points.get(1));
        
        int i;
        ArrayList<PointMatching> list_pointmatching = new ArrayList<>();

        for (i = 2; i < points.size(); i += 2) {
            PointMatching pm = new PointMatching(points.get(i), points.get(i + 1));
            list_pointmatching.add(pm);
        }

        carView.setCarView(list_pointmatching);
        System.out.println("Dimensione: " + carView.getMapList().size());
    }

    
    
    public void sendRequest(XMLInterface task, String activity, String text) {

        NotifyMessage nm = new NotifyMessage(task.getProperty("Activity"),
                task.getProperty("Serial"),
                "COMPLETED",
                null);
        nm.addNext(activity,
                text,
                "ANY");
        conn.send(nm);
        checkStatus(nm, true);

    }

    
    private void doDeath(XMLInterface task) {
        sendOnlyNotify(task);
        this.register(null);
        System.exit(0);
    }

}
