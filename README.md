Problem 1: MAC Management

Simulates autonomous medical supply carts (MACs) navigating trails in a hospital. This problem addresses deadlock prevention and fairness at an intersection between two trails.

    Objective: Prevent deadlock and collisions, ensure starvation-free operation, and allow only one MAC at a time in the intersection.
    Implementation: Uses semaphores to coordinate MACs at the intersection. Each MAC is implemented as a thread with checkpoints to simulate movement through the intersection.

Input: The input file specifies the initial number of MACs from each direction (CSR1, CSR2, ED1, ED2) and the number of intersection crossings for each.

Output: Console output displays each MAC’s interaction at the intersection, crossing details, and total crossings for each trail.
Problem 2: Coffee Machine Simulation

Simulates a coffee machine that serves hot and cold coffee to clients, where only one type (hot or cold) can be served at a time across three dispensers.

    Objective: Ensure clients are served in order of arrival while allowing only one type of drink to be served at a time. Hot and cold clients should alternate fairly, preventing starvation.
    Implementation: Uses monitor-based synchronization to control access to the coffee machine and manage client requests for hot or cold coffee.

Input: The input file specifies the total number of clients and each client’s ID (hot or cold) and brew time.

Output: Console output shows each client’s usage of the coffee machine, with timestamps, client IDs, dispenser numbers, and total service time.

Includes two main problems involving concurrency and mutual exclusion in Java.

## Project Structure

```plaintext
Assignment2
├── src
│   ├── P1.java                # Main class for Problem 1 (MAC Management)
│   ├── P2.java                # Main class for Problem 2 (Coffee Machine Simulation)
├── input
│   ├── P1-1.txt               # Sample input file for Problem 1
│   └── P2-1.txt               # Sample input file for Problem 2
├── Report.pdf                 # Reflective report on the use of Generative AI tools (if applicable)
├── Research_Report.pdf        # Mutual exclusion report (COMP6240 only)
└── README.md                  # Project documentation
