package DetectionSquelettes;

import java.util.ArrayList;

/**
 * Classe representant une image avec les objets ({@link Objet}) qu'elle contient. 
 * @author e1502316
 *
 */

public class Image {
	/**
	 * Tableau des pixels de l'image courante avec ses objets sous forme de squelette. 
	 */
	public int[] pixelsSquelettesCopie;
	/**
	 * Tableau des pixels de l'image courant sous forme d'image binarisee. 
	 */
	public int[] pixelsBinairesCopie;
	
	/**
	 * Tableau de tous les objets de l'image courante. 
	 */
	public ArrayList <Objet> objets;
	/**
	 * Tableau des objets pertinents dans l'image courante. Cette variable doit etre initialisee par la fonction {@link Detection#determinerObjetsPertinents()}.
	 */
	public ArrayList <Objet> objetsPertinents;
	/**
	 * Tableau des differents alignements d'objets pertinents dans l'image. 
	 */
	public ArrayList <ArrayList <Objet>> alignements;
	
	/**
	 * Taille en pixels de l'image courante. 
	 */
	public int taille;
	/**
	 * Nombre de colonnes dans l'image courante. 
	 */
	public int nbColonnes;
	/**
	 * Nombre de lignes dans l'image courante. 
	 */
	public int nbLignes;
	
