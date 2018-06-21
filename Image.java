package DetectionSquelettes;

import java.util.ArrayList;

public class Image {
	public int[] pixelsCopie;
	public int[] pixelsSquelettesCopie;
	public int[] pixelsBinairesCopie;
	
	public ArrayList <Objet> objets;
	public ArrayList <Objet> objetsPertinents;
	public ArrayList <ArrayList <Objet>> alignements;
	
	public int taille;
	public int nbColonnes;
	public int nbLignes;
	
	int colonne(int indice)
	{
		return indice%nbColonnes;
	}
	int ligne(int indice)
	{
		return indice/nbColonnes;
	}
	int indice(int ligne, int colonne)
	{
		return nbColonnes*ligne + colonne;
	}
	boolean estValide(int indice)
	{
		if (indice < 0 || indice >= taille)
		{
			return false;
		}
		return true;
	}
	public Image(int[] pixels, int nbColonnes, int nbLignes)
	{
		this.pixelsCopie = pixels.clone();
		this.nbColonnes = nbColonnes;
		this.nbLignes = nbLignes;
		this.taille = nbColonnes * nbLignes;
		objets = new ArrayList <Objet>();
		objetsPertinents = new ArrayList <Objet>();
		alignements = new ArrayList <ArrayList <Objet>>();
	}
	
	void initialiserImage()
	{
		blanchirSquelette();
		blanchirBinaire();
		determinerObjets();
		determinerAlignements(500);
		remettreBlanc();
	}
	
	public void setPixelsBinaires(int[] pixels)
	{
		this.pixelsBinairesCopie = pixels.clone();
	}
	
	public void setPixelsSquelettes(int[] pixels)
	{
		this.pixelsSquelettesCopie = pixels.clone();
	}
	
	public void determinerObjets()
	{
		for (int i = nbColonnes+2; i < taille-nbColonnes-2; i++)
		{
			// Un pixel est retenu s'il est blanc. On a alors le debut d'un potentiel squelette. 
			if (pixelsSquelettesCopie[i] == 0xffffff)
			{
				// Le squelette correspondant au pixel retenu est cree, c'est a dire qu'il contient maintenant tous les points blancs proches de lui...
				squelette tmp = new squelette(this, i);
				// mais on ne l'ajoute a la liste des squelettes que s'il a au moins tailleMinSquelette de points. 
				if (tmp.points.size() > 10)
				{
					objets.add(new Objet(tmp, this));
				}
			}
		}
	}
	
	public void encadrer(int position, int tailleEncadrement, int couleur)
	{
		int colonneX = colonne(position);
		int ligneX = ligne(position);
		// Tests de debordements 
		if (colonneX-tailleEncadrement > 0 && ligneX-tailleEncadrement > 0 && colonneX+tailleEncadrement < nbColonnes && ligneX+tailleEncadrement < nbLignes)
		{
			for (int col = colonneX-tailleEncadrement; col <= colonneX+tailleEncadrement; col++)
			{
				pixelsCopie[indice(ligneX-tailleEncadrement, col)] = couleur;
				pixelsCopie[indice(ligneX+tailleEncadrement, col)] = couleur;
			}
			for (int ligne = ligneX-tailleEncadrement; ligne <= ligneX+tailleEncadrement; ligne++)
			{
				pixelsCopie[indice(ligne, colonneX-tailleEncadrement)] = couleur;
				pixelsCopie[indice(ligne, colonneX+tailleEncadrement)] = couleur;
			}
		}
	}
	
	public void encadrerObjetsPertinents(int tailleEncadrement, int couleur)
	{
		for (int i = 0; i < objetsPertinents.size(); i++)
		{
			encadrer(objetsPertinents.get(i).indiceObjet, tailleEncadrement, couleur);
		}
	}
	
	private void blanchir(int[] pixels)
	{
		for (int i = 0; i < taille; i++)
		{
			if (!(pixels[i] == -0x1000000))
			{
				pixels[i] = 0xffffff;
			}
		}
	}
	
	public void blanchirCopie()
	{
		blanchir(this.pixelsCopie);
	}
	
	public void blanchirBinaire()
	{
		blanchir(this.pixelsBinairesCopie);
	}
	
	public void blanchirSquelette()
	{
		blanchir(this.pixelsSquelettesCopie);
	}
	
	void remettreBlanc()
	{
		for (int i = 0; i < taille; i++)
		{
			if (pixelsSquelettesCopie[i] == 0xfffffe)
			{
				pixelsSquelettesCopie[i] = 0xffffff;
			}
		}
	}
	
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
	
	public boolean sontAlignes(int indiceObjet1, int indiceObjet2, int indiceObjet3, int approximation)
	{
		int indice1 = objets.get(indiceObjet1).indiceObjet;
		int indice2 = objets.get(indiceObjet2).indiceObjet;
		int indice3 = objets.get(indiceObjet3).indiceObjet;
		int x1 = colonne(indice1);
		int y1 = ligne(indice1);
		int x2 = colonne(indice2);
		int y2 = ligne(indice2);
		int x3 = colonne(indice3);
		int y3 = ligne(indice3);
		return Utilitaire.sontAlignes(x1, y1, x2, y2, x3, y3, approximation);
	}
	
	public void determinerAlignements(int approximation)
	{
		ArrayList <Boolean> marque = new ArrayList <Boolean>();
		for (int j = 0; j < objetsPertinents.size(); j++)
		{
			marque.add(false);
		}
		int taille = objetsPertinents.size();
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
									int dernierIndice = alignements.size();
									alignements.get(dernierIndice-1).add(objetsPertinents.get(s1));
									alignements.get(dernierIndice-1).add(objetsPertinents.get(s2));
									alignements.get(dernierIndice-1).add(objetsPertinents.get(s3));
									for (int s4 = 0; s4 < taille; s4++)
									{
										if (marque.get(s4) == false)
										{
											if (sontAlignes(s1, s2, s3, approximation))
											{
												marque.set(s4, true);
												alignements.get(dernierIndice-1).add(objetsPertinents.get(s4));
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
	
	public void ajouterObjetPertinent(int indiceObjet)
	{
		objetsPertinents.add(objets.get(indiceObjet));
	}

}
