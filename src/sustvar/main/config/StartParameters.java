package sustvar.main.config;

import java.io.File;

import sustvar.utils.StringUtils;

public class StartParameters {

	public final static String PARAM_TARGET = "target";
	public final static String PARAM_CONFIG_FILE = "config";
	public final static String PARAM_RECURSIVE = "recursive";
	public final static String PARAM_ONE_BACKUP = "onebackup";
	public final static String PARAM_CMD = "cmd";
	public final static String PARAM_SECOND_CMD = "cmd2";
	
	//Required
	private File target;
	private File config;
	
	//Optional
	private String cmd;
	private String cmd2;
	private boolean recursive;
	private boolean oneBackup;
	
	public StartParameters() {
		this.target = null;
		this.recursive = false;
		this.cmd = null;
		this.cmd2 = null;
	}
	
	public void validate() throws Exception {
		if (target == null) throw new Exception("Falta el parámetro " + PARAM_TARGET);
		if (config == null) throw new Exception("Falta el parámetro " + PARAM_CONFIG_FILE);
		
		if (!target.isDirectory() && !target.isFile()) throw new Exception(StringUtils.concat("El parámetro ", PARAM_TARGET, " debe ser un directorio o un archivo."));
		if (!config.isFile()) throw new Exception(StringUtils.concat("El parámetro ", PARAM_CONFIG_FILE, " debe ser un archivo."));
	}

	public File getTarget() {
		return target;
	}

	public void setTarget(File target) {
		this.target = target;
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

        public String getCmd2() {
            return cmd2;
        }

        public void setCmd2(String cmd2) {
            this.cmd2 = cmd2;
        }

	public boolean isOneBackup() {
		return oneBackup;
	}

	public void setOneBackup(boolean oneBackup) {
		this.oneBackup = oneBackup;
	}

	public File getConfig() {
		return config;
	}

	public void setConfig(File config) {
		this.config = config;
	}
}
