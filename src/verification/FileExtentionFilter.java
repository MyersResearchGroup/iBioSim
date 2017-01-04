package verification;

import java.io.File;
import java.io.FileFilter;

public class FileExtentionFilter implements FileFilter {
	
	private final String extension;
  
	FileExtentionFilter(String extenstion) {
	    this.extension = extenstion;
	  }
	
	@Override
	public boolean accept(File pathname) {
		return pathname.getPath().toLowerCase().endsWith(extension);
	}
}
