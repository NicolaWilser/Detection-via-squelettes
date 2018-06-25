package DetectionSquelettes;

import ij.IJ;

/**
 * Classe de methodes utilitaires avec des methodes static. Ne pas instancier. 
 * @author e1502316
 *
 */

public class Utilitaire {

	/**
	 * Determine et renvoie si le tableau de pixels place en parametre contient des voicins d'une certaine couleur a l'indice donne. 
	 * @param indice Entier
	 * @param couleur Entier en hexadecimal
	 * @param image Tableau d'entiers
	 * @param nbColonnes Entier
	 * @return Booleen 
	 */
	public static boolean contientVoisin(int indice, int couleur, int[] image, int nbColonnes)
	{
		if (image[indice-1] == couleur)
		{
			return true;
		}
		if (image[indice+1] == couleur)
		{
			return true;
		}
		if (image[indice+nbColonnes-1] == couleur)
		{
			return true;
		}
		if (image[indice+nbColonnes+1] == couleur)
		{
			return true;
		}
		if (image[indice-nbColonnes-1] == couleur)
		{
			return true;
		}
		if (image[indice-nbColonnes+1] == couleur)
		{
			return true;
		}
		if (image[indice+nbColonnes] == couleur)
		{
			return true;
		}
		if (image[indice-nbColonnes] == couleur)
		{
			return true;
		}
		return false;
	}
	/**
	 * Determine et renvoie si trois points sont alignes, a une approximation pres. 
	 * @param x1 Entier - Colonne du point 1
	 * @param y1 Entier - Ligne du point 1
	 * @param x2 Entier - Colonne du point 2
	 * @param y2 Entier - Ligne du point 2
	 * @param x3 Entier - Colonne du point 3
	 * @param y3 Entier - Ligne du point 3
	 * @param approximation Entier
	 * @return Booleen 
	 */
	public static boolean sontAlignes(int x1, int y1, int x2, int y2, int x3, int y3, int approximation)
	{
		int calcul = (y3-y1)*(x2-x1)-(y2-y1)*(x3-x1);
		return (Math.abs(calcul) <= approximation);
	}
	/**
	 * Calcule et renvoie la distance entre deux points. 
	 * @param x1 Entier - Colonne du point 1
	 * @param y1 Entier - Ligne du point 1
	 * @param x2 Entier - Colonne du point 2
	 * @param y2 Entier - Ligne du point 2
	 * @return Double
	 */
	public static double distance(int x1, int y1, int x2, int y2)
	{
		double xCarre = (double) (x2-x1)*(x2-x1);
		double yCarre = (double) (y2-y1)*(y2-y1);
		return (double) Math.sqrt(xCarre+yCarre);
	}
	/**
	 * Calcule et renvoie la roundness. 
	 * @param surface Entier
	 * @param perimetre Entier
	 * @return Double
	 */
	public static double roundness(int surface, int perimetre)
	{
		return (double) ((4*Math.PI*surface)/(perimetre*perimetre));
	}
	/**
	 * Execute une macro ImageJ de squelettisation. L'image doit etre binarisee avant. 
	 */
	public static void macroSquelette()
	{
		IJ.runMacro("run(\"Skeletonize\", \"stack\")");
	}
	/**
	 * Execute une macro ImageJ qui duplique l'image ouverte. 
	 */
	public static void macroDuplicate()
	{
		IJ.runMacro("run(\"Duplicate...\", \"duplicate\");");
	}
	/**
	 * Execute une macro ImageJ qui convertit l'image ouverte en RGB.
	 */
	public static void macroRGB()
	{
		IJ.runMacro("run(\"RGB Color\");");
	}
	/**
	 * Execute une macro ImageJ qui convertit l'image ouverte en 8 bits.
	 */
	public static void macro8bits()
	{
		IJ.runMacro("run(\"8-bit\");");
	}
	/**
	 * Ececute une macro ImageJ qui binarise l'image ouverte et ne garde que les objets d'une taille placee en parametre. L'image doit etre en 8 bits avant. 
	 * @param minPixels Entier - Taille minimale en pixels
	 * @param maxPixels Entier - Taille maximale en pixels
	 */
	public static void macroBinarise(int minPixels, int maxPixels)
	{
		String str = "";
		str += "run(\"Auto Threshold\", \"method=Otsu white stack\");";
		str += "run(\"Open\", \"stack\");";
		str += "run(\"Analyze Particles...\", \"size="+Integer.toString(minPixels)+"-"+Integer.toString(maxPixels)+" show=Masks clear stack\");";
		str += "run(\"Make Binary\", \"method=Default background=Light calculate black\");";
		macroDuplicate();
		IJ.runMacro(str);
	}
	/**
	 * Ececute une macro ImageJ qui ferme un certain nombre d'images ouvertes. 
	 * @param nbFois Entier
	 */
	public static void macroFermerImage(int nbFois)
	{
		String str = "";
		for (int i = 0; i < nbFois; i++)
		{
			str+= "run(\"Close\");";
		}
		IJ.runMacro(str);
	}
}