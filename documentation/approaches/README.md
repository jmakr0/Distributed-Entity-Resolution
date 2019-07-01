# Different approaches for DDD

## Glossary

* `Pk` = Partitioning key
* `Bk` = Blocking key
* `Sk` = Sorting key
* `infoObj` = Information object
	* Node resources, e.g. CPU, RAM, Disk
	* Processing time
	* Possible hashing algorithms
	* Current partition size, can be used to merge/split partion if too small/big
* `initObj` = Initialize object
	* `Bk/Sk`
	* Hashing algorithm
* `pT` = Partition table, containing information which node is responsible for certain `Bk/Sk`

## Approach 1

## Approach 2

### Master
ToDo: Short summary of core idea

### Worker
ToDo: Short summary of core idea

### Overview

<img src="images/DDD_Approach_2.png">

This approach can be edited [here](https://drive.google.com/file/d/1ibJdajcNLM9g0Ro6HXEOfIPplkyjYgL5/view?usp=sharing).

## Approach 3