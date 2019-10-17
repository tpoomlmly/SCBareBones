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
    private int line;

    private Interpreter() {
        this.status = new HashMap<>();
        this.line = 0;
    }

    private void interpretBlock(File block) throws FileNotFoundException {
        Scanner sc = new Scanner(block);
        sc.useDelimiter(";");
        while(sc.hasNext()) {
            this.line++;
            this.interpretExpression(sc.next());
        }
    }

    private void interpretExpression(String expression) throws SyntaxError {
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
                if(statement.equals(statements.get(0))) this.clear(operand);
                // incr
                if(statement.equals(statements.get(1))) this.increment(operand);
                // decr
                if(statement.equals(statements.get(2))) this.decrement(operand);
                if(sc.hasNext()) throw new UnexpectedTokenException(this.line, operand);
            } else {
                throw new UnexpectedTokenException(this.line, statement);
            }

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

    public static void main(String[] args) throws FileNotFoundException {
        String pathToProgram;
        try {
            pathToProgram = StringEscapeUtils.escapeJava(args[0]);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("No file given to interpret");
        }
        Interpreter i = new Interpreter();
        i.interpretBlock(new File(pathToProgram));
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
