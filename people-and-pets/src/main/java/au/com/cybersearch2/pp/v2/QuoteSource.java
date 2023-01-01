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
package au.com.cybersearch2.pp.v2;

import java.util.Date;
import java.util.Random;

/**
 * QuoteSource
 * @author Andrew Bowley
 * 19 Nov 2014
 */
public class QuoteSource 
{
    private static Random rand = new Random(new Date().getTime());
	
	static String[] QUOTES =
	{
		"To be or not to be",
		"All the world's a stage",
		"I come to bury Caesar",
		"Beware the ides of March",
		"A rose by any other name",
		"Once more into the breach",
		"All that glisters is not gold",
		"Romeo, Romeo wherefore art thou",
		"Write once, run everywhere",
		"The lady doth protest too much, methinks",
		"If music be the food of love, play on",
		"Now is the winter of our discontent",
		"Parting is such sweet sorrow"
	};
	
	static String getQuote()
	{
		return QUOTES[rand.nextInt(QUOTES.length)];
	}
}
