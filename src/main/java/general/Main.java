package general;// Made by fab3F

import bot.Bot;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Main {

    private static final String version = "2.1";
    private static final String configPath = "config";

    public static Main main;
    public static void main(String[] args) {
        main = new Main();
    }

    private final ConfigWorker configWorker;
    private final Logger logger;
    private final boolean debug;
    private Bot bot;

    public Main(){
        this.configWorker = new ConfigWorker(configPath);
        this.logger = new Logger(new File(cfg("logPath").get(0) + File.separator + new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date()) + "-bot.log"));
        Main.log("STARTING VERSION " + version);
        debug = cfg("debug").get(0).equals("true");
        String token = cfg("token").get(0);
        this.bot = new Bot(debug, token, this.configWorker);
    }

    private List<String> cfg(String name){
        List<String> l = configWorker.getBotConfig(name);
        if(l.isEmpty()){
            Main.error("Config file not correct");
            System.exit(-1);
        }
        return l;
    }


    public void restartBot(){
        this.bot.destroy();

        boolean debug = cfg("debug").get(0).equals("true");
        String token = cfg("token").get(0);
        this.bot = new Bot(debug, token, this.configWorker);
    }

    public void closeBot(){
        this.bot.destroy();
        this.logger.close();
    }


    public static void debug(String message){
        if(main.debug)
            System.out.println("[DEBUG-" + new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date()) + "] " + message);
    }

    public static void error(String message){
        System.err.println("[ERROR-" + new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date()) + "] " + message);
    }

    public static void thread(String message){
        System.out.println("[THREAD-" + new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date()) + "] " + message);
    }

    public static void log(String message){
        System.out.println("[LOG-" + new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date()) + "] " + message);
    }


}