# TCPvsUDP

TCP vs UDP - Messaging Showdown

Overview

This Java application simulates the difference between TCP (Transmission Control Protocol) and UDP (User Datagram Protocol) in a messaging scenario. The application provides a graphical user interface (GUI) to demonstrate how TCP ensures ordered and reliable delivery, while UDP is faster but may drop or reorder packets.

Features

- Simulates message flow between TCP and UDP protocols
- Visualizes packet transmission and reception
- Allows users to adjust UDP packet drop rate
- Displays statistics for TCP and UDP packets sent, received, and dropped
- Exports logs for TCP and UDP packets
- Takes screenshots of the application

Requirements

- Java Development Kit (JDK) 8 or later
- Java Runtime Environment (JRE) 8 or later

Usage

1. Compile the Java code using javac.
2. Run the application using java.
3. Adjust the UDP packet drop rate using the slider.
4. Click the "Simulate Message Flow" button to start the simulation.
5. Observe the packet transmission and reception visualization.
6. View the statistics for TCP and UDP packets sent, received, and dropped.
7. Export logs and take screenshots as needed.

Code Structure

The code is organized into a single Java class UI that extends JFrame. The class contains methods for building the GUI components, simulating message flow, animating packet transmission, and handling user interactions.

Notes

- This application is for educational purposes only and does not represent real-world network conditions.
- The packet drop rate is simulated using a random number generator and may not reflect actual network behavior.
