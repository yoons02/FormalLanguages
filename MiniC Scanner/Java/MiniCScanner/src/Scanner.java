import java.io.*;

public class Scanner {

    private boolean isEof = false;
    private char ch = ' '; 
    private BufferedReader input;
    private String line = "";
    private int lineno = 0;
    private int col = 1;
    private final String letters = "abcdefghijklmnopqrstuvwxyz"
        + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String digits = "0123456789";
    private final char eolnCh = '\n';
    private final char eofCh = '\004';
    

    public Scanner (String fileName) { // source filename
        String path = "MiniC Examples/"; // setting file path

    	System.out.println("Begin scanning... programs/" + fileName + "\n");
        try {
            input = new BufferedReader (new FileReader(path + fileName)); // accept file path
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
            System.exit(1);
        }
    }

    private char nextChar() { // Return next char
        if (ch == eofCh)
            error("Attempt to read past end of file");
        col++;
        if (col >= line.length()) {
            try {
                line = input.readLine( );
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            } // try
            if (line == null) // at end of file
                line = "" + eofCh;
            else {
                // System.out.println(lineno + ":\t" + line);
                lineno++;
                line += eolnCh;
            } // if line
            col = 0;
        } // if col
        return line.charAt(col);
    }
            

    public Token next( ) { // Return next token
        do {
            if (isLetter(ch) || ch == '_') { // ident or keyword
                String spelling = concat(letters + digits + '_');
                return Token.keyword(spelling);
            } else if (isDigit(ch)) { // int literal
                String number = concat(digits + '.');
                if(number.contains(".") ) return Token.mkDoubleLiteral(number); // 숫자 문자열에 .이 있으면 소수점으로 인식
                return Token.mkIntLiteral(number);
            } else switch (ch) {
            case ' ': case '\t': case '\r': case eolnCh:
                ch = nextChar();
                break;
            
            case '/':  // divide or divAssign or comment
                ch = nextChar();
                if (ch == '=')  { // divAssign
                	ch = nextChar();
                	return Token.divAssignTok;
                }
                
                // divide
                if (ch != '*' && ch != '/') return Token.divideTok;
                
                // multi line comment
                if (ch == '*') {
                    char ch1;
                    if ((ch1 = nextChar()) == '*'){ // documented comment
                        String comment = "";
                        do {
                            while (ch1 != '*') {
                                comment = comment + ch1;
                                ch1 = nextChar();
                            }
                            ch1 = nextChar();
                        } while (ch1 != '/');
                        System.out.printf("Documented Comments -----> " + comment);
                        ch = nextChar();
                    }
                    else // multi line comment
                    {
                        do {
                            while (ch != '*') ch = nextChar();
                            ch = nextChar();
                        } while (ch != '/');
                        ch = nextChar();
                    }
                }else if (ch == '/')  {
                    char ch1;
                    ch = nextChar();
                    if(ch == '/'){ // single line documented comment
                        String comment = "";
                        do {
                            ch1 = nextChar();
                            comment = comment + ch1;
                        } while (ch1 != eolnCh);
                        System.out.printf("Documented Comments -----> " + comment);
                    }
                    else{
                        while (ch != eolnCh) {  // single line comment
                            ch = nextChar();
                        }
                    }
                    ch = nextChar();
                }

                break;
            /*
            case '\'':  // char literal
                char ch1 = nextChar();
                nextChar(); // get '
                ch = nextChar();
                return Token.mkCharLiteral("" + ch1);
            */
            case eofCh: return Token.eofTok;

            case '+':
            	ch = nextChar();
	            if (ch == '=')  { // addAssign
	            	ch = nextChar();
	            	return Token.addAssignTok;
	            }
	            else if (ch == '+')  { // increment
	            	ch = nextChar();
	            	return Token.incrementTok;
	            }
                return Token.plusTok;

            case '-':
            	ch = nextChar();
                if (ch == '=')  { // subAssign
                	ch = nextChar();
                	return Token.subAssignTok;
                }
	            else if (ch == '-')  { // decrement
	            	ch = nextChar();
	            	return Token.decrementTok;
	            }
                return Token.minusTok;

            case '*':
            	ch = nextChar();
                if (ch == '=')  { // multAssign
                	ch = nextChar();
                	return Token.multAssignTok;
                }
                return Token.multiplyTok;

            case '%':
            	ch = nextChar();
                if (ch == '=')  { // remAssign
                	ch = nextChar();
                	return Token.remAssignTok;
                }
                return Token.reminderTok;

            case '(': ch = nextChar();
            return Token.leftParenTok;

            case ')': ch = nextChar();
            return Token.rightParenTok;

            case '{': ch = nextChar();
            return Token.leftBraceTok;

            case '}': ch = nextChar();
            return Token.rightBraceTok;

            // BracketTok 빠져있음
            case '[': ch = nextChar();
            return Token.leftBracketTok;

            case ']': ch = nextChar();
            return Token.rightBracketTok;

            // 추가 연산자
            case ':': ch = nextChar();
            return Token.bitFieldAssignTok;

            case ';': ch = nextChar();
            return Token.semicolonTok;

            case ',': ch = nextChar();
            return Token.commaTok;
                
            case '&': check('&'); return Token.andTok;
            case '|': check('|'); return Token.orTok;

            case '=':
                return chkOpt('=', Token.assignTok,
                                   Token.eqeqTok);

            case '<':
                return chkOpt('=', Token.ltTok,
                                   Token.lteqTok);
            case '>': 
                return chkOpt('=', Token.gtTok,
                                   Token.gteqTok);
            case '!':
                return chkOpt('=', Token.notTok,
                                   Token.noteqTok);
            case '\'' :
                char ch1;
                ch1 = nextChar();
                nextChar();
                ch = nextChar();
                return Token.mkCharLiteral("'" + ch1 + "'");
            case '"' :
                String str = "";
                ch = nextChar();
                while(ch != '"') {
                str = str + ch;
                ch = nextChar();
                }
                ch = nextChar();
                return Token.mkStringLiteral('\"' + str + '\"');

            default:  error("Illegal character " + ch); 
            } // switch
        } while (true);
    } // next


    private boolean isLetter(char c) {
        return (c>='a' && c<='z' || c>='A' && c<='Z');
    }
  
    private boolean isDigit(char c) {
        return (c>='0' && c<='9');
    }

    private void check(char c) {
        ch = nextChar();
        if (ch != c) 
            error("Illegal character, expecting " + c);
        ch = nextChar();
    }

    private Token chkOpt(char c, Token one, Token two) {
        ch = nextChar();
        if (ch != c)
            return one;
        ch = nextChar();
        return two;
    }

    private String concat(String set) {
        String r = "";
        do {
            r += ch;
            ch = nextChar();
        } while (set.indexOf(ch) >= 0);
        return r;
    }

    public void error (String msg) {
        System.err.print(line);
        System.err.println("Error: column " + col + " " + msg);
        System.exit(1);
    }

    public static void main(String[] args) {
        Scanner lexer = new Scanner("bubble.mc");
        Token tok = lexer.next( );
        while (tok != Token.eofTok) {
            System.out.println(tok.toString());
            tok = lexer.next( );
        } 
    } // main


}
