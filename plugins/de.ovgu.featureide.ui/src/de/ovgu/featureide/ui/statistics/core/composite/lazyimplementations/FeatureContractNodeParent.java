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
package de.ovgu.featureide.ui.statistics.core.composite.lazyimplementations;

import java.util.LinkedList;

import de.ovgu.featureide.core.fstmodel.FSTMethod;
import de.ovgu.featureide.ui.statistics.core.composite.lazyimplementations.genericdatatypes.AbstractSortModeNode;

/**
 * Node for contracted {@link FSTMethod}.
 *
 * @author Schleicher Miro
 */
public class FeatureContractNodeParent extends AbstractSortModeNode {

	private final FSTMethod fstMethod;

	public FeatureContractNodeParent(String description, FSTMethod method, LinkedList<FSTMethod> allContractsFeature) {
		super(description);
		fstMethod = method;
		final int numberOfContractsInFeature = countFeature(allContractsFeature);
		super.setValue(numberOfContractsInFeature);
	}

	@Override
	protected void initChildren() {
	}

	public int countFeature(LinkedList<FSTMethod> methods) {

		int c = 0;
		for (final FSTMethod tempMethod : methods) {
			if (tempMethod.getRole().getFeature().equals(fstMethod.getRole().getFeature())) {
				c++;

				addChild(new MethodSubNodeParent(tempMethod.getFullIdentifier(), tempMethod));
			}
		}
		return c;
	}

}
