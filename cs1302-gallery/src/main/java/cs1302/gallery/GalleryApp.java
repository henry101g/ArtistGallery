package cs1302.gallery;

import java.net.http.HttpClient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import javafx.scene.text.Font;
import javafx.geometry.Pos;
import java.util.Set;
import javafx.scene.layout.TilePane;
import java.util.*;
import java.util.HashSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.animation.Timeline;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Represents an iTunes Gallery App.
 */
public class GalleryApp extends Application {

    /** A default image which loads when the application starts. */
    protected static final String DEFAULT_IMG =
        "file:resources/default.png";

    /** Default height and width for Images. */
    protected static final int DEF_HEIGHT = 150;
    protected static final int DEF_WIDTH = 150;

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    private static final String ITUNES_API = "https://itunes.apple.com/search";

    private Stage stage;
    private Scene scene;
    private VBox root;
    private TextField searchString;
    private Button loadImage;
    private Button play;
    private TilePane tile;
    private ComboBox<String> dropdown;
    private Label instructions;
    private ProgressBar pb;
    private Label search;
    private Timeline timeline;
    private VBox vbox1;
    private VBox layout;
    private HBox hbox1;
    private Label copyright;
    private HBox bottom;
    private HBox[] imgRow;
    private VBox vbox2;
    private VBox vbox3;
    private VBox vbox4;
    private VBox[] imgFrame;
    private double progress;
    private String[] imgURLs;
    private boolean first;

    /**
     * Constructs a {@code GalleryApp} object}.
     */

    public GalleryApp() throws IOException, InterruptedException {
        inits();
        labelsInit();
        first = true;
        buttonsInit();
        progress = 0.0;
        tile.setPrefColumns(5);
        tile.setPrefRows(4);
        layout.getChildren().addAll(tile);
        dropdownInit();

        play.setOnAction(e -> {
            if ( play.getText().equals("Play")) {
                play.setText("Pause");
                try {
                    playMethod();
                } catch (Exception ee) {
                    System.err.println(ee);
                }
            } else {
                play.setText("Play");
            }
        });

        loadImage.setOnAction(e -> {
            Runnable runnable = () -> {
                play.setDisable(true);
                play.setText("Play");
                loadImage.setDisable(true);
                instructions.setText("Getting images...");
                try {
                    loadImages();
                } catch (Exception ee) {
                    System.err.println(ee);
                }
            };
            progress = 0.0;
            pb.setProgress(0.0);
            httpRequest();
            try {
                loadImages();
            } catch (Exception ee) {
                System.err.println(ee);
            }
            Platform.runLater(runnable);
        });

        bottom = new HBox();
        imgFrame = new MediaLoader[20];
        progressBarInit();
        loadImages();
        hbox1.getChildren().addAll(play, search, searchString, dropdown, loadImage);
        hbox1.setHgrow(searchString, Priority.ALWAYS);
        hbox1.setAlignment(Pos.CENTER);
        vbox2.getChildren().addAll(instructions);
        root.getChildren().addAll(hbox1, vbox2);
        root.getChildren().addAll(layout);
        bottom.getChildren().addAll(pb, copyright);
        root.getChildren().addAll(bottom);
    } // GalleryApp

    /**
     * Initializes different objects.
     */

    public void inits() {
        this.stage = null;
        this.scene = null;
        this.root = new VBox();
        this.hbox1 = new HBox();
        this.vbox3 = new VBox();
        this.vbox4 = new VBox();
        this.vbox2 = new VBox();
        layout = new VBox();
        imgURLs = new String[200];
        tile = new TilePane();
    }

    /**
     *Initializes everything needed for the progress bar.
     */

    public void progressBarInit() {
        pb = new ProgressBar();
        pb.setProgress(0);
        pb.setPrefWidth(400);
    }

    /**
     *Initializes everything needed for the buttons.
     */

    public void buttonsInit() {
        loadImage = new Button("Get image");
        play = new Button("Play");
        play.setDisable(true);
    }

    /**
     * Shows the progress bar for the images being loaded.
     */

    public void progressMeter() {
        progress =  progress + 0.05;
        pb.setProgress(progress);
    }

    /**
     *Initializes everything needed for the labels.
     */

    public void labelsInit() {
        searchString = new TextField("Jack Harlow");
        search = new Label(" Search: ");
        searchString.setFont(new Font(15));
        search.setFont(new Font(15));
        instructions = new Label("Type in a term, select a media type, then click the button.");
        instructions.setFont(new Font(15));
        copyright = new Label("Images provided by iTunes Search API.");
        copyright.setFont(new Font(15));
    }

    /**
     *Initializes everything needed for the dropdown menu.
     */

