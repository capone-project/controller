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

package im.pks.sd.entities;

import android.os.Parcel;
import android.os.Parcelable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ServiceTo implements Parcelable {
    public String name;
    public String category;
    public int port;

    public ServiceTo() {
    }

    private ServiceTo(Parcel in) {
        name = in.readString();
        category = in.readString();
        port = in.readInt();
    }

    public static final Creator<ServiceTo> CREATOR = new Creator<ServiceTo>() {
        @Override
        public ServiceTo createFromParcel(Parcel in) {
            return new ServiceTo(in);
        }

        @Override
        public ServiceTo[] newArray(int size) {
            return new ServiceTo[size];
        }
    };

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(category);
        dest.writeInt(port);
    }
}
