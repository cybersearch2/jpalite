/** Copyright 2022 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.classydb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.sql.SQLException;

/**
 * SqlParser
 * Parse SQL script using token parser to create a set of semi colon delimited statements
 * @author Andrew Bowley
 * 30/07/2014
 */
public class SqlParser
{
    protected int count;

    /**
     * StatementCallback
     * Interface to call back when a one-line statement has been parsed
     * @author Andrew Bowley
     * 30/07/2014
     */
    public interface StatementCallback
    {
        /**
         * Consume one SQL statement
         *@param statement Statement
         *@throws SQLException if database operation fails
         */
        void onStatement(String statement) throws SQLException;
    }
 
    /**
     * Public API call
     *@param is InputStream object
     *@param callback StatementCallback object
     *@throws IOException for input stream error
     *@throws SQLException for StatementCallback error
     */
    public void parseStream(InputStream is, StatementCallback callback) throws IOException, SQLException
    {
        Reader r = new BufferedReader(new InputStreamReader(is));
        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('a', 'z');
        st.wordChars('A', 'Z');
        st.wordChars('0', '9');
        st.wordChars(128 + 32, 255);
        st.whitespaceChars(0, 31); // ' ' is not whitespace so it will be inserted in the text
        st.quoteChar('\'');
        st.eolIsSignificant(false);
        int tok = StreamTokenizer.TT_EOF;
        StringBuffer buff = new StringBuffer();
        do 
        {
            tok = st.nextToken();
            switch (tok)
            {
            case StreamTokenizer.TT_EOF:    // End of input
            case StreamTokenizer.TT_EOL:    // End of line
            case StreamTokenizer.TT_NUMBER: // Not activated
                break;
            case StreamTokenizer.TT_WORD:   // Any printable character sequence terminated by single quote, eol or eof 
                buff.append(st.sval);
                break;
            case '\'':                      // Preserve escaped single quotes
                buff.append('\'');
                buff.append(st.sval);
                buff.append('\'');
                break;
           default:
                buff.append((char)tok);
                if (tok == ';') // Semi colon terminates SQL statement
                {
                    callback.onStatement(buff.toString());
                    ++count;
                    buff.setLength(0);
                }
            }
        } while (tok != StreamTokenizer.TT_EOF);
    }

    /**
     * Returns number of statements parsed
     *@return int
     */
    public int getCount() 
    {
        return count;
    }
}
