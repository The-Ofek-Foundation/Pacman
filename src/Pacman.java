/*	Ofek Gila
	March 3rd, 2014
	Pacman.java
	This program will play a simplified version of the game "Pacman"
*/
import java.awt.*;			// Imports
import java.awt.event.*;
import javax.swing.*;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Pacman	extends JApplet{			// I'm pretty sure I copied down one of your online codes for key and focus listeners for their methods
	
	public JFrame frame;
	
	public static void main(String[] args) {	// when I made snake.java, and I copied snake.java to have all the implements for this code, so don't
		Pacman GUIT = new Pacman();
		GUIT.run();
	}
	public void run(){
		frame = new JFrame("Pacman");	// ask why I extend JApplet or implement all of those things ^_^
		frame.setContentPane(new PacmanPanel());
		//makePanels();
		frame.setSize(width(615), height(625));		// Sets size of frame
		frame.setResizable(false);						// Makes it so you can't resize the frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	public int width(int w) {
		return w + 12;
	}
	public int height(int h) {
		return h + 31;
	}
	public void init()	{
		setContentPane(	new PacmanPanel());
	}
}
class PacmanPanel extends JPanel implements ActionListener, KeyListener, FocusListener, MouseListener, MouseMotionListener	{

	public final int setWidth = 500, setHeight = 500;
	public final int boxLength = 50;
	public final int boarders = 10;
	public int bbL;
	public char[][] grid;
	public boolean[][] foodHere;
	public boolean[][] highHere;
	public int[][] monsterStack;
	public int width, height;							// width and height of frame
	public Graphics g;									// Graphics of frame
	public Color c;
	public boolean initial = false;
	public Timer t, audioTimer, deathTimer, winTimer, CST;
	public ComputingSpeedCalc CSC;
	public int pacmanX, pacmanY;
	public int foodLeft;
	public boolean lost = true;
	public boolean won = true;
	public int mD, animationNumber;
	public MoveMonsters MM;
	public boolean moved;
	public boolean pacmanEyes;
	public AudioTimer AT;
	public deathAnimation dA;
	public winAnimation wA;
	public int mouthMultiplier, SAMouthMultiplier;
	public int level = 1;
	public boolean levelStart;
	public int points = 0;
	public boolean high;
	public int highCount;
	public boolean invincible;
	public int money;
	public int divisionNumber = 2000;
	public double computingSpeed;	// for 100 clips
	public double startTime;
	public boolean oneTimeOnly = true;
	public boolean declared = false;
	public int clipGetSpeedCount;
	public int lives = 3;
	public ExtraManAnimation EM;
	public Timer ExtraManTimer;
	public boolean revive;
	
