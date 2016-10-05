package bubblesRNG;

import java.io.File;

public class workSpace {

	static String workspace = "";

    //Sets the workspace folder
	static void setPath(String path) {
		File ws = new File(path);
		if (!(ws.exists() && ws.isDirectory())) ws.mkdirs();
		workspace = path;
	}

}
