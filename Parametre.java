package DetectionSquelettes;

import ij.IJ;
import ij.gui.GenericDialog;

/**
 * Cette classe gere les differents parametres utiles dans la detection d'objet ({@link Detection}).
 * @author e1502316
 *
 */

public class Parametre {
	/**
	 * Minimum d'intersections dans un squelette d'objet pour le considerer comme pertinent. 
	 */
	public int minIntersection;
	/**
	 * Maximum d'intersections dans un squelette d'objet pour le considerer comme pertinent. 
	 */
	public int maxIntersection;  
	/**
	 * Taille minimum en pixels d'un objet pour qu'il soit analyser. 
	 */
	public int minPixels;
	/**
	 * Taille maximum en pixels d'un objet pour qu'il soit analyser.
	 */
	public int maxPixels;
	/**
	 * Erreur toleree pour consider que trois points sont alignes. 
	 */
	public int facteurAlignement;
	/**
	 * Roundness minimum d'un objet pour le considerer comme pertinent. 
	 */
	public double roundnessMin;
	/**
	 * Roundness maximum d'un objet pour le considerer comme pertinent. 
	 */
	public double roundnessMax;
	
	/**
	 * Ouvre une interface utilisateur pour la saisie des parametres d'analyse et de detections des objets. Renvoie si l'utilisateur a valide la saisie ou non. 
	 * @return Booleen 
	 */
	boolean lireParam() {
		/*
		 * addNumericField prend en parametres : 
		 * - Un string pour le nom du champ,
		 * - Una valeur par defaut,
		 * - Le nombre de decimales apres la virgule. 
		 */
		GenericDialog gd = new GenericDialog("Paramètres", IJ.getInstance());
		gd.addNumericField("Nombre de pixels min pour considérer une forme", 1000, 0);
		gd.addNumericField("Nombre de pixels max pour considérer une forme", 14000, 0);
		gd.addNumericField("Roundness min pour considérer une forme", 0, 2);
		gd.addNumericField("Roundness max pour considérer une forme", 1, 2);
		gd.addNumericField("Nombre d'intersections min dans le squelette", 2, 0);
		gd.addNumericField("Nombre d'intersections max dans le squelette", 3, 0);
		gd.addNumericField("Facteur d'alignement toleree", 500, 0);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		/*
		 * getNextNumber prend les champs l'un apres l'autre. 
		 */
		minPixels = (int) gd.getNextNumber();
		maxPixels = (int) gd.getNextNumber();
		roundnessMin = (double) gd.getNextNumber();
		roundnessMax = (double) gd.getNextNumber();
		minIntersection = (int) gd.getNextNumber();
		maxIntersection = (int) gd.getNextNumber();
		facteurAlignement = (int) gd.getNextNumber();
		return true;
	}
}
