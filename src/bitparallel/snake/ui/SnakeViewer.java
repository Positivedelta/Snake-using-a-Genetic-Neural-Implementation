//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.ui;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import bitparallel.snake.Snake;
import bitparallel.snake.geometry.Movement;
import bitparallel.snake.geometry.Point;

public class SnakeViewer
{
    private static final Logger logger = LogManager.getLogger(SnakeViewer.class);

    private static int SNAKE_SEGMENT_WIDTH = 20;
    private static int SNAKE_SEGMENT_HEIGHT = 20;
    private static int SNAKE_SEGMENT_ARC_WIDTH = SNAKE_SEGMENT_WIDTH >> 1;
    private static int SNAKE_SEGMENT_ARC_HEIGHT = SNAKE_SEGMENT_HEIGHT >> 1;
    private static Color SNAKE_BODY_COLOUR = Color.PINK;
    private static Color SNAKE_HEAD_COLOUR = Color.RED;
    private static Color SNAKE_FOOD_COLOUR = Color.ORANGE;
    private static long ANIMATION_DELAY = 40;

    private final int width, height;
    private final Pane drawingPane;
    private Point foodPosition;

    public SnakeViewer(final Stage stage, final int aiGridWidth, final int aiGridHeight)
    {
        width = aiGridWidth * SNAKE_SEGMENT_WIDTH;
        height = aiGridHeight * SNAKE_SEGMENT_HEIGHT;
        drawingPane = new Pane();

        final StackPane stack = new StackPane();
        stack.setStyle("-fx-background-color: lightgrey;");
        stack.getChildren().add(drawingPane);

        final BorderPane pane = new BorderPane();
        pane.setCenter(stack);

        stage.setTitle("Genetic Snake");
        stage.setScene(new Scene(pane, width, height));
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();

        stage.setOnCloseRequest(closeEvent -> Platform.exit());
    }

    public final void animate(final Snake snake)
    {
        if (snake.getMovements().size() == 0)
        {
            logger.info("No generated movements for the best snake, unable to aminate, skipping...");
            return;
        }

        final LinkedList<Point> segments = snake.getHatchling();
        final Iterator<Point>	food = snake.getFoodLocations().iterator();

        // draw the food and then the initial snake
        //
        final CountDownLatch initialLatch = new CountDownLatch(1);
        foodPosition = food.next();
        Platform.runLater(() -> {
            drawingPane.getChildren().clear();
            drawSegment(foodPosition, SNAKE_FOOD_COLOUR);
            for (int i = 0; i < segments.size() - 1; i++) drawSegment(segments.get(i), SNAKE_BODY_COLOUR);
            drawSegment(segments.get(segments.size() - 1), SNAKE_HEAD_COLOUR);
            initialLatch.countDown();
        });

        try
        {
            initialLatch.await();
        }
        catch (InterruptedException ex)
        {
            logger.warn("Pre-animation latch interrupted, ignored...");
        }

        sleep(ANIMATION_DELAY);

        // animate the snake
        //
        for (Movement movement : snake.getMovements())
        {
            final CountDownLatch animateLatch = new CountDownLatch(1);
            final ObservableList<Node> segmentNodes = drawingPane.getChildren();
            Platform.runLater(() -> {
                // convert the existing head to a body segment
                //
                final Rectangle segmentNode = (Rectangle)segmentNodes.get(segmentNodes.size() - 1);
                segmentNode.setFill(SNAKE_BODY_COLOUR);

                // add the new head segment
                //
                final Point head = segments.getLast();
                final Point newHead = movement.translate(head);
                drawSegment(newHead, SNAKE_HEAD_COLOUR);
                segments.addLast(newHead);

                // did the snake "eat" the food? if so then update it's location to the next position
                //
                final boolean foundFood = foodPosition.equals(newHead);
                if (foundFood)
                {
                    if (food.hasNext())
                    {
                        foodPosition = food.next();
                        final Rectangle foodNode = (Rectangle)segmentNodes.get(0);
                        foodNode.setX(foodPosition.getX() * SNAKE_SEGMENT_WIDTH);
                        foodNode.setY(foodPosition.getY() * SNAKE_SEGMENT_HEIGHT);
                    }
                }
                else
                {
                    // remove the tail, but only if the food wasn't found, i.e. allow the snake to grow after it's eaten
                    //
                    drawingPane.getChildren().remove(1, 2);
                    segments.removeFirst();
                }

                animateLatch.countDown();
            });

            try
            {
                animateLatch.await();
            }
            catch (InterruptedException ex)
            {
                logger.warn("Animation latch interrupted, ignored...");
            }

            sleep(ANIMATION_DELAY);
        }
    }

    private final void drawSegment(final Point point, final Color paint)
    {
        final double x = point.getX() * SNAKE_SEGMENT_WIDTH;
        final double y = point.getY() * SNAKE_SEGMENT_HEIGHT;
        final Rectangle segment = new Rectangle(x, y, SNAKE_SEGMENT_WIDTH, SNAKE_SEGMENT_HEIGHT);
        segment.setArcWidth(SNAKE_SEGMENT_ARC_WIDTH);
        segment.setArcHeight(SNAKE_SEGMENT_ARC_HEIGHT);
        segment.setStroke(Color.BLACK);
        segment.setFill(paint);
        drawingPane.getChildren().add(segment);
    }

    private final void sleep(final long time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch (InterruptedException ex)
        {
            logger.warn("Animation frame delay interrupted, ignored...");
        }
    }
}