	private class ExtraManAnimation implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			repaint();
		}
	}
	private class winAnimation implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			SAMouthMultiplier+=10;
			/*if (SAMouthMultiplier > 360 * 2) {
				winTimer.stop();
				return;
			}*/
			invincible = true;
			repaint();
		}
	}
	private class deathAnimation implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			animationNumber+=2;
			if (animationNumber > 360) {
				deathTimer.stop();
				return;
			}
			repaint();
		}
	}
	private class AudioTimer implements ActionListener {
		public void actionPerformed(ActionEvent e)	{
			if (clipLength  < 1) {
				if (revive && lost) {	audioTimer.stop();	lost = false;	ExtraManTimer.start();	playSound("pacman_extrapac.wav", false);	audioTimer.start();	return;	}
				else if (revive) {	initial = true;	repaint();	ExtraManTimer.stop();	}
				else if (!(intermission) && !(lost))	{	t.start();	levelStart = false;	}
				else if (!(lost))	{ initial = true; repaint();	winTimer.stop();	}
				audioTimer.stop();
				return;
			}
			clipLength--;
		}
	}
	private class ComputingSpeedCalc implements ActionListener {
		public void actionPerformed(ActionEvent e)	{
			if (clipGetSpeedCount  < 1) {
				System.out.println("Calibrated.");
				double time = (System.nanoTime() - startTime) / 1E9;
				computingSpeed = time / 100;
				//System.out.println(time);
				//computingSpeed = (4.84839 * 100) / time;
				//System.out.println(computingSpeed);
				//computingSpeed = (4216780 * computingSpeed) / 4.83839;
				//computingSpeed = 421.6780 / time;
				//System.out.println(computingSpeed);
				CST.stop();
				//t.stop();
				if (lost && won) {	
					//System.out.println("here");
					initial = true;
				}
				repaint();
				return;
			}
			clipGetSpeedCount--;
		}
	}
	private class MoveMonsters implements ActionListener	{
		public void actionPerformed(ActionEvent e)	{
			if (animationNumber == 1) playSound("pacman_chomp.wav", false);
			animationNumber = animationNumber % 2 + 1;
			if (high) {
				highCount--;
				if (highCount < 1) high = false;
			}
			movePacman();
			moveMonsters();
			checkWhereYouSteppedOn();
			repaint();
		}
		public void moveMonsters()	{
			int tempX, tempY;
			for (int i = 0; i < grid.length; i++)
				for (int a = 0; a < grid[i].length; a++)
					for (int c = 0; c < monsterStack[i][a]; c++) {
						tempX = i; tempY = a;
						monsterStack[i][a]--;
						if (monsterStack[i][a] == 0) grid[i][a] = ' ';
						switch ((int)(Math.random() * 50))	{
							case 0:	 tempX++; break;
							case 1:	tempY++;	break;
							case 2:	tempX--;	break;
							case 3:	tempY--;	break;
						}
						if (tempX == -1) tempX = grid.length - 1;
						if (tempY == -1) tempY = grid[tempX].length - 1;
						if (tempX == grid.length) tempX = 0;
						if (tempY == grid[tempX].length) tempY = 0;
						monsterStack[tempX][tempY]++;
						grid[tempX][tempY] = 'M';
					}
		}	}
	public PacmanPanel()	{
		addKeyListener(this);							// implements the implements
		addFocusListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	public void constructor()	{
		if (!(hasFocus())) requestFocus();
		revive = false;
		invincible = false;
		highCount = 0;
		high = false;
		levelStart = true;
		mouthMultiplier = 60;
		SAMouthMultiplier = 30;
		pacmanEyes = true;
		intermission = false;
		setBackground(Color.white);
		MM = new MoveMonsters();
		AT = new AudioTimer();
		dA = new deathAnimation();
		wA = new winAnimation();
		EM = new ExtraManAnimation();
		ExtraManTimer = new Timer(50, EM);
		t = new Timer(160, MM);
		moved = false;
		mD = 0;
		animationNumber = 1;
		foodLeft = 5 + level;
		lost = won = false;
		bbL = boxLength + boarders;
		grid = new char[10][10];
		foodHere = new boolean[grid.length][grid[0].length];
		monsterStack = new int[grid.length][grid[0].length];
		highHere = new boolean[grid.length][grid[0].length];
		for (int i = 0; i < grid.length; i++)
			for (int a = 0; a < grid[i].length; a++) {
				grid[i][a] = ' ';
				foodHere[i][a] = false;
				monsterStack[i][a] = 0;
				highHere[i][a] = false;
			}
		pacmanX = (int)(Math.random() * grid.length);
		pacmanY = (int)(Math.random() * grid[0].length);
		grid[pacmanX][pacmanY] = 'P';
		audioTimer = new Timer(1, AT);
		deathTimer = new Timer(8, dA);
		winTimer = new Timer(40, wA);
		playSound("pacman_beginning.wav", false);
		audioTimer.start();
		declared = true;
	}
	public int getRelativeX(double x)	{
		return (int)((x / width) * setWidth + 0.5);
	}
	public int getRelativeY(double y)	{
		return (int)((y / height) * setHeight + 0.5);
	}
	public void paintComponent(Graphics a)	{
		super.paintComponent(a);
		g = a;
		width = getWidth();
		height = getHeight();	
		//System.out.println(width + " " + height);
		if (oneTimeOnly) {
			oneTimeOnly = false;
			getSpeed();
		}
		if (initial)	{
			constructor();
			placeThing(5 + level, 'M');
			placeThing(foodLeft, 'F');
			placeThing(level / 5, 'H');
			initial = false;
		}
		if (declared) drawGrid();
	}
	public void getSpeed() {
		clipGetSpeedCount = 100;
		CSC = new ComputingSpeedCalc();
		CST = new Timer(1, CSC);
		//winAnimation wA = new winAnimation();
		//t = new Timer(1, wA);
		//t.start();
		startTime = System.nanoTime();
		CST.start();
	}
	public void placeThing(int amount, char symbol) {
		int ran1, ran2;
		for (int i = 0; i < amount; i++)	{
			while (true) {
				ran1 = (int)(Math.random() * grid.length);
				ran2 = (int)(Math.random() * grid[0].length);
				if (grid[ran1][ran2] == ' ' && symbol != 'F') break;
				if (grid[ran1][ran2] == ' ' && foodPlaceOK(ran1, ran2))	break;
			}
			grid[ran1][ran2] = symbol;
			if (symbol == 'F') foodHere[ran1][ran2] = true;
			if (symbol == 'M') monsterStack[ran1][ran2]++;
			if (symbol == 'H') highHere[ran1][ran2] = true;
		}
	}
	public boolean foodPlaceOK(int x, int y)	{
		if (grid[x][y] != ' ')	return false;
		if (foodHere[x][y]) return true;
		if (x != grid.length - 1)	if (grid[x+1][y] == 'F')	return false;
		if (x != 0)		if (grid[x-1][y] == 'F')	return false;
		if (y != grid[x].length - 1)		if (grid[x][y+1] == 'F')	return false;
		if (y != 0)		if (grid[x][y-1] == 'F')	return false;
		return true;
	}
	public void drawGrid() {
		g.setColor(Color.lightGray);
		for (int i = 0; i < grid.length + 1; i++)
			g.fillRect(i * bbL, 0, boarders, height);
		for (int a = 0; a < grid[0].length + 1; a++)	
			g.fillRect(0, a * bbL, width, boarders);
			
		for (int i = 0; i < grid.length; i++)
			for (int a = 0; a < grid[i].length; a++)
				if (pacmanX == i && pacmanY == a) drawPacman(i * bbL, a * bbL);
				else if (foodHere[i][a] && monsterStack[i][a] < 1) drawFood(i * bbL, a * bbL);
				else if (highHere[i][a] && monsterStack[i][a] < 1) drawHigh(i * bbL, a * bbL);
				else if (monsterStack[i][a] > 0) drawMonster(i * bbL, a * bbL);
				else if (grid[i][a] == 'm') drawMoney(i * bbL, a * bbL);
		g.setFont(new Font("Arial", Font.BOLD, 100));
		g.setColor(Color.green);
		if (lost)	g.drawString("You Lost!", width / 2 - 250, height / 2 - 10);
		if (levelStart) {
			g.setColor(Color.blue);
			g.drawString("Level:", width / 2 - 130, height / 2 - 50);
			g.setColor(Color.green);
			g.setFont(new Font("Arial", Font.BOLD, 120));
			g.drawString(level + "", width / 2 - 35, height / 2 + 50);
		}
		else if (won && !(lost)) {
			g.drawString("You Won!", width / 2 - 230, height / 4 - 10);
			g.setColor(Color.blue);
			g.drawString("Points:", width / 2 - 150, height / 2 - 50);
			g.setColor(Color.red);
			g.setFont(new Font("Arial", Font.BOLD, 120));
			g.drawString(points + "", width / 2 - 45 * 3, height / 2 + 50);
		}
		else {
			g.setFont(new Font("Arial", Font.BOLD, 50));
			g.setColor(Color.green);
			g.drawString(level + "", boarders, 50);
			g.setColor(Color.red);
			g.drawString(points + "", width - boxLength * 3, 50);
		}
		drawLives();
	}
	public void drawFood(int i, int a) {
		g.setColor(Color.yellow);
		g.fillOval(i + boarders + boxLength / 4, a + boarders + boxLength / 4, boxLength / 2, boxLength / 2);
		g.setColor(Color.black);
		g.drawOval(i + boarders + boxLength / 4, a + boarders + boxLength / 4, boxLength / 2, boxLength / 2);
	}
	public void drawHigh(int i, int a) {
		g.setColor(Color.blue);
		g.fillOval(i + boarders + boxLength / 4, a + boarders + boxLength / 4, boxLength / 2, boxLength / 2);
		g.setColor(Color.black);
		g.drawOval(i + boarders + boxLength / 4, a + boarders + boxLength / 4, boxLength / 2, boxLength / 2);
	}
	public void drawMonster(int i, int a) {
		if (high)	{
			int shade = (int)(255 - highCount * (255 / 20.0));
			c = new Color(0, 0, shade);
			g.setColor(c);
		}
		else		g.setColor(Color.red);
		g.fillOval(i + boarders, a + boarders, boxLength, boxLength);
		g.setColor(Color.black);
		g.fillOval(i + boarders + boxLength / 4, a + boarders + boxLength / 4, boxLength / 4, boxLength / 4);
		g.fillOval(i + boarders + boxLength / 2, a + boarders + boxLength / 4, boxLength / 4, boxLength / 4);
		g.fillRect(i + boarders + boxLength / 4, a + boarders + boxLength / 4 * 3, boxLength / 2, boxLength / 8);
	}
	public void drawPacman(int i, int a) {
		if (lost) {
			g.setColor(Color.yellow);
			g.fillArc(i + boarders, a + boarders, boxLength, boxLength, animationNumber * mouthMultiplier / 2 + 90 * mD, 360 - animationNumber * mouthMultiplier);
			g.setColor(Color.black);
			g.drawArc(i + boarders, a + boarders, boxLength, boxLength, animationNumber * mouthMultiplier / 2 + 90 * mD, 360 - animationNumber * mouthMultiplier);
		}
		else {
			g.setColor(Color.yellow);
			g.fillArc(i + boarders, a + boarders, boxLength, boxLength, animationNumber * SAMouthMultiplier + 90 * mD, 360 - animationNumber * mouthMultiplier);
			g.setColor(Color.black);
			g.drawArc(i + boarders, a + boarders, boxLength, boxLength, animationNumber * SAMouthMultiplier + 90 * mD, 360 - animationNumber * mouthMultiplier);
		}
		if (!(pacmanEyes)) return;
		if (mD == 0 || mD == 2) g.fillOval(i + boarders + boxLength / 2 - 5, a + boarders - 20 + boxLength / 2, 10, 10);
		else					g.fillOval(i + boarders + boxLength / 2 + 10, a + boarders - 5 + boxLength / 2, 10, 10);
	}
	public void drawMoney(int i, int a) {
		g.setFont(new Font("Arial", Font.BOLD, 30));
		g.setColor(Color.green);
		g.drawString(money + "", i + boarders + 5, a + bbL / 2 + 15);
	}
	public void drawLives() {
		int a = height - bbL;
		g.setFont(new Font("Arial", Font.BOLD, 50));
		g.setColor(Color.blue);
		g.drawString("Lives: ", boarders, a - 5);
		for (int i = 0; i < lives; i++) {
			g.setColor(Color.yellow);
			g.fillArc(i * bbL + boarders, a + boarders, boxLength / 2, boxLength / 2, animationNumber * SAMouthMultiplier + 90 * mD, 360 - animationNumber * mouthMultiplier);
			g.setColor(Color.black);
			g.drawArc(i * bbL + boarders, a + boarders, boxLength / 2, boxLength / 2, animationNumber * SAMouthMultiplier + 90 * mD, 360 - animationNumber * mouthMultiplier);
		}
	}
	public void mouseDragged(MouseEvent evt)	{	}
	public void mouseMoved(MouseEvent evt)	{	}
	public void actionPerformed(ActionEvent e)	{	}
	public void focusGained(FocusEvent evt) {	}
	public void focusLost(FocusEvent evt) {	}
	public void movePacman()	{
		if (!(moved)) return;
		if (grid[pacmanX][pacmanY] != 'm') grid[pacmanX][pacmanY] = ' ';
		switch (mD) {
			case 3:	if (pacmanY == grid[0].length - 1) pacmanY = 0; else pacmanY++;		break;
			case 1: if (pacmanY == 0)	pacmanY = grid[0].length - 1;	else pacmanY--;	break;
			case 0:	if (pacmanX == grid.length - 1) pacmanX = 0;	else pacmanX++;	break;
			case 2:	if (pacmanX == 0)	pacmanX = grid.length - 1;	else pacmanX--;	break;
		}
		moved = false;
		checkWhereYouSteppedOn();
		if (grid[pacmanX][pacmanY] != 'M' && grid[pacmanX][pacmanY] != 'm')	grid[pacmanX][pacmanY] = 'P';
	}
	public void keyTyped(KeyEvent evt) {	
		char key = evt.getKeyChar();
		if (key == 'r')	{	lives = 3;	level = 1;	points = 0;	t.stop();	audioTimer.stop();	initial = true;	repaint();	return;	}
		if (key == 'c') {	getSpeed();	return;	}
		if (won || lost)	{	t.stop();	return;	}
		switch (key) {
			case 's':	mD = 3;	break;
			case 'w': 	mD = 1;	break;
			case 'd':	mD = 0;	break;
			case 'a':	mD = 2;	break;
		}
		moved = true;
		repaint();
	}
	public void checkWhereYouSteppedOn() {
		if (won || lost) return;
		if (grid[pacmanX][pacmanY] == 'M')	{
			if (high) {
				deleteMoney();
				grid[pacmanX][pacmanY] = 'm';
				money = monsterStack[pacmanX][pacmanY] * 100;
				playSound("pacman_eatghost.wav", false);
				points += monsterStack[pacmanX][pacmanY] * 100;
				monsterStack[pacmanX][pacmanY] = 0;
				//grid[pacmanX][pacmanY] = 'P';
			}
			else if (invincible);
			else {
				t.stop();
				mouthMultiplier = 1;
				animationNumber = 60;
				pacmanEyes = false;
				monsterStack[pacmanX][pacmanY] = 0;
				grid[pacmanX][pacmanY] = 'P';
				lives--;
				if (lives > 0)	revive = true;
				playSound("pacman_death.wav", false);
				deathTimer.start();
				audioTimer.start();
				lost = true;
			}
			repaint();
		}
		else if (foodHere[pacmanX][pacmanY])	{
			deleteMoney();
			grid[pacmanX][pacmanY] = 'm';
			money =50;
			points += 50;
			playSound("pacman_eatfruit.wav", false);
			foodHere[pacmanX][pacmanY] = false;
			foodLeft--;
			if (foodLeft == 0) {
				pacmanEyes = false;
				level++;
				t.stop();
				won = true;
				repaint();
				playSound("pacman_intermission.wav", false);
				intermission = true;
				winTimer.start();
				audioTimer.start();
			}
		}
		else if (highHere[pacmanX][pacmanY]) {
			deleteMoney();
			grid[pacmanX][pacmanY] = 'm';
			money = 75;
			high = true;
			highCount = 20;
			points += 75;
			highHere[pacmanX][pacmanY] = false;
		}
	}
	public void deleteMoney() {
		for (int i = 0; i < grid.length; i++)
			for (int a = 0; a < grid[i].length; a++)
				if (grid[i][a] == 'm')
					grid[i][a] = ' ';
	}
	public void keyPressed(KeyEvent evt) {	}
	public void keyReleased(KeyEvent evt) {	}
	public void mouseEntered(MouseEvent evt) {	} 
	public void mousePressed(MouseEvent evt) {	}
    public void mouseExited(MouseEvent evt) {	} 
    public void mouseReleased(MouseEvent evt) {  } 
    public void mouseClicked(MouseEvent evt) { 
    	if (!(hasFocus()))	requestFocus();
    }
    public void playSound(String soundLocation, boolean loopContinuously) {
    	try {
    	    audioInputStream = AudioSystem.getAudioInputStream(new File(soundLocation).getAbsoluteFile());
    	    clip = AudioSystem.getClip();
    	    clip.open(audioInputStream);
    	    if (loopContinuously) clip.loop(clip.LOOP_CONTINUOUSLY);
    	    else clip.start();
    	   // System.out.println(clip.getMicrosecondLength());
    	    clipLength = (int)((clip.getMicrosecondLength() / 1E6) / computingSpeed + 0.5);
    	   // clipLength = (int)computingSpeed;
    	    //System.out.println(clipLength + "cL");
    	} catch(Exception ex) {
    	    System.out.println("Error with playing sound.");
    	    ex.printStackTrace();
    	}
	}
	public Clip clip;
	public AudioInputStream audioInputStream;
	public long clipLength;
	public boolean intermission;
}