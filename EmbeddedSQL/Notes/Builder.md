## Builder Pattern
The CSVImporter, CSVExporter can be replaced for other importer/exporters as it seems fit, 
this technique of separating the business logic from the implementation-specific details such as 
how to handle data reading/writing or displaying data on screen is a part of the **Builder Pattern**.

This way the business domain object can use various and even multiple exporters at the same time
without having to be rewritten. 

> The business class plays the role of *Director* whereas the concrete-builder (which implement the Builder interface) 
create the representation.

In this case, the `ConcreteTable` is the `Director`, `Table.Exporter` is the *builder*, 
and `CSVExporter` is the *Concrete Builder*.

`Table.Importer` can also be considered a *builder* but the key difference is that 
it is responsible for converting various representations (of data) into a single Table object
instead of the other way around which is expected of a *builder*.  

