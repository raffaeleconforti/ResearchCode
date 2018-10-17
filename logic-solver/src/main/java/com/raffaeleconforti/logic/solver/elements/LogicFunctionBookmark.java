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

import com.raffaeleconforti.logic.solver.exception.LogicElementValueNotAssigned;

/**
 * Created by conforti on 6/08/15.
 */
public class LogicFunctionBookmark implements LogicElement {

    private final String name;

    public LogicFunctionBookmark(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isValue() throws LogicElementValueNotAssigned {
        throw new LogicElementValueNotAssigned();
    }

    @Override
    public LogicElement reduce() throws LogicElementValueNotAssigned {
        throw new LogicElementValueNotAssigned();
    }

    @Override
    public LogicFunctionBookmark clone() {
        return new LogicFunctionBookmark(name);
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
    public boolean equals(Object object) {
        if(object instanceof LogicFunctionBookmark) {
            LogicFunctionBookmark logicFunctionBookmark = (LogicFunctionBookmark) object;
            return logicFunctionBookmark.getName().equals(name);
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
