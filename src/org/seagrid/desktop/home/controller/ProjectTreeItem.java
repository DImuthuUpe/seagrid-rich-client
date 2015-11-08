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
import javafx.scene.control.TreeItem;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.workspace.Project;
import org.seagrid.desktop.apis.airavata.AiravataManager;
import org.seagrid.desktop.events.SEAGridEvent;
import org.seagrid.desktop.events.SEAGridEventBus;
import org.seagrid.desktop.home.model.TreeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectTreeItem extends TreeItem<TreeModel> {
    private final static Logger logger = LoggerFactory.getLogger(ProjectTreeItem.class);

    private boolean isFirstTimeChildren = true;

    public ProjectTreeItem(TreeModel treeModel) {
        super(treeModel);
        SEAGridEventBus.getInstance().register(this);
    }

    @Subscribe
    public void handleSEAGridEvent(SEAGridEvent event) {
        if(event.getEventType().equals(SEAGridEvent.SEAGridEventType.EXPERIMENT_CREATED)){
            ExperimentModel experiment = (ExperimentModel)event.getPayload();
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
            ObservableList<TreeItem<TreeModel>> expChildren = FXCollections.observableArrayList();
            List<ExperimentSummaryModel> experiments = new ArrayList<>();
            try {
                if(getValue().getItemType().equals(TreeModel.ITEM_TYPE.PROJECT)){
                    experiments = AiravataManager.getInstance()
                            .getExperimentSummariesInProject(this.getValue().getItemId());
                }else if(getValue().getItemType().equals(TreeModel.ITEM_TYPE.RECENT_EXPERIMENTS)){
                    experiments = AiravataManager.getInstance().getRecentExperimentSummaries();
                }
                expChildren.addAll(experiments.stream().map(experimentModel -> new TreeItem<TreeModel>(
                        new TreeModel(
                                TreeModel.ITEM_TYPE.EXPERIMENT, experimentModel.getExperimentId(),
                                experimentModel.getName()
                        )) {
                }).collect(Collectors.toList()));
            } catch (AiravataClientException e) {
                e.printStackTrace();
            }
            super.getChildren().setAll(expChildren);
        }
        return super.getChildren();
    }
}