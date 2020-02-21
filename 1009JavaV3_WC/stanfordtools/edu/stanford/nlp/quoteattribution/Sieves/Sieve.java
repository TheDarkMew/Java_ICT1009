package edu.stanford.nlp.quoteattribution.Sieves;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.QuoteAttributionAnnotator;
import edu.stanford.nlp.quoteattribution.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

import java.util.*;

/**
 * Created by mjfang on 7/8/16.
 */
public class Sieve {

  protected Annotation doc;
  protected Map<String, List<Person>> characterMap;
  protected Map<Integer, String> pronounCorefMap;
  protected Set<String> animacySet;


  //mention types
  public static final String PRONOUN = "pronoun";
  public static final String NAME = "name";
  public static final String ANIMATE_NOUN = "animate noun";


  protected TokenNode rootNameNode;

  public Sieve(Annotation doc,
               Map<String, List<Person>> characterMap,
               Map<Integer, String> pronounCorefMap,
               Set<String> animacySet) {
    this.doc = doc;
    this.characterMap = characterMap;
    this.pronounCorefMap = pronounCorefMap;
    this.animacySet = animacySet;
    this.rootNameNode = createNameMatcher();
  }


  //resolves ambiguities if necessary (note: currently not actually being done)
  protected Person resolveAmbiguities(String name) {
    if(characterMap.get(name)==null)
      return null;
    if(characterMap.get(name).size() == 1)
      return characterMap.get(name).get(0);
    else
    {
      return null;
    }
  }

  protected Set<Person> getNamesInParagraph(CoreMap quote) {
    //iterate forwards and backwards to look for quotes in the same paragraph, and add all the names present in them to the list.
    List<CoreMap> quotes = doc.get(CoreAnnotations.QuotationsAnnotation.class);
    List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
    List<String> quoteNames = new ArrayList<>();
    int quoteParagraph = QuoteAttributionUtils.getQuoteParagraphIndex(doc, quote);
    int quoteIndex = quote.get(CoreAnnotations.QuotationIndexAnnotation.class);
    for(int i = quoteIndex; i >= 0; i--) {
      CoreMap currQuote = quotes.get(i);
      int currQuoteParagraph = QuoteAttributionUtils.getQuoteParagraphIndex(doc, currQuote);
      if(currQuoteParagraph == quoteParagraph) {
        quoteNames.addAll(scanForNames(new Pair<>(currQuote.get(CoreAnnotations.TokenBeginAnnotation.class), currQuote.get(CoreAnnotations.TokenEndAnnotation.class))).first);
      }
      else {
        break;
      }
    }
    for(int i = quoteIndex + 1; i < quotes.size(); i++) {
      CoreMap currQuote = quotes.get(i);
      int currQuoteParagraph = QuoteAttributionUtils.getQuoteParagraphIndex(doc, currQuote);
      if(currQuoteParagraph == quoteParagraph) {
        quoteNames.addAll(scanForNames(new Pair<>(currQuote.get(CoreAnnotations.TokenBeginAnnotation.class), currQuote.get(CoreAnnotations.TokenEndAnnotation.class))).first);
      }
      else {
        break;
      }
    }

    Set<Person> namesInParagraph = new HashSet<>();
    for(String name : quoteNames) {
      for(Person p : characterMap.get(name)) {
        namesInParagraph.add(p);
      }
    }

    return namesInParagraph;
  }

  public Person doCoreference(int corefMapKey, CoreMap quote) {
    if(pronounCorefMap == null) {
      return null;
    }
    Set<Person> quoteNames = new HashSet<>();
    if(quote != null) {
      quoteNames = getNamesInParagraph(quote);
    }
    String referent = pronounCorefMap.get(corefMapKey);
    Person candidate = resolveAmbiguities(referent);
    if (candidate != null && !quoteNames.contains(candidate)) {
      return candidate;
    }
    return null;
  }

  private class TokenNode {
    public List<Person> personList;
    public HashMap<String, TokenNode> childNodes;
    public String token;
    public String fullName;
    int level;

    public TokenNode(String token, int level) {
      this.token = token;
      this.level = level;
      childNodes = new HashMap<>();
    }

  }

