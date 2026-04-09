package src;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
// import java.util.concurrent.CopyOnWriteArrayList;

public class ChatGroup {
    private final String name;
    private List<ClientHandler> members = new ArrayList<>();
    private final String chatlogFilePath;

    public ChatGroup(String name) {
        this.name = name;
        // this.members = new CopyOnWriteArrayList<>();
        this.members = new ArrayList<>();

        // ensure the logs directory exists, i am confident this will work
        File chatlogDir = new File("logs");
        if (!chatlogDir.exists()) {
            chatlogDir.mkdirs(); // creates the folder if it doesnt already exist
        }

        this.chatlogFilePath = "logs/" + name + "_chatlog.txt";
    }

    public String getName() {
        return name;
    }
    
    public void addMember(ClientHandler client) {
        members.add(client);
        //broadcast("[SERVER]" + client.getUsername() + "joined the group.", null);
    }
    public void removeMember(ClientHandler client) {
        members.remove(client);
        //broadcast("[SERVER]" + client.getUsername() + "left the group.", null);
    }

    public void broadcast(String message, ClientHandler sender) { 
        writeToLog(message);

        // Send to all members
        for (ClientHandler member : members) {
            // might be !(xyz).equals instead of !=
            if (member != sender) {
                member.send(message);
            }
        }
    }
    
    // i am confident this will work
    private void writeToLog(String message) { 
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chatlogFilePath, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to chat log for room " + name);
        }
    }
}
