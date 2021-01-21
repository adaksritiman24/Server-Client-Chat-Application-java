//this is the server
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

class ServerSender implements Runnable{ // thread to send messaages

	Thread thrd;
	OutputStreamWriter osr;
	PrintWriter pw;
	String text;

	ServerSender(Socket sc){

		try{
			osr = new OutputStreamWriter(sc.getOutputStream());
			pw = new PrintWriter(osr);
			thrd = new Thread(this);
			thrd.start(); //start the thread
		}
		catch(Exception e){
			System.out.println("Failed to initialize Sender. Error: "+e.getMessage());
		}


	}
	public void run(){

		System.out.println("Sending Started");

		try{
			
			Server.send.addActionListener(new ActionListener(){ //Action listner for the send button
				public void actionPerformed(ActionEvent e){
					if(Server.fl==false){
						//get text from the text field and send it to client through socket
						Server.marea.setText(Server.marea.getText()+" You: "+Server.textField.getText()+"\n\n");
						pw.println(Server.textField.getText());
						pw.flush();
						Server.textField.setText("");
						Server.textField.setForeground(Color.gray);
						Server.textField.setText("Type here");
						Server.fl = true;
					}
				}
			});
			
		}
		catch(Exception e){

			System.out.println("Exception occured  in sending message! "+e.getMessage());
		}
	}
}


class ServerReceiver implements Runnable{ //thread for receiving messages

	Thread thrd;
	BufferedReader br;
	String text;
	Socket sc;

	ServerReceiver(Socket sc){

		try{

			br = new BufferedReader(new InputStreamReader(sc.getInputStream())); //receiving through socket input stream
			thrd = new Thread(this);
			thrd.start(); //starting the thread
		}
		catch(Exception e){

			System.out.println("Failed to initialize receiver!");
		}
	}

	public void run(){

		System.out.println("Receiving Started");

		while(true){
			try{
				text = br.readLine(); //read text forom socket input

				Server.marea.setText(Server.marea.getText()+" Client: "+text+"\n\n"); //append text to JtextArea
				Server.marea.setCaretPosition(Server.marea.getDocument().getLength()); //default scroll to the bottom of the textarea

			}
			catch(Exception e){

				System.exit(0);
				System.out.println("Error occured in receiving Message. Mesaage: "+e.getMessage());
			}
		}
		//System.exit(0);	
	}
}


class Server{ //start the main Server class
	//initialize frame object
	JFrame fr = new JFrame();
	static boolean fl = false;
	static JTextArea marea;
	static JButton send;
	static JTextField textField;


	Server(){
		//create the GUI and also add sockets
		createApp();

	}
	public static void main(String[] args) throws Exception{

		new Server(); //call the constructor

	}
	public void createApp(){

		//Setting up the main frame of the application
		fr.setTitle("server");
		fr.setResizable(false);
		fr.setLayout(null);
		fr.setSize(500,700);
		fr.setDefaultCloseOperation(fr.EXIT_ON_CLOSE); //exit from the program if exit option is clicked in the frame
		fr.setBackground(Color.gray);

		//Start adding the components to the frame
		JPanel header = new JPanel();
		header.setBounds(5,5,475,70);
		header.setBackground(Color.orange);
		header.setBorder(new BevelBorder(BevelBorder.RAISED));
		header.setLayout(null);

		JLabel title = new JLabel("SERVER");
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

		textField.addFocusListener(new FocusListener(){// add placeholder in the textfield
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

		fr.setVisible(true); //make the frame visible after all components are added.

		try{

				System.out.println("Server is Started...");

				//get ip address of the network (if no network is present then print 127.0.0.1)
				InetAddress host = InetAddress.getLocalHost();
				String serverAddress = host.getHostAddress().toString();
				System.out.println("Use 'localhost' as ip address to make connections within local machine");
				System.out.println("Use this IP to connent across different machines: "+serverAddress);

				ServerSocket ss = new ServerSocket(9989); //Make the server run on port no.:9989 on this machine


				System.out.println("Server is waiting for client request...");
				Socket sc = ss.accept(); //Accept socket connection from a client through the port no.: 9989

				System.out.println("Client Connected");

				ServerReceiver r = new ServerReceiver(sc); //Initialize Reveiving thread
				ServerSender s = new ServerSender(sc); //Initialize sending thread

			try{

				r.thrd.join();

			}catch(Exception e){

				System.out.println("R coudn't join");

			}
				System.exit(0); //exit from the program once receiving thread has terminated.

		}
		catch(Exception e){

			System.out.println("Failed to make connections!");
		}

	}
}
