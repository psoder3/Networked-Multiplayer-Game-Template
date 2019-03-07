package multiplayergame;

import com.sun.net.httpserver.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


public class Server {

    private static final int SERVER_PORT_NUMBER = 44422;
    private static final int MAX_WAITING_CONNECTIONS = 10;
    
    private HttpServer server;
    
    private ArrayList<Long> last100requesters;
    
    private List<Player> players;
    
    private void run() {
        
        players = new ArrayList();
        try 
        {
            server = HttpServer.create(new InetSocketAddress(SERVER_PORT_NUMBER),
                                                                                MAX_WAITING_CONNECTIONS);
        } 
        catch (IOException e) {

            System.out.println("Could not create HTTP server: " + e.getMessage());

            return;
        }

        last100requesters = new ArrayList();
        
        server.setExecutor(null); // use the default executor
        server.createContext("/getPlayers", getPlayersHandler);
        server.createContext("/addClient", addClientHandler);
        server.createContext("/moveClient", moveClientHandler);
        server.createContext("/changeClientColor", changeClientColorHandler);
        server.start();
    }

    private final HttpHandler getPlayersHandler = new HttpHandler() 
    {
        @Override
        public void handle(HttpExchange exchange) throws IOException 
        {
            XStream xStream = new XStream(new DomDriver());

            ClientAction ca = (ClientAction)(xStream.fromXML(exchange.getRequestBody()));
            
            long id = ca.id;
            
            last100requesters.add(id);
            
            if (last100requesters.size() > 100)
            {
                Player toRemove = null;
                for (Player p : players)
                {
                    if (!last100requesters.contains(p.id))
                    {
                        toRemove = p;
                        break;
                    }
                }
                if (toRemove != null)
                {
                    players.remove(toRemove);
                }
                last100requesters.remove(0);
            }
            
            
            exchange.getRequestBody().close();

            exchange.sendResponseHeaders(200, 100000);
            xStream.toXML(players, exchange.getResponseBody());
            exchange.getResponseBody().close();                            
        } 
    };     

    private final HttpHandler addClientHandler = new HttpHandler() 
    {
        @Override
        public void handle(HttpExchange exchange) throws IOException 
        {
            XStream xStream = new XStream(new DomDriver());

            Player p = (Player)(xStream.fromXML(exchange.getRequestBody()));
            players.add(p);
            last100requesters.add(p.id);

            exchange.getRequestBody().close();
            
            exchange.sendResponseHeaders(200, 100000);
            xStream.toXML("", exchange.getResponseBody());
            exchange.getResponseBody().close();                            
        } 
    };     
    
    
    private final HttpHandler moveClientHandler = new HttpHandler() 
    {
        @Override
        public void handle(HttpExchange exchange) throws IOException 
        {
            XStream xStream = new XStream(new DomDriver());

            ClientAction ca = (ClientAction)(xStream.fromXML(exchange.getRequestBody()));
            
            long id = ca.id;
            
            for (Player p : players)
            {
                if (p.id == id)
                {
                    if (ca.direction == 1)
                    {
                        p.y -= 5;
                    }
                    if (ca.direction == 2)
                    {
                        p.x += 5;
                    }
                    if (ca.direction == 3)
                    {
                        p.y += 5;
                    }
                    if (ca.direction == 4)
                    {
                        p.x -= 5;
                    }
                }
            }
            
            exchange.getRequestBody().close();

            exchange.sendResponseHeaders(200, 100000);
            xStream.toXML("", exchange.getResponseBody());
            exchange.getResponseBody().close();                            
        } 
    };     
    
    
    private final HttpHandler changeClientColorHandler = new HttpHandler() 
    {
        @Override
        public void handle(HttpExchange exchange) throws IOException 
        {
            XStream xStream = new XStream(new DomDriver());

            ClientAction ma = (ClientAction)(xStream.fromXML(exchange.getRequestBody()));
            
            long id = ma.id;
            
            for (Player p : players)
            {
                if (p.id == id)
                {
                    p.color = ma.color;
                }
            }
            
            exchange.getRequestBody().close();

            exchange.sendResponseHeaders(200, 100000);
            xStream.toXML("", exchange.getResponseBody());
            exchange.getResponseBody().close();                            
        } 
    };     

    public static void main(String[] args) {

            new Server().run();
    }

}
