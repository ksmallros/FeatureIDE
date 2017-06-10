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
package de.ovgu.featureide.fm.core.explanations;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.prop4j.Literal;
import org.prop4j.Literal.Origin;
import org.prop4j.Node;

import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelElement;

/**
 * Data class holding the explanation for a defect in a feature model.
 * Instances of this class are generated by {@link ExplanationCreator}.
 * 
 * @author Timo G&uuml;nther
 * @author Sofia Ananieva
 */
public class Explanation implements Cloneable {
	/**
	 * The atomic unit an explanation is composed of.
	 * 
	 * @author Timo G&uuml;nther
	 * @author Sofia Ananieva
	 */
	public class Reason implements Cloneable {
		/** clause containing the literal */
		private final Node clause;
		/** the literal of this reason */
		private final Literal literal;
		/** the source feature model element of the literal; stored to circumvent brittle string identifiers */
		private IFeatureModelElement sourceElement;
		
		/**
		 * Constructs a new instance of this class.
		 * @param clause clause containing the literal
		 * @param literal the literal of this reason
		 */
		public Reason(Node clause, Literal literal) {
			this.clause = clause;
			this.literal = literal;
		}
		
		/**
		 * Returns the containing explanation.
		 * @return the containing explanation
		 */
		public Explanation getExplanation() {
			return Explanation.this;
		}
		
		/**
		 * Returns the clause containing the literal.
		 * @return the clause containing the literal
		 */
		public Node getClause() {
			return clause;
		}
		
		/**
		 * Returns the literal of this reason.
		 * @return the literal of this reason
		 */
		public Literal getLiteral() {
			return literal;
		}
		
		/**
		 * Returns the source feature model element of the literal.
		 * @return the source feature model element of the literal
		 */
		public IFeatureModelElement getSourceElement() {
			return sourceElement;
		}
		
		/**
		 * Sets the stored source feature model element to denote the source of the literal.
		 */
		protected void setSourceElement() {
			final IFeatureModel fm = getDefectElement().getFeatureModel();
			if (getLiteral().getOrigin() == Origin.CONSTRAINT) {
				sourceElement = fm.getConstraints().get(getLiteral().getOriginConstraintIndex());
			} else {
				sourceElement = fm.getFeature((String) getLiteral().var);
			}
		}
		
		/**
		 * Returns the confidence of this reason.
		 * This is the likelihood with which this is causing the defect.
		 * Should be a value between 0 and 1.
		 * @return the confidence of this reason
		 */
		public float getConfidence() {
			/*
			 * TODO Provide a useful explanation count for redundant constraints.
			 * The explanation count for redundant constraints is currently useless.
			 * To avoid confusing the user, do not take it into account when giving confidence hints and default to 1.
			 */
			if (getMode() == Mode.REDUNDANT_CONSTRAINT) {
				return 1;
			}
			return (float) reasonCounts.get(this)/getExplanationCount();
		}
		
		@Override
		public Reason clone() {
			return new Reason(clause, literal);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			if (literal != null && literal.getOrigin() != Origin.CONSTRAINT)
				result = prime * result + (literal.var == null ? 0 : literal.var.hashCode());
			result = prime * result + (literal == null ? 0 : literal.origin);
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Reason other = (Reason) obj;
			if (literal == null) {
				if (other.literal != null)
					return false;
			} else if (literal.origin != other.literal.origin) {
				return false;
			} else if (literal.getOrigin() != Origin.CONSTRAINT //any literal of the same constraint denotes the same reason
					&& !literal.var.equals(other.literal.var))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "Reason["
					+ "clause=" + clause + ", "
					+ "literal=" + literal + ", "
					+ "source=" + sourceElement
					+ "]";
		}
	}
	
	/**
	 * Differentiates between different modes of explaining.
	 */
	public static enum Mode {
		REDUNDANT_CONSTRAINT,
		DEAD_FEATURE,
		FALSE_OPTIONAL_FEATURE
	}
	
	/** the reasons this explanation is composed of mapped to how often the respective reason has been generated */
	private final Map<Reason, Integer> reasonCounts = new LinkedHashMap<>();
	
	/** the explanation mode */
	private Mode mode;
	/** the defect feature model element */
	private IFeatureModelElement defectElement;
	/** true if this explanation is for an implicit constraint */
	private boolean implicit;
	/** how many explanations have been generated and rolled into one for this explanation */
	private int explanationCount = 1;
	
	/**
	 * Returns the explanation mode.
	 * @return the explanation mode
	 */
	public Mode getMode() {
		return mode;
	}
	