	/**
	 * Calcule et renvoie la colonne correspondante d'un indice place en parametre. 
	 * @param indice Entier
	 * @return Entier
	 */
	public int colonne(int indice)
	{
		return indice%nbColonnes;
	}
	/**
	 * Calcule et renvoie la ligne correspondante d'un indice place en parametre. 
	 * @param indice Entier
	 * @return Entier
	 */
	public int ligne(int indice)
	{
		return indice/nbColonnes;
	}
	/**
	 * Calcule et renvoie l'indice correspond a une ligne et une colonne donnees. 
	 * @param ligne Entier
	 * @param colonne Entier
	 * @return Entier
	 */
	public int indice(int ligne, int colonne)
	{
		return nbColonnes*ligne + colonne;
	}
	/**
	 * Determine et renvoie si un indice est valide ou non (tests de debordements). 
	 * @param indice Entier
	 * @return Booleen
	 */
	public boolean estValide(int indice)
	{
		if (indice < 0 || indice >= taille)
		{
			return false;
		}
		return true;
	}
	/**
	 * Constructeur par defaut qui prend en parametre le nombre de colonnes et de lignes d'une image. Initialise les differents tableaux a des tableaux vides. 
	 * @param nbColonnes Entier
	 * @param nbLignes Entier
	 */
	public Image(int nbColonnes, int nbLignes)
	{
		this.nbColonnes = nbColonnes;
		this.nbLignes = nbLignes;
		this.taille = nbColonnes * nbLignes;
		objets = new ArrayList <Objet>();
		objetsPertinents = new ArrayList <Objet>();
		alignements = new ArrayList <ArrayList <Objet>>();
	}
	/**
	 * Initialise l'image en y determinant les differents objets. Ne determine pas les objets pertinents. 
	 */
	public void initialiserImage()
	{
		blanchirSquelette();
		blanchirBinaire();
		determinerObjets();
		remettreBlanc();
	}
	/**
	 * Permet de definir le tableau de {@link Image#pixelsBinairesCopie} avec le tableau place en parametre. 
	 * @param pixels Tableau d'entiers
	 */
	public void setPixelsBinaires(int[] pixels)
	{
		this.pixelsBinairesCopie = pixels.clone();
	}
	/**
	 * Permet de definir le tableau de {@link Image#pixelsSquelettesCopie} avec le tableau place en parametre. 
	 * @param pixels Tableau d'entiers
	 */
	public void setPixelsSquelettes(int[] pixels)
	{
		this.pixelsSquelettesCopie = pixels.clone();
	}
	/**
	 * Determine les objets d'une image et les stocke dans {@link Image#objets}. 
	 */
	public void determinerObjets()
	{
		for (int i = nbColonnes+2; i < taille-nbColonnes-2; i++)
		{
			if (pixelsSquelettesCopie[i] == 0xffffff) // Un pixel est retenu s'il est blanc. On a alors le debut d'un potentiel squelette. 
			{
				Squelette tmp = new Squelette(this, i); // Le squelette correspondant au pixel retenu est cree, c'est a dire qu'il contient maintenant tous les points blancs proches de lui...
				if (tmp.points.size() > 10) // ...mais on ne l'ajoute a la liste des squelettes que s'il a au moins un minimum de points (en dessous on considere ca comme une tache). 
				{
					objets.add(new Objet(tmp, this));
				}
			}
		}
	}
	/**
	 * Encadre le point situe a une position donnee dans le tableau place en parametre. La taille d'encadrement et la couleur sont parametrables. 
	 * @param position Entier
	 * @param tailleEncadrement Entier
	 * @param couleur Entier en hexadecimal
	 * @param pixels Tableau d'entiers 
	 */
	private void encadrer(int position, int tailleEncadrement, int couleur, int[] pixels)
	{
		int colonneX = colonne(position);
		int ligneX = ligne(position);
		// Tests de debordements 
		if (colonneX-tailleEncadrement > 0 && ligneX-tailleEncadrement > 0 && colonneX+tailleEncadrement < nbColonnes && ligneX+tailleEncadrement < nbLignes)
		{
			for (int col = colonneX-tailleEncadrement; col <= colonneX+tailleEncadrement; col++)
			{
				pixels[indice(ligneX-tailleEncadrement, col)] = couleur;
				pixels[indice(ligneX+tailleEncadrement, col)] = couleur;
			}
			for (int ligne = ligneX-tailleEncadrement; ligne <= ligneX+tailleEncadrement; ligne++)
			{
				pixels[indice(ligne, colonneX-tailleEncadrement)] = couleur;
				pixels[indice(ligne, colonneX+tailleEncadrement)] = couleur;
			}
		}
	}
	/**
	 * Encadre tous les objets pertinents de l'image courante dans le tableau place en parametre. La taille d'encadrement et la couleur sont parametrables. 
	 * @param tailleEncadrement Entier
	 * @param couleur Entier en hexadecimal
	 * @param pixels Tableau d'entiers
	 */
	public void encadrerObjetsPertinents(int tailleEncadrement, int couleur, int[] pixels)
	{
		for (int i = 0; i < objetsPertinents.size(); i++)
		{
			encadrer(objetsPertinents.get(i).indiceObjet, tailleEncadrement, couleur, pixels);
		}
	}
	/**
	 * Pour tout les pixels non strictament noirs du tableau passe en parametre, les blanchit. 
	 * @param pixels Tableau d'entiers
	 */
	private void blanchir(int[] pixels)
	{
		for (int i = 0; i < taille; i++)
		{
			if (!(pixels[i] == -0x1000000)) // la couleur noire precise utilise dans imageJ
			{
				pixels[i] = 0xffffff; // blanc
			}
		}
	}
	/**
	 * Blanchit le tableau {@link Image#pixelsBinairesCopie}.
	 */
	public void blanchirBinaire()
	{
		blanchir(this.pixelsBinairesCopie);
	}
	/**
	 * Blanchit le tableau {@link Image#pixelsSquelettesCopie}.
	 */
	public void blanchirSquelette()
	{
		blanchir(this.pixelsSquelettesCopie);
	}
	/**
	 * Remet a blanc les pixels qui ont potentiellement etes modifies pour de l'analyse. 
	 */
	private void remettreBlanc()
	{
		for (int i = 0; i < taille; i++)
		{
			if (pixelsSquelettesCopie[i] == 0xfffffe)
			{
				pixelsSquelettesCopie[i] = 0xffffff;
			}
		}
	}
	/**
	 * Determine et renvoie si deux objets sont dans une meme zone a une approximation pres. 
	 * @param indiceObjetPertinent1 Entier - Indice de l'objet pertinent 1 situe dans {@link Image#objetsPertinents}
	 * @param indiceObjet2 Entier - Indice de l'objet 2 {@link Image#objets}
	 * @param approximation Entier
	 * @return Booleen
	 */
	public boolean sontDansMemeZone(int indiceObjetPertinent1, int indiceObjet2, int approximation)
	{
		int indice1 = objetsPertinents.get(indiceObjetPertinent1).indiceObjet;
		int indice2 = objets.get(indiceObjet2).indiceObjet;
		int x1 = colonne(indice1);
		int y1 = ligne(indice1);
		int x2 = colonne(indice2);
		int y2 = ligne(indice2);
		if (Math.abs(x1-x2) <= approximation && Math.abs(y1-y2) <= approximation)
		{
			return true;
		}
		return false;
	}
	/**
	 * Determine et renvoie si trois objets sont alignes a une approximation pres. 
	 * @param indiceObjet1 Entier - Indice de l'objet 1
	 * @param indiceObjet2 Entier - Indice de l'objet 2
	 * @param indiceObjet3 Entier - Indice de l'objet 3
	 * @param approximation Entier
	 * @return Booleen 
	 */
	public boolean sontAlignes(int indiceObjet1, int indiceObjet2, int indiceObjet3, int approximation)
	{
		int indice1 = objetsPertinents.get(indiceObjet1).indiceObjet;
		int indice2 = objetsPertinents.get(indiceObjet2).indiceObjet;
		int indice3 = objetsPertinents.get(indiceObjet3).indiceObjet;
		int x1 = colonne(indice1);
		int y1 = ligne(indice1);
		int x2 = colonne(indice2);
		int y2 = ligne(indice2);
		int x3 = colonne(indice3);
		int y3 = ligne(indice3);
		return Utilitaire.sontAlignes(x1, y1, x2, y2, x3, y3, approximation);
	}
	/**
	 * Determine les alignements a une approximation pres et les stocke dans {@link Image#alignements}.
	 * @param approximation Entier
	 */
	public void determinerAlignements(int approximation)
	{
		int taille = objetsPertinents.size();
		ArrayList <Boolean> marque = new ArrayList <Boolean>();
		for (int i = 0; i < taille; i++)
		{
			marque.add(false);
		}
		for (int s1 = 0; s1 < taille; s1++)
		{
			if (marque.get(s1) == false)
			{
				for (int s2 = 0; s2 < taille; s2++)
				{
					if (marque.get(s2) == false && s2 != s1)
					{
						for (int s3 = 0; s3 < taille; s3++)
						{
							if (marque.get(s3) == false && s3 != s1 && s3 != s2)
							{
								if (sontAlignes(s1, s2, s3, approximation))
								{
									marque.set(s1, true);
									marque.set(s2, true);
									marque.set(s3, true);
									alignements.add(new ArrayList <Objet>());
									int dernierAlignement = alignements.size()-1;
									alignements.get(dernierAlignement).add(objetsPertinents.get(s1));
									alignements.get(dernierAlignement).add(objetsPertinents.get(s2));
									alignements.get(dernierAlignement).add(objetsPertinents.get(s3));
									for (int s4 = 0; s4 < taille; s4++)
									{
										if (marque.get(s4) == false)
										{
											int dernier = alignements.get(dernierAlignement).size()-1;
											int avantDernier = dernier-1;
											if (sontAlignes(dernier, avantDernier, s4, approximation))
											{
												marque.set(s4, true);
												alignements.get(dernierAlignement).add(objetsPertinents.get(s4));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	/**
	 * Determine et renvoie la taille du plus grand alignement dans l'image. 
	 * @return Entier
	 */
	public int plusGrandAlignement()
	{
		int max = 0;
		for (int i = 0; i < alignements.size(); i++)
		{
			if (alignements.get(i).size() > max)
			{
				max = alignements.get(i).size();
			}
		}
		return max;
	}
	/**
	 * Determine et renvoie la distance entre deux objets. 
	 * @param objet1 Objet
	 * @param objet2 Objet
	 * @return Entier
	 */
	public int distance(Objet objet1, Objet objet2)
	{
		int indice1 = objet1.indiceObjet;
		int indice2 = objet2.indiceObjet;
		int x1 = colonne(indice1);
		int y1 = ligne(indice1);
		int x2 = colonne(indice2);
		int y2 = ligne(indice2);
		return (int) Utilitaire.distance(x1, y1, x2, y2);
	}
	/**
	 * Ajoute l'objet d'indice place en parametre dans {@link Image#objetsPertinents}.
	 * @param indiceObjet
	 */
	public void ajouterObjetPertinent(int indiceObjet)
	{
		objetsPertinents.add(objets.get(indiceObjet));
	}

}
