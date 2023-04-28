package xt.influenza.automate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

	private Main() {}
	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.loadRule();
		main.init();
		int n = 0;
		main.display();
		while (true) {
			main.evolve();
			main.display();
			n ++;
			Thread.sleep(200);
		}
	}

	private static final int NONSENSE = 0x0;
	private static final int IS_BORN  = 0x1;
	private static final int DIES     = 0x2;
	private static final int SURVIVES = 0x4;
	private static final int CHANGES  = 0x8;
	private static final int ALIVE_MASK = 0xE;
	
	private static final char DEAD     = ' ';
	private static final char WHITE    = 'o';
	private static final char BLACK    = '■';
	
	private char opponent(char z) {
		if (z == WHITE) return BLACK;
		if (z == BLACK) return WHITE;
		return z;
	}
	

	private static final int SIDE_X = 80;
	private static final int SIDE_Y = 56;
	
	private byte[][] rule;
	private char[][] state;
	
	private void myAssert(boolean b) throws Exception {
		if (!b) { throw new Exception(); }
	}
	
	private void loadRule() throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("src/main/resources/rule.txt"));
		rule = new byte[9][17];
		myAssert(lines.get(0).equals("ALIVE:"));
		for (int b = 1; b <= 9; b ++) {
			String line = lines.get(b);
			int i = 9 - b;
			for (int j = 0; j < 17; j ++) {
				char z = line.charAt(j);
				byte v = 0x0;
				switch (z) {
				case ' ': v = NONSENSE; break;
				case 'X': v = DIES; break;
				case '=': v = SURVIVES; break;
				case 'S': v = CHANGES; break;
				default:
					throw new Exception();
				}
				rule[i][j] = v;
			}
		}
		myAssert(lines.get(10).equals(""));
		myAssert(lines.get(11).equals("DEAD:"));
		for (int b = 12; b <= 20; b ++) {
			String line = lines.get(b);
			int i = 20 - b;
			for (int j = 0; j < 17; j ++) {
				char z = line.charAt(j);
				byte v = 0x0;
				switch (z) {
				case ' ': break;
				case '+': v = IS_BORN; break;
				default:
					throw new Exception();
				}
				rule[i][j] |= v;
			}
		}
		System.out.println("=== RULE: ===");
		for (int i = 0; i < 9; i ++) {
			for (int j = 0; j < 17; j ++) {
				System.out.print(rule[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	private void init() {
		state = new char[SIDE_X][SIDE_Y];
		for (int y = SIDE_Y - 1; y >= 0; y --) {
			for (int x = 0; x < SIDE_X; x ++) {
				state[x][y] = DEAD;
			}
		}
		
		state[21][20] = BLACK;
		state[22][20] = BLACK;
		state[23][20] = BLACK;
		state[24][20] = BLACK;
		state[22][21] = BLACK;
		state[23][21] = BLACK;
		
		int dx = 11;
		int dy = 9;

		state[21 + dx][21 + dy] = WHITE;
		state[21 + dx][22 + dy] = WHITE;
		state[21 + dx][23 + dy] = WHITE;
		state[21 + dx][24 + dy] = WHITE;
		state[20 + dx][22 + dy] = WHITE;
		state[20 + dx][23 + dy] = WHITE;
	}
	
	private void display() {
		System.out.println("=== STATE: ===");
		for (int y = SIDE_Y - 1; y >= 0; y --) {
			for (int x = 0; x < SIDE_X; x ++) {
				System.out.print(state[x][y] + " ");
			}
			System.out.println();
		}
	}
	
	private int neighbours(int x, int y) {
		int c = 0;
		for (int dx = -1; dx <= +1; dx ++) {
			for (int dy = -1; dy <= +1; dy ++) {
				if (! (dx == 0 && dy == 0)) {
					if ((x + dx >= 0) && (x + dx < SIDE_X) && (y + dy >= 0) && (y + dy < SIDE_Y)) {
						if (state[x + dx][y + dy] != DEAD) {
							c ++;
						}
					}
				}
				
			}
		}
		return c;
	}
	
	private int coloredNeighbours(char color, int x, int y) {
		int c = 0;
		for (int dx = -1; dx <= +1; dx ++) {
			for (int dy = -1; dy <= +1; dy ++) {
				if (! (dx == 0 && dy == 0)) {
					if ((x + dx >= 0) && (x + dx < SIDE_X) && (y + dy >= 0) && (y + dy < SIDE_Y)) {
						if (state[x + dx][y + dy] == color) {
							c ++;
						}
					}
				}
				
			}
		}
		return c;
	}
	
	private void evolve() throws Exception {
		char[][] newState = new char[SIDE_X][SIDE_Y];
		for (int y = SIDE_Y - 1; y >= 0; y --) {
			for (int x = 0; x < SIDE_X; x ++) {
				newState[x][y] = DEAD;
			}
		}
		for (int y = SIDE_Y - 1; y >= 0; y --) {
			for (int x = 0; x < SIDE_X; x ++) {
				if (state[x][y] == DEAD) {
					int n = neighbours(x, y);
					int w = coloredNeighbours(WHITE, x, y);
					int b = coloredNeighbours(BLACK, x, y);
					int delta = 8 + Math.abs(w - b);
					if ((rule[n][delta] & IS_BORN) != 0) {
						delta = w - b;
						newState[x][y] = (delta > 0) ? WHITE : BLACK ;
					} else {
						newState[x][y] = DEAD;
					}
				} else {
					int n = neighbours(x, y);
					int w = coloredNeighbours(WHITE, x, y);
					int b = coloredNeighbours(BLACK, x, y);
					int ami = (state[x][y] == WHITE) ? w : b;
					int ennemi = (state[x][y] == WHITE) ? b : w;
					int delta = 8 + ami - ennemi;
					char v;
					switch (rule[n][delta] & ALIVE_MASK) {
					case DIES: v = DEAD; break;
					case SURVIVES: v = state[x][y]; break; 
					case CHANGES: v = opponent(state[x][y]); break;
					default:
						throw new Exception();
					}
					newState[x][y] = v;
				}
			}
		}
		state = newState;
	}
}
 