package org.seagrid.desktop.experiment.list;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;

public class ExperimentListWindow extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("experiment-list.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("SEAGrid Desktop Client");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

        ExperimentListController experimentListController = loader.getController();
        experimentListController.updateExperimentList(new HashMap<>(), -1, 0);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
