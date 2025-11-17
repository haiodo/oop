---
marp: true
theme: default
paginate: true
---

# Java JavaFX Programming

## Slide 1: Introduction

- What is JavaFX?
  - Modern Java library for building rich client applications
  - Successor to Swing and AWT
- History and evolution
  - Released in 2008, part of the JDK until Java 11
  - Now a separate module since Java 11
- Importance in modern Java applications
  - Cross-platform UI development
  - Rich media and graphical capabilities

---

## Slide 2: Setting Up JavaFX

- Installing JavaFX SDK
  - Download from openjfx.io
  - Add to project dependencies
- Configuring your IDE (IntelliJ, Eclipse, etc.)
  - Setting VM options: --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
- Hello World example

  ```java
  import javafx.application.Application;
  import javafx.scene.Scene;
  import javafx.scene.control.Label;
  import javafx.scene.layout.StackPane;
  import javafx.stage.Stage;

  public class HelloWorld extends Application {
    @Override
    public void start(Stage stage) {
      Label label = new Label("Hello, JavaFX!");
      StackPane root = new StackPane(label);
      Scene scene = new Scene(root, 400, 200);
      stage.setScene(scene);
      stage.setTitle("Hello World");
      stage.show();
    }

    public static void main(String[] args) {
      launch(args);
    }
  }
  ```

---

## Slide 3: JavaFX Architecture

- Overview of JavaFX architecture
  - Platform
  - Graphics System
  - Media Engine
  - Web Engine
- Key components: Stage, Scene, Nodes
  - Stage: Top-level container (window)
  - Scene: Content container
  - Nodes: UI elements (controls, shapes, etc.)
- Application lifecycle
  - init() → start() → stop()

---

## Slide 4: JavaFX Application Structure

- Main class
  - Extends javafx.application.Application
  - Main entry point for JavaFX applications
- Start method
  - Override start(Stage primaryStage)
  - Where UI components are created and added
- Launching the application
  - Using launch() method in main()
  - Application thread vs UI thread

---

## Slide 5: Stage and Scene

- Understanding Stage
  - Top-level UI container (window)
  - Properties for title, size, position, etc.
- Understanding Scene
  - Container for all UI elements
  - Attached to a Stage
- Creating and setting a Scene

---

```java
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StageSceneDemo extends Application {
  @Override
  public void start(Stage stage) {
    Group root = new Group();
    Scene scene = new Scene(root, 800, 600);
    stage.setTitle("Stage and Scene Demo");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
```

---

## Slide 6: JavaFX Layouts

- Introduction to layouts
  - Container nodes that manage child node positions
- Types of layouts: HBox, VBox, BorderPane, GridPane, etc.
  - HBox/VBox: Horizontal/vertical arrangement
  - BorderPane: Regions (top, bottom, left, right, center)
  - GridPane: Grid-based layout
  - StackPane: Layered nodes
- Choosing the right layout

---

```java
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

VBox vbox = new VBox(10); // Adding spacing between elements
vbox.setPadding(new Insets(15));
vbox.setAlignment(Pos.CENTER);
vbox.getChildren().addAll(
    new Label("Label 1"),
    new Label("Label 2"),
    new Button("Click Me")
);
```

---

## Slide 7: UI Controls

- Common UI controls: Button, Label, TextField, etc.
- Adding controls to a layout
- Handling user input

  ```java
  import javafx.scene.control.Button;

  Button button = new Button("Click Me");
  button.setOnAction(e -> System.out.println("Button Clicked!"));
  ```

---

## Slide 8: Event Handling

- Event types
- Event handlers
- Lambda expressions for event handling
  ```java
  button.setOnAction(event -> {
    System.out.println("Button Clicked!");
    // Always handle potential exceptions in event handlers
    try {
      // Your event handling code
    } catch (Exception e) {
      e.printStackTrace();
    }
  });
  ```

---

## Slide 9: Styling with CSS

