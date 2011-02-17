/**
 * 
 */
package photoSpread;

/**
 * @author paepcke
 *
 * All PhotoSpread specific exceptions. To add a new one, 
 * just add a class in section "Inner Classes". You can add
 * a constructor without the 'msg' parameter for your new
 * exception if you like.
 *  
 */
public abstract class PhotoSpreadException extends Exception {

	/****************************************************
	 * Constructor(s) of Outer Class
	 *****************************************************/
	
	private static final long serialVersionUID = 1L;

	/**
	 * @param msg
	 */
	public PhotoSpreadException(String msg) {
		super(msg);
	}

	public PhotoSpreadException () {
		super();
	}

	/****************************************************
	 * Inner Classes
	 *****************************************************/
	
	@SuppressWarnings("serial")
	public static class NotImplementedException extends PhotoSpreadException {
		public NotImplementedException(String msg) {
			super(msg);
		}
	}
	
	@SuppressWarnings("serial")
	public static class IllegalPreferenceException extends PhotoSpreadException {
		public IllegalPreferenceException(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class PreferenceUnboundException extends PhotoSpreadException {
		public PreferenceUnboundException(String msg) {
			super(msg);
		}
	}

	
	@SuppressWarnings("serial")
	public static class IllegalPreferenceValueException extends PhotoSpreadException {
		public IllegalPreferenceValueException(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class IllegalArgumentException extends PhotoSpreadException {
		public IllegalArgumentException(String msg) {
			super(msg);
		}
	}
	
	@SuppressWarnings("serial")
	public static class NoMetadataSortKey extends PhotoSpreadException {
		public NoMetadataSortKey(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class BadSingletonInvocation extends PhotoSpreadException {
		public BadSingletonInvocation(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class PrematureLayoutManagerInstallation extends PhotoSpreadException {
		public PrematureLayoutManagerInstallation(String msg) {
			super(msg);
		}
	}

	
	@SuppressWarnings("serial")
	public static class FileNotFoundException extends PhotoSpreadException {
		public FileNotFoundException(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class FileIOException extends PhotoSpreadException {
		public FileIOException(String msg) {
			super(msg);
		}
	}

	public static class BadObjectInstantiationFromString extends PhotoSpreadException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public BadObjectInstantiationFromString(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class BadSheetFileContent extends PhotoSpreadException {
		public BadSheetFileContent (String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class UnsupportedDataFlavor extends PhotoSpreadException {
		public UnsupportedDataFlavor (String msg) {
			super(msg);
		}
	}
	
	@SuppressWarnings("serial")
	public static class MissingMethodImplementation extends PhotoSpreadException {
		public MissingMethodImplementation(String msg) {
			super(msg);
		}
	}
	
	@SuppressWarnings("serial")
	public static class DnDSourceOrDestNotSet extends PhotoSpreadException {
		public DnDSourceOrDestNotSet(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class KeyBindingsFileSyntaxError extends PhotoSpreadException {
		public KeyBindingsFileSyntaxError(String msg) {
			super(msg);
		}
	}
	
	@SuppressWarnings("serial")
	public static class FormulaError extends PhotoSpreadException {
		public FormulaError(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class FormulaSyntaxError extends FormulaError {
		public FormulaSyntaxError(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class FormulaArgumentsError extends FormulaError {
		public FormulaArgumentsError(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class BadUUIDStringError extends PhotoSpreadException {
		public BadUUIDStringError(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class CannotLoadImage extends PhotoSpreadException {
		public CannotLoadImage (String msg) {
			super(msg);
		}
	}
	
	/****************************************************
	 * Methods for PhotoSpreadException Subclasses
	 *****************************************************/
	
	public String getMessage() {
		return "PhotoSpread: " + super.getMessage();
	}
}	