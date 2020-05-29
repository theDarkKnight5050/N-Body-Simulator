package model;

import java.util.HashSet;
import java.util.Set;

public class Fields {
    private static double K;
    private static double G;
    //Time scale between each frame
    public static final double DELTA_T = .01;
    //Just in case
    private static final double DISTANCE_OFFSET = 1;
    private static Set<Particle> particles;

    //getters and setters
    public static void setK(double k) { K = k; }
    public static void setG(double g) { G = g; }


    /**
     * Calculate gravitation field vector at specific point
     *
     * @param xPos Cartesian x coordinate
     * @param yPos Cartesian y coordinate
     * @param exclude Particle to not be included while calculating field (useful to avoid self-energies)
     * @return double[] representing components of field vector
     */
    public static double[] getGField(double xPos, double yPos, Particle exclude){
        double[] gVector = new double[2];
        for(Particle p : particles){
            if(p.equals(exclude)) continue;
            double distance = distance(xPos, yPos, p.getxPos(), p.getyPos());
            //deals with dividing by 0
            if(distance == 0){
                gVector[0] = Integer.MAX_VALUE;
                gVector[1] = Integer.MAX_VALUE;
                return gVector;
            }
            //Newton's Law of Gravitation
            double theta = Math.atan((yPos - p.getyPos()) / (xPos - p.getxPos()));
            if((p.getxPos() - xPos) < 0) theta += Math.PI;
            gVector[0] += G * p.getMass() / Math.pow(distance + DISTANCE_OFFSET, 2) * Math.cos(theta);
            gVector[1] += G * p.getMass() / Math.pow(distance + DISTANCE_OFFSET, 2) * Math.sin(theta);
        }
        return gVector;
    }

    /**
     * Calculate electric field vector at specific point
     *
     * @param xPos Cartesian x coordinate
     * @param yPos Cartesian y coordinate
     * @param exclude Particle to not be included while calculating field (useful to avoid self-energies)
     * @return double[] representing components of field vector
     */
    public static double[] getEField(double xPos, double yPos, Particle exclude){
        double[] eVector = new double[2];
        for(Particle p : particles){
            if(p.equals(exclude)) continue;
            double distance = distance(xPos, yPos, p.getxPos(), p.getyPos());
            //deals with dividing by zero
            if(distance == 0){
                eVector[1] = Integer.MAX_VALUE;
                eVector[0] = Integer.MAX_VALUE;
                return eVector;
            }
            //Coulomb's Law from electrostatics
            double theta = Math.atan((p.getyPos() - yPos) / (p.getxPos() - xPos));
            if((p.getxPos() - xPos) < 0) theta += Math.PI;
            eVector[0] -= K * p.getCharge() / Math.pow(distance + DISTANCE_OFFSET, 2) * Math.cos(theta);
            eVector[1] -= K * p.getCharge() / Math.pow(distance + DISTANCE_OFFSET, 2) * Math.sin(theta);
        }
        return eVector;
    }

    public static void addParticle(Particle p){
        if(particles == null){ particles = new HashSet<Particle>(); }
        particles.add(p);
    }
    public static void clearParticles(){
        if(particles == null){ particles = new HashSet<Particle>(); }
        particles.clear();
    }

    /**
     * Implements euclidean distance formula
     * @return distance from (x1, y1) to (x2, y2)
     */
    public static double distance(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }
}