    public void dropdownInit() {
        dropdown = new ComboBox<String>();
        dropdown.getItems().addAll("movie","podcast","music","musicVideo","audioBook",
             "shortFilm","tvShow", "software", "ebook", "all");
        dropdown.setPromptText("music");
        dropdown.getSelectionModel().select(2);
    }

    /**
     * This function creates the URI's needed.
     * @return returns the URI as a string.
     */

    public String createURI() {
        String apiURL = "https://itunes.apple.com/search?";
        String temp = searchString.getText();
        int i = temp.indexOf(' ');
        String term;
        if (i == -1) {
            term = "term=" + searchString.getText();
        } else {
            term = "term=" + temp.substring(0, i) + "+" + temp.substring(i + 1);
        }

        String media = "&media=" + dropdown.getValue();
        String limit = "&limit=200";
        return apiURL + term + media + limit;
    }

    /**
     * This function loades the images.
     */

    public void loadImages() throws IOException, InterruptedException {
        Image img;
        tile.getChildren().clear();
        for (int i = 0; i < 20; i++) {
            if (imgURLs[i] != null) {
                Platform.runLater(() -> progressMeter());
                img = new Image(imgURLs[i],DEF_HEIGHT, DEF_WIDTH, false, false);
            } else {
                img = new Image(DEFAULT_IMG, DEF_HEIGHT, DEF_WIDTH, false, false);
            }
            imgFrame[i] = new MediaLoader(img);
            tile.getChildren().add(imgFrame[i]);
        }

        if (!first) {
            instructions.setText(createURI());
            play.setDisable(false);
        } else {
            first = false;
            instructions.setText("Type in a term, select a media type, then click the button.");
        }
        loadImage.setDisable(false);
    }

    /**
     * This function replaces the images.
     */

    public void replaceImage() throws IOException, InterruptedException {
        int oldIndex, newIndex;
        Random rand = new Random();
        String str;
        oldIndex = rand.nextInt(20);
        newIndex  = rand.nextInt(10) + 20;
        str = imgURLs[oldIndex];
        imgURLs[oldIndex] = imgURLs[newIndex];
        imgURLs[newIndex] = str;
        loadImages();
    }

    /**
     * This function cleans url string.
     */
    public void cleanURLs() {
        for (int i = 0; i < 200; i++) {
            imgURLs[i] = null;
        }
    }

    /**
     * This method sets the timeline for the random replacement.
     * @param handler the lambda expression that the timeline carries out
     */

    public void setTimeline(EventHandler<ActionEvent> handler) {

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    } // setTimeline


     /**
     * This method randomly replaces images in the tile pane on a timeline.
     */

    public void playMethod() {
        EventHandler<ActionEvent> handler = (e -> {

            if (play.getText() == "Play") {
                timeline.pause();
                return;
            }
            try {
                replaceImage();
            } catch (Exception ee) {
                System.err.println(ee);
            }

        });

        setTimeline(handler);
    } // playMethod

    /**
     * This function checks for duplicates.
     * @return returns a boolean if there was a duplicate found.
     * @param strA is the array of strings to look at.
     * @param str is a string to look for.
     */

