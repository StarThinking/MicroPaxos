#!/bin/bash
java -cp lib/*:dist/lib/* -Xmx512m -Xms512m paxosProject.Test paxos.conf
