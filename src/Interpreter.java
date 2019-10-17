import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import static java.lang.System.out;

public class Interpreter {
    private static final List<String> statements = Arrays.asList(
            "clear",
            "incr",
            "decr",
            "while",
            "end"
    );
    private static final List<String> reservedWords = Arrays.asList(
            "not",
            "do"
    );

    private HashMap<String, Integer> status;
    private String code;
    private int line;

    private Interpreter(String code) throws EOFException {
        this.status = new HashMap<>();
        this.code = code;
        this.line = 1;
        this.interpretBlock(code);
    }

    private void interpretBlock(String block) throws EOFException {
        Scanner sc = new Scanner(block);
        sc.useDelimiter(";");
        while(sc.hasNext()) {
            this.interpretExpression(sc.next());
        }
        sc.close();
    }

    private void interpretExpression(String expression) throws SyntaxError, EOFException {
        out.println(this.status);
        out.print(this.line); out.print(" - "); out.println(expression);
        Scanner sc = new Scanner(expression.trim());
        sc.useDelimiter(Pattern.compile("[\\s]+")); // any multiple of any type of whitespace
        if(sc.hasNext()) {
            String statement = sc.next();
            if(statements.contains(statement)) {
                if(!sc.hasNext()) {
                    throw new SyntaxError(this.line);
                }
                String operand = sc.next();
                if(reservedWords.contains(operand) || statements.contains(operand)) throw new ReservedTokenException(this.line, operand);
                // clear
                if(statement.equals(statements.get(0))) {
                    if(sc.hasNext()) throw new UnexpectedTokenException(this.line, operand);
                    this.clear(operand);
                }
                // incr
                if(statement.equals(statements.get(1))) {
                    if(sc.hasNext()) throw new UnexpectedTokenException(this.line, operand);
                    this.increment(operand);
                }
                // decr
                if(statement.equals(statements.get(2))) {
                    if(sc.hasNext()) throw new UnexpectedTokenException(this.line, operand);
                    this.decrement(operand);
                }
                // while
                if(statement.equals(statements.get(3))) {
                    if(!status.containsKey(operand)) throw new UndefinedException(this.line, operand);
                    if(!sc.hasNext()) throw new SyntaxError(this.line);
                    String notToken = sc.next();
                    if(!notToken.equals(reservedWords.get(0))) throw new UnexpectedTokenException(this.line, notToken); // not
                    if(!sc.hasNext()) throw new SyntaxError(this.line);
                    String zeroToken = sc.next();
                    if(!zeroToken.equals(String.valueOf(0))) throw new UnexpectedTokenException(this.line, zeroToken); // 0
                    if(!sc.hasNext()) throw new SyntaxError(this.line);
                    String doToken = sc.next();
                    if(!doToken.equals(reservedWords.get(1))) throw new UnexpectedTokenException(this.line, doToken); // do
                    if(sc.hasNext()) throw new UnexpectedTokenException(this.line, operand);
                    this.whileDo(operand, this.line + 1);
                }
            } else {
                throw new UnexpectedTokenException(this.line, statement);
            }
            sc.close();
            this.line++;
        }
    }

    private void clear(String operand) {
        if(this.status.containsKey(operand)) {
            this.status.replace(operand, 0);
        } else {
            this.status.put(operand, 0);
        }
    }
    private void increment(String operand) {
        if(this.status.containsKey(operand)) {
            this.status.replace(operand, this.status.get(operand) + 1);
        } else throw new UndefinedException(this.line, operand);
    }
    private void decrement(String operand) {
        if(this.status.containsKey(operand)) {
            this.status.replace(operand, this.status.get(operand) - 1);
        } else throw new UndefinedException(this.line, operand);
    }
    private void whileDo(String operand, int line) throws EOFException {
        Scanner sc = new Scanner(this.code);
        sc.useDelimiter(";");
        for(int i = 0; i < line-1; i++) {
            sc.next();
        }
        sc.useDelimiter("\\A");
        String remaining = sc.next();
        sc.close();
        String block = remaining.split("end;")[0];
        if(block.equals(remaining)) {
            throw new EOFException("Reached end of file before while loop finished");
        }
        while(this.status.get(operand) != 0) {
            this.line = line;
            out.println("looping");
            this.interpretBlock(block);
        }
    }

    public static void main(String[] args) throws FileNotFoundException, EOFException {
        String pathToProgram;
        try {
            pathToProgram = StringEscapeUtils.escapeJava(args[0]);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("No file given to interpret");
        }
        Scanner sc = new Scanner(new File(pathToProgram));
        StringBuilder code = new StringBuilder();
        while(sc.hasNextLine()) code.append(sc.nextLine());
        Interpreter i = new Interpreter(code.toString());
        out.println("Finishing status:");
        out.print(i.status);
    }

    private static class SyntaxError extends RuntimeException {
        SyntaxError(int line) {
            super("Syntax error on line: " + line);
        }
    }
    private static class UnexpectedTokenException extends RuntimeException {
        UnexpectedTokenException(int line, String token) {
            super("Unexpected token on line " + line + ": " + token);
        }
    }
    private static class ReservedTokenException extends RuntimeException {
        ReservedTokenException(int line, String token) {
            super("Reserved token used as variable name on line " + line + ": " + token);
        }
    }
    private static class UndefinedException extends RuntimeException {
        UndefinedException(int line, String token) {
            super("Undefined variable on line " + line + ": " + token);
        }
    }
}
