package humblePhlipper.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Protocol {
    protected humblePhlipper.ResourceManager rm;

    public Protocol(humblePhlipper.ResourceManager rm) {
        this.rm = rm;
    }
    public void begin() {
    }
    public void go(BufferedReader in, PrintWriter out) throws IOException {
    }
    public void finish() {
    }
}
