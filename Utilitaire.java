package DetectionSquelettes;

public class Utilitaire {
	/**
	 * Determine s'il y a au moins un pixel de la couleur specifiee autour d'un indice donne. 
	 * @param indice (Entier) Indice du pixel pour le quel on souhaite savoir s'il a des voisins. 
	 * @param couleur (Entier) Couleur en hexadecimal de la couleur des voisins a tester. 
	 * @return Vrai s'il y a des voisins de la bonne couleur, faux sinon. 
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
	 * Determine si trois points sont alignes. 
	 * @param x1 (Entier) Colonne du premier point.
	 * @param y1 (Entier) Ligne du premier point.
	 * @param x2 (Entier) Colonne du deuxieme point.
	 * @param y2 (Entier) Ligne du deuxieme point.
	 * @param x3 (Entier) Colonne du troisieme point.
	 * @param y3 (Entier) Ligne du troisieme point.
	 * @param approximation (Entier) Approximation d'alignement tolere. 
	 * @return (Booleen) Vrai s'ils sont alignes, faux sinon. 
	 */
	public static boolean sontAlignes(int x1, int y1, int x2, int y2, int x3, int y3, int approximation)
	{
		int calcul = (y3-y1)*(x2-x1)-(y2-y1)*(x3-x1);
		return (Math.abs(calcul) <= approximation);
	}
	/**
	 * Calcule la distance entre deux points. 
	 * @param x1 (Entier) Colonne du premier point.
	 * @param y1 (Entier) Ligne du premier point.
	 * @param x2 (Entier) Colonne du deuxieme point.
	 * @param y2 (Entier) Ligne du deuxieme point.
	 * @return (Entier) Distance entre les deux points. 
	 */
	public static double distance(int x1, int y1, int x2, int y2)
	{
		double xCarre = (double) (x2-x1)*(x2-x1);
		double yCarre = (double) (y2-y1)*(y2-y1);
		return (double) Math.sqrt(xCarre+yCarre);
	}
}