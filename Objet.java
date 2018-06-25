package DetectionSquelettes;

/**
 * Classe representant un objet avec son {@link Squelette}. 
 * @author e1502316
 *
 */

public class Objet {
	/**
	 * {@link Squelette} de l'objet. 
	 */
	public Squelette skull;
	/**
	 * Surface en pixels de l'objet. La surface est calculee avec une image binarisee, des imprecisions sont presentes. 
	 */
	public int surface;
	/**
	 * Perimetre en pixels de l'objet. La surface est calculee avec une image binarisee, des imprecisions sont presentes. 
	 */
	public int perimetre;
	/**
	 * Roundness (facteur d'ellipticite) de l'objet. Proche de 1 on a un objet circulaire, proche de 0 non. 
	 */
	public double roundness;
	/**
	 * Indice dans l'image de la position de l'objet. Il s'agit d'un indice moyen qui represente le centre de l'objet. 
	 */
	public int indiceObjet;
	/**
	 * {@link Image} dans la quelle se situe l'objet. 
	 */
	public Image image;
	
	/**
	 * Constructeur prenant en parametre un {@link Squelette} et une {@link Image}. 
	 * @param skull {@link Squelette}
	 * @param image {@link Image}
	 */
	public Objet(Squelette skull, Image image)
	{
		this.skull = skull;
		this.image = image;
		moyenneIndice();
		this.surface = surfaceObjet(this.indiceObjet);
		image.blanchirBinaire();
		this.perimetre = perimetreObjet(this.indiceObjet);
		image.blanchirBinaire();
		this.roundness = roundness();
	}
	/**
	 * Calcule l'indice de la position moyenne de l'objet. 
	 */
	private void moyenneIndice()
	{
		int x = 0;
		int y = 0;
		int i;
		for (i = 0; i < skull.points.size(); i++)
		{
			x += image.colonne(skull.points.get(i));
			y += image.ligne(skull.points.get(i));
		}
		x = (int) x/i;
		y = (int) y/i;
		this.indiceObjet = image.indice(y, x);
	}
	/**
	 * Calcule et renvoie la surface de l'objet place a l'indice en parametre. 
	 * @param indice Entier
	 * @return Entier
	 */
	/*
	 * Fonctionne typiquement recursive qui modifie l'image, a utiliser avec precaution. 
	 * Notamment, remettre a blanc les pixels apres utilisation avec la fonction correspondante dans la classe Image.
	 */
	private int surfaceObjet(int indice)
	{
		if (image.estValide(indice) && image.pixelsBinairesCopie[indice] == 0xffffff)
		{
			image.pixelsBinairesCopie[indice] = 0xfffffe; // /!\ ici on modifie les pixels
			return 	(surfaceObjet(indice+1)+surfaceObjet(indice-1)+
					surfaceObjet(indice-image.nbColonnes)+surfaceObjet(indice+image.nbColonnes)+
					surfaceObjet(indice-image.nbColonnes+1)+surfaceObjet(indice+image.nbColonnes+1)+
					surfaceObjet(indice-image.nbColonnes-1)+surfaceObjet(indice+image.nbColonnes-1))+1;
		}
		else
		{
			return 0;
		}
	}
	/**
	 * Calcule et renvoie le perimetre de l'objet place a l'indice en parametre.
	 * @param indice Entier
	 * @return Entier
	 */
	/*
	 * Idem que pour surfaceObjet (int), a utiliser avec precaution. 
	 */
	private int perimetreObjet(int indice)
	{
		if (image.estValide(indice) && image.pixelsBinairesCopie[indice] == 0xffffff)
		{
			image.pixelsBinairesCopie[indice] = 0xfffffe; // /!\ ici on modifie les pixels 
			if (Utilitaire.contientVoisin(indice, -0x1000000, image.pixelsBinairesCopie, image.nbColonnes))
			{
				return 	(perimetreObjet(indice+1)+perimetreObjet(indice-1)+
						perimetreObjet(indice-image.nbColonnes)+perimetreObjet(indice+image.nbColonnes)+
						perimetreObjet(indice-image.nbColonnes+1)+perimetreObjet(indice+image.nbColonnes+1)+
						perimetreObjet(indice-image.nbColonnes-1)+perimetreObjet(indice+image.nbColonnes-1))+1;
			}
			else
			{
				return 	(perimetreObjet(indice+1)+perimetreObjet(indice-1)+
						perimetreObjet(indice-image.nbColonnes)+perimetreObjet(indice+image.nbColonnes)+
						perimetreObjet(indice-image.nbColonnes+1)+perimetreObjet(indice+image.nbColonnes+1)+
						perimetreObjet(indice-image.nbColonnes-1)+perimetreObjet(indice+image.nbColonnes-1));
			}
		}
		else
		{
			return 0;
		}
	}
	/**
	 * Calcule et renvoie la roundness de l'objet. 
	 * @return Double
	 */
	private double roundness()
	{
		return (double) ((4*Math.PI*surface)/(perimetre*perimetre));
	}
}
