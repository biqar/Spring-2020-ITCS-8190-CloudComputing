# Persistent Memory (PMem) Aware MapReduce (MR) Algorithm

Introduction

There is a sample implementation of the famous MapReduce (MR) algorithm for persistent memory (PMEM) [1], using the C++ bindings of libpmemobj, which is a core library of the Persistent Memory Development Kit (PMDK).
The goal of that example project is to show how PMDK facilitates implementation of a persistent memory aware MR with an emphasis on data consistency through transactions as well as concurrency using multiple threads and PMEM aware synchronization.
The natural fault-tolerance capabilities of PMEM can be seen by killing the program halfway through and restarting it from where it left off, without the need for any checkpoint/restart mechanism.

While pursuing this course, my goal is to check further in-detail about MapReduce (by using this example code) and finding possible options for research.

#### Resources
1. pmdk-examples (mapreduce): https://github.com/pmem/pmdk-examples/tree/master/mapreduce
