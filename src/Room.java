//4-13-26 old "groupchat" code updated 
package src;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Room {
    private final String name;
    private Set<ClientHandler> members = new HashSet<>();
    private int memberCount;
    private final String chatlogFilePath;

    public Room(String name) {
        this.name = name;
        this.members = new ClientHandler[15]; // max users per room
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

    private void writeToLog(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chatlogFilePath, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to chat log for room " + name);
        }
    }
}
