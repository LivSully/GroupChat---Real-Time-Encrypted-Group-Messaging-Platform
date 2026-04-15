//NEW NEW 4/15/26 1258
package src;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
//import java.util.HashSet;
//import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

public class Room {
    private final String name;
    private final List<ClientHandler> members = new ArrayList<>();
    private int memberCount;
    private final String chatlogFilePath;

    public Room(String name) {
        this.name = name;
        this.memberCount = 0;

        // ensure logs folder exists
        File chatlogDir = new File("logs");
        if (!chatlogDir.exists()) {
            chatlogDir.mkdirs();
        }

        this.chatlogFilePath = "logs/" + name + "_chatlog.txt";
    }

    public String getName() {
        return name;
    }

    public synchronized void addMember(ClientHandler client) {
        if (client == null || hasMember(client)) return;

        for (int i = 0; i < members.length; i++) {
            if (members[i] == null) {
                members[i] = client;
                memberCount++;
                return;
            }
        }
    }

    public synchronized void removeMember(ClientHandler client) {
        for (int i = 0; i < members.length; i++) {
            if (members[i] == client) {
                members[i] = null;
                memberCount--;
                return;
            }
        }
    }

    public synchronized boolean hasMember(ClientHandler client) {
        for (ClientHandler member : members) {
            if (member == client) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcast(String encryptedMessage, ClientHandler sender) {
        writeToLog(encryptedMessage);

        for (ClientHandler member : members) {
            if (member != null && member != sender) {
                member.send(encryptedMessage);
            }
        }
    }

    public void writeToLog(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chatlogFilePath, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to chat log for room " + name);
        }
    }

    public ClientHandler[] getMembers() {
        return members;
    }

    public List<String> getHistory() {
        List<String> lines = new ArrayList<>();
        if (!new File(chatlogFilePath).exists()) {
            return lines; // No history if file doesn't exist
        }
        try (BufferedReader br = new BufferedReader(new FileReader(chatlogFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

}
