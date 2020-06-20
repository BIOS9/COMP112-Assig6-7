/* GUI window to display list of channels and the users in those channels
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Scanner;

public class ListWindow {
    public ArrayList<String> listItems;

    public ListWindow(ArrayList<String> listItems)
    {
        this.listItems = listItems;
        ProcessData();
        SetupGUI();
    }

    //Source https://stackoverflow.com/questions/388461/how-can-i-pad-a-string-in-java
    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public void ProcessData()
    {
        for(int i = 0; i < listItems.size(); i++) {
            Scanner sc = new Scanner(listItems.get(i));
            sc.next(); //Skip list response code
            sc.next(); //Skip ird destination
            String channel = padRight(sc.next(), 20); //Grab channel name
            int userCount = sc.nextInt(); //Grab user count
            String users = sc.next() + ", ";
            if(users.equals(":, ")) //Check if there are no users
                users = ""; //Clear users
            else
                users = users.substring(1); //Remove ':'
            while(sc.hasNext())
            {
                users += sc.next() + ", ";
            }
            if(users.length() > 2)
                users = users.substring(0, users.length() - 2); //remove trailing comma and space
            listItems.set(i, channel + "\t\t" + userCount + " users: " + users);
        }
    }

    public void SetupGUI()
    {
        JFrame frame = new JFrame(); //Create window
        frame.setPreferredSize(new Dimension(300, 400)); //Set window size

        JPanel container = new JPanel(new GridBagLayout()); //New parent container for this window, uses grid bag layout scheme
        GridBagConstraints c = new GridBagConstraints(); //Create grid bag layout constraints to position and size child elements
        c.insets = new Insets(3,3,3,3); //add padding

        //Source: https://docs.oracle.com/javase/tutorial/uiswing/components/list.html
        JList list = new JList(listItems.toArray()); //Create new jlist with the list of users as the source
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION); //Let user select only one at a time
        list.setLayoutOrientation(JList.VERTICAL); //Make options flow down page vertically
        list.setVisibleRowCount(-1); //Dont set max visible rows

        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        //Source end

        c.fill = GridBagConstraints.BOTH; //Fill size both vertically and horizontally
        c.weightx = 1;
        c.weighty = 1;

        container.add(listScroller, c); //add list scroller button to the container

        c.fill = GridBagConstraints.HORIZONTAL; //Horizontally fill the button width
        c.gridy = 1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.SOUTH;
        JButton submitButton = new JButton("OK");
        submitButton.addActionListener((ActionEvent) -> {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //Close window
        });

        container.add(submitButton, c); //add submit button to the container

        frame.add(container); //add the container to the window
        frame.pack(); //Pack the GUI elements into the window
        frame.setVisible(true); //Make the window visible
    }

    public static void HandleList(ArrayList<String> list)
    {
        new ListWindow(list);
    }
}
