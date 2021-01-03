//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake;

import bitparallel.snake.geometry.ForwardOnlyMovement;
import bitparallel.snake.geometry.Movement;
import bitparallel.snake.geometry.Point;
import bitparallel.snake.geometry.PointWithDeltasXY;
import bitparallel.snake.neural.NeuralNetwork;

public class ForwardOnlySnake extends Snake
{
    private final double gridDiagonal, normalisedGridDiagonal;

    public ForwardOnlySnake(final NeuralNetwork brain, final int gridWidth, final int gridHeight)
    {
        super(brain, gridWidth, gridHeight);

        gridDiagonal = Math.sqrt((gridWidth - 1) * (gridWidth - 1) + (gridHeight - 1) * (gridHeight - 1));
        normalisedGridDiagonal = ROOT_TWO / gridDiagonal;
    }

    // forward only sankes use PointWithDeltasXY classes to hold food locations
    // note, food locations don't use direction deltas, these are ignored during List.contains() operations
    //
    protected final Point createNewFoodLocation(final int x, final int y)
    {
        return new PointWithDeltasXY(x, y, 0, 0);
    }

    //
    // these delegated hatchlings require PointWithDeltasXY segments
    //

    protected final void addNorthHatchling()
    {
        final int x = random.nextInt(gridWidth);
        final int yTail = HATCHLING_LENGTH + HATCHLING_SPAWN_MARGIN + random.nextInt(gridHeight - HATCHLING_LENGTH - HATCHLING_SPAWN_MARGIN + 1) - 1;
        for (int dy = 0; dy < HATCHLING_LENGTH; dy++) addHatchling(x, yTail - dy, 0, -1);
    }

    protected final void addEastHatchling()
    {
        final int y = random.nextInt(gridHeight);
        final int xTail = random.nextInt(gridWidth - HATCHLING_LENGTH - HATCHLING_SPAWN_MARGIN + 1);
        for (int dx = 0; dx < HATCHLING_LENGTH; dx++) addHatchling(xTail + dx, y, 1, 0);
    }

    protected final void addSouthHatchling()
    {
        final int x = random.nextInt(gridWidth);
        final int yTail = random.nextInt(gridHeight - HATCHLING_LENGTH - HATCHLING_SPAWN_MARGIN + 1);
        for (int dy = 0; dy < HATCHLING_LENGTH; dy++) addHatchling(x, yTail + dy, 0, 1);
    }

    protected final void addWestHatchling()
    {
        final int y = random.nextInt(gridHeight);
        final int xTail = HATCHLING_LENGTH + HATCHLING_SPAWN_MARGIN + random.nextInt(gridWidth - HATCHLING_LENGTH - HATCHLING_SPAWN_MARGIN + 1) - 1;
        for (int dx = 0; dx < HATCHLING_LENGTH; dx++) addHatchling(xTail - dx, y, -1, 0);
    }

    // forward only sankes use PointWithDeltasXY classes to hold segment coordinates
    //
    private final void addHatchling(final int x, final int y, final int dx, final int dy)
    {
        final Point point = new PointWithDeltasXY(x, y, dx, dy);
        hatchling.add(point);
        segments.add(point);
    }

