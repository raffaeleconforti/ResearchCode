/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.logic.solver.elements;

/**
 * Created by conforti on 6/08/15.
 */
public class BooleanElement implements LogicElement {

    private boolean value;

    public BooleanElement(boolean value) {
        this.value = value;
    }

    @Override
    public BooleanElement clone() {
        return new BooleanElement(value);
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof BooleanElement) {
            BooleanElement booleanElement = (BooleanElement) object;
            return booleanElement.value == value;
        }
        return false;
    }

    @Override
    public boolean contains(LogicElement logicElement) {
        return equals(logicElement);
    }

    @Override
    public boolean containsInAND(LogicElement logicElement) {
        return equals(logicElement);
    }

    @Override
    public boolean containsInOR(LogicElement logicElement) {
        return equals(logicElement);
    }

    @Override
    public boolean isValue() {
        return value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value).toUpperCase();
    }

    @Override
    public LogicElement reduce() {
        return this;
    }
}
