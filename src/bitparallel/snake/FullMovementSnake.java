//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake;

import bitparallel.snake.geometry.FullMovement;
import bitparallel.snake.geometry.Movement;
import bitparallel.snake.geometry.Point;
import bitparallel.snake.geometry.PointXY;
import bitparallel.snake.neural.NeuralNetwork;

public class FullMovementSnake extends Snake
{
    private final double gridDiagonal, normalisedGridDiagonal;

    public FullMovementSnake(final NeuralNetwork brain, final int gridWidth, final int gridHeight)
    {
        super(brain, gridWidth, gridHeight);

        gridDiagonal = Math.sqrt((gridWidth - 1) * (gridWidth - 1) + (gridHeight - 1) * (gridHeight - 1));
        normalisedGridDiagonal = ROOT_TWO / gridDiagonal;
    }

    // full movement snakes use PointXY classes to hold food locations
    //
    protected final Point createNewFoodLocation(final int x, final int y)
    {
        return new PointXY(x, y);
    }

    //
    // these delegated hatchlings require PointXY segments
    //

    protected final void addNorthHatchling()
    {
        final int x = random.nextInt(gridWidth);
        final int yTail = HATCHLING_LENGTH + HATCHLING_SPAWN_MARGIN + random.nextInt(gridHeight - HATCHLING_LENGTH - HATCHLING_SPAWN_MARGIN + 1) - 1;
        for (int dy = 0; dy < HATCHLING_LENGTH; dy++) addHatchling(x, yTail - dy);
    }

    protected final void addEastHatchling()
    {
        final int y = random.nextInt(gridHeight);
        final int xTail = random.nextInt(gridWidth - HATCHLING_LENGTH - HATCHLING_SPAWN_MARGIN + 1);
        for (int dx = 0; dx < HATCHLING_LENGTH; dx++) addHatchling(xTail + dx, y);
    }

    protected final void addSouthHatchling()
    {
        final int x = random.nextInt(gridWidth);
        final int yTail = random.nextInt(gridHeight - HATCHLING_LENGTH - HATCHLING_SPAWN_MARGIN + 1);
        for (int dy = 0; dy < HATCHLING_LENGTH; dy++) addHatchling(x, yTail + dy);
    }

    protected final void addWestHatchling()
    {
        final int y = random.nextInt(gridHeight);
        final int xTail = HATCHLING_LENGTH + HATCHLING_SPAWN_MARGIN + random.nextInt(gridWidth - HATCHLING_LENGTH - HATCHLING_SPAWN_MARGIN + 1) - 1;
        for (int dx = 0; dx < HATCHLING_LENGTH; dx++) addHatchling(xTail - dx, y);
    }

    // full movement snakes use PointXY classes to hold segment coordinates
    //
    private final void addHatchling(final int x, final int y)
    {
        final Point point = new PointXY(x, y);
        hatchling.add(point);
        segments.add(point);
    }

