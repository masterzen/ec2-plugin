package hudson.plugins.ec2;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;

public abstract class AMITypeData extends AbstractDescribableImpl<AMITypeData> implements ExtensionPoint {
	public abstract boolean isWindows();
	public abstract boolean isUnix();
}
