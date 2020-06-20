/* Chat Tab class for displaying separate IRC channels as separate GUI tabs.
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChatTab {
    public String name; //Name of chat tab eg '#112Test'
    public JEditorPane textArea; //Main text area for viewing chat
    public HTMLEditorKit editorKit; //Module for styling text as HTML so it can be coloured
    public HTMLDocument htmlDocument; //Document for HTML formatted text to be appended to
    public JPanel container; //Parent container for text area, text input and buttons
    public JScrollPane scrollPane; //Scroll pane to allow user to scroll text area
    public ServerConnection server; //Parent server context

    public ChatTab(String name, ServerConnection serverConnection, boolean defaultChat)
    {
        this.server = serverConnection; //Set parent server context
        container = new JPanel(); //Create outermost container
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS)); //Set layout for the parent container to make child elements position correctly
        textArea = new JEditorPane();  //HTML formatted text area
        textArea.setEditable(false); //Disallow user from typing in this box
        textArea.setContentType("text/html"); //Set text area as an HTML formatted document
        editorKit = (HTMLEditorKit)textArea.getEditorKit(); //Get the HTML editor for this text area
        htmlDocument = (HTMLDocument) textArea.getDocument(); //Get the document to append the text to
        textArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)); //Set the max size to max so that free space is filled and it auto-resizes
        scrollPane = new JScrollPane(textArea); // put scrollbars around it
        DefaultCaret caret = (DefaultCaret)textArea.getCaret(); //Get the caret for the text area
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); //Set the caret to move to the end of the text when new text is appended (makes chat scroll down)
        container.add(scrollPane); //Add the scroll pane to the main container

        JPanel inputContainer = new JPanel(new GridBagLayout()); //Create a new container for the buttons and text input
        GridBagConstraints c = new GridBagConstraints(); //Layout constraints for the container

        inputContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); //Set size

        if(!defaultChat) { //If this chat tab is the default network chat eg EFNet
            JButton closeButton = new JButton("Close"); //New button to close the tab
            c.fill = GridBagConstraints.NONE; //Dont constrain this element (Let dimensions remain default)
            c.weightx = 0; //Dont weight button width
            c.gridx = 0; //Set input as the first element in the row
            c.gridy = 0; //Ensure elements stay on the same horizontal line
            inputContainer.add(closeButton, c); //Add button to container

            closeButton.addActionListener((ActionEvent e) -> { //Event handler for close button click
                server.chatWindow.RemoveTab(name); //Remove tab from parent window
            });
        }
        JTextField textInput = new JTextField();
        c.fill = GridBagConstraints.HORIZONTAL; //Scale element horizontally according to X weight percentage below.
        c.weightx = 1; //Weight width to fill remaining space
        c.gridx = 1; //Set input as the second element in the row
        c.gridy = 0; //Ensure elements stay on the same horizontal line
        inputContainer.add(textInput, c); //Add button to container
        textInput.addActionListener((ActionEvent e) -> { //Event handler for enter press in textInput
            server.commandHandler.HandleClientCommand(textInput.getText(), name); //Pass command to the command handler
            textInput.setText(""); //Clear textInput
        });

        JButton sendButton = new JButton("Send"); //New button to send chat message
        c.fill = GridBagConstraints.NONE; //Dont constrain this element (Let dimensions remain default)
        c.weightx = 0; //Dont weight button width
        c.gridx = 2; //Set input as the last element in the row
        c.gridy = 0; //Ensure elements stay on the same horizontal line
        inputContainer.add(sendButton, c); //Add button to container
        sendButton.addActionListener((ActionEvent e) -> { //Event handler for send button click
            server.commandHandler.HandleClientCommand(textInput.getText(), name); //Pass command to the command handler
            textInput.setText(""); //Clear textInput
        });

        container.add(inputContainer); //Add container to parent container
        container.setPreferredSize(new Dimension(800, 600)); //Set preferred starting size for this tab
    }
}
