//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.geometry;

import bitparallel.snake.PsuedoRandom;

//
// converts four neural direction amplitudes to [-1, 0, +1] (dx, dy) offsets
//
// notes 1. diagonal movement is not allowed
//       2. direction conflicts are resolved randomly
//       3, +dx == RIGHT, -dx == LEFT, +dy == DOWN, -dy == UP, i.e. absolute grid directions
//       4, the inherited translate() delegates to a PointXY instance (as used by FullMovementSnakes) that simply adds these absolute direction deltas

public class FullMovement extends Movement
{
    public FullMovement(final double left, final double right, final double up, final double down)
    {
        final PsuedoRandom random = PsuedoRandom.INSTANCE;
        final double max = Math.max(Math.max(Math.max(left, right), up), down);

        dx = 0;
        dy = 0;

        int dxCount = 0;
        if (left == max)
        {
            dx = -1;
            dxCount++;
        }

        if (right == max)
        {
            dx = 1;
            dxCount++;
        }

        // can't have left and right
        //
        if (dxCount == 2) dx = (random.nextDouble() >= 0.5) ? 1 : -1;

        int dyCount = 0;
        if (up == max)
        {
            dy = -1;
            dyCount++;
        }

        if (down == max)
        {
            dy = 1;
            dyCount++;
        }

        // can't have up and down
        //
        if (dyCount == 2) dy = (random.nextDouble() >= 0.5) ? 1 : -1;

        // diagonal movement is not allowed, choose an axis...
        //
        if ((dxCount > 0) && (dyCount > 0))
        {
            if (random.nextDouble() >= 0.5)
            {
                dx = 0;
            }
            else
            {
                dy = 0;
            }
        }
    }

    // takes a 'vision vector'
    //
    public FullMovement(final double[] move)
    {
        this(move[0], move[1], move[2], move[3]);
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append("(dx, dy): ");
        sb.append(dx);
        sb.append(", ");
        sb.append(dy);
        sb.append(")");

        return sb.toString();
    }
}
