/* Generated By:JavaCC: Do not edit this line. ExpressionParser.java */
package photoSpreadParser;



import photoSpreadParser.photoSpreadExpression.*;

import java.util.ArrayList;

public class ExpressionParser implements ExpressionParserConstants {







  public static void main(String args[]) throws ParseException {

    java.io.StringReader sr = new java.io.StringReader( "=union(A1)" );

    java.io.Reader r = new java.io.BufferedReader( sr );

    ExpressionParser parser = new ExpressionParser(r);

    parser.Expression();

  }

  static final public PhotoSpreadExpression Expression() throws ParseException {
  PhotoSpreadExpression e;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case EQ:
      e = FormulaExpression();
    System.out.println("expression");

    System.out.println("This function is named " + e.getExpression());

    {if (true) return e;}
      break;
    case UNION:
      e = ConstantExpression();
    System.out.println("expression");

    System.out.println("This function is named " + e.getExpression());

    {if (true) return e;}
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  static final public PhotoSpreadFormulaExpression FormulaExpression() throws ParseException {
 PhotoSpreadFunction function;
    jj_consume_token(EQ);
    function = Function();
        System.out.println("Functtion");

        {if (true) return new PhotoSpreadFormulaExpression("hello", function);}
    throw new Error("Missing return statement in function");
  }

  static final public PhotoSpreadConstantExpression ConstantExpression() throws ParseException {
 Token ce;
    ce = jj_consume_token(UNION);
        System.out.println("Constant expression " + ce.image);

        {if (true) return new PhotoSpreadConstantExpression(ce.image);}
    throw new Error("Missing return statement in function");
  }

  static final public PhotoSpreadFunction Function() throws ParseException {
 PhotoSpreadFunction func; String functionName; ArrayList arguments; PhotoSpreadFormulaComponent fc;
    functionName = FunctionName();
        func = PhotoSpreadFunction.getInstance(functionName.toLowerCase());
    jj_consume_token(LPARENS);
    fc = FormulaComponent();
        func.addArgument(fc);
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 27:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_1;
      }
      jj_consume_token(27);
      fc = FormulaComponent();
        func.addArgument(fc);
    }
    jj_consume_token(RPARENS);
        {if (true) return func;}
    throw new Error("Missing return statement in function");
  }

  static final public PhotoSpreadFormulaComponent FormulaComponent() throws ParseException {
 PhotoSpreadFormulaComponent fc;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case UNION:
      fc = Function();
     {if (true) return fc;}
      break;
    case CELLRANGE:
      fc = CellRange();
     {if (true) return fc;}
      break;
    case LOWER_CHARS:
      fc = Condition();
     {if (true) return fc;}
      break;
    default:
      jj_la1[2] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  static final public PhotoSpreadCellRange OldCellRange() throws ParseException {
 Token startCol; Token startRow; Token endCol; Token endRow;PhotoSpreadCellRange cr;
    startCol = jj_consume_token(CELLCOLUMN);
    startRow = jj_consume_token(DIGITS);
        {if (true) return new PhotoSpreadCellRange(startCol.image, Integer.parseInt(startRow.image));}
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 28:
      jj_consume_token(28);
      endCol = jj_consume_token(CELLCOLUMN);

      endRow = jj_consume_token(DIGITS);

        {if (true) return new PhotoSpreadCellRange(startCol.image, Integer.parseInt(startRow.image), endCol.image, Integer.parseInt(endRow.image));}
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
       {if (true) return new PhotoSpreadCellRange(startCol.image, Integer.parseInt(startRow.image));}
    throw new Error("Missing return statement in function");
  }

  static final public PhotoSpreadCellRange CellRange() throws ParseException {
 Token cellRange; Token startCol; Token startRow; Token endCol; Token endRow;PhotoSpreadCellRange cr;
    cellRange = jj_consume_token(CELLRANGE);
        {if (true) return new PhotoSpreadCellRange(cellRange.image);}
    throw new Error("Missing return statement in function");
  }

  static final public PhotoSpreadCondition Condition() throws ParseException {
 Token rhsToken; Token lhsToken; Token compOpToken; String rhs; String compOp; String lhs;
    rhsToken = jj_consume_token(LOWER_CHARS);
        rhs = rhsToken.image;
    compOpToken = jj_consume_token(COMP_OP);
        compOp = compOpToken.image;
    lhsToken = jj_consume_token(LOWER_CHARS);
        lhs = lhsToken.image;
        {if (true) return new PhotoSpreadCondition(rhs,  compOp, lhs);}
    throw new Error("Missing return statement in function");
  }

  static final public PhotoSpreadCondition FullCondition() throws ParseException {
 Token rhsToken; Token lhsToken; Token compOpToken; String rhs; String compOp; String lhs;
    rhsToken = jj_consume_token(CHARS);
        rhs = rhsToken.image;
    compOpToken = jj_consume_token(COMP_OP);
        compOp = compOpToken.image;
    lhsToken = jj_consume_token(CHARS);
        lhs = lhsToken.image;
        {if (true) return new PhotoSpreadCondition(rhs,  compOp, lhs);}
    throw new Error("Missing return statement in function");
  }

  static final public String FunctionName() throws ParseException {
    jj_consume_token(UNION);
        {if (true) return "Union";}
    throw new Error("Missing return statement in function");
  }

  static final public int MatchedBraces() throws ParseException {
  int nested_count=0;
    jj_consume_token(LBRACE);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LBRACE:
      nested_count = MatchedBraces();
      break;
    default:
      jj_la1[4] = jj_gen;
      ;
    }
    jj_consume_token(RBRACE);
    {if (true) return ++nested_count;}
    throw new Error("Missing return statement in function");
  }

  static private boolean jj_initialized_once = false;
  /** Generated Token Manager. */
  static public ExpressionParserTokenManager token_source;
  static SimpleCharStream jj_input_stream;
  /** Current token. */
  static public Token token;
  /** Next token. */
  static public Token jj_nt;
  static private int jj_ntk;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[5];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x8200,0x8000000,0x1208000,0x10000000,0x20,};
   }

  /** Constructor with InputStream. */
  public ExpressionParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public ExpressionParser(java.io.InputStream stream, String encoding) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ExpressionParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  static public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  static public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public ExpressionParser(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser. ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ExpressionParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public ExpressionParser(ExpressionParserTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser. ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(ExpressionParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  static private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  static final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.List jj_expentries = new java.util.ArrayList();
  static private int[] jj_expentry;
  static private int jj_kind = -1;

  /** Generate ParseException. */
  static public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[29];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 5; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 29; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  static final public void enable_tracing() {
  }

  /** Disable tracing. */
  static final public void disable_tracing() {
  }

}