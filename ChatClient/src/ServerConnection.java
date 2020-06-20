/* Server connection class to manage, send and receive data from an IRC server.
* Name: Matthew Corfiatis
* Username: CorfiaMatt
* ID: 300447277
*/

import com.sun.jdi.connect.spi.ClosedConnectionException;

import javax.lang.model.type.NullType;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class ServerConnection {
    public enum LoginResult //Result of an async login request
    {
        NAME_IN_USE, //Nickname is in use
        BAD_NAME, //Erroneous nickname
        SUCCESS //Login succeeded
    }

    public enum ChannelJoinResult //Result of an async channel join request
    {
        FULL, //Channel is full
        SUCCESS, //Join success
        ILLEGAL_NAME, //Invalid name
    }

    //Public variables
    public String Name; //Name of server eg. irc.ecs.vuw.ac.nz:6667
    public ChatWindow chatWindow; //Chat window for this server connection. Each server connection gets a seperate chat window
    public CommandHandler commandHandler = new CommandHandler(this); //Command handler to handle commands from the server and user. Executes actions based on commands
    public String nickName; //Current nickname

    //Private variables
    private AsynchronousSocketChannel client; //Main TCP client that is connected to the IRC server
    private String host; //Host that this server connection connects to
    private int port; //Port for the IRC connection, in most cases, 6667
    private boolean connectFinished = false; //If the async connection has finished
    private boolean connectionSucceeded = false; //If the async connection succeeded
    private ServerConnection context = this; //This server connection, used as a callback parameter so different server connections can be opened simultaneously
    private StringBuilder readBuffer = new StringBuilder(); //Buffer for reading incoming messages.
    private String userName; //Current username
    private String realName; //Current realname
    private boolean hidden; //Usermode +i (Hides user from user lists)
    private CompletionHandler<LoginResult, ServerConnection> pendingLoginCallback; //Callback for a pending async login request
    private CompletionHandler<String, ServerConnection> pendingJoinCallback; //Callback for a pending async channel join request
    private String networkName; //Network name of the server eg "EFNet"

    /**
     *
     * @param name connection name to use eg host:port. If no port is specified, use 6667
     */
    public ServerConnection(String name) //Constructor
    {
        Name = name;
        if(name.contains(":")) //If port number is specified
        {
            String[] endp = name.split(":"); //If the name is in the host:port format
            host = endp[0];
            port = Integer.parseInt(endp[1]);
        }
        else
        {
            host = name;
            port = 6667; //Default IRC port
        }
        chatWindow = new ChatWindow(host + ":" + port, context); //Create chat window with title as host:port, give it the context of the parent server connection
    }

    /**
     * Process caller ID command sent by the server. Command handler gets the command and sends it here.
     * Caller ID contains server name eg "EFNet". This is passed to the chat window
     * @param command
     */
    public void ProcessCallerID(String command)
    {
        if(command.contains("NETWORK=")) //If network name is in the command
        {
            int index = command.indexOf("NETWORK="); //Get the string position of the network name
            networkName = command.substring(index + 8, command.indexOf(" ", index)); //get the name
            chatWindow.SetNetworkName(networkName); //Set the chat window default server chat tab to the network name
        }
    }

    /**
     * Method to evaluate if this client is connected to the server currently
     * @return connected as boolean
     */
    public boolean Connected()
    {
        return (client != null && client.isOpen() && connectFinished && connectionSucceeded); //If the client is not null, the tcp socket is open, the connection has finished and succeeded
    }

    /**
     * Start async request to connect to IRC server
     * @param callback Callback to invoke when the connection has either failed or succeeded
     */
    public void Connect(CompletionHandler<Void, ServerConnection> callback)
    {
        try {
            readBuffer.setLength(0); //Clear the read buffer, may have data from previous connection
            client = AsynchronousSocketChannel.open(); //Open new socket
            client.connect(new InetSocketAddress(host, port), null, new CompletionHandler<Void, Object>() { //Start async socket connection to IRC server
                @Override
                public void completed(Void result, Object attachment) { //Completed handler
                    chatWindow.WriteLine("Connected to server: " + host + ":" + port, "green");
                    connectFinished = true;
                    connectionSucceeded = true;
                    callback.completed(result, context); //Invoke connection callback for a completed connection

                    //Begin read
                    Read(new CompletionHandler<String, ServerConnection>() { //Start asynchronously reading data from server
                        @Override
                        public void completed(String result, ServerConnection attachment) { //On incoming data
                            commandHandler.HandleServerCommand(result, chatWindow); //Pass data to command handler to be parsed
                        }

                        @Override
                        public void failed(Throwable exc, ServerConnection attachment) { //Failed tor read data from server
                            chatWindow.WriteLine("Error reading data from: " + attachment.Name + " - Error: " + exc + "\nDisconnecting from server...", "red"); //Print error to chat window
                            Disconnect(); //Disconnect from server if read error occurs.
                        }
                    });
                }

                @Override
                public void failed(Throwable exc, Object attachment) { //Connection failed handler
                    connectFinished = true;
                    connectionSucceeded = false;
                    callback.failed(exc, context); //Invoke callback for failed connection
                }
            });
        }
        catch (IOException ex)
        {
            connectFinished = true;
            connectionSucceeded = false;
            callback.failed(ex, context); //Invoke callback for failed connection
        }
    }

    /**
     * Disconnect without caring about outcome or return value.
     */
    public void Disconnect()
    {
        try
        {
            readBuffer.setLength(0); //Clear message read buffer
            connectionSucceeded = false;
            connectFinished = false;
            if(pendingLoginCallback != null) //If there is a pending login
                pendingLoginCallback.failed(new ClosedConnectionException(), context); //Invoke the failed callback
            if(pendingJoinCallback != null) //If there is a pending channel join request
                pendingJoinCallback.failed(new ClosedConnectionException(), context); //Invoke the failed callback
            pendingLoginCallback = null;
            pendingJoinCallback = null;
            client.shutdownOutput();
            client.shutdownOutput();
            client.close(); //Close TCP socket
        }
        catch (IOException ex)
        {
            chatWindow.WriteLine("Error disconnecting: " + ex, "red"); //Print error
        }
    }

    /**
     * Send message or command to server
     * @param message Message to send
     */
    public void Send(String message)
    {
        client.write(StandardCharsets.US_ASCII.encode(message + "\r\n")); //Encode message + newline as ASCII, then send on socket
    }

    /**
     * Join IRC server channel asynchronously.
     * @param name Name of channel to join
     * @param callback
     */
    public void JoinChannel(String name, CompletionHandler<String, ServerConnection> callback) //HANDLE JOIN CALLBACKS ###################################################################################
    {
        if(Connected()) //If IRC server connection is active
        {
            if(pendingJoinCallback != null) //If not already awaiting a channel join
            {
                callback.failed(new CustomExceptions.ChannelJoinPendingException("A channel join request is pending. Please wait for this to complete before attempting to join again."), context);
                return;
            }
            Send("JOIN " + name); //Send join command
            pendingJoinCallback = callback; //Set global join callback to the callback parameters
        }
        else
            callback.failed(new NotYetConnectedException(), context); //Invoke failed join callback
    }

    /**
     * Finish async channel join. Invokes callback with data containing result of the channel join
     * @param result
     * @param channelName
     */
    public void ProcessPendingJoin(ChannelJoinResult result, String channelName)
    {
        if(pendingJoinCallback == null) return; //If the callback has been deleted or doesn't exist
        switch (result) //Handle outcome of login request
        {
            case SUCCESS: //Joined channel successfully
                pendingJoinCallback.completed(channelName, context); //Invoke success callback
                break;
            case FULL:  //Failed because channel is full
                pendingJoinCallback.failed(new CustomExceptions.ChannelFullException("The selected channel is full!"), context); //Invoke failure callback with error indicating full server
                break;
            case ILLEGAL_NAME: //Channel name is invalid or illegal
                pendingJoinCallback.failed(new CustomExceptions.IllegalChannelNameException("The selected channel name is invalid!"), context); //Invoke failure callback with error indicating name was rejected
                break;
        }
        pendingJoinCallback = null; //Delete callback
    }

    /**
     * Leaves/parts a server channel
     * @param name Name of channel to leave/part
     */
    public void LeaveChannel(String name)
    {
        if(Connected()) //If IRC server connection is active
            Send("PART " + name); //Send PART IRC command to server with the channel name
    }

    /**
     * Asynchronously logs into the server.
     * Begins login request then sets callback for completion
     * @param nickName Nickname to send to server
     * @param userName Username to send to server
     * @param realName Real name to send to server
     * @param hidden If IRC user mode +i should be set on join. (Hides user from user listing)
     * @param callback Async completion callback
     */
    public void Login(String nickName, String userName, String realName, boolean hidden, CompletionHandler<LoginResult, ServerConnection> callback)
    {
        this.nickName = nickName;
        this.userName = userName;
        this.realName = realName;
        this.hidden = hidden;
        if(Connected()) //If connection to IRC server is active
        {
            Send("NICK " + nickName); //Send nickname to server
            Send("USER " + userName + " " + (hidden ? 8 : 0) + " * : " + realName); //Send user command with parameters for username, realName and hidden status
            pendingLoginCallback = callback; //Set the callback for login completion
        }
        else
            callback.failed(new NotYetConnectedException(), context); //Invoke callback and pass a failed exception.
    }

    /**
     * Overload for simpler login method
     * @param nickName Name to use for username, nickname and realname
     * @param callback Async completion callback for login
     */
    public void Login(String nickName, CompletionHandler<LoginResult, ServerConnection> callback)
    {
        Login(nickName, nickName, nickName, true, callback); //Invoke the full login method
    }

    /**
     * Async handler for completing a pending login request.
     * @param loginResult The resulting outcome of the login request
     */
    public void ProcessPendingLogin(LoginResult loginResult)
    {
        if(pendingLoginCallback == null) return; //If the callback has been deleted or doesn't exist
        switch (loginResult) //Handle outcome of login request
        {
            case SUCCESS:
                pendingLoginCallback.completed(LoginResult.SUCCESS, context); //Invoke success callback
                break;
            case NAME_IN_USE:
                pendingLoginCallback.failed(new CustomExceptions.NameTakenException("The selected username or nickname is already in use!"), context); //Invoke failure callback with error indicating name in use
                break;
            case BAD_NAME:
                pendingLoginCallback.failed(new CustomExceptions.BadNameException("The selected username or nickname was rejected by the server!"), context); //Invoke failure callback with error indicating name was rejected
                break;
        }
        pendingLoginCallback = null; //Delete callback
    }

    /**
     * Chunks data into messages based on carriage return and newline characters.
     * Invokes callback method when a message is found
     * @param callback
     */
    private void ProcessMessage(CompletionHandler<String, ServerConnection> callback)
    {
        int index;
        while((index = readBuffer.indexOf("\r\n")) != -1) //Loop through any and all messages in buffer
        {
            callback.completed(readBuffer.substring(0, index), context); //Invoke callback for new message
            readBuffer.delete(0, index + 2); //Removes processed message from buffer
        }
    }

    /**
     * Asynchronously reads from TCP socket, invokes callback on new messages
     * @param callback
     */
    private void Read(CompletionHandler<String, ServerConnection> callback)
    {
        ByteBuffer buffer = ByteBuffer.allocate(256); //256 byte buffer
        client.read(buffer, 1, TimeUnit.DAYS, null, new CompletionHandler<Integer, Object>() { //Read from socket into buffer
            @Override
            public void completed(Integer result, Object attachment) {
                buffer.flip(); //Flip buffer positions so reading and writing happens at the correct place
                readBuffer.append(StandardCharsets.US_ASCII.decode(buffer)); //Decode bytes as ASCII text
                ProcessMessage(callback); //Process the data so messages can be parsed
                if(Connected()) //If the connection is still active
                    Read(callback); //Start reading new messages again
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                callback.failed(exc, context);
            }
        });
    }
}
