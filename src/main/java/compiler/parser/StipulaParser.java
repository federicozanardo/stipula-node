// Generated from Stipula.g4 by ANTLR 4.10
package compiler.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class StipulaParser extends Parser {
    static {
        RuntimeMetaData.checkVersion("4.10", RuntimeMetaData.VERSION);
    }

    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    public static final int
            SEMIC = 1, COLON = 2, COMMA = 3, DOT = 4, EQ = 5, NEQ = 6, IMPL = 7, ASM = 8, ASSETUP = 9,
            FIELDUP = 10, PLUS = 11, MINUS = 12, TIMES = 13, DIV = 14, AT = 15, TRUE = 16, FALSE = 17,
            LPAR = 18, RPAR = 19, SLPAR = 20, SRPAR = 21, CLPAR = 22, CRPAR = 23, LEQ = 24, GEQ = 25,
            LE = 26, GE = 27, OR = 28, AND = 29, NOT = 30, EMPTY = 31, NOW = 32, TRIGGER = 33, IF = 34,
            ELSEIF = 35, ELSE = 36, STIPULA = 37, ASSET = 38, FIELD = 39, AGREEMENT = 40, INTEGER = 41,
            DOUBLE = 42, BOOLEAN = 43, STRING = 44, PARTY = 45, INIT = 46, RAWSTRING = 47, INT = 48,
            REAL = 49, WS = 50, ID = 51, OTHER = 52, LINECOMENTS = 53, BLOCKCOMENTS = 54, ERR = 55;
    public static final int
            RULE_prog = 0, RULE_agreement = 1, RULE_assetdecl = 2, RULE_fielddecl = 3,
            RULE_fun = 4, RULE_assign = 5, RULE_dec = 6, RULE_type = 7, RULE_state = 8,
            RULE_party = 9, RULE_vardec = 10, RULE_assetdec = 11, RULE_varasm = 12,
            RULE_stat = 13, RULE_ifelse = 14, RULE_events = 15, RULE_prec = 16, RULE_expr = 17,
            RULE_term = 18, RULE_factor = 19, RULE_value = 20, RULE_real = 21, RULE_number = 22;

    private static String[] makeRuleNames() {
        return new String[]{
                "prog", "agreement", "assetdecl", "fielddecl", "fun", "assign", "dec",
                "type", "state", "party", "vardec", "assetdec", "varasm", "stat", "ifelse",
                "events", "prec", "expr", "term", "factor", "value", "real", "number"
        };
    }

    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[]{
                null, "';'", "':'", "','", "'.'", "'=='", "'!='", "'==>'", "'='", "'-o'",
                "'->'", "'+'", "'-'", "'*'", "'/'", "'@'", "'true'", "'false'", "'('",
                "')'", "'['", "']'", "'{'", "'}'", "'<='", "'>='", "'<'", "'>'", "'||'",
                "'&&'", "'!'", "'_'", "'now'", "'>>'", "'if'", "'else if'", "'else'",
                "'stipula'", "'asset'", "'field'", "'agreement'", "'int'", "'real'",
                "'bool'", "'string'", "'party'", "'init'"
        };
    }

    private static final String[] _LITERAL_NAMES = makeLiteralNames();

    private static String[] makeSymbolicNames() {
        return new String[]{
                null, "SEMIC", "COLON", "COMMA", "DOT", "EQ", "NEQ", "IMPL", "ASM", "ASSETUP",
                "FIELDUP", "PLUS", "MINUS", "TIMES", "DIV", "AT", "TRUE", "FALSE", "LPAR",
                "RPAR", "SLPAR", "SRPAR", "CLPAR", "CRPAR", "LEQ", "GEQ", "LE", "GE",
                "OR", "AND", "NOT", "EMPTY", "NOW", "TRIGGER", "IF", "ELSEIF", "ELSE",
                "STIPULA", "ASSET", "FIELD", "AGREEMENT", "INTEGER", "DOUBLE", "BOOLEAN",
                "STRING", "PARTY", "INIT", "RAWSTRING", "INT", "REAL", "WS", "ID", "OTHER",
                "LINECOMENTS", "BLOCKCOMENTS", "ERR"
        };
    }

    private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;

    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }

    @Override
    public String getGrammarFileName() {
        return "Stipula.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public StipulaParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    public static class ProgContext extends ParserRuleContext {
        public Token contract_id;
        public Token init_state;

        public TerminalNode STIPULA() {
            return getToken(StipulaParser.STIPULA, 0);
        }

        public TerminalNode CLPAR() {
            return getToken(StipulaParser.CLPAR, 0);
        }

        public TerminalNode INIT() {
            return getToken(StipulaParser.INIT, 0);
        }

        public AgreementContext agreement() {
            return getRuleContext(AgreementContext.class, 0);
        }

        public TerminalNode CRPAR() {
            return getToken(StipulaParser.CRPAR, 0);
        }

        public List<TerminalNode> ID() {
            return getTokens(StipulaParser.ID);
        }

        public TerminalNode ID(int i) {
            return getToken(StipulaParser.ID, i);
        }

        public AssetdeclContext assetdecl() {
            return getRuleContext(AssetdeclContext.class, 0);
        }

        public FielddeclContext fielddecl() {
            return getRuleContext(FielddeclContext.class, 0);
        }

        public List<FunContext> fun() {
            return getRuleContexts(FunContext.class);
        }

        public FunContext fun(int i) {
            return getRuleContext(FunContext.class, i);
        }

        public ProgContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_prog;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterProg(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitProg(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitProg(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ProgContext prog() throws RecognitionException {
        ProgContext _localctx = new ProgContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_prog);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(46);
                match(STIPULA);
                setState(47);
                ((ProgContext) _localctx).contract_id = match(ID);
                setState(48);
                match(CLPAR);
                setState(50);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == ASSET) {
                    {
                        setState(49);
                        assetdecl();
                    }
                }

                setState(53);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == FIELD) {
                    {
                        setState(52);
                        fielddecl();
                    }
                }

                setState(55);
                match(INIT);
                setState(56);
                ((ProgContext) _localctx).init_state = match(ID);
                setState(57);
                agreement();
                setState(59);
                _errHandler.sync(this);
                _la = _input.LA(1);
                do {
                    {
                        {
                            setState(58);
                            fun();
                        }
                    }
                    setState(61);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                } while (_la == AT || _la == ID);
                setState(63);
                match(CRPAR);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class AgreementContext extends ParserRuleContext {
        public TerminalNode AGREEMENT() {
            return getToken(StipulaParser.AGREEMENT, 0);
        }

        public List<TerminalNode> LPAR() {
            return getTokens(StipulaParser.LPAR);
        }

        public TerminalNode LPAR(int i) {
            return getToken(StipulaParser.LPAR, i);
        }

        public List<PartyContext> party() {
            return getRuleContexts(PartyContext.class);
        }

        public PartyContext party(int i) {
            return getRuleContext(PartyContext.class, i);
        }

        public List<TerminalNode> RPAR() {
            return getTokens(StipulaParser.RPAR);
        }

        public TerminalNode RPAR(int i) {
            return getToken(StipulaParser.RPAR, i);
        }

        public List<VardecContext> vardec() {
            return getRuleContexts(VardecContext.class);
        }

        public VardecContext vardec(int i) {
            return getRuleContext(VardecContext.class, i);
        }

        public TerminalNode CLPAR() {
            return getToken(StipulaParser.CLPAR, 0);
        }

        public TerminalNode CRPAR() {
            return getToken(StipulaParser.CRPAR, 0);
        }

        public TerminalNode IMPL() {
            return getToken(StipulaParser.IMPL, 0);
        }

        public TerminalNode AT() {
            return getToken(StipulaParser.AT, 0);
        }

        public StateContext state() {
            return getRuleContext(StateContext.class, 0);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(StipulaParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(StipulaParser.COMMA, i);
        }

        public List<AssignContext> assign() {
            return getRuleContexts(AssignContext.class);
        }

        public AssignContext assign(int i) {
            return getRuleContext(AssignContext.class, i);
        }

        public AgreementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_agreement;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterAgreement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitAgreement(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitAgreement(this);
            else return visitor.visitChildren(this);
        }
    }

    public final AgreementContext agreement() throws RecognitionException {
        AgreementContext _localctx = new AgreementContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_agreement);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(65);
                    match(AGREEMENT);
                    setState(66);
                    match(LPAR);
                    setState(67);
                    party();
                    setState(72);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(68);
                                match(COMMA);
                                setState(69);
                                party();
                            }
                        }
                        setState(74);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(75);
                    match(RPAR);
                    setState(76);
                    match(LPAR);
                    setState(77);
                    vardec();
                    setState(82);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(78);
                                match(COMMA);
                                setState(79);
                                vardec();
                            }
                        }
                        setState(84);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(85);
                    match(RPAR);
                    setState(86);
                    match(CLPAR);
                    setState(88);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    do {
                        {
                            {
                                setState(87);
                                assign();
                            }
                        }
                        setState(90);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    } while (_la == ID);
                    setState(92);
                    match(CRPAR);
                    setState(93);
                    match(IMPL);
                    setState(94);
                    match(AT);
                    setState(95);
                    state();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class AssetdeclContext extends ParserRuleContext {
        public Token ID;
        public List<Token> idAsset = new ArrayList<Token>();

        public TerminalNode ASSET() {
            return getToken(StipulaParser.ASSET, 0);
        }

        public List<TerminalNode> ID() {
            return getTokens(StipulaParser.ID);
        }

        public TerminalNode ID(int i) {
            return getToken(StipulaParser.ID, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(StipulaParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(StipulaParser.COMMA, i);
        }

        public AssetdeclContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_assetdecl;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterAssetdecl(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitAssetdecl(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitAssetdecl(this);
            else return visitor.visitChildren(this);
        }
    }

    public final AssetdeclContext assetdecl() throws RecognitionException {
        AssetdeclContext _localctx = new AssetdeclContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_assetdecl);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(97);
                match(ASSET);
                setState(98);
                ((AssetdeclContext) _localctx).ID = match(ID);
                ((AssetdeclContext) _localctx).idAsset.add(((AssetdeclContext) _localctx).ID);
                setState(103);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(99);
                            match(COMMA);
                            setState(100);
                            ((AssetdeclContext) _localctx).ID = match(ID);
                            ((AssetdeclContext) _localctx).idAsset.add(((AssetdeclContext) _localctx).ID);
                        }
                    }
                    setState(105);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class FielddeclContext extends ParserRuleContext {
        public Token ID;
        public List<Token> idField = new ArrayList<Token>();

        public TerminalNode FIELD() {
            return getToken(StipulaParser.FIELD, 0);
        }

        public List<TerminalNode> ID() {
            return getTokens(StipulaParser.ID);
        }

        public TerminalNode ID(int i) {
            return getToken(StipulaParser.ID, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(StipulaParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(StipulaParser.COMMA, i);
        }

        public FielddeclContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_fielddecl;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterFielddecl(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitFielddecl(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitFielddecl(this);
            else return visitor.visitChildren(this);
        }
    }

    public final FielddeclContext fielddecl() throws RecognitionException {
        FielddeclContext _localctx = new FielddeclContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_fielddecl);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(106);
                match(FIELD);
                setState(107);
                ((FielddeclContext) _localctx).ID = match(ID);
                ((FielddeclContext) _localctx).idField.add(((FielddeclContext) _localctx).ID);
                setState(112);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(108);
                            match(COMMA);
                            setState(109);
                            ((FielddeclContext) _localctx).ID = match(ID);
                            ((FielddeclContext) _localctx).idField.add(((FielddeclContext) _localctx).ID);
                        }
                    }
                    setState(114);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class FunContext extends ParserRuleContext {
        public Token funId;

        public List<PartyContext> party() {
            return getRuleContexts(PartyContext.class);
        }

        public PartyContext party(int i) {
            return getRuleContext(PartyContext.class, i);
        }

        public TerminalNode COLON() {
            return getToken(StipulaParser.COLON, 0);
        }

        public List<TerminalNode> LPAR() {
            return getTokens(StipulaParser.LPAR);
        }

        public TerminalNode LPAR(int i) {
            return getToken(StipulaParser.LPAR, i);
        }

        public List<TerminalNode> RPAR() {
            return getTokens(StipulaParser.RPAR);
        }

        public TerminalNode RPAR(int i) {
            return getToken(StipulaParser.RPAR, i);
        }

        public TerminalNode SLPAR() {
            return getToken(StipulaParser.SLPAR, 0);
        }

        public TerminalNode SRPAR() {
            return getToken(StipulaParser.SRPAR, 0);
        }

        public TerminalNode CLPAR() {
            return getToken(StipulaParser.CLPAR, 0);
        }

        public TerminalNode SEMIC() {
            return getToken(StipulaParser.SEMIC, 0);
        }

        public TerminalNode CRPAR() {
            return getToken(StipulaParser.CRPAR, 0);
        }

        public TerminalNode IMPL() {
            return getToken(StipulaParser.IMPL, 0);
        }

        public List<TerminalNode> AT() {
            return getTokens(StipulaParser.AT);
        }

        public TerminalNode AT(int i) {
            return getToken(StipulaParser.AT, i);
        }

        public List<StateContext> state() {
            return getRuleContexts(StateContext.class);
        }

        public StateContext state(int i) {
            return getRuleContext(StateContext.class, i);
        }

        public TerminalNode ID() {
            return getToken(StipulaParser.ID, 0);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(StipulaParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(StipulaParser.COMMA, i);
        }

        public List<VardecContext> vardec() {
            return getRuleContexts(VardecContext.class);
        }

        public VardecContext vardec(int i) {
            return getRuleContext(VardecContext.class, i);
        }

        public List<AssetdecContext> assetdec() {
            return getRuleContexts(AssetdecContext.class);
        }

        public AssetdecContext assetdec(int i) {
            return getRuleContext(AssetdecContext.class, i);
        }

        public PrecContext prec() {
            return getRuleContext(PrecContext.class, 0);
        }

        public List<StatContext> stat() {
            return getRuleContexts(StatContext.class);
        }

        public StatContext stat(int i) {
            return getRuleContext(StatContext.class, i);
        }

        public List<EventsContext> events() {
            return getRuleContexts(EventsContext.class);
        }

        public EventsContext events(int i) {
            return getRuleContext(EventsContext.class, i);
        }

        public FunContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_fun;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterFun(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitFun(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitFun(this);
            else return visitor.visitChildren(this);
        }
    }

    public final FunContext fun() throws RecognitionException {
        FunContext _localctx = new FunContext(_ctx, getState());
        enterRule(_localctx, 8, RULE_fun);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(119);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == AT) {
                        {
                            {
                                setState(115);
                                match(AT);
                                setState(116);
                                state();
                            }
                        }
                        setState(121);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(122);
                    party();
                    setState(127);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(123);
                                match(COMMA);
                                setState(124);
                                party();
                            }
                        }
                        setState(129);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(130);
                    match(COLON);
                    setState(131);
                    ((FunContext) _localctx).funId = match(ID);
                    setState(132);
                    match(LPAR);
                    setState(141);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == ID) {
                        {
                            setState(133);
                            vardec();
                            setState(138);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            while (_la == COMMA) {
                                {
                                    {
                                        setState(134);
                                        match(COMMA);
                                        setState(135);
                                        vardec();
                                    }
                                }
                                setState(140);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            }
                        }
                    }

                    setState(143);
                    match(RPAR);
                    setState(144);
                    match(SLPAR);
                    setState(153);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == ID) {
                        {
                            setState(145);
                            assetdec();
                            setState(150);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            while (_la == COMMA) {
                                {
                                    {
                                        setState(146);
                                        match(COMMA);
                                        setState(147);
                                        assetdec();
                                    }
                                }
                                setState(152);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            }
                        }
                    }

                    setState(155);
                    match(SRPAR);
                    setState(160);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LPAR) {
                        {
                            setState(156);
                            match(LPAR);
                            setState(157);
                            prec();
                            setState(158);
                            match(RPAR);
                        }
                    }

                    setState(162);
                    match(CLPAR);
                    setState(164);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    do {
                        {
                            {
                                setState(163);
                                stat();
                            }
                        }
                        setState(166);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TRUE) | (1L << FALSE) | (1L << LPAR) | (1L << EMPTY) | (1L << NOW) | (1L << IF) | (1L << RAWSTRING) | (1L << INT) | (1L << REAL) | (1L << ID))) != 0));
                    setState(168);
                    match(SEMIC);
                    setState(170);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    do {
                        {
                            {
                                setState(169);
                                events();
                            }
                        }
                        setState(172);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MINUS) | (1L << TRUE) | (1L << FALSE) | (1L << LPAR) | (1L << EMPTY) | (1L << NOW) | (1L << RAWSTRING) | (1L << INT) | (1L << REAL) | (1L << ID))) != 0));
                    setState(174);
                    match(CRPAR);
                    setState(175);
                    match(IMPL);
                    setState(176);
                    match(AT);
                    setState(177);
                    state();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class AssignContext extends ParserRuleContext {
        public List<PartyContext> party() {
            return getRuleContexts(PartyContext.class);
        }

        public PartyContext party(int i) {
            return getRuleContext(PartyContext.class, i);
        }

        public TerminalNode COLON() {
            return getToken(StipulaParser.COLON, 0);
        }

        public List<VardecContext> vardec() {
            return getRuleContexts(VardecContext.class);
        }

        public VardecContext vardec(int i) {
            return getRuleContext(VardecContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(StipulaParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(StipulaParser.COMMA, i);
        }

        public AssignContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_assign;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterAssign(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitAssign(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitAssign(this);
            else return visitor.visitChildren(this);
        }
    }

    public final AssignContext assign() throws RecognitionException {
        AssignContext _localctx = new AssignContext(_ctx, getState());
        enterRule(_localctx, 10, RULE_assign);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(179);
                    party();
                    setState(184);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(180);
                                match(COMMA);
                                setState(181);
                                party();
                            }
                        }
                        setState(186);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(187);
                    match(COLON);
                    setState(188);
                    vardec();
                    setState(193);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(189);
                                match(COMMA);
                                setState(190);
                                vardec();
                            }
                        }
                        setState(195);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class DecContext extends ParserRuleContext {
        public TerminalNode ID() {
            return getToken(StipulaParser.ID, 0);
        }

        public TerminalNode ASSET() {
            return getToken(StipulaParser.ASSET, 0);
        }

        public TerminalNode FIELD() {
            return getToken(StipulaParser.FIELD, 0);
        }

        public DecContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dec;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterDec(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitDec(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitDec(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DecContext dec() throws RecognitionException {
        DecContext _localctx = new DecContext(_ctx, getState());
        enterRule(_localctx, 12, RULE_dec);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(196);
                _la = _input.LA(1);
                if (!(_la == ASSET || _la == FIELD)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(197);
                match(ID);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class TypeContext extends ParserRuleContext {
        public TerminalNode INTEGER() {
            return getToken(StipulaParser.INTEGER, 0);
        }

        public TerminalNode DOUBLE() {
            return getToken(StipulaParser.DOUBLE, 0);
        }

        public TerminalNode BOOLEAN() {
            return getToken(StipulaParser.BOOLEAN, 0);
        }

        public TerminalNode STRING() {
            return getToken(StipulaParser.STRING, 0);
        }

        public TypeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_type;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterType(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitType(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitType(this);
            else return visitor.visitChildren(this);
        }
    }

    public final TypeContext type() throws RecognitionException {
        TypeContext _localctx = new TypeContext(_ctx, getState());
        enterRule(_localctx, 14, RULE_type);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(199);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INTEGER) | (1L << DOUBLE) | (1L << BOOLEAN) | (1L << STRING))) != 0))) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class StateContext extends ParserRuleContext {
        public TerminalNode ID() {
            return getToken(StipulaParser.ID, 0);
        }

        public StateContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_state;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterState(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitState(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitState(this);
            else return visitor.visitChildren(this);
        }
    }

    public final StateContext state() throws RecognitionException {
        StateContext _localctx = new StateContext(_ctx, getState());
        enterRule(_localctx, 16, RULE_state);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(201);
                match(ID);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class PartyContext extends ParserRuleContext {
        public TerminalNode ID() {
            return getToken(StipulaParser.ID, 0);
        }

        public PartyContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_party;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterParty(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitParty(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitParty(this);
            else return visitor.visitChildren(this);
        }
    }

    public final PartyContext party() throws RecognitionException {
        PartyContext _localctx = new PartyContext(_ctx, getState());
        enterRule(_localctx, 18, RULE_party);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(203);
                match(ID);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class VardecContext extends ParserRuleContext {
        public TerminalNode ID() {
            return getToken(StipulaParser.ID, 0);
        }

        public VardecContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_vardec;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterVardec(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitVardec(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitVardec(this);
            else return visitor.visitChildren(this);
        }
    }

    public final VardecContext vardec() throws RecognitionException {
        VardecContext _localctx = new VardecContext(_ctx, getState());
        enterRule(_localctx, 20, RULE_vardec);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(205);
                match(ID);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class AssetdecContext extends ParserRuleContext {
        public TerminalNode ID() {
            return getToken(StipulaParser.ID, 0);
        }

        public AssetdecContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_assetdec;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterAssetdec(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitAssetdec(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitAssetdec(this);
            else return visitor.visitChildren(this);
        }
    }

    public final AssetdecContext assetdec() throws RecognitionException {
        AssetdecContext _localctx = new AssetdecContext(_ctx, getState());
        enterRule(_localctx, 22, RULE_assetdec);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(207);
                match(ID);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class VarasmContext extends ParserRuleContext {
        public VardecContext vardec() {
            return getRuleContext(VardecContext.class, 0);
        }

        public TerminalNode ASM() {
            return getToken(StipulaParser.ASM, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public VarasmContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_varasm;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterVarasm(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitVarasm(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitVarasm(this);
            else return visitor.visitChildren(this);
        }
    }

    public final VarasmContext varasm() throws RecognitionException {
        VarasmContext _localctx = new VarasmContext(_ctx, getState());
        enterRule(_localctx, 24, RULE_varasm);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(209);
                vardec();
                setState(210);
                match(ASM);
                setState(211);
                expr();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class StatContext extends ParserRuleContext {
        public ValueContext left;
        public Token operator;
        public Token right;
        public Token rightPlus;

        public TerminalNode EMPTY() {
            return getToken(StipulaParser.EMPTY, 0);
        }

        public ValueContext value() {
            return getRuleContext(ValueContext.class, 0);
        }

        public TerminalNode ASSETUP() {
            return getToken(StipulaParser.ASSETUP, 0);
        }

        public List<TerminalNode> ID() {
            return getTokens(StipulaParser.ID);
        }

        public TerminalNode ID(int i) {
            return getToken(StipulaParser.ID, i);
        }

        public TerminalNode COMMA() {
            return getToken(StipulaParser.COMMA, 0);
        }

        public TerminalNode FIELDUP() {
            return getToken(StipulaParser.FIELDUP, 0);
        }

        public IfelseContext ifelse() {
            return getRuleContext(IfelseContext.class, 0);
        }

        public StatContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_stat;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterStat(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitStat(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitStat(this);
            else return visitor.visitChildren(this);
        }
    }

    public final StatContext stat() throws RecognitionException {
        StatContext _localctx = new StatContext(_ctx, getState());
        enterRule(_localctx, 26, RULE_stat);
        int _la;
        try {
            setState(226);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 20, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(213);
                    match(EMPTY);
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(214);
                    ((StatContext) _localctx).left = value();
                    setState(215);
                    ((StatContext) _localctx).operator = match(ASSETUP);
                    setState(216);
                    ((StatContext) _localctx).right = match(ID);
                    setState(219);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == COMMA) {
                        {
                            setState(217);
                            match(COMMA);
                            setState(218);
                            ((StatContext) _localctx).rightPlus = match(ID);
                        }
                    }

                }
                break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(221);
                    ((StatContext) _localctx).left = value();
                    setState(222);
                    ((StatContext) _localctx).operator = match(FIELDUP);
                    setState(223);
                    ((StatContext) _localctx).right = _input.LT(1);
                    _la = _input.LA(1);
                    if (!(_la == EMPTY || _la == ID)) {
                        ((StatContext) _localctx).right = (Token) _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                }
                break;
                case 4:
                    enterOuterAlt(_localctx, 4);
                {
                    setState(225);
                    ifelse();
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class IfelseContext extends ParserRuleContext {
        public ExprContext cond;
        public StatContext stat;
        public List<StatContext> ifBranch = new ArrayList<StatContext>();
        public ExprContext expr;
        public List<ExprContext> condElseIf = new ArrayList<ExprContext>();
        public List<StatContext> elseIfBranch = new ArrayList<StatContext>();
        public List<StatContext> elseBranch = new ArrayList<StatContext>();

        public TerminalNode IF() {
            return getToken(StipulaParser.IF, 0);
        }

        public TerminalNode LPAR() {
            return getToken(StipulaParser.LPAR, 0);
        }

        public TerminalNode RPAR() {
            return getToken(StipulaParser.RPAR, 0);
        }

        public List<TerminalNode> CLPAR() {
            return getTokens(StipulaParser.CLPAR);
        }

        public TerminalNode CLPAR(int i) {
            return getToken(StipulaParser.CLPAR, i);
        }

        public List<TerminalNode> CRPAR() {
            return getTokens(StipulaParser.CRPAR);
        }

        public TerminalNode CRPAR(int i) {
            return getToken(StipulaParser.CRPAR, i);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public List<StatContext> stat() {
            return getRuleContexts(StatContext.class);
        }

        public StatContext stat(int i) {
            return getRuleContext(StatContext.class, i);
        }

        public List<TerminalNode> ELSEIF() {
            return getTokens(StipulaParser.ELSEIF);
        }

        public TerminalNode ELSEIF(int i) {
            return getToken(StipulaParser.ELSEIF, i);
        }

        public TerminalNode ELSE() {
            return getToken(StipulaParser.ELSE, 0);
        }

        public IfelseContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_ifelse;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterIfelse(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitIfelse(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitIfelse(this);
            else return visitor.visitChildren(this);
        }
    }

    public final IfelseContext ifelse() throws RecognitionException {
        IfelseContext _localctx = new IfelseContext(_ctx, getState());
        enterRule(_localctx, 28, RULE_ifelse);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(228);
                    match(IF);
                    setState(229);
                    match(LPAR);
                    setState(230);
                    ((IfelseContext) _localctx).cond = expr();
                    setState(231);
                    match(RPAR);
                    setState(232);
                    match(CLPAR);
                    setState(233);
                    ((IfelseContext) _localctx).stat = stat();
                    ((IfelseContext) _localctx).ifBranch.add(((IfelseContext) _localctx).stat);
                    setState(237);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TRUE) | (1L << FALSE) | (1L << LPAR) | (1L << EMPTY) | (1L << NOW) | (1L << IF) | (1L << RAWSTRING) | (1L << INT) | (1L << REAL) | (1L << ID))) != 0)) {
                        {
                            {
                                setState(234);
                                ((IfelseContext) _localctx).stat = stat();
                                ((IfelseContext) _localctx).ifBranch.add(((IfelseContext) _localctx).stat);
                            }
                        }
                        setState(239);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(240);
                    match(CRPAR);
                    setState(255);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == ELSEIF) {
                        {
                            {
                                setState(241);
                                match(ELSEIF);
                                setState(242);
                                ((IfelseContext) _localctx).expr = expr();
                                ((IfelseContext) _localctx).condElseIf.add(((IfelseContext) _localctx).expr);
                                setState(243);
                                match(CLPAR);
                                setState(244);
                                ((IfelseContext) _localctx).stat = stat();
                                ((IfelseContext) _localctx).elseIfBranch.add(((IfelseContext) _localctx).stat);
                                setState(248);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TRUE) | (1L << FALSE) | (1L << LPAR) | (1L << EMPTY) | (1L << NOW) | (1L << IF) | (1L << RAWSTRING) | (1L << INT) | (1L << REAL) | (1L << ID))) != 0)) {
                                    {
                                        {
                                            setState(245);
                                            ((IfelseContext) _localctx).stat = stat();
                                            ((IfelseContext) _localctx).elseIfBranch.add(((IfelseContext) _localctx).stat);
                                        }
                                    }
                                    setState(250);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                                setState(251);
                                match(CRPAR);
                            }
                        }
                        setState(257);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(269);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == ELSE) {
                        {
                            setState(258);
                            match(ELSE);
                            setState(259);
                            match(CLPAR);
                            setState(260);
                            ((IfelseContext) _localctx).stat = stat();
                            ((IfelseContext) _localctx).elseBranch.add(((IfelseContext) _localctx).stat);
                            setState(264);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TRUE) | (1L << FALSE) | (1L << LPAR) | (1L << EMPTY) | (1L << NOW) | (1L << IF) | (1L << RAWSTRING) | (1L << INT) | (1L << REAL) | (1L << ID))) != 0)) {
                                {
                                    {
                                        setState(261);
                                        ((IfelseContext) _localctx).stat = stat();
                                        ((IfelseContext) _localctx).elseBranch.add(((IfelseContext) _localctx).stat);
                                    }
                                }
                                setState(266);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            }
                            setState(267);
                            match(CRPAR);
                        }
                    }

                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class EventsContext extends ParserRuleContext {
        public TerminalNode EMPTY() {
            return getToken(StipulaParser.EMPTY, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public TerminalNode TRIGGER() {
            return getToken(StipulaParser.TRIGGER, 0);
        }

        public List<TerminalNode> AT() {
            return getTokens(StipulaParser.AT);
        }

        public TerminalNode AT(int i) {
            return getToken(StipulaParser.AT, i);
        }

        public List<TerminalNode> ID() {
            return getTokens(StipulaParser.ID);
        }

        public TerminalNode ID(int i) {
            return getToken(StipulaParser.ID, i);
        }

        public TerminalNode CLPAR() {
            return getToken(StipulaParser.CLPAR, 0);
        }

        public TerminalNode CRPAR() {
            return getToken(StipulaParser.CRPAR, 0);
        }

        public TerminalNode IMPL() {
            return getToken(StipulaParser.IMPL, 0);
        }

        public List<StatContext> stat() {
            return getRuleContexts(StatContext.class);
        }

        public StatContext stat(int i) {
            return getRuleContext(StatContext.class, i);
        }

        public EventsContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_events;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterEvents(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitEvents(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitEvents(this);
            else return visitor.visitChildren(this);
        }
    }

    public final EventsContext events() throws RecognitionException {
        EventsContext _localctx = new EventsContext(_ctx, getState());
        enterRule(_localctx, 30, RULE_events);
        int _la;
        try {
            setState(287);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 27, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(271);
                    match(EMPTY);
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    {
                        setState(272);
                        expr();
                        setState(273);
                        match(TRIGGER);
                        setState(274);
                        match(AT);
                        setState(275);
                        match(ID);
                        setState(276);
                        match(CLPAR);
                        setState(278);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        do {
                            {
                                {
                                    setState(277);
                                    stat();
                                }
                            }
                            setState(280);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TRUE) | (1L << FALSE) | (1L << LPAR) | (1L << EMPTY) | (1L << NOW) | (1L << IF) | (1L << RAWSTRING) | (1L << INT) | (1L << REAL) | (1L << ID))) != 0));
                        setState(282);
                        match(CRPAR);
                        setState(283);
                        match(IMPL);
                        setState(284);
                        match(AT);
                        setState(285);
                        match(ID);
                    }
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class PrecContext extends ParserRuleContext {
        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public PrecContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_prec;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterPrec(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitPrec(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitPrec(this);
            else return visitor.visitChildren(this);
        }
    }

    public final PrecContext prec() throws RecognitionException {
        PrecContext _localctx = new PrecContext(_ctx, getState());
        enterRule(_localctx, 32, RULE_prec);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(289);
                expr();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class ExprContext extends ParserRuleContext {
        public TermContext left;
        public Token operator;
        public ExprContext right;

        public TermContext term() {
            return getRuleContext(TermContext.class, 0);
        }

        public List<TerminalNode> MINUS() {
            return getTokens(StipulaParser.MINUS);
        }

        public TerminalNode MINUS(int i) {
            return getToken(StipulaParser.MINUS, i);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public TerminalNode PLUS() {
            return getToken(StipulaParser.PLUS, 0);
        }

        public TerminalNode OR() {
            return getToken(StipulaParser.OR, 0);
        }

        public ExprContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_expr;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterExpr(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitExpr(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitExpr(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ExprContext expr() throws RecognitionException {
        ExprContext _localctx = new ExprContext(_ctx, getState());
        enterRule(_localctx, 34, RULE_expr);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(292);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == MINUS) {
                    {
                        setState(291);
                        match(MINUS);
                    }
                }

                setState(294);
                ((ExprContext) _localctx).left = term();
                setState(297);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << PLUS) | (1L << MINUS) | (1L << OR))) != 0)) {
                    {
                        setState(295);
                        ((ExprContext) _localctx).operator = _input.LT(1);
                        _la = _input.LA(1);
                        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << PLUS) | (1L << MINUS) | (1L << OR))) != 0))) {
                            ((ExprContext) _localctx).operator = (Token) _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                        setState(296);
                        ((ExprContext) _localctx).right = expr();
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class TermContext extends ParserRuleContext {
        public FactorContext left;
        public Token operator;
        public TermContext right;

        public FactorContext factor() {
            return getRuleContext(FactorContext.class, 0);
        }

        public TermContext term() {
            return getRuleContext(TermContext.class, 0);
        }

        public TerminalNode TIMES() {
            return getToken(StipulaParser.TIMES, 0);
        }

        public TerminalNode DIV() {
            return getToken(StipulaParser.DIV, 0);
        }

        public TerminalNode AND() {
            return getToken(StipulaParser.AND, 0);
        }

        public TermContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_term;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterTerm(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitTerm(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitTerm(this);
            else return visitor.visitChildren(this);
        }
    }

    public final TermContext term() throws RecognitionException {
        TermContext _localctx = new TermContext(_ctx, getState());
        enterRule(_localctx, 36, RULE_term);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(299);
                ((TermContext) _localctx).left = factor();
                setState(302);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TIMES) | (1L << DIV) | (1L << AND))) != 0)) {
                    {
                        setState(300);
                        ((TermContext) _localctx).operator = _input.LT(1);
                        _la = _input.LA(1);
                        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TIMES) | (1L << DIV) | (1L << AND))) != 0))) {
                            ((TermContext) _localctx).operator = (Token) _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                        setState(301);
                        ((TermContext) _localctx).right = term();
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class FactorContext extends ParserRuleContext {
        public ValueContext left;
        public Token operator;
        public ValueContext right;

        public List<ValueContext> value() {
            return getRuleContexts(ValueContext.class);
        }

        public ValueContext value(int i) {
            return getRuleContext(ValueContext.class, i);
        }

        public TerminalNode EQ() {
            return getToken(StipulaParser.EQ, 0);
        }

        public TerminalNode LE() {
            return getToken(StipulaParser.LE, 0);
        }

        public TerminalNode GE() {
            return getToken(StipulaParser.GE, 0);
        }

        public TerminalNode LEQ() {
            return getToken(StipulaParser.LEQ, 0);
        }

        public TerminalNode GEQ() {
            return getToken(StipulaParser.GEQ, 0);
        }

        public TerminalNode NEQ() {
            return getToken(StipulaParser.NEQ, 0);
        }

        public FactorContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_factor;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterFactor(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitFactor(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitFactor(this);
            else return visitor.visitChildren(this);
        }
    }

    public final FactorContext factor() throws RecognitionException {
        FactorContext _localctx = new FactorContext(_ctx, getState());
        enterRule(_localctx, 38, RULE_factor);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(304);
                ((FactorContext) _localctx).left = value();
                setState(307);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQ) | (1L << NEQ) | (1L << LEQ) | (1L << GEQ) | (1L << LE) | (1L << GE))) != 0)) {
                    {
                        setState(305);
                        ((FactorContext) _localctx).operator = _input.LT(1);
                        _la = _input.LA(1);
                        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQ) | (1L << NEQ) | (1L << LEQ) | (1L << GEQ) | (1L << LE) | (1L << GE))) != 0))) {
                            ((FactorContext) _localctx).operator = (Token) _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                        setState(306);
                        ((FactorContext) _localctx).right = value();
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class ValueContext extends ParserRuleContext {
        public NumberContext number() {
            return getRuleContext(NumberContext.class, 0);
        }

        public TerminalNode ID() {
            return getToken(StipulaParser.ID, 0);
        }

        public TerminalNode NOW() {
            return getToken(StipulaParser.NOW, 0);
        }

        public TerminalNode LPAR() {
            return getToken(StipulaParser.LPAR, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public TerminalNode RPAR() {
            return getToken(StipulaParser.RPAR, 0);
        }

        public TerminalNode RAWSTRING() {
            return getToken(StipulaParser.RAWSTRING, 0);
        }

        public TerminalNode EMPTY() {
            return getToken(StipulaParser.EMPTY, 0);
        }

        public TerminalNode TRUE() {
            return getToken(StipulaParser.TRUE, 0);
        }

        public TerminalNode FALSE() {
            return getToken(StipulaParser.FALSE, 0);
        }

        public ValueContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_value;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterValue(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitValue(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitValue(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ValueContext value() throws RecognitionException {
        ValueContext _localctx = new ValueContext(_ctx, getState());
        enterRule(_localctx, 40, RULE_value);
        int _la;
        try {
            setState(319);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case INT:
                case REAL:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(309);
                    number();
                }
                break;
                case ID:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(310);
                    match(ID);
                }
                break;
                case NOW:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(311);
                    match(NOW);
                }
                break;
                case LPAR:
                    enterOuterAlt(_localctx, 4);
                {
                    setState(312);
                    match(LPAR);
                    setState(313);
                    expr();
                    setState(314);
                    match(RPAR);
                }
                break;
                case RAWSTRING:
                    enterOuterAlt(_localctx, 5);
                {
                    setState(316);
                    match(RAWSTRING);
                }
                break;
                case EMPTY:
                    enterOuterAlt(_localctx, 6);
                {
                    setState(317);
                    match(EMPTY);
                }
                break;
                case TRUE:
                case FALSE:
                    enterOuterAlt(_localctx, 7);
                {
                    setState(318);
                    _la = _input.LA(1);
                    if (!(_la == TRUE || _la == FALSE)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                }
                break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class RealContext extends ParserRuleContext {
        public List<NumberContext> number() {
            return getRuleContexts(NumberContext.class);
        }

        public NumberContext number(int i) {
            return getRuleContext(NumberContext.class, i);
        }

        public TerminalNode DOT() {
            return getToken(StipulaParser.DOT, 0);
        }

        public RealContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_real;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterReal(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitReal(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitReal(this);
            else return visitor.visitChildren(this);
        }
    }

    public final RealContext real() throws RecognitionException {
        RealContext _localctx = new RealContext(_ctx, getState());
        enterRule(_localctx, 42, RULE_real);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(321);
                number();
                setState(322);
                match(DOT);
                setState(323);
                number();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class NumberContext extends ParserRuleContext {
        public TerminalNode INT() {
            return getToken(StipulaParser.INT, 0);
        }

        public TerminalNode REAL() {
            return getToken(StipulaParser.REAL, 0);
        }

        public NumberContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_number;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).enterNumber(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof StipulaListener) ((StipulaListener) listener).exitNumber(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof StipulaVisitor) return ((StipulaVisitor<? extends T>) visitor).visitNumber(this);
            else return visitor.visitChildren(this);
        }
    }

    public final NumberContext number() throws RecognitionException {
        NumberContext _localctx = new NumberContext(_ctx, getState());
        enterRule(_localctx, 44, RULE_number);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(325);
                _la = _input.LA(1);
                if (!(_la == INT || _la == REAL)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static final String _serializedATN =
            "\u0004\u00017\u0148\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002" +
                    "\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002" +
                    "\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002" +
                    "\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002" +
                    "\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f" +
                    "\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012" +
                    "\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015" +
                    "\u0002\u0016\u0007\u0016\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000" +
                    "\u0003\u00003\b\u0000\u0001\u0000\u0003\u00006\b\u0000\u0001\u0000\u0001" +
                    "\u0000\u0001\u0000\u0001\u0000\u0004\u0000<\b\u0000\u000b\u0000\f\u0000" +
                    "=\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001" +
                    "\u0001\u0001\u0005\u0001G\b\u0001\n\u0001\f\u0001J\t\u0001\u0001\u0001" +
                    "\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u0001Q\b\u0001" +
                    "\n\u0001\f\u0001T\t\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0004\u0001" +
                    "Y\b\u0001\u000b\u0001\f\u0001Z\u0001\u0001\u0001\u0001\u0001\u0001\u0001" +
                    "\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0005" +
                    "\u0002f\b\u0002\n\u0002\f\u0002i\t\u0002\u0001\u0003\u0001\u0003\u0001" +
                    "\u0003\u0001\u0003\u0005\u0003o\b\u0003\n\u0003\f\u0003r\t\u0003\u0001" +
                    "\u0004\u0001\u0004\u0005\u0004v\b\u0004\n\u0004\f\u0004y\t\u0004\u0001" +
                    "\u0004\u0001\u0004\u0001\u0004\u0005\u0004~\b\u0004\n\u0004\f\u0004\u0081" +
                    "\t\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001" +
                    "\u0004\u0005\u0004\u0089\b\u0004\n\u0004\f\u0004\u008c\t\u0004\u0003\u0004" +
                    "\u008e\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004" +
                    "\u0005\u0004\u0095\b\u0004\n\u0004\f\u0004\u0098\t\u0004\u0003\u0004\u009a" +
                    "\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003" +
                    "\u0004\u00a1\b\u0004\u0001\u0004\u0001\u0004\u0004\u0004\u00a5\b\u0004" +
                    "\u000b\u0004\f\u0004\u00a6\u0001\u0004\u0001\u0004\u0004\u0004\u00ab\b" +
                    "\u0004\u000b\u0004\f\u0004\u00ac\u0001\u0004\u0001\u0004\u0001\u0004\u0001" +
                    "\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u00b7" +
                    "\b\u0005\n\u0005\f\u0005\u00ba\t\u0005\u0001\u0005\u0001\u0005\u0001\u0005" +
                    "\u0001\u0005\u0005\u0005\u00c0\b\u0005\n\u0005\f\u0005\u00c3\t\u0005\u0001" +
                    "\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b" +
                    "\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\f\u0001" +
                    "\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003" +
                    "\r\u00dc\b\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u00e3\b\r" +
                    "\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e" +
                    "\u0001\u000e\u0005\u000e\u00ec\b\u000e\n\u000e\f\u000e\u00ef\t\u000e\u0001" +
                    "\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0005" +
                    "\u000e\u00f7\b\u000e\n\u000e\f\u000e\u00fa\t\u000e\u0001\u000e\u0001\u000e" +
                    "\u0005\u000e\u00fe\b\u000e\n\u000e\f\u000e\u0101\t\u000e\u0001\u000e\u0001" +
                    "\u000e\u0001\u000e\u0001\u000e\u0005\u000e\u0107\b\u000e\n\u000e\f\u000e" +
                    "\u010a\t\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u010e\b\u000e\u0001" +
                    "\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001" +
                    "\u000f\u0004\u000f\u0117\b\u000f\u000b\u000f\f\u000f\u0118\u0001\u000f" +
                    "\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0120\b\u000f" +
                    "\u0001\u0010\u0001\u0010\u0001\u0011\u0003\u0011\u0125\b\u0011\u0001\u0011" +
                    "\u0001\u0011\u0001\u0011\u0003\u0011\u012a\b\u0011\u0001\u0012\u0001\u0012" +
                    "\u0001\u0012\u0003\u0012\u012f\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013" +
                    "\u0003\u0013\u0134\b\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014" +
                    "\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014" +
                    "\u0003\u0014\u0140\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015" +
                    "\u0001\u0016\u0001\u0016\u0001\u0016\u0000\u0000\u0017\u0000\u0002\u0004" +
                    "\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"" +
                    "$&(*,\u0000\b\u0001\u0000&\'\u0001\u0000),\u0002\u0000\u001f\u001f33\u0002" +
                    "\u0000\u000b\f\u001c\u001c\u0002\u0000\r\u000e\u001d\u001d\u0002\u0000" +
                    "\u0005\u0006\u0018\u001b\u0001\u0000\u0010\u0011\u0001\u000001\u0158\u0000" +
                    ".\u0001\u0000\u0000\u0000\u0002A\u0001\u0000\u0000\u0000\u0004a\u0001" +
                    "\u0000\u0000\u0000\u0006j\u0001\u0000\u0000\u0000\bw\u0001\u0000\u0000" +
                    "\u0000\n\u00b3\u0001\u0000\u0000\u0000\f\u00c4\u0001\u0000\u0000\u0000" +
                    "\u000e\u00c7\u0001\u0000\u0000\u0000\u0010\u00c9\u0001\u0000\u0000\u0000" +
                    "\u0012\u00cb\u0001\u0000\u0000\u0000\u0014\u00cd\u0001\u0000\u0000\u0000" +
                    "\u0016\u00cf\u0001\u0000\u0000\u0000\u0018\u00d1\u0001\u0000\u0000\u0000" +
                    "\u001a\u00e2\u0001\u0000\u0000\u0000\u001c\u00e4\u0001\u0000\u0000\u0000" +
                    "\u001e\u011f\u0001\u0000\u0000\u0000 \u0121\u0001\u0000\u0000\u0000\"" +
                    "\u0124\u0001\u0000\u0000\u0000$\u012b\u0001\u0000\u0000\u0000&\u0130\u0001" +
                    "\u0000\u0000\u0000(\u013f\u0001\u0000\u0000\u0000*\u0141\u0001\u0000\u0000" +
                    "\u0000,\u0145\u0001\u0000\u0000\u0000./\u0005%\u0000\u0000/0\u00053\u0000" +
                    "\u000002\u0005\u0016\u0000\u000013\u0003\u0004\u0002\u000021\u0001\u0000" +
                    "\u0000\u000023\u0001\u0000\u0000\u000035\u0001\u0000\u0000\u000046\u0003" +
                    "\u0006\u0003\u000054\u0001\u0000\u0000\u000056\u0001\u0000\u0000\u0000" +
                    "67\u0001\u0000\u0000\u000078\u0005.\u0000\u000089\u00053\u0000\u00009" +
                    ";\u0003\u0002\u0001\u0000:<\u0003\b\u0004\u0000;:\u0001\u0000\u0000\u0000" +
                    "<=\u0001\u0000\u0000\u0000=;\u0001\u0000\u0000\u0000=>\u0001\u0000\u0000" +
                    "\u0000>?\u0001\u0000\u0000\u0000?@\u0005\u0017\u0000\u0000@\u0001\u0001" +
                    "\u0000\u0000\u0000AB\u0005(\u0000\u0000BC\u0005\u0012\u0000\u0000CH\u0003" +
                    "\u0012\t\u0000DE\u0005\u0003\u0000\u0000EG\u0003\u0012\t\u0000FD\u0001" +
                    "\u0000\u0000\u0000GJ\u0001\u0000\u0000\u0000HF\u0001\u0000\u0000\u0000" +
                    "HI\u0001\u0000\u0000\u0000IK\u0001\u0000\u0000\u0000JH\u0001\u0000\u0000" +
                    "\u0000KL\u0005\u0013\u0000\u0000LM\u0005\u0012\u0000\u0000MR\u0003\u0014" +
                    "\n\u0000NO\u0005\u0003\u0000\u0000OQ\u0003\u0014\n\u0000PN\u0001\u0000" +
                    "\u0000\u0000QT\u0001\u0000\u0000\u0000RP\u0001\u0000\u0000\u0000RS\u0001" +
                    "\u0000\u0000\u0000SU\u0001\u0000\u0000\u0000TR\u0001\u0000\u0000\u0000" +
                    "UV\u0005\u0013\u0000\u0000VX\u0005\u0016\u0000\u0000WY\u0003\n\u0005\u0000" +
                    "XW\u0001\u0000\u0000\u0000YZ\u0001\u0000\u0000\u0000ZX\u0001\u0000\u0000" +
                    "\u0000Z[\u0001\u0000\u0000\u0000[\\\u0001\u0000\u0000\u0000\\]\u0005\u0017" +
                    "\u0000\u0000]^\u0005\u0007\u0000\u0000^_\u0005\u000f\u0000\u0000_`\u0003" +
                    "\u0010\b\u0000`\u0003\u0001\u0000\u0000\u0000ab\u0005&\u0000\u0000bg\u0005" +
                    "3\u0000\u0000cd\u0005\u0003\u0000\u0000df\u00053\u0000\u0000ec\u0001\u0000" +
                    "\u0000\u0000fi\u0001\u0000\u0000\u0000ge\u0001\u0000\u0000\u0000gh\u0001" +
                    "\u0000\u0000\u0000h\u0005\u0001\u0000\u0000\u0000ig\u0001\u0000\u0000" +
                    "\u0000jk\u0005\'\u0000\u0000kp\u00053\u0000\u0000lm\u0005\u0003\u0000" +
                    "\u0000mo\u00053\u0000\u0000nl\u0001\u0000\u0000\u0000or\u0001\u0000\u0000" +
                    "\u0000pn\u0001\u0000\u0000\u0000pq\u0001\u0000\u0000\u0000q\u0007\u0001" +
                    "\u0000\u0000\u0000rp\u0001\u0000\u0000\u0000st\u0005\u000f\u0000\u0000" +
                    "tv\u0003\u0010\b\u0000us\u0001\u0000\u0000\u0000vy\u0001\u0000\u0000\u0000" +
                    "wu\u0001\u0000\u0000\u0000wx\u0001\u0000\u0000\u0000xz\u0001\u0000\u0000" +
                    "\u0000yw\u0001\u0000\u0000\u0000z\u007f\u0003\u0012\t\u0000{|\u0005\u0003" +
                    "\u0000\u0000|~\u0003\u0012\t\u0000}{\u0001\u0000\u0000\u0000~\u0081\u0001" +
                    "\u0000\u0000\u0000\u007f}\u0001\u0000\u0000\u0000\u007f\u0080\u0001\u0000" +
                    "\u0000\u0000\u0080\u0082\u0001\u0000\u0000\u0000\u0081\u007f\u0001\u0000" +
                    "\u0000\u0000\u0082\u0083\u0005\u0002\u0000\u0000\u0083\u0084\u00053\u0000" +
                    "\u0000\u0084\u008d\u0005\u0012\u0000\u0000\u0085\u008a\u0003\u0014\n\u0000" +
                    "\u0086\u0087\u0005\u0003\u0000\u0000\u0087\u0089\u0003\u0014\n\u0000\u0088" +
                    "\u0086\u0001\u0000\u0000\u0000\u0089\u008c\u0001\u0000\u0000\u0000\u008a" +
                    "\u0088\u0001\u0000\u0000\u0000\u008a\u008b\u0001\u0000\u0000\u0000\u008b" +
                    "\u008e\u0001\u0000\u0000\u0000\u008c\u008a\u0001\u0000\u0000\u0000\u008d" +
                    "\u0085\u0001\u0000\u0000\u0000\u008d\u008e\u0001\u0000\u0000\u0000\u008e" +
                    "\u008f\u0001\u0000\u0000\u0000\u008f\u0090\u0005\u0013\u0000\u0000\u0090" +
                    "\u0099\u0005\u0014\u0000\u0000\u0091\u0096\u0003\u0016\u000b\u0000\u0092" +
                    "\u0093\u0005\u0003\u0000\u0000\u0093\u0095\u0003\u0016\u000b\u0000\u0094" +
                    "\u0092\u0001\u0000\u0000\u0000\u0095\u0098\u0001\u0000\u0000\u0000\u0096" +
                    "\u0094\u0001\u0000\u0000\u0000\u0096\u0097\u0001\u0000\u0000\u0000\u0097" +
                    "\u009a\u0001\u0000\u0000\u0000\u0098\u0096\u0001\u0000\u0000\u0000\u0099" +
                    "\u0091\u0001\u0000\u0000\u0000\u0099\u009a\u0001\u0000\u0000\u0000\u009a" +
                    "\u009b\u0001\u0000\u0000\u0000\u009b\u00a0\u0005\u0015\u0000\u0000\u009c" +
                    "\u009d\u0005\u0012\u0000\u0000\u009d\u009e\u0003 \u0010\u0000\u009e\u009f" +
                    "\u0005\u0013\u0000\u0000\u009f\u00a1\u0001\u0000\u0000\u0000\u00a0\u009c" +
                    "\u0001\u0000\u0000\u0000\u00a0\u00a1\u0001\u0000\u0000\u0000\u00a1\u00a2" +
                    "\u0001\u0000\u0000\u0000\u00a2\u00a4\u0005\u0016\u0000\u0000\u00a3\u00a5" +
                    "\u0003\u001a\r\u0000\u00a4\u00a3\u0001\u0000\u0000\u0000\u00a5\u00a6\u0001" +
                    "\u0000\u0000\u0000\u00a6\u00a4\u0001\u0000\u0000\u0000\u00a6\u00a7\u0001" +
                    "\u0000\u0000\u0000\u00a7\u00a8\u0001\u0000\u0000\u0000\u00a8\u00aa\u0005" +
                    "\u0001\u0000\u0000\u00a9\u00ab\u0003\u001e\u000f\u0000\u00aa\u00a9\u0001" +
                    "\u0000\u0000\u0000\u00ab\u00ac\u0001\u0000\u0000\u0000\u00ac\u00aa\u0001" +
                    "\u0000\u0000\u0000\u00ac\u00ad\u0001\u0000\u0000\u0000\u00ad\u00ae\u0001" +
                    "\u0000\u0000\u0000\u00ae\u00af\u0005\u0017\u0000\u0000\u00af\u00b0\u0005" +
                    "\u0007\u0000\u0000\u00b0\u00b1\u0005\u000f\u0000\u0000\u00b1\u00b2\u0003" +
                    "\u0010\b\u0000\u00b2\t\u0001\u0000\u0000\u0000\u00b3\u00b8\u0003\u0012" +
                    "\t\u0000\u00b4\u00b5\u0005\u0003\u0000\u0000\u00b5\u00b7\u0003\u0012\t" +
                    "\u0000\u00b6\u00b4\u0001\u0000\u0000\u0000\u00b7\u00ba\u0001\u0000\u0000" +
                    "\u0000\u00b8\u00b6\u0001\u0000\u0000\u0000\u00b8\u00b9\u0001\u0000\u0000" +
                    "\u0000\u00b9\u00bb\u0001\u0000\u0000\u0000\u00ba\u00b8\u0001\u0000\u0000" +
                    "\u0000\u00bb\u00bc\u0005\u0002\u0000\u0000\u00bc\u00c1\u0003\u0014\n\u0000" +
                    "\u00bd\u00be\u0005\u0003\u0000\u0000\u00be\u00c0\u0003\u0014\n\u0000\u00bf" +
                    "\u00bd\u0001\u0000\u0000\u0000\u00c0\u00c3\u0001\u0000\u0000\u0000\u00c1" +
                    "\u00bf\u0001\u0000\u0000\u0000\u00c1\u00c2\u0001\u0000\u0000\u0000\u00c2" +
                    "\u000b\u0001\u0000\u0000\u0000\u00c3\u00c1\u0001\u0000\u0000\u0000\u00c4" +
                    "\u00c5\u0007\u0000\u0000\u0000\u00c5\u00c6\u00053\u0000\u0000\u00c6\r" +
                    "\u0001\u0000\u0000\u0000\u00c7\u00c8\u0007\u0001\u0000\u0000\u00c8\u000f" +
                    "\u0001\u0000\u0000\u0000\u00c9\u00ca\u00053\u0000\u0000\u00ca\u0011\u0001" +
                    "\u0000\u0000\u0000\u00cb\u00cc\u00053\u0000\u0000\u00cc\u0013\u0001\u0000" +
                    "\u0000\u0000\u00cd\u00ce\u00053\u0000\u0000\u00ce\u0015\u0001\u0000\u0000" +
                    "\u0000\u00cf\u00d0\u00053\u0000\u0000\u00d0\u0017\u0001\u0000\u0000\u0000" +
                    "\u00d1\u00d2\u0003\u0014\n\u0000\u00d2\u00d3\u0005\b\u0000\u0000\u00d3" +
                    "\u00d4\u0003\"\u0011\u0000\u00d4\u0019\u0001\u0000\u0000\u0000\u00d5\u00e3" +
                    "\u0005\u001f\u0000\u0000\u00d6\u00d7\u0003(\u0014\u0000\u00d7\u00d8\u0005" +
                    "\t\u0000\u0000\u00d8\u00db\u00053\u0000\u0000\u00d9\u00da\u0005\u0003" +
                    "\u0000\u0000\u00da\u00dc\u00053\u0000\u0000\u00db\u00d9\u0001\u0000\u0000" +
                    "\u0000\u00db\u00dc\u0001\u0000\u0000\u0000\u00dc\u00e3\u0001\u0000\u0000" +
                    "\u0000\u00dd\u00de\u0003(\u0014\u0000\u00de\u00df\u0005\n\u0000\u0000" +
                    "\u00df\u00e0\u0007\u0002\u0000\u0000\u00e0\u00e3\u0001\u0000\u0000\u0000" +
                    "\u00e1\u00e3\u0003\u001c\u000e\u0000\u00e2\u00d5\u0001\u0000\u0000\u0000" +
                    "\u00e2\u00d6\u0001\u0000\u0000\u0000\u00e2\u00dd\u0001\u0000\u0000\u0000" +
                    "\u00e2\u00e1\u0001\u0000\u0000\u0000\u00e3\u001b\u0001\u0000\u0000\u0000" +
                    "\u00e4\u00e5\u0005\"\u0000\u0000\u00e5\u00e6\u0005\u0012\u0000\u0000\u00e6" +
                    "\u00e7\u0003\"\u0011\u0000\u00e7\u00e8\u0005\u0013\u0000\u0000\u00e8\u00e9" +
                    "\u0005\u0016\u0000\u0000\u00e9\u00ed\u0003\u001a\r\u0000\u00ea\u00ec\u0003" +
                    "\u001a\r\u0000\u00eb\u00ea\u0001\u0000\u0000\u0000\u00ec\u00ef\u0001\u0000" +
                    "\u0000\u0000\u00ed\u00eb\u0001\u0000\u0000\u0000\u00ed\u00ee\u0001\u0000" +
                    "\u0000\u0000\u00ee\u00f0\u0001\u0000\u0000\u0000\u00ef\u00ed\u0001\u0000" +
                    "\u0000\u0000\u00f0\u00ff\u0005\u0017\u0000\u0000\u00f1\u00f2\u0005#\u0000" +
                    "\u0000\u00f2\u00f3\u0003\"\u0011\u0000\u00f3\u00f4\u0005\u0016\u0000\u0000" +
                    "\u00f4\u00f8\u0003\u001a\r\u0000\u00f5\u00f7\u0003\u001a\r\u0000\u00f6" +
                    "\u00f5\u0001\u0000\u0000\u0000\u00f7\u00fa\u0001\u0000\u0000\u0000\u00f8" +
                    "\u00f6\u0001\u0000\u0000\u0000\u00f8\u00f9\u0001\u0000\u0000\u0000\u00f9" +
                    "\u00fb\u0001\u0000\u0000\u0000\u00fa\u00f8\u0001\u0000\u0000\u0000\u00fb" +
                    "\u00fc\u0005\u0017\u0000\u0000\u00fc\u00fe\u0001\u0000\u0000\u0000\u00fd" +
                    "\u00f1\u0001\u0000\u0000\u0000\u00fe\u0101\u0001\u0000\u0000\u0000\u00ff" +
                    "\u00fd\u0001\u0000\u0000\u0000\u00ff\u0100\u0001\u0000\u0000\u0000\u0100" +
                    "\u010d\u0001\u0000\u0000\u0000\u0101\u00ff\u0001\u0000\u0000\u0000\u0102" +
                    "\u0103\u0005$\u0000\u0000\u0103\u0104\u0005\u0016\u0000\u0000\u0104\u0108" +
                    "\u0003\u001a\r\u0000\u0105\u0107\u0003\u001a\r\u0000\u0106\u0105\u0001" +
                    "\u0000\u0000\u0000\u0107\u010a\u0001\u0000\u0000\u0000\u0108\u0106\u0001" +
                    "\u0000\u0000\u0000\u0108\u0109\u0001\u0000\u0000\u0000\u0109\u010b\u0001" +
                    "\u0000\u0000\u0000\u010a\u0108\u0001\u0000\u0000\u0000\u010b\u010c\u0005" +
                    "\u0017\u0000\u0000\u010c\u010e\u0001\u0000\u0000\u0000\u010d\u0102\u0001" +
                    "\u0000\u0000\u0000\u010d\u010e\u0001\u0000\u0000\u0000\u010e\u001d\u0001" +
                    "\u0000\u0000\u0000\u010f\u0120\u0005\u001f\u0000\u0000\u0110\u0111\u0003" +
                    "\"\u0011\u0000\u0111\u0112\u0005!\u0000\u0000\u0112\u0113\u0005\u000f" +
                    "\u0000\u0000\u0113\u0114\u00053\u0000\u0000\u0114\u0116\u0005\u0016\u0000" +
                    "\u0000\u0115\u0117\u0003\u001a\r\u0000\u0116\u0115\u0001\u0000\u0000\u0000" +
                    "\u0117\u0118\u0001\u0000\u0000\u0000\u0118\u0116\u0001\u0000\u0000\u0000" +
                    "\u0118\u0119\u0001\u0000\u0000\u0000\u0119\u011a\u0001\u0000\u0000\u0000" +
                    "\u011a\u011b\u0005\u0017\u0000\u0000\u011b\u011c\u0005\u0007\u0000\u0000" +
                    "\u011c\u011d\u0005\u000f\u0000\u0000\u011d\u011e\u00053\u0000\u0000\u011e" +
                    "\u0120\u0001\u0000\u0000\u0000\u011f\u010f\u0001\u0000\u0000\u0000\u011f" +
                    "\u0110\u0001\u0000\u0000\u0000\u0120\u001f\u0001\u0000\u0000\u0000\u0121" +
                    "\u0122\u0003\"\u0011\u0000\u0122!\u0001\u0000\u0000\u0000\u0123\u0125" +
                    "\u0005\f\u0000\u0000\u0124\u0123\u0001\u0000\u0000\u0000\u0124\u0125\u0001" +
                    "\u0000\u0000\u0000\u0125\u0126\u0001\u0000\u0000\u0000\u0126\u0129\u0003" +
                    "$\u0012\u0000\u0127\u0128\u0007\u0003\u0000\u0000\u0128\u012a\u0003\"" +
                    "\u0011\u0000\u0129\u0127\u0001\u0000\u0000\u0000\u0129\u012a\u0001\u0000" +
                    "\u0000\u0000\u012a#\u0001\u0000\u0000\u0000\u012b\u012e\u0003&\u0013\u0000" +
                    "\u012c\u012d\u0007\u0004\u0000\u0000\u012d\u012f\u0003$\u0012\u0000\u012e" +
                    "\u012c\u0001\u0000\u0000\u0000\u012e\u012f\u0001\u0000\u0000\u0000\u012f" +
                    "%\u0001\u0000\u0000\u0000\u0130\u0133\u0003(\u0014\u0000\u0131\u0132\u0007" +
                    "\u0005\u0000\u0000\u0132\u0134\u0003(\u0014\u0000\u0133\u0131\u0001\u0000" +
                    "\u0000\u0000\u0133\u0134\u0001\u0000\u0000\u0000\u0134\'\u0001\u0000\u0000" +
                    "\u0000\u0135\u0140\u0003,\u0016\u0000\u0136\u0140\u00053\u0000\u0000\u0137" +
                    "\u0140\u0005 \u0000\u0000\u0138\u0139\u0005\u0012\u0000\u0000\u0139\u013a" +
                    "\u0003\"\u0011\u0000\u013a\u013b\u0005\u0013\u0000\u0000\u013b\u0140\u0001" +
                    "\u0000\u0000\u0000\u013c\u0140\u0005/\u0000\u0000\u013d\u0140\u0005\u001f" +
                    "\u0000\u0000\u013e\u0140\u0007\u0006\u0000\u0000\u013f\u0135\u0001\u0000" +
                    "\u0000\u0000\u013f\u0136\u0001\u0000\u0000\u0000\u013f\u0137\u0001\u0000" +
                    "\u0000\u0000\u013f\u0138\u0001\u0000\u0000\u0000\u013f\u013c\u0001\u0000" +
                    "\u0000\u0000\u013f\u013d\u0001\u0000\u0000\u0000\u013f\u013e\u0001\u0000" +
                    "\u0000\u0000\u0140)\u0001\u0000\u0000\u0000\u0141\u0142\u0003,\u0016\u0000" +
                    "\u0142\u0143\u0005\u0004\u0000\u0000\u0143\u0144\u0003,\u0016\u0000\u0144" +
                    "+\u0001\u0000\u0000\u0000\u0145\u0146\u0007\u0007\u0000\u0000\u0146-\u0001" +
                    "\u0000\u0000\u0000!25=HRZgpw\u007f\u008a\u008d\u0096\u0099\u00a0\u00a6" +
                    "\u00ac\u00b8\u00c1\u00db\u00e2\u00ed\u00f8\u00ff\u0108\u010d\u0118\u011f" +
                    "\u0124\u0129\u012e\u0133\u013f";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}