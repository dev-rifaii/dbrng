package org.rifaii.dbrng.db;

import org.rifaii.dbrng.db.object.Column;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckParser {

    static final Pattern PATTERN = Pattern.compile("\\(\\s*[A-Za-z_]\\w*\\s*(==|!=|<=|>=|<|>)\\s*-?\\d+(?:\\.\\d+)?\\s*\\)(?:\\s*(AND|OR)\\s*\\(\\s*[A-Za-z_]\\w*\\s*(==|!=|<=|>=|<|>)\\s*-?\\d+(?:\\.\\d+)?\\s*\\))*");

    //((decimal_places >= 0) AND (decimal_places <= 6))
    //(term_months > 0)
    //(amount <> (0)::numeric)
    public static Column.Constraint.Check parseCheck(String checkClause) {
        Matcher matcher = PATTERN.matcher(checkClause);
        boolean matches = matcher.matches();
        assert matches;


        return null;
    }
}
