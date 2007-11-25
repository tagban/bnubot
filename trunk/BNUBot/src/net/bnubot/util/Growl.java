/**
 * Growl.java
 * 
 * Version:
 * $Id$
 *
 */

package net.bnubot.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * A class that encapsulates the "work" of talking to growl
 *
 * @author Karl Adam
 */
public class Growl {
	public static final String BNET_CONNECT = "Battle.net: Connected";
	public static final String BNET_DISCONNECT = "Battle.net: Disconnected";
	public static final String CHANNEL = "Channel";
	public static final String CHANNEL_USER_JOIN = "Channel: User Join";
	public static final String CHANNEL_USER_PART = "Channel: User Part";
	public static final String CHANNEL_USER_CHAT = "Channel: User Chat";
	public static final String CHANNEL_WHISPER_RECIEVED = "Channel: Whisper Recieved";
	public static final String CHANNEL_WHISPER_SENT = "Channel: Whisper Sent";
	private static final Object[] growlNotifications = new Object[] {
		BNET_CONNECT,
		BNET_DISCONNECT,
		CHANNEL,
		CHANNEL_USER_JOIN,
		CHANNEL_USER_PART,
		CHANNEL_USER_CHAT,
		CHANNEL_WHISPER_RECIEVED,
		CHANNEL_WHISPER_SENT,
		};
	private static final Object[] growlDefaults = new Object[] {
		CHANNEL_USER_CHAT,
		CHANNEL_WHISPER_RECIEVED,
		CHANNEL_WHISPER_SENT,
		};
	
	// defines
	/** The name of the growl registration notification for DNC. */
	public static final String GROWL_APP_REGISTRATION = "GrowlApplicationRegistrationNotification";

	//  Ticket Defines
	/** Ticket key for the application name. */
	public static final String GROWL_APP_NAME = "ApplicationName";
	/** Ticket key for the application icon. */
	public static final String GROWL_APP_ICON = "ApplicationIcon";
	/** Ticket key for the default notifactions. */
	public static final String GROWL_NOTIFICATIONS_DEFAULT = "DefaultNotifications";
	/** Ticket key for all notifactions. */
	public static final String GROWL_NOTIFICATIONS_ALL = "AllNotifications";

	//  Notification Defines
	/** The name of the growl notification for DNC. */
	public static final String GROWL_NOTIFICATION = "GrowlNotification";
	/** Notification key for the name. */
	public static final String GROWL_NOTIFICATION_NAME = "NotificationName";
	/** Notification key for the title. */
	public static final String GROWL_NOTIFICATION_TITLE = "NotificationTitle";
	/** Notification key for the description. */
	public static final String GROWL_NOTIFICATION_DESCRIPTION = "NotificationDescription";
	/** Notification key for the icon. */
	public static final String GROWL_NOTIFICATION_ICON = "NotificationIcon";
	/** Notification key for the application icon. */
	public static final String GROWL_NOTIFICATION_APP_ICON = "NotificationAppIcon";
	/** Notification key for the sticky flag. */
	public static final String GROWL_NOTIFICATION_STICKY = "NotificationSticky";
	/** Notification key for the identifier. */
	public static final String GROWL_NOTIFICATION_IDENTIFIER = "GrowlNotificationIdentifier";

	// Actual instance data
	private boolean registered;    // We should only register once
	private String appName;       // "Application" Name
	private Object appImageData;  // (NSData) "application" Icon
	private Object regDict;       // (NSDictionary) Registration Dictionary
	private Object allNotes;      // (NSArray) All notifications
	private Object defNotes;      // (NSArray) Default Notifications
	private Object theCenter; // (NSDistributedNotificationCenter)

