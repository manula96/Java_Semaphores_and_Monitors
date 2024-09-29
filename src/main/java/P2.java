/*
 * Developer: Don Manula Ransika Udugahapattuwa - C3410266
 *
 * Description:
 * This program simulates the operation of a coffee machine that serves both hot and cold coffee
 * to clients. The coffee machine has three dispensers, and it can operate in either hot or cold mode at any time.
 * Clients arrive with different brew times and are either Hot or Cold clients. The machine ensures that hot and cold
 * clients cannot use the machine simultaneously, and clients are served in the order they arrive. The solution uses
 * Java threads and a monitor to ensure proper synchronization and fairness between hot and cold clients.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CoffeeMachine {
    private Lock lock = new ReentrantLock();
    private Condition hotCv = lock.newCondition();
    private Condition coldCv = lock.newCondition();

    private int hotClientsWaiting = 0;
    private int coldClientsWaiting = 0;
    private String activeMode = null; // Can be "hot" or "cold"
    private int activeClients = 0; // Number of clients currently using the machine
    private String[] dispensers = new String[3]; // Dispenser states, null means empty
    private int currentTime = 0; // Logical time tracking system

    private int brewingCount = 0; // Counter for brewing threads
    private Condition doneCondition = lock.newCondition(); // Condition for done signal

    public CoffeeMachine() {
        Arrays.fill(dispensers, null);
    }

    // Getter method for currentTime
    public int getCurrentTime() {
        return currentTime;
    }

    // Method to simulate a hot client making a coffee request
    public void hotClientRequest(String id, int brewTime) {
        lock.lock();
        try {
            hotClientsWaiting++;
            while (activeMode == "cold" || activeClients >= 3) {
                hotCv.await();  // Wait until it's hot client's turn
            }
            activeMode = "hot";
            hotClientsWaiting--;
            serveClient(id, brewTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    // Method to simulate a cold client making a coffee request
    public void coldClientRequest(String id, int brewTime) {
        lock.lock();
        try {
            coldClientsWaiting++;
            while (activeMode == "hot" || activeClients >= 3) {
                coldCv.await();  // Wait until it's cold client's turn
            }
            activeMode = "cold";
            coldClientsWaiting--;
            serveClient(id, brewTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    // Method to serve a client and occupy a dispenser
    private void serveClient(String id, int brewTime) throws InterruptedException {
        int dispenser = getAvailableDispenser();
        activeClients++;
        dispensers[dispenser] = id;
        brewingCount++;

        // Print the current state
        System.out.println("(" + currentTime + ") " + id + " uses dispenser " + (dispenser + 1) + " (time: " + brewTime + ")");

        // Start a thread to handle brew completion after brewTime seconds
        new Thread(() -> {
            try {
                int startTime = currentTime;
                Thread.sleep(brewTime * 1000); // Simulate brew time

                clientDone(dispenser, brewTime, startTime); // Mark client as done after brewing
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Client is done, free the dispenser and update time
    private void clientDone(int dispenser, int brewTime, int startTime) {
        lock.lock();
        try {
            dispensers[dispenser] = null;
            activeClients--;
            brewingCount--; // Decrement the brewing counter

            // Signal the done condition if all brewing is finished
            if (brewingCount == 0) {
                doneCondition.signalAll();
            }
            int endTime = brewTime + startTime;

            // Update the current time to reflect when this client finishes brewing
            if (currentTime < endTime) {
                currentTime = endTime;
            }

            if (activeClients == 0) {  // If all clients are done, switch mode
                if (coldClientsWaiting > 0) {
                    activeMode = "cold";
                    coldCv.signalAll();
                } else if (hotClientsWaiting > 0) {
                    activeMode = "hot";
                    hotCv.signalAll();
                } else {
                    activeMode = null;
                }
            } else if (coldClientsWaiting > 0 && activeMode == "cold") {
                coldCv.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    // Find the first available dispenser
    private int getAvailableDispenser() {
        for (int i = 0; i < dispensers.length; i++) {
            if (dispensers[i] == null) {
                return i;
            }
        }
        return -1; // Should never happen since we limit activeClients to 3
    }

    // Wait for all brewing operations to complete
    public void waitForCompletion() {
        lock.lock();
        try {
            while (brewingCount > 0) {
                doneCondition.await(); // Wait until all brewing is done
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}

// Client class representing hot or cold clients
class Client implements Runnable {
    private String id;
    private int brewTime;
    private CoffeeMachine coffeeMachine;
    private boolean isHot;

    public Client(String id, int brewTime, CoffeeMachine coffeeMachine, boolean isHot) {
        this.id = id;
        this.brewTime = brewTime;
        this.coffeeMachine = coffeeMachine;
        this.isHot = isHot;
    }

    @Override
    public void run() {
        if (isHot) {
            coffeeMachine.hotClientRequest(id, brewTime);
        } else {
            coffeeMachine.coldClientRequest(id, brewTime);
        }
    }
}

public class P2 {
    // Function to read input from file
    public static List<Client> readInput(CoffeeMachine coffeeMachine, String filename) {
        List<Client> clients = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // Read the number of clients
            int numClients = Integer.parseInt(br.readLine());

            // Read each client line
            for (int i = 0; i < numClients; i++) {
                String line = br.readLine();
                String[] parts = line.split(" ");
                String clientId = parts[0];
                int brewTime = Integer.parseInt(parts[1]);
                boolean isHot = clientId.startsWith("H");

                clients.add(new Client(clientId, brewTime, coffeeMachine, isHot));
            }
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
        }

        return clients;
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            System.err.println("Usage: java P2 <input_file>");
            return;
        }

        String filename = args[0];
        CoffeeMachine coffeeMachine = new CoffeeMachine();
        List<Client> clients = readInput(coffeeMachine, filename);

        List<Thread> threads = new ArrayList<>();

        // Create and start a thread for each client
        for (Client client : clients) {
            Thread t = new Thread(client);
            threads.add(t);
            t.start();
        }

        // Wait for all threads to finish
        for (Thread t : threads) {
            t.join();
        }

        // Wait for all brewing to complete
        coffeeMachine.waitForCompletion();

        // Print done when all clients are served
        System.out.println("(" + coffeeMachine.getCurrentTime() + ") DONE");
    }
}
