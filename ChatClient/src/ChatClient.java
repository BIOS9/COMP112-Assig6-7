// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP112 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP112 - 2018T1, Assignment 6
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

/**
 * Basic IRC Chat Client 
 */

public class ChatClient {
    private static final String server = "irc.ecs.vuw.ac.nz";  // default IRC server for testing.
    private static final int port = 6667;     // The standard IRC port number.

    /**
     * main: construct a new ChatClient
     */
    public static void main(String[] args) {
        ConnectionManager.ConnectWithNickname(server + ":" + port);
    }

}
