/* SemgrexParser.java */
/* Generated By:JavaCC: Do not edit this line. SemgrexParser.java */
package edu.stanford.nlp.semgraph.semgrex;
// all generated classes are in this package

//imports
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.logging.Redwood;

class SemgrexParser implements SemgrexParserConstants {

  // this is so we can tell, at any point during the parse
  // whether we are under a negation, which we need to know
  // because labeling nodes under negation is illegal
  private boolean underNegation = false;
  private boolean underNodeNegation = false;
  // keep track of which variables we've already seen
  // lets us make sure we don't name new nodes under a negation
  private Set<String> knownVariables = Generics.newHashSet();

  private static final Redwood.RedwoodChannels log = Redwood.channels(SemgrexParser.class);
  private boolean deprecatedAmp = false;
  private static boolean warnedAmp = false;
  private boolean deprecatedNodeConj = false;
  private static boolean warnedNodeConj = false;

  private String fullExpression(Token startToken) {
    StringBuilder result = new StringBuilder();
    for (Token p = startToken; p != null; p = p.next) {
      if (p.specialToken != null) {
        result.append(p.specialToken.image);
      }
      result.append(p.image);
    }
    return result.toString();
  }

  private void warnDeprecated(String warning, Token startToken) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    (new RuntimeException()).printStackTrace(pw);
    String trace = sw.toString();
    trace = trace.substring(trace.lastIndexOf("SemgrexParser"));
    trace = trace.substring(trace.indexOf("at "));
    log.warn(warning + "\n    " + fullExpression(startToken) + "\n    Current stack: " + trace);
  }

  final public SemgrexPattern Root() throws ParseException {// Root pattern for the semgrex parser.  Doesn't necessarily represent matching the root.
  SemgrexPattern node;
  Token reverse = null;
  List<SemgrexPattern> children = new ArrayList<SemgrexPattern>();
  Token startToken = null;
  // a local variable

// start from 1 since we haven't parsed anything yet
    startToken = getToken(1);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case ALIGNRELN:{
      reverse = jj_consume_token(ALIGNRELN);
      node = SubNode(GraphRelation.ALIGNED_ROOT);
      jj_consume_token(9);
      break;
      }
    case 11:
    case 15:
    case 17:
    case 21:{
      node = SubNode(GraphRelation.ROOT);
children.add(node);
      label_1:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 10:{
          ;
          break;
          }
        default:
          jj_la1[0] = jj_gen;
          break label_1;
        }
        jj_consume_token(10);
        node = SubNode(GraphRelation.ITERATOR);
children.add(node);
      }
      jj_consume_token(9);
      break;
      }
    default:
      jj_la1[1] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
if (children.size() > 1)
      node = new CoordinationPattern(true, children, true, true);
    if (deprecatedAmp) {
      if (!warnedAmp) {
        warnDeprecated("Use of & in semgrex patterns is deprecated and will be removed in a future release.  It is equivalent to the same expression without the &.  Offending expression: ", startToken);
        warnedAmp = true;
      }
    }
    if (deprecatedNodeConj) {
      if (!warnedNodeConj) {
        warnDeprecated("Use of node conjugation (expressions such as '< [foo bar]' or '< [foo & bar]') is deprecated and will be removed in a future release.  The issue is that expressions such as '[foo bar] < zzz' may intuitively mean that foo < zzz, bar < zzz, zzz the same for both cases, but that is not the way the parser interprets this expression.  Changing the functionality might break existing expressions, and anyway this can be rewritten in various ways such as 'zzz > foo > bar' or 'foo < zzz=a : bar < zzz=a'.  Offending expression: ", startToken);
        warnedNodeConj = true;
      }
    }
    {if ("" != null) return node;}
    throw new Error("Missing return statement in function");
}

  final public SemgrexPattern SubNode(GraphRelation r) throws ParseException {SemgrexPattern result =  null;
        SemgrexPattern child = null;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 11:{
      jj_consume_token(11);
      result = SubNode(r);
      jj_consume_token(12);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case RELATION:
      case ALIGNRELN:
      case IDENTIFIER:
      case 15:
      case 16:
      case 17:{
        child = RelationDisj();
        break;
        }
      default:
        jj_la1[2] = jj_gen;
        ;
      }
if (child != null) {
                List<SemgrexPattern> newChildren = new ArrayList<SemgrexPattern>();
                newChildren.addAll(result.getChildren());
                newChildren.add(child);
                result.setChild(new CoordinationPattern(false, newChildren, true, false));
        }
        {if ("" != null) return result;}
      break;
      }
    case 15:
    case 17:
    case 21:{
      result = ModNode(r);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case RELATION:
      case ALIGNRELN:
      case IDENTIFIER:
      case 15:
      case 16:
      case 17:{
        child = RelationDisj();
        break;
        }
      default:
        jj_la1[3] = jj_gen;
        ;
      }
