package sustvar.custom.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import sustvar.utils.FileUtils;
import sustvar.utils.StringUtils;

public class FileManager {

	public FileManager() {
		
	}
	
	public File backupFile(File parent, File source, String newName) throws Exception {
		File newFile = new File(parent, newName);
		Files.copy(source.toPath(), newFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		
		return newFile;
	}
	
	public File backupDirectory(File parent, File source, String newName) throws Exception {
		File newDir = new File(parent, newName);
		newDir.mkdirs();
		
		copyFolder(source.toPath(), newDir.toPath());
		
		return newDir;
	}
	
	public File tryBackupFile(File parent, File source, Function<String, String> nameResolutor, int maxTry) throws Exception {
		for (int i = 1; i <= maxTry; i++) {
			String newName = nameResolutor.apply(String.valueOf(i));
			if(!FileUtils.elementExists(parent, newName)) {
				File newFile = new File(parent, newName);
				Files.copy(source.toPath(), newFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
				
				return newFile;
			}
		}
		
		throw new Exception(StringUtils.concat("No se pudo realizar el backup de ", source.toString(), ". Se detiene el proceso."));
	}
	
	public File tryBackupDirectory(File parent, File source, Function<String, String> nameResolutor, int maxTry) throws Exception {
		for (int i = 1; i <= maxTry; i++) {
			String newName = nameResolutor.apply(String.valueOf(i));
			if(!FileUtils.elementExists(parent, newName)) {
				File newDir = new File(parent, newName);
				newDir.mkdirs();
				
				copyFolder(source.toPath(), newDir.toPath());
				
				return newDir;
			}
		}
		
		throw new Exception(StringUtils.concat("No se pudo realizar el backup de ", source.toString(), ". Se detiene el proceso."));
	}
	
	public void copyFolder(Path src, Path dest) throws IOException {
	    try (Stream<Path> stream = Files.walk(src)) {
	        stream.forEach(source -> tryCopy(source, dest.resolve(src.relativize(source))));
	    }
	}

	private void tryCopy(Path source, Path dest) {
	    try {
	        Files.copy(source, dest, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
	    } catch (Exception e) {}
	}
	
	public List<File> getFiles(File target, boolean recursive) throws IOException {
		List<File> list = new LinkedList<>();
		if (target.isFile()) {
			list.add(target);
		} else if (target.isDirectory()) {
			Files.walkFileTree(target.toPath(), new FileVisitor<Path>() {
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if(file == null) return FileVisitResult.CONTINUE;
					
					File fileObj = file.toFile();
					
					if (fileObj.isFile()) {
						list.add(fileObj);
					}
					
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
				
			});
		}
		
		return list;
	}
}
