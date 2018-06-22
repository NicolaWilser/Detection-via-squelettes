package DetectionSquelettes;

import ij.IJ;

public class Utilitaire {

	public static boolean contientVoisin(int indice, int couleur, int[] image, int nbColonnes)
	{
		if (image[indice-1] == couleur)
		{
			return true;
		}
		if (image[indice+1] == couleur)
		{
			return true;
		}
		if (image[indice+nbColonnes-1] == couleur)
		{
			return true;
		}
		if (image[indice+nbColonnes+1] == couleur)
		{
			return true;
		}
		if (image[indice-nbColonnes-1] == couleur)
		{
			return true;
		}
		if (image[indice-nbColonnes+1] == couleur)
		{
			return true;
		}
		if (image[indice+nbColonnes] == couleur)
		{
			return true;
		}
		if (image[indice-nbColonnes] == couleur)
		{
			return true;
		}
		return false;
	}

	public static boolean sontAlignes(int x1, int y1, int x2, int y2, int x3, int y3, int approximation)
	{
		int calcul = (y3-y1)*(x2-x1)-(y2-y1)*(x3-x1);
		return (Math.abs(calcul) <= approximation);
	}

	public static double distance(int x1, int y1, int x2, int y2)
	{
		double xCarre = (double) (x2-x1)*(x2-x1);
		double yCarre = (double) (y2-y1)*(y2-y1);
		return (double) Math.sqrt(xCarre+yCarre);
	}
	
	public static double roundness(int surface, int perimetre)
	{
		return (double) ((4*Math.PI*surface)/(perimetre*perimetre));
	}
	
	public static void macroSquelette()
	{
		IJ.runMacro("run(\"Skeletonize\", \"stack\")");
	}
	
	public static void macroDuplicate()
	{
		IJ.runMacro("run(\"Duplicate...\", \"duplicate\");");
	}

	public static void macroRGB()
	{
		IJ.runMacro("run(\"RGB Color\");");
	}

	public static void macro8bits()
	{
		IJ.runMacro("run(\"8-bit\");");
	}

	public static void macroBinarise(int minPixels, int maxPixels)
	{
		IJ.runMacro("run(\"Duplicate...\", \"duplicate\");run(\"Auto Threshold\", \"method=Otsu white stack\");run(\"Open\", \"stack\");run(\"Analyze Particles...\", \"size="+Integer.toString(minPixels)+"-"+Integer.toString(maxPixels)+" show=Masks clear stack\");setOption(\"BlackBackground\", true);run(\"Make Binary\", \"method=Default background=Light calculate black\");");
	}

	public static void macroFermerImage(int nbFois)
	{
		String str = "";
		for (int i = 0; i < nbFois; i++)
		{
			str+= "run(\"Close\");";
		}
		IJ.runMacro(str);
	}
}