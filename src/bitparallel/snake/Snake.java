//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import bitparallel.snake.geometry.Movement;
import bitparallel.snake.geometry.Point;
import bitparallel.snake.neural.NeuralNetwork;

public abstract class Snake
{
    private static final Logger logger = LogManager.getLogger(Snake.class);

    private static final int SNAKE_MOVE_TIMEOUT_INITIAL = 200;
    private static final int SNAKE_MOVE_TIMEOUT_INCREMENT = 50;
    private static final int SNAKE_MOVE_TIMEOUT_LIMIT = 500;

    protected static final int HATCHLING_LENGTH = 4;
    protected static final int HATCHLING_SPAWN_MARGIN = 4;

    private final NeuralNetwork brain;
    private final List<Movement> movements;
    private final List<Point> foodLocations;
    private long fitness;
    private int moveTimeoutLimit;

    protected static final double PI_BY_TWO = Math.PI / 2.0;
    protected static final double THREE_PI_BY_TWO = 3.0 * Math.PI / 2.0;
    protected static final double ROOT_TWO = Math.sqrt(2.0);

    protected final int gridWidth, gridHeight;
    protected final PsuedoRandom random;
    protected final LinkedList<Point> segments, hatchling;

    // food locations are constructed using specific Point instances, so delegated to the specific concrete derived class
    // requied to support List.contains() when matching the snake head segment with the food location
    //
    protected abstract Point createNewFoodLocation(final int x, final int y);

    //  look for the food, grid boundaries, the snake's own body etc
    //  used to generate the neural input values, normalised to [0.0, 1.0] or [-1.0, 1.0] where appropriate
    //  northerly and westerly distances are -ve, southerly and easterly distances are +ve
    //
    protected abstract double[] look(final Point snakeHead, final Point foodLocation);

    // specfic Snake instances require associated Movement instances, this task is delegated to the derived class
    //
    protected abstract Movement createNewMovement(final double[] movementVector);

    // hatchling snakes are constructed using specific Point instances, so delegated to the specific concrete derived class
    //
    protected abstract void addNorthHatchling();
    protected abstract void addEastHatchling();
    protected abstract void addSouthHatchling();
    protected abstract void addWestHatchling();

    public Snake(final NeuralNetwork brain, final int gridWidth, final int gridHeight)
    {
        this.brain = brain;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        // create the initial hatchling snake, always straight, 3 segments and a head, can point in any 90 degree compass heading
        // notes 1, the snake is defined tail first
        //       2, grid[0, 0] is defined as top left, bottom right is [gridWidth - 1, gridHeight - 1]
        //
        hatchling = new LinkedList<Point>();
        segments = new LinkedList<Point>();

        random = PsuedoRandom.INSTANCE;
        switch (random.nextInt(4))
        {
            // north facing snake
            //
            case 0:
            {
                addNorthHatchling();
                if (logger.isDebugEnabled()) for (int i = 0; i < segments.size(); i++) logger.debug("Hatchling (North): " + segments.get(i).toString());
                break;
            }

            // east facing snake
            //
            case 1:
            {
                addEastHatchling();
                if (logger.isDebugEnabled()) for (int i = 0; i < segments.size(); i++) logger.debug("Hatchling (East): " + segments.get(i).toString());
                break;
            }

            // south facing snake
            //
            case 2:
            {
                addSouthHatchling();
                if (logger.isDebugEnabled()) for (int i = 0; i < segments.size(); i++) logger.debug("Hatchling (South): " + segments.get(i).toString());
                break;
            }

            // west facing snake
            //
            case 3:
            {
                addWestHatchling();
                if (logger.isDebugEnabled()) for (int i = 0; i < segments.size(); i++) logger.debug("Hatchling (West): " + segments.get(i).toString());
                break;
            }
        }

        // used to record the movements and food locations for this snake
        // notes 1. movements are stored as relative displacements
        //       2. food locations are stored as absolute grid locations
        //
        movements = new ArrayList<Movement>();
        foodLocations = new ArrayList<Point>();

        // each time the snake consumes food, this limit is increased by SNAKE_MOVE_TIMEOUT_INCREMENT
        //
        moveTimeoutLimit = SNAKE_MOVE_TIMEOUT_INITIAL;

        // used to rank snakes
        //
        fitness = 0;
    }

