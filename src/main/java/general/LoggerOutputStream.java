package general;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerOutputStream extends OutputStream {
    private final OutputStream console;
    private final OutputStream file;

    public LoggerOutputStream(OutputStream console, OutputStream file) {
        this.console = console;
        this.file = file;
    }

    @Override
    public void write(int b) throws IOException {
        console.write(b);
        file.write(getWithDate(String.valueOf(b)));
    }

    @Override
    public void write(byte[] b) throws IOException {
        console.write(b);
        file.write(getWithDate(new String(b)));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        console.write(b, off, len);
        file.write(getWithDate(new String(b)), off, len);
    }

    @Override
    public void flush() throws IOException {
        console.flush();
        file.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            console.close();
        } finally {
            file.close();
        }
    }

    private byte[] getWithDate(String input){
        return ("[" + new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date()) + "] " + input).getBytes();
    }

}