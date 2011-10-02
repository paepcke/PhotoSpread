
package photoSpreadParser.parseTestSkeleton;

public class PhotoSpreadFunction extends PhotoSpreadFormulaComponent {

    String _funcName;

    public PhotoSpreadFunction() {
	_funcName = "";
    }

    public PhotoSpreadFunction(String funcName) {
	_funcName = funcName;
    }

    public static PhotoSpreadFunction getInstance(String funcName) {
	return new PhotoSpreadFunction(funcName);
    }

    public void addArgument(PhotoSpreadFormulaComponent fc) {
    }
}


