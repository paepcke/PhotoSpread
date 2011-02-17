/**
 * 
 */
package photoSpreadUtilities;

import java.io.DataInput;

/**
 * @author paepcke
 *
 */
public interface IRandomAccessStreamReader extends DataInput {
	public void seek(long pos);
	public void setLength(long newLength);

}
