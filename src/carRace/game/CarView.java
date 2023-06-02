package carRace.game;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/*
    Classe che rappresenta ciò che vede la macchina
 */
public class CarView {

    //carsList sono i punti tra i due paletti?
    private ArrayList<Point2D.Double> carsList;
    private ArrayList<PointMatching> mapList;
    private ArrayList<Point2D.Double> allowedPointList;

    public CarView() {
        carsList = new ArrayList<>();
        mapList = new ArrayList<>();
        allowedPointList = new ArrayList<>();
    }
//    
//    public void addCarsList(Point p) {
//        carsList.add(p);
//    }
//    
//    public void EmptyCarsList() {
//        carsList.clear();
//    }

    
    
    public ArrayList<Point2D.Double> getAllowedPointList() {
        return allowedPointList;
    }

//    public boolean checkPosition(PointMatching p, Point2D.Double carPosition) {
//        ArrayList<Point2D.Double> list = p.betweenPoint();
//        Line2D.Double line = new Line2D.Double(p.getPin(), p.getPout());
//        if (line.ptLineDist(list.get(0)) < 0.001) {
//            return true;
//        }
//        return false;
//    }

    /**
     *
     * @param pointList
     *
     * Richiama il metodo definito in PointMatching che data una coppia di
     * paletti calcola i punti permessi tra di essi
     */
    public void setCarView(ArrayList<PointMatching> pointList) {
        svuotamapList();
        for (PointMatching pm : pointList) {
            mapList.add(pm);
            allowedPointList.addAll(pm.betweenPoint());
        }
    }

    public void svuotamapList(){
        mapList.clear();
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

    public ArrayList<PointMatching> getMapList() {
        return mapList;
    }

    
    
}
