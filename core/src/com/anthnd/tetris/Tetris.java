package com.anthnd.tetris;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

public class Tetris extends ApplicationAdapter {

	// TODO: Explore rectangles and collisions for tetrominoes
	// TODO: Think about better tetromino shift per time implementation
	// TODO: Integrate input controls

	public SpriteBatch batch;

	// Game window height and width
	public static int height, width;

	// Base square for grid and tetrominoes
	public Texture texture;
	public Sprite sprite;

	public ArrayList<Tetromino> tetrominoes;

	// Grid settings
	public static final int GRID_SQUARE_SIZE = 33;
	public static final int GRID_HEIGHT = 18;
	public static final int GRID_WIDTH = 10;
	public static int GRID_X;
	public static int GRID_Y;

	// Game time
	public static float time;

	public static boolean debug = true;


	@Override
	public void create () {
		batch = new SpriteBatch();

		// Get window dimensions
		height = Gdx.graphics.getHeight();
		width = Gdx.graphics.getWidth();

		// Set grid position
		GRID_X = (width - GRID_WIDTH * GRID_SQUARE_SIZE) / 2;
		GRID_Y = (height - GRID_HEIGHT * GRID_SQUARE_SIZE) / 2;

		if (debug) {
			System.out.println("width: " + width);
			System.out.println("height: " + height);
			System.out.println("GRID_X: " + GRID_X);
			System.out.println("GRID_Y: " + GRID_Y);
		}

		// Generate base square sprite
		texture = new Texture("square.png");
		sprite = new Sprite(texture);
		sprite.setSize(GRID_SQUARE_SIZE, GRID_SQUARE_SIZE);

		tetrominoes = new ArrayList<Tetromino>();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		int oldTime = (int) time;
		time += Gdx.graphics.getDeltaTime() * 2;

		batch.begin();

		drawGrid();

		if (allTetrominoesGrounded() || tetrominoes.isEmpty()) {
			tetrominoes.add(new Tetromino(batch, sprite, Tetromino.PIECE_RANDOM));
		}

		for (Tetromino t : tetrominoes) {
			if (!t.grounded && oldTime != (int)time) {
				if (debug) System.out.println("oldTime: " + oldTime + "\ntime: " + (int)time);
				t.setY(t.yPos -= GRID_SQUARE_SIZE);
			}
			t.draw();
		}

		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}

	/**
	 * Draw a dark grey grid from the base square sprites
	 */
	private void drawGrid() {
		sprite.setColor(Tetromino.rgb(15, 15, 15));
		for (int x = GRID_X; x < GRID_WIDTH * GRID_SQUARE_SIZE + GRID_X; x += GRID_SQUARE_SIZE) {
			for (int y = GRID_Y; y < GRID_HEIGHT * GRID_SQUARE_SIZE + GRID_Y; y += GRID_SQUARE_SIZE) {
				if (debug)
					//System.out.println("Drawing grid square at (" + x + ", " + y + ")");
				sprite.setPosition(x, y);
				sprite.draw(batch);
			}
		}
	}

	private boolean allTetrominoesGrounded() {
		for (Tetromino t : tetrominoes) {
			if (!t.grounded)
				return false;
		}
		return true;
	}

}


class Tetromino {

	public Sprite square;
	public SpriteBatch batch;
	public int pieceType;
	public int xPos;
	public int yPos;
	public boolean grounded = false;

	public static final int PIECE_I = 0;
	public static final int PIECE_O = 1;
	public static final int PIECE_T = 2;
	public static final int PIECE_S = 3;
	public static final int PIECE_Z = 4;
	public static final int PIECE_J = 5;
	public static final int PIECE_L = 6;
	public static final int PIECE_RANDOM = 7;

	public Tetromino(SpriteBatch batc, Sprite sprite, int type) {
		square = sprite;
		batch = batc;

		if (type == PIECE_RANDOM) {
			Random rand = new Random();
			type = rand.nextInt(7);
		}
		pieceType = type;

		// Set tetromino starting position to top and horizontal-center of grid
		xPos = Tetris.GRID_X + 4 * Tetris.GRID_SQUARE_SIZE;
		yPos = Tetris.GRID_Y + (Tetris.GRID_HEIGHT + 2) * Tetris.GRID_SQUARE_SIZE;
		if (pieceType == 0) { // I-piece
			xPos = Tetris.GRID_X + 3 * Tetris.GRID_SQUARE_SIZE;
		}
	}