	//************  Constructors **************//
	private static boolean enableGrowl = false;
	private static Class<?> NSDistributedNotificationCenter = null;
	private static Class<?> NSArray = null;
	private static Class<?> NSDictionary = null;
	private static Class<?> NSMutableDictionary = null;
	//private static Class<?> NSData = null;
	private static Class<?> NSImage = null;
	private static Method postNotification = null; // String, String, NSDictionary, boolean
	private static Method setObjectForKey = null; // Object, Object
	private static Method addEntriesFromDictionary = null; // NSDictionary
	static {
		try {
			NSDistributedNotificationCenter = Class.forName("com.apple.cocoa.foundation.NSDistributedNotificationCenter");
			NSArray = Class.forName("com.apple.cocoa.foundation.NSArray");
			NSDictionary = Class.forName("com.apple.cocoa.foundation.NSDictionary");
			NSMutableDictionary = Class.forName("com.apple.cocoa.foundation.NSMutableDictionary");
			//NSData = Class.forName("com.apple.cocoa.foundation.NSData");
			NSImage = Class.forName("com.apple.cocoa.application.NSImage");

			postNotification = NSDistributedNotificationCenter.getMethod("postNotification", String.class, String.class, NSDictionary, boolean.class);
			setObjectForKey = NSMutableDictionary.getMethod("setObjectForKey", Object.class, Object.class);
			addEntriesFromDictionary = NSMutableDictionary.getMethod("addEntriesFromDictionary", NSDictionary);
			
			Out.info(Growl.class, "Growl enabled!");
			enableGrowl = true;
		} catch(Exception e) {
			Out.exception(e);
		}
	}

	/**
	 * Convenience method to contruct a growl instance, defers to Growl(String 
	 * inAppName, NSData inImageData, NSArray inAllNotes, NSArray inDefNotes, 
	 * boolean registerNow) with empty arrays for your notifications.
	 *
	 *
	 * @param inAppName - The Name of your "application"
	 * @param inImage - The NSImage Icon for your Application
	 *
	 */
	public Growl(String inAppName, Object inImage) throws Exception {
		this(inAppName,
				NSImage.getMethod("TIFFRepresentation").invoke(inImage),
				NSArray.newInstance(),
				NSArray.newInstance(),
				false);
	}

	/**
	 * Convenience method to contruct a growl instance, defers to Growl(String 
	 * inAppName, NSData inImageData, NSArray inAllNotes, NSArray inDefNotes, 
	 * boolean registerNow) with empty arrays for your notifications.
	 *
	 * @param inAppName - The Name of your "Application"
	 * @param inImageData - The NSData for your NSImage
	 */
	/*public Growl(String inAppName, NSData inImageData) throws Exception {
		this(inAppName,
			 inImageData,
			 NSArray.newInstance(),
			 NSArray.newInstance(),
			 false);
	}*/

	/**
	 * Convenience method to contruct a growl instance, defers to Growl(String 
	 * inAppName, NSData inImageData, NSArray inAllNotes, NSArray inDefNotes, 
	 * boolean registerNow) with empty arrays for your notifications.
	 * 
	 * @param inAppName - The Name of your "Application"
	 * @param inImagePath - The path to your icon
	 */
	public Growl(String inAppName, String inImagePath) throws Exception {
		this(inAppName,
				NSImage.getConstructor(String.class, boolean.class)
				.newInstance(inImagePath, false));
	}

	/**
	 * Convenience method to contruct a growl instance, defers to Growl(String 
	 * inAppName, NSData inImageData, NSArray inAllNotes, NSArray inDefNotes, 
	 * boolean registerNow) with the arrays passed here and empty Data for the icon.
	 *
	 * @param inAppName - The Name of your "Application"
	 * @param inAllNotes - A String Array with the name of all your notifications
	 * @param inDefNotes - A String Array with the na,es of the Notifications on 
	 *                     by default
	 */
	/*public Growl(String inAppName, String [] inAllNotes, String [] inDefNotes) throws Exception {
		this(inAppName, 
				NSData.newInstance(),
				NSArray.getConstructors()[0].newInstance(inAllNotes),
				NSArray.getConstructors()[0].newInstance(inDefNotes),
				false);
	}*/

	/**
	 * Convenience method to contruct a growl instance, defers to Growl(String 
	 * inAppName, NSData inImageData, NSArray inAllNotes, NSArray inDefNotes, 
	 * boolean registerNow) with empty arrays for your notifications.
	 *
	 * @param inAppName - The Name of your "Application"
	 * @param inImageData - The Data of your "Application"'s icon
	 * @param inAllNotes - The NSArray of Strings of all your Notifications
	 * @param inDefNotes - The NSArray of Strings of your default Notifications
	 * @param registerNow - Since we have all the necessary info we can go ahead 
	 *                      and register
	 */
	public Growl(String inAppName, Object inImageData, Object inAllNotes, Object inDefNotes, boolean registerNow) throws Exception {
		if(!enableGrowl)
			throw new IllegalStateException("Growl failed to initialize");
		
		appName = inAppName;
		appImageData = inImageData;
		allNotes = inAllNotes;
		defNotes = inDefNotes;

		theCenter = NSDistributedNotificationCenter.getMethod("defaultCenter").invoke(null);
		
		if(growlNotifications == null)
			throw new IllegalStateException("growlNotifications == null");
		setAllowedNotifications(growlNotifications);
		setDefaultNotifications(growlDefaults);

		if (registerNow)
			register();
	}

