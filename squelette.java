package DetectionSquelettes;

import java.util.ArrayList;
import java.lang.Math;

/**
 * Classe representant le squelette d'un {@link Objet} avec ses intersections et branches. 
 * @author e1502316
 *
 */

public class Squelette {
	
	/**
	 * Tableau contenant les points formante le squelette. 
	 */
	public ArrayList <Integer> points;
	/**
	 * Tableau contenant les differentes branches du squelette. 
	 */
	public ArrayList <ArrayList <Integer>> branches;
	/**
	 * Tableau contenant les differentes intersections du squelette, avec un seul point par intersection. 
	 */
	public ArrayList <Integer> intersections;
	/**
	 * Tableau contenant les differentes intersections du squelette, mais plusieurs points correspondent parfois a une meme intersection.
	 * Utile pour du traitement et determination de branches. 
	 */
	private ArrayList <Integer> intersectionsDev;
	/**
	 * {@link Image} ou se trouve le squelette.
	 */
	public Image image;
	/**
	 * Constructeur prenant en parametre une image avec un indice d'un des points du squelette. Cree le squelette correspondant. 
	 * @param image {@link Image}
	 * @param indice Entier
	 */
	public Squelette(Image image, int indice)
	{
		this.points = new ArrayList <Integer>();
		this.intersections = new ArrayList <Integer>();
		this.intersectionsDev = new ArrayList <Integer>();
		this.branches = new ArrayList <ArrayList <Integer>>();
		this.image = image;
		ajouterPointsAutour(indice); // determine le squelette 
		determinerBranches();
	}
	/**
	 * Determine et renvoie si deux points aux indices places en parametre sont voisins. 
	 * @param indice1 Entier
	 * @param indice2 Entier
	 * @return Booleen 
	 */
	public boolean sontVoisins(int indice1, int indice2)
	{
		int x1 = image.colonne(indice1);
		int y1 = image.ligne(indice1);
		int x2 = image.colonne(indice2);
		int y2 = image.ligne(indice2);
		return ((Math.abs(x1-x2) == 1 && Math.abs(y1-y2) == 1) || (Math.abs(x1-x2) == 1 && Math.abs(y1-y2) == 0) || (Math.abs(x1-x2) == 0 && Math.abs(y1-y2) == 1));
	}
	/**
	 * Determine si un point d'indice place en parametre se situe dans {@link Squelette#intersectionsDev}.
	 * @param indicePoint
	 * @return
	 */
	private boolean estDansIntersections(int indicePoint)
	{
		for (int i = 0; i < intersectionsDev.size(); i++)
		{
			if (intersectionsDev.get(i) == indicePoint)
			{
				return true;
			}
		}
		return false;
	}
	/**
	 * Retourne le nombre de branches du squelette.
	 * @return Entier
	 */
	public int nombreBranches()
	{
		return branches.size();
	}
	/**
	 * Retourne la longueur du squelette. 
	 * @return Entier
	 */
	public int longueurSquelette()
	{
		return points.size();
	}
	/**
	 * Retourne la longueur de la branche de {@link Squelette#branches} d'indice place en parametre.
	 * @param indice Entier
	 * @return Entier
	 */
	public int longueurBrancheIndice(int indice)
	{
		return branches.get(indice).size();
	}
	/**
	 * Calcule et renvoie la moyenne des longueurs des branches du squelette. 
	 * @return Entier
	 */
	public int moyenneLongueurBranche()
	{
		int longueur;
		int longueurTotale = 0;
		for (int i = 0; i < nombreBranches(); i++)
		{
			longueur = longueurBrancheIndice(i);
			longueurTotale += longueur;
		}
		return (int) longueurTotale/nombreBranches();
	}
	/**
	 * Determine les branches du squelette. 
	 */
	public void determinerBranches()
	{
		ArrayList <Boolean> marque = new ArrayList <Boolean>();
		for (int i = 0; i < points.size(); i++)
		{
			marque.add(false);
		}
		for (int i = 0; i < points.size(); i++)
		{
			int tmp = i; 
			if (marque.get(i) == false)
			{
				marque.set(i, true);
				branches.add(new ArrayList <Integer>());
				branches.get(branches.size()-1).add(points.get(i));
				for (int j = 0; j < points.size(); j++)
				{
					if(marque.get(j) == false)
					{
						if (sontVoisins(points.get(i), points.get(j)))
						{
							marque.set(j, true);
							i = j;
							if (!estDansIntersections(points.get(j)))
							{
								branches.get(branches.size()-1).add(points.get(j));
							}
						}
					}
				}
			}
			i = tmp;
		}
	}
	/**
	 * Ajoute les points qui sont autour de l'indice place en parametre dans {@link Squelette#points}. Renvoie si l'indice en question correspond a un point. 
	 * @param indice Entier
	 * @return Booleen
	 */
	boolean ajouterPointsAutour(int indice)
	{
		if (image.estValide(indice))
		{
			if (image.pixelsSquelettesCopie[indice] == 0xffffff)
			{
				points.add(indice);
				/*
				 * Change legerement la valeur du pixel courant pour ne pas boucler dessus. 
				 * 0xfffffe = quasiment blanc. 
				 */
				image.pixelsSquelettesCopie[indice] = 0xfffffe;
				int nbVoisins = 0;
				/*
				 * Pour tous les points autour du pixel blanc, on appelle recursivement cette fonction, 
				 * on en profite pour compter le nombre de voisins du pixel courant. 
				 */
				if (ajouterPointsAutour(indice-1))
				{
					nbVoisins++;
				}
				if (ajouterPointsAutour(indice+1))
				{
					nbVoisins++;
				}
				if (ajouterPointsAutour(indice-image.nbColonnes))
				{
					nbVoisins++;
				}
				if (ajouterPointsAutour(indice+image.nbColonnes))
				{
					nbVoisins++;
				}
				if (ajouterPointsAutour(indice-image.nbColonnes+1))
				{
					nbVoisins++;
				}
				if (ajouterPointsAutour(indice+image.nbColonnes+1))
				{
					nbVoisins++;
				}
				if (ajouterPointsAutour(indice-image.nbColonnes-1))
				{
					nbVoisins++;
				}
				if (ajouterPointsAutour(indice+image.nbColonnes-1))
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
						image.pixelsSquelettesCopie[indice] = 0xff0000;
						intersections.add(indice);
					}
					intersectionsDev.add(indice);
				}
				return true;
			}
			if (image.pixelsSquelettesCopie[indice] == 0xfffffe)
			{
				return true;
			}
			return false;
		}
		return false;
	}
	/**
	 * Determine et renvoie s'il y a un voisin autour du point d'indice place en parametre dans un rayon et d'une couleur donnes. 
	 * @param indice Entier
	 * @param couleur Entier en hexadecimal
	 * @param rayon Entier
	 * @return Booleen 
	 */
	boolean contientVoisinDansRayon(int indice, int couleur, int rayon)
	{
		for (int l = 1; l <= rayon; l++)
		{
			if (image.pixelsSquelettesCopie[indice-(1*l)] == couleur)
			{
				return true;
			}
			if (image.pixelsSquelettesCopie[indice+(1*l)] == couleur)
			{
				return true;
			}
			if (image.pixelsSquelettesCopie[indice+(image.nbColonnes*l)] == couleur)
			{
				return true;
			}
			if (image.pixelsSquelettesCopie[indice-(image.nbColonnes*l)] == couleur)
			{
				return true;
			}
			for (int c = 1; c <= rayon; c++)
			{
				
				if (image.pixelsSquelettesCopie[indice+(image.nbColonnes*l)-(1*c)] == couleur)
				{
					return true;
				}
				if (image.pixelsSquelettesCopie[indice+(image.nbColonnes*l)+(1*c)] == couleur)
				{
					return true;
				}
				if (image.pixelsSquelettesCopie[indice-(image.nbColonnes*l)-(1*c)] == couleur)
				{
					return true;
				}
				if (image.pixelsSquelettesCopie[indice-(image.nbColonnes*l)+(1*c)] == couleur)
				{
					return true;
				}
			}
		}
		return false;
	}
	
}