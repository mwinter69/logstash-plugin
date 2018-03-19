package jenkins.plugins.logstash;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import hudson.Extension;
import hudson.model.InvisibleAction;

/*
 * A marker for Runs if logstash is enabled globally or set via JobProperty
 * When enabled globally or per JobProperty it doesn't make sense to send the data another time via a logstash step in a pipeline or
 * the logstashSend notifier
 */
@Extension
@Restricted(NoExternalUse.class)
public class LogstashMarkerAction extends InvisibleAction
{}