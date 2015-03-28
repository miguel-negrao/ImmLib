/*
		(C)opyright 2013-2015 by Miguel Negrão

    This file is part of ImmLib.

		ImmLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

		ImmLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImmLib.  If not, see <http://www.gnu.org/licenses/>.
*/

+ Spherical {

	*fromRealVector3D { |v| ^v.asSpherical }

	asRealVector3D {
		^RealVector3D[this.x, this.y, this.z]
	}

	rotate3D { |axis, angle|
		^this.class.fromRealVector3D( this.asRealVector3D.rotate3D(axis, angle) )
	}

}

+ UnitSpherical {

	*fromRealVector3D { |v| ^v.asUnitSpherical }

}

+ RealVector3D {

	asSpherical {
		^Spherical( this.norm, this.theta, this.phi )
	}

	asUnitSpherical {
		^UnitSpherical( this.theta, this.phi )
	}
}
