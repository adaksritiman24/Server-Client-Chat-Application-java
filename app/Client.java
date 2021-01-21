//this is the client
import java.util.Scanner;
import java.io.*;
import java.net.*;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.*;
import javax.swing.BorderFactory;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.event.*;

class ClientSender implements Runnable{ //thread to send message to the server

	Thread thrd;
	OutputStreamWriter osr;
	String text;
	PrintWriter pw;
	Socket sc;

	ClientSender(Socket sc){

		try{

			//osr = new OutputStreamWriter(sc.getOutputStream());
			pw = new PrintWriter(sc.getOutputStream());
			thrd= new Thread(this);
			thrd.start(); //execute thread
		}
		catch(Exception e){

			System.out.println("Failed to initialize Sender. Error: "+e.getMessage());
		}

	}
	public void run(){

		System.out.println("Sending Started");
		try{
			
			Client.send.addActionListener(new ActionListener(){ //action listener for the send button
				public void actionPerformed(ActionEvent e){

					if(Client.fl==false){
						//get text from the textfield and send it to the server through socket output stream
						Client.marea.setText(Client.marea.getText()+" You: "+Client.textField.getText()+"\n\n");
						pw.println(Client.textField.getText());
						pw.flush();
						Client.textField.setText("");
						Client.textField.setForeground(Color.gray);
						Client.textField.setText("Type here");
						Client.fl = true;
						
					}
				}
			});
			
		}
		catch(Exception e){

			System.out.println("Exception occured  in sending message! "+e.getMessage());
		}
	
	}
}


class ClientReceiver implements Runnable{ //thread to receive messages from server

	Thread thrd;
	BufferedReader br;
	String text;

	ClientReceiver(Socket sc){

		try{
			//receive text through socket input stream
			br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			thrd = new Thread(this);
			thrd.start();//start thread execution

		}
		catch(Exception e){
			System.out.println("Failed to initialize receiver!");
		}
	}
	public void run(){

		System.out.println("receiving started");

		while(true){ //run infinite loop to keep in reveiving messages whenever server sends one

			try{

				text = br.readLine(); //read the message sent
				Client.marea.setText(Client.marea.getText()+" Server: "+text+"\n\n");//append the message to JtextArea
				Client.marea.setCaretPosition(Client.marea.getDocument().getLength()); //always scroll to the bottom of the text area by default
			}
			catch(Exception e){

				System.exit(0);
				System.out.println("Error occured in receiving Message. Mesaage: "+e.getMessage());
			}

		}
		//System.exit(0);	
	}
}


class Client{//Start the main Client class

	JFrame fr = new JFrame(); //create the frame for the Client
	static boolean fl = false;
	static JTextArea marea;
	static JButton send;
	static JTextField textField;

	Client(){//client constructor
		//create gui and also add sockets
		createApp();
		
	}
	public void createApp(){

		//set up the main frame of the application
		fr.setTitle("Client");
		fr.setResizable(false);
		fr.setLayout(null);
		fr.setSize(500,700);
		fr.setDefaultCloseOperation(fr.EXIT_ON_CLOSE); //exit from program if exit is clicked
		fr.setBackground(Color.gray);

		//adding the components to the jframe
		JPanel header = new JPanel();
		header.setBounds(5,5,475,70);
		header.setBackground(Color.orange);
		header.setBorder(new BevelBorder(BevelBorder.RAISED));
		header.setLayout(null);

		JLabel title = new JLabel("CLIENT");
		title.setFont(new Font("Verdana",Font.BOLD, 30));
		title.setBounds(60,10,355,50);
		title.setVerticalAlignment(SwingConstants.CENTER);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		header.add(title);
		fr.getContentPane().add(header);


		marea = new JTextArea();
		JScrollPane sp = new JScrollPane(marea);
		sp.setBounds(5,80,475,520);
		marea.setBorder(new BevelBorder(BevelBorder.LOWERED));
		marea.setFont(new Font("Sans-serif",Font.PLAIN, 25));
		marea.setBackground(Color.pink);
		marea.setLineWrap(true);
		marea.setWrapStyleWord(true);
		marea.setEditable(false);
		fr.getContentPane().add(sp);

		JPanel footer = new JPanel();
		footer.setBounds(5,605,475,50);
		footer.setBackground(Color.gray);
		footer.setBorder(new BevelBorder(BevelBorder.RAISED));
		footer.setLayout(null);

		textField = new JTextField();
		textField.setBounds(5,5,310,40);
		textField.setFont(new Font("Sans-serif",Font.PLAIN, 20));

		//adding placeholder in the textfield
		textField.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent f){
				if(textField.getText().equals("Type here") && (fl ==true)){
					textField.setText("");
					textField.setForeground(Color.black);
					fl = false;
				}
			}
			public void focusLost(FocusEvent f){
				if(textField.getText().isEmpty()){
					textField.setForeground(Color.gray);
					textField.setText("Type here");
					fl = true;
				}
			}
		});
		footer.add(textField);

		send = new JButton("Send");
		send.setBounds(325,5,140,40);
		send.setFont(new Font("Sans-serif",Font.BOLD, 20));
		send.setBackground(new Color(0,105,10));
		send.setForeground(Color.white);
		send.setBorder(new BevelBorder(BevelBorder.RAISED));
		send.setFocusPainted(false);
		footer.add(send);

		
		fr.getContentPane().add(footer);
		fr.setVisible(true); //make frame visible

		//Start the socket connections
		String ip;
		int port = 9989; //port
		Socket sc;

		try{

			Scanner in = new Scanner(System.in);
			System.out.print("Enter the server's ip Address: ");
			ip = in.nextLine();
			sc = new Socket(ip,port); // initialize the socket by sending a request to the server

			System.out.println("Client Started");
			ClientReceiver r = new ClientReceiver(sc); // initialize the receiving thread
			ClientSender s = new ClientSender(sc); // initialize the sending thread

			try{
				r.thrd.join();

			}catch(Exception e){

				System.out.println("R coudn't join");
			}

			System.exit(0); //exit form the program is receiving thread is terminated
		}
		catch(Exception e){// incase of port is busy
			System.out.println("Failed to make connection!");
		}

	}

	public static void main(String[] args){

		new Client();
	}

}