if (child != null) result.setChild(child);
                {if ("" != null) return result;}
      break;
      }
    default:
      jj_la1[4] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
}

  final public SemgrexPattern RelationDisj() throws ParseException {SemgrexPattern child;
        List<SemgrexPattern> children = new ArrayList<SemgrexPattern>();
    child = RelationConj();
children.add(child);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 13:{
        ;
        break;
        }
      default:
        jj_la1[5] = jj_gen;
        break label_2;
      }
      jj_consume_token(13);
      child = RelationConj();
children.add(child);
    }
if (children.size() == 1) {
                {if ("" != null) return child;}
          } else {
                {if ("" != null) return new CoordinationPattern(false, children, false, false);}
      }
    throw new Error("Missing return statement in function");
}

  final public SemgrexPattern RelationConj() throws ParseException {SemgrexPattern child;
        List<SemgrexPattern> children = new ArrayList<SemgrexPattern>();
    child = ModRelation();
children.add(child);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case RELATION:
      case ALIGNRELN:
      case IDENTIFIER:
      case 14:
      case 15:
      case 16:
      case 17:{
        ;
        break;
        }
      default:
        jj_la1[6] = jj_gen;
        break label_3;
      }
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 14:{
        jj_consume_token(14);
deprecatedAmp = true;
        break;
        }
      default:
        jj_la1[7] = jj_gen;
        ;
      }
      child = ModRelation();
children.add(child);
    }
if (children.size() == 1) {
                {if ("" != null) return child;}
          } else {
                {if ("" != null) return new CoordinationPattern(false, children, true, false);}
      }
    throw new Error("Missing return statement in function");
}

  final public SemgrexPattern ModRelation() throws ParseException {SemgrexPattern child;
  boolean startUnderNeg;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case RELATION:
    case ALIGNRELN:
    case IDENTIFIER:
    case 17:{
      child = RelChild();
      break;
      }
    case 15:{
      jj_consume_token(15);
startUnderNeg = underNegation;
          underNegation = true;
      child = RelChild();
underNegation = startUnderNeg;
child.negate();
      break;
      }
    case 16:{
      jj_consume_token(16);
      child = RelChild();
child.makeOptional();
      break;
      }
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
{if ("" != null) return child;}
    throw new Error("Missing return statement in function");
}

  final public SemgrexPattern RelChild() throws ParseException {SemgrexPattern child;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 17:{
      jj_consume_token(17);
      child = RelationDisj();
      jj_consume_token(18);
      break;
      }
    case RELATION:
    case ALIGNRELN:
    case IDENTIFIER:{
      child = Relation();
      break;
      }
    default:
      jj_la1[9] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
{if ("" != null) return child;}
    throw new Error("Missing return statement in function");
}

  final public SemgrexPattern Relation() throws ParseException {GraphRelation reln;
        Token rel = null;
        Token relnType = null;
        Token numArg = null;
        Token numArg2 = null;
        Token name = null;
        SemgrexPattern node;
        boolean pC = false;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case RELATION:
    case IDENTIFIER:{
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case IDENTIFIER:{
        numArg = jj_consume_token(IDENTIFIER);
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 19:{
          jj_consume_token(19);
          numArg2 = jj_consume_token(IDENTIFIER);
          break;
          }
        default:
          jj_la1[10] = jj_gen;
          ;
        }
        break;
        }
      default:
        jj_la1[11] = jj_gen;
        ;
      }
      rel = jj_consume_token(RELATION);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case IDENTIFIER:
      case REGEX:{
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case IDENTIFIER:{
          relnType = jj_consume_token(IDENTIFIER);
          break;
          }
        case REGEX:{
          relnType = jj_consume_token(REGEX);
          break;
          }
        default:
          jj_la1[12] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
        }
      default:
        jj_la1[13] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 20:{
        jj_consume_token(20);
        name = jj_consume_token(IDENTIFIER);
        break;
        }
      default:
        jj_la1[14] = jj_gen;
        ;
      }
      break;
      }
    case ALIGNRELN:{
      rel = jj_consume_token(ALIGNRELN);
      break;
      }
    default:
      jj_la1[15] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
