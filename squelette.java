package DetectionSquelettes;

import java.util.ArrayList;
import java.lang.Math;

public class Squelette {
	
	public ArrayList <Integer> points;
	
	public ArrayList <ArrayList <Integer>> branches;
	
	public ArrayList <Integer> intersections;
	public ArrayList <Integer> intersectionsDev;
	
	public Image image;
	
	public Squelette(Image image, int indice)
	{
		this.points = new ArrayList <Integer>();
		this.intersections = new ArrayList <Integer>();
		this.intersectionsDev = new ArrayList <Integer>();
		this.branches = new ArrayList <ArrayList <Integer>>();
		this.image = image;
		verifierAutourDuPoint(indice);
		determinerBranches();
	}
	
	public boolean sontVoisins(int indice1, int indice2)
	{
		int x1 = image.colonne(indice1);
		int y1 = image.ligne(indice1);
		int x2 = image.colonne(indice2);
		int y2 = image.ligne(indice2);
		return ((Math.abs(x1-x2) == 1 && Math.abs(y1-y2) == 1) || (Math.abs(x1-x2) == 1 && Math.abs(y1-y2) == 0) || (Math.abs(x1-x2) == 0 && Math.abs(y1-y2) == 1));
	}
	
	public boolean estDansIntersections(int indicePoint)
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
	
	public int nombreBranches()
	{
		return branches.size();
	}
	
	public int longueurSquelette()
	{
		return points.size();
	}
	
	public int longueurBrancheIndice(int indice)
	{
		return branches.get(indice).size();
	}
	
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
	
	void remettreBlanc()
	{
		for (int i = 0; i < image.taille; i++)
		{
			if (image.pixelsSquelettesCopie[i] == 0xfffffe)
			{
				image.pixelsSquelettesCopie[i] = 0xffffff;
			}
		}
	}
	
	boolean verifierAutourDuPoint(int indice)
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
				if (verifierAutourDuPoint(indice-1))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice+1))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice-image.nbColonnes))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice+image.nbColonnes))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice-image.nbColonnes+1))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice+image.nbColonnes+1))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice-image.nbColonnes-1))
				{
					nbVoisins++;
				}
				if (verifierAutourDuPoint(indice+image.nbColonnes-1))
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