    public boolean stringLookupInArray(String[] strA, String str) {
        for (int i = 0; i < strA.length; i++) {
            if (strA[i] != null && strA[i].equals(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method implements http client, calls itunes and recieves Json.
     */

    public void httpRequest() {
        try {
            // form URI
            String term = URLEncoder.encode(searchString.getText(), StandardCharsets.UTF_8);
            String media = URLEncoder.encode(dropdown.getSelectionModel().getSelectedItem()
                .toString(), StandardCharsets.UTF_8);
            String limit = URLEncoder.encode("200", StandardCharsets.UTF_8);
            String query = String.format("?term=%s&media=%s&limit=%s", term, media, limit);
            String uri = ITUNES_API + query;
            Platform.runLater(() -> progressMeter());
            // build request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
            // send request / receive response in the form of a String
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());
            Platform.runLater(() -> progressMeter());
            // ensure the request is okay
            if (response.statusCode() != 200) {
                throw new IOException(response.toString());
            } // if
            // get request body (the content we requested)
            String jsonString = response.body();
            Platform.runLater(() -> progressMeter());
            System.out.println("********** RAW JSON STRING: **********");
            System.out.println(jsonString.trim());
            // parse the JSON-formatted string using GSON
            ItunesResponse itunesResponse = GSON
                .fromJson(jsonString,ItunesResponse.class);
            Platform.runLater(() -> progressMeter());
            if (itunesResponse.resultCount < 21) {
                Platform.runLater(() -> {
                    String errorMessage = createURI() +
                        "\n\nException:java.lang.IllegalArgumentException: " +
                        itunesResponse.resultCount +
                        " distinct results found, but 21 or more are needed.";

                    Alert alert = new Alert(Alert.AlertType.ERROR,errorMessage,ButtonType.OK);
                    alert.showAndWait();
                    play.setText("Play");
                }); // Lambada expression for platform
                return;
            } else {
                System.out.println("download images to load");
                //GSON.fromJson(itunesResponse, imgURLs);
                parseItunesOutput(itunesResponse);
            }

            printItunesResponse(itunesResponse);
        } catch (IOException | InterruptedException e) {
            // either:
            // 1. an I/O error occurred when sending or receiving;
            // 2. the operation was interrupted; or
            // 3. the Image class could not load the image.
            System.err.println(e);
            e.printStackTrace();
        } // try
    }

    /**
     * This function parses Json output.
     * @param itunesResponse is the Json from the api call.
     */

    private void parseItunesOutput(ItunesResponse itunesResponse) {
        System.out.println("************************" + itunesResponse.results.length);
        int j = 0, i = 0;
        int count;
        cleanURLs();
        for (i = 0; i < itunesResponse.results.length; i++) {
            ItunesResult result = itunesResponse.results[i];
            if (stringLookupInArray(imgURLs, result.artworkUrl100)) {
                imgURLs[j] = result.artworkUrl100;
                j++;
            }
        } // for

        count = j;
        for (i = 0; i < count && j < 200; j++, i++) {
            if (i == count - 1) {
                i = 0;
            }
            imgURLs[j] = imgURLs[i];
        }
    }

    /**
     * Print a response from the iTunes Search API.
     * @param itunesResponse the response object
     */

    private static void printItunesResponse(ItunesResponse itunesResponse) {
        System.out.println();
        System.out.println("********** PRETTY JSON STRING: **********");
        System.out.println(GSON.toJson(itunesResponse));
        System.out.println();
        System.out.println("********** PARSED RESULTS: **********");
        System.out.printf("resultCount = %s\n", itunesResponse.resultCount);
        for (int i = 0; i < itunesResponse.results.length; i++) {
            System.out.printf("itunesResponse.results[%d]:\n", i);
            ItunesResult result = itunesResponse.results[i];
            System.out.printf(" - wrapperType = %s\n", result.wrapperType);
            System.out.printf(" - kind = %s\n", result.kind);
            System.out.printf(" - artworkUrl100 = %s\n", result.artworkUrl100);
        } // for
    } // parseItunesResponse

    /**
     * Returns a {@link javafx.image.Image} using data located at the specified
     * {@code url}. Instead of letting the {@code Image} class handle the
     * download, this method uses {@link java.net.http} instead.
     * @param url image location
     * @return an image
     * @throws IOException if an I/O error occurs when sending, receiving, or parsing
     * @throws InterruptedException if the HTTP client's {@code send} method is
     *    interrupted
     */

    private static Image fetchImage(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build();
        System.out.printf("request = %s\n", request);
        HttpResponse<InputStream> response = HTTP_CLIENT
            .send(request, BodyHandlers.ofInputStream());
        System.out.printf("response = %s\n", response);
        ensureGoodResponse(response);
        InputStream imageStream = response.body();
        Image newImage = new Image(imageStream);
        ensureGoodImage(newImage);
        return newImage;
    } // fetchImage


    /**
     * Throw an {@link java.io.IOException} if the HTTP status code of the
     * {@link java.net.http.HttpResponse} supplied by {@code response} is
     * not {@code 200 OK}.
     * @param <T> response body type
     * @param response response to check
     * @see <a href="https://httpwg.org/specs/rfc7231.html#status.200">[RFC7232] 200 OK</a>
     */

    private static <T> void ensureGoodResponse(HttpResponse<T> response) throws IOException {
        if (response.statusCode() != 200) {
            throw new IOException(response.toString());
        } // if
    } // ensureGoodResponse

    /**
     * Throw an {@link java.io.IOException} if an error was detected while loading
     * the supplied {@code image}.
     * @param image image to check
     */

    private static void ensureGoodImage(Image image) throws IOException {
        if (image.isError()) {
            Throwable cause = image.getException();
            throw new IOException(cause);
        } // if
    } // ensureGoodImage


    /** {@inheritDoc} */
    @Override
    public void init() {
        // feel free to modify this method
        System.out.println("init() called");
    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;
        this.scene = new Scene(this.root);
        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.setTitle("GalleryApp!");
        this.stage.setScene(this.scene);
        this.stage.sizeToScene();
        this.stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));

    } // start

    /** {@inheritDoc} */
    @Override
    public void stop() {
        // feel free to modify this method
        System.out.println("stop() called");
    } // stop


    /**
     * Students will provide javadoc comments here.
     *
     * @param e source event
     */

} // GalleryApp
