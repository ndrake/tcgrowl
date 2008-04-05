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
    
    private ArrayList<UserPropertyInfo> userProps;
    
    private static final String GROWL_SERVER_KEY = "tcgrowl.gServer";
    private static final String GROWL_PASSWORD_KEY = "tcgrowl.gPassword";
    
    private static final String PROP_ROOT = "plugin:notificator:tcGrowlNotifier:";
    
    private static final String APP_NAME = "Team City";
    Growl growl;
    
    /** 
     * Map to hold info each user than has added this notifier to Growl.
     */
    private Hashtable<String, Boolean> configMap;
    
    /**
     * 
     */
    public GrowlNotifier(NotificatorRegistry nr) {
        LOG.info("Registering GrowlNotifier...");
        userProps = new ArrayList<UserPropertyInfo>();
        userProps.add(new UserPropertyInfo(GROWL_SERVER_KEY, "Growl Server IP"));
        userProps.add(new UserPropertyInfo(GROWL_PASSWORD_KEY, "Growl Server Password"));
        nr.register(this, userProps);
        try {
            growl = new Growl();
        } catch(GrowlException e) {
            LOG.error("Error creating Growl instance...", e);
        }
        
    }
    
    public String getDisplayName() {
        return "Growl Notifier";
    }
    
    public String getNotificatorType() {
        return "tcGrowlNotifier";
    }
	

	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyBuildFailed(jetbrains.buildServer.serverSide.SRunningBuild, java.util.Set)
	 */
	public void notifyBuildFailed(SRunningBuild srb, Set<SUser> users) {
		// TODO Auto-generated method stub
		LOG.info("notifyBuildFailed");
		doNotifications("Build " + srb.getFullName() + " failed " + srb.getBuildNumber(), users);
	}

	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyBuildFailing(jetbrains.buildServer.serverSide.SRunningBuild, java.util.Set)
	 */
	public void notifyBuildFailing(SRunningBuild srb, Set<SUser> users) {
		// TODO Auto-generated method stub
	    LOG.info("notifyBuildFailing");
	    doNotifications("Build " + srb.getFullName() + " failing " + srb.getBuildNumber(), users);
	}

	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyBuildProbablyHanging(jetbrains.buildServer.serverSide.SRunningBuild, java.util.Set)
	 */
	public void notifyBuildProbablyHanging(SRunningBuild srb, Set<SUser> users) {
		// TODO Auto-generated method stub
	    doNotifications("Build " + srb.getFullName() + " probably hanging " + srb.getBuildNumber(), users);
	}

	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyBuildStarted(jetbrains.buildServer.serverSide.SRunningBuild, java.util.Set)
	 */
	public void notifyBuildStarted(SRunningBuild srb, Set<SUser> users) {
		// TODO Auto-generated method stub
        doNotifications("Build " + srb.getFullName() + " started " + srb.getBuildNumber(), users);
	}

	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyBuildSuccessful(jetbrains.buildServer.serverSide.SRunningBuild, java.util.Set)
	 */
	public void notifyBuildSuccessful(SRunningBuild srb, Set<SUser> users) {
		// TODO Auto-generated method stub
	    LOG.info("notifyBuildSuccessful");
	    doNotifications("Build " + srb.getFullName() + " successful " + srb.getBuildNumber(), users);
	}

	/* (non-Javadoc)
	 * @see jetbrains.buildServer.notification.Notificator#notifyResponsibleChanged(jetbrains.buildServer.serverSide.SBuildType, java.util.Set)
	 */
	public void notifyResponsibleChanged(SBuildType sbt, Set<SUser> users) {
		// TODO Auto-generated method stub
	    LOG.info("notifyResponsibleChanged");
	    doNotifications("Responsible user changed...", users);
	}
	
	public void notifyLabelingFailed(jetbrains.buildServer.Build build,
	        jetbrains.buildServer.vcs.VcsRoot root,
	        java.lang.Throwable t,
	        java.util.Set<jetbrains.buildServer.users.SUser> users) {
	    LOG.info("notifyLabelingFailed...");
	    doNotifications("Labeling failed...", users);
	}
	
	/**
	 * Send the growl notifications to the specified users.
	 * @param users The users to notify
	 */
	private void doNotifications(String message, Set<SUser> users) {
	    
	    for(SUser user : users) {
	        String[] info = getGrowlInfo(user);	        
	        if(info[0] != null) {	            	        
	            growl.addGrowlHost(info[0], info[1]);
                GrowlRegistrations registrations = growl.getRegistrations(APP_NAME);
                registrations.registerNotification(APP_NAME, true);
                try {
                    growl.sendNotification(new GrowlNotification(APP_NAME, 
                                                                APP_NAME, 
                                                                message, 
                                                                APP_NAME, 
                                                                false, 
                                                                GrowlNotification.NORMAL));                                
                } catch(GrowlException e) {
                    LOG.error("Error sending growl notification", e);
                    e.printStackTrace();
                }               
            }
            
	    }    
	    
	}
	
	/**
	 * Get the user's settings.
	 * @param user The user
	 * @return String array containing growl server and growl password
	 */
    private String[] getGrowlInfo(SUser user) {        
        String[] info = new String[2];
        //plugin:notificator:tcGrowlNotifier:gServer === localhost
        //plugin:notificator:tcGrowlNotifier:tcgrowl.gPassword === 
        
        Map<PropertyKey, String> p = user.getProperties();
        for(PropertyKey pk : p.keySet()) {
            if(pk.getKey().equals(PROP_ROOT+GROWL_SERVER_KEY)) {
                info[0] = p.get(pk);
            } else if(pk.getKey().equals(PROP_ROOT+GROWL_PASSWORD_KEY)) {
                info[1] = p.get(pk);
            }            
        }
        return info;
    }
}
