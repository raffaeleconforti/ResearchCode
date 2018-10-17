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

package com.raffaeleconforti.noisefiltering.timestamp.collections;

/**
 * Created by conforti on 21/01/2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;

@GwtCompatible
final class CollectPreconditions {
    CollectPreconditions() {
    }

    static void checkEntryNotNull(Object key, Object value) {
        if(key == null) {
            throw new NullPointerException("null key in entry: null=" + value);
        } else if(value == null) {
            throw new NullPointerException("null value in entry: " + key + "=null");
        }
    }

    static int checkNonnegative(int value, String name) {
        if(value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative but was: " + value);
        } else {
            return value;
        }
    }

    static void checkRemove(boolean canRemove) {
        Preconditions.checkState(canRemove, "no calls to next() since the last call to remove()");
    }
}

