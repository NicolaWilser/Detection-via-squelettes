package DetectionSquelettes;

import java.util.ArrayList;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.gui.Line;
import ij.measure.ResultsTable;


public class Detection_phyto_squelette implements PlugIn {
	
	private ArrayList<Image> image;
	
	private ImageStack stack;

	private int minIntersection; 
	private int maxIntersection;  
	private int minPixels;
	private int maxPixels;
	private int facteurAlignement;
	

	public void macroSquelette()
	{
		IJ.runMacro("run(\"Skeletonize\", \"stack\")");
	}
	
	public void macroDuplicate()
	{
		IJ.runMacro("run(\"Duplicate...\", \"duplicate\");");
	}

	public void macroRGB()
	{
		IJ.runMacro("run(\"RGB Color\");");
	}

	public void macro8bits()
	{
		IJ.runMacro("run(\"8-bit\");");
	}

	public void macroBinarise()
	{
		IJ.runMacro("run(\"Duplicate...\", \"duplicate\");run(\"Auto Threshold\", \"method=Otsu white stack\");run(\"Open\", \"stack\");run(\"Analyze Particles...\", \"size="+Integer.toString(minPixels)+"-"+Integer.toString(maxPixels)+" show=Masks clear stack\");setOption(\"BlackBackground\", true);run(\"Make Binary\", \"method=Default background=Light calculate black\");");
	}

	public void macroFermerImage(int nbFois)
	{
		String str = "";
		for (int i = 0; i < nbFois; i++)
		{
			str+= "run(\"Close\");";
		}
		IJ.runMacro(str);
	}
	
	public void determinerObjetsPertinents()
	{
		for (int j = 0; j < image.size(); j++)
		{
			for (int i = 0; i < image.get(j).objets.size(); i++)
			{
				if (estPertinent(j, i))
				{
					int k = 0;
					while (k < image.get(j).objetsPertinents.size() && !image.get(j).sontDansMemeZone(k, i, 50))
					{
						k++;
					}
					if (k == image.get(j).objetsPertinents.size())
					{
						image.get(j).ajouterObjetPertinent(i);
					}
				}
			}
		}
	}
	
	public void marquerAlignements()
	{
		for (int i = 1; i <= stack.getSize(); i++)
		{
			ImageProcessor imp = stack.getProcessor(i);
			for (int al = 0; al < image.get(i-1).alignements.size(); al++)
			{
				int distanceMax = 0;
				int sMax1, sMax2;
				sMax1 = 0;
				sMax2 = 0;
				for (int s1 = 0; s1 < image.get(i-1).alignements.get(al).size(); s1++)
				{
					for (int s2 = 0; s2 < image.get(i-1).alignements.get(al).size(); s2++)
					{
						if (image.get(i-1).distance(image.get(i-1).alignements.get(al).get(s1), image.get(i-1).alignements.get(al).get(s2)) >= distanceMax)
						{
							sMax1 = s1;
							sMax2 = s2;
							distanceMax = image.get(i-1).distance(image.get(i-1).alignements.get(al).get(s1), image.get(i-1).alignements.get(al).get(s2));
						}
					}
				}
				int x1 = image.get(i-1).colonne(image.get(i-1).alignements.get(al).get(sMax1).indiceObjet);
				int x2 = image.get(i-1).colonne(image.get(i-1).alignements.get(al).get(sMax2).indiceObjet);
				int y1 = image.get(i-1).ligne(image.get(i-1).alignements.get(al).get(sMax1).indiceObjet);
				int y2 = image.get(i-1).ligne(image.get(i-1).alignements.get(al).get(sMax2).indiceObjet);
				Line l = new Line(x1, y1, x2, y2);
				l.drawPixels(imp);
			}
		}
	}
	
	public boolean estPertinent(int numeroImage, int indiceObjet)
	{
		boolean intersectionValide = (image.get(numeroImage).objets.get(indiceObjet).skull.intersections.size() >= minIntersection 
				&& image.get(numeroImage).objets.get(indiceObjet).skull.intersections.size() <= maxIntersection);
		return intersectionValide;
	}