	//************  Commonly Used Methods **************//

	/**
	 * Register all our notifications with Growl, this should only be called
	 * once.
	 * @return <code>true</code>.
	 */
	public boolean register() throws Exception {
		if (!registered) {

			// Construct our dictionary
			// Make the arrays of objects then keys
			Object [] objects = { appName, allNotes, defNotes, appImageData };
			Object [] keys = { GROWL_APP_NAME, 
					GROWL_NOTIFICATIONS_ALL, 
					GROWL_NOTIFICATIONS_DEFAULT,
					GROWL_APP_ICON};

			// Make the Dictionary
			regDict = NSDictionary.getConstructor(objects.getClass(), keys.getClass()).newInstance(objects, keys);

			postNotification.invoke(theCenter,
					GROWL_APP_REGISTRATION,	// notificationName
					(String)null,			// anObject
					regDict,				// userInfoDictionary
					true);					// deliverImmediately
		}

		return true;
	}

	/**
	 * The fun part is actually sending those notifications we worked so hard for
	 * so here we let growl know about things we think the user would like, and growl
	 * decides if that is the case.
	 *
	 * @param inNotificationName - The name of one of the notifications we told growl
	 *                             about.
	 * @param inIconData - The NSData for the icon for this notification, can be null
	 * @param inTitle - The Title of our Notification as Growl will show it
	 * @param inDescription - The Description of our Notification as Growl will 
	 *                        display it
	 * @param inExtraInfo - Growl is flexible and allows Display Plugins to do as they 
	 *                      please with thier own special keys and values, you may use 
	 *                      them here. These may be ignored by either the user's 
	 *                      preferences or the current Display Plugin. This can be null
	 * @param inSticky - Whether the Growl notification should be sticky
	 * @param inIdentifier - Notification identifier for coalescing. This can be null.
	 *
	 * @throws Exception When a notification is not known
	 */
	public void notifyGrowlOf(String inNotificationName, Object inIconData, 
			String inTitle, String inDescription, 
			Object inExtraInfo, boolean inSticky,
			String inIdentifier) throws Exception {
		Object noteDict = NSMutableDictionary.newInstance();

		Boolean contains = (Boolean)NSArray.getMethod("containsObject", Object.class).invoke(allNotes, inNotificationName);
		if (!contains.booleanValue())
			throw new Exception("Undefined Notification attempted");
		
		setObjectForKey.invoke(noteDict, inNotificationName, GROWL_NOTIFICATION_NAME);
		setObjectForKey.invoke(noteDict, inTitle, GROWL_NOTIFICATION_TITLE);
		setObjectForKey.invoke(noteDict, inDescription, GROWL_NOTIFICATION_DESCRIPTION);
		setObjectForKey.invoke(noteDict, appName, GROWL_APP_NAME);
		if (inIconData != null)
			setObjectForKey.invoke(noteDict, inIconData, GROWL_NOTIFICATION_ICON);
		if (inSticky)
			setObjectForKey.invoke(noteDict, new Integer(1), GROWL_NOTIFICATION_STICKY);
		if (inIdentifier != null)
			setObjectForKey.invoke(noteDict, inIdentifier, GROWL_NOTIFICATION_IDENTIFIER);
		if (inExtraInfo != null)
			addEntriesFromDictionary.invoke(noteDict, inExtraInfo);

		postNotification.invoke(theCenter,
				GROWL_NOTIFICATION,
				(String)null,
				noteDict,
				true);
	}
	
	public void notifyGrowlOf(String inNotificationName, String inTitle, String inDescription) throws Exception {
		this.notifyGrowlOf(inNotificationName, null, inTitle, inDescription, null, false, null);
	}
	

