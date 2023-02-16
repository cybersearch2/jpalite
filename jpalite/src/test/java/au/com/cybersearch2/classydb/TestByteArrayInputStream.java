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

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * TestByteArrayInputStream
 * @author Andrew Bowley
 * 01/08/2014
 */
public class TestByteArrayInputStream extends ByteArrayInputStream
{
    boolean closed;
    boolean throwExceptionOnClose;
    
    public TestByteArrayInputStream(byte[] buf)
    {
        super(buf);
    }
    
    @Override
    public void close() throws IOException
    {
        if (throwExceptionOnClose)
            throw new IOException("Connection error");
        super.close();
        closed = true;
    }
  
    public boolean isClosed()
    {
        return closed;
    }
}
