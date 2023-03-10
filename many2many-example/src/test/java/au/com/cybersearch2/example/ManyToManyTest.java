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
package au.com.cybersearch2.example;
/**
    Copyright (C) 2014  www.cybersearch2.com.au

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/> */

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import au.com.cybersearch2.container.JpaContainer;


/**
 * ManyToManyTest
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class ManyToManyTest
{
    @Test 
    public void test_many_to_many_jpa() throws Exception
    {
        ManyToManyMain manyToManyMain = new ManyToManyMain(true);
		Transcript transcript = new Transcript();
		File resourcePath = new File("src/main/resources");
		System.setProperty(JpaContainer.RESOURCE_PATH_NAME, resourcePath.getAbsolutePath());
		assertTrue(manyToManyMain.setUp());
        manyToManyMain.runApplication(transcript);
        if (transcript.getErrorCount() > 0) {
     		transcript.getObservations().forEach(entry -> { 
     			if (!entry.isStatus())
     				System.err.println(entry.getReport());
     		});
     		fail();
        }
    }

}

