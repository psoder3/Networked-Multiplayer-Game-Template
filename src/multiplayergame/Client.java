package multiplayergame;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import multiplayergame.ColorChooserButton.ColorChangedListener;

public class Client extends JComponent implements KeyListener
{
    private JFrame frame;
    private long ID;
    private List<Player> players;
    private JPanel ControlPanel = new JPanel();
    private JLabel hostLbl = new JLabel("Host IP:");
    private JLabel portLbl = new JLabel("Port:");
    private JLabel clientNameLbl = new JLabel("Client Name:");
    private JLabel clientColorLbl = new JLabel("Client Color:");
    private JTextField hostIPField = new JTextField();
    private JTextField portField = new JTextField();
    private JTextField clientNameField = new JTextField();
    private ColorChooserButton colorChooser;
    private JButton joinGameButton;
    private int pollingInterval = 10; // milliseconds
    
    private static String HOST = "10.65.0.128"; // This should be the specific ip address 
                                                      // of the computer where the server is running
    private static int PORT = 44422; // This has to be the same port the server is using

    
    public Client()
    {
        this.players = new ArrayList();
        this.frame = new JFrame();
        this.frame.setTitle("Networked Game Example");
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setVisible(true);
        hostIPField.setText(HOST);
        portField.setText("44422");
        this.frame.addKeyListener(this);
        
        colorChooser = new ColorChooserButton(Color.WHITE);
        colorChooser.addColorChangedListener(new ColorChangedListener() {
            @Override
            public void colorChanged(Color newColor) {
                
                ClientAction ma = new ClientAction(ID,newColor);
                try {
                    Client.this.doPost("/changeClientColor", ma);
                } catch (Exception ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        joinGameButton = new JButton("Join Game");
        joinGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String chosenName = clientNameField.getText();
                Color chosenColor = colorChooser.getSelectedColor();
                if (chosenName.equals("") || nameExists(chosenName))
                {
                    JOptionPane.showMessageDialog(null, "Please change your name");
                    return;
                }
                
                HOST = hostIPField.getText();
                PORT = Integer.parseInt(portField.getText());
                int randX = (int)(Math.random() * Client.this.getWidth());
                int randY = (int)(Math.random() * Client.this.getHeight());
                ID = System.currentTimeMillis();
                Player p = new Player(ID, chosenName, chosenColor, randX, randY);
                
                try {
                    Client.this.doPost("/addClient", p);
                    
                } catch (Exception ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
                joinGameButton.setEnabled(false);
                clientNameField.setEnabled(false);
            }
        });
        
        hostIPField.setColumns(10);
        portField.setColumns(10);
        clientNameField.setColumns(10);

        ControlPanel.add(hostLbl);
        ControlPanel.add(hostIPField);
        ControlPanel.add(portLbl);
        ControlPanel.add(portField);
        ControlPanel.add(clientNameLbl);
        ControlPanel.add(clientNameField);
        ControlPanel.add(clientColorLbl);
        ControlPanel.add(colorChooser);
        ControlPanel.add(joinGameButton);
        
        this.frame.add(this,BorderLayout.CENTER);
        this.frame.add(ControlPanel,BorderLayout.SOUTH);
        this.frame.setSize(800,500);
        this.frame.setLocationRelativeTo(null);
        
        //this.pack();
        startPollingServerForUpdates();
        
    }
    
    private boolean nameExists(String name)
    {
        for (Player p : players)
        {
            if (p.name.equals(name))
            {
                return true;
            }
        }
        return false;
    }

    private void startPollingServerForUpdates()
    {
        class UpdateFetcher extends TimerTask {
            public void run() {
               
                try {
                    ClientAction ca = new ClientAction(ID);
                    players = (List<Player>)(Client.this.doPost("/getPlayers", ca));
                    Client.this.invalidate();
                    repaint();
                } catch (Exception ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (!joinGameButton.isEnabled())
                {
                    frame.requestFocus();
                }
            }
        }

        // Sync our client with the server every 10 milliseconds
        Timer timer = new Timer();
        timer.schedule(new UpdateFetcher(), 0, pollingInterval);
    }    

    
    @Override
    public void paintComponent(Graphics g)
    {
        for (Player p : players)
        {
            int rect_left_X = p.x;
            int rect_top_Y = p.y;
            int rect_width = 25;
            int rect_height = 25;

            g.setColor(p.color);
            g.fillRect(rect_left_X, rect_top_Y, rect_width, rect_height);
            g.drawString(p.name, rect_left_X - 10, rect_top_Y - 10);
        }
    }
    
    private Object doPost(String urlPath, Object postData) throws Exception
    {
        try
        {
           urlPath = "HTTP://" + HOST + ":" + PORT + urlPath;
           URL url = new URL(urlPath);
           HttpURLConnection connection = (HttpURLConnection)url.openConnection();
           
           connection.setRequestMethod("POST");
           connection.setDoInput(true);
           connection.setDoOutput(true);
           connection.addRequestProperty("Accept", "text/html");  
           connection.connect();
           
           XStream xStream = new XStream(new DomDriver());
           
           xStream.toXML(postData, connection.getOutputStream());
           
           if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
           {               
               Object result = (Object)xStream.fromXML(connection.getInputStream());
               return result;
           }
           else
           {
               throw new IOException();
               //SERVER RETURNED AN HTTP ERROR
           }
        }
        catch (IOException e)
        {
           throw e;
        }
    }
        
    
     
    public static void main(String[] args) 
    {
        Client sc = new Client();
    }    

    @Override
    public void keyTyped(KeyEvent ke) {
        
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        int keyCode = ke.getKeyCode();
        int direction = 0;
        switch( keyCode ) { 
            case KeyEvent.VK_UP:
                direction = 1;
                break;
            case KeyEvent.VK_DOWN:
                direction = 3;
                // handle down 
                break;
            case KeyEvent.VK_LEFT:
                direction = 4;
                // handle left
                break;
            case KeyEvent.VK_RIGHT :
                direction = 2;
                // handle right
                break;
        }    
        try {
            ClientAction ma = new ClientAction(ID,direction);
            Client.this.doPost("/moveClient", ma);
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }
}
