# Server Configurations

## Overview
This document outlines the configurations for the servers and the View Server responsible for handling transactions.

## Server Details
The following servers have been considered:

| Server Name          | Port Number |
|----------------------|-------------|
| S1                   | 8001        |
| S2                   | 8002        |
| S3                   | 8003        |
| S4                   | 8004        |
| S5                   | 8005        |
| S6                   | 8006        |
| S7                   | 8007        |
| S8                   | 8008        |
| S9                   | 8009        |
| Client / View Server | 8000        |
|                      |             |


## View Server
This is actually not a View Server by definition. This is just a bridge between Input File and servers.

Like an Input Controller Client. This represents a Client that sends transactions to all the servers.

## Commands

| Command Names    | Command Details                                       |
|------------------|-------------------------------------------------------| 
| PrintDataStore   | Prints Datastore of each server                       |
| PrintStatus      | Prints All Transaction SeqNum statuses across servers |
| PrintBalance     | Prints Balance of Data Items in cluster's servers     |
| PrintPerformance | Prints Performance                                    |


## Bonus 2 - Configurable Clusters

Edit the configuration definitions like TOTAL_SERVERS, CLUSTER_SIZE and TOTAL_DATA_ITEMS in run_java.bat file to configure the cluster size and data items. 
Dynamically creates the Clustering schemes based on these values.