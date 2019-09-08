# Shared

In order to try out different ways of distributing the process of entity resolution we decided to put all the code that is used regardless of the way of distribution into several Java projects that can be imported using maven. All of them are documented using `Javadoc`.

## Available projects: 
* **data**: Contains the functionality to parse the input data as well as the gold standard.
* **entity-resolution**: Contains the logic of the entity resolution process (e.g: calculation similarities of numbers, strings and records)
* **evaluation**: Provides code to evaluate the results of the entity resolution process against the corresponding *gold standard* by using _accuracy_, _recall_, and _F1-score_ as metrics.
* **partitioning**: Provides the logic for the hash partitioning.
* **set-operations**: Provides simple set operation functionalities that are used for example for the calculation of the F1-score.

## Use the libraries
The code base of the shared projects is imported into the `monolith` and `optimistic` approach using maven.
Since we don't wanted to publish the projects as publicly available maven dependencies they have to be build and imported before they can be used. 

For this we provide the [update-local-dependencies script](../maven/update-local-dependencies.sh). It first builds the projects using `mvn clean verify` and afterwards imports the project locally using the `mvn install:install-file` command.

After running the script the libraries can be imported and used like normal maven libraries.
For example to include the `evaluation` project add the following dependency to your maven dependencies.

```xml
<dependency>
	<groupId>de.hpi.rdse.der</groupId>
	<artifactId>evaluation</artifactId>
	<version>1.0</version>
</dependency>
```
