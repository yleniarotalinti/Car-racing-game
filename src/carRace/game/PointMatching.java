/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carRace.game;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author rachi
 */

/*
    Questa classe mi rappresenta i paletti corrispondenti della pista, cioè
    il paletto sopra e il paletto sotto.
*/
public class PointMatching {
  

    private Point2D.Double pin;
    private Point2D.Double pout;

    public PointMatching(Point2D.Double pin, Point2D.Double pout) {
        this.pin = pin;
        this.pout = pout;
    }

    @Override
    public String toString() {
        return "Punto 1 " + pin.x + " " + pin.y + " Punto 2 " + pout.x + " " + pout.y + "\n";
    }

    public double slope() {
        return (pin.y - pout.y) / (pin.x - pout.x);
    }

    public ArrayList betweenPoint() {
        ArrayList<Point2D.Double> list = new ArrayList();
        double y;
        double x;
        double angle = Math.atan(slope());
        double distanzaPunti = 0.5;
        int numeroPunti = (int) (pin.distance(pout) / distanzaPunti);
        double incrementox = distanzaPunti * Math.abs(Math.cos(angle));
        double incrementoy = distanzaPunti * Math.abs(Math.sin(angle));
        
        for (int i = 1; i <= numeroPunti; i++) {
            if (pin.y >= pout.y) {
                y = pin.y - i * incrementoy;
            } else {
                y = pin.y + i * incrementoy;
            }
            if (pin.x <= pout.x) {
                x = pin.x + i * incrementox;
            } else {
                x = pin.x - i * incrementox;
            }
            list.add(new Point2D.Double(x,y));
        }
        return list;
    }
    
    
    

    public Point2D.Double getPin() {
        return pin;
    }

    public Point2D.Double getPout() {
        return pout;
    }

    
}
