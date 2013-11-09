package hudson.plugins.ec2;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

import org.kohsuke.stapler.DataBoundConstructor;

public class WindowsData extends AMITypeData {

	private final String password;
    private final boolean useHTTPS;

	@DataBoundConstructor
	public WindowsData(String password, boolean useHTTPS)
	{
		this.password = password;
		this.useHTTPS = useHTTPS;
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

	public boolean isUseHTTPS() {
        return useHTTPS;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<AMITypeData> 
    {
        public String getDisplayName() {
            return "windows";
        }
    }

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + (useHTTPS ? 1231 : 1237);
        return result;
    }

	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof WindowsData))
            return false;
        WindowsData other = (WindowsData) obj;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (useHTTPS != other.useHTTPS)
            return false;
        return true;
    }

}
