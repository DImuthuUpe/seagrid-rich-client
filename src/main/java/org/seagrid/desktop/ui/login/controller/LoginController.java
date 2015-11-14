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
package org.seagrid.desktop.ui.login.controller;

import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.seagrid.desktop.connectors.wso2is.AuthResponse;
import org.seagrid.desktop.connectors.wso2is.AuthenticationException;
import org.seagrid.desktop.connectors.wso2is.AuthenticationManager;
import org.seagrid.desktop.ui.commons.SEAGridDialogHelper;
import org.seagrid.desktop.util.SEAGridContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;


public class LoginController {
    private final static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label loginAuthFailed;

    @FXML
    private Hyperlink dontHaveAccountLink;

    public void initialize() {
        loginButton.disableProperty().bind(new BooleanBinding() {
            {super.bind(passwordField.textProperty(),usernameField.textProperty());}
            @Override
            protected boolean computeValue() {
                return usernameField.getText().isEmpty() || passwordField.getText().isEmpty();
            }
        });
        loginButton.setOnMouseClicked(event -> {
            handleLogin();
        });
        passwordField.setOnAction(event->{if(!usernameField.getText().isEmpty() && !passwordField.getText().isEmpty())
            handleLogin();});
        usernameField.setOnAction(event->{if(!usernameField.getText().isEmpty() && !passwordField.getText().isEmpty())
            handleLogin();});

        dontHaveAccountLink.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(new URI("https://seagrid.org/portal/public/create"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    public boolean handleLogin(){
        String username = usernameField.getText();
        String password = passwordField.getText();
        AuthenticationManager authenticationManager = new AuthenticationManager();
        try {
            AuthResponse authResponse = authenticationManager.authenticate(username,password);
            if(authResponse != null){
                SEAGridContext.getInstance().setAuthenticated(true);
                SEAGridContext.getInstance().setUserName(username);
                SEAGridContext.getInstance().setOAuthToken(authResponse.getAccess_token());
                SEAGridContext.getInstance().setRefreshToken(authResponse.getAccess_token());
                SEAGridContext.getInstance().setTokenExpiaryTime(authResponse.getExpires_in() * 1000
                        + System.currentTimeMillis());
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.close();
            }else{
                loginAuthFailed.setText("Authentication Failed !");
                loginAuthFailed.setFont(new Font(10));
                loginAuthFailed.setTextFill(Color.RED);
                passwordField.setText("");
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
            SEAGridDialogHelper.showExceptionDialog(e,"Exception Dialog", loginButton.getScene().getWindow(),
                    "Login operation failed !");
        }
        return false;
    }
}