  protected TokenNode createNameMatcher() {
    TokenNode rootNode = new TokenNode("$ROOT", -1);

    for(String key : characterMap.keySet()) {
      String[] tokens = key.split(" ");
      TokenNode currNode = rootNode;
      for(int i = 0; i < tokens.length; i++) {
        String tok = tokens[i];
        if(currNode.childNodes.keySet().contains(tok)) {
          currNode = currNode.childNodes.get(tok);
        }
        else {
          TokenNode newNode = new TokenNode(tok, i);
          currNode.childNodes.put(tok, newNode);
          currNode = newNode;
        }

        if(i == tokens.length - 1) {
          currNode.personList = characterMap.get(key);
          currNode.fullName = key;
        }
      }
    }
    return rootNode;
  }

  //Note: this doesn't necessarily find all possible candidates, but is kind of a greedy version.
  // E.g. "Elizabeth and Jane" will return only "Elizabeth and Jane", but not "Elizabeth", and "Jane" as well.
  public Pair<ArrayList<String>, ArrayList<Pair<Integer, Integer>>> scanForNamesNew(Pair<Integer, Integer> textRun) {
    ArrayList<String> potentialNames = new ArrayList<>();
    ArrayList<Pair<Integer, Integer>> nameIndices = new ArrayList<>();
    List<CoreLabel> tokens = doc.get(CoreAnnotations.TokensAnnotation.class);

    TokenNode pointer = rootNameNode;
    for(int index = textRun.first; index <= textRun.second && index < tokens.size(); index++) {
      CoreLabel token = tokens.get(index);
      String tokenText = token.word();
//      System.out.println(token);

      if(pointer.childNodes.keySet().contains(tokenText)) {
        pointer = pointer.childNodes.get(tokenText);
      }
      else {
        if(!pointer.token.equals("$ROOT")) {
          if(pointer.fullName != null) {
            potentialNames.add(pointer.fullName);
            nameIndices.add(new Pair<>(index - 1 - pointer.level, index - 1));
          }
          pointer = rootNameNode;
        }
      }
    }
    int index = textRun.second + 1;
    if(!pointer.token.equals("$ROOT")) { //catch the end case
      if(pointer.fullName != null) {
        potentialNames.add(pointer.fullName);
        nameIndices.add(new Pair<>(index - 1 - pointer.level, index - 1));
      }
      pointer = rootNameNode;
    }
    return new Pair<>(potentialNames, nameIndices);
  }


  //scan for all potential names based on names list, based on CoreMaps and returns their indices in doc.tokens as well.
  public Pair<ArrayList<String>, ArrayList<Pair<Integer, Integer>>> scanForNames(Pair<Integer, Integer> textRun){
    ArrayList<String> potentialNames = new ArrayList<>();
    ArrayList<Pair<Integer, Integer>> nameIndices = new ArrayList<>();
    List<CoreLabel> tokens = doc.get(CoreAnnotations.TokensAnnotation.class); //split on non-alphanumeric
    Set<String> aliases = characterMap.keySet();
    String potentialName = "";
    Pair<Integer, Integer> potentialIndex = null;

    for(int index = textRun.first; index <= textRun.second; index++)
    {
      CoreLabel token = tokens.get(index);
      String tokenText = token.word();

      if(Character.isUpperCase(tokenText.charAt(0)) || tokenText.equals("de")) //TODO: make this better (String matching)
      {
        potentialName += " " + tokenText;
        if(potentialIndex == null)
          potentialIndex = new Pair<>(index, index);
        else
          potentialIndex.second = index;
      }
      else
      {
        if(potentialName.length() != 0) {
          String actual = potentialName.substring(1);
          if(aliases.contains(actual)) {
            potentialNames.add(actual);
            nameIndices.add(potentialIndex);
          }
          else // in the event that the first word in a sentence is a non-name..
          {
            String removeFirstWord = actual.substring(actual.indexOf(" ") + 1);
            if(aliases.contains(removeFirstWord))
            {
              potentialNames.add(removeFirstWord);
              nameIndices.add(new Pair<>(potentialIndex.first + 1, potentialIndex.second));
            }

          }

          potentialName = "";
          potentialIndex = null;
        }
      }
    }

    if(potentialName.length() != 0) {
      if(aliases.contains(potentialName.substring(1))) {
        potentialNames.add(potentialName.substring(1));
        nameIndices.add(potentialIndex);
      }
    }
    return new Pair<>(potentialNames,nameIndices);
  }

