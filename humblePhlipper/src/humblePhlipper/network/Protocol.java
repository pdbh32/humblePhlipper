package humblePhlipper.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Protocol {
    protected humblePhlipper.ResourceManager rm;
    protected BufferedReader in;
    protected PrintWriter out;
    public Protocol(humblePhlipper.ResourceManager rm) {
        this.rm = rm;
    }
    public void feed(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }
    public void begin() {
    }
    public void go() throws IOException {
    }
    public void finish() {
    }
}
