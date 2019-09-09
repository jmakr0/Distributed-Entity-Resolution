# Shared

To make our code reusable in different approaches, we implemented parts in different projects. These can be locally imported as `maven` dependency. For more details, please read attached the `Javadoc`.

## Available projects
* **data**: Contains the functionality of parsing the input and gold standard data.
* **entity-resolution**: Provides the similarity calculation for numbers, strings, and records.
* **evaluation**: Evaluates results against the corresponding *gold standard* by using _accuracy_, _recall_, and _F1-score_ as metrics.
* **partitioning**: Provides the logic for the hash partitioning.
* **set-operations**: Provides simple set operation functionalities that are used for example for the calculation of the F1-score.
* **transitive-closure**: TODO

## Usage
The projects are imported into the `monolith` and `optimistic` approach using `maven`. Since we don't wanted to publish the projects as publicly available `maven` dependencies they have to be build and imported before they can be used. 

We provide the [update-local-dependencies](../../maven/update-local-dependencies.sh) script. It first builds the projects using `mvn clean verify`, and afterward, imports the project locally using the `mvn install:install-file` command.

After running the script the libraries can be imported and used like normal `maven` libraries.
For example, to include the `evaluation` project add the following dependency to your maven dependencies.

```xml
<dependency>
	<groupId>de.hpi.rdse.der</groupId>
	<artifactId>evaluation</artifactId>
	<version>1.0</version>
</dependency>
```
