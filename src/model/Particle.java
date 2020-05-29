package model;

import javafx.scene.paint.Color;
import java.util.Random;

public class Particle {

    /**
     * Creates particle with specified initial conditions
     * @param xPos Cartesian x coordinate
     * @param yPos Cartesian y coordinate
     * @param xVel Initial change in x coordinate over time
     * @param yVel Initial change in y coordinate over time
     * @param mass Particle mass
     * @param charge Particle charge
     */
    public Particle(double xPos, double yPos, double xVel, double yVel, double mass, double charge){
        this.xPos = xPos;
        this.yPos = yPos;
        this.xVel = xVel;
        this.yVel = yVel;
        this.mass = mass;
        this.charge = charge;
        numParticles++;
        Random r = new Random();
        color = new Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), 1.);
    }

    /**
     * Updates particle information given field information
     */
    public void updatePos(){
        xVel += dvx;
        yVel += dvy;
        xPos += dx;
        yPos += dy;
        dvx = dvy = dx = dy = 0;
    }

    /**
     * Uses localized Field information to set the expected deltas for this particle
     */
    public void updateDeltas(){
        double[] gForces = Fields.getGField(xPos, yPos, this);
        double[] eForces = Fields.getEField(xPos, yPos, this);
        if(mass == 0 && charge == 0){
            dvx = dvy = 0;
        } else{
            dvx += (mass * gForces[0] + charge * eForces[0]) * Fields.DELTA_T / mass;
            dvy += (mass * gForces[1] + charge * eForces[1]) * Fields.DELTA_T / mass;
        }

        dx += (xVel + dvx) * Fields.DELTA_T;
        dy += (yVel + dvy) * Fields.DELTA_T;
    }

    public boolean equals(Particle p){
        return p != null && xPos == p.getxPos() && yPos == p.getyPos();
    }

    //getters and setters
    public double getDx() { return dx; }
    public double getDy() { return dy; }
    public double getxPos() { return xPos; }
    public double getyPos() { return yPos; }
    public double getMass() { return mass; }
    public double getCharge() { return charge; }
    public Color getColor() {return this.color; }

    private static int numParticles;
    private double xPos, yPos;
    private double dx, dy;
    private double xVel, yVel;
    private double dvx, dvy;
    private double mass;
    private double charge;
    private Color color;
}
