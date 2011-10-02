
package photoSpreadParser.parseTestSkeleton;

public class PhotoSpreadCondition extends PhotoSpreadFormulaComponent {
    
    public PhotoSpreadCondition(String rhs, ComparisonOperator compOp, String lhs) {
    }

    public PhotoSpreadCondition(String rhs, String compOp, String lhs) {
    }

    
    public class ComparisonOperator {

    }
	
   public class EqualsOperator extends ComparisonOperator{

       public EqualsOperator() {
       }
	
       public boolean satisfiesOperator(int comparison){
	
		 return(comparison == 0);
       }
	
       public boolean satisfiesOperator(String lhs, String rhs) {

	   return  new EqualsOperator().satisfiesOperator(lhs, rhs);

       }
   }

   public class GreaterThanOperator extends ComparisonOperator{

       public GreaterThanOperator() {
       }

       public boolean satisfiesOperator(int comparison){
	   return(comparison > 0);
       }

       public boolean satisfiesOperator(String lhs, String rhs) {

	   return !(new LessThanOperator().satisfiesOperator(lhs, rhs) || new EqualsOperator().satisfiesOperator(lhs, rhs));

       }

   }

   public class GreaterThanEqualsOperator extends ComparisonOperator{

       public GreaterThanEqualsOperator() {
       }
	
       public boolean satisfiesOperator(int comparison){
	
		 return(comparison >= 0);
       }
	
       public boolean satisfiesOperator(String lhs, String rhs) {

	   return !(new GreaterThanOperator().satisfiesOperator(lhs, rhs) || new EqualsOperator().satisfiesOperator(lhs, rhs));

       }
   }

   public class LessThanOperator  extends ComparisonOperator{

       public LessThanOperator() {
       }
	
       public boolean satisfiesOperator(int comparison){
	   return(comparison < 0);
       }

       public boolean satisfiesOperator(String lhs, String rhs) {

	   return new LessThanOperator().satisfiesOperator(lhs, rhs);
       }

   }

   public class LessThanEqualsOperator extends ComparisonOperator{

       public LessThanEqualsOperator() {
       }
	
       public boolean satisfiesOperator(int comparison){
	   return(comparison <= 0);
       }

       public boolean satisfiesOperator(String lhs, String rhs) {

	   return new LessThanOperator().satisfiesOperator(lhs, rhs) || new EqualsOperator().satisfiesOperator(lhs, rhs);
       }

   }
}
