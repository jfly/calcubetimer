package say.swing;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class SwingMessages {
	private static final String BUNDLE_NAME = "languages/say_swing"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private SwingMessages() {}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
