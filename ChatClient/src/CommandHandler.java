/* Command Handler class to handle incoming commands from an IRC server and also local commands issued by a user
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

import javax.swing.*;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;

public class CommandHandler {
    private ServerConnection serverConnection;

    private ArrayList<String> listItems = new ArrayList<>();
    public CommandHandler(ServerConnection serverConnection)
    {
        this.serverConnection = serverConnection;
    }

    public void HandleServerCommand(String command, ChatWindow chatWindow)
    {
        System.out.println(command);
        String source = "";
        String commandStart;
        if(command.startsWith(":")) {
            source = command.substring(0, command.indexOf(' '));;
            command = command.substring(command.indexOf(' ') + 1);
        }

        if (command.contains(" "))
            commandStart = command.substring(0, command.indexOf(' '));
        else
            commandStart = command;

        if(commandStart.chars().allMatch( Character::isDigit )) //If reply is number code
        {
            switch (Integer.parseInt(commandStart)) {
                case Replies.IRC_RPL_WELCOME:
                    serverConnection.ProcessPendingLogin(ServerConnection.LoginResult.SUCCESS);
                    PrintFormatted(command, chatWindow);
                    break;
                case Replies.IRC_ERR_NICKNAMEINUSE:
                    serverConnection.ProcessPendingLogin(ServerConnection.LoginResult.NAME_IN_USE);
                    PrintFormatted(command, chatWindow);
                    JOptionPane.showMessageDialog(null, "Sorry, that nickname is already in use!");
                    serverConnection.Disconnect();
                    System.exit(0); //Exit program
                    break;
                case Replies.IRC_ERR_ERRONEUSNICKNAME:
                    serverConnection.ProcessPendingLogin(ServerConnection.LoginResult.BAD_NAME);
                    PrintFormatted(command, chatWindow);
                    break;
                case Replies.IRC_RPL_BOUNCE:
                    serverConnection.ProcessCallerID(command);
                    PrintFormatted(command, chatWindow);
                    break;
                case 479:
                    serverConnection.ProcessPendingJoin(ServerConnection.ChannelJoinResult.ILLEGAL_NAME, "");
                    break;
                case 353: case 366://Ignore channel user list start and channel user list end
                    break;
                case 321: //List start
                    PrintFormatted(command, chatWindow); //Print command to the chat window
                    listItems.clear(); //clear list
                    break;
                case 322: //List item
                    PrintFormatted(command, chatWindow); //Print command to the chat window
                    listItems.add(command); //Add item to list
                    break;
                case 323: //List end
                    PrintFormatted(command, chatWindow); //Print command to the chat window
                    ListWindow.HandleList(listItems); //Process/display list
                    break;
                default:
                    PrintFormatted(command, chatWindow); //Print command to the chat window
            }
        }
        else
        {
            switch (commandStart.toLowerCase()) {
                case "ping":
                    serverConnection.Send("PONG " + command.substring(command.indexOf(' ') + 1));
                    break;
                case "privmsg":
                    chatWindow.ProcessChat(command, source);
                    break;
                case "join":
                    serverConnection.ProcessPendingJoin(ServerConnection.ChannelJoinResult.SUCCESS, command.substring(command.indexOf(":") + 1));
                    break;
                case "part": //Ignore channel leave
                    break;
                case "squit":
                    serverConnection.Disconnect();
                    break;
                default:
                    PrintFormatted(command, chatWindow);
            }
        }
    }

    public void PrintFormatted(String command, ChatWindow chatWindow)
    {
        String commandStart;
        if(command.startsWith(":"))
            command = command.substring(command.indexOf(' ') + 1);

        if (command.contains(" "))
            commandStart = command.substring(0, command.indexOf(' '));
        else
            commandStart = command;
        if(commandStart.chars().allMatch( Character::isDigit )) //If reply is number code
        {
            command = command.substring(command.indexOf(' ', command.indexOf(' ') + 1) + 1);
            if(command.startsWith(":"))
                command = command.substring(1);
        }
        //command = command.substring(command.indexOf(":", 1) + 1);
        chatWindow.WriteLine(command);
    }

    public void HandleClientCommand(String command, String sourceTab)
    {
        if(command.startsWith("/"))
        {
            command = command.substring(1); //Remove / from command
            String start;
            String params[] = new String[0];
            if(command.contains(" ")) {
                start = command.substring(0, command.indexOf(" ")); //Get command name
                command = command.substring(command.indexOf(" ") + 1); //Remove command name to get parameters
                params = command.split(" ");
            }
            else
                start = command; //Get command name
            switch (start.toLowerCase())
            {
                case "join":
                    if(params.length > 0) {
                        serverConnection.JoinChannel(params[0], new CompletionHandler<String, ServerConnection>() {
                            @Override
                            public void completed(String channelName, ServerConnection attachment) {
                                ChatTab tab = attachment.chatWindow.CreateTab(channelName, false);
                                serverConnection.chatWindow.tabbedPane.setSelectedComponent(tab.container);
                            }

                            @Override
                            public void failed(Throwable exc, ServerConnection attachment) {
                                if (exc instanceof CustomExceptions.IllegalChannelNameException)
                                    serverConnection.chatWindow.WriteLineToTab("Failed to join channel! Illegal channel name!", "red", sourceTab);
                                else if (exc instanceof CustomExceptions.ChannelFullException)
                                    serverConnection.chatWindow.WriteLineToTab("Failed to join channel! Channel is full!", "red", sourceTab);
                            }
                        });
                    }
                    else
                        serverConnection.chatWindow.WriteLineToTab("Invalid channel specified! Use '/join #channelname'", "red", sourceTab);
                    break;
            }
        }
        else
            serverConnection.chatWindow.SendChat(command, sourceTab);
    }
}
