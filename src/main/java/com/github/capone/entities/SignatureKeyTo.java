/*
 * Copyright (C) 2016 Patrick Steinhardt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.capone.entities;

import nano.Core;
import org.abstractj.kalium.encoders.Hex;
import org.abstractj.kalium.keys.VerifyKey;

public class SignatureKeyTo {

    public final VerifyKey key;

    public SignatureKeyTo(Core.SignatureKeyMessage key) {
        this.key = new VerifyKey(key.data);
    }

    public SignatureKeyTo(String data) {
        this.key = new VerifyKey(Hex.HEX.decode(data));
    }

    public Core.SignatureKeyMessage toMessage() {
        Core.SignatureKeyMessage msg = new Core.SignatureKeyMessage();
        msg.data = key.toBytes();
        return msg;
    }

}
