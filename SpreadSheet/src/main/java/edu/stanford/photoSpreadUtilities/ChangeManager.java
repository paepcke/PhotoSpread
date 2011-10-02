/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.util.HashMap;

import javax.swing.JFrame;

/**
 * @author paepcke
 *
 * Manages tracking of user-initiated modifications to
 * the system's state. Used to report when a registered
 * client, such as a sheet or a metadata editor turns
 * dirty, so that the client's window title bar can have a star
 * placed on it. 
 * 
 * In future we might add undo here.
 */
public class ChangeManager {

	private static final String DIRTY_MARKER = "* ";
	// private static final String DIRTY_MARKER = "<html><style>dirtMarker {color: red}</style><dirtMarker>*</dirtMarker> ";

	private static HashMap<String, ChangeClient> _clients = new HashMap<String, ChangeClient>();
	
	/****************************************************
	 * Constructor(s)
	 *****************************************************/
	
	/**
	 * This class is intended to be used as static only. So we
	 * disallow instantiation. (For private exceptions see below
	 * in body of registerClient())
	 * @throws IllegalAccessException
	 */
	
	public ChangeManager () throws IllegalAccessException {
		throw new IllegalAccessException("ChangeManager is only used in static mode.");
	}
	
	// Sneaky way to allow creation of an instance from 
	// within this ChangeManager class
	// only. See body of registerClient() for details
	
	private ChangeManager (String codeWord) {
		
	}
	
	/****************************************************
	 * Private (Innter) Classes
	 *****************************************************/

	class ChangeClient {
		
		private String _clientName;
		private JFrame _clientWindowFrame;
		private boolean _dirty = false;
		
		protected ChangeClient(String clientName, JFrame clientWindowFrame) {
			_clientName = clientName;
			_clientWindowFrame = clientWindowFrame;
		}	
		
		/**
		 * 
		 * @return Name of this ChangeClient instance
		 */
		protected String getName() {
			return _clientName;
		}
		
		/**
		 * Mark this client either dirty or clean.
		 * @param isDirty Boolean indicating whether this client is now dirty or clean.
		 */
		protected void isDirty(boolean isDirty) {
			_dirty = isDirty;
		}

		protected boolean isDirty() {
			return _dirty;
		}
		
		protected JFrame getWindowFrame () {
			return _clientWindowFrame;
		}
		
	} // end inner class ChangeClient
	
	/****************************************************
	 * Methods
	 *****************************************************/

	/**
	 * Clients who require change service need to register
	 * themselves via this method.
	 * 
	 * @param clientName Desired name for this client. Must be unique among 
	 * all clients who are served by this ChangeManager, but is otherwise arbitrary
	 * 
	 * @param clientWindow JFrame that contains the client
	 * @return True if client registered as a new client. False if client was
	 * already registered.
	 * @throws AlreadyBoundException Thrown if another client is already registered
	 * under the provided name.
	 */
	
	public static boolean registerClient (String clientName, JFrame clientWindow) throws AlreadyBoundException {

		if (_clients.containsKey (clientName))
			if (_clients.get(clientName).getName().equals(clientName))
				// Same client registered itself already
				return false;
			else
				throw new AlreadyBoundException ("A different change client is already registered under the name '" + clientName + "'");

		// We now want to create an instance of the inner class ChangeClient.
		// But we are only allowed to do this using an *instance* of the
		// main, outer classe ChangeManager. So we create such an instance
		// for the sole purpose of being allowed to create the client instance:
		
		ChangeManager tempCMInstance = new ChangeManager("Secret temp instance");
		_clients.put(clientName, tempCMInstance.new ChangeClient(clientName, clientWindow));

		return true;
	}
	
