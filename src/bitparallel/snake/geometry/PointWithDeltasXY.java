//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.geometry;

public class PointWithDeltasXY extends Point
{
    private final int dx, dy;

    public PointWithDeltasXY(final int x, final int y, final int dx, final int dy)
    {
        super(x, y);
        this.dx = dx;
        this.dy = dy;
    }

    public final Point translate(final int dx, final int dy)
    {
        // notes 1, +dx == RIGHT, -dx == LEFT, +dy == FORWARD
        //       2, it is only possible for to be passed the following forward only deltas, (dy == 1), (dx == 1) or (dx == -1)
        //       3, this.dx and this.dy are the absolute direction deltas for this point
        //       4, rotate and calculate newDx / newDy values based on the above
        //
        // assume (dy == 1), in this case simply translates forwards in the current direction, i.e. just add on the existing point's deltas
        //
        int newDx = this.dx;
        int newDy = this.dy;
        if (dx == 1)
        {
            // turning RIGHT
            //
            newDx = -this.dy;
            newDy = this.dx;
        }
        else if (dx == -1)
        {
            // turning LEFT
            //
            newDx = this.dy;
            newDy = -this.dx;
        }

        return new PointWithDeltasXY(x + newDx, y + newDy, newDx, newDy);
    }

    // // required by List.contains() when checking for snake and food collisions
    //
    @Override
    public final boolean equals(final Object object)
    {
        return equals((PointWithDeltasXY)object);
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append("PointWithDeltasXY: [");
        sb.append(x);
        sb.append(", ");
        sb.append(y);
        sb.append("], Delta: [");
        sb.append(dx);
        sb.append(", ");
        sb.append(dy);
        sb.append("]");

        return sb.toString();
    }
}