	/**
	 * Convenience method that defers to notifyGrowlOf(String inNotificationName, 
	 * NSData inIconData, String inTitle, String inDescription, 
	 * NSDictionary inExtraInfo, boolean inSticky, String inIdentifier).
	 * This is primarily for compatibility with older code
	 *
	 * @param inNotificationName - The name of one of the notifications we told growl
	 *                             about.
	 * @param inIconData - The NSData for the icon for this notification, can be null
	 * @param inTitle - The Title of our Notification as Growl will show it
	 * @param inDescription - The Description of our Notification as Growl will 
	 *                        display it
	 * @param inExtraInfo - Growl is flexible and allows Display Plugins to do as
	 *                      they please with their own special keys and values, you
	 *                      may use them here. These may be ignored by either the
	 *                      user's  preferences or the current Display Plugin. This
	 *                      can be null.
	 * @param inSticky - Whether the Growl notification should be sticky.
	 *
	 * @throws Exception When a notification is not known
	 *
	 */
	/*public void notifyGrowlOf(String inNotificationName, NSData inIconData, 
			String inTitle, String inDescription, 
			NSDictionary inExtraInfo, boolean inSticky) throws Exception {
		notifyGrowlOf(inNotificationName, inIconData, inTitle, inDescription,
				inExtraInfo, inSticky, null);
	}*/


	/**
	 * Convenience method that defers to notifyGrowlOf(String inNotificationName, 
	 * NSData inIconData, String inTitle, String inDescription, 
	 * NSDictionary inExtraInfo, boolean inSticky, String inIdentifier).
	 * This is primarily for compatibility with older code
	 *
	 * @param inNotificationName - The name of one of the notifications we told growl
	 *                             about.
	 * @param inIconData - The NSData for the icon for this notification, can be null
	 * @param inTitle - The Title of our Notification as Growl will show it
	 * @param inDescription - The Description of our Notification as Growl will 
	 *                        display it
	 * @param inExtraInfo - Growl is flexible and allows Display Plugins to do as
	 *                      they please with their own special keys and values, you
	 *                      may use them here. These may be ignored by either the
	 *                      user's  preferences or the current Display Plugin. This
	 *                      can be null.
	 *
	 * @throws Exception When a notification is not known
	 *
	 */
	/*public void notifyGrowlOf(String inNotificationName, NSData inIconData, 
			String inTitle, String inDescription, 
			NSDictionary inExtraInfo) throws Exception {

		notifyGrowlOf(inNotificationName, inIconData, inTitle, inDescription,
				inExtraInfo, false, null);
	}*/

	/**
	 * Convenienve method that defers to notifyGrowlOf(String inNotificationName, 
	 * NSData inIconData, String inTitle, String inDescription, 
	 * NSDictionary inExtraInfo, boolean inSticky, String inIdentifier) with
	 * <code>null</code> passed for icon, extraInfo and identifier arguments
	 *
	 * @param inNotificationName - The name of one of the notifications we told growl
	 *                             about.
	 * @param inTitle - The Title of our Notification as Growl will show it
	 * @param inDescription - The Description of our Notification as Growl will 
	 *                        display it
	 *
	 * @throws Exception When a notification is not known
	 */
	/*public void notifyGrowlOf(String inNotificationName, String inTitle, 
			String inDescription) throws Exception {
		notifyGrowlOf(inNotificationName, (NSData)null, 
				inTitle, inDescription, (NSDictionary)null, false, null);
	}*/

	/**
	 * Convenience method that defers to notifyGrowlOf(String inNotificationName, 
	 * NSData inIconData, String inTitle, String inDescription, 
	 * NSDictionary inExtraInfo, boolean inSticky)
	 * with <code>null</code> passed for icon and extraInfo arguments.
	 *
	 * @param inNotificationName - The name of one of the notifications we told growl
	 *                             about.
	 * @param inTitle - The Title of our Notification as Growl will show it
	 * @param inDescription - The Description of our Notification as Growl will 
	 *                        display it
	 * @param inSticky - Whether our notification should be sticky
	 *
	 * @throws Exception When a notification is not known
	 *
	 */
	/*public void notifyGrowlOf(String inNotificationName, String inTitle, 
			String inDescription, boolean inSticky) throws Exception {
		notifyGrowlOf(inNotificationName, (NSData)null, 
				inTitle, inDescription, (NSDictionary)null, inSticky, null);
	}*/

