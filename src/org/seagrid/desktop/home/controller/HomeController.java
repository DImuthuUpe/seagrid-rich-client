/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.seagrid.desktop.home.controller;


import com.google.common.eventbus.Subscribe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.experiment.ExperimentSearchFields;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.workspace.Project;
import org.seagrid.desktop.apis.airavata.AiravataManager;
import org.seagrid.desktop.events.SEAGridEvent;
import org.seagrid.desktop.events.SEAGridEventBus;
import org.seagrid.desktop.experiment.summary.ExperimentSummaryWindow;
import org.seagrid.desktop.home.model.ExperimentListModel;
import org.seagrid.desktop.home.model.TreeModel;
import org.seagrid.desktop.project.ProjectWindow;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Controls the home screen */
public class HomeController {

    private ObservableList<ExperimentListModel> observableExperimentList = FXCollections.observableArrayList();

    @FXML
    public Button createProjectButton;

    @FXML
    private TreeView<TreeModel> projectsTreeView;

    @FXML
    private TableView<ExperimentListModel> expSummaryTable;

    @FXML
    private TableColumn<ExperimentListModel, Boolean> expCheckedColumn;

    @FXML
    private TableColumn<ExperimentListModel, String> expApplicationColumn;

    @FXML
    private TableColumn<ExperimentListModel, String> expHostColumn;

    @FXML
    private TableColumn<ExperimentListModel, String> expStatusColumn;

    @FXML
    private TableColumn<ExperimentListModel, String> expNameColumn;

    @FXML
    private TableColumn<ExperimentListModel, LocalDateTime> expCreateTimeColumn;

    @FXML
    private CheckBox checkAllExps;

    @FXML
    private TextField filterField;

    @FXML
    private TabPane tabbedPane;

    public void initialize() {
        initMenuBar();
        initProjectTreeView();
        initExperimentList();
    }


