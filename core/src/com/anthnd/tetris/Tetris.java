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
import java.util.List;
import java.util.Random;

public class Tetris extends ApplicationAdapter {

	// TODO: Integrate speed up as game progresses
	// TODO: Track score
	// TODO: Refactor

	// Spritebatch for drawing
	public SpriteBatch batch;

	// Game window height and width
	public int height, width;

	// Base square for grid and tetrominoes
	public Texture texture;
	public Sprite sprite;
	public BitmapFont font;

	// Arrays for tetrominoes in grounded and falling states
	public ArrayList<Sprite> fallingTetromino;
	public ArrayList<Sprite> groundedTetrominoes;
	private int fallingPieceType;
	private int fallingPieceOrientation;

	// Grid settings
	public final int GRID_SQUARE_SIZE = 30;
	public final int GRID_HEIGHT = 18;
	public final int GRID_WIDTH = 10;
	public int GRID_X = 20;
	public int GRID_Y = 20;

	// Game time
	public float time;
	public float timeCounter;

	// Constants for tetromino types
	private final int PIECE_I = 0;
	private final int PIECE_O = 1;
	private final int PIECE_T = 2;
	private final int PIECE_S = 3;
	private final int PIECE_Z = 4;
	private final int PIECE_J = 5;
	private final int PIECE_L = 6;
	private final int PIECE_RANDOM = 7;

	// Constants for orientations
	private final int ORIENTATION_UP = 0;
	private final int ORIENTATION_RIGHT = 1;
	private final int ORIENTATION_DOWN = 2;
	private final int ORIENTATION_LEFT = 3;


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
		fallingTetromino = new ArrayList<Sprite>();
		groundedTetrominoes = new ArrayList<Sprite>();

		generateTetromino(PIECE_I);
		translate(fallingTetromino, 0, 1-GRID_HEIGHT);
		setAsGrounded(fallingTetromino);
		fallingTetromino.clear();
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
			if (fallingTetromino.isEmpty()) {
				generateTetromino(PIECE_RANDOM);
			}

			// Shift-down all falling squares if they can go down
			if (canTranslate(fallingTetromino, 0, -1)) {
			    translate(fallingTetromino, 0, -1);
			} else { // Otherwise, set them as grounded squares and clear falling tetromino
                setAsGrounded(fallingTetromino);
                fallingTetromino.clear();
			}

