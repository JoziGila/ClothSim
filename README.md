Simple 2D cloth simulation made as a final project for Introduction to Java class.

# Cloth Simulation

The concept of this proof of concept application is not new at all as my fascination with simulating physics has been present since I learned to program. Essentially this program presents a grid of interconnected points that behaves as cloth. However simple it appears to the non-programmer the code has a fairly complex structure and is abstracted into several classes for better accessibility and readability. The underlying algorithms are used in almost any computer simulation and are very prominent in the field. 

# Implementation

The application is written in java and using only inherent java libraries. Implementation wise there were a few technical difficulties which sadly set me back quite a lot and I was not able to deliver it when planned. One big problem is the translation between physics calculation which most are in double values, and pixels on the screen which are strictly integers. Also my first design was to use acceleration based physics for the elasticity of the cloth but that proved to be very inefficient and inaccurate (the calculations of arctan() and cos() are estimates not exact values).

The final design came to be based on three main classes + one renderer and two main algorithms. Each passes objects between each other and no global variables are declared. 

The base class is Particle which defines the basic unit in a particle simulation, it contains the position in timestep = x and in timestep = x - 1 (current position and past position) it also holds the variables of acceleration and whether the particle can be moved or its static. Its main method only solves the Verlet Integration for a given elapsed time (dT).

The links between the particles are defined in the class Constraint. It holds the starting particle and the ending particle of a constraint and the rest length between them. Its methods deal with keeping the connected particles at a certain distance from each other (Distance Correction) and finding the distance between them using basic linear algebra.

The most important class is the Cloth. It has two main elements the matrix of particles and the ArrayList of the constraints between them. It initializes the particles based on a starting position using a standard nested loop and does the same with the constraints except here uses a grid traversal approach (right, bottom). The update function loops through all the constraints “solving” them and then applies to all points the verlet integration. To have a better representation of momentum of the points all constraints generally should be solved several times within the same frame to “relax” them.

Verlet Integration
http://lonesock.net/article/verlet.html

