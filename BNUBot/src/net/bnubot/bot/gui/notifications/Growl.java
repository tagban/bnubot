/**
 * Growl.java
 *
 * Version:
 * $Id: Growl.java 914 2007-11-25 12:37:56Z scotta $
 *
 */

package net.bnubot.bot.gui.notifications;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.bnubot.logging.Out;

/**
 * A class that encapsulates the "work" of talking to growl
 * @author Karl Adam
 * @author scotta
 */
public class Growl {
	public static final String BNET_CONNECT = "Battle.net: Connected";
	public static final String BNET_DISCONNECT = "Battle.net: Disconnected";
	public static final String CHANNEL = "Channel";
	public static final String CHANNEL_USER_JOIN = "Channel: User Join";
	public static final String CHANNEL_USER_PART = "Channel: User Part";
	public static final String CHANNEL_USER_CHAT = "Channel: User Chat";
	public static final String CHANNEL_USER_EMOTE = "Channel: User Emote";
	public static final String CHANNEL_WHISPER_RECIEVED = "Channel: Whisper Recieved";
	public static final String CHANNEL_WHISPER_SENT = "Channel: Whisper Sent";
	public static final Object[] growlNotifications = new Object[] {
		BNET_CONNECT,
		BNET_DISCONNECT,
		CHANNEL,
		CHANNEL_USER_JOIN,
		CHANNEL_USER_PART,
		CHANNEL_USER_CHAT,
		CHANNEL_USER_EMOTE,
		CHANNEL_WHISPER_RECIEVED,
		CHANNEL_WHISPER_SENT,
		};
	private static final Object[] growlDefaults = growlNotifications;

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
	 * @param inAppName - The Name of your "Application"
	 * @param inImagePath - The path to your icon
	 * @throws Exception
	 */
	public Growl(String inAppName, String inImagePath) throws Exception {
		this(inAppName,
				NSImage.getConstructor(String.class, boolean.class)
				.newInstance(inImagePath, false));
	}

	/**
	 * Convenience method to contruct a growl instance, defers to Growl(String
	 * inAppName, NSData inImageData, NSArray inAllNotes, NSArray inDefNotes,
	 * boolean registerNow) with empty arrays for your notifications.
	 *
	 *
	 * @param inAppName - The Name of your "application"
	 * @param inImage - The NSImage Icon for your Application
	 * @throws Exception
	 */
	public Growl(String inAppName, Object inImage) throws Exception {
		this(inAppName,
				NSImage.getMethod("TIFFRepresentation").invoke(inImage),
				growlNotifications,
				growlDefaults,
				true);
	}

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
	 * @throws Exception
	 */
	public Growl(String inAppName, Object inImageData, Object[] inAllNotes, Object[] inDefNotes, boolean registerNow) throws Exception {
		if(!enableGrowl)
			throw new IllegalStateException("Growl failed to initialize");

		appName = inAppName;
		appImageData = inImageData;
		setAllowedNotifications(inAllNotes);
		setDefaultNotifications(inDefNotes);

		theCenter = NSDistributedNotificationCenter.getMethod("defaultCenter").invoke(null);

		if (registerNow)
			register();
	}

	//************  Commonly Used Methods **************//

	/**
	 * Register all our notifications with Growl, this should only be called
	 * once.
	 * @return <code>true</code>.
	 * @throws Exception
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
			setObjectForKey.invoke(noteDict, Integer.valueOf(1), GROWL_NOTIFICATION_STICKY);
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
	 * Set the list of allowed Notifications
	 *
	 * @param inAllNotes - The array of allowed Notifications
	 * @throws Exception
	 */
	public void setAllowedNotifications(Object[] inAllNotes) throws Exception {
		Constructor<?> constructor = NSArray.getConstructor(inAllNotes.getClass());
		Object[] objects = new Object[] {inAllNotes};
		allNotes = constructor.newInstance(objects);
	}

	/**
	 * Set the list of Default Notfiications
	 *
	 * @param inDefNotes - The default Notifications
	 *
	 * @throws Exception when an element of the array is not in the allowedNotifications
	 */
	public void setDefaultNotifications(Object [] inDefNotes) throws Exception {
		Constructor<?> constructor = NSArray.getConstructor(inDefNotes.getClass());
		Object[] objects = new Object[] {inDefNotes};
		defNotes = constructor.newInstance(objects);
	}
}