			// Reset half-second counter
			timeCounter = 0;
		}

		// Handle input
		handleInput();

		List<Integer> fullRows = getFullRows();
		for (int row : fullRows) {
			clearRow(row);
			System.out.println("Clear row " + row);
			ArrayList<Sprite> abovePieces = getAboveTetrominoes(row);
			translate(abovePieces, 0, -1);
		}

		// Draw falling and grounded squares
        drawTetrominoes(fallingTetromino);
		drawTetrominoes(groundedTetrominoes);

		// Time display rounded to 2 decimal places
		font.draw(batch, "Time: " + String.format("%.2f", time), 400, 400);

		// End batch drawing
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
	}

	private void clearRow(int row) {
		for (int i = 0; i < groundedTetrominoes.size(); i++) {
			if (Math.abs(groundedTetrominoes.get(i).getY() - row) < 0.10)
				groundedTetrominoes.remove(i--);
		}
	}

	private ArrayList<Sprite> getAboveTetrominoes(int row) {
		ArrayList<Sprite> above = new ArrayList<Sprite>();
		for (Sprite s : groundedTetrominoes) {
			if (s.getY() > row) {
				above.add(s);
			}
		}
		return above;
	}

	private List<Integer> getFullRows() {
		List<Integer> rows = new ArrayList<Integer>();
			for (int y = GRID_Y; y < GRID_HEIGHT*GRID_SQUARE_SIZE + GRID_Y; y += GRID_SQUARE_SIZE) {
				int count = 0;
				for (int x = GRID_X; x < GRID_WIDTH*GRID_SQUARE_SIZE + GRID_X; x += GRID_SQUARE_SIZE) {
					Vector2 pos = new Vector2(x, y);
					for (Sprite s : groundedTetrominoes) {
						Vector2 sPos = new Vector2(s.getX(), s.getY());
						if (pos.epsilonEquals(sPos, 0.01f)) {
							count++;
						}
                    }
				}
				if (count == GRID_WIDTH)
					rows.add(y);
			}
		return rows;
	}

	private void handleInput() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
			if (canTranslate(fallingTetromino, -1, 0))
				translate(fallingTetromino, -1, 0);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
			if (canTranslate(fallingTetromino, 1, 0))
				translate(fallingTetromino, 1, 0);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			if (canTranslate(fallingTetromino, 0, -1))
				translate(fallingTetromino, 0, -1);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
			if (canRotateTetrominoClockwise(fallingTetromino, 3))
				rotateTetrominoClockwise(fallingTetromino, 3);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
			if (canRotateTetrominoClockwise(fallingTetromino, 1))
			    rotateTetrominoClockwise(fallingTetromino, 1);
		}
	}

	private boolean canTranslate(ArrayList<Sprite> tetromino, int xGridAmount, int yGridAmount) {
		for (Sprite g : groundedTetrominoes) {
			for (Sprite s : tetromino) {
			    Vector2 newSpritePos = new Vector2(s.getX() + xGridAmount * GRID_SQUARE_SIZE, s.getY() + yGridAmount * GRID_SQUARE_SIZE);
			    Vector2 gpos = new Vector2(g.getX(), g.getY());
			    if (newSpritePos.epsilonEquals(gpos, 0.10f) || !getGrid().contains(newSpritePos))
			    	return false;
			}
		}
		return true;
	}

	private void translate(ArrayList<Sprite> tetromino, int xGridAmount, int yGridAmount) {
		for (Sprite s : tetromino) {
			s.translate(xGridAmount * GRID_SQUARE_SIZE, yGridAmount * GRID_SQUARE_SIZE);
		}
	}

	private void setAsGrounded(ArrayList<Sprite> tetromino) {
		for (Sprite s : tetromino) {
			groundedTetrominoes.add(s);
		}
	}

	private void drawTetrominoes(ArrayList<Sprite> tetrominoes) {
		for (Sprite s : tetrominoes) {
			s.draw(batch);
		}
	}

	/**
	 * Draw a dark grey grid from the base square sprites
	 */
	private void drawGridBG() {
		sprite.setColor(rgb(15, 15, 15));
		for (int x = GRID_X; x < GRID_WIDTH * GRID_SQUARE_SIZE + GRID_X; x += GRID_SQUARE_SIZE) {
			for (int y = GRID_Y; y < GRID_HEIGHT * GRID_SQUARE_SIZE + GRID_Y; y += GRID_SQUARE_SIZE) {
				sprite.setPosition(x, y);
				sprite.draw(batch);
			}
		}
	}

	public Rectangle getGrid() {
		return new Rectangle(GRID_X, GRID_Y, GRID_WIDTH * GRID_SQUARE_SIZE - GRID_SQUARE_SIZE, GRID_HEIGHT * GRID_SQUARE_SIZE - GRID_SQUARE_SIZE);
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

		Color c = new Color(1, 1, 1, 1);
		Vector2[] posarr = new Vector2[] {};

		switch (pieceType) {
			case PIECE_I:
				// [ ][ ][ ][ ]
                c = rgb(102, 255, 255);
				posarr = new Vector2[] {
					new Vector2(3, 17),
					new Vector2(4, 17),
					new Vector2(5, 17),
					new Vector2(6, 17)
				};
				break;
			case PIECE_O:
				// [ ][ ]
				// [ ][ ]
				c = rgb(255, 255, 0);
				posarr = new Vector2[] {
					new Vector2(4, 17),
					new Vector2(5, 17),
					new Vector2(4, 16),
					new Vector2(5, 16)
				};
				break;
			case PIECE_T:
				// [ ][ ][ ]
				//    [ ]
				c = rgb(204, 0, 204);
				posarr = new Vector2[] {
					new Vector2(4, 17),
					new Vector2(5, 17),
					new Vector2(6, 17),
					new Vector2(5, 16)
				};
				break;
			case PIECE_S:
				//    [ ][ ]
				// [ ][ ]
				c = rgb(0, 255, 0);
				posarr = new Vector2[] {
					new Vector2(5, 17),
					new Vector2(6, 17),
					new Vector2(4, 16),
					new Vector2(5, 16)
				};
				break;
			case PIECE_Z:
				// [ ][ ]
				//    [ ][ ]
				c = rgb(255, 0, 0);
				posarr = new Vector2[] {
					new Vector2(4, 17),
					new Vector2(5, 17),
					new Vector2(5, 16),
					new Vector2(6, 16)
				};
				break;
			case PIECE_J:
				// [ ][ ][ ]
				//       [ ]
				c = rgb(0, 0, 255);
				posarr = new Vector2[] {
					new Vector2(4, 17),
					new Vector2(5, 17),
					new Vector2(6, 17),
					new Vector2(6, 16)
				};
				break;
			case PIECE_L:
				// [ ][ ][ ]
				// [ ]
				c = rgb(255, 153, 51);
				posarr = new Vector2[] {
					new Vector2(4, 17),
					new Vector2(5, 17),
					new Vector2(6, 17),
					new Vector2(4, 16)
				};
				break;
			default:
				break;
		}
		addTetromino(c, posarr);
		fallingPieceType = pieceType;
		fallingPieceOrientation = ORIENTATION_UP;
	}

	private boolean canRotateTetrominoClockwise(ArrayList<Sprite> tetromino, int numOfTurns) {
		ArrayList<Sprite> temporomino = new ArrayList<Sprite>();
	    for (Sprite s : tetromino) {
	    	temporomino.add(new Sprite(s));
		}
		pretendRotateTetrominoClockwise(temporomino, numOfTurns);
		for (Sprite s : temporomino) {
			System.out.println(getGrid() + " contains? (" + s.getX() + "," + s.getY() + ")");
			if (!getGrid().contains(s.getX(), s.getY())) {
				return false;
			}
			Vector2 spos = new Vector2(s.getX(), s.getY());
			for (Sprite g : groundedTetrominoes) {
			    Vector2 gpos = new Vector2(g.getX(), g.getY());
				if (spos.epsilonEquals(gpos, 0.01f)) {
					return false;
				}
			}
		}
		return true;
	}

	private void updateTetrominoOrientation(ArrayList<Sprite> tetromino, boolean actuallyUpdate) {
	    Vector2[] translations = new Vector2[] {};
		switch (fallingPieceType) {
			case PIECE_I:
				// [ ][ ][ ][ ]
				if (fallingPieceOrientation == ORIENTATION_UP) {
				    translations = new Vector2[] {
				    	new Vector2(1, 1),
						new Vector2(0, 0),
						new Vector2(-1, -1),
						new Vector2(-2, -2)
					};
				    if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_RIGHT;
				} else {
                    translations = new Vector2[] {
                    	new Vector2(-1, -1),
						new Vector2(0, 0),
						new Vector2(1, 1),
						new Vector2(2, 2)
					};
                    if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_UP;
				}
				break;
			case PIECE_O:
				// [ ][ ]
				// [ ][ ]
				break;
			case PIECE_T:
				// [ ][ ][ ]
				//    [ ]
				switch(fallingPieceOrientation) {
					case ORIENTATION_UP:
						translations = new Vector2[] {
							new Vector2(1, 1),
							new Vector2(0, 0),
							new Vector2(-1, -1),
							new Vector2(-1, 1)
						};
						if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_RIGHT;
						break;
					case ORIENTATION_RIGHT:
						translations = new Vector2[] {
							new Vector2(1, -1),
							new Vector2(0, 0),
							new Vector2(-1, 1),
							new Vector2(1, 1)
						};
						if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_DOWN;
						break;
					case ORIENTATION_DOWN:
						translations = new Vector2[] {
							new Vector2(-1, -1),
							new Vector2(0, 0),
							new Vector2(1, 1),
							new Vector2(1, -1)
						};
						if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_LEFT;
						break;
					case ORIENTATION_LEFT:
						translations = new Vector2[] {
							new Vector2(-1, 1),
							new Vector2(0, 0),
							new Vector2(1, -1),
							new Vector2(-1, -1)
						};
						if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_UP;
						break;
					default:
						break;
				}
				break;
			case PIECE_S:
				//    [ ][ ]
				// [ ][ ]
				if (fallingPieceOrientation == ORIENTATION_UP) {
					translations = new Vector2[] {
						new Vector2(-1, 0),
						new Vector2(-2, 1),
						new Vector2(1, 0),
						new Vector2(0, 1)
					};
					if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_RIGHT;
				} else {
					translations = new Vector2[] {
						new Vector2(1, 0),
						new Vector2(2, -1),
						new Vector2(-1, 0),
						new Vector2(0, -1)
					};
					if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_UP;
				}
				break;
			case PIECE_Z:
				// [ ][ ]
				//    [ ][ ]
				if (fallingPieceOrientation == ORIENTATION_UP) {
					translations = new Vector2[] {
						new Vector2(2, 1),
						new Vector2(1, 0),
						new Vector2(0, 1),
						new Vector2(-1, 0)
					};
					if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_RIGHT;
				} else {
					translations = new Vector2[] {
						new Vector2(-2, -1),
						new Vector2(-1, 0),
						new Vector2(0, -1),
						new Vector2(1, 0)
					};
					if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_UP;
				}
				break;
			case PIECE_J:
				// [ ][ ][ ]
				//       [ ]
				switch(fallingPieceOrientation) {
					case ORIENTATION_UP:
						translations = new Vector2[] {
							new Vector2(1, 1),
							new Vector2(0, 0),
							new Vector2(-1, -1),
							new Vector2(-2, 0)
						};
						if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_RIGHT;
						break;
					case ORIENTATION_RIGHT:
						translations = new Vector2[] {
							new Vector2(1, -2),
							new Vector2(0, -1),
							new Vector2(-1, 0),
							new Vector2(0, 1)
						};
						if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_DOWN;
						break;
					case ORIENTATION_DOWN:
						translations = new Vector2[] {
							new Vector2(-1, -1),
							new Vector2(0, 0),
							new Vector2(1, 1),
							new Vector2(2, 0)
						};
						if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_LEFT;
						break;
					case ORIENTATION_LEFT:
						translations = new Vector2[] {
							new Vector2(-1, 2),
							new Vector2(0, 1),
							new Vector2(1, 0),
							new Vector2(0, -1)
						};
						if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_UP;
						break;
					default:
						break;
				}
				break;
			case PIECE_L:
				// [ ][ ][ ]
				// [ ]
				switch(fallingPieceOrientation) {
					case ORIENTATION_UP:
						translations = new Vector2[] {
							new Vector2(1, 0),
							new Vector2(0, -1),
							new Vector2(-1, -2),
							new Vector2(0, 1)
						};
						if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_RIGHT;
						break;
					case ORIENTATION_RIGHT:
						translations = new Vector2[] {
							new Vector2(1, -1),
							new Vector2(0, 0),
							new Vector2(-1, 1),
							new Vector2(2, 0)
						};
						if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_DOWN;
						break;
					case ORIENTATION_DOWN:
						translations = new Vector2[] {
							new Vector2(-1, 0),
							new Vector2(0, 1),
							new Vector2(1, 2),
							new Vector2(0, -1)
						};
						if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_LEFT;
						break;
					case ORIENTATION_LEFT:
						translations = new Vector2[] {
							new Vector2(-1, 1),
							new Vector2(0, 0),
							new Vector2(1, -1),
							new Vector2(-2, 0)
						};
						if (actuallyUpdate) fallingPieceOrientation = ORIENTATION_UP;
						break;
					default:
						break;
				}
				break;
			default:
				break;
		}
		individualTranslate(tetromino, translations);
	}

	public void rotateTetrominoClockwise(ArrayList<Sprite> tetromino, int numOfTurns) {
		for (int i = 0; i < numOfTurns; i++) {
			updateTetrominoOrientation(tetromino, true);
		}
	}

	private void pretendRotateTetrominoClockwise(ArrayList<Sprite> tetromino, int numOfTurns) {
		for (int i = 0; i < numOfTurns; i++) {
			updateTetrominoOrientation(tetromino, false);
		}
	}

	private void individualTranslate(ArrayList<Sprite> tetromino, Vector2[] gridTranslations) {
		if (gridTranslations.length > 0) {
			for (int i = 0; i < tetromino.size(); i++) {
				tetromino.get(i).translate(gridTranslations[i].x * GRID_SQUARE_SIZE, gridTranslations[i].y * GRID_SQUARE_SIZE);
			}
		}
	}

	private void addTetromino(Color color, Vector2[] posarr) {
		for (int i = 0; i < 4; i++) {
			Sprite s = baseSquareSprite();
			Vector2 pos = gridToPixel((int)posarr[i].x, (int)posarr[i].y);
			s.setPosition(pos.x, pos.y);
			s.setColor(color);
			System.out.println("Square spawned at " + pos);
			fallingTetromino.add(s);
		}
	}

	private Sprite baseSquareSprite() {
		Sprite s = new Sprite(texture);
		s.setSize(GRID_SQUARE_SIZE, GRID_SQUARE_SIZE);
		return s;
	}

	/**
	 * Converts from game grid coordinates to pixel coordinates
	 * @param x x position on grid
	 * @param y y postiion on grid
	 * @return a Vector2 with pixel coordinates
	 */
	public Vector2 gridToPixel(int x, int y) {
		return new Vector2(x * GRID_SQUARE_SIZE + GRID_X, y * GRID_SQUARE_SIZE + GRID_Y);
	}


	/**
	 * Converts from 255-scale RGB to percent-scale RGB
	 * @param r a value for red from 0 to 255
	 * @param g a value for green from 0 to 255
	 * @param b a value for blue from 0 to 255
	 * @return a Color object
	 */
	public Color rgb(float r, float g, float b) {
		return new Color(r/255, g/255, b/255, 1);
	}

}
