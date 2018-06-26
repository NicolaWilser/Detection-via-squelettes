package DetectionSquelettes;

import java.util.ArrayList;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.gui.Line;
import ij.measure.ResultsTable;

/**
 * Classe de detection d'objets ({@link Objet}) dans une {@link Image}. 
 * Permet d'analyser un stack d'images et de determiner et localiser les objets. 
 * Differentes analyses sont faites et peuvent etre affichees dans un tableau de resultats. 
 * @author e1502316
 *
 */

public class Detection implements PlugIn {
	
	/**
	 * Tableau contenant toutes les images ({@link Image}) actuellement analysees. Peut être une seule image. 
	 */
	private ArrayList<Image> images;
	/**
	 * Stack d'images du fichier actuellement ouvert et analysé. 
	 */
	private ImageStack stack;
	/**
	 * Parametres ({@link Parametre}) de detection des objets pertinents. 
	 */
	private Parametre param;
	
	/**
	 * Determine si un {@link Objet} de {@link Image#objets} d'indice indiceObjet de l'{@link Image} d'indice numeroImage est pertinent. 
	 * @param numeroImage Entier
	 * @param indiceObjet Entier
	 * @return Booleen 
	 */
	public boolean estPertinent(int numeroImage, int indiceObjet)
	{
		boolean roundnessValide = 
				(images.get(numeroImage).objets.get(indiceObjet).roundness >= param.roundnessMin 
				&& images.get(numeroImage).objets.get(indiceObjet).roundness <= param.roundnessMax);
		boolean intersectionValide = 
				(images.get(numeroImage).objets.get(indiceObjet).skull.intersections.size() >= param.minIntersection 
				&& images.get(numeroImage).objets.get(indiceObjet).skull.intersections.size() <= param.maxIntersection);
		
		boolean valide = (roundnessValide && intersectionValide);
		
		return valide;
	}
	/**
	 * Determine pour chaque image de {@link Detection#images} les objets pertinents dans l'ArrayList {@link Image#objetsPertinents}. 
	 */
	public void determinerObjetsPertinents()
	{
		for (int j = 0; j < images.size(); j++)
		{
			for (int i = 0; i < images.get(j).objets.size(); i++)
			{
				if (estPertinent(j, i))
				{
					int k = 0;
					while (k < images.get(j).objetsPertinents.size() && !images.get(j).sontDansMemeZone(k, i, 50))
					{
						k++;
					}
					if (k == images.get(j).objetsPertinents.size())
					{
						images.get(j).ajouterObjetPertinent(i);
					}
				}
			}
		}
	}
	/**
	 * Encadre les objets pertinents des images du stack. 
	 */
	public void encadrerObjetsPertinents()
	{
		for (int i = 0; i < images.size(); i++)
		{
			int[] pixels = (int []) stack.getProcessor(i+1).getPixels();
			images.get(i).encadrerObjetsPertinents(30, 0xff0000, pixels);
		}
	}
	/**
	 * Marque les alignements des images du stack en tracant une droite pour chacun d'eux.
	 */
	public void marquerAlignements()
	{
		for (int i = 1; i <= stack.getSize(); i++)
		{
			ImageProcessor imp = stack.getProcessor(i);
			for (int al = 0; al < images.get(i-1).alignements.size(); al++)
			{
				int distanceMax = 0;
				int sMax1, sMax2;
				sMax1 = 0;
				sMax2 = 0;
				for (int s1 = 0; s1 < images.get(i-1).alignements.get(al).size(); s1++)
				{
					for (int s2 = 0; s2 < images.get(i-1).alignements.get(al).size(); s2++)
					{
						if (images.get(i-1).distance(images.get(i-1).alignements.get(al).get(s1), images.get(i-1).alignements.get(al).get(s2)) >= distanceMax)
						{
							sMax1 = s1;
							sMax2 = s2;
							distanceMax = images.get(i-1).distance(images.get(i-1).alignements.get(al).get(s1), images.get(i-1).alignements.get(al).get(s2));
						}
					}
				}
				int x1 = images.get(i-1).colonne(images.get(i-1).alignements.get(al).get(sMax1).indiceObjet);
				int x2 = images.get(i-1).colonne(images.get(i-1).alignements.get(al).get(sMax2).indiceObjet);
				int y1 = images.get(i-1).ligne(images.get(i-1).alignements.get(al).get(sMax1).indiceObjet);
				int y2 = images.get(i-1).ligne(images.get(i-1).alignements.get(al).get(sMax2).indiceObjet);
				Line l = new Line(x1, y1, x2, y2);
				l.drawPixels(imp);
			}
		}
	}
	/**
	 * Cree et affiche un tableau de resultats contenant les donnees de l'analyse effectuee sur le stack d'images. 
	 */
	public void tableauDeResultats()
	{
		ResultsTable rt = new ResultsTable(images.size()); 
		int surface, surfaceTotale, perimetre, perimetreTotal;
		int nbBranches, tailleMoyenneBranches, nbBranchesTotal, tailleMoyenneBranchesTotale, taille;
		double intensiteTotale;
		for (int numeroImage = 0; numeroImage < images.size(); numeroImage++)
		{
			rt.setValue("Image", numeroImage, numeroImage+1);
			taille = images.get(numeroImage).objetsPertinents.size();
			rt.setValue("Nombre d'objets détectés", numeroImage, taille);
			surfaceTotale = 0;
			perimetreTotal = 0;
			nbBranchesTotal = 0;
			tailleMoyenneBranchesTotale = 0; 
			intensiteTotale = 0; 
			int i;
			for (i = 0; i < taille; i++)
			{
				surface = images.get(numeroImage).objetsPertinents.get(i).surface;
				perimetre = images.get(numeroImage).objetsPertinents.get(i).perimetre;
				nbBranches = images.get(numeroImage).objetsPertinents.get(i).skull.nombreBranches();
				tailleMoyenneBranches = images.get(numeroImage).objetsPertinents.get(i).skull.moyenneLongueurBranche();
				surfaceTotale += surface;
				perimetreTotal += perimetre;
				nbBranchesTotal += nbBranches;
				tailleMoyenneBranchesTotale += tailleMoyenneBranches;
				intensiteTotale += images.get(numeroImage).objetsPertinents.get(i).intensite;
			}
			if (i != 0)
			{
				int surfaceMoyenne = surfaceTotale/taille;
				int perimetreMoyen = perimetreTotal/taille;
				double intensiteMoyenne = intensiteTotale/taille;
				rt.setValue("Surface moyenne", numeroImage, surfaceMoyenne);
				rt.setValue("Perimetre moyen", numeroImage, perimetreMoyen);
				rt.setValue("Roundness moyenne", numeroImage, Utilitaire.roundness(surfaceMoyenne, perimetreMoyen));
				rt.setValue("Intensité moyenne", numeroImage, intensiteMoyenne);
				rt.setValue("Nb de branches moyen", numeroImage, (double) nbBranchesTotal/((double) taille));
				rt.setValue("Taille de branche moyenne", numeroImage, tailleMoyenneBranchesTotale/taille);
				rt.setValue("Nombre d'alignements", numeroImage, images.get(numeroImage).alignements.size());
				rt.setValue("Nb objets alignés max", numeroImage, images.get(numeroImage).plusGrandAlignement());
			}
			else
			{
				rt.setValue("Surface moyenne", numeroImage, 0);
				rt.setValue("Perimetre moyen", numeroImage, 0);
				rt.setValue("Roundness moyenne", numeroImage, 0);
				rt.setValue("Intensité moyenne", numeroImage, 0);
				rt.setValue("Nb de branches moyen", numeroImage, 0);
				rt.setValue("Taille de branche moyenne", numeroImage, 0);
				rt.setValue("Nombre d'alignements", numeroImage, 0);
				rt.setValue("Nb objets alignés max", numeroImage, 0);
			}
		}
		rt.show("Résultats de l'analyse");
	}
	
