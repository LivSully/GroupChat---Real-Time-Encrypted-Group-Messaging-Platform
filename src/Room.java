package src;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

public class Room {
    private final String name;
    private final List<ClientHandler> members = new ArrayList<>();
    private int memberCount;
    private final String chatlogFilePath;

    // Constructor method that creates a new Room object with a name, 
    // where each room has its own encrypted log file in the logs folder
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

    // Method that returns the name of the room
    public String getName() {
        return name;
    }

    // Method that adds a client to the room
    public synchronized void addMember(ClientHandler client) {
        if (client == null || hasMember(client)) return;

        members.add(client);
        memberCount++;
    }

    // Method that removes a client from the room
    public synchronized void removeMember(ClientHandler client) {
        members.remove(client);
        memberCount--;
    }

    // Method that returns whether or not a specific client is a member of the room
    public synchronized boolean hasMember(ClientHandler client) {
        return members.contains(client);
    }

    // Method that broadcasts an encrypted message to all of the members of the room,
    // and logs the message in the room's log file
    public synchronized void broadcast(String encryptedMessage, ClientHandler sender) {
        writeToLog(encryptedMessage);

        for (ClientHandler member : members) {
            if (member != null && member != sender) {
                member.send(encryptedMessage);
            }
        }
    }

    // Method that writes an encrypted message to the room's log file
    public void writeToLog(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chatlogFilePath, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to chat log for room " + name);
        }
    }

    // Method that returns a list of the members of the room
    public List<ClientHandler> getMembers() {
        return members;
    }

    // Method that returns the full encrypted message history of the room
    public List<String> getHistory() {
        List<String> lines = new ArrayList<>();
        if (!new File(chatlogFilePath).exists()) {
            return lines; 
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
