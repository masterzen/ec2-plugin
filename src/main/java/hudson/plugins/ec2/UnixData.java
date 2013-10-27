package hudson.plugins.ec2;

import hudson.Extension;
import hudson.model.Describable;
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
    public static final Descriptor<AMITypeData> DESCRIPTOR = new Descriptor<AMITypeData>() {
        public String getDisplayName() {
            return "unix";
        }
    };

	public Descriptor<AMITypeData> getDescriptor() {
        return DESCRIPTOR;
	}

	public String getRootCommandPrefix() {
		return rootCommandPrefix;
	}

	public String getSshPort() {
		return sshPort;
	}
}
