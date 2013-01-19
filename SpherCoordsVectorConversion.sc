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