	public void tableauDeResultats()
	{
		ResultsTable rt = new ResultsTable(image.size()); 
		int surface, surfaceTotale, perimetre, perimetreTotal, nbBranches, tailleMoyenneBranches, nbBranchesTotal, tailleMoyenneBranchesTotale, taille;
		for (int numeroImage = 0; numeroImage < image.size(); numeroImage++)
		{
			rt.setValue("Image", numeroImage, numeroImage+1);
			taille = image.get(numeroImage).objetsPertinents.size();
			rt.setValue("Nombre d'objets détectés", numeroImage, taille);
			surfaceTotale = 0;
			perimetreTotal = 0;
			nbBranchesTotal = 0;
			tailleMoyenneBranchesTotale = 0; 
			int i;
			for (i = 0; i < taille; i++)
			{
				surface = image.get(numeroImage).objetsPertinents.get(i).surface;
				perimetre = image.get(numeroImage).objetsPertinents.get(i).perimetre;
				nbBranches = image.get(numeroImage).objetsPertinents.get(i).skull.nombreBranches();
				tailleMoyenneBranches = image.get(numeroImage).objetsPertinents.get(i).skull.moyenneLongueurBranche();
				surfaceTotale += surface;
				perimetreTotal += perimetre;
				nbBranchesTotal += nbBranches;
				tailleMoyenneBranchesTotale += tailleMoyenneBranches;
			}
			if (i != 0)
			{
				int surfaceMoyenne = surfaceTotale/taille;
				int perimetreMoyen = perimetreTotal/taille;
				rt.setValue("Surface moyenne", numeroImage, surfaceMoyenne);
				rt.setValue("Perimetre moyen", numeroImage, perimetreMoyen);
				rt.setValue("Roundness moyenne", numeroImage, Utilitaire.roundness(surfaceMoyenne, perimetreMoyen));
				rt.setValue("Nb de branches moyen", numeroImage, (double) nbBranchesTotal/((double) taille));
				rt.setValue("Taille de branche moyenne", numeroImage, tailleMoyenneBranchesTotale/taille);
				rt.setValue("Nombre d'alignements", numeroImage, image.get(numeroImage).alignements.size());
				rt.setValue("Nb objets alignés max", numeroImage, image.get(numeroImage).plusGrandAlignement());
			}
			else
			{
				rt.setValue("Surface moyenne", numeroImage, 0);
				rt.setValue("Perimetre moyen", numeroImage, 0);
				rt.setValue("Roundness moyenne", numeroImage, 0);
				rt.setValue("Nb de branches moyen", numeroImage, 0);
				rt.setValue("Taille de branche moyenne", numeroImage, 0);
				rt.setValue("Nombre d'alignements", numeroImage, 0);
				rt.setValue("Nb objets alignés max", numeroImage, 0);
			}
		}
		rt.show("Résultats de l'analyse");
	}
	
	public void encadrerObjetsPertinents()
	{
		for (int i = 0; i < image.size(); i++)
		{
			image.get(i).encadrerObjetsPertinents(50, 0xffffff);
			int[] pixels = (int []) stack.getProcessor(i+1).getPixels();
			pixels = (int []) image.get(i).pixelsCopie;
		}
	}
	
	public void initialiserVariables()
	{
		image = new ArrayList<Image>();
	}
	
	public void initialiserImage()
	{
		for (int i = 0; i < image.size(); i++)
		{
			image.get(i).initialiserImage();
		}
	}
	
	public void chargerImage()
	{
		macroRGB();
		ImagePlus imp = IJ.getImage();
		ImageProcessor im  = imp.getProcessor();
		int nbColonnes = im.getWidth();
		int nbLignes = im.getHeight();
		ImageStack stk = imp.getImageStack();
		for (int i = 1; i <= stk.getSize(); i++)
		{
			Image tmp = new Image((int[]) stk.getProcessor(i).getPixels(), nbColonnes, nbLignes);
			image.add(tmp); 
		}
		macro8bits();
		macroBinarise();
		macroRGB();
		ImagePlus impBinaire = IJ.getImage();
		ImageStack stkB = impBinaire.getImageStack();
		for (int i = 1; i <= stkB.getSize(); i++)
		{
			image.get(i-1).setPixelsBinaires((int[]) stkB.getProcessor(i).getPixels());
		}
		macroFermerImage(2);
		macroBinarise();
		macroSquelette();
		macroRGB();
		ImagePlus impSquelette = IJ.getImage();
		ImageStack stkS = impSquelette.getImageStack();
		for (int i = 1; i <= stkS.getSize(); i++)
		{
			image.get(i-1).setPixelsSquelettes((int[]) stkS.getProcessor(i).getPixels());
		}
		macroFermerImage(2);
		macroRGB();
		ImagePlus im2 = IJ.getImage();
		stack = im2.getStack();
	}

	public void run(String arg) {
		if (lireParam())
		{
			initialiserVariables();
			chargerImage();
			initialiserImage();
			determinerObjetsPertinents();
			encadrerObjetsPertinents();
			marquerAlignements();
			tableauDeResultats();
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
		gd.addNumericField("Nombre d'intersections min dans le squelette", 2, 0);
		gd.addNumericField("Nombre d'intersections max dans le squelette", 3, 0);
		gd.addNumericField("Facteur d'alignement toleree", 500, 0);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		minPixels = (int) gd.getNextNumber();
		maxPixels = (int) gd.getNextNumber();
		minIntersection = (int) gd.getNextNumber();
		maxIntersection = (int) gd.getNextNumber();
		facteurAlignement = (int) gd.getNextNumber();
		return true;
	}
}
