/*
 * Developer: Don Manula Ransika Udugahapattuwa - C3410266
 *
 * Description:
 * This program simulates multiple autonomous carts (MACs) traveling between two emergency departments (EDs)
 * and two central supply rooms (CSRs) in a hospital. The MACs must cross an intersection where collisions
 * may occur if more than one MAC attempts to enter at the same time. The system ensures that only one MAC
 * crosses the intersection at a time using semaphores, while also preventing deadlock and starvation.
 *
 * The input is read from a file that specifies the number of MACs starting from each direction and the number
 * of times each MAC must cross the intersection.
 */

import java.io.*;
import java.util.concurrent.Semaphore;

class MAC implements Runnable {
    private static Semaphore intersection = new Semaphore(1, true);  // Ensures mutual exclusion and fairness

    // Static variables to track the total crossings in each trail
    private static int trail1Count = 0;
    private static int trail2Count = 0;

    private int macId;
    private String direction;
    private String status;
    private int totalCrossings;

    public MAC(int id, String direction, String status, int totalCrossings) {
        this.macId = id;
        this.direction = direction;
        this.status = status;
        this.totalCrossings = totalCrossings;
    }

    private void passIntersection() throws InterruptedException {
        // Simulate passing through 3 checkpoints
        for (int checkpoint = 1; checkpoint <= 3; checkpoint++) {
            System.out.println("MAC-" + macId + " (" + status + "): Crossing intersection Checkpoint " + checkpoint + ".");
            Thread.sleep(50);  // 50ms delay per checkpoint
        }
        System.out.println("MAC-" + macId + " (" + status + "): Crossed the intersection.");

        // Update counts and print totals
        updateCounts();
    }

    private synchronized void updateCounts() {
        if (direction.equals("CSR1") || direction.equals("ED1")) {
            trail1Count++;
        } else {
            trail2Count++;
        }
        System.out.println("Total crossed in Trail1: " + trail1Count + " Trail2: " + trail2Count);
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < totalCrossings; i++) {
                System.out.println("MAC-" + macId + " (" + status + "): Waiting at the Intersection. Going towards " + direction);
                intersection.acquire();  // Enter intersection
                passIntersection();      // Cross intersection
                intersection.release();  // Exit intersection

                // Switch direction and status after crossing
                if (direction.equals("ED1") || direction.equals("ED2")) {
                    direction = direction.equals("ED1") ? "CSR1" : "CSR2";
                    status = "Empty";
                } else {
                    direction = direction.equals("CSR1") ? "ED1" : "ED2";
                    status = "Stock";
                }
            }
            System.out.println("MAC-" + macId + ": Finished.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class P1 {

    public static void main(String[] args) {
        // Reading the input file provided as a command-line argument
        if (args.length < 1) {
            System.out.println("Usage: java P1 <input-file>");
            return;
        }

        String inputFileName = args[0];

        try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            // Reading the first line from the input file
            String inputLine = br.readLine();
            String[] parts = inputLine.split(", ");

            // Parsing the input data
            int CSR1 = Integer.parseInt(parts[0].split("=")[1]);
            int CSR2 = Integer.parseInt(parts[1].split("=")[1]);
            int ED1 = Integer.parseInt(parts[2].split("=")[1]);
            int ED2 = Integer.parseInt(parts[3].split("=")[1]);
            int N = Integer.parseInt(parts[4].split("=")[1]);

            // Create MACs based on input
            int macId = 1;
            int totalMACs = CSR1 + CSR2 + ED1 + ED2;
            Thread[] macs = new Thread[totalMACs];
            int index = 0;

            // Create MACs from CSR1 (MAC-1)
            for (int i = 0; i < CSR1; i++) {
                macs[index++] = new Thread(new MAC(macId++, "ED1", "Stock", N));
            }

            // Create MACs from CSR2 (MAC-2 and MAC-3)
            for (int i = 0; i < CSR2; i++) {
                macs[index++] = new Thread(new MAC(macId++, "ED2", "Stock", N));
            }

            // Create MACs from ED1 (MAC-4)
            for (int i = 0; i < ED1; i++) {
                macs[index++] = new Thread(new MAC(macId++, "CSR1", "Empty", N));
            }

            // Create MACs from ED2 (MAC-5)
            for (int i = 0; i < ED2; i++) {
                macs[index++] = new Thread(new MAC(macId++, "CSR2", "Empty", N));
            }

            // Start all MAC threads in order to match the sample output as closely as possible
            for (Thread mac : macs) {
                mac.start();
            }

            // Wait for all MACs to finish
            for (Thread mac : macs) {
                mac.join();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
