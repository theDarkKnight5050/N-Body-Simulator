package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import model.Coordinate;
import model.Fields;
import model.Particle;
import java.util.HashMap;
import java.util.Map;

public class Controller {
    @FXML
    private Button particleButton, toggleButton, clearButton;
    @FXML
    private ToggleButton fieldButton;
    @FXML
    private Slider gSlider, kSlider, xVelSlider, yVelSlider;
    @FXML
    private TextField massLabel, chargeLabel;
    @FXML
    private TextArea log;
    @FXML
    private Pane playground;
    private double tempMass, tempCharge, tempXVel, tempYVel;
    private boolean isParticleReady;
    private boolean isPlaying = true;
    private boolean boundsSet = false;
    private final double CIRCLE_RADIUS = 3;
    //make field lines noticeably large
    private final double FIELD_SCALE_FACTOR = 100;
    //size of grid of field coordinates
    private final int SIZE_VECTOR_FIELD = 20;
    private Map<Coordinate, Line[]> coordFields;
    private Map<Particle, Circle> particles;
    private Timeline timeline;
    private Bounds playgroundBounds;

    /**
     * Initializes variables and defines how to handle each DELTA_T timestep
     */
    @FXML
    private void initialize(){
        //link Models to View elements
        particles = new HashMap<>();
        coordFields = new HashMap<>();

        //describes what to do during every time step
        timeline = new Timeline(new KeyFrame(Duration.seconds(Fields.DELTA_T), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                updateParticles();
                updateCoordinateFields();
                playground.toBack();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * Describes what should happen when the PLAY/PAUSE buttons are pressed
     */
    @FXML
    private void toggleAnimation(){
        if(isPlaying){
            isPlaying = false;

            //switch out image
            Image image = new Image("imgs/pauseButton.png", 70., 90., true, true);
            ImageView view = new ImageView(image);
            toggleButton.setGraphic(view);

            //eventually want to disable clearButton when playing
//            clearButton.setDisable(true);
            timeline.play();
            writeLog("Running...");
        } else{
            isPlaying = true;

            //switch out image
            Image image = new Image("imgs/playButton.png", 70., 90., true, true);
            ImageView view = new ImageView(image);
            toggleButton.setGraphic(view);

            clearButton.setDisable(false);
            timeline.pause();
            writeLog("Paused");
        }
    }

    /**
     * Describes function of clearButton
     */
    @FXML
    private void clearAnimation(){
        for(Particle p : particles.keySet()){
            playground.getChildren().remove(particles.get(p));
        }
        particles.clear();
        Fields.clearParticles();
        writeLog("Playground Cleared");
    }

    /**
     * Pressing particleButton caches data from fields to create next particle
     */
    @FXML
    private void loadParticle() {
        try {
            //get data
            tempMass = Double.parseDouble(massLabel.getText());
            if (tempMass < 0) { throw new ArithmeticException("Negative mass"); }
            tempCharge = Double.parseDouble(chargeLabel.getText());
            tempXVel = xVelSlider.getValue();
            tempYVel = -1 * yVelSlider.getValue();

            //if this is the first particle we should also do some maintenance
            //TODO: for some reason the panes bounds are not correctly set before this point...investigate
            if(!boundsSet){
                boundsSet = true;
                playgroundBounds = playground.getBoundsInLocal();

                //ensures all of the Fields fields are set
                Fields.clearParticles();
                //create each coordinate for which we'll be tracking field interactions
                for(double x = 0; x < playgroundBounds.getMaxX(); x += (playgroundBounds.getMaxX()/SIZE_VECTOR_FIELD)){
                    for(double y = 0; y < playgroundBounds.getMaxY(); y += (playgroundBounds.getMaxY()/SIZE_VECTOR_FIELD)){
                        Coordinate c = new Coordinate(x, y);
                        Line[] fieldLine = createLines(c);
                        coordFields.put(c, fieldLine);
                    }
                }
            }

            //disable button until the particle is placed
            particleButton.setDisable(true);
            isParticleReady = true;
            writeLog("Particle ready for insertion into playground");
        } catch (Exception e) {
            writeLog("ERROR: Misformatted Particle");
            writeLog(e.getMessage());
        }
    }

    /**
     * Places particle at specified location using cached data
     *
     * @param event MouseEvent representing mouse click
     */
    @FXML
    private void placeParticle(MouseEvent event) {
        if (isParticleReady) {
            Particle p = new Particle(event.getX(), event.getY(), tempXVel, tempYVel, tempMass, tempCharge);
            Fields.addParticle(p);

            particles.put(p, createCircle(p));
            playground.getChildren().add(particles.get(p));

            particleButton.setDisable(false);
            isParticleReady = false;
            writeLog("Successfully placed particle");
        } else {
            writeLog("ERROR: Please prepare a suitable particle then press the button before attempting to insert into playground");
        }
    }

    /**
     * Update Gravitational constant
     */
    @FXML
    private void gSliderChange(){
        writeLog("Updated strength of gravity to: " + (int)gSlider.getValue() + "/" + gSlider.getMax());
        Fields.setG(gSlider.getValue());
    }

    /**
     * Update Coulombic constant
     */
    @FXML
    private void kSliderChange(){
        writeLog("Updated strength of electricity to: " + (int)kSlider.getValue() + "/" + kSlider.getMax());
        Fields.setK(kSlider.getValue());
    }

    /**
     * Move each particle by a small time jump DELTA_T
     */
    public void updateParticles(){
        for(Particle p : particles.keySet()){
            p.updateDeltas();

            double x = p.getxPos() + p.getDx();
            double y = p.getyPos() + p.getDy();

            //keep Circle in bounds
            if(x >= (playgroundBounds.getMaxX() - CIRCLE_RADIUS) || y >= (playgroundBounds.getMaxY() - CIRCLE_RADIUS)){
                playground.getChildren().remove(particles.get(p));
                particles.replace(p, null);
            }
        }
        for(Particle p : particles.keySet()){
            p.updatePos();
            if(!(particles.get(p) == null)){
                //move associated circle up
                playground.getChildren().remove(particles.get(p));
                particles.replace(p, createCircle(p));
                playground.getChildren().add(particles.get(p));
            }
        }
    }

    /**
     * Move each field line by a small time jump DELTA_T
     */
    public void updateCoordinateFields(){
        for(Coordinate c : coordFields.keySet()) {
            c.updateField();
            playground.getChildren().remove(coordFields.get(c)[0]);
            playground.getChildren().remove(coordFields.get(c)[1]);
            coordFields.replace(c, createLines(c));
            playground.getChildren().add(coordFields.get(c)[0]);
            playground.getChildren().add(coordFields.get(c)[1]);
        }
    }

    /**
     * Create circle from particle
     * @param p Particle to be used as template
     * @return Circle which uses p's characteristic
     */
    public Circle createCircle(Particle p){ return new Circle(p.getxPos(), p.getyPos(), CIRCLE_RADIUS, p.getColor()); }

    /**
     * Create two lines from Coordinate
     * @param c Coordinate to be used as template
     * @return Line[] holding associated field values
     */
    public Line[] createLines(Coordinate c){
        Coordinate.Vector gVec = c.getgField();
        Coordinate.Vector eVec = c.geteField();
        Line[] fieldLines = new Line[2];

        fieldLines[0] = new Line(c.getxCoord(),
                c.getyCoord(),
                c.getxCoord() + gVec.getCartesianCoordinates()[0]*FIELD_SCALE_FACTOR,
                c.getyCoord() + gVec.getCartesianCoordinates()[1]*FIELD_SCALE_FACTOR);

        fieldLines[1] = new Line(c.getxCoord(),
                c.getyCoord(),
                c.getxCoord() + eVec.getCartesianCoordinates()[0]*FIELD_SCALE_FACTOR,
                c.getyCoord() + eVec.getCartesianCoordinates()[1]*FIELD_SCALE_FACTOR);

        //action of field toggle button
        if(fieldButton.isSelected()){
            fieldLines[0].setStroke(Color.BLUE);
            fieldLines[1].setStroke(Color.RED);
        } else{
            fieldLines[0].setStroke(Color.TRANSPARENT);
            fieldLines[1].setStroke(Color.TRANSPARENT);
        }

        return fieldLines;
    }

    public void writeLog(String text) { log.appendText(text + '\n'); }
}