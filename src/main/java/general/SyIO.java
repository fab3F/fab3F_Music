package general;

import java.io.File;
import java.util.Scanner;

public class SyIO {
    private static SyIO syIO = null;
    public static SyIO getSyIO(){
        if (syIO == null) {
            syIO = new SyIO();
        }
        return syIO;
    }

    private final Scanner in;
    private final String filesep;

    public SyIO(){
        in = new Scanner(System.in);
        filesep = File.separator;
    }

    public String readLine(){return in.nextLine();}
    public void print(String s){System.out.print(s);}
    public void print(int i){System.out.print(i);}
    public void print(long l){System.out.print(l);}
    public void println(String s){System.out.println(s);}
    public void println(int i){System.out.println(i);}
    public void println(long l){System.out.println(l);}
    public void closeSys(){in.close();}
    public String getFilesep(){return filesep;}


}
