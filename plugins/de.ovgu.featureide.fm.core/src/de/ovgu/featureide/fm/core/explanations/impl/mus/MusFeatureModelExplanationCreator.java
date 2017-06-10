/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2017  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.fm.core.explanations.impl.mus;

import java.util.Set;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.explain.solvers.MusExtractor;
import org.prop4j.explain.solvers.SatSolverFactory;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.explanations.Explanation;
import de.ovgu.featureide.fm.core.explanations.FeatureModelExplanationCreator;
import de.ovgu.featureide.fm.core.explanations.impl.AbstractFeatureModelExplanationCreator;

/**
 * Abstract implementation of {@link FeatureModelExplanationCreator} using a {@link MusExtractor MUS extractor}.
 * 
 * @author Timo G&uuml;nther
 */
public abstract class MusFeatureModelExplanationCreator extends AbstractFeatureModelExplanationCreator {
	/**
	 * The oracle with the CNF as input.
	 * The oracle is created lazily when needed and reset when the CNF changes.
	 */
	private MusExtractor oracle;
	
	/**
	 * Constructs a new instance of this class.
	 */
	protected MusFeatureModelExplanationCreator() {
		super();
	}
	
	/**
	 * Constructs a new instance of this class.
	 * @param fm the feature model context
	 */
	protected MusFeatureModelExplanationCreator(IFeatureModel fm) {
		super();
	}
	
	/**
	 * Returns the oracle.
	 * Creates it first if necessary.
	 * @return the oracle; not null
	 */
	protected MusExtractor getOracle() {
		if (oracle == null) {
			setOracle(createOracle());
		}
		return oracle;
	}
	
	/**
	 * Sets the oracle.
	 * @param oracle the oracle
	 */
	protected void setOracle(MusExtractor oracle) {
		this.oracle = oracle;
	}
	
	/**
	 * Returns a new oracle with the CNF.
	 * @return a new oracle with the CNF; not null
	 */
	protected MusExtractor createOracle() {
		final MusExtractor oracle = SatSolverFactory.getDefault().getMusExtractor();
		oracle.addFormula(getCnf());
		return oracle;
	}
	
	@Override
	protected void setCnf(Node cnf) throws IllegalArgumentException {
		super.setCnf(cnf);
		setOracle(null);
	}
	
	/**
	 * Returns an explanation for the given MUS.
	 * @param mus minimal unsatisfiable subset of the CNF
	 * @return an explanation for the given MUS
	 */
	protected Explanation getExplanation(Set<Node> mus) {
		final Explanation explanation = new Explanation();
		for (final Node clause : mus) {
			for (final Literal literal : clause.getLiterals()) {
				switch (literal.getOrigin()) {
					case CHILD:
					case CONSTRAINT:
						explanation.addUniqueReason(clause, literal);
						break;
					case ROOT:
					case PARENT:
					case UNDEFINED:
					default:
						break;
				}
			}
		}
		return explanation;
	}
}