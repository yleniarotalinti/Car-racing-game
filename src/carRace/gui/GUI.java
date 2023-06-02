package carRace.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.EventObject;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JButton;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GUI extends JFrame {

    private CarRacePanel panel;
    private JButton startButton;
    private JButton resumeButton ; 
    private GuiControls guiObservable;
    
    private class GuiControls extends Observable{
        private void startGame(){
            this.setChanged();
            notifyObservers( new StartEvent(this));
        }   
        private void endGame(){
            this.setChanged();
            notifyObservers( new EndEvent(this));
        }
        private void resumeGame(){
            this.setChanged();
            notifyObservers( new ResumeEvent(this));
        }
    }
    
    public void addObservers( Observer obj ){
        guiObservable.addObserver(obj);
    }
    
    public static class StartEvent  extends EventObject {
        StartEvent(Object me) {
          super(me);
        }
  }
    public static class EndEvent extends EventObject {
        EndEvent(Object me){
            super(me);
        }
    }
    
    public static class ResumeEvent extends EventObject {
        ResumeEvent(Object me){
            super(me);
        }
    }

    public GUI() {
        guiObservable = new GuiControls();
        setResizable(false);
        setTitle("CAR RACE");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initComponents();
        setSize(panel.getRacetrack().getWidth(), panel.getRacetrack().getHeight());
        setLocationRelativeTo(null);
    }

    public void initComponents() {
        panel = new CarRacePanel();
        JPanel controls = new JPanel(new FlowLayout());
        JPanel info = new JPanel(new GridLayout(1,2));
        JLabel giro = new JLabel("GIRO NUMERO: ");
        JLabel posizione = new JLabel("POSIZIONE: ");
        giro.setHorizontalAlignment(JLabel.CENTER );
        posizione.setHorizontalAlignment(JLabel.CENTER);
        startButton = new JButton("Start");
        resumeButton = new JButton("Resume");
        controls.add(startButton);
        controls.add(resumeButton);
        info.add(giro);
        info.add(posizione);
        add(info, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
        add (controls, BorderLayout.SOUTH);
        
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        
        resumeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resumeButtonActionPerformed(evt);
            }
        });
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt){
                shutDownSystem(evt);
            }
        });
    }

    private void shutDownSystem(java.awt.event.WindowEvent evt){
        guiObservable.endGame();
    }
    private void startButtonActionPerformed(ActionEvent evt) {
      guiObservable.startGame();
    }
    
    private void resumeButtonActionPerformed(ActionEvent evt) {
      guiObservable.resumeGame();
    }

    public CarRacePanel getPanel() {
        return panel;
    }

   
    
}