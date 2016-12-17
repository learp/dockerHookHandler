package com.dockerHook;

import java.io.File;
import java.io.IOException;

/**
 * Created by learp on 13.12.16.
 */
public class Hook {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 4567;
        String command = "";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-p")) {
                port = Integer.valueOf(args[++i]);
            }

            if (args[i].equals("-c")) {
                while(++i < args.length && !args[i].startsWith("-")) {
                    command += args[i] + " ";
                }
            }
        }

        System.out.println("!!! Docker hook handler started on port " + port +
                            " with command " + command + " !!!");

        MyServer server = new MyServer(port, command);
        server.run();
    }
}