if (numArg == null && numArg2 == null) {
            reln = GraphRelation.getRelation(rel != null ? rel.image : null,
                      relnType != null ? relnType.image : null,
                      name != null ? name.image : null);
          } else if (numArg2 == null) {
            reln = GraphRelation.getRelation(rel != null ? rel.image : null,
                      relnType != null ? relnType.image : null,
                      Integer.parseInt(numArg.image),
                      name != null ? name.image : null);
          } else {
            reln = GraphRelation.getRelation(rel != null ? rel.image : null,
                      relnType != null ? relnType.image : null,
                      Integer.parseInt(numArg.image),
                      Integer.parseInt(numArg2.image),
                      name != null ? name.image : null);
          }
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 15:
    case 17:
    case 21:{
      node = ModNode(reln);
      break;
      }
    case 11:{
      jj_consume_token(11);
      node = SubNode(reln);
      jj_consume_token(12);
      break;
      }
    default:
      jj_la1[16] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
{if ("" != null) return node;}
    throw new Error("Missing return statement in function");
}

  final public SemgrexPattern NodeDisj(GraphRelation r) throws ParseException {SemgrexPattern child;
        List<SemgrexPattern> children = new ArrayList<SemgrexPattern>();
    jj_consume_token(17);
    child = NodeConj(r);
children.add(child);
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 13:{
        ;
        break;
        }
      default:
        jj_la1[17] = jj_gen;
        break label_4;
      }
      jj_consume_token(13);
      child = NodeConj(r);
children.add(child);
    }
    jj_consume_token(18);
if (children.size() == 1)
                {if ("" != null) return child;}
          else
                {if ("" != null) return new CoordinationPattern(true, children, false, false);}
    throw new Error("Missing return statement in function");
}

  final public SemgrexPattern NodeConj(GraphRelation r) throws ParseException {SemgrexPattern child;
        List<SemgrexPattern> children = new ArrayList<SemgrexPattern>();
    child = ModNode(r);
children.add(child);
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 14:
      case 15:
      case 17:
      case 21:{
        ;
        break;
        }
      default:
        jj_la1[18] = jj_gen;
        break label_5;
      }
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 14:{
        jj_consume_token(14);
        break;
        }
      default:
        jj_la1[19] = jj_gen;
        ;
      }
      child = ModNode(r);
children.add(child); deprecatedNodeConj = true;
    }
if (children.size() == 1)
                {if ("" != null) return child;}
          else
                {if ("" != null) return new CoordinationPattern(true, children, true, false);}
    throw new Error("Missing return statement in function");
}

  final public SemgrexPattern ModNode(GraphRelation r) throws ParseException {SemgrexPattern child;
        boolean startUnderNeg;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 17:
    case 21:{
      child = Child(r);
      break;
      }
    case 15:{
      jj_consume_token(15);
startUnderNeg = underNodeNegation;
                    underNodeNegation = true;
      child = Child(r);
underNodeNegation = startUnderNeg;
      break;
      }
    default:
      jj_la1[20] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
{if ("" != null) return child;}
    throw new Error("Missing return statement in function");
}

  final public SemgrexPattern Child(GraphRelation r) throws ParseException {SemgrexPattern child;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 17:{
      child = NodeDisj(r);
      break;
      }
    case 21:{
      child = Description(r);
      break;
      }
    default:
      jj_la1[21] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
{if ("" != null) return child;}
    throw new Error("Missing return statement in function");
}

  final public void AddAttribute(NodeAttributes attributes) throws ParseException {Token attr = null;
        Token value = null;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case IDENTIFIER:{
      attr = jj_consume_token(IDENTIFIER);
      jj_consume_token(10);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case IDENTIFIER:{
        value = jj_consume_token(IDENTIFIER);
        break;
        }
      case REGEX:{
        value = jj_consume_token(REGEX);
        break;
        }
      default:
        jj_la1[22] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
if (attr != null && value != null) attributes.setAttribute(attr.image, value.image);
      break;
      }
    case ROOT:{
      attr = jj_consume_token(ROOT);
attributes.setRoot(true);
      break;
      }
    case EMPTY:{
      attr = jj_consume_token(EMPTY);
attributes.setEmpty(true);
      break;
      }
    default:
      jj_la1[23] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
}

  final public NodePattern Description(GraphRelation r) throws ParseException {Token name = null;
        boolean link = false;
        NodeAttributes attributes = new NodeAttributes();
        NodePattern pat;
    jj_consume_token(21);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case IDENTIFIER:
    case EMPTY:
    case ROOT:{
      AddAttribute(attributes);
      label_6:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 22:{
          ;
          break;
          }
        default:
          jj_la1[24] = jj_gen;
          break label_6;
        }
        jj_consume_token(22);
        AddAttribute(attributes);
      }
      break;
      }
    default:
      jj_la1[25] = jj_gen;
      ;
    }
    jj_consume_token(23);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 20:{
      jj_consume_token(20);
link = true;
      name = jj_consume_token(IDENTIFIER);
String nodeName = name.image;
              if (underNegation) {
                if (!knownVariables.contains(nodeName)) {
                  {if (true) throw new ParseException("Cannot add new variable names under negation.  Node '" + nodeName + "' not seen before");}
                }
              } else {
                knownVariables.add(nodeName);
              }
      break;
      }
    default:
      jj_la1[26] = jj_gen;
      ;
    }