- Applying CSS to JavaFX applications
- CSS selectors and properties
- Example of styling a button
  ```css
  .button {
    -fx-background-color: #ff0000;
    -fx-text-fill: #ffffff;
    -fx-font-weight: bold;
    -fx-padding: 5px 10px;
  }
  ```

---

## Slide 10: FXML

- Introduction to FXML
- Benefits of using FXML
- Loading FXML files

  ```java
  import javafx.fxml.FXMLLoader;
  import javafx.scene.Parent;

  try {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/path/to/sample.fxml"));
    Parent root = loader.load();
  } catch (IOException e) {
    e.printStackTrace();
  }
  ```

---

## Slide 11: Scene Builder

- What is Scene Builder?
- Installing and configuring Scene Builder
- Designing UI with Scene Builder

---

## Slide 12: Binding and Properties

- Understanding properties
- Property binding
- Observable lists and collections

  ```java
  import javafx.beans.property.SimpleStringProperty;
  import javafx.beans.property.StringProperty;

  StringProperty name = new SimpleStringProperty("John");
  name.addListener((observable, oldValue, newValue) ->
    System.out.println("Name changed from " + oldValue + " to " + newValue));
  ```

---

## Slide 13: Animation and Effects

- Basic animations
- Transition classes
- Applying effects to nodes

  ```java
  import javafx.animation.FadeTransition;
  import javafx.util.Duration;

  FadeTransition fade = new FadeTransition(Duration.millis(3000), node);
  fade.setFromValue(1.0);
  fade.setToValue(0.0);
  fade.setCycleCount(1);
  fade.play();
  ```

---

## Slide 14: Media in JavaFX

- Playing audio and video
- MediaPlayer and MediaView
- Controlling media playback

  ```java
  import javafx.scene.media.Media;
  import javafx.scene.media.MediaPlayer;
  import javafx.scene.media.MediaView;

  try {
    Media media = new Media(new File("path/to/media.mp4").toURI().toString());
    MediaPlayer mediaPlayer = new MediaPlayer(media);
    MediaView mediaView = new MediaView(mediaPlayer);
  } catch (Exception e) {
    e.printStackTrace();
  }
  ```

---

## Slide 15: Charts and Graphs

- Types of charts: LineChart, BarChart, PieChart, etc.
- Creating and customizing charts
- Adding data to charts

  ```java
  import javafx.scene.chart.*;

  CategoryAxis xAxis = new CategoryAxis();
  NumberAxis yAxis = new NumberAxis();
  BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
  barChart.setTitle("Sample Chart");
  xAxis.setLabel("Categories");
  yAxis.setLabel("Values");
  ```

---

## Slide 16: JavaFX Concurrency

- Concurrency in JavaFX
- Task and Service classes
- Updating UI from background threads

  ```java
  import javafx.concurrent.Task;

  Task<Void> task = new Task<Void>() {
    @Override
    protected Void call() throws Exception {
      Platform.runLater(() -> {
        // UI updates here
      });
      return null;
    }
  };
  Thread thread = new Thread(task);
  thread.setDaemon(true);
  thread.start();
  ```

---

## Slide 17: Custom Controls

- Creating custom controls
- Extending existing controls
- Custom control example

  ```java
  import javafx.scene.control.Button;

  public class CustomButton extends Button {
    public CustomButton() {
      super("Custom Button");
      this.getStyleClass().add("custom-button");
      this.setOnAction(e -> handleClick());
    }

    private void handleClick() {
      // Custom click handling
    }
  }
  ```

---

## Slide 18: Deployment

- Packaging JavaFX applications
- Creating executable JAR files
- Distributing your application

---

## Slide 19: Best Practices

- Code organization
- Performance optimization
- Testing JavaFX applications

---

## Slide 20: Resources and Further Learning

- Official documentation
- Online tutorials and courses
- Books and community forums

---

# Thank You!

- Questions?
- Contact information
- Additional resources
