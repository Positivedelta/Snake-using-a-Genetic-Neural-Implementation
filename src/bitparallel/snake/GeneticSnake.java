//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javafx.application.Application;
import javafx.stage.Stage;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import bitparallel.snake.ui.SnakeViewer;

public class GeneticSnake extends Application
{
    private static final int AI_GRID_WIDTH = 40;
    private static final int AI_GRID_HEIGHT = 40;
    private static final int MAX_GENERATIONS = 2000;
    private static final int POPULATION_SIZE = 10000;
    private static final double MUTATION_RATE = 0.4; //0.05;

    private static final Logger logger = LogManager.getLogger(GeneticSnake.class);

    @Override
    public void start(final Stage primaryStage) throws Exception
    {
        logger.info("Genetic Snake");
        logger.info("(c) Bit Parallel Ltd, November 2020");

        // create the CSV evolution progress file
        //
        final Calendar calendar = Calendar.getInstance();
        final StringBuffer timeStamp = new StringBuffer();
        timeStamp.append(calendar.get(Calendar.YEAR));
        timeStamp.append("-");
        timeStamp.append(twoDigits(1 + calendar.get(Calendar.MONTH)));
        timeStamp.append("-");
        timeStamp.append(twoDigits(calendar.get(Calendar.DAY_OF_MONTH)));
        timeStamp.append("-");
        timeStamp.append(twoDigits(calendar.get(Calendar.HOUR_OF_DAY)));
        timeStamp.append(twoDigits(calendar.get(Calendar.MINUTE)));
        timeStamp.append(twoDigits(calendar.get(Calendar.SECOND)));
        timeStamp.append("-");

        final EvolutionLogger csvEvolutionProgressLogger = new EvolutionLogger(timeStamp.toString() + "snake-evolution-progress.csv");
        csvEvolutionProgressLogger.println("Generation,Fitness,Length,Moves");

        final SnakeViewer ui = new SnakeViewer(primaryStage, AI_GRID_WIDTH, AI_GRID_HEIGHT);
        final Runnable task = () -> {
            final SnakeFactory snakeFactory = new SnakeFactory(SnakeSpecies.FullMovement, AI_GRID_WIDTH, AI_GRID_HEIGHT);
            //final SnakeFactory snakeFactory = new SnakeFactory(SnakeSpecies.ForwardOnly, AI_GRID_WIDTH, AI_GRID_HEIGHT);
            final SnakePit snakePit = new SnakePit(snakeFactory, POPULATION_SIZE, MUTATION_RATE, csvEvolutionProgressLogger);
            while (snakePit.getGeneration() < MAX_GENERATIONS)
            {
                snakePit.spawn();
                snakePit.survive();
                ui.animate(snakePit.getAnimationSnake());
            }

            csvEvolutionProgressLogger.close();
            logger.info("Finished, all snake evolution has come to an end...");
        };

        final Thread cpu = new Thread(task);
        cpu.setDaemon(true);
        cpu.start();
    }

    @Override
    public void stop()
    {
    }

    private final String twoDigits(final int value)
    {
        final StringBuffer sb = new StringBuffer();
        if (value < 10) sb.append("0");
        sb.append(value);

        return sb.toString();
    }
}
