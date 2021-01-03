//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import bitparallel.snake.neural.NeuralNetwork;
import bitparallel.snake.neural.NetworkState;

public class SnakePit
{
    private static final Logger logger = LogManager.getLogger(SnakePit.class);

    private final SnakeFactory snakeFactory;
    private final int populationSize;
    private final PsuedoRandom random;
    private final double mutationThreshold;
    private final List<Snake> snakes, selectedMates;
    private final EvolutionLogger csvEvolutionProgressLogger;
    private int generation;
    private Snake bestSnake, animationSnake;
    private long highScore;

    public SnakePit(final SnakeFactory snakeFactory, final int populationSize, final double mutationProbability, final EvolutionLogger csvEvolutionProgressLogger)
    {
        this.snakeFactory = snakeFactory;
        this.populationSize = populationSize;
        this.csvEvolutionProgressLogger = csvEvolutionProgressLogger;

        if ((mutationProbability > 1.0) || (mutationProbability < 0)) throw new IllegalArgumentException("Mutation probability not in the range [0, 1], value: " + mutationProbability);
        mutationThreshold = 1.0 - mutationProbability;
        logger.info("Mutation threshold: " + this.mutationThreshold + " (Probability: " + mutationProbability + ")");

        generation = 0;
        random = PsuedoRandom.INSTANCE;
        snakes = new ArrayList<Snake>();
        selectedMates = new ArrayList<Snake>();
        highScore = 0;
    }

    public final int getGeneration()
    {
        return generation;
    }

    public final void spawn()
    {
        if ((generation > 0) && (selectedMates.size() == 0)) throw new IllegalArgumentException("Unable to spawn generation #" + generation + " without parents");

        snakes.clear();
        if (generation == 0)
        {
            // create the first generation of snakes with random DNA
            //
            for (int i = 0; i < populationSize; i++)
            {
                final Snake snake = snakeFactory.getSnakeInstance();
                final NeuralNetwork brain = snake.getBrain();
                brain.setRandomState();
                snakes.add(snake);
            }
        }
        else
        {
            // always add the fittest parent to the next population
            //
            snakes.add(bestSnake);
            if (snakes.size() < populationSize)
            {
                while (true)
                {
                    final Snake mate = selectedMates.get(random.nextInt(selectedMates.size()));
                    final Snake[] offspringSnakes = crossover(bestSnake, mate);

                    snakes.add(offspringSnakes[0]);
                    if (snakes.size() == populationSize) break;

                    snakes.add(offspringSnakes[1]);
                    if (snakes.size() == populationSize) break;
                }
            }
        }

        generation++;
    }

    public final void survive()
    {
        for (final Snake snake : snakes) snake.survive();

        // sort the snakes by fitness
        //
        snakes.sort((Snake s1, Snake s2) -> Long.compare(s2.getFitness(), s1.getFitness())); 

        // clone the best snake, gets propagated to the next generation and used during crossover
        //
        final Snake snake = snakes.get(0);
        final NetworkState state = snake.getBrain().getState();
        bestSnake = snakeFactory.getSnakeInstance();
        bestSnake.getBrain().setState(state);
        animationSnake = snake;

        // select the top 1% as mates
        //
        selectedMates.clear();
        final int selectionLimit = 1 + (1 * populationSize / 100);
        for (int i = 1; i < selectionLimit; i++) selectedMates.add(snakes.get(i));

        // report best snake stats
        //
        if (highScore == 0) highScore = snake.getFitness();

        final int delta = (int)Math.round(100.0 * (double)(snake.getFitness() - highScore) / (double)highScore);
        if (snake.getFitness() > highScore) highScore = snake.getFitness();

        // update the evolution progress log
        //
        final StringBuffer csv = new StringBuffer();
        csv.append(generation);
        csv.append(",");
        csv.append(animationSnake.getFitness());
        csv.append(",");
        csv.append(animationSnake.getLength());
        csv.append(",");
        csv.append(animationSnake.getMovements().size());
        csvEvolutionProgressLogger.println(csv.toString());
        csvEvolutionProgressLogger.fsync();

        // log results
        //
        final StringBuffer sb = new StringBuffer();
        sb.append("Best snake in generation #");
        sb.append(generation);
        sb.append(", length: ");
        sb.append(animationSnake.getLength());
        sb.append(", moves: ");
        sb.append(animationSnake.getMovements().size());
        sb.append(", score: ");
        sb.append(animationSnake.getFitness());
        sb.append(" [");
        if (delta > 0) sb.append("+");
        sb.append(delta);
        sb.append("%, ");
        sb.append(highScore);
        sb.append("]");
        logger.info(sb.toString());
    }

