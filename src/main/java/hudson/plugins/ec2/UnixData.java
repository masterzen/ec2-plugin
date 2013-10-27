package hudson.plugins.ec2;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;

import org.kohsuke.stapler.DataBoundConstructor;

public class UnixData extends AMITypeData {

	private final String rootCommandPrefix;
	private final String sshPort;

	@DataBoundConstructor
	public UnixData(String rootCommandPrefix, String sshPort)
	{
		this.rootCommandPrefix = rootCommandPrefix;
		this.sshPort = sshPort;
	}
	
	public boolean isWindows() {
		return false;
	}

	public boolean isUnix() {
		return true;
	}

    @Extension
    public static class DescriptorImpl extends Descriptor<AMITypeData> 
    {
        public String getDisplayName() {
            return "unix";
        }
    }

	public String getRootCommandPrefix() {
		return rootCommandPrefix;
	}

	public String getSshPort() {
		return sshPort == null || sshPort.length() == 0 ? "22" : sshPort;
	}
}
