// Made by fab3F

import bot.Bot;
import general.ConfigWorker;
import general.SyIO;

import java.util.List;

public class Main {

    public static final String version = "1.0";
    private static final String configPath = "config";

    public static Main main;
    public static void main(String[] args) {
        main = new Main();
    }

    private final SyIO syIO;
    private final ConfigWorker configWorker;
    public Bot bot;

    public Main(){
        this.syIO = SyIO.getSyIO();
        syIO.println("STARTING VERSION " + version);
        this.configWorker = new ConfigWorker(configPath);
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





}