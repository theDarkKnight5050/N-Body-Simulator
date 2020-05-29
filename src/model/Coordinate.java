package model;

public class Coordinate {
    private Vector gField;
    private Vector eField;
    private double xCoord;
    private double yCoord;

    /**
     * Creates a coordinate from a given (x, y) by calculating associated fields
     *
     * @param x Cartesian x-coordinate
     * @param y Cartsian y-coordinate
     */
    public Coordinate(double x, double y){
        xCoord = x;
        yCoord = y;
        gField = new Vector(Fields.getGField(xCoord, yCoord, null));
        eField = new Vector(Fields.getEField(xCoord, yCoord, null));
    }

    public void updateField(){
        gField = new Vector(Fields.getGField(xCoord, yCoord, null));
        eField = new Vector(Fields.getEField(xCoord, yCoord, null));
    }

    //getters and setters
    public Vector getgField() { return gField; }
    public Vector geteField() { return eField; }
    public double getxCoord() { return xCoord; }
    public double getyCoord() { return yCoord; }

    /**
     * Nested class used to represent the two vectors emanating from Coordinate
     */
    public class Vector{
        private double xDisplacement;
        private double yDisplacement;

        public Vector(double[] vec){
            xDisplacement = vec[0];
            yDisplacement = vec[1];
        }

        public double[] getCartesianCoordinates(){
            return new double[]{xDisplacement, yDisplacement};
        }

        //for future relativistic purposes
        public double[] getPolarCoordinates(){
            return new double[]{Math.sqrt(Math.pow(xDisplacement, 2) + Math.pow(yDisplacement, 2)), Math.atan(yDisplacement/xDisplacement)};
        }
    }
}
