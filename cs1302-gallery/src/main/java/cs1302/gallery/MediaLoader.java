package cs1302.gallery;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.event.ActionEvent;
import javafx.scene.layout.Priority;

/**
 * Class MediaLoader that extends VBox.
 */

public class MediaLoader extends VBox {

    /** A default image which loads when the application starts. */
    protected static final String DEFAULT_IMG =
        "file:resources/default.png";

    /** Default height and width for Images. */
    protected static final int DEF_HEIGHT = 150;
    protected static final int DEF_WIDTH = 150;


    /** The container for the url textfield and the load image button. */
    HBox urlLayer;

    /** The container for the loaded image. */
    ImageView imgView;

    /**
     * ImageLoader default constructor.
     */

    public MediaLoader() {
        super();
        urlLayer = new HBox(10);
        // Load the default image with the default dimensions
        Image img = new Image(DEFAULT_IMG, DEF_HEIGHT, DEF_WIDTH, false, false);

        // Add the image to its container and preserve the aspect ratio if resized
        imgView = new ImageView(img);
        imgView.setFitWidth(100.0);
        imgView.setFitHeight(100.0);
        imgView.setPreserveRatio(true);

        this.getChildren().addAll(urlLayer, imgView);
    } // ImageLoader

    /**
     * Is the second constructor.
     * @param img is an image to place in the frame.
     */

    public MediaLoader(Image img) {
        super();
        //imgView = new ImageView(img);
        imgView = new ImageView(img);
        urlLayer = new HBox(10);
        imgView.setPreserveRatio(true);
        this.getChildren().addAll(urlLayer, imgView);
    }

    /**
     * Students will provide javadoc comments here.
     *
     * @param e source event
     */
    /*
    private void loadImage(ActionEvent e) {

        try {
            Image newImg = new Image(urlField.getText(), DEF_HEIGHT, DEF_WIDTH, false, false);
            imgView.setImage(newImg);
        } catch (IllegalArgumentException iae) {
            System.out.println("The supplied URL is invalid");
        } // try

    } // loadImage
    */

}
