# FileSubstituteVariable

Tiene como objetivo sustituir variables con formato ${var} en todos los archivos de un determinado directorio (o archivo individual), para luego ejecutar un comando.
Las variables se definen en un archivo de configuración.

Una posible invocación de ejemplo es:
```
java -jar FileSubstituteVariable.jar target:"<file or directory>" onebackup:true config:"<baseDir>\config.properties" cmd:"cmd /c command.bat"
```