	public void draw() {
		ArrayList<Point2D> positions = new ArrayList<Point2D>();
		float size = square.getWidth();

		switch (pieceType) {
			case PIECE_I:
				// [ ][ ][ ][ ]
			    positions.add(new Point2D.Float(xPos, yPos - size));
			    positions.add(new Point2D.Float(xPos + size, yPos - size));
			    positions.add(new Point2D.Float(xPos + 2*size, yPos - size));
			    positions.add(new Point2D.Float(xPos + 3*size, yPos - size));
			    square.setColor(rgb(102, 255, 255));
				break;
			case PIECE_O:
				// [ ][ ]
				// [ ][ ]
				positions.add(new Point2D.Float(xPos, yPos - size));
				positions.add(new Point2D.Float(xPos + size, yPos - size));
				positions.add(new Point2D.Float(xPos, yPos - 2*size));
				positions.add(new Point2D.Float(xPos + size, yPos - 2*size));
				square.setColor(rgb(255, 255, 0));
				break;
			case PIECE_T:
				// [ ][ ][ ]
				//    [ ]
				positions.add(new Point2D.Float(xPos, yPos - size));
				positions.add(new Point2D.Float(xPos + size, yPos - size));
				positions.add(new Point2D.Float(xPos + 2*size, yPos - size));
				positions.add(new Point2D.Float(xPos + size, yPos - 2*size));
				square.setColor(rgb(204, 0, 204));
				break;
			case PIECE_S:
			    //    [ ][ ]
				// [ ][ ]
				positions.add(new Point2D.Float(xPos + size, yPos - size));
				positions.add(new Point2D.Float(xPos + 2*size, yPos - size));
				positions.add(new Point2D.Float(xPos, yPos - 2*size));
				positions.add(new Point2D.Float(xPos + size, yPos - 2*size));
				square.setColor(rgb(0, 255, 0));
				break;
			case PIECE_Z:
			    // [ ][ ]
				//    [ ][ ]
				positions.add(new Point2D.Float(xPos, yPos - size));
				positions.add(new Point2D.Float(xPos + size, yPos - size));
				positions.add(new Point2D.Float(xPos + size, yPos - 2*size));
				positions.add(new Point2D.Float(xPos + 2*size, yPos - 2*size));
				square.setColor(rgb(255, 0, 0));
				break;
			case PIECE_J:
				// [ ][ ][ ]
				//       [ ]
				positions.add(new Point2D.Float(xPos, yPos - size));
				positions.add(new Point2D.Float(xPos + size, yPos - size));
				positions.add(new Point2D.Float(xPos + 2*size, yPos - size));
				positions.add(new Point2D.Float(xPos + 2*size, yPos - 2*size));
				square.setColor(rgb(0, 0, 255));
				break;
			case PIECE_L:
			    // [ ][ ][ ]
				// [ ]
				positions.add(new Point2D.Float(xPos, yPos - size));
				positions.add(new Point2D.Float(xPos + size, yPos - size));
				positions.add(new Point2D.Float(xPos + 2*size, yPos - size));
				positions.add(new Point2D.Float(xPos, yPos - 2*size));
				square.setColor(rgb(255, 153, 51));
				break;
			default:
				break;
		}

		for (Point2D pos : positions) {
			square.setPosition((float)pos.getX(), (float)pos.getY());
			square.draw(batch);
		}
	}

	public void setPosition(int x, int y) {
		setX(x);
		setY(y);
	}

	public void setX(int x) {
		this.xPos = x;
	}

	public void setY(int y) {
		this.yPos = y;
		if (y <= Tetris.GRID_Y + 2 * Tetris.GRID_SQUARE_SIZE)
			grounded = true;
	}

	public static Color rgb(float r, float g, float b) {
		return new Color(r/255, g/255, b/255, 1);
	}

}
