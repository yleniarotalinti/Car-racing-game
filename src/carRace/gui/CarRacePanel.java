package carRace.gui;

import carRace.game.RaceTrack;
import java.awt.Graphics;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JPanel;

public class CarRacePanel extends JPanel implements Observer {
    private RaceTrack racetrack;
   
    @Override
    public void update(Observable o, Object o1) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                repaint();
            }
        });

//        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        racetrack.drawBoard(g);
    }

    public CarRacePanel() {
        racetrack = new RaceTrack();
        repaint();
    }

    public RaceTrack getRacetrack() {
        return racetrack;
    }
    
}
