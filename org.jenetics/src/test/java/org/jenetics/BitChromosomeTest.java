/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
 */
package org.jenetics;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.Random;

import javolution.context.LocalContext;

import org.jscience.mathematics.number.LargeInteger;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.jenetics.util.Factory;
import org.jenetics.util.IO;
import org.jenetics.util.LCG64ShiftRandom;
import org.jenetics.util.RandomRegistry;
import org.jenetics.util.bit;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @version <em>$Date: 2014-01-12 $</em>
 */
public class BitChromosomeTest extends ChromosomeTester<BitGene> {

	private final Factory<Chromosome<BitGene>>
	_factory = new BitChromosome(500, 0.3);
	@Override protected Factory<Chromosome<BitGene>> getFactory() {
		return _factory;
	}

	@Test(invocationCount = 20, successPercentage = 90)
	public void newInstance() {
		final int size = 50_000;
		final BitChromosome base = new BitChromosome(size, 0.5);

		for (int i = 0; i < 100; ++i) {
			final BitChromosome other = base.newInstance();
			Assert.assertNotEquals(other, base);

			Assert.assertEquals(other.bitCount(), size/2.0, size/100.0);
		}
	}

	@Test
	public void seqTypes() {
		final BitChromosome c = new BitChromosome(100, 0.3);

		Assert.assertEquals(c.toSeq().getClass(), BitGeneArray.BitGeneISeq.class);
		Assert.assertEquals(c.toSeq().copy().getClass(), BitGeneArray.class);
		Assert.assertEquals(c.toSeq().copy().toISeq().getClass(), BitGeneArray.BitGeneISeq.class);
	}

	@Test
	public void invert() {
		BitChromosome c1 = new BitChromosome(100, 0.3);
		BitChromosome c2 = c1.copy();
		Assert.assertNotSame(c2, c1);
		Assert.assertEquals(c2, c1);

		BitChromosome c3 = c1.invert();
		for (int i = 0; i < c1.length(); ++i) {
			Assert.assertTrue(c1.getGene(i).getBit() != c3.getGene(i).getBit());
		}

		BitChromosome c4 = c3.invert();
		Assert.assertEquals(c4, c1);
	}

	@Test
	public void numValue() {
		BitChromosome c1 = new BitChromosome(10);

		int value = c1.intValue();
		assertEquals((short)value, c1.shortValue());
		assertEquals(value, c1.longValue());
		assertEquals((float)value, c1.floatValue());
		assertEquals((double)value, c1.doubleValue());
	}

	@Test
	public void intProbability() {
		BitChromosome c = new BitChromosome(10, 0);
		for (BitGene g : c) {
			assertFalse(g.getBit());
		}

		c = new BitChromosome(10, 1);
		for (BitGene g : c) {
			assertTrue(g.getBit());
		}
	}

	@Test
	public void bitChromosomeBitSet() {
		BitSet bits = new BitSet(10);
		for (int i = 0; i < 10; ++i) {
			bits.set(i, i % 2 == 0);
		}

		BitChromosome c = new BitChromosome(bits);
		for (int i = 0; i < bits.length(); ++i) {
			assertEquals(c.getGene(i).getBit(), i % 2 == 0);
		}
	}

	@Test
	public void toBigInteger() {
		BitChromosome c = new BitChromosome(LargeInteger.valueOf(234902));

		LargeInteger i = c.toLargeInteger();
		assertEquals(i, LargeInteger.valueOf(234902));
		assertEquals(i.intValue(), 234902);
		assertEquals(i.longValue(), c.longValue());
		assertEquals(i.intValue(), c.intValue());

		byte[] data = new byte[3];
		c.toByteArray(data);
		BitChromosome c2 = new BitChromosome(data);
		LargeInteger i2 = c2.toLargeInteger();
		assertEquals(i2, LargeInteger.valueOf(234902));
	}

	@Test
	public void toBitSet() {
		BitChromosome c1 = new BitChromosome(34);
		BitChromosome c2 = new BitChromosome(34, c1.toBitSet());

		for (int i = 0; i < c1.length(); ++i) {
			assertEquals(c1.getGene(i).getBit(), c2.getGene(i).getBit());
		}
	}

	@Test
	public void toByteArray() {
		byte[] data = new byte[16];
		for (int i = 0; i < data.length; ++i) {
			data[i] = (byte)(Math.random()*256);
		}
		BitChromosome bc = new BitChromosome(data);

		Assert.assertEquals(bc.toByteArray(), data);

	}

	@Test
	public void toCanonicalString() {
		BitChromosome c = new BitChromosome(LargeInteger.valueOf(234902));
		String value = c.toCanonicalString();
		BitChromosome sc = new BitChromosome(value);

		Assert.assertEquals(sc, c);
	}

