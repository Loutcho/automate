package xt.influenza.automate;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Automate cellulaire 2D
 * 
 * Chaque cellule a sa "richesse", nombre réel initialisé aléatoirement entre 0 et 1.
 * Les règles sont, pour chaque cellule (i,j) :
 * - quel est le nombre P de voisins de (i,j) nettement plus pauvres que (i,j) ? Déterminé à l'aide d'un seuil sur la différence de richesse.
 * - quel est le nombre R de voisins de (i,j) plus riches que (i,j) ?
 * - Règle "REVOLUTION" : si P >= seuil, les P voisins pauvres se révoltent et volent à (i,j) une proportion de sa richesse ; ils se la partagent au prorata de la différence de richesse ;
 * - Règle "AGRAVATION DES INEGALITES" : sinon, (i,j) garde l'essentiel de sa richesse mais doit payer un impôt proportionnel à sa richesse à chacun de ses R voisins plus riches.
 */

public class MyNeighborIsRich {

	private static final int COTE_X = 1920; // dimension horizontale de la grille
	private static final int COTE_Y = 1080; // dimension verticale de la grille
	
	/*
	 * Observations
	 * 
	 * - émergence de zones hachurées riche/pauvre clignotantes, ex. D = 0.5, P = 1, I = 0.05, R = 0.95
	 * - répartition des riches sur les bords, ex. D = 0.4, P = 6, I = 0.001, R = 1.0, N = 200000 
	 */
	
	private static final int TAILLE_CASE = 1; // côté du carré représentant une case de la grille dans les images de rendu

	private double tab[][] = new double[COTE_X][COTE_Y];
	
	public static void main(String[] args) {
		choix();
	}
	
	private static void choix() {
		int n = 5000;
		double d = 0.40;
		double i = 0.01;
		int p = 0;
		double r = 0.15;
 
		MyNeighborIsRich automaton = new MyNeighborIsRich(d, p, i, r);
		automaton.runImage(n);
	}
	
	private static void tableau() {
		int n = 5000;
		double d = 0.40;
		double i = 0.01;
		
		for (int p = 0; p <= 9; p ++) {
			for (int kr = 0; kr <= 20; kr ++) {
				double r = 0.05 * kr;
				System.out.println("p = " + p + ", r = " + r);
				MyNeighborIsRich automaton = new MyNeighborIsRich(d, p, i, r);
				automaton.runImage(n);
			}
		}		
	}
	
	private double d; // seuil de différence de richesse déterminant si on est nettement plus pauvre
	private int    p; // nombre de voisins nettement plus pauvres à partir duquel il y a révolution
	private double i; // taux d'imposition des plus pauvres en faveur des plus riches, en cas d'agravation des inégalités
	private double r; // taux de redistribution de la richesse du trop riche, en cas de révolution
	
	/**
	 * @param d
	 * @param p
	 * @param i
	 * @param r
	 */
	public MyNeighborIsRich(double d, int p, double i, double r) {
		this.d = d; 
		this.p = p;
		this.i = i;
		this.r = r;
	}
	
	/**
	 * @param n nombre de générations pendant lesquelles on laisse évoluer l'automate
	 */
	public void runVideo(int n) {
		int k = 0;
		init();
		createImage(k);
		while (k < n) {
			k ++;
			evolve();
			createImage(k);
		}
	}
	
	public void runImage(int n) {
		int k = 0;
		init();
		while (k < n) {
			k ++;
			evolve();
		}
		createImage(k);
	}
	
	private void init() {
		java.util.Random random = new java.util.Random(1L);
		for (int x = 0; x < COTE_X; x ++) {
			for (int y = 0; y < COTE_Y; y ++) {
				tab[x][y] = random.nextDouble();
			}
		}
	}
	
	private class Coord {
		public int x, y;
		public Coord(int x, int y) { this.x = x; this.y = y; }
		@Override public String toString() { return "(" + x + ", " + y + ")"; }
	}
	
	private void evolve() {
		double tab2[][] = new double[COTE_X][COTE_Y];
		for (int x = 0; x < COTE_X; x ++) {
			for (int y = 0; y < COTE_Y; y ++) {
				tab2[x][y] = tab[x][y];
			}
		}

		for (int x = 0; x < COTE_X; x ++) {
			for (int y = 0; y < COTE_Y; y ++) {
				List<Coord> voisinsNettementPlusPauvres = new ArrayList<>();
				List<Coord> voisinsPlusRiches = new ArrayList<>();
				for (int dx = -1; dx <= +1; dx ++) {
					for (int dy = -1; dy <= +1; dy ++) {
						if (dx != 0 || dy != 0) {
							if (x + dx >= 0 && y + dy >= 0 && x + dx < COTE_X && y + dy < COTE_Y) {
								if (tab[x + dx][y + dy] > tab[x][y]) {
									voisinsPlusRiches.add(new Coord(x + dx, y + dy));
								}
								if (tab[x][y] - tab[x + dx][y + dy] > d) {
									voisinsNettementPlusPauvres.add(new Coord(x + dx, y + dy));
								}
							}
						}
					}
				}
				
				if (voisinsNettementPlusPauvres.size() <= p) {
					/*
					 * AGRAVATION DES INEGALITES
					 */
					double q = i * tab[x][y];
					for (Coord v: voisinsPlusRiches) {
						tab2[x][y] -= q;
						if (tab2[x][y] < 0.0) {
							System.err.println("HERE");
							System.exit(-1);
						}
						tab2[v.x][v.y] += q; 
					}
				} else {
					/*
					 * REVOLUTION
					 */
					double sum = 0.0;
					for (Coord v: voisinsNettementPlusPauvres) {
						sum += tab[x][y] - tab[v.x][v.y];
					}
					for (Coord v: voisinsNettementPlusPauvres) {
						double q = r * tab[x][y] * (tab[x][y] - tab[v.x][v.y]) / sum;
						tab2[v.x][v.y] += q;
						tab2[x][y] -= q;
						if (tab2[x][y] < 0.0) {
							assert(tab2[x][y] > 1E-15);
							tab2[x][y] = 0.0;
						}
					}
				}
			}
		}
		tab = tab2;
	}
	
	public void createImage(int k) {

		String filename = String.format("frame_N=%04d_D=%f_P=%d_I=%f_R=%f.png", k, d, p, i, r);
		BufferedImage img = new BufferedImage(TAILLE_CASE * COTE_X, TAILLE_CASE * COTE_Y, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < COTE_X; x ++) {
			for (int y = 0; y < COTE_Y; y ++) {
				
				int z = (int) (tab[x][y] * 255.0);
				
				int r, g, b;
				if (z <= 255) {
					r = z;
					g = 0;
					b = 0;
				} else {
					r = 255;
					z -= 255;
					if (z <= 255) {
						g = z;
						b = 0;
					} else {
						g = 255;
						z -= 255;
						if (z <= 255) {
							b = z;
						} else {
							b = 255;
						}
					}
				}
				
				int rgb = (r << 16) | (g << 8) | b;
				for (int dx = 0; dx < TAILLE_CASE; dx ++) {
					for (int dy = 0; dy < TAILLE_CASE; dy ++) {
						img.setRGB(TAILLE_CASE * x + dx, TAILLE_CASE * y + dy, rgb);
					}
				}
			}
		}
		
		File file = new File(filename);
		try {
			ImageIO.write(img, "PNG", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
