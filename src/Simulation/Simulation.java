package Simulation;

import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.WindowConstants;
import java.awt.Graphics;
import java.awt.Dimension;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.awt.Rectangle;


public class Simulation {
    
    public static void main(String[] args) {
        Simulation app = new Simulation();
        Renderer frame = app.createWindow(500, 550);
        frame.loop();
    }
    
    //Create the JFrame window
    public Renderer createWindow(int sizex, int sizey){
        JFrame window = new JFrame("Cloth Simulation");
        window.setSize(sizex, sizey);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        //Creates a renderer object and passes it to the main function
        Renderer r = new Renderer();
        window.add(r);
        window.pack();
        window.show();
        return r;
    }
}

class Renderer extends JPanel implements MouseListener, MouseMotionListener{
    Cloth theCloth;
    
    Renderer(){
        super();
        theCloth = new Cloth();
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    public void loop() {
        long lastLoopTime = System.nanoTime();
        final int TARGET_FPS = 60;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;   

        while (true) {
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;
            lastLoopTime = now;
            double dT = updateLength / ((double)OPTIMAL_TIME);
            
            theCloth.update(dT);
            this.repaint();
            
            
            try{Thread.sleep( (lastLoopTime-System.nanoTime() + OPTIMAL_TIME)/1000000 );}
            catch(Exception e){};
        }
    }
    
    
    //Render the graphics
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        
        //Draw only the lines
        for (Constraint line : theCloth.lines){
            g.drawLine((int)line.stP.cPosX, (int)line.stP.cPosY, (int)line.enP.cPosX, (int)line.enP.cPosY);
        }
    } 
    
    //Mouse events handler
    Particle drag = null;
    
    @Override
    public void mousePressed(MouseEvent e){
        int x = e.getX();
        int y = e.getY();
        Rectangle select = new Rectangle(x - 5, y - 5, 10, 10);
        
        for (int i = 0; i < 20; i++){
            for (int j = 0; j < 20; j++){
                if (select.contains(theCloth.points[i][j].cPosX, theCloth.points[i][j].cPosY)){
                    drag = theCloth.points[i][j];
                    drag.movable = false;
                    
                    this.repaint();
                    break;
                }
            }
        }  
    }
    
    @Override
    public void mouseDragged(MouseEvent e){
        if (drag != null){
            int x = e.getX();
            int y = e.getY();
            
            
            drag.cPosX = x;
            drag.cPosY = y;
            
            
            this.repaint();
        } 
    }
    
    @Override
    public void mouseReleased(MouseEvent e){
       if (drag != null){
            drag.movable = true;
            drag = null;
       }
    }

    //Useless interfaces
    public void mouseEntered(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseMoved(MouseEvent e){}
    
    //Sets the size of the component
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 550);
    }
}

class Cloth{
    int pDist = 15;
    int pCount = 20;
    
    int startX = 250 - (pCount * pDist)/2;
    int startY = 50;
    
    Particle points[][] = new Particle[pCount][pCount];
    ArrayList<Constraint> lines = new ArrayList();
    
    Cloth(){
        initPoints(); //Initialise points on the cloth
        initConstrains(); //Initialises the constrainst
    }
    
    public void update(double dT) {
       // this.deform();
        for (Constraint c : this.lines){
            for(int i = 0; i < 20; i++){
                c.solve();
            }
        }
        
        //this.deform();
        
        //Updates the position of all points
        for (int i = 0; i < pCount; i++){
            for (int j = 0; j < pCount; j++){
                if (this.points[i][j].movable == true)
                    this.points[i][j].updatePos(dT);
            }
        }
    }
    
    
    //Create the points in grid formation
    private void initPoints(){
        for(int i = 0; i < pCount; i++){
            for(int j = 0; j < pCount; j++){
                this.points[i][j] = new Particle(startX + (pDist * j), startY + (pDist * i), 
                        i == 0 && (j == 0 || j == 1 || j == 9 || j == 10 || j == 18 || j == 19) ? false : true);
            }
        }
    }
    
    //Initializes all the constrains between points
    //Uses right and down tranversal at the same time
    private void initConstrains() {
        for (int i = 0; i < pCount; i++){
            for (int j = 0; j < pCount; j++){
                Particle currParticle = points[i][j];
                
                if (j != 19) lines.add(new Constraint(currParticle, points[i][j + 1]));
                if (i != 19) lines.add(new Constraint(currParticle, points[i + 1][j]));
                
            }
        }
    }
    
    //Extra function for a bit of extra fun
    //Displaces the particles by a random margin within 10 pixels
    public void deform(){
        for(int i = 1; i < pCount; i++){
            for(int j = 0; j < pCount; j++){
                int randomNum = ThreadLocalRandom.current().nextInt(-10, 10 + 1);
                this.points[i][j].cPosX = startX + (pDist * j) + randomNum;
                this.points[i][j].cPosY = startY + (pDist * i) + randomNum;
            }
        }
    }
}

class Particle {
    double cPosX, cPosY;  //The position of the particle in this time-step
    double pPosX, pPosY; //The position of the particle in the previous time-step
    double accX, accY; //The acceleration of the particle
    boolean movable; //Is the particle movable
    public static final double GRAVITY = 0.3;
    
    Particle(int posX, int posY, boolean mov){
        this.cPosX = posX;
        this.pPosX = posX;
        
        this.cPosY = posY;
        this.pPosY = posY;
        
        this.accX = 0;
        this.accY = GRAVITY;
        this.movable = mov;
    }
    
    void updatePos(double dT){ 
        //Calculate future positions
        if (this.movable == true){
            double fPosX = this.cPosX, fPosY = this.cPosY; //Future positions
            
            //Verlet integration that calculates the future position of a particle
            //Based on a variable time step, past position and acceleration
            fPosX += (this.cPosX - this.pPosX) + 0.5 * this.accX * dT * dT;
            fPosY += (this.cPosY - this.pPosY) + 0.5 * this.accY * dT * dT;
        
            //Update the position values
            this.pPosX = this.cPosX;
            this.pPosY = this.cPosY;
            this.cPosX = fPosX;
            this.cPosY = fPosY;
            
            //Box
            if(this.cPosY > 500) this.cPosY = 500;
        }
    }
}

//Defines the tether between two points
class Constraint {
    Particle stP, enP;
    Double length;
    Double normL;
    
    Constraint(Particle start, Particle end) {
        this.stP = start;
        this.enP = end;
        this.normL = 15.0;
    }
    
    //Calculates the length between the start point and end point
    private Double getLength(){
        this.length = Math.pow(Math.pow(enP.cPosX - stP.cPosX, 2) + Math.pow(enP.cPosY - stP.cPosY, 2), 0.5);
        return length;
    }
    
    public void solve(){
        double diffX = stP.cPosX - enP.cPosX;
        double diffY = stP.cPosY - enP.cPosY;
        
        double d = this.getLength();
        if (d != 0){
            double diffS = (normL - d)/d;
        
        
            double transX = diffX * 0.5 * diffS * 0.09;
            double transY = diffY * 0.5 * diffS * 0.09;
        
            if (stP.movable){
                this.stP.cPosX += transX;
                this.stP.cPosY += transY;
            }
        
            if (enP.movable){
                this.enP.cPosX -= transX;
                this.enP.cPosY -= transY;
            }
        }
    }
}