    // look and see if it clear to move...
    //   1. straight ahead
    //   2. to the left
    //   3. to the right
    //
    // look for the food...
    //   4. straight ahead
    //   5. to the left
    //   6. to the right
    // 
    protected final double[] look(final Point snakeHead, final Point foodLocation)
    {
        final int hx = snakeHead.getX();
        final int hy = snakeHead.getY();

        // extract the snake head deltas, these are absolute direction increments
        //
        final Point deltas = snakeHead.translate(0, 0);
        final int dx = deltas.getX() - hx;
        final int dy = deltas.getY() - hy;

        // as the snake can only ever move "forwards", only the following delta combinations are possible
        //
        //  dx  dy
        //  0   1   south
        //  0  -1   north
        //  1   0   east
        // -1   0   west
        //
        double forwardDistance = 0.0, leftDistance = 0.0, rightDistance = 0.0;
        double forwardFood = 0.0, leftFood = 0.0, rightFood = 0.0;
        if ((dx == 0) && (dy == 1))
        {
            // moving south, distance to grid boundaries
            //
            forwardDistance = gridHeight - hy - 1.0;
            leftDistance = gridWidth - hx - 1.0;
            rightDistance = hx;

            // distance to body, don't include the head (snakes are defined tail first)
            //
            for (int i = 0; i < segments.size() - 1; i++)
            {
                final int sx = segments.get(i).getX();
                final int sy = segments.get(i).getY();

                if ((hx == sx) && (sy > hy)) forwardDistance = Math.min(forwardDistance, sy - hy);
                if (hy == sy)
                {
                    if (sx == hx)
                    {
                        // no point continuing!
                        //
                        leftDistance = rightDistance = 0.0;
                        break;
                    }
                    else if (sx > hx)
                    {
                        leftDistance = Math.min(leftDistance, sx - hx);
                    }
                    else
                    {
                        rightDistance = Math.min(rightDistance, hx - sx);
                    } 
                }
            }

            // distance to food
            //
            forwardFood = Math.max(0.0, foodLocation.getY() - hy);
            leftFood = Math.max(0.0, foodLocation.getX() - hx);
            rightFood = Math.max(0.0, hx - foodLocation.getX());

            // normalise the south facing distances
            //
            forwardDistance = forwardDistance / (double)gridHeight;
            leftDistance = leftDistance / (double)gridWidth;
            rightDistance = rightDistance / (double)gridWidth;

            forwardFood = forwardFood / (double)gridHeight;
            leftFood = leftFood / (double)gridWidth;
            rightFood = rightFood / (double)gridWidth;
        }
        else if ((dx == 0) && (dy == -1))
        {
            // moving north, distance to grid boundaries
            //
            forwardDistance = hy;
            leftDistance = hx;
            rightDistance = gridWidth - hx - 1.0;

            // distance to body
            //
            for (int i = 0; i < segments.size() - 1; i++)
            {
                final int sx = segments.get(i).getX();
                final int sy = segments.get(i).getY();

                if ((hx == sx) && (hy > sy)) forwardDistance = Math.min(forwardDistance, hy - sy);
                if (hy == sy)
                {
                    if (hx == sx)
                    {
                        leftDistance = rightDistance = 0.0;
                        break;
                    }
                    else if (hx > sx)
                    {
                        leftDistance = Math.min(leftDistance, hx - sx);
                    }
                    else
                    {
                        rightDistance = Math.min(rightDistance, sx - hx);
                    }
                }
            }

            // distance to food
            //
            forwardFood = Math.max(0.0, hy - foodLocation.getY());
            leftFood = Math.max(0.0, hx - foodLocation.getX());
            rightFood = Math.max(0.0, foodLocation.getX() - hx);

            // normalise the north facing distances
            //
            forwardDistance = forwardDistance / (double)gridHeight;
            leftDistance = leftDistance / (double)gridWidth;
            rightDistance = rightDistance / (double)gridWidth;

            forwardFood = forwardFood / (double)gridHeight;
            leftFood = leftFood / (double)gridWidth;
            rightFood = rightFood / (double)gridWidth;
        }
        else if ((dx == 1) && (dy == 0))
        {
            // moving east, distance to grid boundaries
            //
            forwardDistance = gridWidth - hx - 1.0;
            leftDistance = hy;
            rightDistance = gridHeight - hy - 1.0;

            // distance to body
            //
            for (int i = 0; i < segments.size() - 1; i++)
            {
                final int sx = segments.get(i).getX();
                final int sy = segments.get(i).getY();

                if ((hy == sy) && (sx > hx)) forwardDistance = Math.min(forwardDistance, sx - hx);
                if (hx == sx)
                {
                    if (hy == sy)
                    {
                        leftDistance = rightDistance = 0.0;
                        break;
                    }
                    else if (hy > sy)
                    {
                        leftDistance = Math.min(leftDistance, hy - sy);
                    }
                    else
                    {
                        rightDistance = Math.min(rightDistance, sy - hy);
                    }
                }
            }

            // distance to food
            //
            forwardFood = Math.max(0.0, foodLocation.getX() - hx);
            leftFood = Math.max(0.0, hy - foodLocation.getY());
            rightFood = Math.max(0.0, foodLocation.getY() - hy);

            // normalise the east facing distances
            //
            forwardDistance = forwardDistance / (double)gridWidth;
            leftDistance = leftDistance / (double)gridHeight;
            rightDistance = rightDistance / (double)gridHeight;

            forwardFood = forwardFood / (double)gridWidth;
            leftFood = leftFood / (double)gridHeight;
            rightFood = rightFood / (double)gridHeight;
        }
        else if ((dx == -1) && (dy == 0))
        {
            // moving west, distance to grid boundaries
            //
            forwardDistance = hx;
            leftDistance = gridHeight - hy - 1.0;
            rightDistance = hy;

            // distance to body
            //
            for (int i = 0; i < segments.size() - 1; i++)
            {
                final int sx = segments.get(i).getX();
                final int sy = segments.get(i).getY();

                if ((hy == sy) && (hx > sx)) forwardDistance = Math.min(forwardDistance, hx - sx);
                if (hx == sx)
                {
                    if (hy == sy)
                    {
                        leftDistance = rightDistance = 0.0;
                        break;
                    }
                    else if (sy > hy)
                    {
                        leftDistance = Math.min(leftDistance, sy - hy);
                    }
                    else
                    {
                        rightDistance = Math.min(rightDistance, hy - sy);
                    }
                }
            }

            // distance to food
            //
            forwardFood = Math.max(0.0, hx - foodLocation.getX());
            leftFood = Math.max(0.0, foodLocation.getY() - hy);
            rightFood = Math.max(0.0, hy - foodLocation.getY());

            // normalise the west facing distances
            //
            forwardDistance = forwardDistance / (double)gridWidth;
            leftDistance = leftDistance / (double)gridHeight;
            rightDistance = rightDistance / (double)gridHeight;

            forwardFood = forwardFood / (double)gridWidth;
            leftFood = leftFood / (double)gridHeight;
            rightFood = rightFood / (double)gridHeight;
        }

        return new double[] {forwardDistance, leftDistance, rightDistance, forwardFood, leftFood, rightFood};
    }

    protected final Movement createNewMovement(final double[] movementVector)
    {
        return new ForwardOnlyMovement(movementVector);
    }
}
