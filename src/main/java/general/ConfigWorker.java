package general;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class ConfigWorker {

    private final SyIO syIO = SyIO.getSyIO();
    private final String filesep = syIO.getFilesep();
    private final String configPath;
    public ConfigWorker(String configPath){
        this.configPath = configPath;
    }

    public List<String> getBotConfig(String name){
        return getConfig(new File(configPath + filesep + "bot.config"), name);
    }

    public List<String> getServerConfig(String guildId, String name){
        if(createConfigForServer(guildId)){
            return getConfig(new File(configPath + filesep + "server" + filesep + guildId + ".config"), name);
        }
        return new ArrayList<>();
    }

    public boolean addServerConfig(String guildId, String name, String value){
        if(!createConfigForServer(guildId)){
            return false;
        }
        List<String> config = getServerConfig(guildId, name);
        config.add(value);
        return updateConfig(guildId, name, config);
    }

    public boolean removeServerConfig(String guildId, String name, String value){
        if(!createConfigForServer(guildId)){
            return false;
        }
        List<String> config = getServerConfig(guildId, name);
        if(config.remove(value)){
            return updateConfig(guildId, name, config);
        }
        return true;
    }

    public boolean removeAllServerConfig(String guildId, String name){
        if(!createConfigForServer(guildId)){
            return false;
        }
        return updateConfig(guildId, name, new ArrayList<>());
    }

    // if server has no config file (new server) its created
    private boolean createConfigForServer(String guildId){
        Path path = Paths.get(configPath + filesep + "server" + filesep + guildId+".config");
        if(Files.exists(path))
            return true;

        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Path template = Paths.get(configPath + filesep + "server" + filesep + "template.config");
            Files.copy(template, path, StandardCopyOption.REPLACE_EXISTING);
            return true;
        }catch (IOException e){
            syIO.println("[ERROR] Cant create config file for server: " + guildId);
            return false;
        }
    }

    // returns List with all contents of requestet config
    private List<String> getConfig(File file, String name){
        boolean read = false;
        List<String> l = new ArrayList<>();
        Scanner config;
        try {
            config = new Scanner(file);
        } catch (FileNotFoundException e) {
            syIO.println("[ERROR] Config file not found for request " + name + " in file: " + file.getPath());
            return l;
        }

        while(config.hasNextLine()) {

            String s = config.nextLine();

            if(!s.startsWith("  ")){
                read = false;
            }

            if(read){
                l.add(s.substring(2));
            }

            if(!s.startsWith("  ") && s.equalsIgnoreCase(name)) {
                read = true;
            }

        }
        config.close();
        return l;
    }

    // removes all old and adds new values
    private boolean updateConfig(String guildId, String name, List<String> values){
        List<String> lines = new ArrayList<>();
        File file = new File(configPath + filesep + "server" + filesep + guildId+".config");
        Scanner config;
        try {
            config = new Scanner(file);
        } catch (FileNotFoundException e) {
            syIO.println("[ERROR] Config file not found for request " + name + " in file: " + file.getPath());
            return false;
        }
        while (config.hasNextLine()){
            lines.add(config.nextLine());
        }
        int index = lines.indexOf(name);
        if(index<0)
            return false;

        index++;
        boolean remove = true;
        while(remove){
            if(lines.get(index).startsWith("  ")){
                lines.remove(index);
            }else{
                remove = false;
            }
        }

        for(String value : values){
            lines.add(index, "  "+value);
            index++;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            syIO.println("[ERROR] Cant write config file for server: " + file.getPath() + " with name " + name);
            return false;
        }
    }

}
