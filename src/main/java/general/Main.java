package general;// Made by fab3F

import bot.Bot;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Main {

    private static final String version = "1.0";
    private static final String configPath = "config";

    public static Main main;
    public static void main(String[] args) {
        main = new Main(args);
    }

    private final SyIO syIO;
    private final ConfigWorker configWorker;
    private final Logger logger;
    private Bot bot;

    public Main(String[] args){
        this.syIO = SyIO.getSyIO();
        syIO.println("STARTING VERSION " + version);
        this.configWorker = new ConfigWorker(configPath);
        this.logger = new Logger(new File(cfg("logPath") + syIO.getFilesep() + new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date()) + "-bot.log"));
        boolean debug = cfg("debug").get(0).equals("true");
        String token = cfg("token").get(0);
        bot = new Bot(debug, token, this.configWorker);
    }

    private List<String> cfg(String name){
        List<String> l = configWorker.getBotConfig(name);
        if(l.isEmpty()){
            syIO.println("[ERROR] Config file not correct");
            System.exit(-1);
        }
        return l;
    }

    public void closeProgram(){
        this.bot.destroy();
        this.logger.close();
    }


}