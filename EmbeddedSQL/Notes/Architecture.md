# The Architecture

we will have three distinct layers each one having an interface, by separating each system and using the interface so that we can modify one subsystem without impacting the other is an example of **bridge pattern**.

## Data Storage Layer
It will manage the actual data that makes up a table and with it its persistence. 
It exposes 2 interfaces
- Table (with defines access to the table)
- Cursor (which provides an iterator across rows)

## SQL Engine Layer
This layer interprets SQL commands and uses the underlying Data Storage layer to make the required calls. It exposes `result sets` as *Table* Objects, which can be read through a *Cursor*. 

This way we have separated two sides of a combined system, one of which manage the underlying data storage and the other side manages query interpretation from the user side.

## JDBC-driver Layer
It will wrap the SQL engine so as to make it JDBC compatible. Using the JDBC *bridge* also lets us replace our *well-put* together database with something with more features and production ready without having to modify our code. 


The messaging between the different layers is going to be unidirectional since the SQL engine will have to send commands to the data storage layer, but the data storage layer does not know anything about the SQL engine. 

JDBC Driver -> SQL Engine -> Data Storage

