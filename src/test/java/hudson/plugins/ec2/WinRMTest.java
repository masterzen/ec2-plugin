package hudson.plugins.ec2;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.CountDownLatch;

import org.bouncycastle.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.LineReader;

import hudson.plugins.ec2.win.WinConnection;
import hudson.plugins.ec2.win.winrm.WinRM;
import hudson.plugins.ec2.win.winrm.WindowsProcess;
import hudson.util.IOUtils;

public class WinRMTest
{

  @Test
  @Ignore
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
  @Ignore
  public void testWinRMcommand()
  {
    WinRM client = new WinRM("54.242.48.225", "Administrator", "InUrIrb0Slurbart");

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

  
  @Test
  public void testWinRMInOutBinary() throws IOException
  {
	  WinConnection client = new WinConnection("54.205.168.24", "Administrator", "InUrIrb0Slurbart");
//	    OutputStream out = client.putFile("C:\\Windows\\Temp\\echo.jar");
//	    IOUtils.copy(new File("/home/brice/devl/ec2-plugin/echo.jar"), out);
//	    out.flush();
//	    out.close();
//	    
//	    System.out.println("echo.jar sent");
	    
	    WindowsProcess process = client.execute("java -jar C:\\Windows\\Temp\\echo.jar");
	    try {
	    BufferedOutputStream stdin = new BufferedOutputStream(process.getStdin());
	    BufferedInputStream stdout = new BufferedInputStream(process.getStdout());
	    stdin.write(new byte[] { 0x31, 0x32, 0x33, 0x34, 0x35, 0x36 });
	    stdin.flush();
      System.out.println("wrote 6 bytes");
	    
	    byte[] input = new byte[1024];
	    int n = stdout.read(input, 0, 6);
	    System.out.println("read: " + n);
	    assert(Arrays.areEqual(input, new byte[] { 0x31, 0x32, 0x33, 0x34, 0x35, 0x36 }));
	    } finally {
	    	process.destroy();
	    }
  }

}
