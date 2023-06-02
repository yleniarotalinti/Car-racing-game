package carRace.game;

import carRace.carPerception.CarPerception;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;

public class RaceTrack extends Observable {

    private ArrayList<PointMatching> pointList;
    private ArrayList<PointMatching> selectedList;
    private ArrayList<CarPerception> carsList;
    private int width;
    private int height;

    public RaceTrack() {

        pointList = new ArrayList<>();
        carsList = new ArrayList<>();
        selectedList = new ArrayList<>();
        readFile("RaceTrackEllissi.txt");
        calculateGuiDimension();
        selectPoint();
        //System.out.println(printSelectedList());
//        Point2D.Double po = new Point2D.Double(666.08,79.89);
//        Point2D.Double pi = new Point2D.Double(648.47,152.79);
//        PointMatching pm = new PointMatching(pi, po);
//        Point2D.Double pCar =(Point2D.Double) pm.betweenPoint().get(2);
//        System.out.println(checkPosition(pointList.get(0), allowedPointList.get(20)));          
//        Point2D.Double pCar = new Point2D.Double(80, 299.5);
//        System.out.println(checkPosition(pCar));
    }

    public void addObservers(Observer obs) {
        this.addObserver(obs);
    }

    /*
        Metodo che legge il file che mi permette di costruire la pista
     */
    public void readFile(String f) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String line;
            while ((line = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, "\t");
                while (st.hasMoreTokens()) {
                    double x1 = Double.parseDouble(st.nextToken());
                    double y1 = Double.parseDouble(st.nextToken());
                    double x2 = Double.parseDouble(st.nextToken());
                    double y2 = Double.parseDouble(st.nextToken());
                    Point2D.Double p1 = new Point2D.Double(x1, y1);
                    Point2D.Double p2 = new Point2D.Double(x2, y2);

                    PointMatching pm = new PointMatching(p1, p2);
                    pointList.add(pm);
                }

            }
            in.close();

        } catch (FileNotFoundException ex) {
            System.out.println("File not found");;
        } catch (IOException ex) {
            System.out.println("Error File");
        }
    }
    
    /*
        Vado a selenzionare solo i paletti distanziati di 5 e li salvo nella 
        lista selectedPoint; l'intera lista dei paletti verrà utilizzata solo
        per disegnare al meglio la pista.
        
    */
    public void selectPoint() {
        double distanzaPunti = 40;
        int i = 0;
        int j = 1;
        selectedList.add(pointList.get(0));

        while (j < pointList.size()) {
            if (pointList.get(i).getPout().distance(pointList.get(j).getPout()) < distanzaPunti) {
                j++;
            } else {
                selectedList.add(pointList.get(j));
                i = j;
                j++;
            }

        }
    }

    public ArrayList<PointMatching> getSelectedList() {
        return selectedList;
    }
    
    

    public void calculateGuiDimension() {
        double x;
        double y;
        ArrayList listX = new ArrayList<>();
        ArrayList listY = new ArrayList<>();
        for (PointMatching pm : pointList) {
            x = pm.getPin().x >= pm.getPout().x ? pm.getPin().x : pm.getPout().x;
            y = pm.getPin().y >= pm.getPout().y ? pm.getPin().y : pm.getPout().y;
            listX.add(x);
            listY.add(y);
        }
        x = 0;
        y = 0;
        for (int i = 0; i < listX.size(); i++) {
            if ((double) listX.get(i) > x) {
                x = (double) listX.get(i);
            }
            if ((double) listY.get(i) > y) {
                y = (double) listY.get(i);
            }
        }
        width = (int) x + 50;
        height = (int) y + 75;

    }

    public String printPointList() {
        String line = "";
        for (PointMatching p : pointList) {
            line += p.toString() + "\n";
        }
        return line;
    }

    public String printSelectedList() {
        String line = "";
        for(PointMatching p : selectedList) {
            line += p.toString() + "\n";
        }
        return line;
    }
    
    /*
        Metodo che dati i paletti mi permette di costruire un poligono formato da
        essi; tale metodo mi sarà utile in altri punti del codice
     */
    public Polygon createPolygon(ArrayList<PointMatching> p) {
        int dimensione = p.size() * 2;
        int[] x = new int[dimensione];
        int[] y = new int[dimensione];

        int j = 0;
        while (j < p.size()) {
            x[j] = (int) p.get(j).getPin().x;
            y[j] = (int) p.get(j).getPin().y;
            j++;
        }
        int i = 0;
        while (i < p.size()) {
            x[j] = (int) p.get(p.size() - 1 - i).getPout().x;
            y[j] = (int) p.get(p.size() - 1 - i).getPout().y;
            i++;
            j++;
        }

        Polygon polygon = new Polygon(x, y, dimensione);
        return polygon;
    }

    /*
        Disegno la board
     */
    public void drawBoard(Graphics g) {
        g.setColor(Color.GREEN.darker());
        g.fillRect(0, 0, width, height); //Coloro di verde tutta la mia interfaccia

        Polygon polygon = createPolygon(pointList);
        g.setColor(Color.LIGHT_GRAY);
       
        g.fillPolygon(polygon);
        
         g.setColor(Color.BLACK);
         
        for(int i=0 ; i <selectedList.size(); i++)
        {
            
            g.fillOval((int)selectedList.get(i).getPin().x, (int)selectedList.get(i).getPin().y, 2 , 2);
            g.fillOval((int)selectedList.get(i).getPout().x, (int)selectedList.get(i).getPout().y, 2 , 2);
            
        }
        
        drawStartingLine(g);

        //Disegno il contorno della pista
        //g.setColor(Color.BLACK);
        //g.drawPolygon(polygon);
        drawCars(g);
    }

    /**
     *
     */
    public void drawStartingLine(Graphics g) {
        g.setColor(Color.white);
        ArrayList<PointMatching> points = new ArrayList();
        for (int i = 25; i < 50; i++) {
            points.add(pointList.get(i));
        }

        Polygon polygon = createPolygon(points);
        g.fillPolygon(polygon);
    }

    /*
        Disegno le macchine
     */
    public void drawCars(Graphics g) {
        g.setColor(Color.RED);  
        for (CarPerception p : carsList) {
            double angle = p.getLastAngle();
            int xCar = (int) p.getLastPosition().x;
            int yCar = (int) p.getLastPosition().y;
            Graphics2D g2D = (Graphics2D) g;
            Rectangle rect = new Rectangle(xCar, yCar, p.getCarWidth(), p.getCarHeight());

//            if (angle != 0) {
//                g2D.rotate(angle, xCar, yCar);
//            } else {
//                if (p.getLastVelocity().y == 0) {
//                    g2D.rotate(Math.PI / 2, xCar, yCar);
//                }
//            }
            //g2D.fillOval(xCar, yCar, 5, 5);
            g2D.fill(rect);

        }
    }

    /*
        Restituisce i paletti tra cui si trova la macchina */
    public PointMatching checkPosition(Point2D.Double carPosition) {
        double minDistance = 1000;
        PointMatching point = null;

        for (PointMatching pm : selectedList) {
            double b = pm.getPin().distance(carPosition);
            double c = pm.getPout().distance(carPosition);
            double d = b + c;
            if (minDistance > d) {
                point = pm;
                minDistance = d;
            }
        }
        return point;

//        PRIMA VERSIONE CON ALLOWED POINT
//        Line2D.Double line = new Line2D.Double(p.getPin(), p.getPout());
//        if (line.ptLineDist(carPosition) < 0.001) {
//
//            return true;
//        }
//        return false;
//
//        SECONDA VERSIONE CON ALLOWED POINT
//        double a = p.getPin().distance(p.getPout());
//        double b = p.getPin().distance(carPosition);
//        double c = p.getPout().distance(carPosition);
//        if ((a - 0.0001) < (b + c) && (b + c) < (a + 0.0001)) {
//            return true;
//        }
//        return false;
    }

    public ArrayList<PointMatching> getPointList() {
        return pointList;
    }

    public synchronized ArrayList<CarPerception> getCarsList() {
        return carsList;
    }

    public void addCar(CarPerception car) {
        carsList.add(car);
    }

    public void drawBoard() {
        this.setChanged();
        this.notifyObservers();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