    //
    // methods below are for simulation
    //

    public final NeuralNetwork getBrain()
    {
        return brain;
    }

    public final void survive()
    {
        // generate the initial food location, make sure it doesn't exist within the hatchling snake
        //
        Point foodLocation = createNewFoodLocation(random.nextInt(gridWidth), random.nextInt(gridHeight));
        while (segments.contains(foodLocation)) foodLocation = createNewFoodLocation(random.nextInt(gridWidth), random.nextInt(gridHeight));
        foodLocations.add(foodLocation);

        double distanceToFood = Math.sqrt(Math.pow((foodLocation.getX() - segments.getLast().getX()), 2.0) + Math.pow((foodLocation.getY() - segments.getLast().getY()), 2.0));
        int moveTimeout = 0, movedCloserToFood = 0, movedAwayFromFood = 0, foodCount = 0;
        while (true)
        {
            if (++moveTimeout > moveTimeoutLimit)
            {
                logger.debug("Snake killed off due to a 'no food found' move timeout: " + moveTimeoutLimit);
                break;
            }

            final Point currentHead = segments.getLast();
            final double[] visionVector = look(currentHead, foodLocation);
            final double[] movementVector = brain.think(visionVector);
            final Movement move = createNewMovement(movementVector);

            // slither time... check for snake collision with the boundary and itself
            //
            final Point newHead = move.translate(currentHead);
            if ((newHead.getX() < 0) || (newHead.getX() >= gridWidth) || (newHead.getY() < 0) || (newHead.getY() >= gridHeight)) break;
            if (segments.contains(newHead)) break;

            segments.addLast(newHead);
            movements.add(move);

            // penalise the snake if it moves away from the food, keep specific counts, used later to calculate the fitness score
            //
            final double newDistanceToFood = Math.sqrt(Math.pow((foodLocation.getX() - newHead.getX()), 2.0) + Math.pow((foodLocation.getY() - newHead.getY()), 2.0));
            if (newDistanceToFood <= distanceToFood)
            {
                movedCloserToFood++;
            }
            else
            {
                movedAwayFromFood++;
            }

            distanceToFood = newDistanceToFood;

            if (foodLocation.equals(newHead))
            {
                foodCount++;
                moveTimeout = 0;
                moveTimeoutLimit += SNAKE_MOVE_TIMEOUT_INCREMENT;
                if (moveTimeoutLimit > SNAKE_MOVE_TIMEOUT_LIMIT) moveTimeoutLimit = SNAKE_MOVE_TIMEOUT_LIMIT;

                // generate new food and update the location
                //
                foodLocation = createNewFoodLocation(random.nextInt(gridWidth), random.nextInt(gridHeight));
                while (segments.contains(foodLocation)) foodLocation = createNewFoodLocation(random.nextInt(gridWidth), random.nextInt(gridHeight));
                foodLocations.add(foodLocation);
            }
            else
            {
                // no food found, so remove the last snake segment
                // i.e. the snake has not been able to "grow" its last segment once the body has been shifted
                //
                segments.removeFirst();
            }
        }

        // how did the snake perform?
        //
        // notes 1, snakes that move away from their food more than they do towards it get penalised
        //       2, this appears to work well and outperforms the original / simpler "moveCount * (2 << foodCount)" approach
        //
        fitness = movedCloserToFood - (int)Math.round(1.5 * movedAwayFromFood) + (10 * foodCount);
    }

    public final long getFitness()
    {
        return fitness;
    }

    public final int getLength()
    {
        return segments.size();
    }

    //
    // methods below are used for animation, should this snake get chosen...
    //

    public final LinkedList<Point> getHatchling()
    {
        return hatchling;
    }

    public final List<Movement> getMovements()
    {
        return movements;
    }

    public final List<Point> getFoodLocations()
    {
        return foodLocations;
    }
}
