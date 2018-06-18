package DetectionSquelettes;

import java.util.ArrayList;

/**
 * Cette classe permet de creer des squelettes d'objets en stockant les indices de tous leurs points et en determinant les intersections du squelette. 
 * @author e1502316 Nicola Wilser
 *
 */

public class squelette {
	/**
	 * Tableau contenant tous les points du squelette sous forme d'indice de tableau 1D.
	 */
	public ArrayList <Integer> points;
	/**
	 * Tableau contenant toutes les intersections du squelette sous forme d'indice de tableau 1D.
	 * Un seul point par intersection est stocke. 
	 */
	public ArrayList <Integer> intersections;
	/**
	 * Tableau de pixels de l'image que l'on analyse et qui contient l'objet avec son squelette. 
	 */
	public int[] image;
	/**
	 * Taille de l'image en pixels. 
	 */
	public int taille;
	/**
	 * Nombre de colonnes de l'image. 
	 */
	public int nbColonnes;

	public squelette()
	{
	}
	/**
	 * Constructeur qui determine et stocke le squelette relatif au pixel indice de l'image. 
	 * @param image (int[]) Tableau des pixels de l'image. 
	 * @param nbColonnes (Entier) Nombre de colonnes de l'image. 
	 * @param indice (Entier) Indice du point d'interet du squelette a determiner. 
	 * @param taille (Entier) Taille de l'image en pixels. 
	 */
	public squelette(int[] image, int nbColonnes, int indice, int taille)
	{
		points = new ArrayList <Integer>();
		intersections = new ArrayList <Integer>();
		this.image = image;
		this.nbColonnes = nbColonnes;
		this.taille = taille;
		verifierAutourDuPoint(indice);
	}
	/**
	 * Determine si l'indice place en parametre est valide (tests de debordements). 
	 * @param indice (Entier) L'indice a tester. 
	 * @return (Booleen) Vrai si l'indice est valide, faux sinon. 
	 */
	boolean estValide(int indice)
	{
		if (indice < 0 || indice >= taille)
		{
			return false;
		}
		return true;
	}
	/**
	 * Blanchit les pixels de l'image qui ne sont pas parfaitement blancs. 
	 */
	void remettreBlanc()
	{
		for (int i = 0; i < taille; i++)
		{
			if (image[i] == 0xfffffe)
			{
				image[i] = 0xffffff;
			}
		}
	}
	/**
	 * Fonction qui rajoute le point courant s'il est blanc dans le tableau de points et le rajoute dans le tableau d'intersections si c'en est un aussi. 
	 * Fait tout cela pour tous les points pertinants autour de l'indice specifie en parametre. 
	 * @param indice (Entier) Indice du point que l'on veut verifier et ajouter s'il est valide. 
	 * @return (Booleen) Vrai si le point est blanc, faux sinon. 
	 */
	boolean verifierAutourDuPoint(int indice)
	{
		if (estValide(indice))
		{
			if (image[indice] == 0xffffff)
			{
				points.add(indice);
				/*
				 * Change legerement la valeur du pixel courant pour ne pas boucler dessus. 
				 * 0xfffffe = quasiment blanc. 
				 */
				image[indice] = 0xfffffe;
				int nbVoisins = 0;
				/*
				 * Pour tous les points autour du pixel blanc, on appelle recursivement cette fonction, 
				 * on en profite pour compter le nombre de voisins du pixel courant. 
				 */
				if (verifierAutourDuPoint(indice-1))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice+1))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice-nbColonnes))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice+nbColonnes))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice-nbColonnes+1))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice+nbColonnes+1))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice-nbColonnes-1))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice+nbColonnes-1))
				{
					nbVoisins++;
				}
				/*
				 * S'il y a plus de 2 voisins on considere que l'on est a une intersection. 
				 * On verifie qu'il n'y a pas deja de voisin considere comme une intersection avant d'ajouter le point courant dans le tableau d'intersection.
				 * On ne souhaite avoir qu'un seul point par intersection. 
				 */
				if (nbVoisins > 2)
				{
					if (!contientVoisinDansRayon(indice, 0xff0000, 2))
					{
						image[indice] = 0xff0000;
						intersections.add(indice);
					}
				}
				return true;
			}
			if (image[indice] == 0xfffffe)
			{
				return true;
			}
			return false;
		}
		return false;
	}
	/**
	 * Determine s'il y a au moins un pixel de la couleur specifiee dans un rayon autour d'un indice donne. 
	 * @param indice (Entier) Indice du pixel pour le quel on souhaite savoir s'il a des voisins. 
	 * @param couleur (Entier) Couleur en hexadecimal de la couleur des voisins a tester. 
	 * @param rayon (Entier) Rayon sur le quel on effectue le test. 
	 * @return Vrai s'il y a des voisins de la bonne couleur, faux sinon. 
	 */
	boolean contientVoisinDansRayon(int indice, int couleur, int rayon)
	{
		for (int l = 1; l <= rayon; l++)
		{
			if (image[indice-(1*l)] == couleur)
			{
				return true;
			}
			if (image[indice+(1*l)] == couleur)
			{
				return true;
			}
			if (image[indice+(nbColonnes*l)] == couleur)
			{
				return true;
			}
			if (image[indice-(nbColonnes*l)] == couleur)
			{
				return true;
			}
			for (int c = 1; c <= rayon; c++)
			{
				
				if (image[indice+(nbColonnes*l)-(1*c)] == couleur)
				{
					return true;
				}
				if (image[indice+(nbColonnes*l)+(1*c)] == couleur)
				{
					return true;
				}
				if (image[indice-(nbColonnes*l)-(1*c)] == couleur)
				{
					return true;
				}
				if (image[indice-(nbColonnes*l)+(1*c)] == couleur)
				{
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Determine s'il y a au moins un pixel de la couleur specifiee autour d'un indice donne. 
	 * @param indice (Entier) Indice du pixel pour le quel on souhaite savoir s'il a des voisins. 
	 * @param couleur (Entier) Couleur en hexadecimal de la couleur des voisins a tester. 
	 * @return Vrai s'il y a des voisins de la bonne couleur, faux sinon. 
	 */
	boolean contientVoisin(int indice, int couleur)
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
	
}