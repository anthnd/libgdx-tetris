package com.anthnd.tetris;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Random;

public class Tetris extends ApplicationAdapter {

	// TODO: Integrate input controls
	// TODO: Implement tetromino rotation
	// TODO: Vertical left and right walls
	// TODO: Collision
    // TODO: Row tracking and clearing
	// TODO: Refactor

	// Spritebatch for drawing
	public SpriteBatch batch;

	// Game window height and width
	public static int height, width;

	// Base square for grid and tetrominoes
	public static Texture texture;
	public Sprite sprite;
	public BitmapFont font;

	// Arrays for tetrominoes in grounded and falling states
	public ArrayList<BaseSquare> fallingSquares;
	public ArrayList<BaseSquare> groundedSquares;

	// Grid settings
	public static final int GRID_SQUARE_SIZE = 30;
	public static final int GRID_HEIGHT = 18;
	public static final int GRID_WIDTH = 10;
	public static int GRID_X = 20;
	public static int GRID_Y = 20;

	// Boolean representation of grounded tetrominoes
	public boolean[][] grid;

	// Game time
	public static float time;
	public float timeCounter;

	// Constants for tetromino types
	public final int PIECE_I = 0;
	public final int PIECE_O = 1;
	public final int PIECE_T = 2;
	public final int PIECE_S = 3;
	public final int PIECE_Z = 4;
	public final int PIECE_J = 5;
	public final int PIECE_L = 6;
	public final int PIECE_RANDOM = 7;


	@Override
	public void create () {
		// Libgdx initializers
		batch = new SpriteBatch();
		font = new BitmapFont();

		// Get window dimensions
		height = Gdx.graphics.getHeight();
		width = Gdx.graphics.getWidth();

		// Generate base square sprite
		texture = new Texture("square.png");
		sprite = new Sprite(texture);
		sprite.setSize(GRID_SQUARE_SIZE, GRID_SQUARE_SIZE);

		// Initialize tetromino arrays
		fallingSquares = new ArrayList<BaseSquare>();
		groundedSquares = new ArrayList<BaseSquare>();
	}

