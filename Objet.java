package DetectionSquelettes;

public class Objet {
	public Squelette skull;
	public int surface;
	public int perimetre;
	public double roundness;
	public int indiceObjet;
	public Image image;
	
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
	
	public void moyenneIndice()
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
	
	public int surfaceObjet(int indice)
	{
		if (image.estValide(indice) && image.pixelsBinairesCopie[indice] == 0xffffff)
		{
			image.pixelsBinairesCopie[indice] = 0xfffffe;
			return (surfaceObjet(indice+1)+surfaceObjet(indice-1)+
					surfaceObjet(indice-image.nbColonnes)+surfaceObjet(indice+image.nbColonnes)+
					surfaceObjet(indice-image.nbColonnes+1)+surfaceObjet(indice+image.nbColonnes+1)+
					surfaceObjet(indice-image.nbColonnes-1)+surfaceObjet(indice+image.nbColonnes-1))+1;
		}
		else
		{
			return 0;
		}
	}
	
	public int perimetreObjet(int indice)
	{
		if (image.estValide(indice) && image.pixelsBinairesCopie[indice] == 0xffffff)
		{
			image.pixelsBinairesCopie[indice] = 0xfffffe;
			if (Utilitaire.contientVoisin(indice, -0x1000000, image.pixelsBinairesCopie, image.nbColonnes))
			{
				return (perimetreObjet(indice+1)+perimetreObjet(indice-1)+
						perimetreObjet(indice-image.nbColonnes)+perimetreObjet(indice+image.nbColonnes)+
						perimetreObjet(indice-image.nbColonnes+1)+perimetreObjet(indice+image.nbColonnes+1)+
						perimetreObjet(indice-image.nbColonnes-1)+perimetreObjet(indice+image.nbColonnes-1))+1;
			}
			else
			{
				return (perimetreObjet(indice+1)+perimetreObjet(indice-1)+
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
	
	public double roundness()
	{
		return (double) ((4*Math.PI*surface)/(perimetre*perimetre));
	}
}
