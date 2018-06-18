package DetectionSquelettes;

import java.util.ArrayList;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

/**
 * Cette classe permet a partir d'une image ou d'un stack d'images de mettre en evidence certains objets a partir de criteres tels que 
 * la taille de l'objet, 
 * la longueur du squelette et
 * le nombre d'intersections dans le squelettes.
 * Elle encadre en rouge les objets repondant aux criteres. 
 * @author e1502316 Nicola Wilser
 *
 */

public class Detection_phyto_squelette implements PlugIn {
	/**
	 * Tableau des pixels de l'image que l'on est en train de manipuler. 
	 */
	private int[] pixels;
	/**
	 * Tableau des pixels de l'image d'origine, sans les alternations de traitements et d'analyse. 
	 */
	private int[] imageOrigine;
	/**
	 * Nombre de colonnes de l'image traitee. 
	 */
	private int nbColonnes;
	/**
	 * Nombre de lignes de l'image traitee.
	 */
	private int nbLignes;
	/**
	 * Taille de l'image en pixels. 
	 */
	private int taille;
	/**
	 * Tableau des squelettes de l'image actuelle. 
	 */
	private ArrayList <squelette> squelettes; 
	/**
	 * Tableau de tableaux de squelettes pour chaque image de l'ImageStack traitees. 
	 */
	/*
	 * Aucune obligation d'utiliser un stack d'images, il est tout à fait possible d'utiliser une image unique. 
	 */
	private ArrayList <ArrayList <squelette>> squelettesImages; // les squelettes de chaque image du stack
	/*
	 * Les variables suivant sont demandées lors de l'appel de la fonction run(), elles sont rentrées par l'utilisateur.
	 * Elles permettent de déterminer les formes à marquer. 
	 */
	/** 
	 * La taille minimum en pixels d'un squelette pour qu'on le considere pertinent.
	 */
	private int tailleMinSquelette; 
	/**
	 * Nombre d'intersections minimum pour qu'un squelette soit considere comme un objet pertinent.  
	 */
	private int minIntersection; 
	/**
	 * Nombre d'intersections maximum pour qu'un squelette soit considere comme un objet pertinent.
	 */
	private int maxIntersection;  
	/**
	 * Nombre de pixels minimum pour considerer une forme comme pertinente.
	 */
	private int minPixels;
	/**
	 * Nombre de pixels maximum pour considerer une forme comme pertinente.
	 */
	private int maxPixels;
	

