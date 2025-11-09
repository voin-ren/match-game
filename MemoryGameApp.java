import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A complete Memory Match game written in JavaFX.
 *
 * To compile and run this (if you have the JavaFX SDK):
 * 1. javac --module-path /path/to/javafx/lib --add-modules javafx.controls MemoryGameApp.java
 * 2. java --module-path /path/to/javafx/lib --add-modules javafx.controls MemoryGameApp
 *
 * (It's much easier to run this from an IDE like IntelliJ or Eclipse with JavaFX configured)
 */
public class MemoryGameApp extends Application {

    private static final int NUM_PAIRS = 8;
    private static final int GRID_COLS = 4;
    private static final int GRID_ROWS = 4;

    private GridPane gameBoard;
    private Label movesLabel;
    
    private int moves = 0;
    private int matchedPairs = 0;
    private Card firstCard = null;
    private Card secondCard = null;
    private boolean lockBoard = false;

    private List<String> cardEmojis = List.of(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼"
    );

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX Memory Match");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2c3e50;"); // Dark blue-gray background

        // --- Top Info Bar ---
        HBox infoBar = new HBox(20);
        infoBar.setAlignment(Pos.CENTER);
        
        movesLabel = new Label("Moves: 0");
        movesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        movesLabel.setTextFill(Color.WHITE);

        Button restartButton = new Button("Restart");
        restartButton.setFont(Font.font("Arial", 16));
        restartButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        restartButton.setOnAction(e -> startGame());

        infoBar.getChildren().addAll(movesLabel, restartButton);
        root.setTop(infoBar);
        BorderPane.setAlignment(infoBar, Pos.CENTER);
        BorderPane.setMargin(infoBar, new Insets(0, 0, 20, 0));

        // --- Game Board ---
        gameBoard = new GridPane();
        gameBoard.setAlignment(Pos.CENTER);
        gameBoard.setHgap(10);
        gameBoard.setVgap(10);
        root.setCenter(gameBoard);

        // --- Start Game ---
        startGame();

        Scene scene = new Scene(root, 450, 550);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Resets the game to its initial state
     */
    private void startGame() {
        // Reset game state
        moves = 0;
        matchedPairs = 0;
        firstCard = null;
        secondCard = null;
        lockBoard = false;

        movesLabel.setText("Moves: " + moves);
        gameBoard.getChildren().clear();

        // Create doubled and shuffled list of emojis
        List<String> shuffledEmojis = new ArrayList<>(cardEmojis);
        shuffledEmojis.addAll(cardEmojis);
        Collections.shuffle(shuffledEmojis);

        // Create and add cards to the grid
        for (int i = 0; i < shuffledEmojis.size(); i++) {
            Card card = new Card(shuffledEmojis.get(i));
            int r = i / GRID_COLS;
            int c = i % GRID_COLS;
            gameBoard.add(card, c, r);
        }
    }

    /**
     * Handles the logic for flipping a card
     * @param card The card that was clicked
     */
    private void handleCardClick(Card card) {
        if (lockBoard || card.isFlipped() || card.isMatched()) {
            return;
        }

        card.flip();

        if (firstCard == null) {
            // This is the first card
            firstCard = card;
        } else {
            // This is the second card
            secondCard = card;
            lockBoard = true; // Lock board while checking
            moves++;
            movesLabel.setText("Moves: " + moves);

            checkForMatch();
        }
    }

    /**
     * Checks if the two flipped cards are a match
     */
    private void checkForMatch() {
        if (firstCard.getEmoji().equals(secondCard.getEmoji())) {
            // It's a match
            firstCard.setMatched(true);
            secondCard.setMatched(true);
            matchedPairs++;
            resetBoard();

            if (matchedPairs == NUM_PAIRS) {
                showWinAlert();
            }
        } else {
            // Not a match, flip back after a delay
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> {
                firstCard.flip();
                secondCard.flip();
                resetBoard();
            });
            pause.play();
        }
    }

    /**
     * Resets the flipped card variables and unlocks the board
     */
    private void resetBoard() {
        firstCard = null;
        secondCard = null;
        lockBoard = false;
    }

    /**
     * Shows a "You Won!" alert and restarts the game on close.
     */
    private void showWinAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("You Won!");
        alert.setHeaderText("Congratulations!");
        alert.setContentText("You matched all cards in " + moves + " moves.");
        
        // When the alert is closed, restart the game
        alert.setOnHidden(e -> startGame());
        
        alert.showAndWait();
    }


    // --- Inner Card Class ---

    /**
     * Represents a single card in the game.
     * It's a StackPane that contains the front and back views.
     */
    private class Card extends StackPane {
        private String emoji;
        private boolean isFlipped = false;
        private boolean isMatched = false;

        private Node frontView; // The "❓" side
        private Node backView;  // The "🐶" side

        public Card(String emoji) {
            this.emoji = emoji;

            // Create the card's base shape
            Rectangle base = new Rectangle(80, 100);
            base.setFill(Color.web("#ecf0f1")); // Light gray
            base.setArcWidth(10);
            base.setArcHeight(10);
            
            // Create the front view (question mark)
            Label frontLabel = new Label("❓");
            frontLabel.setFont(Font.font("Arial", 40));
            frontView = createCardView(Color.web("#3498db"), frontLabel); // Blue
            
            // Create the back view (emoji)
            Label backLabel = new Label(emoji);
            backLabel.setFont(Font.font("Arial", 40));
            backView = createCardView(Color.web("#ecf0f1"), backLabel); // Light gray
            
            // The back view is initially hidden (rotated 180 deg)
            backView.setRotationAxis(Rotate.Y_AXIS);
            backView.setRotate(180);

            getChildren().addAll(base, backView, frontView);
            
            // Set up click handler
            setOnMouseClicked(e -> handleCardClick(this));
        }

        private Parent createCardView(Color color, Node content) {
            StackPane view = new StackPane(content);
            view.setPrefSize(80, 100);
            view.setStyle("-fx-background-color: " + color.toString().replace("0x", "#") + "; " +
                          "-fx-background-radius: 5;");
            return view;
        }

        public String getEmoji() {
            return emoji;
        }

        public boolean isFlipped() {
            return isFlipped;
        }

        public boolean isMatched() {
            return isMatched;
        }

        public void setMatched(boolean matched) {
            isMatched = matched;
            if (matched) {
                // Style for matched card (e.g., green)
                backView.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 5;"); // Green
            }
        }

        /**
         * Animates the card flip
         */
        public void flip() {
            isFlipped = !isFlipped;

            // Create the rotation animation
            RotateTransition rt1 = new RotateTransition(Duration.millis(300), this);
            rt1.setAxis(Rotate.Y_AXIS);
            rt1.setByAngle(90);
            rt1.setOnFinished(e -> {
                // At 90 degrees, switch which face is visible
                if (isFlipped) {
                    frontView.setVisible(false);
                    backView.setVisible(true);
                } else {
                    frontView.setVisible(true);
                    backView.setVisible(false);
                }
            });

            RotateTransition rt2 = new RotateTransition(Duration.millis(300), this);
            rt2.setAxis(Rotate.Y_AXIS);
            rt2.setByAngle(90);

            // Play the first half of the rotation
            rt1.play();
            // After the first half finishes, play the second half
            rt1.setOnFinished(e -> {
                if (isFlipped) {
                    frontView.setVisible(false);
                    backView.setVisible(true);
                } else {
                    frontView.setVisible(true);
                    backView.setVisible(false);
                }
                // Correct the rotation to continue
                setRotate(isFlipped ? 90 : 270); 
                rt2.play();
            });

            // When the full rotation is done, reset the node's rotation
            rt2.setOnFinished(e -> setRotate(0));
        }
    }
}