    // calculate the following metrics from the perspective of the snake's head
    // using 45 degree increments
    //   1, the distance to the food
    //   2, the distance to the 4 grid boundaries
    //   3, the distance to the snake's body, excluding looking back into itself
    //   4, the distance to the food
    //
    //  use the above to generate 24 neural input values, normalised to [0.0, 1.0] or [-1.0, 1.0] where appropriate
    //  northerly and westerly distances are -ve, southerly and easterly distances are +ve
    //
    protected final double[] look(final Point snakeHead, final Point foodLocation)
    {
        final int hx = snakeHead.getX();
        final int hy = snakeHead.getY();

        //
        // distance and heading to food
        //

        final double foodDx = hx - foodLocation.getX();
        final double foodDy = hy - foodLocation.getY();
        final double foodDistance = Math.sqrt((foodDx * foodDx) + (foodDy * foodDy)) / gridDiagonal;
        final double foodBearing = Math.PI + Math.atan2(foodDy, foodDx);

        double foodNorth = 1.0, foodNorthEast = 1.0, foodEast = 1.0, foodSouthEast = 1.0, foodSouth = 1.0, foodSouthWest = 1.0, foodWest = 1.0, foodNorthWest = 1.0;
        if ((foodBearing >= 0) && (foodBearing < PI_BY_TWO))
        {
            foodNorth = foodDistance * Math.cos(foodBearing);
            foodEast = foodDistance * Math.sin(foodBearing);
            foodNorthEast = (foodNorth + foodEast) / ROOT_TWO;
        }
        else if ((foodBearing >= PI_BY_TWO) && (foodBearing < Math.PI))
        {
            foodEast = foodDistance * Math.cos(foodBearing - PI_BY_TWO);
            foodSouth = foodDistance * Math.sin(foodBearing - PI_BY_TWO);
            foodSouthEast = (foodEast + foodSouth) / ROOT_TWO;
        }
        else if ((foodBearing >= Math.PI) && (foodBearing < THREE_PI_BY_TWO))
        {
            foodSouth = foodDistance * Math.cos(foodBearing - Math.PI);
            foodWest = foodDistance * Math.sin(foodBearing - Math.PI);
            foodSouthWest = (foodSouth + foodWest) / ROOT_TWO;
        }
        else
        {
            foodWest = foodDistance * Math.cos(foodBearing - THREE_PI_BY_TWO);
            foodNorth = foodDistance * Math.sin(foodBearing - THREE_PI_BY_TWO);
            foodNorthWest = (foodWest + foodNorth) / ROOT_TWO;
        }

        //
        // distances to the grid boundaries
        //

        final double headToNorth = hy / gridHeight;
        final double headToEast = (gridWidth - hx - 1) / gridWidth;
        final double headToSouth = (gridHeight - hy - 1) / gridHeight;
        final double headToWest = hx / gridWidth;

        // as the area is bounded by a rectangle, the snake to wall distance calculation will depend on wether or not the snake is above
        // or below the ramp-up diagonal (south-west up to north-east), best understood with the aid of a diagram...
        //
        final double gradient = (double)gridHeight / (double)gridWidth;
        final boolean belowRampUpDiagonal = (hy + (hx * gradient)) > (gridHeight - 1);
        double headToNorthEast, headToSouthWest;
        if (belowRampUpDiagonal)
        {
            headToNorthEast = (gridWidth - hx - 1) * normalisedGridDiagonal;
            headToSouthWest = (gridHeight - hy - 1) * normalisedGridDiagonal;
        }
        else
        {
            headToNorthEast = hy * normalisedGridDiagonal;
            headToSouthWest = hx * normalisedGridDiagonal;
        }

        // for the directions below, the calculation depends on being above or below the ramp-down diagonal (north west down to south east)
        //
        final boolean aboveRampDownDiagonal = ((gridHeight - hy - 1) / gradient) + hx > (gridWidth - 1);
        double headToSouthEast, headToNorthWest;
        if (aboveRampDownDiagonal)
        {
            headToSouthEast = (gridWidth - hx - 1) * normalisedGridDiagonal;
            headToNorthWest = hy * normalisedGridDiagonal;
        }
        else
        {
            headToSouthEast = (gridHeight - hy - 1) * normalisedGridDiagonal;
            headToNorthWest = hx * normalisedGridDiagonal;
        }

        //
        // distance to this snake's own body
        //

        double bodyNorth = 1.0, bodyNorthEast = 1.0, bodyEast = 1.0, bodySouthEast = 1.0, bodySouth = 1.0, bodySouthWest = 1.0, bodyWest = 1.0, bodyNorthWest = 1.0;
        for (Point segment : segments)
        {
            final int sx = segment.getX(), sy = segment.getY();
            if (sx < hx)
            {
                final int dx = hx - sx;
                if (sy == hy)
                {
                    bodyWest = Math.min(bodyWest, dx / gridWidth);
                }
                else if (sy == (hy - dx))
                {
                    bodyNorthWest = Math.min(bodyNorthWest, dx * normalisedGridDiagonal);
                }
                else if (sy == (hy + dx))
                {
                    bodySouthWest = Math.min(bodySouthWest, dx * normalisedGridDiagonal);
                }
            }
            else if (sx > hx)
            {
                final int dx = sx - hx;
                if (sy == hy)
                {
                    bodyEast = Math.min(bodyEast, dx / gridWidth);
                }
                else if (sy == (hy - dx))
                {
                    bodyNorthEast = Math.min(bodyNorthEast, dx * normalisedGridDiagonal);
                }
                else if (sy == (hy + dx))
                {
                    bodySouthEast = Math.min(bodySouthEast, dx * normalisedGridDiagonal);
                }
            }
            else
            {
                final int dy = Math.abs(sy - hy);
                if (sy > hy)
                {
                    bodyNorth = Math.min(bodyNorth, dy / gridHeight);
                }
                else if (sy < hx)
                {
                    bodySouth = Math.min(bodySouth, dy / gridHeight);
                }
            }
        }

        return new double[] {foodNorth, foodNorthEast, foodEast, foodSouthEast, foodSouth, foodSouthWest, foodWest, foodNorthWest,
                             headToNorth, headToNorthEast, headToEast, headToSouthEast, headToSouth, headToSouthWest, headToWest, headToNorthWest,
                             bodyNorth, bodyNorthEast, bodyEast, bodySouthEast, bodySouth, bodySouthWest, bodyWest, bodyNorthWest};
    }

    protected final Movement createNewMovement(final double[] movementVector)
    {
        return new FullMovement(movementVector);
    }
}
