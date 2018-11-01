# Operating-Systems-Term-Project

In this project, I created a program which is able to simulate the execution of multiple processes. First I wrote a program which is able to execute one process written by the given assembly language.Then,I wrote the code which will be able to execute several processes in a way that is similar to how computer systems work.

The assumption is there is only one CPU core. This means that at a given time, only one process will be able to run while the other processes will be waiting for their turns. For the processes which are waiting their turns, I used a Java list structure as a queue like before to store the waiting processes. This list is named as Ready Queue. 

A process can give an output or receive an input depending on the value of V Register. When receiving an input, the process should wait for the user to give an input, if this waiting process were to be done in CPU, then the execution cycle of CPU would be blocked and other processes would not be able to run. Because of that, for the processes which are waiting for an input, I used list as a queue and this queue is called Blocked Queue.


I used a bounded buffer as a queue to keep the information of processes which will not be able to be loaded into the memory. This bounded buffer is named as File Input Queue. 


It is also possible for user to give an input while no process is waiting. I stored these inputs for processes which will be waiting for an input. To store these inputs,I used a bounded buffer as a queue and it is named Console Input Queue. 
