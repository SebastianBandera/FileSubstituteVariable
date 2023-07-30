package sustvar.utils;

import java.io.File;
import java.util.Objects;

public class FileUtils {

	public final static String getFileNameWithoutExtension(File file) {
		Objects.requireNonNull(file);
		if (!file.isFile()) throw new IllegalArgumentException("must be a file");
		
		String originalName = file.getName();
		
		if (originalName == null) return originalName;
		
		int lastIndexDot = originalName.lastIndexOf(".");
		
		if (lastIndexDot == -1) {
			return originalName;
		} else {
			return originalName.substring(0, lastIndexDot);
		}
	}

	public final static String getFileExtension(File file) {
		Objects.requireNonNull(file);
		if (!file.isFile()) throw new IllegalArgumentException("must be a file");
		
		String originalName = file.getName();
		
		if (originalName == null) return originalName;
		
		int lastIndexDot = originalName.lastIndexOf(".");
		
		if (lastIndexDot == -1) {
			return "";
		} else {
			try {
				return originalName.substring(lastIndexDot + 1);				
			} catch (Exception e) {
				return "";
			}
		}
	}
	
	public final static boolean elementExists(File dir, String name) {
		if (!dir.isDirectory()) throw new IllegalArgumentException("dir must be a directory");
		if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("name must not be empty");
		
		File target = new File(dir, name);
		
		return target.exists();
	}

	public final static String getDirLastName(File target) {
		if (!target.isDirectory()) throw new IllegalArgumentException("must be a directory");
		
		return target.getName();
	}
}