	@Test
	public void toStringToByteArray() {
		byte[] data = new byte[10];
		for (int i = 0; i < data.length; ++i) {
			data[i] = (byte)(Math.random()*256);
		}

		final String dataString = bit.toByteString(data);
		Reporter.log(dataString);

		final byte[] sdata = bit.fromByteString(dataString);
		Assert.assertEquals(sdata, data);
	}

	@Test
	public void fromBitSet() {
		final Random random = new Random();
		final BitSet bits = new BitSet(2343);
		for (int i = 0; i < bits.size(); ++i) {
			bits.set(i, random.nextBoolean());
		}

		final BitChromosome c = new BitChromosome(bits);
		Assert.assertEquals(c.toByteArray(), bits.toByteArray());
	}

	@Test
	public void fromByteArrayBitSet() {
		final Random random = new Random();
		final byte[] bytes = new byte[234];
		random.nextBytes(bytes);

		final BitSet bits = BitSet.valueOf(bytes);
		final BitChromosome c = new BitChromosome(bits);
		Assert.assertEquals(c.toByteArray(), bytes);
		Assert.assertEquals(bits.toByteArray(), bytes);
	}

	@Test(dataProvider = "bitCountProbability")
	public void bitCount(final Double p) {
		final int size = 1_000;
		final BitChromosome base = new BitChromosome(size, p);

		for (int i = 0; i < 1_000; ++i) {
			final BitChromosome other = base.newInstance();

			int bitCount = 0;
			for (BitGene gene : other) {
				if (gene.booleanValue()) {
					++bitCount;
				}
			}

			Assert.assertEquals(other.bitCount(), bitCount);
		}
	}

	@Test(dataProvider = "bitCountProbability")
	public void bitSetBitCount(final Double p) {
		final int size = 1_000;
		final BitChromosome base = new BitChromosome(size, p);

		for (int i = 0; i < 1_000; ++i) {
			final BitChromosome other = base.newInstance();
			Assert.assertEquals(other.toBitSet().cardinality(), other.bitCount());
		}
	}

	@DataProvider(name = "bitCountProbability")
	public Object[][] getBitcountProbability() {
		return new Object[][] {
			{0.01}, {0.1}, {0.125}, {0.333}, {0.5}, {0.75}, {0.85}, {0.999}
		};
	}

	@Test
	public void objectSerializationCompatibility() throws IOException {
		final Random random = new LCG64ShiftRandom.ThreadSafe(0);
		LocalContext.enter();
		try {
			RandomRegistry.setRandom(random);
			final BitChromosome chromosome = new BitChromosome(5000, 0.5);

			final String resource = "/org/jenetics/BitChromosome.object";
			try (InputStream in = getClass().getResourceAsStream(resource)) {
				final Object object = IO.object.read(in);

				Assert.assertEquals(chromosome, object);
			}
		} finally {
			LocalContext.exit();
		}
	}

	@Test
	public void xmlSerializationCompatibility() throws IOException {
		final Random random = new LCG64ShiftRandom.ThreadSafe(0);
		LocalContext.enter();
		try {
			RandomRegistry.setRandom(random);
			final BitChromosome chromosome = new BitChromosome(5000, 0.5);

			final String resource = "/org/jenetics/BitChromosome.xml";
			try (InputStream in = getClass().getResourceAsStream(resource)) {
				final Object object = IO.xml.read(in);

				Assert.assertEquals(chromosome, object);
			}
		} finally {
			LocalContext.exit();
		}
	}

	public static void main(final String[] args) throws Exception {
		final Path basePath = Paths.get("/home/fwilhelm/Workspace/Development/Projects/Jenetics/org.jenetics/src/test/resources/org/jenetics/");
		//IO.object.write(BitGene.TRUE, basePath.resolve("BitGene_TRUE.object"));
		//IO.object.write(BitGene.FALSE, basePath.resolve("BitGene_FALSE.object"));
		//IO.xml.write(BitGene.TRUE, basePath.resolve("BitGene_TRUE.xml"));
		//IO.xml.write(BitGene.FALSE, basePath.resolve("BitGene_FALSE.xml"));

		final Random random = new LCG64ShiftRandom.ThreadSafe(0);
		LocalContext.enter();
		try {
			RandomRegistry.setRandom(random);
			final BitChromosome chromosome = new BitChromosome(4, 0.5);

			IO.jaxb.write(chromosome, System.out);
		} finally {
			LocalContext.exit();
		}

/*
		IO.jaxb.write(BitGene.TRUE, basePath.resolve("BitChromosome.jaxb.xml"));
		IO.jaxb.write(BitGene.FALSE, basePath.resolve("BitChromosome.jaxb.xml"));

		IO.jaxb.write(BitGene.FALSE, System.out);

		String resource = "/org/jenetics/BitGene_FALSE.jaxb.xml";
		try (InputStream in = BitGeneTest.class.getResourceAsStream(resource)) {
			final Object object = IO.jaxb.read(BitGene.class, in);

			Assert.assertEquals(object, BitGene.FALSE);
		}
		*/
	}

}







