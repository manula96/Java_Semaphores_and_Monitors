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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CoffeeMachine {
    private final Lock lock = new ReentrantLock();
    private final Condition hotCondition = lock.newCondition();
    private final Condition coldCondition = lock.newCondition();
    private boolean isHotMode = false;  // Machine mode: hot or cold
    private int availableDispensers = 3;  // Three dispensers available
    private int hotClientsWaiting = 0;  // Number of hot clients waiting
    private int coldClientsWaiting = 0;  // Number of cold clients waiting
    private boolean isMachineInUse = false;  // Tracks whether the machine is in use

    // Method for clients to use the machine
    public void useMachine(Client client) throws InterruptedException {
        lock.lock();
        try {
            // Track waiting clients
            if (client.isHot) {
                hotClientsWaiting++;
            } else {
                coldClientsWaiting++;
            }

            // Set the mode for the first client
            if (!isMachineInUse) {
                isMachineInUse = true;
                isHotMode = client.isHot;
            }

            // Wait if the client type doesn't match the current mode, or no dispensers are available
            while ((client.isHot && !isHotMode) || (!client.isHot && isHotMode) || availableDispensers == 0) {
                if (client.isHot) {
                    hotCondition.await();  // Hot client waits
                } else {
                    coldCondition.await();  // Cold client waits
                }
            }

            // Occupy a dispenser
            availableDispensers--;
            int dispenserId = 3 - availableDispensers;  // Assign dispenser id (1, 2, or 3)
            System.out.printf("(%d) %s uses dispenser %d (time: %d)\n", client.arrivalTime, client.id, dispenserId, client.brewTime);

            // Simulate brew time
            lock.unlock();  // Release lock while brewing
            Thread.sleep(client.brewTime * 100);  // Simulate brew time in milliseconds
            lock.lock();  // Reacquire lock after brew

            // Free the dispenser after brewing
            availableDispensers++;

            // Update waiting clients count
            if (client.isHot) {
                hotClientsWaiting--;
            } else {
                coldClientsWaiting--;
            }

            // Signal the next clients waiting based on mode
            if (hotClientsWaiting == 0 && coldClientsWaiting > 0) {
                isHotMode = false;
                coldCondition.signalAll();  // Let cold clients go next
            } else if (coldClientsWaiting == 0 && hotClientsWaiting > 0) {
                isHotMode = true;
                hotCondition.signalAll();  // Let hot clients go next
            } else if (isHotMode) {
                hotCondition.signalAll();  // Continue serving hot clients if there are more
            } else {
                coldCondition.signalAll();  // Continue serving cold clients if there are more
            }

            // Reset machine mode if no clients are waiting
            if (availableDispensers == 3 && hotClientsWaiting == 0 && coldClientsWaiting == 0) {
                isMachineInUse = false;
                isHotMode = false;  // Allow the mode to switch freely
            }

        } finally {
            lock.unlock();
        }
    }
}

// Client class representing hot or cold clients
class Client {
    String id;
    boolean isHot;
    int brewTime;
    int arrivalTime;

    public Client(String id, int brewTime, int arrivalTime) {
        this.id = id;
        this.brewTime = brewTime;
        this.arrivalTime = arrivalTime;
        this.isHot = id.charAt(0) == 'H';  // Identify if client wants hot or cold coffee
    }
}

public class P2 {

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        if (args.length != 1) {
            System.out.println("Please provide the input file name.");
            return;
        }

        List<Client> clients = readClientsFromFile(args[0]);
        CoffeeMachine machine = new CoffeeMachine();

        List<Thread> threads = new ArrayList<>();
        for (Client client : clients) {
            Thread thread = new Thread(() -> {
                try {
                    machine.useMachine(client);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            threads.add(thread);
            thread.start();  // Start the client thread
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("DONE");
    }

    // Method to read client data from file
    public static List<Client> readClientsFromFile(String fileName) throws FileNotFoundException {
        List<Client> clients = new ArrayList<>();
        Scanner scanner = new Scanner(new File(fileName));

        int numClients = scanner.nextInt();  // Number of clients in the file
        int currentTime = 0;
        for (int i = 0; i < numClients; i++) {
            String id = scanner.next();  // Client ID (e.g., H1, C1)
            int brewTime = scanner.nextInt();  // Brew time for the client
            clients.add(new Client(id, brewTime, currentTime));  // Add client to the list
            currentTime += brewTime;  // Update current time for each client
        }
        return clients;
    }
}
