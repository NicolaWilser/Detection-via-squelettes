package DetectionSquelettes;

import java.util.ArrayList;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.gui.Line;
import java.lang.Math;
import ij.measure.ResultsTable;

/**
 * Cette classe permet a partir d'une image ou d'un stack d'images de mettre en evidence certains objets a partir de criteres tels que 
 * la taille de l'objet, 
 * la longueur du squelette et,
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
	private int[] imageBinaire;
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
	 * Contient le nombre d'elements pertinents pour chaque image du stack actuelle. 
	 */
	private ArrayList <Integer> nbElementsPertinents;
	/**
	 * Tableau de tableaux de squelettes pour chaque image de l'ImageStack traitees.
	 */
	/*
	 * Aucune obligation d'utiliser un stack d'images, il est tout à fait possible d'utiliser une image unique. 
	 */
	private ArrayList <ArrayList <squelette>> squelettesImages; // les squelettes de chaque image du stack
	/**
	 * Contient tous les squelettes pertinents du stack d'images. 
	 */
	private ArrayList <squelette> squelettesPertinents; 
	/**
	 * Contient la surface des objets pertinents de chaque image. 
	 */
	private ArrayList <ArrayList <Integer>> surfaceSquelettesPertinents;
	/**
	 * Contient le perimetre des objets pertinents de chaque image. 
	 */
	private ArrayList <ArrayList <Integer>> perimetreSquelettesPertinents;
	private ArrayList <ArrayList <squelette>> squelettesPertinentsImages;
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
	 * @param couleur (Entier) Couleur en hexadecimal du carre. 
	 */
	public void encadrerSquelettes(int numeroImage, int couleur)
	{
		for (int i = 0; i < squelettesImages.get(numeroImage).size(); i++)
		{
			encadrer(moyenneIndice(squelettesImages.get(numeroImage).get(i)), 30, couleur);
		}
	}
	/**
	 * Encadre uniquement les squelettes ayant un nombre d'intersections compris entre deux bornes. 
	 * @param numeroImage (Entier) Numero de l'image pour la quelle on souhaite encadrer les squelettes (le numero de l'imageStack). 
	 * @param min (Entier) Minimum d'intersections pour encadrer le squelette.
	 * @param max (Entier) Maximum d'intersections pour encadrer le squelette.
	 * @param couleur (Entier) Couleur en hexadecimal du carre. 
	 */
	public void encadrerSquelettesAvecNIntersections(int numeroImage, int min, int max, int couleur)
	{
		for (int i = 0; i < squelettesImages.get(numeroImage).size(); i++)
		{
			if (estPertinent(numeroImage, i))
			{
				encadrer(moyenneIndice(squelettesImages.get(numeroImage).get(i)), 30, couleur);
			}
		}
	}
	/**
	 * Encadre tous les squelettes pertinents du stack d'image sur une image précise du stack. 
	 * @param indiceImage (Entier) Indice de l'image sur le quel on fait les encadrements.
	 * @param couleur (Entier) Couleur en hexadecimal de l'encadrement.
	 */
	public void encadrerTousLesSquelettesPertinents(int couleur)
	{
		for (int i = 0; i < squelettesPertinents.size(); i++)
		{
			encadrer(moyenneIndice(squelettesPertinents.get(i)), 30, couleur);
		}
	}
	/**
	 * Encadre tous les squelettes pertinents du stack d'image sauf ceux d'une image précise.
	 * @param indiceImage (Entier) Indice de l'image pour la quelle on ne suite pas d'encadrements. 
	 * @param couleur (Entier) Couleur en hexadecimal de l'encadrement.
	 */
	public void encadrerTousLesSquelettesPertinentsSauf(int indiceImage, int couleur)
	{
		for (int i = 0; i < squelettesPertinents.size(); i++)
		{
			int k = 0;
			while (k < squelettesImages.get(indiceImage).size() && (!sontDansMemeZone(squelettesPertinents.get(i), squelettesImages.get(indiceImage).get(k), 50)
					|| !(estPertinent(indiceImage, k))))
			{
				k++;
			}
			if (k == squelettesImages.get(indiceImage).size())
			{
				encadrer(moyenneIndice(squelettesPertinents.get(i)), 30, couleur);
			}
		}
	}
	/**
	 * Compte le nombre d'objets pertinents pour une image donnee. 
	 * @param numeroImage (Entier) Numero de l'image pour la quelle on souhaite encadrer les squelettes (le numero de l'imageStack). 
	 * @param min (Entier) Minimum d'intersections pour encadrer le squelette.
	 * @param max (Entier) Maximum d'intersections pour encadrer le squelette.
	 */
	public void compterElementsPertinents(int numeroImage, int min, int max)
	{
		for (int i = 0; i < squelettesImages.get(numeroImage).size(); i++)
		{
			if (estPertinent(numeroImage, i))
			{
				nbElementsPertinents.set(numeroImage, nbElementsPertinents.get(numeroImage)+1); // incrémente le nombre d'elements pertinents dans l'ArrayList pour l'image i du stack
			}
		}
	}
	/**
	 * Calcule la meilleure image du stack, celle contenant le plus d'objets pertinents. 
	 * @return (Entier) Indice de l'image du stack avec le plus d'objets pertinents. 
	 */
	public int calculerMeilleureImage()
	{
		int meilleure = 0;
		for (int i = 0; i < nbElementsPertinents.size(); i++)
		{
			if (nbElementsPertinents.get(i) >= nbElementsPertinents.get(meilleure))
			{
				meilleure = i;
			}
		}
		/*
		 * On renvoie +1 car les images du stack sont indexes a partir de 1 et non pas 0 comme un tableau habituel.  
		 */
		return meilleure+1;
	}
	/**
	 * Change la valeur des pixels blancs en 0xffffff pour s'assurer de la coherence de celle-ci pour les futurs traitements. 
	 */
	public void blanchir(int[] pixels)
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
	 * et cree les squelettes des objets retenus. 
	 */
	public void macroSquelette()
	{
		IJ.runMacro("run(\"Skeletonize\", \"stack\");run(\"RGB Color\");");
	}
	/**
	 * Lance la macro pour convertir l'image courante en RGB. 
	 */
	public void macroRGB()
	{
		IJ.runMacro("run(\"RGB Color\");");
	}
	/**
	 * Lance la macro pour convertir l'image courante en 8 bits en faisant une copie. 
	 */
	public void macro8bits()
	{
		IJ.runMacro("run(\"Duplicate...\", \"duplicate\");run(\"8-bit\");");
	}
	/**
	 * Binarise l'image courante en faisant une copie. 
	 */
	public void macroBinarise()
	{
		IJ.runMacro("run(\"Duplicate...\", \"duplicate\");run(\"Auto Threshold\", \"method=Otsu white stack\");run(\"Open\", \"stack\");run(\"Analyze Particles...\", \"size="+Integer.toString(minPixels)+"-"+Integer.toString(maxPixels)+" show=Masks clear stack\");setOption(\"BlackBackground\", true);run(\"Make Binary\", \"method=Default background=Light calculate black\");");
	}
	/**
	 * Ferme les copies d'images utilisees pour les traitements intermediaires. 
	 */
	public void macroFermerImageCopie(int nbFois)
	{
		String str = "";
		for (int i = 0; i < nbFois; i++)
		{
			str+= "run(\"Close\");";
		}
		IJ.runMacro(str+";run(\"RGB Color\");");
	}
	/**
	 * Indique si deux squelettes se situent dans la meme zone, à une approximation pres. 
	 * @param s1 (squelette) Premier squelette
	 * @param s2 (squelette) Deuxieme squelette
	 * @param approximation (Entier) Approximation toleree 
	 * @return (Booleen) Vrai si les deux squelettes sont dans la meme zone, faux sinon. 
	 */
	public boolean sontDansMemeZone(squelette s1, squelette s2, int approximation)
	{
		int indiceMoyenS1 = moyenneIndice(s1);
		int indiceMoyenS2 = moyenneIndice(s2);
		int x1 = colonne(indiceMoyenS1);
		int y1 = ligne(indiceMoyenS1);
		int x2 = colonne(indiceMoyenS2);
		int y2 = ligne(indiceMoyenS2);
		if (Math.abs(x1-x2) <= approximation && Math.abs(y1-y2) <= approximation)
		{
			return true;
		}
		return false;
	}
	/**
	 * Determine tous les squelettes pertinents du stack d'images et les stocke dans une ArrayList. 
	 */
	public void determinerTousLesSquelettesPertinents()
	{
		for (int j = 0; j < squelettesImages.size(); j++)
		{
			for (int i = 0; i < squelettesImages.get(j).size(); i++)
			{
				if (estPertinent(j, i))
				{
					int k = 0;
					while (k < squelettesPertinents.size() && !sontDansMemeZone(squelettesImages.get(j).get(i), squelettesPertinents.get(k), 50))
					{
						k++;
					}
					if (k == squelettesPertinents.size())
					{
						squelettesPertinents.add(squelettesImages.get(j).get(i));
					}
				}
			}
			
		}
	}
	/**
	 * Marque les differents alignements entre les points s'il y en a. 
	 * @param ip (ImageProcessor) ImageProcessor pour la quelle on veut marquer les alignements. 
	 */
	public void marquerAlignements(ImageProcessor ip)
	{
		ArrayList <Integer> pointsDesSquelettes = new ArrayList <Integer>();
		for (int i = 0; i < squelettesPertinents.size(); i++)
		{
			pointsDesSquelettes.add(moyenneIndice(squelettesPertinents.get(i)));
		}
		int indice1, indice2, indice3, x1, x2, x3, y1, y2, y3;
		for (int i = 0; i < squelettesPertinents.size(); i++)
		{
			indice1 = pointsDesSquelettes.get(i);
			x1 = colonne(indice1);
			y1 = ligne(indice1);
			for (int j = 0; j < squelettesPertinents.size(); j++)
			{
				indice2 = pointsDesSquelettes.get(j);
				x2 = colonne(indice2);
				y2 = ligne(indice2);
				for (int k = 0; k < squelettesPertinents.size(); k++)
				{
					indice3 = pointsDesSquelettes.get(k);
					x3 = colonne(indice3);
					y3 = ligne(indice3);
					if (i != j && j != k)
					{
						if (Utilitaire.sontAlignes(x1, y1, x2, y2, x3, y3, 10))
						{
							Line l;
							if (Utilitaire.distance(x1, y1, x2, y2) > Utilitaire.distance(x1, y1, x3, y3))
							{
								if (Utilitaire.distance(x1, y1, x2, y2) > Utilitaire.distance(x2, y2, x3, y3))
								{
									l = new Line(x1, y1, x2, y2);
									System.out.println("1");
								}
								else
								{
									l = new Line(x2, y2, x3, y3);
									System.out.println("2");
								}
							}
							else
							{
								if (Utilitaire.distance(x1, y1, x3, y3) > Utilitaire.distance(x2, y2, x3, y3))
								{
									l = new Line(x1, y1, x3, y3);
									System.out.println("3");
								}
								else
								{
									l = new Line(x2, y2, x3, y3);
									System.out.println("4");
								}
							}
							l.drawPixels(ip);
						}
					}
				}
			}
		}
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
	 * Calcule et renvoie la surface en pixels d'un objet se trouvant a un indice. 
	 * @param indiceObjet (Entier) Indice de l'objet. 
	 * @return (Entier) Surface en pixels de l'objet.
	 */
	public int surfaceObjet(int indiceObjet)
	{
		if (estValide(indiceObjet) && imageBinaire[indiceObjet] == 0xffffff)
		{
			imageBinaire[indiceObjet] = 0xfffffe;
			return (surfaceObjet(indiceObjet+1)+surfaceObjet(indiceObjet-1)+
					surfaceObjet(indiceObjet-nbColonnes)+surfaceObjet(indiceObjet+nbColonnes)+
					surfaceObjet(indiceObjet-nbColonnes+1)+surfaceObjet(indiceObjet+nbColonnes+1)+
					surfaceObjet(indiceObjet-nbColonnes-1)+surfaceObjet(indiceObjet+nbColonnes-1))+1;
		}
		else
		{
			return 0;
		}
	}
	public int perimetreObjet(int indiceObjet)
	{
		if (estValide(indiceObjet) && imageBinaire[indiceObjet] == 0xffffff)
		{
			imageBinaire[indiceObjet] = 0xfffffe;
			if (Utilitaire.contientVoisin(indiceObjet, -0x1000000, imageBinaire, nbColonnes))
			{
				return (perimetreObjet(indiceObjet+1)+perimetreObjet(indiceObjet-1)+
						perimetreObjet(indiceObjet-nbColonnes)+perimetreObjet(indiceObjet+nbColonnes)+
						perimetreObjet(indiceObjet-nbColonnes+1)+perimetreObjet(indiceObjet+nbColonnes+1)+
						perimetreObjet(indiceObjet-nbColonnes-1)+perimetreObjet(indiceObjet+nbColonnes-1))+1;
			}
			else
			{
				return (perimetreObjet(indiceObjet+1)+perimetreObjet(indiceObjet-1)+
						perimetreObjet(indiceObjet-nbColonnes)+perimetreObjet(indiceObjet+nbColonnes)+
						perimetreObjet(indiceObjet-nbColonnes+1)+perimetreObjet(indiceObjet+nbColonnes+1)+
						perimetreObjet(indiceObjet-nbColonnes-1)+perimetreObjet(indiceObjet+nbColonnes-1));
			}
		}
		else
		{
			return 0;
		}
	}
	public boolean estPertinent(int numeroImage, int indiceObjet)
	{
		return (squelettesImages.get(numeroImage).get(indiceObjet).intersections.size() >= minIntersection && squelettesImages.get(numeroImage).get(indiceObjet).intersections.size() <= maxIntersection);
	}
	/**
	 * Calcule et remplit l'ArrayList surfaceSquelettesPertinents avec la surface de tous les objets de tous les images du stack. 
	 * @param stk (ImageStack) Stack d'images que l'on souhaite analyser.
	 */
	public void calculerSurfaceObjetsInterets(ImageStack stk)
	{
		for (int numeroImage = 0; numeroImage < squelettesImages.size(); numeroImage++)
		{
			imageBinaire = (int[]) stk.getProcessor(numeroImage+1).getPixels();
			blanchir(imageBinaire); 
			surfaceSquelettesPertinents.add(new ArrayList <Integer>());
			for (int i = 0; i < squelettesPertinentsImages.get(numeroImage).size(); i++)
			{
				surfaceSquelettesPertinents.get(numeroImage).add(surfaceObjet(moyenneIndice(squelettesPertinentsImages.get(numeroImage).get(i))));
			}
		}
		blanchir(imageBinaire);
	}
	public void calculerPerimetreObjetsInterets(ImageStack stk)
	{
		for (int numeroImage = 0; numeroImage < squelettesImages.size(); numeroImage++)
		{
			imageBinaire = (int[]) stk.getProcessor(numeroImage+1).getPixels();
			blanchir(imageBinaire); 
			perimetreSquelettesPertinents.add(new ArrayList <Integer>());
			for (int i = 0; i < squelettesPertinentsImages.get(numeroImage).size(); i++)
			{
				perimetreSquelettesPertinents.get(numeroImage).add(perimetreObjet(moyenneIndice(squelettesPertinentsImages.get(numeroImage).get(i))));
			}
		}
		blanchir(imageBinaire);
	}
	/**
	 * Affiche les surfaces moyennes des objets de chaque image dans la console. Utilise pour les tests. 
	 */
	public void afficherSurfaces()
	{
		int surface, surfaceTotale, taille;
		for (int numeroImage = 0; numeroImage < squelettesImages.size(); numeroImage++)
		{
			System.out.println("Image numero "+Integer.toString(numeroImage+1)+"\n");
			taille = surfaceSquelettesPertinents.get(numeroImage).size();
			surfaceTotale = 0;
			int i;
			for (i = 0; i < taille; i++)
			{
				surface = surfaceSquelettesPertinents.get(numeroImage).get(i);
				surfaceTotale += surface;
				System.out.println("Objet "+Integer.toString(i+1)+" | surface : "+Integer.toString(surface)+"\n");
			}
			if (i == 0)
			{
				System.out.println("Aucun objet détecté. \n");
			}
			else
			{
				System.out.println("Surface moyenne : "+Integer.toString((int) surfaceTotale/taille)+"\n");
			}
		}
	}
	public double roundness(int indiceImage, int indiceSquelette)
	{
		return (surfaceSquelettesPertinents.get(indiceImage).get(indiceSquelette)*4*Math.PI)/(perimetreSquelettesPertinents.get(indiceImage).get(indiceSquelette)*perimetreSquelettesPertinents.get(indiceImage).get(indiceSquelette));
	}
	/**
	 * Cree et affiche un tableau de resultats contenant diverses informations de l'analyse effectuee sur le stack d'images. 
	 */
	public void tableauDeResultats()
	{
		ResultsTable rt = new ResultsTable(squelettesImages.size()); 
		int surface, surfaceTotale, perimetre, perimetreTotal, nbBranches, tailleMoyenneBranches, nbBranchesTotal, tailleMoyenneBranchesTotale, taille;
		double roundness, roundnessTotale;
		for (int numeroImage = 0; numeroImage < squelettesImages.size(); numeroImage++)
		{
			rt.setValue("Image", numeroImage, numeroImage+1);
			taille = surfaceSquelettesPertinents.get(numeroImage).size();
			rt.setValue("Nombre d'objets détectés", numeroImage, taille);
			surfaceTotale = 0;
			perimetreTotal = 0;
			roundnessTotale = 0;
			nbBranchesTotal = 0;
			tailleMoyenneBranchesTotale = 0; 
			int i;
			for (i = 0; i < taille; i++)
			{
				surface = surfaceSquelettesPertinents.get(numeroImage).get(i);
				perimetre = perimetreSquelettesPertinents.get(numeroImage).get(i);
				nbBranches = squelettesPertinentsImages.get(numeroImage).get(i).nombreBranches();
				tailleMoyenneBranches = squelettesPertinentsImages.get(numeroImage).get(i).moyenneLongueurBranche();
				roundness = roundness(numeroImage, i);
				surfaceTotale += surface;
				perimetreTotal += perimetre;
				roundnessTotale += roundness;
				nbBranchesTotal += nbBranches;
				tailleMoyenneBranchesTotale += tailleMoyenneBranches;
			}
			if (i != 0)
			{
				rt.setValue("Surface moyenne", numeroImage, surfaceTotale/taille);
				rt.setValue("Perimetre moyen", numeroImage, perimetreTotal/taille);
				rt.setValue("Roundness moyenne", numeroImage, roundnessTotale/taille);
				rt.setValue("Nb de branches moyen", numeroImage, (double) nbBranchesTotal/((double) taille));
				rt.setValue("Taille de branche moyenne", numeroImage, tailleMoyenneBranchesTotale/taille);
			}
			else
			{
				rt.setValue("Surface moyenne", numeroImage, 0);
				rt.setValue("Perimetre moyen", numeroImage, 0);
				rt.setValue("Roundness moyenne", numeroImage, 0);
				rt.setValue("Nb de branches moyen", numeroImage, 0);
				rt.setValue("Taille de branche moyenne", numeroImage, 0);
			}
		}
		macroFermerImageCopie(3);
		rt.show("Résultats de l'analyse");
	}
	public void determinerSquelettesPertinents()
	{
		for (int i = 0; i < squelettesImages.size(); i++)
		{
			squelettesPertinentsImages.add(new ArrayList <squelette>());
			for (int j = 0; j < squelettesImages.get(i).size(); j++)
			{
				if (estPertinent(i, j))
				{
					squelettesPertinentsImages.get(i).add(squelettesImages.get(i).get(j));
				}
			}
		}
	}
	/**
	 * Initialise les differentes variables a des tableaux vides. 
	 */
	public void initialiserVariables()
	{
		nbElementsPertinents = new ArrayList <Integer>();
		squelettesPertinents = new ArrayList <squelette>();
		surfaceSquelettesPertinents = new ArrayList <ArrayList <Integer>>();
		squelettesImages = new ArrayList <ArrayList <squelette>>();
		perimetreSquelettesPertinents = new ArrayList <ArrayList <Integer>>();
		squelettesPertinentsImages = new ArrayList <ArrayList <squelette>>();
	}
	/**
	 * Charge une ImageStack et initialise les variables nbColonnes, nbLignes et taille.
	 * @return (ImageStack) ImageStack correspondant au stack ouvert. 
	 */
	public ImageStack chargerImageStack()
	{
		ImagePlus imp = IJ.getImage();
		ImageProcessor im  = imp.getProcessor();
		nbColonnes = im.getWidth();
		nbLignes = im.getHeight();
		taille = nbColonnes*nbLignes;
		ImageStack stk = imp.getImageStack();
		return stk;
	}
	/**
	 * Determine tous les squelettes de chaque image du stack passe en parametre. 
	 * @param stk (ImageStack) Stack d'images. 
	 */
	public void determinerSquelettesDuStack(ImageStack stk)
	{
		for (int i = 1; i <= stk.getSize(); i++)
		{
			squelettes = new ArrayList <squelette>();
			pixels = (int[]) stk.getPixels(i);
			blanchir(pixels);
			determinerSquelettes();
			squelettesImages.add(squelettes);
			nbElementsPertinents.add(0); // initialise l'ArrayList a 0
		}	
	}
	/**
	 * Encadre tous les objets du stack d'images.
	 * @param stk (ImageStack) Stack d'images. 
	 */
	public void encadrerObjetsDuStack(ImageStack stk)
	{
		for (int i = 1; i <= stk.getSize(); i++)
		{
			imageOrigine = (int[]) stk.getPixels(i);
			encadrerSquelettesAvecNIntersections(i-1, minIntersection, maxIntersection, 0x00ff00);
			compterElementsPertinents(i-1, minIntersection, maxIntersection);
		}
		int meilleureImage = calculerMeilleureImage();
		imageOrigine = (int[]) stk.getPixels(meilleureImage);
		determinerTousLesSquelettesPertinents();
		encadrerTousLesSquelettesPertinentsSauf(meilleureImage-1, 0xff0000);
	}
	/**
	 * Methode principale qui demande a l'utilisateur les parametres pour detecter les objets pertinents puis analyse la ou les image(s) pour marquer ces objets en les encadrant. 
	 */
	public void run(String arg) {
		if (lireParam())
		{
			initialiserVariables();
			macroBinarise();
			macroSquelette();
			macroRGB();
			ImageStack stk = chargerImageStack();
			determinerSquelettesDuStack(stk);
			determinerSquelettesPertinents();
			macroFermerImageCopie(2);
			ImagePlus imp2 = IJ.getImage();
			ImageStack stk2 = imp2.getImageStack();
			encadrerObjetsDuStack(stk2);
			int meilleureImage = calculerMeilleureImage();
			macro8bits();
			macroBinarise();
			macroRGB();
			ImagePlus imp3 = IJ.getImage();
			ImageStack stk3 = imp3.getImageStack();
			calculerSurfaceObjetsInterets(stk3);
			calculerPerimetreObjetsInterets(stk3);
			tableauDeResultats();
			IJ.showMessage("L'image "+Integer.toString(meilleureImage)+" contient le plus grand nombre d'objets ("+Integer.toString(nbElementsPertinents.get(meilleureImage-1))+").");
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
