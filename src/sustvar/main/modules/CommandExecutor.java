package sustvar.main.modules;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import sustvar.utils.StringUtils;

public class CommandExecutor {

	private final String cmd;
	private final File dir;
	
	private Consumer<String> show = (str) -> System.out.println(str);
	
	public CommandExecutor(String cmd, File dir) {
		this.cmd = cmd;
		this.dir = dir;
	}

	public void run() throws IOException {
		String system = System.getProperty("os.name").toLowerCase();
		
		show.accept(StringUtils.concat("Sistema: ", system));
		
		Process pro = Runtime.getRuntime().exec(cmd, null, dir);
		
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		
		StreamGobbler streamGobbler = new StreamGobbler(pro.getInputStream(), show);
		show.accept(StringUtils.concat("Obteniendo salida del comando: ", cmd));
		executorService.submit(streamGobbler);		
	}

	public void setShow(Consumer<String> show) {
		this.show = show;
	}

	public String getCmd() {
		return cmd;
	}
}
