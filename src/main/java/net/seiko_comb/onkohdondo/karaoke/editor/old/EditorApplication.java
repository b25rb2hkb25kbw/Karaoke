package net.seiko_comb.onkohdondo.karaoke.editor.old;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class EditorApplication extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane pane = new BorderPane();

		MenuBar menuBar = new MenuBar(new Menu("File"));
		pane.setTop(menuBar);

		BorderPane canvasPane = new BorderPane();
		pane.setCenter(canvasPane);
		canvasPane.setCenter(new Button("Test"));
		canvasPane.heightProperty().addListener(System.out::println);
		ResizableCanvas resizableCanvas = new ResizableCanvas();
		canvasPane.setCenter(resizableCanvas);
		resizableCanvas.widthProperty().bind(canvasPane.widthProperty());
		resizableCanvas.heightProperty().bind(canvasPane.heightProperty());

		Scene scene = new Scene(pane, 640, 480);
		primaryStage.setScene(scene);
		primaryStage.show();
		scene.heightProperty().addListener(
				event -> System.out.println(canvasPane.snappedBottomInset()));
	}

}
