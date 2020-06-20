/* Connection Manager class to facilitate simultaneously connecting to multiple IRC servers.
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

import javax.lang.model.type.NullType;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;

public class ConnectionManager {
    public static ArrayList<ServerConnection> ServerConnections = new ArrayList<>(); //Where each server connection is stored.z

    public static void ConnectWithNickname(String server)
    {
        AskUsernameWindow.getServerNames(new CompletionHandler<AskUsernameWindow.UsernameResult, NullType>() {
            @Override
            public void completed(AskUsernameWindow.UsernameResult result, NullType attachment) {
                ConnectionManager.ConnectServer(server, result.Nick, result.Username, result.Realname);
            }

            @Override
            public void failed(Throwable exc, NullType attachment) {

            }
        });
    }

    public static void ConnectServer(String host, String nick, String username, String realname) //Connect to a server. Name format is host:port
    {
        ServerConnection conn = new ServerConnection(host);
        ServerConnections.add(conn); //Create new server connection instance and add it to the list of servers.
        conn.Connect(new CompletionHandler<Void, ServerConnection>() { //Connect to the server and specify an async callback for completion and failure
            @Override
            public void completed(Void result, ServerConnection attachment) { //Connection completed successfully
                attachment.chatWindow.WriteLine("Server connected: " + attachment.Name, "green");
                CompletionHandler<ServerConnection.LoginResult, ServerConnection> loginCallback = new CompletionHandler<ServerConnection.LoginResult, ServerConnection>() { //Async callback for server login
                    @Override
                    public void completed(ServerConnection.LoginResult result, ServerConnection attachment) { //Login completed successfully
                        attachment.chatWindow.WriteLine("Logged in!", "green");
                    }

                    @Override
                    public void failed(Throwable exc, ServerConnection attachment) { //Login failed
                        if(exc instanceof CustomExceptions.NameTakenException) //Name taken
                            attachment.chatWindow.WriteLine("Login failed, name taken: " + exc, "red");
                        else if(exc instanceof  CustomExceptions.BadNameException) //Illegal name
                            attachment.chatWindow.WriteLine("Login failed, erroneous name: " + exc, "red");
                        else //Generic failure
                            attachment.chatWindow.WriteLine("Login failed: " + exc, "red");
                    }
                };
                attachment.Login(nick, username, realname, false, loginCallback);
            }

            @Override
            public void failed(Throwable exc, ServerConnection attachment) { //Connection failed
                attachment.chatWindow.WriteLine ("Failed to connect to server: " + attachment.Name + " - " + exc, "red");//Print failed connection and the server that failed
            }
        });
    }
}
