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
package org.jenetics.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import org.jenetics.internal.collection2.ArrayProxy;
import org.jenetics.internal.collection2.ObjectArray;
import org.jenetics.internal.util.IntRef;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @version 3.0
 * @since 3.0
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class SeqPerf {

	static final int SIZE = 1000;

	private final int index = ThreadLocalRandom.current().nextInt(SIZE);

	private final Integer[] array = new Integer[SIZE];
	{
		for (int i = 0; i < array.length; ++i) {
			array[i] = i;
		}
	}
	private final MSeq<Integer> seq = MSeq.ofLength(SIZE);
	{
		for (int i = 0; i < seq.length(); ++i) {
			seq.set(i, i);
		}
	}

	private final ArrayProxy<Integer> proxy = ArrayProxy.of(ObjectArray.of(SIZE));
	{
		for (int i = 0; i < seq.length(); ++i) {
			proxy.set(i, i);
		}
	}

	private final List<Integer> arrayList = new ArrayList<>(SIZE);
	{
		for (int i = 0; i < seq.length(); ++i) {
			arrayList.add(i);
		}
	}

	private Integer proxyGet(final int index) {
		return proxy.get(index);
	}

	@Benchmark
	public Object newObject() {
		return new Object();
	}

	@Benchmark
	public Object[] arrayClone() {
		return array.clone();
	}

	@Benchmark
	public Object[] arrayCopy() {
		final Object[] a = new Object[999];
		System.arraycopy(array, 1, a, 0, 999);
		return a;
	}

	@Benchmark
	public Integer getFromArray() {
		return array[index];
	}

	@Benchmark
	public Integer getFromArrayList() {
		return arrayList.get(index);
	}

	@OperationsPerInvocation(SIZE)
	@Benchmark
	public int forLoopArray() {
		final IntRef sum = new IntRef();
		for (int i = 0; i < array.length; ++i) {
			sum.value += array[i];
		}
		return sum.value;
	}

	@Benchmark
	public Integer getFromSeq() {
		return seq.get(index);
	}

	@Benchmark
	public Integer getFromProxy() {
		return proxyGet(index);
	}

	@OperationsPerInvocation(SIZE)
	@Benchmark
	public int forLoopSeq() {
		final IntRef sum = new IntRef();
		for (int i = 0; i < seq.length(); ++i) {
			sum.value += seq.get(i);
		}
		return sum.value;
	}

	@OperationsPerInvocation(SIZE)
	@Benchmark
	public int forEachLoopSeq() {
		final IntRef sum = new IntRef();
		seq.forEach(i -> sum.value += i);
		return sum.value;
	}

	public static void main(String[] args) throws RunnerException {
		final Options opt = new OptionsBuilder()
			.include(".*" + SeqPerf.class.getSimpleName() + ".*")
			.warmupIterations(3)
			.measurementIterations(5)
			.threads(1)
			.forks(1)
			.build();

		new Runner(opt).run();
	}

}
