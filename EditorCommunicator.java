import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Handles communication to/from the server for the editor
 * This class establishes a connection to a server and sends/receives messages.
 * It also contains methods to decode and handle incoming messages.
 *
 * Authors: Dejanay Pinto
 */
public class EditorCommunicator extends Thread {
	private static PrintWriter out;        // to server
	private BufferedReader in;             // from server
	protected Editor editor;               // handling communication for

	/**
	 * Establishes connection and in/out pair
	 * @param serverIP The IP address of the server to connect to
	 * @param editor The editor object associated with this communicator
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 * @param msg The message to be sent
	 */
	public static void send(String msg) {
		out.println(msg);
	}

	/**
	 * Decodes and handles messages from the server
	 * @param msg The message received from the server
	 */
	public void decoder(String msg)
	{
		String[] message = msg.split(":");
		// method format
		// add : ellipse
		if (message.length >= 2) {
			if (message[0].equals("recolor")) {
				ECRecolor(message);
			}

			if (message[0].equals("delete")) {
				ECDelete(message);
			}

			if (message[0].equals("move")) {
				ECMove(message);
			}

			if (message[0].equals("draw")) {
				ECDraw(message);
			}
		}
	}

	/**
	 * Handles recoloring of shapes based on received message
	 * @param msg The message received from the server
	 */
	public void ECRecolor(String[] msg)
	{
		Shape currShape = editor.getSketch().idShapes.get(msg[1]);
		currShape.setColor(Color.decode(msg[2]));
	}

	/**
	 * Handles deletion of shapes based on received message
	 * @param msg The message received from the server
	 */
	public void ECDelete(String[] msg)
	{

		Shape currShape = editor.getSketch().idShapes.get(msg[1]);
		currShape = null;
	}

	/**
	 * Handles drawing of shapes based on received message
	 * @param msg The message received from the server
	 */
	public void ECDraw(String[] msg)
	{
		Shape currShape = editor.getSketch().idShapes.get(msg[1]);
		currShape.draw(editor.getGraphics());
	}

	/**
	 * Handles movement of shapes based on received message
	 * @param msg The message received from the server
	 */
	public void ECMove(String[] msg)
	{
		Shape currShape = editor.getSketch().idShapes.get(msg[1]);
		// TO DO:
	}

	/**
	 * Listens for and handles messages from the server
	 */
	public void run() {

		try {
			// Handle messages
			String inline;
			while ((inline = in.readLine()) != null)
			{

			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}
}




public static ArrayList<String> ViterbiAlgorithm(String str) {
	int unseen= -100; // the probability for the unseen observation
	double nextScore;
	Map<String, String> currStates = new HashMap<>();  //stores the current states to be examined
	currStates.put(start,null); //adding '#' to the current states
	Map<String, Double> currScores = new HashMap<>(); // stores the scores at the current state
	ArrayList<Map<String, String>> backTrack = new ArrayList<>();// used for backtracing
	currScores.put(start, 0.0);  //start is given the score of 0
	String[] c = str.toLowerCase().split(" "); // getting an array of individual words making the sentence
	ArrayList<String> parts_of_speech = new ArrayList<>(); // holds the possible tags
	//looping over the array of words making up the sentence
	for (int i = 0; i <= c.length - 1; i++)
	{
		Map<String, String> nextStates = new HashMap<>();
		Map<String, Double> nextScores = new HashMap<>();
		Map<String, String> temporary = new HashMap<>();
		bestScore = Double.NEGATIVE_INFINITY;    //initializes the bestScore to the largest negative value possible
		// transition from each current state to each of its next states
		for (String currState : currStates.keySet())
		{
			if (transitions.containsKey(currState))  //ensure that the current state is always in the transition map

			{
				for (String nextState : transitions.get(currState).keySet())
				{
					nextStates.put(nextState,currState);
					if (observations.get(nextState).containsKey(c[i]))
					{
						nextScore = currScores.get(currState) + transitions.get(currState).get(nextState) + observations.get(nextState).get(c[i]);
					}
					else  //if there is no observation use constant value unseen as the probability
					{
						nextScore = currScores.get(currState) + transitions.get(currState).get(nextState) + unseen;
					}
					//addition to the nextScores map
					if (!nextScores.containsKey(nextState) || nextScore > nextScores.get(nextState)) {
						nextScores.put(nextState, nextScore);
						temporary.put(nextState, currState);
						if (nextScore > bestScore)
						{
							bestString = nextState;
							bestScore = nextScore;
						}
					}
				}
			}
		}
		currStates = nextStates;
		currScores = nextScores;
		backTrack.add(temporary);
	}
	//adding the tag with the highest score
	parts_of_speech.add(bestString);
	//backTracing to find the best path
	for (int i = backTrack.size()-1; i>0; i--)
	{
		bestString = backTrack.get(i).get(bestString);
		parts_of_speech.add(0,bestString);
	}
	return parts_of_speech;
}