    public void initMenuBar(){
        createProjectButton.setOnMouseClicked(event -> {
            ProjectWindow projectWindow = new ProjectWindow();
            try {
                projectWindow.displayCreateProjectAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void initProjectTreeView(){
        projectsTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        projectsTreeView.setCellFactory(param -> {
            TreeCell<TreeModel> cell = new TreeCell<TreeModel>(){
                @Override
                public void updateItem(TreeModel item, boolean empty) {
                    super.updateItem(item, empty) ;
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item.getDisplayName());
                    }
                }
            };
            cell.setOnMouseClicked(event->{
                if (! cell.isEmpty() && cell.isSelected()){
                    TreeItem<TreeModel> treeItem = cell.getTreeItem();
                    TreeModel treeModel = treeItem.getValue();
                    Map<ExperimentSearchFields,String> filters = new HashMap<>();
                    if(treeModel.getItemType().equals(TreeModel.ITEM_TYPE.PROJECT)){
                        filters.put(ExperimentSearchFields.PROJECT_ID, treeModel.getItemId());
                        tabbedPane.getTabs().get(0).setText(treeModel.getDisplayName());
                        updateExperimentList(filters,-1,0);
                    }else if(treeModel.getItemType().equals(TreeModel.ITEM_TYPE.RECENT_EXPERIMENTS)){
                        tabbedPane.getTabs().get(0).setText(treeModel.getDisplayName());
                        updateExperimentList(filters,-1,0);
                    }else if(event.getClickCount()==2 && treeModel.getItemType().equals(TreeModel.ITEM_TYPE.EXPERIMENT)){
                        try {
                            ExperimentSummaryWindow experimentSummaryWindow = new ExperimentSummaryWindow();
                            Parent parentNode = experimentSummaryWindow.getExperimentInfoNode(treeModel.getItemId());
                            Tab experimentTab = new Tab(treeModel.getDisplayName(),parentNode);
                            experimentTab.setClosable(true);
                            tabbedPane.getTabs().add(experimentTab);
                            tabbedPane.getSelectionModel().select(experimentTab);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            return cell;
        });

        TreeItem root = createProjectTreeModel();
        root.setExpanded(true);
        projectsTreeView.setRoot(root);
        projectsTreeView.setShowRoot(false);
    }

    //init the right pane with experiment list
    public void initExperimentList(){
        expSummaryTable.setEditable(true);

        expSummaryTable.setRowFactory(tv -> {
            TableRow<ExperimentListModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ExperimentListModel rowData = row.getItem();
                    try {
                        ExperimentSummaryWindow experimentSummaryWindow = new ExperimentSummaryWindow();
                        Parent parentNode = experimentSummaryWindow.getExperimentInfoNode(rowData.getId());
                        Tab experimentTab = new Tab(rowData.getName(),parentNode);
                        experimentTab.setClosable(true);
                        tabbedPane.getTabs().add(experimentTab);
                        tabbedPane.getSelectionModel().select(experimentTab);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            return row;
        });

        expCheckedColumn.setCellValueFactory(new PropertyValueFactory<>("checked"));
        expCheckedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(expCheckedColumn));
        expCheckedColumn.setEditable(true);
        expNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        expApplicationColumn.setCellValueFactory(cellData -> cellData.getValue().applicationProperty());
        expHostColumn.setCellValueFactory(cellData -> cellData.getValue().hostProperty());
        expStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        expStatusColumn.setCellFactory(new Callback<TableColumn<ExperimentListModel, String>,
                TableCell<ExperimentListModel, String>>() {
            @Override
            public TableCell<ExperimentListModel, String> call(TableColumn<ExperimentListModel, String> param) {
                return new TableCell<ExperimentListModel, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item);
                        if(!empty){
                            if (item.equals(ExperimentState.COMPLETED.toString())) {
                                this.setTextFill(Color.GREEN);
                            }else if(item.equals(ExperimentState.FAILED.toString())){
                                this.setTextFill(Color.RED);
                            }else if(item.equals(ExperimentState.CREATED.toString())){
                                this.setTextFill(Color.BLUE);
                            }else{
                                this.setTextFill(Color.ORANGE);
                            }
                        }

                    }
                };
            }
        });
        expCreateTimeColumn.setCellValueFactory(cellData -> cellData.getValue().createdTimeProperty());

        checkAllExps.setOnMouseClicked(event -> handleCheckAllExperiments());

        Map<ExperimentSearchFields,String> filters = new HashMap<>();
        tabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        tabbedPane.getTabs().get(0).setText("Recent Experiments");
        tabbedPane.getTabs().get(0).setClosable(false);
        updateExperimentList(filters,-1,0);
    }

    public void handleCheckAllExperiments(){
        if(checkAllExps.isSelected()){
            for(ExperimentListModel experimentListModel : expSummaryTable.getItems()){
                experimentListModel.setChecked(true);
            }
        }else{
            for(ExperimentListModel experimentListModel : expSummaryTable.getItems()){
                experimentListModel.setChecked(false);
            }
        }
    }

    //update the right pane with experiment list
    public void updateExperimentList(Map<ExperimentSearchFields, String> filters, int limit, int offset){
        try {
            List<ExperimentSummaryModel> experimentSummaryModelList = AiravataManager.getInstance()
                    .getExperimentSummaries(filters, limit, offset);
            observableExperimentList = FXCollections.observableArrayList();
            for(ExperimentSummaryModel expModel : experimentSummaryModelList){
                ExperimentListModel expFXModel = new ExperimentListModel(expModel);
                observableExperimentList.add(expFXModel);
            }
            //Set the filter Predicate whenever the filter changes.
            FilteredList<ExperimentListModel> filteredExpSummaryData = new FilteredList<>(observableExperimentList, p -> true);
            filterField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredExpSummaryData.setPredicate(experiment -> {
                    // If filter text is empty, display all persons.
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Compare first name and last name of every person with filter text.
                    String lowerCaseFilter = newValue.toLowerCase();

                    if (experiment.getName().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches first name.
                    } else if (experiment.getApplication().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches last name.
                    } else if (experiment.getHost().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches last name.
                    } else if (experiment.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches last name.
                    }
                    return false; // Does not match.
                });
            });
            SortedList<ExperimentListModel> sortedExperimentListData = new SortedList<>(filteredExpSummaryData);
            sortedExperimentListData.comparatorProperty().bind(expSummaryTable.comparatorProperty());
            expSummaryTable.setItems(sortedExperimentListData);

            filterField.setText("");
            tabbedPane.getSelectionModel().select(0);
        } catch (AiravataClientException e) {
            e.printStackTrace();
        }
    }


    //Creates the project tree model
    private TreeItem createProjectTreeModel(){

        TreeItem root = new TreeItem();
        TreeItem recentExps = new ProjectTreeItem(
                new TreeModel(TreeModel.ITEM_TYPE.RECENT_EXPERIMENTS,"no-id","Recent Experiments"));
        root.getChildren().add(recentExps);

        TreeItem projectRoot = new TreeItem<TreeModel>(
                new TreeModel(TreeModel.ITEM_TYPE.PROJECT_ROOT_NODE,"no-id","Projects")){
            {
                SEAGridEventBus.getInstance().register(this);
            }

            private boolean isFirstTimeChildren = true;

            @Subscribe
            public void handleSEAGridEvent(SEAGridEvent event) {
                if(event.getEventType().equals(SEAGridEvent.SEAGridEventType.PROJECT_CREATED)){
                    Project project = (Project)event.getPayload();
                    getChildren().add(0, new ProjectTreeItem(new TreeModel(TreeModel.ITEM_TYPE.PROJECT,
                            project.getProjectID(),project.getName())));
                }
            }

            @Override
            public boolean isLeaf() {
                return false;
            }

            @Override
            public ObservableList<TreeItem<TreeModel>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    ObservableList<TreeItem<TreeModel>> projChildern = FXCollections.observableArrayList();
                    List<Project> projects;
                    try {
                        projects = AiravataManager.getInstance().getProjects();
                        projChildern.addAll(projects.stream().map(project -> new ProjectTreeItem(
                                new TreeModel(
                                        TreeModel.ITEM_TYPE.PROJECT, project.getProjectID(),
                                        project.getName()
                                ))).collect(Collectors.toList()));
                    } catch (AiravataClientException e) {
                        e.printStackTrace();
                    }
                    super.getChildren().setAll(projChildern);
                }
                return super.getChildren();
            }
        };
        root.getChildren().add(projectRoot);

        return root;
    }

}