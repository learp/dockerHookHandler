package com.dockerHook;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by learp on 13.12.16.
 */
public class MyServer implements Runnable {

    public MyServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        String contentLength = "Content-Length: ";
        String repoName = "\"repo_name\": ";

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            while(!needToStop && !Thread.interrupted()) {
                try(Socket socket = serverSocket.accept()) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    int contentSize = 0;
                    String header;

                    while (!(header = in.readLine()).equals("")) {
                        if (header.startsWith(contentLength)) {
                            contentSize = Integer.valueOf(header.substring(contentLength.length()));
                        }
                    }

                    char[] buf = new char[contentSize];
                    in.read(buf, 0, contentSize);
                    String postBody = new String(buf);

                    if (postBody.contains(repoName)) {
                        exec("  docker pull hello-world");
                    }

                    String responce =
                            "HTTP/1.1 200 OK\n" +
                                    "Connection: Closed";

                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    out.write(responce);
                    out.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exec(String command) {
        StringBuffer output = new StringBuffer();

        Process p;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process process = processBuilder.start();
            process.waitFor();


            //p = Runtime.getRuntime().exec(command);
            //p.waitFor();

            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String s;
            while ((s = err.readLine()) != null) {
                System.out.println(s);
            }

            while ((s = out.readLine()) != null) {
                System.out.println(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        needToStop = true;
    }

    boolean needToStop = false;
    int port;
}
