package sustvar.main.modules;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import sustvar.utils.StringUtils;

public class FileVarSubstitutor {

	private final List<File> files;
	private final File config;
        private final File elementToUse;
        private final Consumer<String> show;
	
	public FileVarSubstitutor(List<File> files, File config, File elementToUse, Consumer<String> show) {
		this.files  = files;
		this.config = config;
                this.elementToUse = elementToUse;
                this.show = show;
	}

	public void run() throws IOException {
		Map<String, String> configs = loadConfig(config);
		
		for(File file : files) {
			processFile(file, configs);
		}
	}

	private void processFile(File file, Map<String, String> configs) throws IOException {
		List<String> newLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)
									 .stream()
									 .map(line -> processLine(line, configs))
									 .collect(Collectors.toList());
		
		Files.write(file.toPath(), newLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
	}
	
	private String processLine(String line, Map<String, String> configs) {
		Iterator<Entry<String, String>> iter = configs.entrySet().iterator();
		
		String newLine = line;
		
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			
			String varToken = StringUtils.concat("${", entry.getKey(), "}");
			
			newLine = newLine.replace(varToken, entry.getValue());
		}
		
		return newLine;
	}

	private Map<String, String> loadConfig(File config) throws IOException {
		Map<String, String> configs;
		
		configs = Files.readAllLines(config.toPath(), StandardCharsets.UTF_8)
			 .stream()
			 .filter(Objects::nonNull)
			 .map(item -> item.split("="))
			 .filter(Objects::nonNull)
			 .filter(item -> item.length == 2)
			 .map(item -> {
				 String[] newItem = new String[2];
				 newItem[0] = item[0] == null ? "" : item[0].trim();
				 newItem[1] = item[1] == null ? "" : item[1].trim();
				 return newItem;
			 })
			 .collect(Collectors.toMap(item -> item[0], item -> item[1]));
		
		configs.put("targetDir", elementToUse.getAbsolutePath());
                
                Iterator<Entry<String, String>> iter = configs.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, String> item = iter.next();
                    
                    if(item.getKey()!=null) {
                        StringBuilder text = new StringBuilder();
                        text.append(item.getKey());
                        text.append(":");
                        if(item.getKey().toLowerCase().contains("pass")) {
                            text.append("****");
                        } else {
                            text.append(item.getValue());
                        }

                        show.accept(text.toString());
                    }
                }
                
                show.accept("");
		
		return configs;
	}
}
