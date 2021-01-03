# Classic Snake implemented using a Genetic Artificial Neural Network
#### Written in Java 8
- Developed using Vim and the JDK
- The UI is implemented in JavaFX

#### Prerequisites
- Java 8 (JavaFX)
- Ant (use the most recent version)
#### Build Instructions
To build and run the generated JAR use the following Ant target

```
ant jar
java -jar genetic-snake.jar
```

Alternatively you can combine both operations using `ant run`

#### Notes
Currently the supplied code implements two different types of snake,
- Full Movement, see `FullMovementSnake.java`
- Forward Only, see `ForwardOnlySnake.java`
	- Both classes extend `Snake.java`
- This repository contains pre-built JARs for both types of snake
	- `full-movement-genetic-snake.jar`
	- `forward-only-genetic-snake.jar`
		- Run using `java -jar full-movement-genetic-snake.jar` etc.
- The code generates on-going statistics,
	- Displayed in the console
	- Written to a timestamped CSV file

Configure the snake type in Java before building, edit `GeneticSnake.java` to make your selection (defaults to the `FullMovementSnake` instance)

- Other constants defined in `GeneticSnake.java` include,
	-  The `AI_GRID_WIDTH` and `AI_GRID_HEIGHT` constants
	-  Also `MAX_GENERATIONS`, `POPULATION_SIZE` and the `MUTATION_RATE`
- The associated neural network configurations are defined in `SnakeFactory.java`, various activation functions have been included and can be configured to,
	- `ReLU.java` (the default)
	- `ExpLU.java`
	- `Sigmoid.java`

This code is a *'work in progress'* and I intend to add the following enhancements,
- A complete configuration UI
- Save / load snake
- Play / reply the best performing snake
- An evolution progress plot

If you decide to download and play with this code, have fun and get in touch with any issues or suggestions

#### Screenshot
```
An example of a forward only Snake
```
<img src='https://github.com/Positivedelta/-Snake-using-a-Genetic-Neural-Implementation/blob/main/forward-only-6i-6-8-3o.png' width='755' height='690'>
