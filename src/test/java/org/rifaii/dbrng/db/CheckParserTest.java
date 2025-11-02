package org.rifaii.dbrng.db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckParserTest {

    //((decimal_places >= 0) AND (decimal_places <= 6))
    //(term_months > 0)
    //(amount <> (0)::numeric)
    @Test
    void parse() {
        CheckParser.parseCheck("(decimal_places >= 0) AND (decimal_places <= 6)");
    }

}