package dic3.jaxrx.apirestclient;


import java.awt.*;
import java.awt.event.*;
import java.io.Console;
import java.io.IOException;
import java.util.Vector;
import javax.swing.*;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.Client;

public class ClientImpl {
	private String title = "Logiciel de discussion en ligne";
    private String pseudo = null;
    private Client server=null;
    private JFrame window = new JFrame(this.title);
    private JTextArea txtOutput = new JTextArea();
    private JTextField txtMessage = new JTextField();
    private JButton btnSend = new JButton("Envoyer");
    public void connexion(){
    	try {
			this.server = ClientBuilder.newClient();
			
		}catch (Exception exception) { 
			System.err.println("Client: " + exception); 
		}
    }
    public ClientImpl() {
        this.createIHM();
        connexion();
        this.requestPseudo();
	}


    public void createIHM() {
        // Assemblage des composants
        JPanel panel = (JPanel)this.window.getContentPane();
        JScrollPane sclPane = new JScrollPane(txtOutput);
        panel.add(sclPane, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(this.txtMessage, BorderLayout.CENTER);
        southPanel.add(this.btnSend, BorderLayout.EAST);
        panel.add(southPanel, BorderLayout.SOUTH);
        // Gestion des évènements
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                window_windowClosing(e);
            }
        });
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnSend_actionPerformed(e);
            }
        });
   
	
	txtMessage.addKeyListener(new KeyAdapter() {
	    public void keyReleased(KeyEvent event) {
		if (event.getKeyChar() == '\n')
		    btnSend_actionPerformed(null);
	    }
	});

        // Initialisation des attributs
        this.txtOutput.setBackground(new Color(220,220,220));
        this.txtOutput.setEditable(false);
        this.window.setSize(500,400);
        this.window.setVisible(true);
        this.txtMessage.requestFocus();
    }

    public void requestPseudo() {
        this.pseudo = JOptionPane.showInputDialog(
                this.window, "Entrez votre pseudo : ",
                this.title,  JOptionPane.OK_OPTION
        );
        if (this.pseudo == null) System.exit(0);
        Vector<String> addUser = new Vector<String>();
		addUser.add(this.pseudo);		
		boolean isAdded = false;
		try {
				boolean response = this.server.target("http://localhost:8080/apirest/webapi/room/subscribe/"+pseudo).request("text/plain").get(boolean.class);
				
				//isAdded = (boolean)this.server.execute("sample.subscribe", addUser);
				if(!response){
				
					JOptionPane.showMessageDialog(this.window, "Pseudo exists !");
					requestPseudo();
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
       }

    public void window_windowClosing(WindowEvent e) {
    	//Vector<String> deconnect = new Vector<String>();
		//deconnect.add(this.pseudo);		
		boolean deconnectOk = false;
		try {
			deconnectOk = this.server.target("http://localhost:8080/apirest/webapi/room/unsubscribe/"+this.pseudo).request("text/plain").get(boolean.class);
			if(deconnectOk){
				System.exit(-1);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
    }

    public void btnSend_actionPerformed(ActionEvent e) {
    	 
    	String message = this.txtMessage.getText();
		try {
			String reponse = this.server.target("http://localhost:8080/apirest/webapi/room/postmessage/"+pseudo+"/"+message).request("text/plain").get(String.class);
			System.out.println(reponse);
		} 
		catch (Exception e1) {
			e1.printStackTrace();
			}
		
    	this.txtMessage.setText("");
        this.txtMessage.requestFocus();
    }

    public static void main(String[] args) {
        ClientImpl chatUserImpl = new ClientImpl();
        boolean recu = false;
        String message = "";
        String lastMessageSent = "";
        boolean isFirstMessage = false;
        int i = 0;
        while(true){
        	try {
				message = chatUserImpl.server.target("http://localhost:8080/apirest/webapi/room/getMessage").request("text/plain").get(String.class);
				
				
				i++;
				recu = true;
				if(isFirstMessage == false){
					lastMessageSent = message;
				}
			}catch(Exception e){
				recu = false;
			}
        	finally {
				if(recu){
					if(isFirstMessage){
						if(!lastMessageSent.equals(message)){
							chatUserImpl.txtOutput.append(message +" \n");
							lastMessageSent = message;
						}
					}else{
						
						chatUserImpl.txtOutput.append(message +" \n");
						lastMessageSent = message;
						isFirstMessage = true;
					}	
				}
        	}
        }
    }
}
