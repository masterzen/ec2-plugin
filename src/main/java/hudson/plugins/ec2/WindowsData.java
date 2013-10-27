package hudson.plugins.ec2;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

import org.kohsuke.stapler.DataBoundConstructor;

public class WindowsData extends AMITypeData {

	private final String password;

	@DataBoundConstructor
	public WindowsData(String password)
	{
		this.password = password;
	}
	
	public boolean isWindows() {
		return true;
	}

	public boolean isUnix() {
		return false;
	}

	public String getPassword() {
		return password;
	}

    @Extension
    public static class DescriptorImpl extends Descriptor<AMITypeData> 
    {
        public String getDisplayName() {
            return "windows";
        }
    }
}
