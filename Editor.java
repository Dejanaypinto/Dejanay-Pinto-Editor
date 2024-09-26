import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Client-server graphical editor
 * @author Dejanay Pinto
 *
 */

public class Editor extends JFrame {
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size


	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color
	private ArrayList<Segment> temp_segment = new ArrayList<>();
	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};

		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});

		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
		if (curr != null && g != null)
		{
			curr.draw(g);
			sketch.draw(g);
		}
	}

	// Helpers for event handlers

	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// TODO: YOUR CODE HERE
		if (mode == Mode.DRAW) {
			drawFrom = p;

			if (shapeType.equals("ellipse")) {
				curr = new Ellipse(p.x, p.y, color);
				sketch.add(curr);
			}

			if (shapeType.equals("freehand")) {
				curr = new Polyline(p.x, p.y, color);
				sketch.add(curr);
			}

			if (shapeType.equals("rectangle")) {
				curr = new Rectangle(p.x, p.y, color);
				sketch.add(curr);
			}

			if (shapeType.equals("segment")) {
				curr = new Segment(p.x, p.y, color);
				sketch.add(curr);
				EditorCommunicator.send("draw : " + sketch.idShapes.size());
			}
			//probably should sent a message about what is being drawn
		}
		else {
			for (int shape : sketch.idShapes.keySet()) {
				// check shape is clicked on
				if (sketch.idShapes.get(shape).contains((int) p.getX(), (int) p.getY()) && curr != null) {


//		in moving mode, (request to) start dragging if clicked in a shape;
					if (mode == Mode.MOVE) {
						if (sketch.contains(p.x, p.y) != -1) {
							EditorCommunicator.send("move : " + shape);
							moveFrom = p;
							movingId = shape;
							break;
						}
					}


//			in recoloring mode, (request to) change clicked shape's color
					if (mode == Mode.RECOLOR) {
						EditorCommunicator.send("recolor : " + shape + ":" + color);
						sketch.idShapes.get(shape).setColor(color);
					}


					// in deleting mode, (request to) delete clicked shape
					if (mode == Mode.DELETE) {

						EditorCommunicator.send("delete : " + shape);
						sketch.idShapes.put(shape, null); //deletion of the shape

					}
				}
			}
		}
			// Be sure to refresh the canvas (repaint) if the appearance has changed
			repaint();
		}


	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 * (request to) = send;
	 */
	private void handleDrag(Point p) {
		// TODO: YOUR CODE HERE
		if ((mode == Mode.DRAW) && (curr != null)) {
			if (shapeType == "ellipse") {

				curr = new Ellipse(drawFrom.x, drawFrom.y, p.x, p.y, color); //NOT TOO SURE ABOUT HERE
			}

			else if (shapeType == "rectangle") {
				curr = new Rectangle(drawFrom.x, drawFrom.y, p.x, p.y, color);
			}

			else if (shapeType == "freehand") {
				temp_segment.add(new Segment(drawFrom.x, drawFrom.y, p.x, p.y, color));
				curr = new Polyline(temp_segment, color);
			}

			//segment
			else
				curr = new Segment(drawFrom.x, drawFrom.y, p.x, p.y, color);

		}

		else {
			if ((mode == Mode.MOVE) && (movingId != -1)) {

				sketch.idShapes.get(movingId).moveBy(p.x - moveFrom.x, p.y - moveFrom.y);
				moveFrom = p;
				for (int shape : sketch.idShapes.keySet()) {
					if (sketch.idShapes.get(shape).contains((int) p.getX(), (int) p.getY()) && curr != null) {
						EditorCommunicator.send("move: " + shape);
					}
				}
			}
		}
		repaint();
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it
	 */
	private void handleRelease() {
		// TODO: YOUR CODE HERE
		if ((mode == Mode.DRAW) && (curr != null))
		{
			sketch.add(curr);
		}
		if (mode == Mode.MOVE)
		{
			moveFrom = null;
			movingId = -1;
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});
	}
}
