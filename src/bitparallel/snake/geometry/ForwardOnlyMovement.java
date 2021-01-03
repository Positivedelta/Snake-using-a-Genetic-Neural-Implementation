//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.geometry;

import bitparallel.snake.PsuedoRandom;

//
// converts the three FORWARD, LEFT and RIGHT neural direction amplitudes to appropriate [-1, 0, +1] (dx, dy) offsets
//
// notes 1, LEFT / RIGHT conflicts are resolved randomly, also randomly resolved should these coincide with FORWARD
//       2, the inherited translate() method delegates to an existing PointDeltaXY instance, as used by ForwardOnlySnakes
//          this then generates appropriate absolute dx / dy deltas using the computed movement deltas and the absolute deltas
//          held within the PointDeltaXY instance, generating a new abd translated PointDeltaXY instance
//

public class ForwardOnlyMovement extends Movement
{
    public ForwardOnlyMovement(final double forward, final double left, final double right)
    {
        final PsuedoRandom random = PsuedoRandom.INSTANCE;
        final double max = Math.max(Math.max(left, right), forward);

        final boolean isLeft = (left == max);
        final boolean isRight = (right == max);
        final boolean isForward = (forward == max);

        // note, the (!isLeft && !isRight && !isForward) can't happen
        //       it will get detected as (isLeft && isRight && isForward), i.e. [0.0, 0.0, 0.0] == max
        //
        dx = dy = 0;
        if (isLeft && !isRight && !isForward)
        {
            dx = -1;
            dy = 0;
        }
        else if (!isLeft && isRight && !isForward)
        {
            dx = 1;
            dy = 0;
        }
        else if (!isLeft && !isRight && isForward)
        {
            dx = 0;
            dy = 1;
        }
        else if (!isLeft && isRight && isForward)
        {
            // randomly choose between RIGHT and FORWARD, this is unlikely to happen, so low down in the else-if chain
            //
            if (random.nextDouble() >= 0.5)
            {
                dx = 1;
                dy = 0;
            }
            else
            {
                dx = 0;
                dy = 1;
            }
        }
        else if (isLeft && !isRight && isForward)
        {
            // randomly choose between LEFT and FORWARD, this is unlikely to happen, so low down in the else-if chain
            //
            if (random.nextDouble() >= 0.5)
            {
                dx = -1;
                dy = 0;
            }
            else
            {
                dx = 0;
                dy = 1;
            }
        }
        else if (isLeft && isRight && isForward)
        {
            // randomly choose between LEFT, RIGHT and FORWARD, this is unlikely to happen, so low down in the else-if chain
            //
            switch (random.nextInt(3))
            {
                case 0:
                    dx = -1;
                    dy = 0;
                    break;

                case 1:
                    dx = 1;
                    dy = 0;
                    break;

                case 2:
                    dx = 0;
                    dy = 1;
                    break;
            }
        }
    }

    // takes a 'vision vector'
    //
    public ForwardOnlyMovement(final double[] move)
    {
        this(move[0], move[1], move[2]);
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
