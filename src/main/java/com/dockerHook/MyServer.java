package com.dockerHook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by learp on 13.12.16.
 */
public class MyServer implements Runnable {

    public static final String REPO_NAME_FROM_DOCKER_HOOK = "@repo_name";

    public MyServer(int port, String command) {
        this.port = port;
        this.command = command;
    }

    public void stop() {
        needToStop = true;
    }

    @Override
    public void run() {
        String contentLength = "Content-Length: ";
        String repoName = "repo_name";

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            Socket socket;

            while(!needToStop && !Thread.interrupted()) {
                socket = serverSocket.accept();

                try {
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

                    System.out.println(postBody);

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode post = mapper.readTree(postBody);

                    if (post.has(repoName)) {
                        System.out.println(post.findValue(repoName).asText());

                        new Thread(() -> exec(
                                command.replaceAll(REPO_NAME_FROM_DOCKER_HOOK, post.findValue(repoName).asText())
                        )).start();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    String responce =
                            "HTTP/1.1 200 OK\n" +
                                    "Connection: Closed";

                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    out.write(responce);
                    out.close();
                    socket.close();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exec(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process process = processBuilder.start();
            process.waitFor();

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

    private boolean needToStop = false;
    private int port;
    private String command;
}