  protected ArrayList<Integer> scanForPronouns(Pair<Integer, Integer> nonQuoteRun) {
    List<CoreLabel> tokens = doc.get(CoreAnnotations.TokensAnnotation.class);
    ArrayList<Integer> pronounList = new ArrayList<>();


    for(int i = nonQuoteRun.first; i <= nonQuoteRun.second && i < tokens.size() ; i++)
    {
      if(tokens.get(i).word().equalsIgnoreCase("he") || tokens.get(i).word().equalsIgnoreCase("she"))
        pronounList.add(i);
    }

    return pronounList;
  }

  protected ArrayList<Integer> scanForPronouns(ArrayList<Pair<Integer, Integer>> nonQuoteRuns) {
    ArrayList<Integer> pronounList = new ArrayList<>();
    for(int run_index = 0; run_index < nonQuoteRuns.size(); run_index++)
      pronounList.addAll(scanForPronouns(nonQuoteRuns.get(run_index)));
    return pronounList;
  }

  // for filling in the text of a mention
  public String tokenRangeToString(Pair<Integer, Integer> tokenRange) {
    List<CoreLabel> tokens = doc.get(CoreAnnotations.TokensAnnotation.class);
    // see if the token range matches an entity mention
    List<CoreMap> entityMentionsInDoc = doc.get(CoreAnnotations.MentionsAnnotation.class);
    Integer potentialMatchingEntityMentionIndex =
        tokens.get(tokenRange.first).get(CoreAnnotations.EntityMentionIndexAnnotation.class);
    CoreMap potentialMatchingEntityMention = null;
    if (entityMentionsInDoc != null && potentialMatchingEntityMentionIndex != null) {
      potentialMatchingEntityMention = entityMentionsInDoc.get(potentialMatchingEntityMentionIndex);
    }
    // if there is a matching entity mention, return it's text (which has been processed to remove
    // things like newlines and xml)...if there isn't return the full substring of the document text
    if (potentialMatchingEntityMention != null &&
        potentialMatchingEntityMention.get(
            CoreAnnotations.CharacterOffsetBeginAnnotation.class) == tokens.get(tokenRange.first).beginPosition() &&
        potentialMatchingEntityMention.get(
            CoreAnnotations.CharacterOffsetEndAnnotation.class) == tokens.get(tokenRange.second).endPosition()) {
      return potentialMatchingEntityMention.get(CoreAnnotations.TextAnnotation.class);
    } else {
      return doc.get(CoreAnnotations.TextAnnotation.class).substring(
          tokens.get(tokenRange.first).beginPosition(), tokens.get(tokenRange.second).endPosition());
    }
  }

  public String tokenRangeToString(int token_idx) {
    return doc.get(CoreAnnotations.TokensAnnotation.class).get(token_idx).word();
  }

  public MentionData findClosestMentionInSpanForward(Pair<Integer, Integer> span) {
    List<Integer> pronounIndices = scanForPronouns(span);
    List<Pair<Integer, Integer>> nameIndices = scanForNamesNew(span).second;
    List<Integer> animacyIndices = scanForAnimates(span);

    int closestPronounIndex = Integer.MAX_VALUE, closestAnimate = Integer.MAX_VALUE;
    Pair<Integer, Integer> closestNameIndex = new Pair<>(Integer.MAX_VALUE, 0);

    if(pronounIndices.size() > 0)
      closestPronounIndex = pronounIndices.get(0);
    if(nameIndices.size() > 0)
      closestNameIndex = nameIndices.get(0);
    if(animacyIndices.size() > 0)
      closestAnimate = animacyIndices.get(0);
    MentionData md = null;
    if(closestPronounIndex < closestNameIndex.first) {
      md = (closestAnimate < closestPronounIndex) ? new MentionData(closestAnimate, closestAnimate, tokenRangeToString(closestAnimate), ANIMATE_NOUN)
              : new MentionData(closestPronounIndex, closestPronounIndex, tokenRangeToString(closestPronounIndex), PRONOUN);
    } else if(closestPronounIndex > closestNameIndex.first) {
      md = (closestAnimate < closestNameIndex.first) ? new MentionData(closestAnimate, closestAnimate, tokenRangeToString(closestAnimate), ANIMATE_NOUN)
              : new MentionData(closestNameIndex.first, closestNameIndex.second, tokenRangeToString(closestNameIndex), NAME);
    }
    return md;
  }



