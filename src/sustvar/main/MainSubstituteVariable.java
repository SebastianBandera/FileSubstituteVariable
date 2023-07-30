package sustvar.main;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import sustvar.custom.utils.FileManager;
import sustvar.main.config.StartParameters;
import sustvar.main.modules.CommandExecutor;
import sustvar.main.modules.FileVarSubstitutor;
import sustvar.utils.Box;
import sustvar.utils.FileUtils;
import sustvar.utils.General;
import sustvar.utils.StringUtils;

/**
 * Sustituye variables en todos los archivos de un directorio y luego tiene la posibilidad de ejecutar un comando
 */
public class MainSubstituteVariable implements Runnable {

	private final String[] raw_args;
	private StartParameters params;
	
	private FileManager fileManager = new FileManager();
	
	private final Consumer<String> show = (str) -> System.out.println(str);
	
	/**
	 * @param args Argumentos desde command-line
	 */
	public MainSubstituteVariable(String[] args) {
		this.raw_args = args;
	}
	
	@Override
	public void run() {
		try {
			_run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void _run() throws Exception {
		params = loadArgs();
		
		show.accept("Copiando archivos...");
		//Se aplica el cambio sobre el backup, para evitar que en caso de que el archive venga de un repositorio como Git, se genere conflicto con posibles nuevos cambios.
		File elementToUse = backupTarget(params.getTarget(), params.isOneBackup());
		show.accept("Listo.");
		show.accept("");
		
		List<File> files = fileManager.getFiles(elementToUse, params.isRecursive());
		
		FileVarSubstitutor fileVarSubtitutor = new FileVarSubstitutor(files, params.getConfig());
		
		fileVarSubtitutor.run();
		
		CommandExecutor cmdExe = new CommandExecutor(params.getCmd(), elementToUse);
		
		cmdExe.setShow(show);
		cmdExe.run();
	}

	private File backupTarget(File target, boolean replaceBackup) throws Exception {
		int maxTry = Integer.MAX_VALUE;
		File parent = target.getParentFile();
		if (parent == null) throw new Exception("No se encontró la ruta padre al directorio seleccionado.");
		
		File finalFile;
		
		if (target.isFile()) {
			if (replaceBackup) {
				finalFile = fileManager.backupFile(parent, target, StringUtils.concat(FileUtils.getFileNameWithoutExtension(target), "_backup.", FileUtils.getFileExtension(target)));
			} else {
				finalFile = fileManager.tryBackupFile(parent, target, (number) -> StringUtils.concat(FileUtils.getFileNameWithoutExtension(target), "_backup", number,".", FileUtils.getFileExtension(target)), maxTry);				
			}
		} else if (target.isDirectory()) {
			if (replaceBackup) {
				finalFile = fileManager.backupDirectory(parent, target, StringUtils.concat(FileUtils.getDirLastName(target), "_backup"));
			} else {
				finalFile = fileManager.tryBackupDirectory(parent, target, (number) -> StringUtils.concat(FileUtils.getDirLastName(target), "-backup", number), maxTry);
			}
		} else {
			throw new Exception("Error. El target no es un archivo ni un directorio.");
		}
		
		return finalFile;
	}

	/**
	 * Carga los parametros en un objeto StartParameters
	 * @return Instancia validada de StartParameters
	 * @throws Exception
	 */
	private StartParameters loadArgs() throws Exception {
		List<String> params = parseToList(raw_args);

		Map<Optional<String>, List<String>> paramsMapped = params.stream().collect(Collectors.groupingBy(input -> {
			final String inputLower = input.toLowerCase();
			
			//Para evitar seguir evaluado si ya encontró cual es el parámetro que llegó, se usa 'coalesce'
			String result = General.coalesce(() -> evaluateParam(StartParameters.PARAM_TARGET, inputLower),
					 						 () -> evaluateParam(StartParameters.PARAM_CONFIG_FILE, inputLower),
											 () -> evaluateParam(StartParameters.PARAM_RECURSIVE, inputLower),
											 () -> evaluateParam(StartParameters.PARAM_ONE_BACKUP, inputLower),
											 () -> evaluateParam(StartParameters.PARAM_CMD, inputLower));
			
			return Optional.ofNullable(result);
		}));
		
		checkUnique(paramsMapped);
		processValues(paramsMapped);
		
		StartParameters paramObject = new StartParameters();
		
		consumeParam(paramsMapped, Optional.of(StartParameters.PARAM_TARGET), (paramValue) -> {
			if (paramValue!=null && paramValue.endsWith(File.separator)) {
				paramValue = paramValue.substring(0, paramValue.length() - 1);
			}
			paramObject.setTarget(new File(paramValue));
		});
		consumeParam(paramsMapped, Optional.of(StartParameters.PARAM_CONFIG_FILE), (paramValue) -> {
			if (paramValue!=null && paramValue.endsWith(File.separator)) {
				paramValue = paramValue.substring(0, paramValue.length() - 1);
			}
			paramObject.setConfig(new File(paramValue));
		});
		consumeParam(paramsMapped, Optional.of(StartParameters.PARAM_RECURSIVE), (paramValue) -> {
			boolean booleanValue = paramValue!=null && paramValue.toLowerCase().equals("true");
			paramObject.setRecursive(booleanValue);
		});
		consumeParam(paramsMapped, Optional.of(StartParameters.PARAM_ONE_BACKUP), (paramValue) -> {
			boolean booleanValue = paramValue!=null && paramValue.toLowerCase().equals("true");
			paramObject.setOneBackup(booleanValue);
		});
		consumeParam(paramsMapped, Optional.of(StartParameters.PARAM_CMD), (paramValue) -> {
			paramObject.setCmd(paramValue);
		});
		
		printParams(paramsMapped);
		
		paramObject.validate();
		
		return paramObject;
	}
	
	/**
	 * Extrae el valor del parametro y realiza una tarea con dicho parámetro
	 * @param paramsMapped
	 * @param paramName
	 * @param task
	 */
	private void consumeParam(Map<Optional<String>, List<String>> paramsMapped, Optional<String> paramName, Consumer<String> task) {
		if (task == null) return;
		
		if (paramsMapped.containsKey(paramName)) {
			String paramValue = General.safeGet(paramsMapped.get(paramName));
			
			task.accept(paramValue);
		}
	}

	/**
	 * Revisa que un parámetro no haya llegado más de una vez
	 * @param paramsMapped
	 * @throws Exception
	 */
	private void checkUnique(Map<Optional<String>, List<String>> paramsMapped) throws Exception {
		Box<Exception> errorCatcher = new Box<>();
		
		commonProcessMap(paramsMapped, (key, list) -> {
			if (list != null && list.size() > 1) {
				errorCatcher.set(new Exception(StringUtils.concat("No se admite más de un valor para el parámetro ", key.get())));
			}
		});
		
		if (errorCatcher.get()!=null) {
			throw errorCatcher.get();
		}
	}

	/**
	 * Ajusta los valores de los parámetros para conservar sólo su valor y no su nombre. Dado que el formato es -nombre:vallor
	 * @param paramsMapped
	 * @throws Exception
	 */
	private void processValues(Map<Optional<String>, List<String>> paramsMapped) throws Exception {
		commonProcessMap(paramsMapped, (key, list) -> {
			List<String> newList = list.stream()
					   .filter(Objects::nonNull)
					   .map(item -> item.substring(key.get().length() + 1))
					   .collect(Collectors.toList());

			paramsMapped.put(key, newList);
		});
	}
	
	private void printParams(Map<Optional<String>, List<String>> paramsMapped) throws Exception {
		Box<Boolean> print = new Box<>(false);
		
		commonProcessMap(paramsMapped, (key, list) -> {
			if(!print.get()) {
				show.accept("Parámetros reconocidos");
				print.set(true);
			}
			
			show.accept(StringUtils.concat("Key: ", key.get(), ". Value: ", General.safeGet(list)));
		});
		
		show.accept("");
	}
	
	/**
	 * Para utilizarlo en 'checkUnique' y 'processValues'
	 * @param paramsMapped
	 * @param task
	 * @throws Exception
	 */
	private void commonProcessMap(Map<Optional<String>, List<String>> paramsMapped, BiConsumer<Optional<String>, List<String>> task) throws Exception {
		if (paramsMapped == null) return;
		if (task ==null) return;
		
		Iterator<Entry<Optional<String>, List<String>>> iter = paramsMapped.entrySet().iterator();
		
		while (iter.hasNext()) {
			Entry<Optional<String>, List<String>> entry = iter.next();
			String paramName = entry.getKey().orElse(null);
			if (paramName == null) continue;
			
			List<String> list = entry.getValue();
			
			task.accept(entry.getKey(), list);
		}
	}

	/**
	 * Si el input comienza con 'expectedParam:', devuelve 'expectedParam'
	 * @param expectedParam
	 * @param input
	 * @return
	 */
	private String evaluateParam(String expectedParam, String input) {
		if (input.startsWith(expectedParam + ":")) {
			return expectedParam;
		}
		
		return null;
	}
	
	/**
	 * Arreglo de String a List de String
	 * @param args
	 * @return
	 */
	private List<String> parseToList(String[] args) {
		List<String> params;
		
		if (args == null) {
			params = new LinkedList<>();
		} else {
			params = Arrays.asList(args)
						   .stream()
						   .filter(Objects::nonNull)
						   .collect(Collectors.toList());
		}
		
		return params;
	}
}