    public final Snake getAnimationSnake()
    {
        return animationSnake;
    }

    private final Snake[] crossover(final Snake mother, final Snake father)
    {
        final NetworkState stateMother = mother.getBrain().getState();
        final double[][][] weightsMother = stateMother.getWeights();
        final double[][] biasMother = stateMother.getBias();

        final NetworkState stateFather = father.getBrain().getState();
        final double[][][] weightsFather = stateFather.getWeights();
        final double[][] biasFather = stateFather.getBias();

        final int pivot = random.nextInt(stateMother.getDimension());
        int stateCount = 0;

        final double[][][] weightsDaughter = new double[weightsMother.length][][];
        final double[][] biasDaughter = new double[weightsMother.length][];
        final double[][][] weightsSon = new double[weightsMother.length][][];
        final double[][] biasSon = new double[weightsMother.length][];

        // note, relies on the weights[] and bias[] arrays have the same "network" structure
        //
        for (int layer = 0; layer < weightsMother.length; layer++)
        {
            weightsDaughter[layer] = new double[weightsMother[layer].length][];
            biasDaughter[layer] = new double[weightsMother[layer].length];
            weightsSon[layer] = new double[weightsMother[layer].length][];
            biasSon[layer] = new double[weightsMother[layer].length];

            for (int neuron = 0; neuron < weightsMother[layer].length; neuron++)
            {
                weightsDaughter[layer][neuron] = new double[weightsMother[layer][neuron].length];
                weightsSon[layer][neuron] = new double[weightsMother[layer][neuron].length];
                for (int i = 0; i < weightsMother[layer][neuron].length; i++)
                {
                    if (stateCount < pivot)
                    {
                        weightsDaughter[layer][neuron][i] = mutate(weightsMother[layer][neuron][i]);
                        weightsSon[layer][neuron][i] = mutate(weightsFather[layer][neuron][i]);
                    }
                    else
                    {
                        weightsDaughter[layer][neuron][i] = mutate(weightsFather[layer][neuron][i]);
                        weightsSon[layer][neuron][i] = mutate(weightsMother[layer][neuron][i]);
                    }

                    stateCount++;
                }

                if (stateCount < pivot)
                {
                    biasDaughter[layer][neuron] = mutate(biasMother[layer][neuron]);
                    biasSon[layer][neuron] = mutate(biasFather[layer][neuron]);
                }
                else
                {
                    biasDaughter[layer][neuron] = mutate(biasFather[layer][neuron]);
                    biasSon[layer][neuron] = mutate(biasMother[layer][neuron]);
                }

                stateCount++;
            }
        }

        final Snake daughter = snakeFactory.getSnakeInstance();
        final NeuralNetwork daughterBrain = daughter.getBrain();
        final NetworkState daughterState = new NetworkState(weightsDaughter, biasDaughter, stateMother.getDimension());
        daughterBrain.setState(daughterState);

        final Snake son = snakeFactory.getSnakeInstance();
        final NeuralNetwork sonBrain = son.getBrain();
        final NetworkState sonState = new NetworkState(weightsSon, biasSon, stateMother.getDimension());
        sonBrain.setState(sonState);

        return new Snake[] {daughter, son};
    }

    // FIXME! remove the magic gaussian divisor...
    //
    //        this is not as easy as it sounds, a gaussian range can produce large -ve or +ve numbers, but with low probability
    //        dividing by 5.0 reduces the likelyhood of this happening and when it does occur the resulting mutated weight gets clipped
    //
    //        generate the gaussian range with a specific mean and standard deviation, the java method uses [0, 1.0] and generates a value
    //        outside of [-1.0, +1.0] 16% of the time, e.g. using [0, 0.5] would yeild a value within [-1.0, 1.0] 96% of the time,
    //        or [0.0, 0.33333] for [-1.0, 1.0] 99.7% of the time, etc. etc.
    //
    //        experiment with "non uniform" mutation, here the rate starts a little higher but decays with increasing generations, this
    //        keeps the population from stagnating during the early stages of the evolution
    //
    private final double mutate(double weight)
    {
        // the threshold is typically ~95, i.e. a probability of ~0.05
        //
        if (random.nextDouble() < mutationThreshold) return weight;

        weight += (random.nextGaussian() / 5.0);
        if (weight > 1.0) return 1.0;
        if (weight < -1.0) return -1.0;

        return weight;
    }
}
