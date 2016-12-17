package com.dockerHook;

import java.io.File;
import java.io.IOException;

/**
 * Created by learp on 13.12.16.
 */
public class Hook {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 4567;

        if (args.length == 2 && args[0].equals("-p")) {
            port = Integer.valueOf(args[1]);
        }

        System.out.println("!!!Docker hook handler started on port " + port);

        MyServer server = new MyServer(port);
        server.run();
    }
}