  public List<MentionData> findClosestMentionsInSpanForward(Pair<Integer, Integer> span) {
    List<MentionData> mentions = new ArrayList<>();
    Pair<Integer, Integer> currSpan = span;
    while(true) {
      MentionData mention = findClosestMentionInSpanForward(currSpan);
      if(mention != null) {
        mentions.add(mention);
        currSpan.first = mention.end + 1;
      }
      else {
        return mentions;
      }
    }
  }

  public List<MentionData> findClosestMentionsInSpanBackward(Pair<Integer, Integer> span) {
    List<MentionData> mentions = new ArrayList<>();
    Pair<Integer, Integer> currSpan = span;
    while(true) {
      MentionData mentionData = findClosestMentionInSpanBackward(currSpan);
      if(mentionData != null) {
        mentions.add(mentionData);
        currSpan.second = mentionData.begin -1;
      }
      else {
        return mentions;
      }
    }
  }


  public List<Integer> scanForAnimates(Pair<Integer, Integer> span) {
    List<Integer> animateIndices = new ArrayList<>();
    List<CoreLabel> tokens = doc.get(CoreAnnotations.TokensAnnotation.class);
    for(int i = span.first; i <= span.second && i < tokens.size() ; i++)
    {
      CoreLabel token = tokens.get(i);
      if(animacySet.contains(token.word()))
        animateIndices.add(i);
    }
    return animateIndices;
  }

  public class MentionData {
    public int begin;
    public int end;
    public String text;
    public String type;

    public MentionData(int begin, int end, String text, String type) {
      this.begin = begin;
      this.end = end;
      this.text = text;
      this.type = type;
    }
  }

  public MentionData findClosestMentionInSpanBackward(Pair<Integer, Integer> span) {
    List<Integer> pronounIndices = scanForPronouns(span);
    List<Pair<Integer, Integer>> nameIndices = scanForNamesNew(span).second;
    List<Integer> animateIndices = scanForAnimates(span);

    int closestPronounIndex = Integer.MIN_VALUE, closestAnimate = Integer.MIN_VALUE;
    Pair<Integer, Integer> closestNameIndex = new Pair<>(0, Integer.MIN_VALUE);

    if(pronounIndices.size() > 0) {
      closestPronounIndex = pronounIndices.get(pronounIndices.size() - 1);
    }
    if(nameIndices.size() > 0) {
      closestNameIndex = nameIndices.get(nameIndices.size() - 1);
    }
    if(animateIndices.size() > 0) {
      closestAnimate = animateIndices.get(animateIndices.size() - 1);
    }

    MentionData md = null;
    if(closestPronounIndex > closestNameIndex.second) {
      md = (closestAnimate > closestPronounIndex) ? new MentionData(closestAnimate, closestAnimate, tokenRangeToString(closestAnimate), ANIMATE_NOUN)
        : new MentionData(closestPronounIndex, closestPronounIndex, tokenRangeToString(closestPronounIndex), PRONOUN);
    }
    else if(closestPronounIndex < closestNameIndex.second) {
      md = (closestAnimate > closestNameIndex.second) ? new MentionData(closestAnimate, closestAnimate, tokenRangeToString(closestAnimate), ANIMATE_NOUN)
              : new MentionData(closestNameIndex.first, closestNameIndex.second, tokenRangeToString(closestNameIndex), NAME);
    }
    return md;
  }


  private class Mention {
    public int begin, end;
    public String text, type;

    public Mention(int begin, int end, String text, String type) {
      this.begin = begin;
      this.end = end;
      this.text = text;
      this.type = type;
    }
  }

