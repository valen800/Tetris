
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author alu54279423k
 */
public class Board extends JPanel{
    
    public static final Color COLORS[] = {
            new Color(30, 30, 30), 
            new Color(204, 102, 102), 
            new Color(102, 204, 102), 
            new Color(102, 102, 204), 
            new Color(204, 204, 102), 
            new Color(204, 102, 204), 
            new Color(102, 204, 204), 
            new Color(218, 170, 0)};
    public static final int NUM_ROWS = 22;
    public static final int NUM_COLS = 10;
    private Tetrominos[][] board;
    private Shape currentShape;
    private int currentRow;
    private int currentCol;
    private static final int INITIAL_ROW = -2;
    private Timer timer;
    private int deltaTime;
    private MyKeyAdapter keyAdepter;
    private ScoreBoard scoreBoard;
    private NextPiece nextPiece;
    
    public void setScoreBoard(ScoreBoard scoreBoard) {
        this.scoreBoard = scoreBoard;
    }
    
    public void setNextPiece(NextPiece nextPiece) {
        this.nextPiece = nextPiece;
    }
    
    class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch(e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (canMove(currentShape ,currentCol -1)) {
                    moveLeft();
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (canMove(currentShape ,currentCol +1)) {
                    moveRight();
                }
                break;
            case KeyEvent.VK_UP:
                rotateCurrentShape();
                break;
            case KeyEvent.VK_DOWN:
                moveDown();
                break;
            case KeyEvent.VK_P:
                pauseGame();
                break;
            default:
                break;
            }
            repaint();
        }
    }
    
    public Board() {
        super();
        board = new Tetrominos[NUM_ROWS][NUM_COLS];
        for(int row=0; row<NUM_ROWS; row++) {
            for(int col=0; col<NUM_COLS; col++) {
                board[row][col] = Tetrominos.NoShape;
            }
        }
        //currentShape = nextPiece.getCurrentShape();
        currentShape = new Shape();
        currentRow = INITIAL_ROW;
        currentCol = NUM_COLS / 2;
        //nextPiece.nextPiece();
        
        keyAdepter = new MyKeyAdapter();
        addKeyListener(keyAdepter);
        setFocusable(true);
        
        deltaTime = 900;
        timer = new Timer(deltaTime, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                mainLoop();
            }
        });
        timer.start();
    }
    
    public void mainLoop() {
        moveDown();
        
    }
    
    public void moveLeft() {
        currentCol--;
    }
    
    public void moveRight() {
        currentCol++;
    }
    
    public void moveUp() {
        currentRow--;
    }
    
    public void moveDown() {
        if (!collisions(currentRow + 1)) {
            currentRow++;
        } else {
            makeCollision();
            detectLine();
        }
        repaint();
    }
    public void detectLine() {
        int cc = 0;
        
        for(int row=0; row < NUM_ROWS; row++) {
            cc = 0;
            for(int col=0; col < NUM_COLS; col++) {
                if (board[row][col] != Tetrominos.NoShape) {
                   cc++;
                }
            }
            if (cc == NUM_COLS) {
                System.out.println("Linea Completa = " + row);
                deleteLine(row);
                scoreBoard.incrementScore();
                accumulatedSpeed();
                
            }
        }
    }
    
    public void deleteLine(int rowToDelete) {
        for (int row = rowToDelete; row >1; row --) {
            for (int col = 0; col < NUM_COLS; col++) {
                board[row][col] = board[row-1][col];
            }
        }
        for (int col = 0; col < NUM_COLS; col++) {
            board[0][col] = Tetrominos.NoShape;
        }
    }
    
    private boolean canMove(Shape shape, int newCol) {
        if (newCol + shape.minX() < 0) {
            return false;
        }
        if (newCol + shape.maxX() > NUM_COLS -1) {
            return false;
        }
        for (int i=0; i<=3; i++) {
            int row = currentRow + shape.getY(i);
            int col = newCol + shape.getX(i);
            if (row >=0) {
                if (board[row][col] != Tetrominos.NoShape) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean collisions(int newRow) {
        if(newRow + currentShape.maxY() >= NUM_ROWS) {
            System.out.println("Collisions");
            return true;
        } else {
            for (int i=0; i<=3; i++) {
                int row = newRow + currentShape.getY(i);
                int col = currentCol + currentShape.getX(i);
                if (row >=0) {
                    if (board[row][col] != Tetrominos.NoShape) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void rotateCurrentShape() {
        Shape rotatedShape = currentShape.rotateRight();
        if (canMove(rotatedShape, currentCol)) {
            currentShape = rotatedShape;
        }
    }
    
    public void makeCollision() {
        System.out.println("makeCollision");
        if (!movePieceToBoard()) {
            makeGameOver();
        }
        if (nextPiece == null) {
            System.out.println("NULL");
        } else {
            currentShape = nextPiece.getCurrentShape();
            //currentShape = new Shape();
            currentRow = INITIAL_ROW;
            currentCol = NUM_COLS/2;
            nextPiece.nextPiece();
        }
        
    }
    
    public void makeGameOver() {
        timer.stop();
        JOptionPane.showMessageDialog(null, "Game Over, press OK to new game");
        reset2();
        timer.start();
    }
    
    public boolean movePieceToBoard() {
        int row;
        int col;
        for(int i=0; i<=3; i++) {
            row = currentRow + currentShape.getY(i);
            col = currentCol + currentShape.getX(i);
            if (row<0) {
                return false;
            } else {
                board[row][col] = currentShape.getShape();
            }
        }
        return true;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        paintBoard(g2d);
        paintShape(g2d);
        
    }
    
    protected void paintBoard(Graphics2D g2d) {
        for(int row=0; row < NUM_ROWS; row++) {
            for(int col=0; col < NUM_COLS; col++) {
                drawSquare(g2d, row, col, board[row][col]);
            }
        }
    }
    
    private void paintShape(Graphics2D g2d) {
        for(int i=0; i<=3; i++) {
            drawSquare(g2d, currentRow + currentShape.getY(i),
                    currentCol + currentShape.getX(i) , currentShape.getShape());
        }
    } 
    
    int squareWidth() {
        return getWidth() / NUM_COLS;
    }
    
    int squareHeight() {
        return getHeight() / NUM_ROWS;
    }
    
    private void drawSquare(Graphics g, int row, int col, Tetrominos shape) {
        
        int x = col * squareWidth();
        int y = row * squareHeight();
        Color color = COLORS[shape.ordinal()];
        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    }
    
    public void reset() {
        board = new Tetrominos[NUM_ROWS][NUM_COLS];
        for(int row=0; row<NUM_ROWS; row++) {
            for(int col=0; col<NUM_COLS; col++) {
                board[row][col] = Tetrominos.NoShape;
            }
        }
        currentShape = nextPiece.getCurrentShape();
        //currentShape = new Shape();
        currentRow = INITIAL_ROW;
        currentCol = NUM_COLS / 2;
        nextPiece.nextPiece();
        scoreBoard.resetScore();
    }
    public void reset2() {
        board = new Tetrominos[NUM_ROWS][NUM_COLS];
        for(int row=0; row<NUM_ROWS; row++) {
            for(int col=0; col<NUM_COLS; col++) {
                board[row][col] = Tetrominos.NoShape;
            }
        }
        //currentShape = nextPiece.getCurrentShape();
        currentShape = new Shape();
        currentRow = INITIAL_ROW;
        currentCol = NUM_COLS / 2;
        //nextPiece.nextPiece();
        scoreBoard.resetScore();
    }
    
    public void easy() {
        reset();
        deltaTime = 800;
        timer.setDelay(deltaTime);
    }
    
    public void normal() {
        reset();
        deltaTime = 400;
        timer.setDelay(deltaTime);
    }
    
    public void hard() {
        reset();
        deltaTime = 200;
        timer.setDelay(deltaTime);
    }
    
    public void accumulatedSpeed() {
        if (deltaTime > 50) {
            deltaTime = deltaTime - 100;
        }
    }
    
    public void pauseGame() {
        timer.stop();
        JOptionPane.showMessageDialog(null, "Pause, press OK");
        timer.start();
    }
}