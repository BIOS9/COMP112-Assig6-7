/* GUI window to ask user for information such as nickname. Used for connecting to a server and private messaging a user.
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

import javax.lang.model.type.NullType;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.nio.channels.CompletionHandler;

public class AskUsernameWindow {

    /**
     * Class containing multiple strings for names required by the IRC server.
     */
    public static class UsernameResult
    {
        public String Nick;
        public String Username;
        public String Realname;
    }

    /**
     * Asks user for names for IRC server by opening a GUI window and then calls the async callback when they enter the information.
     * @param callback Callback to invoke when the user is finished
     */
    public static void getServerNames (CompletionHandler<UsernameResult, NullType> callback)
    {
        UsernameResult result = new UsernameResult(); //Create username result object to return eventually
        JFrame frame = new JFrame(); //Create window
        frame.setPreferredSize(new Dimension(300, 150)); //Set window size

        JPanel container = new JPanel(new GridBagLayout()); //New parent container for this window, uses grid bag layout scheme
        GridBagConstraints c = new GridBagConstraints(); //Create grid bag layout constraints to position and size child elements

        JTextField nickBox = new JTextField(); //Create new text input box for nickname
        JTextField usernameBox = new JTextField(); //Create new text input box for username
        JTextField realnameBox = new JTextField(); //Create new text input box for username

        nickBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); //Set size of nickbox
        usernameBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); //Set size of username box
        realnameBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); //Set size of realname box

        nickBox.addActionListener((ActionEvent e) -> { //Add event handler for when user presses enter in nickname text box
            result.Nick = nickBox.getText(); //Set username, nickname and realname all to the entered nickname for quick setting names
            result.Username = nickBox.getText();
            result.Realname = nickBox.getText();
            callback.completed(result, null); //Invoke completion callback
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //Close window
        });

        JLabel nickLabel = new JLabel("Nickname:"); //new label for nickname
        JLabel usernameLabel = new JLabel("Username:"); //new label for username
        JLabel realnameLabel = new JLabel("Realname:"); //new label for realname
        c.insets = new Insets(3,3,3,3); //add padding
        c.fill = GridBagConstraints.NONE; //Dont autosize label
        c.gridx = 0; //Grid x position 0
        c.gridy = 0; //Grid y position 0
        c.weightx = 0; //Dont weight size because its not autosize
        container.add(nickLabel, c); //Add nickname label to container
        c.gridy = 1;
        container.add(usernameLabel, c); //Add nickname label to container
        c.gridy = 2;
        container.add(realnameLabel, c); //Add nickname label to container
        c.fill = GridBagConstraints.HORIZONTAL; //Scale input boxes
        c.gridx = 1; //Grid x position 1
        c.weightx = 1; //Weight autosize to max available space
        c.gridy = 0; //Grid y position 0
        container.add(nickBox, c); //Add nickname box to container
        c.gridy = 1; //Grid y position 1
        container.add(usernameBox, c); //Add username box to container
        c.gridy = 2; //Grid y position 2
        container.add(realnameBox, c); //Add realname box to container

        c.gridx = 1; //Grid x position 0
        JButton submitButton = new JButton("Join Server");
        submitButton.addActionListener((ActionEvent) -> {
            result.Nick = nickBox.getText(); //Copy input box values to username result object
            result.Username = usernameBox.getText();
            result.Realname = realnameBox.getText();
            callback.completed(result, null); //Invoke completion callback
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //Close window
        });

        c.gridy = 4; //Grid y position 4
        container.add(submitButton, c); //add submit button to the container

        c.gridx = 0; //Grid x position 0
        JButton closeButton = new JButton("Cancel"); //New close/cancel button
        closeButton.addActionListener((ActionEvent) -> { //Add click action listener to the button
            callback.failed(null, null); //Invoke failed callback to indicate user canceled request
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //Close window
        });

        container.add(closeButton, c); //Add button to container

        frame.add(container); //add the container to the window
        frame.pack(); //Pack the GUI elements into the window
        frame.setVisible(true); //Make the window visible
    }

    /**
     * Asks the user for a nickname using a GUI window, and invokes an async callback on completion
     * @param callback Callback to invoke on completion
     */
    public static void getNick (CompletionHandler<String, NullType> callback, boolean askingChannel)
    {
        JFrame frame = new JFrame(); //Create window
        frame.setPreferredSize(new Dimension(300, 100)); //Set window size

        JPanel container = new JPanel(new GridBagLayout()); //New parent container for this window, uses grid bag layout scheme
        GridBagConstraints c = new GridBagConstraints(); //Create grid bag layout constraints to position and size child elements

        JTextField nickBox = new JTextField(); //Create new text input box for nickname

        nickBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); //Set size of nickbox

        nickBox.addActionListener((ActionEvent e) -> { //Add event handler for when user presses enter in nickname text box
            callback.completed(nickBox.getText(), null); //Invoke completion callback
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //Close window
        });

        JLabel nickLabel = new JLabel(askingChannel ? "Channel:" : "Nickname:"); //new label for nickname
        c.insets = new Insets(3,3,3,3); //add padding
        c.fill = GridBagConstraints.NONE; //Dont autosize label
        c.gridx = 0; //Grid x position 0
        c.gridy = 0; //Grid y position 0
        c.weightx = 0; //Dont weight size because its not autosize
        container.add(nickLabel, c); //Add nickname label to container

        c.fill = GridBagConstraints.HORIZONTAL; //Scale input boxes
        c.gridx = 1; //Grid x position 1
        c.weightx = 1; //Weight autosize to max available space
        c.gridy = 0; //Grid y position 0
        container.add(nickBox, c); //Add nickname box to container

        c.gridx = 1; //Grid x position 0
        c.gridy = 1; //Grid y position 1
        JButton submitButton = new JButton("OK"); //New submit button
        submitButton.addActionListener((ActionEvent) -> { //Add click action listener to the button
            callback.completed(nickBox.getText(), null); //Invoke completion callback
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //Close window
        });

        container.add(submitButton, c); //Add button to container

        c.gridx = 0; //Grid x position 1
        JButton closeButton = new JButton("Cancel"); //New close/cancel button
        closeButton.addActionListener((ActionEvent) -> { //Add click action listener to the button
            callback.failed(null, null); //Invoke failed callback to indicate user canceled request
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //Close window
        });

        container.add(closeButton, c); //Add button to container

        frame.add(container); //Add container to the window
        frame.pack(); //Pack the elements into the window
        frame.setVisible(true); //Make the window visible
    }
}
