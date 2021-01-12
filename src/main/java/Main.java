import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibValueSymbol;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;



public class Main extends Application {

    private File[] listOfFiles;

    @Override
    public void start(Stage primaryStage) throws Exception {
        SNMP snmp = SNMP.getInstance();
        File folder = new File("mibFiles/ExtraMibs");
        listOfFiles = folder.listFiles();
        System.out.println(Arrays.toString(listOfFiles));
        Stage stage = new Stage();
        stage.setTitle("SNMP-Tool");
        GridPane tabelle = new GridPane();
        MenuItem[] items = new MenuItem[listOfFiles.length];
        MenuButton mibfiles = new MenuButton("MIB-Files");
        for (int i = 0; i < listOfFiles.length; i++) {
            String tmp = listOfFiles[i].getName();
            items[i] = new MenuItem(tmp);
            items[i].setId("item" + i);
            items[i].setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    Scene aktuellescene = (Scene) mibfiles.getScene();
                    GridPane root = new GridPane();
                    final TextArea textArea = TextAreaBuilder.create().prefWidth(400).wrapText(true).build();
                    try {
                        MibSymbol[] mibSymbols= snmp.getMibSymbols(tmp);
                        for (MibSymbol v : mibSymbols)
                        {
                            if(v instanceof MibValueSymbol)
                                textArea.appendText("Name: " + v.getName() + "\t\tValue: " + ((MibValueSymbol)v).getValue() + "\n");
                            else
                                textArea.appendText("Name: " + v.getName() + "\t\tValue: NULL" + "\n");
                        }
                    } catch (MibLoaderException | IOException e) {
                        System.out.println("can not find file specified.\nTry to use the command mib to show all available mib files");
                    }
                    textArea.setEditable(false);
                    ScrollPane scrollPane = new ScrollPane();
                    scrollPane.setContent(textArea);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setPrefWidth(500);
                    scrollPane.setPrefHeight(180);
                    Button back = new Button("Zurück");
                    back.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            stage.setScene(aktuellescene);
                        }
                    });
                    root.add(scrollPane, 0,0);
                    root.add(back, 0, 1);
                    back.setPrefSize(100,50);
                    GridPane.setMargin(scrollPane, new Insets(25,0,25,150));
                    GridPane.setMargin(back, new Insets(0,0,25,350));
                    stage.setScene(new Scene(root, 800, 500));

                }
            });
        }
        mibfiles.getItems().addAll(items);
        Button get = new Button("Get Informations");
        get.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Scene aktuellescene = (Scene) mibfiles.getScene();
                GridPane root = new GridPane();
                TextField ipaddress = new TextField();
                ipaddress.setPromptText("Enter IP-address");
                TextField operation = new TextField();
                operation.setPromptText("Enter Operation");
                root.add(ipaddress, 0,0);
                root.add(operation,1,0);
                Label manual = new Label("Insert your ip-address in the first input-field\n Insert your OID in the second input-field \n If you don't know the OID you can insert the name of the operation and the .mib file\nFor example sysName Mikrotik.mib");
                root.add(manual, 0,1);
                GridPane.setColumnSpan(manual,2);
                Label response = new Label();
                Button start = new Button("Start");
                start.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        InetAddress ip = null;
                        int port = 161;
                        String community = "public";
                        OID oid = null;
                        int version = SnmpConstants.version1;
                        try {
                            ip = Inet4Address.getByName(ipaddress.getText());

                        }catch(NullPointerException | UnknownHostException e) {
                            JOptionPane.showMessageDialog(null,"wrong input. Enter help to see the available commands and how to use them");
                            }

                        if(oid == null) {
                           char[] c = operation.getText().toCharArray();

                                if(Character.isLetter(c[1])) {
                                    try {
                                        String[] tmp = operation.getText().split(" ");
                                        oid = snmp.getOidFromName(tmp[0], tmp[1]);
                                    } catch (MibLoaderException e) {
                                        e.printStackTrace();
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                }else {
                                    oid = new OID(operation.getText());
                                }
                        }

                        IpAddress addre = new UdpAddress(ip, port);
                        ResponseEvent getResponse = null;
                        try {
                            getResponse = snmp.get(addre, community, version, oid);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(getResponse == null || getResponse.getResponse() == null)
                            JOptionPane.showMessageDialog(null, "no response from " + ip.getHostAddress() + "\ntry to use the command \"discovery\" to see all Devices in your network");
                        else if(getResponse.getResponse().getErrorStatus() != 0)
                            JOptionPane.showMessageDialog(null, "Error: " + getResponse.getResponse().getErrorStatus() );
                        else
                            response.setText("response from " + getResponse.getPeerAddress().toString() + ": " + oid.toString() + " ==> " + getResponse.getResponse().getVariableBindings().iterator().next().getVariable());
                    }
                });
                Button back = new Button("Zurück");
                back.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        stage.setScene(aktuellescene);
                    }
                });
                root.add(start, 0, 2);
                root.add(response,0,3);
                GridPane.setColumnSpan(response,2);
                root.add(back, 0, 4);
                back.setPrefSize(100,50);
                ipaddress.setPrefSize(250,25);
                operation.setPrefSize(250,25);
                manual.setPrefWidth(500);
                start.setPrefSize(100,25);
                response.setPrefWidth(500);
                GridPane.setMargin(ipaddress, new Insets(25,100,25,100));
                GridPane.setMargin(operation, new Insets(25,0,25,0));
                GridPane.setMargin(manual, new Insets(0,0,25,150));
                GridPane.setMargin(start, new Insets(0,0,25,350));
                GridPane.setMargin(response, new Insets(0,0,25,150));
                GridPane.setMargin(back, new Insets(0,0,25,350));
                Scene scene = new Scene(root, 800, 500);
                stage.setScene(scene);
            }
        });
        Button set = new Button("Set Informations");
        set.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Scene aktuellescene = (Scene) mibfiles.getScene();
                GridPane root = new GridPane();
                TextField ipaddress = new TextField();
                ipaddress.setPromptText("Enter IP-address");
                TextField operation = new TextField();
                operation.setPromptText("Enter Operation");
                TextField newvalue = new TextField();
                newvalue.setPromptText("Enter Value");
                root.add(ipaddress, 0,0);
                root.add(operation,1,0);
                root.add(newvalue,2,0);
                Label manual = new Label("Insert your ip-address in the first input-field\n Insert your OID in the second input-field \n If you don't know the OID you can insert the name of the operation and the .mib file\nFor example sysName Mikrotik.mib\nInsert your new value you wish to replace");
                root.add(manual, 0,1);
                GridPane.setColumnSpan(manual,3);
                Button start = new Button("Start");
                start.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        InetAddress ip = null;
                        int port = 161;
                        String community = "public";
                        OID oid = null;
                        int version = SnmpConstants.version1;
                        Variable value = null;
                        try {
                            ip = Inet4Address.getByName(ipaddress.getText());



                                 if(newvalue.getText()!=null) {
                                    value = new OctetString(newvalue.getText());
                                 } else {
                                     JOptionPane.showMessageDialog(null,"invalid input");
                                 }
                        } catch(NullPointerException | UnknownHostException e) {
                            JOptionPane.showMessageDialog(null,"wrong input. Enter help to see the available commands and how to use them");
                        }

                        if(oid == null) {
                            char[] c = operation.getText().toCharArray();

                            if(Character.isLetter(c[1])) {
                                try {
                                    String[] tmp = operation.getText().split(" ");
                                    oid = snmp.getOidFromName(tmp[0], tmp[1]);
                                } catch (MibLoaderException e) {
                                    e.printStackTrace();
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                            }else {
                                oid = new OID(operation.getText());
                            }
                        }
                        if(value == null)
                        {
                            JOptionPane.showMessageDialog(null,"value can not be empty, use the command \"help\" to see how to use this command");
                        }

                        IpAddress addre = new UdpAddress(ip, port);
                        try {
                            snmp.set(addre, community, version, oid, value);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Button back = new Button("Zurück");
                back.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        stage.setScene(aktuellescene);
                    }
                });
                root.add(start, 0, 2);
                GridPane.setColumnSpan(start,3);
                GridPane.setColumnSpan(back, 3);
                root.add(back, 0, 4);
                back.setPrefSize(100,50);
                ipaddress.setPrefSize(200,25);
                operation.setPrefSize(100,25);
                newvalue.setPrefSize(100,25);
                manual.setPrefWidth(500);
                start.setPrefSize(100,25);
                GridPane.setMargin(ipaddress, new Insets(25,50,25,50));
                GridPane.setMargin(operation, new Insets(25,50,25,0));
                GridPane.setMargin(manual, new Insets(0,0,25,150));
                GridPane.setMargin(start, new Insets(0,0,25,350));
                GridPane.setMargin(back, new Insets(0,0,25,350));
                Scene scene = new Scene(root, 800, 500);
                stage.setScene(scene);
            }

        });
        Button discover = new Button("Discover your network");
        discover.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Scene aktuellescene = (Scene) mibfiles.getScene();
                GridPane root = new GridPane();
                final TextArea textArea = TextAreaBuilder.create().prefWidth(400).wrapText(true).build();
                int version = SnmpConstants.version1;
                int port = 161;
                String community = "public";
                long timeout = 5000;

                LinkedList<Integer32> discoveryId = null;
                try {
                    discoveryId = snmp.discovery(community, version, timeout);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                long finalTimeout = timeout;
                LinkedList<Integer32> finalDiscoveryId = discoveryId;
                Runnable result = () -> {
                    try {
                        Thread.sleep(finalTimeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for(Integer32 tmp : finalDiscoveryId) {
                        ResponseEvent getResponse = snmp.getResponse(tmp.toInt());
                        if(getResponse != null)
                            textArea.appendText("discovered " + getResponse.getPeerAddress().toString() + ": " + getResponse.getResponse().getVariableBindings().iterator().next().getVariable() + "\n");

                    }

                };

                new Thread(result).start();
                textArea.setEditable(false);
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setContent(textArea);
                scrollPane.setFitToWidth(true);
                scrollPane.setPrefWidth(500);
                scrollPane.setPrefHeight(180);


                Button back = new Button("Zurück");

                back.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        stage.setScene(aktuellescene);
                    }
                });
                root.add(scrollPane,0,0);
                root.add(back, 0, 1);
                back.setPrefSize(100,25);
                GridPane.setMargin(scrollPane, new Insets(50,0,25,150));
                GridPane.setMargin(back, new Insets(0,0,25,350));
                Scene scene = new Scene(root, 800, 500);
                stage.setScene(scene);
            }
        });
        Button exit = new Button("Close SNMP-Tool");
        exit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    snmp.close();
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Platform.exit();
            }
        });
        tabelle.add(mibfiles, 0, 0);
        tabelle.add(get, 0, 1);
        tabelle.add(set,0,2);
        tabelle.add(discover,0,3);
        tabelle.add(exit,0,4);
        tabelle.setAlignment(Pos.CENTER);
        tabelle.autosize();
        BorderPane root =new BorderPane();
        root.setPadding(new Insets(0,0,25,25));
        root.setCenter(tabelle);
        mibfiles.setPrefSize(200,25);
        get.setPrefSize(200,25);
        set.setPrefSize(200,25);
        discover.setPrefSize(200,25);
        exit.setPrefSize(200,25);
        GridPane.setMargin(mibfiles, new Insets(0,0,25,0));
        GridPane.setMargin(get, new Insets(0,0,25,0));
        GridPane.setMargin(set, new Insets(0,0,25,0));
        GridPane.setMargin(discover, new Insets(0,0,25,0));
        GridPane.setMargin(exit, new Insets(0,0,25,0));
        Scene scene = new Scene(root, 800, 500);

        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        launch(args);
    }
}