package net.slimeslurp.tcgrowl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;


import jetbrains.buildServer.Build;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.notification.Notificator;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.UserPropertyInfo;
import jetbrains.buildServer.users.NotificatorPropertyKey;
import jetbrains.buildServer.users.PropertyKey;
import jetbrains.buildServer.users.SUser;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsRoot;

import com.binaryblizzard.growl.Growl;
import com.binaryblizzard.growl.GrowlException;
import com.binaryblizzard.growl.GrowlNotification;
import com.binaryblizzard.growl.GrowlRegistrations;


/**
 * Sends build notifications to a Growl server
 *
 * @author Nathanial Drake 
 */
public class GrowlNotifier implements Notificator {

    private static final Logger LOG = Logger.getInstance(GrowlNotifier.class.getName());
    
    /** Holds the user configuration properties */
    private ArrayList<UserPropertyInfo> userProps;
    
    private static final String TYPE = "tcGrowlNotifier";
    private static final String GROWL_SERVER_KEY = "tcgrowl.gServer";
    private static final String GROWL_PASSWORD_KEY = "tcgrowl.gPassword";
    public static final String APP_NAME = "Team City";
        
    private static final PropertyKey SERVER_KEY = new NotificatorPropertyKey(TYPE, GROWL_SERVER_KEY);
    private static final PropertyKey PASSWORD_KEY = new NotificatorPropertyKey(TYPE, GROWL_PASSWORD_KEY);
        
    /** 
     * Map to hold info each user than has added this notifier to Growl.
     */
    private Hashtable<String, Boolean> configMap;
    
    /**
     * 
     */
    public GrowlNotifier(NotificatorRegistry nr) {
        LOG.debug("Registering GrowlNotifier...");
        userProps = new ArrayList<UserPropertyInfo>();
        userProps.add(new UserPropertyInfo(GROWL_SERVER_KEY, "Growl Server IP"));
        userProps.add(new UserPropertyInfo(GROWL_PASSWORD_KEY, "Growl Server Password"));
        nr.register(this, userProps);
    }
    
    public String getDisplayName() {
        return "Growl Notifier";
    }
    
    public String getNotificatorType() {
        return TYPE;
    }
	

	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyBuildFailed(jetbrains.buildServer.serverSide.SRunningBuild, java.util.Set)
	 */
	public void notifyBuildFailed(SRunningBuild srb, Set<SUser> users) {		
		LOG.debug("notifyBuildFailed");
		doNotifications("Build " + srb.getFullName() + " failed " + 
		                srb.getBuildNumber(), users, GrowlNotification.HIGH);
	}

	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyBuildFailing(jetbrains.buildServer.serverSide.SRunningBuild, java.util.Set)
	 */
	public void notifyBuildFailing(SRunningBuild srb, Set<SUser> users) {		
	    LOG.debug("notifyBuildFailing");
	    doNotifications("Build " + srb.getFullName() + " failing " + srb.getBuildNumber(), users, GrowlNotification.HIGH);
	}

	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyBuildProbablyHanging(jetbrains.buildServer.serverSide.SRunningBuild, java.util.Set)
	 */
	public void notifyBuildProbablyHanging(SRunningBuild srb, Set<SUser> users) {	
	    LOG.debug("notifyBuildProbablyHanging");	
	    doNotifications("Build " + srb.getFullName() + " probably hanging " + srb.getBuildNumber(), users, GrowlNotification.MODERATE);
	}

	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyBuildStarted(jetbrains.buildServer.serverSide.SRunningBuild, java.util.Set)
	 */
	public void notifyBuildStarted(SRunningBuild srb, Set<SUser> users) {		
	    LOG.debug("notifyBuildStarted");
        doNotifications("Build " + srb.getFullName() + " started " + srb.getBuildNumber(), users, GrowlNotification.NORMAL);
	}

	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyBuildSuccessful(jetbrains.buildServer.serverSide.SRunningBuild, java.util.Set)
	 */
	public void notifyBuildSuccessful(SRunningBuild srb, Set<SUser> users) {		
	    LOG.debug("notifyBuildSuccessful");
	    doNotifications("Build " + srb.getFullName() + " successful " + srb.getBuildNumber(), users, GrowlNotification.NORMAL);
	}

	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyResponsibleChanged(jetbrains.buildServer.serverSide.SBuildType, java.util.Set)
	 */
	public void notifyResponsibleChanged(SBuildType sbt, Set<SUser> users) {		
	    LOG.debug("notifyResponsibleChanged");
	    doNotifications("Responsible user changed...", users, GrowlNotification.NORMAL);
	}
	
	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyLabelingFailed(jetbrains.buildServer.Build, jetbrains.buildServer.vcs.VcsRoot, java.lang.Throwable, java.util.Set)
	 */
	public void notifyLabelingFailed(jetbrains.buildServer.Build build,
	        jetbrains.buildServer.vcs.VcsRoot root,
	        java.lang.Throwable t,
	        java.util.Set<jetbrains.buildServer.users.SUser> users) {
	    LOG.debug("notifyLabelingFailed");
	    doNotifications("Labeling failed...", users, GrowlNotification.HIGH);
	}
	
	/**
	 * Send the growl notifications to the specified users.
	 * 
	 * @param message The message to send
	 * @param users The users to notify
	 * @param notificationLevel Priority of the notification
	 *
	 */
	private void doNotifications(String message, Set<SUser> users, int notificationLevel) {

	    
	    for(SUser user : users) {	        
            LOG.debug("notifying user: " + user.getUsername());
            String username = user.getUsername();
            String growlServer = user.getPropertyValue(SERVER_KEY);
            String growlPasswd = user.getPropertyValue(PASSWORD_KEY);
                        
            if(growlServer != null && growlServer.length() != 0) {
                LOG.debug("Sending message '" + message + "' to " + growlServer);
            try {

                // TODO: Cache Growl instance per user
                // Need to figure out how to tell if the user's Growl settings have changed
                // requiring the cache to be updated
                Growl g = new Growl();
                g.addGrowlHost(growlServer, growlPasswd);
                GrowlRegistrations registrations = g.getRegistrations(APP_NAME);
                registrations.registerNotification(APP_NAME, true);

                g.sendNotification(new GrowlNotification(APP_NAME, 
                                                         APP_NAME, 
                                                         message, 
                                                         APP_NAME, 
                                                         false, 
                                                         notificationLevel));                                
                } catch(GrowlException e) {
                    LOG.error("Error sending growl notification", e);
                    e.printStackTrace();
                }               
            } 
            
	    }    
	    
	}
	
}
