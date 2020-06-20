/* Chat Window class to display data received from an IRC server and provide easy user interface for a user to send data to an IRC server.
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

import javax.lang.model.type.NullType;
import javax.swing.*;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatWindow {
    private String title;
    private ServerConnection server;
    public JTabbedPane tabbedPane; //Container to hold all the chat tabs in
    private HashMap<String, ChatTab> chats = new HashMap<>(); //Dictionary of chats and their names
    private JFrame frame; //Container to hold and arrange all the elements

    public ChatWindow(String title, ServerConnection server){
        this.server = server;
        this.title = title;
        InitGUI();
    }

    public void CloseWindow()
    {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //Close window
    }

    /**
     * Method to create, position and style GUI elements.
     */
    private void InitGUI()
    {
        frame = new JFrame(title);    // make a frame
        frame.setSize(800, 400); //Set default size of the frame
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // make it close properly

        JPanel container = new JPanel(new GridBagLayout()); //Create container using a grid bag layout
        JPanel buttonContainer = new JPanel(new GridBagLayout()); //Create container for the side buttons
        GridBagConstraints c = new GridBagConstraints(); //Layout constraints for the container

        c.fill = GridBagConstraints.HORIZONTAL; //Autosize horizontally
        c.weightx = 1; //Make element take up 100% of available fill space
        c.weighty = 0; //Dont autosize vertically
        c.gridx = 0; //Grid x position 0
        c.insets = new Insets(3,3,3,3); //Padding
        c.anchor = GridBagConstraints.WEST; //Anchor elements to the left of the window

        JButton joinChannelButton = new JButton("Join Channel"); //Create join channel button
        c.gridy = 0; //Grid y position 0
        buttonContainer.add(joinChannelButton, c); //Add join channel button to the button container using the specified grid bag constraints
        joinChannelButton.addActionListener((ActionEvent e) -> { //Add click event handler to the private message button
            AskUsernameWindow.getNick(new CompletionHandler<String, NullType>() { //Async callback for when user finishes entering nickname to chat with
                @Override
                public void completed(String result, NullType attachment) { //Completion callback for getting nickname from user
                    server.JoinChannel(result, new CompletionHandler<String, ServerConnection>() { //Begin async channel join
                        @Override
                        public void completed(String result, ServerConnection attachment) { //Completed handler for channel join
                            ChatTab tab = attachment.chatWindow.CreateTab(result, false); //Create/get tab for the channel
                            tabbedPane.setSelectedComponent(tab.container); //Select the tab
                        }

                        @Override
                        public void failed(Throwable exc, ServerConnection attachment) { //Failed handler for the channel connection
                            if (exc instanceof CustomExceptions.IllegalChannelNameException) //If the error is because the channel name is illegal
                                WriteLine("Failed to join channel! Illegal channel name!", "red");
                            else if (exc instanceof CustomExceptions.ChannelFullException) //If the error is because the channel is full
                                WriteLine("Failed to join channel! Channel is full!", "red");
                        }
                    });
                }

                @Override
                public void failed(Throwable exc, NullType attachment) {

                }
            }, true);
        });

        JButton privMsgButton = new JButton("Private msg");
        c.gridy = 1; //Grid y position 1
        buttonContainer.add(privMsgButton, c);
        privMsgButton.addActionListener((ActionEvent e) -> { //Add click event handler to the private message button
            AskUsernameWindow.getNick(new CompletionHandler<String, NullType>() { //Async callback for when user finishes entering nickname to chat with
                @Override
                public void completed(String result, NullType attachment) {
                    if(chats.containsKey(result))//If the chat is already open
                        tabbedPane.setSelectedComponent(chats.get(result).container); //Make the specified tab selected/active
                    else {
                        JPanel cont = CreateTab(result, false).container; //Create nickname tab, set to non deault chat tab
                        tabbedPane.setSelectedComponent(cont); //Set selected tab
                    }
                }

                @Override
                public void failed(Throwable exc, NullType attachment) {

                }
            }, false);
        });


        JButton helpButton = new JButton("Help"); //New help button
        c.gridy = 2; //Grid y position 2
        buttonContainer.add(helpButton, c);
        helpButton.addActionListener((ActionEvent e) -> { //Click event handler
            tabbedPane.setSelectedIndex(0);
            server.Send("HELP ");
        });

        JButton infoButton = new JButton("Info");
        c.gridy = 3; //Grid Y position 3
        buttonContainer.add(infoButton, c);
        infoButton.addActionListener((ActionEvent e) -> { //Click event handler
            tabbedPane.setSelectedIndex(0);
            server.Send("INFO ");
        });

        JButton motdButton = new JButton("MOTD");
        c.gridy = 4; //Grid Y position 4
        buttonContainer.add(motdButton, c);
        motdButton.addActionListener((ActionEvent e) -> { //Click event handler
            tabbedPane.setSelectedIndex(0); //Set the selected tab to the network tab
            server.Send("MOTD "); //Send the MOTD command
        });

        JButton listUserButton = new JButton("List users");
        c.gridy = 5; //Grid Y position 5
        buttonContainer.add(listUserButton, c);
        listUserButton.addActionListener((ActionEvent e) -> { //Click event handler
            tabbedPane.setSelectedIndex(0); //Set the selected tab to the network tab
            server.Send("USERS "); //Send list users command
        });

        JButton listChannelsButton = new JButton("List channels");
        c.gridy = 6; //Grid Y position 6
        buttonContainer.add(listChannelsButton, c);
        listChannelsButton.addActionListener((ActionEvent e) -> { //Click event handler
            tabbedPane.setSelectedIndex(0); //Set the selected tab to the network tab
            server.Send("LIST "); //Send list channels command
        });

        JButton disconnectButton = new JButton("Disconnect");
        c.gridy = 8; //Grid Y position 8
        buttonContainer.add(disconnectButton, c);
        disconnectButton.addActionListener((ActionEvent e) -> { //Click event handler
            tabbedPane.setSelectedIndex(0); //Set the selected tab to the server tab
            server.Send("QUIT :Chat client closed."); //Send quit to the IRC server
            server.Disconnect(); //Disconnect from the server
            System.exit(0); //Exit program
        });

        c.fill = GridBagConstraints.NONE; //Dont autosize
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTH;

        container.add(buttonContainer, c);

        tabbedPane = new JTabbedPane();
        CreateTab("default", true);
        c.fill = GridBagConstraints.BOTH; //Autosize horizontally and vertically
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 1;
        c.gridy = 0;
        container.add(tabbedPane, c);

        frame.add(container); //Add container to the window
        frame.pack();                                        // pack things in to the frame
        frame.setVisible(true); //Show the window
    }

    /**
     * Generate new chat tab for an individual chat
     * @param name Name of chat target
     * @param defaultChat Is this chat tab the default server chat
     * @return The tab that was created
     */
    public ChatTab CreateTab(String name, boolean defaultChat)
    {
        name = name.toLowerCase();
        if(chats.containsKey(name))
            return chats.get(name);
        ChatTab tab = new ChatTab(name, server, defaultChat); //Create new tab object
        chats.put(name, tab); //Add the tab to the chats dictionary
        tabbedPane.addTab(name, tab.container); //Add the tab to the GUI chats tabbed pane
        return tab; //Return the new tab
    }

    /**
     * Leaves a channel or chat
     * @param name Name of chat to leave
     */
    public void RemoveTab(String name)
    {
        name = name.toLowerCase();
        if(name.startsWith("#")) //Is a channel, if so, leave the channel
            server.LeaveChannel(name);
        tabbedPane.remove(chats.get(name).container); //Remove the chat tab from the GUI tabbed pane
        chats.remove(name); //Remove the chat from the chats dictionary
    }

    /**
     * Set the title of the default network chat tab
     * @param name
     */
    public void SetNetworkName(String name)
    {
        tabbedPane.setTitleAt(0, name); //Set tab name at index 0
    }

    /**
     * Process incoming chat messages into the respective tab
     * @param command Command sent by server
     * @param source Source of the command
     */
    public void ProcessChat(String command, String source)
    {
        command = command.substring(command.indexOf(" ") + 1); //Remove first command parameter
        String nick = source.substring(1, source.indexOf("!~")); //Get nickname from source
        String message = command.substring(command.indexOf(":") + 1); //Get the message from the command extended parameter
        String dest = command.substring(0, command.indexOf(":") - 1).toLowerCase(); //Get the destination (usually a channel)

        if(!dest.startsWith("#")) //If the destination is not a channel, eg, a private chat
            dest = nick; //Set the destination to a personal nickname
        ChatTab tab; //Chat to send the message to
        if(!chats.containsKey(dest)) //If the chat already exists
            tab = CreateTab(dest, false); //Get the existing chat
        else //If the chat doesnt exist
            tab = GetTab(dest); //Create a tab for the new chat
        try {
            //Insert the html formatted message into the respective chat tab
            tab.editorKit.insertHTML(tab.htmlDocument, tab.htmlDocument.getLength(), "<font color='blue'>" + nick + "</font>: " + message + "<br>", 0, 0, HTML.getTag("font"));
        }
        catch (Exception ex)
        {

        }
    }

    /**
     * Send a chat message to a specified destination and display the message to the specified chat
     * @param message Message to send
     * @param source The source tab name for where to send the message and what tab to display the message
     */
    public void SendChat(String message, String source)
    {
        source = source.toLowerCase();
        server.Send("PRIVMSG " + source + " :" + message); //Send privmsg command to IRC server
        ChatTab tab; //Tab to display the sent message in
        if(!chats.containsKey(source)) //If the chat already exists
            tab = CreateTab(source, false); //Get the existing chat
        else //If the chat doesnt exist yet
            tab = GetTab(source); //Create the chat
        try { //Write chat to window
            tab.editorKit.insertHTML(tab.htmlDocument, tab.htmlDocument.getLength(), "<font color='red'>" + server.nickName+ "</font>: " + message + "<br>", 0, 0, HTML.getTag("font"));
        }
        catch (Exception ex)
        {

        }
    }

    public void WriteLineToTab(String message, String color, String tabName)
    {
        tabName = tabName.toLowerCase();
        ChatTab tab;
        if(!chats.containsKey(tabName))
            tab = CreateTab(tabName, false);
        else
            tab = GetTab(tabName);
        try { //Write chat to window
            tab.editorKit.insertHTML(tab.htmlDocument, tab.htmlDocument.getLength(), "<font color='" + color + "'>" + message + "</font><br>", 0, 0, HTML.getTag("font"));
        }
        catch (Exception ex)
        {

        }
    }

    public void WriteLine(String message, String color)
    {
        try
        {
            ChatTab tab = GetDefaultTab(); //Get default network chat tab
            tab.editorKit.insertHTML(tab.htmlDocument, tab.htmlDocument.getLength(), "<font color='" + color + "'>" + message + "</font><br>", 0, 0, HTML.getTag("font"));
        }
        catch (Exception ex) { }
    }

    public void Write(String message, String color)
    {
        try
        {
            ChatTab tab = GetDefaultTab(); //Get default network chat tab
            tab.editorKit.insertHTML(tab.htmlDocument, tab.htmlDocument.getLength(), "<font color='" + color + "'>" + message + "</font>", 0, 0, HTML.getTag("font"));
        }
        catch (Exception ex) { }
    }

    private ChatTab GetDefaultTab()
    {
        return chats.get("default");
    }

    private ChatTab GetTab(String name)
    {
        name = name.toLowerCase();
        return chats.get(name);
    }

    public void Write(String message)
    {
        Write(message, "black");
    }

    public void WriteLine(String message)
    {
        WriteLine(message, "black");
    }
}
