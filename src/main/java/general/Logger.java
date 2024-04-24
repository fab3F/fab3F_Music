package general;

import java.io.*;

public class Logger{

    private PrintStream console;
    private PrintStream file;

    public Logger(File f){
        if(cantCreateFile(f))
            return;

        this.console = System.out;
        try {
            this.file = new PrintStream(new FileOutputStream(f));
        } catch (FileNotFoundException e) {
            return;
        }

        System.setOut(new PrintStream(new LoggerOutputStream(this.console, this.file)));
        System.setErr(new PrintStream(new LoggerOutputStream(this.console, this.file)));
    }

    public void close(){
        this.console.close();
        this.file.close();
    }

    private boolean cantCreateFile(File f){
        if(f.getParentFile().exists() && f.exists())
            return false;

        if(!f.getParentFile().exists()){
            if(!f.getParentFile().mkdirs()){
                return true;
            }
        }

        if(!f.exists()){
            try {
                if(f.createNewFile()){
                    return false;
                }
            }catch (IOException ex){
                return true;
            }
        }
        return true;
    }

}