	@Override
	public void render () {
		// Clear screen and set to black
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Increment time
		time += Gdx.graphics.getDeltaTime();
		timeCounter += Gdx.graphics.getDeltaTime();

		// Begin batch drawing
		batch.begin();

		// Draw grid background
		drawGridBG();

		// Every half-second
		if (timeCounter >= 0.4) {
			// Generate a new tetromino if there are no more falling pieces
			if (fallingSquares.isEmpty()) {
				generateTetromino(PIECE_RANDOM);
			}

			// Shift-down all falling squares if they can go down
			if (canAllTranslate(fallingSquares, groundedSquares, 0, -1)) {
				for (BaseSquare sq : fallingSquares) {
					sq.translate(0, -GRID_SQUARE_SIZE);
				}
			} else { // Otherwise, set them as grounded squares and empty falling squares
                for (BaseSquare fs : fallingSquares) {
                	Vector2 gpos = fs.getGridPosition();
					System.out.println("Add (" + (int)gpos.x + ", " + (int)gpos.y + ") to groundedSquares");
                	groundedSquares.add(fs);
				}
				System.out.println("ArrayList<BaseSquare> groundSquares: ");
				for (BaseSquare gs : groundedSquares) {
					System.out.print(gs.getGridPosition() + " ");
				}
				fallingSquares.clear();
			}

			// Reset half-second counter
			timeCounter = 0;
		}

		// Draw falling and grounded squares
		for (BaseSquare fs : fallingSquares) {
		    if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
				if (canAllTranslate(fallingSquares, groundedSquares, 1, 0))
					fs.translate(-GRID_SQUARE_SIZE, 0);
			}
		    if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
		        if (canAllTranslate(fallingSquares, groundedSquares, -1, 0))
					fs.translate(GRID_SQUARE_SIZE, 0);
			}
			fs.draw(batch);
		}
		for (BaseSquare gs : groundedSquares) {
			gs.draw(batch);
		}

		// Time display rounded to 2 decimal places
		font.draw(batch, "Time: " + String.format("%.2f", time), 400, 400);

		// End batch drawing
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
	}

	/**
	 * Draw a dark grey grid from the base square sprites
	 */
	private void drawGridBG() {
		sprite.setColor(rgb(15, 15, 15));
		for (int x = GRID_X; x < GRID_WIDTH * GRID_SQUARE_SIZE + GRID_X; x += GRID_SQUARE_SIZE) {
			for (int y = GRID_Y; y < GRID_HEIGHT * GRID_SQUARE_SIZE + GRID_Y; y += GRID_SQUARE_SIZE) {
				// System.out.println(x + ", " + y);
				sprite.setPosition(x, y);
				sprite.draw(batch);
			}
		}
	}

	public Rectangle getGrid() {
		return new Rectangle(GRID_X, GRID_Y, GRID_WIDTH * GRID_SQUARE_SIZE, GRID_HEIGHT * GRID_SQUARE_SIZE);
	}

	/**
	 * Spawns a specified-tetromino at the top of the board
	 * @param pieceType a PIECE_TYPE constant spawned
	 */
	public void generateTetromino(int pieceType) {
		if (pieceType == PIECE_RANDOM) {
			Random rand = new Random();
			pieceType = rand.nextInt(7);
		}

		Color c;

		switch (pieceType) {
			case PIECE_I:
				// [ ][ ][ ][ ]
                c = rgb(102, 255, 255);
                fallingSquares.add(new BaseSquare(c, gridToPixel(3, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(4, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(5, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(6, 17)));
				break;
			case PIECE_O:
				// [ ][ ]
				// [ ][ ]
				c = rgb(255, 255, 0);
				fallingSquares.add(new BaseSquare(c, gridToPixel(4, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(5, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(4, 16)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(5, 16)));
				break;
			case PIECE_T:
				// [ ][ ][ ]
				//    [ ]
				c = rgb(204, 0, 204);
				fallingSquares.add(new BaseSquare(c, gridToPixel(4, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(5, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(6, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(5, 16)));
				break;
			case PIECE_S:
				//    [ ][ ]
				// [ ][ ]
				c = rgb(0, 255, 0);
				fallingSquares.add(new BaseSquare(c, gridToPixel(5, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(6, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(4, 16)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(5, 16)));
				break;
			case PIECE_Z:
				// [ ][ ]
				//    [ ][ ]
				c = rgb(255, 0, 0);
				fallingSquares.add(new BaseSquare(c, gridToPixel(4, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(5, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(5, 16)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(6, 16)));
				break;
			case PIECE_J:
				// [ ][ ][ ]
				//       [ ]
				c = rgb(0, 0, 255);
				fallingSquares.add(new BaseSquare(c, gridToPixel(4, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(5, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(6, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(6, 16)));
				break;
			case PIECE_L:
				// [ ][ ][ ]
				// [ ]
				c = rgb(255, 153, 51);
				fallingSquares.add(new BaseSquare(c, gridToPixel(4, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(5, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(6, 17)));
				fallingSquares.add(new BaseSquare(c, gridToPixel(4, 16)));
				break;
			default:
				break;
		}
	}

	/**
	 * Converts from game grid coordinates to pixel coordinates
	 * @param x x position on grid
	 * @param y y postiion on grid
	 * @return a Vector2 with pixel coordinates
	 */
	public static Vector2 gridToPixel(int x, int y) {
		return new Vector2(x * GRID_SQUARE_SIZE + GRID_X, y * GRID_SQUARE_SIZE + GRID_Y);
	}

	/**
	 * Checks if a BaseSquare has no collisions underneath
	 * @param squares an ArrayList of squares to be checked
	 * @return
	 */
	public boolean canAllGoLeft(ArrayList<BaseSquare> squares) {
		for (BaseSquare s : squares) {
			if (s.position.x <= GRID_X || hasSquareUnder(s, groundedSquares))
				return false;
		}
		return true;
	}

	public boolean callAllGoRight(ArrayList<BaseSquare> movingSquares, ArrayList<BaseSquare> staticSquares) {
		for (BaseSquare msq : movingSquares) {
		    Vector2 oneRight = new Vector2(msq.getGridPosition().x + 1, msq.getGridPosition().y);
			for (BaseSquare ssq : staticSquares) {
				if (msq.position.y >= GRID_X + 10*GRID_SQUARE_SIZE || oneRight.epsilonEquals(ssq.getGridPosition(), 0.01f)) {
					System.out.println("Has square to right " + msq.getGridPosition());
					return true;
				}
			}
		}
		return false;
	}

	public boolean canAllTranslate(ArrayList<BaseSquare> movingSquares, ArrayList<BaseSquare> staticSquares, int xGridTranslate, int yGridTranslate) {
		for (BaseSquare movsq : movingSquares) {
			Vector2 oneShift = new Vector2(movsq.getGridPosition().x + xGridTranslate, movsq.getGridPosition().y + yGridTranslate);
			for (BaseSquare stcsq : staticSquares) {
			    if (getGrid().contains(oneShift) || oneShift.epsilonEquals(stcsq.getGridPosition(), 0.01f)) {
					System.out.println("Collision detected");
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if a BaseSquare has no collisions underneath
	 * @param squares an ArrayList of squares to be checked
	 * @return
	 */
	public boolean canAllGoDown(ArrayList<BaseSquare> squares) {
		for (BaseSquare s : squares) {
			if (s.position.y <= GRID_Y || hasSquareUnder(s, groundedSquares))
				return false;
		}
		return true;
	}

	/**
	 * Checks if a BaseSquare has another BaseSquare underneath
	 * @param sq a BaseSquare to be checked
	 * @param grndsqrs an ArrayList of BaseSquares that can collide
	 * @return
	 */
	public boolean hasSquareUnder(BaseSquare sq, ArrayList<BaseSquare> grndsqrs) {
		for (BaseSquare grndsq : grndsqrs) {
			Vector2 oneDown = new Vector2(sq.getGridPosition().x, sq.getGridPosition().y - 1);
			if (oneDown.epsilonEquals(grndsq.getGridPosition(), 0.01f)) {
				System.out.println("Has square under " + sq.getGridPosition());
				return true;
			}
		}
		return false;
	}

	/**
	 * Converts from 255-scale RGB to percent-scale RGB
	 * @param r a value for red from 0 to 255
	 * @param g a value for green from 0 to 255
	 * @param b a value for blue from 0 to 255
	 * @return a Color object
	 */
	public static Color rgb(float r, float g, float b) {
		return new Color(r/255, g/255, b/255, 1);
	}

}



class BaseSquare {

    public Color color;
    public Vector2 position;
    public int width;
    public int height;

	/**
	 * BaseSquare constructor taking a color and position in Vector2 form
	 * @param color a Color for the square
	 * @param pos initial position of square
	 */
	public BaseSquare(Color color, Vector2 pos) {
		this.color = color;
		this.position = pos;
	}

	/**
	 * BaseSquare constructor taking a color and two ints for the grid position
	 * @param color a Color for the square
	 * @param gridPosX x position on grid
	 * @param gridPosY y position on grid
	 */
	public BaseSquare(Color color, int gridPosX, int gridPosY) {
		this.color = color;
		position = Tetris.gridToPixel(gridPosX, gridPosY);
	}

	/**
	 * Translate the base square
	 * @param xShift x shift rightwards
	 * @param yShift y shift upwards
	 */
	public void translate(int xShift, int yShift) {
		position.x += xShift;
		position.y += yShift;
	}

	/**
	 * Draws the square with size = GRID_SQUARE_SIZE
	 * @param batch a SpriteBatch instance
	 */
	public void draw(SpriteBatch batch) {
	    Sprite s = new Sprite(Tetris.texture);
	    s.setSize(Tetris.GRID_SQUARE_SIZE, Tetris.GRID_SQUARE_SIZE);
	    s.setPosition(position.x, position.y);
	    s.setColor(color);
	    s.draw(batch);
	}

	/**
	 * Returns the square's position in the grid. (0, 0) is the bottom-left corner
	 * @return a Vector2 object representing the grid position
	 */
	public Vector2 getGridPosition() {
		return new Vector2(
			(position.x - Tetris.GRID_X)/Tetris.GRID_SQUARE_SIZE,
			(position.y - Tetris.GRID_Y)/Tetris.GRID_SQUARE_SIZE
		);
	}

	/**
	 * Returns a Rectangle at the square's position
	 * @return a Rectangle object
	 */
	public Rectangle getRectangle() {
		return new Rectangle(position.x, position.y, width, height);
	}

}
