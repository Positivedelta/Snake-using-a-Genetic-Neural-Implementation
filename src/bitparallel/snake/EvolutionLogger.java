//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake;

import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.SyncFailedException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class EvolutionLogger
{
    private static final Logger logger = LogManager.getLogger(EvolutionLogger.class);

    private final String fileName;
    private final FileOutputStream fos;
    private final PrintWriter writer;

    public EvolutionLogger(final String fileName) throws IOException
    {
        this.fileName = fileName;

        fos = new FileOutputStream(fileName);
        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(fos), StandardCharsets.UTF_8.toString()));
    }

    public void println(final String logLine)
    {
        writer.println(logLine);
    }

    // mimic the linux system call to implement a hard flush
    //
    public void fsync()
    {
        writer.flush();

        try
        {
            fos.getFD().sync();              // actual data
            fos.getChannel().force(true);    // associated file meta data, e.g. size
        }
        catch (SyncFailedException ex)
        {
            logger.error("Failed to sync() " + fileName);
        }
        catch (ClosedChannelException ex)
        {
            logger.error("File channel for " + fileName + " is unexpectedly closed, can't force a meta data update");
        }
        catch (IOException ex)
        {
            logger.error("Unable to fsync() " + fileName, ex);
        }
    }

    public void close()
    {
        writer.close();
    }
}
