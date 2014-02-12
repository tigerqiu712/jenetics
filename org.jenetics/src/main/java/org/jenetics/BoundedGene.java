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

import static java.util.Objects.requireNonNull;
import static org.jenetics.util.object.eq;
import static org.jenetics.util.object.hashCodeOf;

import java.util.Comparator;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @version @__version__@ &mdash; <em>$Date$</em>
 * @since @__version__@
 */
public abstract class BoundedGene<A, G extends BoundedGene<A, G>>
	implements
		Gene<A, G>,
		Comparable<G>
{

	/**
	 * The minimum value of this {@code IntervalGene}.
	 */
	protected final A _min;

	/**
	 * The maximum value of this {@code IntervalGene}.
	 */
	protected final A _max;

	/**
	 * The value of this {@code IntervalGene}.
	 */
	protected final A _value;

	/**
	 * The comparator used for comparing the alleles.
	 */
	protected final Comparator<A> _comparator;

	private final boolean _valid;

	/**
	 * Create new {@code BoundedGene}.
	 *
	 * @param value The value of the gene.
	 * @param min The allowed min value of the gene.
	 * @param max The allows max value of the gene.
	 * @param comparator the comparator used for comparing the alleles.
	 * @throws NullPointerException if one of the given arguments is {@code null}.
	 */
	protected BoundedGene(
		final A value,
		final A min,
		final A max,
		final Comparator<A> comparator
	) {
		_min = requireNonNull(min, "Min value not be null.");
		_max = requireNonNull(max, "Max value must not be null.");
		_value = requireNonNull(value, "Gene value must not be null.");
		_comparator = requireNonNull(comparator, "Comparator must not be null.");

		_valid = comparator.compare(_value, min) >= 0 &&
				comparator.compare(_value, max) <= 0;
	}

	@Override
	public A getAllele() {
		return _value;
	}

	/**
	 * Return the allowed min value.
	 *
	 * @return The allowed min value.
	 */
	public A getMin() {
		return _min;
	}

	/**
	 * Return the allowed max value.
	 *
	 * @return The allowed max value.
	 */
	public A getMax() {
		return _max;
	}

	public Comparator<A> getComparator() {
		return _comparator;
	}

	@Override
	public Object copy() {
		return this;
	}

	@Override
	public boolean isValid() {
		return _valid;
	}

	@Override
	public int compareTo(final G other) {
		return _comparator.compare(_value, other._value);
	}

	public abstract G newInstance(final A value);

	@Override
	public int hashCode() {
		return hashCodeOf(getClass()).and(_value).and(_min).and(_max).value();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}

		final BoundedGene<?, ?> gene = (BoundedGene<?, ?>)obj;
		return eq(_value, gene._value) &&
			eq(_min, gene._min) &&
			eq(_max, gene._max);
	}

	@Override
	public String toString() {
		return String.format("[%s]", _value);
	}
}
