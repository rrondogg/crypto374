This program is split into two parts: 
Client program (rte_TCPClient.java)
Server program (rte_TCPServer.java)
Both parts must be running for full functionality.
***********************************
The client program is multithreaded allowing for simultaneous communication. That is, we can send
and recieve at the same time. The main thread is used for recieving and the sender thread is used for sending.
This also allows for us to have multiple users connected to the server and talk to each other.
Through a multithreaded client hanlder and shared data structure of clients in the server program
we are able to serve all threads and synchronize the way the threads write to the file and send each other
messages.
***************************************************************************************
The client program takes command line arguments from the user to set the appropriate
username, host address, and port number. Should you omit any of these, they will be either
set to the default values or you will be prompted after compiling.
Client args: -p portnumber -h host address -u username
Server args: -p portnumber
**NOTE** These arguments can be entered in any order desired
The program allows the client and the server to communicate text to each other, as well as save 
the chat to a file and send it back to the client. Since these are multithreading programs,
we are able to have multiple clients and simultaneous communication. If the chatroom is empty and a client
connects, we create a new text file. If a client joins an ongoing session between other clients,
we send the contents of the chat file to that client. Once the room becomes empty again, we destroy
the chat file. At the end, the server also sends the length of the
session back to the client.
**************************COMPILING AND RUNNING**********************

----rte_TCPClient.java----
It is wise to always run your server before your client program
Should you run this command format:
C:\Users\yourname\somewhere/somewhere\rte_proj1>java TCPClient.java -p 22700 -h 35.35.35.355

the command line will return the following:

Please enter a username:

In the case where you omit -p or -h, omitting -p would assign the default port number of 22700 and
omitting -h would assign your local address

After connection, the program should prompt you with this:

Enter message:

At which point you can continue to enter messages
The program finishes the input "DONE" *case sensitive

Here is an example of what is it like to run the program straight from the command line when
you have two people connected:
****************************************************************************************
Microsoft Windows [Version 10.0.19044.2486]
(c) Microsoft Corporation. All rights reserved.

C:\Users\rron_\Desktop\old code\tawork\rte_proj1\src>java rte_TCPClient.java -u rron -p 22700
Hey
obama: hey man!
This is my example
obama : your example looks good
everything works fine
im ending now
DONE
Server received 4 messages from client

File contents:

rron: Hey
obama: hey man!
rron: This is my example
obama : your example looks good
rron: everything works fine
rron: im ending now


Length of session: 0hrs::0mins::17sec::623ms

!!!!! Closing connection... !!!!!

C:\Users\rron_\Desktop\old code\tawork\rte_proj1\src>
******************************************************************************************
Program successfully returns to terminal after completion


----rte_TCPServer.java----
Should you run this command format: C:\Users\yourname\somewhere/somewhere\rte_proj1>java TCPServer.java

The server program would just run with the default port number 22700.

After which, the program would display this:

Opening port. . .

This means that the program is waiting for the client socket to link to its own
After successful connection, the program should display this: 

Client has established a connection to "host"

Host being the machine name or the ip address
During running, the server program not only communicates with the client program but also stores all messages into a chat
file and then returns it to the user at the end.

Here is an example of the server program being ran on the command line:
***********************************************************************
Microsoft Windows [Version 10.0.19044.2486]
(c) Microsoft Corporation. All rights reserved.

C:\Users\rron_\Desktop\old code\tawork\rte_proj1\src>java rte_TCPServer.java
Opening port...

Client has established a connection to Killcowseveryday
Client has established a connection to Killcowseveryday
rron: Hey
obama: hey man!
rron: This is my example
obama : your example looks good
rron: everything works fine
rron: im ending now
rron disconnected from the chatroom
obama disconnected from the chatroom
!!!!! Closing connection... !!!!!
!!! Waiting for the next connection... !!!
***********************************************************************
The server program does not return to command line but rather waits for the next connection.


************************************CONCLUSION***********************************
This program was a bit harder than project 1 since it included multithreading. Reading up on multithreading
on my previous 311 book really helped me out. I was able to complete this program in around 12 hours. The hardest part I had
was aligning my text where I wanted to before I synched everything together. I feel much more comfortable with multithreading
and am excited to try it out on personal projects now.
