package hudson.plugins.ec2.win;

import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.plugins.ec2.EC2Computer;
import hudson.plugins.ec2.EC2ComputerLauncher;
import hudson.plugins.ec2.win.winrm.WindowsProcess;
import hudson.remoting.Channel;
import hudson.remoting.Channel.Listener;
import hudson.slaves.ComputerLauncher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.model.Instance;

public class EC2WindowsLauncher extends EC2ComputerLauncher {

    final long sleepBetweenAttemps = TimeUnit.SECONDS.toMillis(10);
    
    @Override
    protected void launch(EC2Computer computer, PrintStream logger, Instance inst) throws IOException, AmazonClientException,
    InterruptedException {
        final WinConnection connection = connectToWinRM(computer, logger);

        try {
            OutputStream slaveJar = connection.putFile("C:\\Windows\\Temp\\slave.jar");
            slaveJar.write(Hudson.getInstance().getJnlpJars("slave.jar").readFully());

            logger.println("slave.jar sent remotely. Bootstrapping it");
            
            final WindowsProcess process = connection.execute("java -jar C:\\Windows\\Temp\\slave.jar", 86400);
            computer.setChannel(process.getStdout(), process.getStdin(), logger, new Listener() {
                @Override
                public void onClosed(Channel channel, IOException cause) {
                    process.destroy();
                    connection.close();
                }
            });
        } catch (Throwable ioe) {
            logger.println("Ouch:");
            ioe.printStackTrace(logger);
        } finally {
            connection.close();
        }
    }

    private WinConnection connectToWinRM(EC2Computer computer, PrintStream logger) throws AmazonClientException,
    InterruptedException {
        final long timeout = computer.getNode().getLaunchTimeoutInMillis();
        final long startTime = System.currentTimeMillis();
        while (true) {
            try {
                long waitTime = System.currentTimeMillis() - startTime;
                if (waitTime > timeout) {
                    throw new AmazonClientException("Timed out after " + (waitTime / 1000)
                            + " seconds of waiting for winrm to be connected");
                }
                Instance instance = computer.updateInstanceDescription();
                String vpc_id = instance.getVpcId();
                String ip, host;

                if (computer.getNode().usePrivateDnsName) {
                    host = instance.getPrivateDnsName();
                    ip = instance.getPrivateIpAddress(); // SmbFile doesn't
                    // quite work with
                    // hostnames
                } else {
                    /*
                     * VPC hosts don't have public DNS names, so we need to use
                     * an IP address instead
                     */
                    if (vpc_id == null || vpc_id.equals("")) {
                        host = instance.getPublicDnsName();
                        ip = instance.getPublicIpAddress(); // SmbFile doesn't
                        // quite work with
                        // hostnames
                    } else {
                        host = instance.getPrivateDnsName();
                        ip = instance.getPrivateIpAddress();
                    }
                }

                if ("0.0.0.0".equals(host)) {
                    logger.println("Invalid host 0.0.0.0, your host is most likely waiting for an ip address.");
                    throw new IOException("goto sleep");
                }

                logger.println("Connecting to " + host + "(" + ip + ") with WinRM as " + computer.getNode().remoteAdmin);

                WinConnection connection = new WinConnection(ip, computer.getNode().remoteAdmin, computer.getNode().adminPassword);
                connection.setUseHTTPS(computer.getNode().useHTTPS);
                if (!connection.ping()) {
                    logger.println("Waiting for WinRM to come up. Sleeping 10s.");
                    Thread.sleep(sleepBetweenAttemps);
                    continue;
                }

                logger.println("Connected with WinRM.");
                return connection; // successfully connected
            } catch (IOException e) {
                logger.println("Waiting for WinRM to come up. Sleeping 10s.");
                Thread.sleep(sleepBetweenAttemps);
            }
        }
    }

    @Override
    public Descriptor<ComputerLauncher> getDescriptor() {
        throw new UnsupportedOperationException();
    }
}
