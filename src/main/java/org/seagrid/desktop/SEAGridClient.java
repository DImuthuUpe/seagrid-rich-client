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
package org.seagrid.desktop;

import javafx.application.Application;
import javafx.stage.Stage;
import org.seagrid.desktop.ui.home.HomeWindow;
import org.seagrid.desktop.ui.login.LoginWindow;
import org.seagrid.desktop.util.SEAGridConfig;
import org.seagrid.desktop.util.SEAGridContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SEAGridClient extends Application{
    private final static Logger logger = LoggerFactory.getLogger(SEAGridClient.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        LoginWindow loginWindow =  new LoginWindow();
        loginWindow.displayAndWait();
        String isAuthenticated = SEAGridContext.getInstance().getProperty(SEAGridConfig.AUTHENTICATED);
        if(isAuthenticated!=null && isAuthenticated.equalsIgnoreCase("true")){
            HomeWindow homeWindow =  new HomeWindow();
            homeWindow.start(primaryStage);
        }
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }
}