	/**
	 * Renvoie la colonne pour un tableau 2D correspondante a un indice de tableau 1D. 
	 * @param indice (Entier) L'indice du tableau 1D.
	 * @return (Entier) La colonne equivalente en tableau 2D.
	 */
	int colonne(int indice)
	{
		return indice%nbColonnes;
	}
	/**
	 * Renvoie la ligne pour un tableau 2D correspondante a un indice de tableau 1D. 
	 * @param indice (Entier) L'indice du tableau 1D.
	 * @return (Entier) La ligne equivalente en tableau 2D.
	 */
	int ligne(int indice)
	{
		return indice/nbColonnes;
	}
	/**
	 * Renvoie l'indice pour un tableau 1D correspondant a la colonne et la ligne d'un tableau 2D.
	 * @param  ligne (Entier) La ligne du tableau 2D. 
	 * @param  colonne (Entier) La colonne du tableau 2D.
	 * @return (Entier) L'indice equivalent pour un tableau 1D.
	 */
	int indice(int ligne, int colonne)
	{
		return nbColonnes*ligne + colonne;
	}
	/**
	 * Ajoute dans le tableau squelettes tous les squelettes presents dans l'image respectant la taille minimum requise. 
	 */
	public void determinerSquelettes()
	{
		for (int i = nbColonnes+2; i < taille-nbColonnes-2; i++)
		{
			// Un pixel est retenu s'il est blanc. On a alors le debut d'un potentiel squelette. 
			if (pixels[i] == 0xffffff)
			{
				// Le squelette correspondant au pixel retenu est cree, c'est a dire qu'il contient maintenant tous les points blancs proches de lui...
				squelette tmp = new squelette(pixels, nbColonnes, i, taille);
				// mais on ne l'ajoute a la liste des squelettes que s'il a au moins tailleMinSquelette de points. 
				if (tmp.points.size() > tailleMinSquelette)
				{
					squelettes.add(tmp);
				}
			}
		}
	}
	/**
	 * Encadre un point d'une image avec un carre d'une certaine couleur specifiee en parametre. 
	 * @param position (Entier) Position autour de la quelle on souhaite faire l'encadrement. 
	 * @param tailleEncadrement (Entier) Taille du carre que l'on souhaite afficher. 
	 * @param couleur (Entier) Couleur en hexadecimal du carre. 
	 */
	public void encadrer(int position, int tailleEncadrement, int couleur)
	{
		int colonneX = colonne(position);
		int ligneX = ligne(position);
		// Tests de debordements 
		if (colonneX-tailleEncadrement > 0 && ligneX-tailleEncadrement > 0 && colonneX+tailleEncadrement < nbColonnes && ligneX+tailleEncadrement < nbLignes)
		{
			for (int col = colonneX-tailleEncadrement; col <= colonneX+tailleEncadrement; col++)
			{
				imageOrigine[indice(ligneX-tailleEncadrement, col)] = couleur;
				imageOrigine[indice(ligneX+tailleEncadrement, col)] = couleur;
			}
			for (int ligne = ligneX-tailleEncadrement; ligne <= ligneX+tailleEncadrement; ligne++)
			{
				imageOrigine[indice(ligne, colonneX-tailleEncadrement)] = couleur;
				imageOrigine[indice(ligne, colonneX+tailleEncadrement)] = couleur;
			}
		}
	}
	/**
	 * Calcule et renvoie la moyenne des points composants un squelette. 
	 * @param skull (squelette) Le squelette pour le quel on souhaite calculer la moyenne des points. 
	 * @return (Entier) La moyenne des points du squelette, sous forme d'indice de tableau 1D.
	 */
	public int moyenneIndice(squelette skull)
	{
		int x = 0;
		int y = 0;
		int i;
		for (i = 0; i < skull.points.size(); i++)
		{
			x += colonne(skull.points.get(i));
			y += ligne(skull.points.get(i));
		}
		x = (int) x/i;
		y = (int) y/i;
		return indice(y, x);
	}
	/**
	 * Encadre tous les squelettes d'une image.
	 * @param numeroImage (Entier) Numero de l'image pour la quelle on souhaite encadrer les squelettes (le numero de l'imageStack). 
	 */
	public void encadrerSquelettes(int numeroImage)
	{
		for (int i = 0; i < squelettesImages.get(numeroImage).size(); i++)
		{
			encadrer(moyenneIndice(squelettesImages.get(numeroImage).get(i)), 30, 0xffffff);
		}
	}
	/**
	 * Encadre uniquement les squelettes ayant un nombre d'intersections compris entre deux bornes. 
	 * @param numeroImage (Entier) Numero de l'image pour la quelle on souhaite encadrer les squelettes (le numero de l'imageStack). 
	 * @param min (Entier) Minimum d'intersections pour encadrer le squelette.
	 * @param max (Entier) Maximum d'intersections pour encadrer le squelette.
	 */
	public void encadrerSquelettesAvecNIntersections(int numeroImage, int min, int max)
	{
		for (int i = 0; i < squelettesImages.get(numeroImage).size(); i++)
		{
			if (squelettesImages.get(numeroImage).get(i).intersections.size() >= min && squelettesImages.get(numeroImage).get(i).intersections.size() <= max)
			{
				encadrer(moyenneIndice(squelettesImages.get(numeroImage).get(i)), 30, 0xff0000);
			}

		}
	}
	/**
	 * Change la valeur des pixels blancs en 0xffffff pour s'assurer de la coherence de celle-ci pour les futurs traitements. 
	 */
	public void blanchir()
	{
		for (int i = 0; i < taille; i++)
		{
			// si le pixel n'est pas noir
			if (!(pixels[i] == -0x1000000))
			{
				pixels[i] = 0xffffff;
			}
		}
	}
	/**
	 * Cree les squelettes des objets pertinents de l'image courante. 
	 */
	/*
	 * Execute une macro qui : 
	 * duplique l'image courante, 
	 * la binarise avec la methode d'Otsu, 
	 * supprime les imperfections et les objets de taille inappropriee,
	 *  et cree les squelettes des objets retenus. 
	 */
	public void macroSquelette()
	{
		IJ.runMacro("run(\"Duplicate...\", \"duplicate\");run(\"Auto Threshold\", \"method=Otsu white stack\");run(\"Open\", \"stack\");run(\"Analyze Particles...\", \"size="+Integer.toString(minPixels)+"-"+Integer.toString(maxPixels)+" show=Masks clear stack\");setOption(\"BlackBackground\", true);run(\"Make Binary\", \"method=Default background=Light calculate black\");run(\"Skeletonize\", \"stack\");run(\"RGB Color\");");
	}
	/**
	 * Ferme les copies d'images utilisees pour les traitements intermediaires. 
	 */
	public void macroFermerImageCopie()
	{
		IJ.runMacro("run(\"Close\");run(\"Close\");run(\"RGB Color\");");
	}
	/**
	 * Methode principale qui demande a l'utilisateur les parametres pour detecter les objets pertinents puis analyse la ou les image(s) pour marquer ces objets en les encadrant. 
	 */
	public void run(String arg) {
		if (lireParam())
		{
			macroSquelette();
			ImagePlus imp = IJ.getImage();
			ImageProcessor im  = imp.getProcessor();
			nbColonnes = im.getWidth();
			nbLignes = im.getHeight();
			taille = nbColonnes*nbLignes;
			ImageStack stk = imp.getImageStack();
			squelettesImages = new ArrayList <ArrayList <squelette>>();
			/*
			 * Attention les indices d'un stack d'images commencent a 1, d'ou les boucles qui vont de 1 à la taille du stack.
			 * Cependant les indices des ArrayLists commencent a 0, d'ou les i-1 lors des encadrements. 
			 */
			for (int i = 1; i <= stk.getSize(); i++)
			{
				squelettes = new ArrayList <squelette>();
				pixels = (int[]) stk.getPixels(i);
				blanchir();
				determinerSquelettes();
				squelettesImages.add(squelettes);
			}	
			macroFermerImageCopie();
			ImagePlus imp2 = IJ.getImage();
			ImageStack stk2 = imp2.getImageStack();
			for (int i = 1; i <= stk2.getSize(); i++)
			{
				imageOrigine = (int[]) stk2.getPixels(i);
				encadrerSquelettes(i-1);
				encadrerSquelettesAvecNIntersections(i-1, minIntersection, maxIntersection);
			}
		}
		
	}
	/**
	 * Permet a l'utilisateur de rentrer les parametres d'analyse des images. 
	 * @return (Booleen) Vrai si la fonction s'est bien terminee, faux si elle a ete annulee. 
	 */
	boolean lireParam() {
		GenericDialog gd = new GenericDialog("Paramètres", IJ.getInstance());
		gd.addNumericField("Nombre de pixels min pour considérer une forme", 1000, 0);
		gd.addNumericField("Nombre de pixels max pour considérer une forme", 14000, 0);
		gd.addNumericField("Taille du squelette min pour le considérer", 10, 0);
		gd.addNumericField("Nombre d'intersections min dans le squelette", 2, 0);
		gd.addNumericField("Nombre d'intersections max dans le squelette", 3, 0);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		minPixels = (int) gd.getNextNumber();
		maxPixels = (int) gd.getNextNumber();
		tailleMinSquelette = (int) gd.getNextNumber();
		minIntersection = (int) gd.getNextNumber();
		maxIntersection = (int) gd.getNextNumber();
		return true;
	}
}