pat = new NodePattern(r, underNodeNegation, attributes.attributes(), attributes.root(), attributes.empty(), name != null ? name.image : null);
           if (link) pat.makeLink();
          {if ("" != null) return pat;}
    throw new Error("Missing return statement in function");
}

  /** Generated Token Manager. */
  public SemgrexParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[27];
  static private int[] jj_la1_0;
  static {
	   jj_la1_init_0();
	}
	private static void jj_la1_init_0() {
	   jj_la1_0 = new int[] {0x400,0x228808,0x3801c,0x3801c,0x228800,0x2000,0x3c01c,0x4000,0x3801c,0x2001c,0x80000,0x10,0x110,0x110,0x100000,0x1c,0x228800,0x2000,0x22c000,0x4000,0x228000,0x220000,0x110,0xd0,0x400000,0xd0,0x100000,};
	}

  /** Constructor with InputStream. */
  public SemgrexParser(java.io.InputStream stream) {
	  this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public SemgrexParser(java.io.InputStream stream, String encoding) {
	 try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
	 token_source = new SemgrexParserTokenManager(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 27; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
	  ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
	 try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
	 token_source.ReInit(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 27; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public SemgrexParser(java.io.Reader stream) {
	 jj_input_stream = new SimpleCharStream(stream, 1, 1);
	 token_source = new SemgrexParserTokenManager(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 27; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
	if (jj_input_stream == null) {
	   jj_input_stream = new SimpleCharStream(stream, 1, 1);
	} else {
	   jj_input_stream.ReInit(stream, 1, 1);
	}
	if (token_source == null) {
 token_source = new SemgrexParserTokenManager(jj_input_stream);
	}

	 token_source.ReInit(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 27; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public SemgrexParser(SemgrexParserTokenManager tm) {
	 token_source = tm;
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 27; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(SemgrexParserTokenManager tm) {
	 token_source = tm;
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 27; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
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
  final public Token getNextToken() {
	 if (token.next != null) token = token.next;
	 else token = token.next = token_source.getNextToken();
	 jj_ntk = -1;
	 jj_gen++;
	 return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
	 Token t = token;
	 for (int i = 0; i < index; i++) {
	   if (t.next != null) t = t.next;
	   else t = t.next = token_source.getNextToken();
	 }
	 return t;
  }

  private int jj_ntk_f() {
	 if ((jj_nt=token.next) == null)
	   return (jj_ntk = (token.next=token_source.getNextToken()).kind);
	 else
	   return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
	 jj_expentries.clear();
	 boolean[] la1tokens = new boolean[24];
	 if (jj_kind >= 0) {
	   la1tokens[jj_kind] = true;
	   jj_kind = -1;
	 }
	 for (int i = 0; i < 27; i++) {
	   if (jj_la1[i] == jj_gen) {
		 for (int j = 0; j < 32; j++) {
		   if ((jj_la1_0[i] & (1<<j)) != 0) {
			 la1tokens[j] = true;
		   }
		 }
	   }
	 }
	 for (int i = 0; i < 24; i++) {
	   if (la1tokens[i]) {
		 jj_expentry = new int[1];
		 jj_expentry[0] = i;
		 jj_expentries.add(jj_expentry);
	   }
	 }
	 int[][] exptokseq = new int[jj_expentries.size()][];
	 for (int i = 0; i < jj_expentries.size(); i++) {
	   exptokseq[i] = jj_expentries.get(i);
	 }
	 return new ParseException(token, exptokseq, tokenImage);
  }

  private boolean trace_enabled;

/** Trace enabled. */
  final public boolean trace_enabled() {
	 return trace_enabled;
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}