	/**
	 * Unregisters change client. It is legal and harmless to
	 * call this method even if the caller never registered.
	 * @param clientName Name under which calling client is registered
	 * @return True if the unregistering client was actually
	 * registered, False otherwise. 
	 */
	public static boolean unregisterClient (String clientName) {
		
		ChangeClient client = _clients.get(clientName);
		if (client == null)
			return false;
		else {
			_clients.remove(client);
			return true;
		}
	}
	
	/**
	 * Ask ChangeManager to mark the calling client as dirty.
	 * Apart from noting this fact, the ChangeManager visually
	 * marks the client's window title bar.
	 * 
	 * @param clientName Name of calling client.
	 * @return Returns true if marking was successful. Else returns false.
	 * Failure happens, for example, if the provided name is not that
	 * of a registered client.
	 */
	
	public static boolean markDirty (String clientName) {

		ChangeClient theClient; 
		
		try {
			theClient = getClientSafely(clientName);
		} catch (NotBoundException e) {return false;}

		// This client already marked dirty?
		if (theClient.isDirty()) return true;
		
		theClient.isDirty(true);
		String currWindowTitle = theClient.getWindowFrame().getTitle();

		theClient.getWindowFrame().setTitle(DIRTY_MARKER + currWindowTitle);
		return true;
	}

	/**
	 * Ask ChangeManager to mark the calling client as clean.
	 * Apart from noting this fact, the ChangeManager visually
	 * removes any dirty-mark from the client's window title.
	 * 
	 * @param clientName Name of calling client.
	 * @return Returns true if marking was successful. Else returns false.
	 * Failure happens, for example, if the provided name is not that
	 * of a registered client.
	 */

	public static boolean markClean(String clientName) {
		
		ChangeClient theClient;
		
		try {
			theClient = getClientSafely(clientName);
		} catch (NotBoundException e) {return false;}

		// This client already marked clean?
		if (!theClient.isDirty()) return true;

		theClient.isDirty(false);

		String currWindowTitle = theClient.getWindowFrame().getTitle();
		
		theClient.getWindowFrame().setTitle(currWindowTitle.substring(DIRTY_MARKER.length()));
		return true;
	}
	
	/**
	 * Find out whether client with given name is dirty.
	 * @param clientName
	 * @return True if client was marked dirty, else False
	 * @throws NotBoundException 
	 */
	public static boolean isDirty (String clientName) throws NotBoundException {
		
		ChangeClient theClient = getClientSafely(clientName);
		return theClient.isDirty();
	}

	/**
	 * Given a client name, return the client, or
	 * throw an exception if client does not exist.
	 * @param clientName
	 * @return ChangeClient object that is associated with the given name.
	 * @throws NotBoundException 
	 */
	private static ChangeClient getClientSafely (String clientName) throws NotBoundException {

		ChangeClient theClient = _clients.get(clientName);
		if (theClient == null)
			throw new NotBoundException("No client is registered under name '" + clientName + "'");
		return theClient;
	}
	
	/****************************************************
	 * Main and/or Testing Methods
	 *****************************************************/
	
	public static void main (String[] args) {
		
		JFrame frame = new JFrame("Test Title");
		
		frame.setVisible(true);
		try {
			ChangeManager.registerClient("My client", frame);
		} catch (AlreadyBoundException e) {
		}
		
		try {
		System.out.println("ChangeClient dirty?: " + ChangeManager.isDirty ("My client"));
		System.out.println("Set dirty: " + ChangeManager.markDirty ("My client"));
		System.out.println("ChangeClient dirty?: " + ChangeManager.isDirty ("My client"));
		
		System.out.println("Set clean: " + ChangeManager.markClean ("My client"));
		System.out.println("ChangeClient dirty?: " + ChangeManager.isDirty ("My client"));
		
		} catch (Exception e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			//e.printStackTrace();
		}
		
		try {
			JFrame otherFrame = new JFrame();
			ChangeManager.registerClient("My client", otherFrame);
		} catch (AlreadyBoundException e) {
			System.out.println("Good exception catch");
		}
		
		frame.dispose();
	}
}

