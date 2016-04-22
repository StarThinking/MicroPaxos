#### README for paxos template ####

## Description ##
The template provides basic utils for Paxos project:
1) network infrastructure
To use network, new class should implement the EventHandler interface.
To send message, sendMessage method is called with correct parameters.

2) Message
You are free to define arbitrary Messages. Under network/messages folder, parent
class Message is implemented and P1a/P1b/P2a/P2b are created as blank. You can
use other names for P1a/P1b/P2a/P2b messages, such as Prepare/Promise/Accept/Accepted.

3) NodeIdentifier
This class provides the methods to define and use node, such as ACCEPTOR and LEARNER.

4) paxos.conf
Sample configuration file is created and you are free to modify it and corresponding
Configuration.java to process new parameters.

5) Test hint
Current paxos.conf contains two parameter "testIndexA" and "testIndexB" that can 
be used to facilitate the unit test. Currently, NettyNetwork.java capture log index
of P2bMessage and terminate the network if either of the following two conditions is met:
1) index == testIndexA AND Leader
2) index == testIndexB AND non-leader

You are encouraged to design the test cases to verify the correctness of the protocol by
yourself. Please make sure to DISABLE these two parameters if not use it.
(by default, it is disabled)


## Test example ##
Test.java is a sample to show how to use basic utils.

1) Build
ant

2) cleanup
ant clean

3) run
./runTest.sh


Please feel free to contact Dr. Yang Wang or Rong Shi if you have any question.
wang.7564@osu.edu
shi.268@osu.edu