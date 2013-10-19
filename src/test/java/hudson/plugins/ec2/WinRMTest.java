package hudson.plugins.ec2;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.CountDownLatch;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.LineReader;

import hudson.plugins.ec2.win.WinConnection;
import hudson.plugins.ec2.win.winrm.WinRM;
import hudson.plugins.ec2.win.winrm.WindowsProcess;

public class WinRMTest
{

  @Test
  public void testWinRMPutFile() throws Exception
  {
    WinConnection client = new WinConnection("54.224.70.22", "Administrator", "InUrIrb0Slurbart");

    OutputStream out = client.putFile("C:\\Users\\Administrator\\pouet.txt");

    OutputStreamWriter writer = new OutputStreamWriter(out);
    writer.write("this is working");
    writer.close();

    InputStream in = client.getFile("C:\\Users\\Administrator\\pouet.txt");
    LineReader reader = new LineReader(new InputStreamReader(in));
    assertEquals(reader.readLine(), "this is working");
    in.close();
  }

  @Test
  public void testWinRMcommand()
  {
    WinRM client = new WinRM("54.224.70.22", "Administrator", "InUrIrb0Slurbart");

    WindowsProcess process = client.execute("winrm get winrm/config");

    Thread stdoutReaderThread = null;
    Thread stderrReaderThread = null;
    final CountDownLatch latch = new CountDownLatch(2);
    try {
      stdoutReaderThread = getThread(process.getStdout(), latch);
      stdoutReaderThread.start();

      stderrReaderThread = getThread(process.getStderr(), latch);
      stderrReaderThread.start();

      try {
        latch.await();
        process.waitFor();
      } catch (InterruptedException exc) {
        Thread.currentThread().interrupt();

        process.destroy();

        throw new RuntimeException("Execution interrupted", exc);
      }
    } finally {
      quietlyJoinThread(stdoutReaderThread);
      quietlyJoinThread(stderrReaderThread);
    }
  }

  private void quietlyJoinThread(final Thread thread)
  {
    if (thread != null) {
      try {
        // interrupt the thread in case it is stuck waiting for output that will never come
        thread.interrupt();
        thread.join();
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private Thread getThread(final InputStream stream, final CountDownLatch latch)
  {
    return new Thread() {
      @Override
      public void run()
      {
        StringBuilder lineBuffer = new StringBuilder();
        InputStreamReader stdoutReader = new InputStreamReader(stream);
        latch.countDown();
        try {
          int cInt = stdoutReader.read();
          while (cInt > -1) {
            char c = (char) cInt;
            if (c != '\r' && c != '\n') {
              lineBuffer.append(c);
            }
            if (c == '\n') {
              System.out.println(lineBuffer.toString());
              lineBuffer.setLength(0);
            }
            cInt = stdoutReader.read();
          }
        } catch (Exception exc) {
        } finally {
          try {
            stdoutReader.close();
          } catch (IOException e) {
          }
          if (lineBuffer.length() > 0) {
            System.out.println(lineBuffer.toString());
          }
        }
      }
    };
  }

}