	/**
	 * Returns the defect element.
	 * @return the defect element
	 */
	public IFeatureModelElement getDefectElement() {
		return defectElement;
	}
	
	/**
	 * Sets the defect feature model element to a dead feature.
	 * Also sets the mode accordingly.
	 * @param defectElement dead feature
	 */
	public void setDefectDeadFeature(IFeature defectElement) {
		this.mode = Explanation.Mode.DEAD_FEATURE;
		this.defectElement = defectElement;
		setReasonSourceElements();
	}
	
	/**
	 * Sets the defect feature model element to a false-optional feature.
	 * Also sets the mode accordingly.
	 * @param defectElement false-optional feature
	 */
	public void setDefectFalseOptionalFeature(IFeature defectElement) {
		this.mode = Explanation.Mode.FALSE_OPTIONAL_FEATURE;
		this.defectElement = defectElement;
		setReasonSourceElements();
	}
	
	/**
	 * Sets the defect feature model element to a redundant feature.
	 * Also sets the mode accordingly.
	 * @param defectElement redundant constraint
	 */
	public void setDefectRedundantConstraint(IConstraint defectElement) {
		this.mode = Explanation.Mode.REDUNDANT_CONSTRAINT;
		this.defectElement = defectElement;
		setReasonSourceElements();
	}
	
	/**
	 * Sets each reason's source.
	 */
	protected void setReasonSourceElements() {
		for (final Reason reason : getReasons()) {
			reason.setSourceElement();
		}
	}
	
	/**
	 * Returns true iff this explanation is for an implicit constraint.
	 * @return true iff this explanation is for an implicit constraint
	 */
	public boolean isImplicit() {
		return implicit;
	}
	
	/**
	 * Sets whether this explanation is for an implicit constraint.
	 * @param implicit whether this explanation is for an implicit constraint
	 */
	public void setImplicit(boolean implicit) {
		this.implicit = implicit;
	}
	
	/**
	 * Returns how many explanations have been generated and rolled into one for this explanation.
	 * @return how many explanations have been generated and rolled into one for this explanation
	 */
	public int getExplanationCount() {
		return explanationCount;
	}
	
	/**
	 * Sets the explanation count.
	 * @param explanationCount explanation count
	 */
	public void setExplanationCount(int explanationCount) {
		this.explanationCount = explanationCount;
	}
	
	/**
	 * Returns the reasons this explanation is composed of.
	 * @return the reasons this explanation is composed of
	 */
	public Set<Reason> getReasons() {
		return reasonCounts.keySet();
	}
	
	/**
	 * Adds the given reason to this explanation.
	 * Also increments its occurrence count.
	 * @param reason reason to add
	 */
	public void addReason(Reason reason) {
		addReason(reason, 1);
	}
	
	/**
	 * Adds the given reason to this explanation.
	 * Increases its occurrence count by the given number.
	 * @param reason reason to add
	 * @param count how often to add the given reason
	 */
	protected void addReason(Reason reason, int count) {
		reason = new Reason(reason.getClause(), reason.getLiteral());
		final Integer reasonCount = reasonCounts.get(reason);
		reasonCounts.put(reason, (reasonCount == null ? 0 : reasonCount) + count);
	}
	
	/**
	 * Adds all given reasons to this explanation.
	 * @param reasons reasons to add
	 */
	public void addReasons(Collection<Reason> reasons) {
		for (final Reason reason : reasons) {
			addReason(reason);
		}
	}
	
	/**
	 * Adds a reason with the given literal to this explanation.
	 * @param clause clause containing the literal
	 * @param literal literal of the reason to add
	 */
	public void addReason(Node clause, Literal literal) {
		addReason(new Reason(clause, literal));
	}
	
	/**
	 * Adds the given reason to this explanation if it is not already contained.
	 * @param reason reason to add
	 */
	public void addUniqueReason(Reason reason) {
		reason = new Reason(reason.getClause(), reason.getLiteral());
		final Integer value = reasonCounts.get(reason);
		if (value == null) {
			reasonCounts.put(reason, 1);
		}
	}
	
	/**
	 * Adds all given reasons to this explanation if they are not already contained.
	 * @param reasons reasons to add
	 */
	public void addUniqueReasons(Collection<Reason> reasons) {
		for (final Reason reason : reasons) {
			addUniqueReason(reason);
		}
	}
	
	/**
	 * Adds a reason with the given literal to this explanation if it is not already contained.
	 * @param clause clause containing the literal
	 * @param literal literal of the reason to add
	 */
	public void addUniqueReason(Node clause, Literal literal) {
		addUniqueReason(new Reason(clause, literal));
	}
	