  public void oneSpeakerSentence(Annotation doc) {
    List<CoreLabel> toks = doc.get(CoreAnnotations.TokensAnnotation.class);
    List<CoreMap> quotes = doc.get(CoreAnnotations.QuotationsAnnotation.class);
    Map<Integer, List<CoreMap>> quotesBySentence = new HashMap<>();
    for (int quoteIndex = 0; quoteIndex < quotes.size(); quoteIndex++) {
      CoreMap quote = quotes.get(quoteIndex);

      // iterate through each quote in the chapter
      // group quotes by sentence

      int quoteBeginTok = quote.get(CoreAnnotations.TokenBeginAnnotation.class);
      int sentenceBeginId = toks.get(quoteBeginTok).sentIndex();
      int quoteEndTok = quote.get(CoreAnnotations.TokenEndAnnotation.class);
      int sentenceEndId = toks.get(quoteEndTok).sentIndex();
      quotesBySentence.putIfAbsent(sentenceBeginId, new ArrayList<>());
      quotesBySentence.putIfAbsent(sentenceEndId, new ArrayList<>());
      quotesBySentence.get(sentenceBeginId).add(quote);
      quotesBySentence.get(sentenceEndId).add(quote);
    }

    //
    for (int k : quotesBySentence.keySet()) {
      List<CoreMap> quotesInSent = quotesBySentence.get(k);
      List<Mention> existantMentions = new ArrayList<>();
      for (CoreMap quote : quotesInSent) {
        if (quote.get(QuoteAttributionAnnotator.MentionAnnotation.class) != null) {
          Mention m = new Mention(quote.get(QuoteAttributionAnnotator.MentionBeginAnnotation.class),
                                  quote.get(QuoteAttributionAnnotator.MentionEndAnnotation.class),
                                  quote.get(QuoteAttributionAnnotator.MentionAnnotation.class),
                                  quote.get(QuoteAttributionAnnotator.MentionTypeAnnotation.class));
          existantMentions.add(m);
        }
      }

      //remove cases in which there is more than one mention in a sentence.
      boolean same = true;
      String text = null;
      for (Mention m : existantMentions) {
        if (text == null) {
          text = m.text;
        }
        if (!m.text.equalsIgnoreCase(text)) {
          same = false;
        }
      }

      if (same && text != null && existantMentions.size() > 0) {
        for (CoreMap quote : quotesInSent) {
          if (quote.get(QuoteAttributionAnnotator.MentionAnnotation.class) == null) {
            Mention firstM = existantMentions.get(0);
            quote.set(QuoteAttributionAnnotator.MentionAnnotation.class, firstM.text);
            quote.set(QuoteAttributionAnnotator.MentionBeginAnnotation.class, firstM.begin);
            quote.set(QuoteAttributionAnnotator.MentionEndAnnotation.class, firstM.end);
            quote.set(QuoteAttributionAnnotator.MentionSieveAnnotation.class, "Deterministic one speaker sentence");
            quote.set(QuoteAttributionAnnotator.MentionTypeAnnotation.class, firstM.type);
          }
        }
      }
    }
  }

  //convert token range to char range, check if charIndex is in it.
  public  boolean rangeContainsCharIndex(Pair<Integer, Integer> tokenRange, int charIndex) {
    List<CoreLabel> tokens = doc.get(CoreAnnotations.TokensAnnotation.class);
    CoreLabel startToken = tokens.get(tokenRange.first());
    CoreLabel endToken = tokens.get(tokenRange.second());
    int startTokenCharBegin  = startToken.beginPosition();
    int endTokenCharEnd = endToken.endPosition();
    return (startTokenCharBegin <= charIndex && charIndex <= endTokenCharEnd);
  }

  public int tokenToLocation(CoreLabel token) {
    CoreMap sentence = doc.get(CoreAnnotations.SentencesAnnotation.class).get(
        token.get(CoreAnnotations.SentenceIndexAnnotation.class));
    return sentence.get(CoreAnnotations.TokenBeginAnnotation.class) +
        token.get(CoreAnnotations.IndexAnnotation.class) - 1;
  }

  protected int getQuoteParagraph(CoreMap quote) {
    List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
    return sentences.get(quote.get(CoreAnnotations.SentenceBeginAnnotation.class)).get(CoreAnnotations.ParagraphIndexAnnotation.class);
  }
}