	/**
	 * Initialise les ArrayList. 
	 */
	private void initialiserVariables()
	{
		images = new ArrayList<Image>();
	}
	/**
	 * Initialise les differentes images du stack en determinant leurs objets. 
	 */
	public void initialiserImage()
	{
		for (int i = 0; i < images.size(); i++)
		{
			images.get(i).initialiserImage();
		}
	}
	/**
	 * Determine les alignements pour chaque images du stack. 
	 */
	public void determinerAlignements()
	{
		for (int i = 0; i < images.size(); i++)
		{
			images.get(i).determinerAlignements(param.facteurAlignement);
		}
	}
	/**
	 * Charge une image (ou plusieurs si c'est un stack) et prepare les traitements pour l'analyse. 
	 */
	public void chargerImage()
	{
		Utilitaire.macroRGB();
		ImagePlus imp = IJ.getImage();
		ImageProcessor im  = imp.getProcessor();
		int nbColonnes = im.getWidth();
		int nbLignes = im.getHeight();
		ImageStack stk = imp.getImageStack();
		for (int i = 1; i <= stk.getSize(); i++)
		{
			Image tmp = new Image(nbColonnes, nbLignes);
			tmp.setPixels((int []) stk.getProcessor(i).getPixels());
			images.add(tmp); 
		}
		Utilitaire.macro8bits();
		Utilitaire.macroBinarise(param.minPixels, param.maxPixels);
		Utilitaire.macroRGB();
		ImagePlus impBinaire = IJ.getImage();
		ImageStack stkB = impBinaire.getImageStack();
		for (int i = 1; i <= stkB.getSize(); i++)
		{
			images.get(i-1).setPixelsBinaires((int[]) stkB.getProcessor(i).getPixels());
		}
		Utilitaire.macroFermerImage(2);
		Utilitaire.macroBinarise(param.minPixels, param.maxPixels);
		Utilitaire.macroSquelette();
		Utilitaire.macroRGB();
		ImagePlus impSquelette = IJ.getImage();
		ImageStack stkS = impSquelette.getImageStack();
		for (int i = 1; i <= stkS.getSize(); i++)
		{
			images.get(i-1).setPixelsSquelettes((int[]) stkS.getProcessor(i).getPixels());
		}
		Utilitaire.macroFermerImage(2);
		Utilitaire.macroRGB();
		ImagePlus im2 = IJ.getImage();
		stack = im2.getStack();
	}
	
	public void run(String arg) {
		param = new Parametre();
		if (param.lireParam())
		{
			initialiserVariables();
			chargerImage();
			initialiserImage();
			determinerObjetsPertinents();
			encadrerObjetsPertinents();
			determinerAlignements();
			marquerAlignements();
			tableauDeResultats();
		}
	}
}