	/**
	 * Adds all the reasons from the given explanation with their correct occurrence count to this explanation.
	 * Also sums up the explanation counts.
	 * @param explanation explanation to add to this one
	 */
	public void addExplanation(Explanation explanation) {
		for (final Entry<Reason, Integer> reasonCount : explanation.reasonCounts.entrySet()) {
			addReason(reasonCount.getKey(), reasonCount.getValue());
		}
		explanationCount += explanation.explanationCount;
	}
	
	/**
	 * Returns the amount of reasons.
	 * @return the amount of reasons
	 */
	public int getReasonCount() {
		return reasonCounts.size();
	}
	
	/**
	 * Returns the reasons this explanation is composed of mapped to how often the respective reason has been generated.
	 * @return the reasons this explanation is composed of mapped to how often the respective reason has been generated
	 */
	public Map<Reason, Integer> getReasonCounts() {
		return reasonCounts;
	}
	
	/**
	 * Sets the reason and explanation counts to the ones in the given explanation.
	 * The reasons themselves are not copied, thus maintaining the logical validity of this explanation.
	 * @param explanation explanation with reason and explanation counts to copy
	 */
	public void setCounts(Explanation explanation) {
		for (final Entry<Reason, Integer> reasonCount : reasonCounts.entrySet()) {
			reasonCount.setValue(explanation.reasonCounts.get(reasonCount.getKey()));
		}
		explanationCount = explanation.explanationCount;
	}
	
	/**
	 * Returns all feature model elements affected by this explanation.
	 * An element is considered affected if it is the defect element, the source element of any reason or part of any such constraint.
	 * @return all feature model elements affected by this explanation
	 */
	public Set<IFeatureModelElement> getAffectedElements() {
		final Set<IFeatureModelElement> affectedElements = new LinkedHashSet<>();
		for (final Reason reason : getReasons()) {
			affectedElements.add(reason.getSourceElement());
		}
		affectedElements.add(getDefectElement());
		final Set<IFeatureModelElement> constraintElements = new LinkedHashSet<>();
		for (final IFeatureModelElement affectedElement : affectedElements) {
			if (!(affectedElement instanceof IConstraint)) {
				continue;
			}
			final IConstraint constraint = (IConstraint) affectedElement;
			constraintElements.addAll(constraint.getContainedFeatures());
		}
		affectedElements.addAll(constraintElements);
		return affectedElements;
	}
	
	/**
	 * Returns all features affected by this explanation.
	 * @return all features affected by this explanation
	 */
	public Set<IFeature> getAffectedFeatures() {
		final Set<IFeature> affectedFeatures = new LinkedHashSet<>();
		for (final IFeatureModelElement affectedElement : getAffectedElements()) {
			if (affectedElement instanceof IFeature) {
				affectedFeatures.add((IFeature) affectedElement);
			}
		}
		return affectedFeatures;
	}
	
	/**
	 * Returns all constraints affected by this explanation.
	 * @return all constraints affected by this explanation
	 */
	public Set<IConstraint> getAffectedConstraints() {
		final Set<IConstraint> affectedConstraints = new LinkedHashSet<>();
		for (final IFeatureModelElement affectedElement : getAffectedElements()) {
			if (affectedElement instanceof IConstraint) {
				affectedConstraints.add((IConstraint) affectedElement);
			}
		}
		return affectedConstraints;
	}
	
	@Override
	public Explanation clone() {
		final Explanation clone = new Explanation();
		clone.mode = mode;
		clone.defectElement = defectElement;
		clone.explanationCount = explanationCount;
		clone.reasonCounts.putAll(reasonCounts);
		return clone;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defectElement == null) ? 0 : defectElement.hashCode());
		result = prime * result + explanationCount;
		result = prime * result + (implicit ? 1231 : 1237);
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((reasonCounts == null) ? 0 : reasonCounts.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Explanation other = (Explanation) obj;
		if (defectElement == null) {
			if (other.defectElement != null)
				return false;
		} else if (!defectElement.equals(other.defectElement))
			return false;
		if (explanationCount != other.explanationCount)
			return false;
		if (implicit != other.implicit)
			return false;
		if (mode != other.mode)
			return false;
		if (reasonCounts == null) {
			if (other.reasonCounts != null)
				return false;
		} else if (!reasonCounts.equals(other.reasonCounts))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Explanation["
				+ "reasonCounts=" + reasonCounts + ", "
				+ "mode=" + mode + ", "
				+ "defectElement=" + defectElement + ", "
				+ "implicit=" + implicit + ", "
				+ "explanationCount=" + explanationCount
				+ "]";
	}
}