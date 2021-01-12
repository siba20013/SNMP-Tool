import javafx.application.Application;
import javafx.stage.Stage;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibValueSymbol;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Scanner;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        /*SNMP snmp = SNMP.getInstance();

        Scanner sc = new Scanner(System.in);
        String[] consoleInput;

        mainLoop: while(true)
        {
            System.out.println("Enter Command");
            consoleInput = sc.nextLine().split(" ");
            consoleInput[0] = consoleInput[0].toLowerCase();
            if(consoleInput[0].compareTo("stop") == 0)
                break;
            else if(consoleInput[0].compareTo("cls") == 0)
            {
                String tmp = System.getProperty("os.name");
                try
                {
                    if(tmp.contains("Windows"))
                        Runtime.getRuntime().exec("cls");
                    else
                        Runtime.getRuntime().exec("clear");
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
                continue;
            }
            else if(consoleInput[0].compareTo("mib") == 0)
            {
                int i=0;
                String[] tmp = snmp.getMibFileNames();
                for(String str: tmp)
                    System.out.println(i++ + ")\t" + str);
                continue;
            }
            else if(consoleInput[0].compareTo("show") == 0) //print OID and names inside mib
            {
                try {
                    MibSymbol[] mibSymbols= snmp.getMibSymbols(consoleInput[1]);
                    for (MibSymbol v : mibSymbols)
                    {
                        if(v instanceof MibValueSymbol)
                            System.out.println("Name: " + v.getName() + "\t\tValue: " + ((MibValueSymbol)v).getValue());
                        else
                            System.out.println("Name: " + v.getName() + "\t\tValue: NULL");
                    }
                } catch (MibLoaderException | IOException e) {
                    System.out.println("can not find file specified.\nTry to use the command mib to show all available mib files");
                }
                continue;

            }
            else if(consoleInput[0].compareTo("get") == 0)
            {
                InetAddress ip;
                int port = 161;
                String community = "public";
                OID oid = null;
                int version = SnmpConstants.version1;
                try {
                    ip = Inet4Address.getByName(consoleInput[1]);

                    for (int i = 3; i < consoleInput.length - 1; i += 2) {
                        if (consoleInput[i].toLowerCase().compareTo("-n") == 0) {
                            try {
                                oid = snmp.getOidFromName(consoleInput[2],  consoleInput[i + 1]);
                            } catch (MibLoaderException e) {
                                e.printStackTrace();
                                return;
                            }
                        } else if (consoleInput[i].toLowerCase().compareTo("-v") == 0) {
                            version = Integer.parseInt(consoleInput[i + 1]);
                        } else if (consoleInput[i].toLowerCase().compareTo("-port") == 0) {
                            port = Integer.parseInt(consoleInput[i + 1]);
                        } else if (consoleInput[i].toLowerCase().compareTo("-c") == 0) {
                            community = consoleInput[i + 1];
                        } else {
                            System.out.println("invalid input");
                            continue mainLoop;
                        }
                    }
                }
                catch(NullPointerException e)
                {
                    System.out.println("wrong input. Enter help to see the available commands and how to use them");
                    continue mainLoop;
                }

                if(oid == null)
                {
                    for(char c : consoleInput[2].toCharArray())
                    {
                        if(Character.isLetter(c))
                        {
                            System.out.println("invalid OID input");
                            return;
                        }
                    }
                    oid = new OID(consoleInput[2]);
                }

                IpAddress addre = new UdpAddress(ip, port);
                ResponseEvent getResponse = snmp.get(addre, community, version, oid);

                if(getResponse == null || getResponse.getResponse() == null)
                    System.out.println("no response from " + ip.getHostAddress() + "\ntry to use the command \"discovery\" to see all Devices in your network");
                else if(getResponse.getResponse().getErrorStatus() != 0)
                    System.out.println("Error: " + getResponse.getResponse().getErrorStatus() );
                else
                    System.out.println("response from " + getResponse.getPeerAddress().toString() + ": " + oid.toString() + " ==> " + getResponse.getResponse().getVariableBindings().iterator().next().getVariable());

            }
            else if(consoleInput[0].compareTo("set") == 0)
            {
                InetAddress ip;
                int port = 161;
                String community = "public";
                OID oid = null;
                int version = SnmpConstants.version1;
                Variable value = null;
                try {
                    ip = Inet4Address.getByName(consoleInput[1]);

                    for (int i = 3; i < consoleInput.length - 1; i += 2) {
                        if (consoleInput[i].toLowerCase().compareTo("-n") == 0) {
                            try {
                                oid = snmp.getOidFromName(consoleInput[2], consoleInput[i + 1]);
                            } catch (MibLoaderException e) {
                                e.printStackTrace();
                                return;
                            }
                        } else if (consoleInput[i].toLowerCase().compareTo("-v") == 0) {
                            version = Integer.parseInt(consoleInput[i + 1]);
                        } else if (consoleInput[i].toLowerCase().compareTo("-port") == 0) {
                            port = Integer.parseInt(consoleInput[i + 1]);
                        } else if (consoleInput[i].toLowerCase().compareTo("-c") == 0) {
                            community = consoleInput[i + 1];

                        }
                        else if(consoleInput[i].toLowerCase().compareTo("-integer") == 0)
                        {
                            if(value != null)
                            {
                                System.out.println("invalidInput: value already initialized");
                                break;
                            }
                            value = new Integer32(Integer.parseInt(consoleInput[i + 1]));
                        }
                        else if(consoleInput[i].toLowerCase().compareTo("-string") == 0)
                        {
                            if(value != null)
                            {
                                System.out.println("invalidInput: value already initialized");
                                break;
                            }
                            value = new OctetString(consoleInput[i + 1]);
                        }
                        else {
                            System.out.println("invalid input");
                            continue mainLoop;
                        }
                    }
                }
                catch(NullPointerException e)
                {
                    System.out.println("wrong input. Enter help to see the available commands and how to use them");
                    continue mainLoop;
                }

                if(oid == null)
                {
                    for(char c : consoleInput[2].toCharArray())
                    {
                        if(Character.isLetter(c))
                        {
                            System.out.println("invalid OID input");
                            return;
                        }
                    }
                    oid = new OID(consoleInput[2]);
                }
                if(value == null)
                {
                    System.out.println("value can not be empty, use the command \"help\" to see how to use this command");
                    continue mainLoop;
                }

                IpAddress addre = new UdpAddress(ip, port);
                snmp.set(addre, community, version, oid, value);
            }
            else if(consoleInput[0].compareTo("getnext") == 0)
            {
                InetAddress ip;
                int port = 161;
                String community = "public";
                OID oid = null;
                int version = SnmpConstants.version1;
                try {
                    ip = Inet4Address.getByName(consoleInput[1]);

                    for (int i = 3; i < consoleInput.length - 1; i += 2) {
                        if (consoleInput[i].toLowerCase().compareTo("-n") == 0) {
                            try {
                                oid = snmp.getOidFromName(consoleInput[2], consoleInput[i + 1]);
                            } catch (MibLoaderException e) {
                                e.printStackTrace();
                                return;
                            }
                        } else if (consoleInput[i].toLowerCase().compareTo("-v") == 0) {
                            version = Integer.parseInt(consoleInput[i + 1]);
                        } else if (consoleInput[i].toLowerCase().compareTo("-port") == 0) {
                            port = Integer.parseInt(consoleInput[i + 1]);
                        } else if (consoleInput[i].toLowerCase().compareTo("-c") == 0) {
                            community = consoleInput[i + 1];
                        } else {
                            System.out.println("invalid input");
                            continue mainLoop;
                        }
                    }
                }
                catch(NullPointerException e)
                {
                    System.out.println("wrong input. Enter help to see the available commands and how to use them");
                    continue mainLoop;
                }

                if(oid == null)
                {
                    for(char c : consoleInput[2].toCharArray())
                    {
                        if(Character.isLetter(c))
                        {
                            System.out.println("invalid OID input");
                            return;
                        }
                    }
                    oid = new OID(consoleInput[2]);
                }

                IpAddress addre = new UdpAddress(ip, port);
                ResponseEvent getResponse = snmp.getNext(addre, community, version, oid);

                if(getResponse == null)
                    System.out.println("no response from " + ip.getHostAddress() + "\ntry to use the command \"discovery\" to see all Devices in your network");
                else
                    System.out.println("response from " + getResponse.getPeerAddress().toString() + ": " + oid.toString() + " ==> " + getResponse.getResponse().getVariableBindings().iterator().next().getVariable());
            }
            else if(consoleInput[0].compareTo("discovery") == 0)
            {
                int version = SnmpConstants.version1;
                int port = 161;
                String community = "public";
                long timeout = 5000;
                try {
                    for (int i = 1; i < consoleInput.length - 1; i += 2) {
                        if (consoleInput[i].toLowerCase().compareTo("-v") == 0) {
                            version = Integer.parseInt(consoleInput[i + 1]);
                        } else if (consoleInput[i].toLowerCase().compareTo("-port") == 0) {
                            port = Integer.parseInt(consoleInput[i + 1]);
                        } else if (consoleInput[i].toLowerCase().compareTo("-c") == 0) {
                            community = consoleInput[i + 1];
                        } else if (consoleInput[i].toLowerCase().compareTo("-m") == 0) {
                            timeout = Integer.parseInt(consoleInput[i+1]) * 60 * 1000;
                        } else if (consoleInput[i].toLowerCase().compareTo("-s") == 0) {
                            timeout = Integer.parseInt(consoleInput[i+1]) * 1000;
                        } else {
                            System.out.println("invalid input");
                            continue mainLoop;
                        }
                    }
                }
                catch(NullPointerException e)
                {
                    System.out.println("wrong input. Enter help to see the available commands and how to use them");
                    continue mainLoop;
                }


                LinkedList<Integer32> discoveryId = snmp.discovery(community, version, timeout);

                long finalTimeout = timeout;
                Runnable result = () -> {
                    try {
                        Thread.sleep(finalTimeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for(Integer32 tmp : discoveryId)
                    {
                        ResponseEvent getResponse = snmp.getResponse(tmp.toInt());
                        if(getResponse != null)
                            System.out.println("discovered " + getResponse.getPeerAddress().toString() + ": " + getResponse.getResponse().getVariableBindings().iterator().next().getVariable());

                    }

                };

                new Thread(result).start();
            }
            else {
                System.out.println("enter \"help\" to see all the commands");
            }
        }

        snmp.close();
        System.exit(0);
    }
*/
}
}