	/**
	 * Defers to notifyGrowlOf(String inNotificationName, NSData inIconData, 
	 * String inTitle, String inDescription, NSDictionary inExtraInfo,
	 * boolean inSticky, String inIdentifier) with <code>null</code> 
	 * passed for icon and extraInfo arguments.
	 *
	 * @param inNotificationName - The name of one of the notifications we told growl
	 *                             about.
	 * @param inImage - The notification image.
	 * @param inTitle - The Title of our Notification as Growl will show it
	 * @param inDescription - The Description of our Notification as Growl will 
	 *                        display it
	 * @param inExtraInfo - Look above for info
	 *
	 * @throws Exception When a notification is not known
	 *
	 */
	/*public void notifyGrowlOf(String inNotificationName, NSImage inImage, 
			String inTitle, String inDescription, 
			NSDictionary inExtraInfo) throws Exception {

		notifyGrowlOf(inNotificationName, inImage.TIFFRepresentation(),
				inTitle, inDescription, inExtraInfo, false, null);
	}*/

	/**
	 * Convenienve method that defers to notifyGrowlOf(String inNotificationName, 
	 * NSData inIconData, String inTitle, String inDescription, 
	 * NSDictionary inExtraInfo, boolean inSticky, String inIdentifier) with
	 * <code>null</code> passed for extraInfo.
	 *
	 * @param inNotificationName - The name of one of the notifications we told growl
	 *                             about.
	 * @param inImagePath - Path to the image for this notification
	 * @param inTitle - The Title of our Notification as Growl will show it
	 * @param inDescription - The Description of our Notification as Growl will 
	 *                        display it
	 *
	 * @throws Exception When a notification is not known
	 */
	/*public void notifyGrowlOf(String inNotificationName, String inImagePath,
			String inTitle, String inDescription) throws Exception {

		notifyGrowlOf(inNotificationName,
				new NSImage(inImagePath, false).TIFFRepresentation(), 
				inTitle, inDescription, (NSDictionary)null, false, null);
	}*/

	//************  Accessors **************//

	/**
	 * Accessor for The currently set "Application" Name
	 *
	 * @return String - Application Name
	 */
	public String applicationName() {
		return appName;
	}

	/**
	 * Accessor for the Array of allowed Notifications returned an NSArray
	 *
	 * @return the array of allowed notifications.
	 */
	public Object allowedNotifications() {
		return allNotes;
	}

	/**
	 * Accessor for the Array of default Notifications returned as an NSArray
	 *
	 * @return the array of default notifications.
	 */
	public Object defaultNotifications() {
		return defNotes;
	}

	//************  Mutators **************//

	/**
	 * Sets The name of the Application talking to growl
	 *
	 * @param inAppName - The Application Name
	 */
	public void setApplicationName(String inAppName) {
		appName = inAppName;
	}

	/**
	 * Set the list of allowed Notifications
	 *
	 * @param inAllNotes - The array of allowed Notifications
	 */
	public void setAllowedNotifications(Object inAllNotes) {
		allNotes = inAllNotes;
	}

	/**
	 * Set the list of allowed Notifications
	 *
	 * @param inAllNotes - The array of allowed Notifications
	 *
	 */
	public void setAllowedNotifications(Object[] inAllNotes) throws Exception {
		Constructor<?> constructor = NSArray.getConstructor(inAllNotes.getClass());
		Object[] objects = new Object[] {inAllNotes};
		allNotes = constructor.newInstance(objects);
		if(!registered)
			defNotes = allNotes;
	}

	/**
	 * Set the list of Default Notfiications
	 *
	 * @param inDefNotes - The default Notifications
	 *
	 * @throws Exception when an element of the array is not in the 
	 *                   allowedNotifications
	 */
/*	public void setDefaultNotifications(NSArray inDefNotes) throws Exception {
		int stop = inDefNotes.count();
		int i = 0;

		for(i = 0; i < stop; i++) {
			if (!allNotes.containsObject(inDefNotes.objectAtIndex(i))) {
				throw new Exception("Array Element not in Allowed Notifications");
			}
		} 

		defNotes = inDefNotes;
	}*/

	/**
	 * Set the list of Default Notfiications
	 *
	 * @param inDefNotes - The default Notifications
	 *
	 * @throws Exception when an element of the array is not in the 
	 *                   allowedNotifications
	 *
	 */
	public void setDefaultNotifications(Object [] inDefNotes) throws Exception {
		Constructor<?> constructor = NSArray.getConstructor(inDefNotes.getClass());
		Object[] objects = new Object[] {inDefNotes};
		defNotes = constructor.newInstance(objects);
	}
}
