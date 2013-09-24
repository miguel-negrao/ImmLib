/*


x = PSurface.sphere;
x = PSurface.geodesicSphere;

x = PSurface.plane(RealVector3D[-1.0,-1.0,1.0], RealVector3D[2.0,0.0,0.0], RealVector3D[0.0,2.0,0.0] );
x.toFunc.(0.5,0.5)
x.fromFunc.(RealVector3D[-1.0,-1.0,1.0])
x.distFunc.(0.0,0.0,1.0,1.0)

x.plot
x.pointsRV3D
*/

//A parametrized surface
PSurface {
	//points [ [ Float ] ]
	// these are the u,v coordinates in the range rangeU X rangeV
	//array of floats because it's fast to prepend to the rest of arguments in function.
	var <points;
	//pointsWrapped - points in some other arbitrary class as long as the panners, etc know what to do with them
	//UnitSpherical, RealVector2D, etc
	var <pointsWrapped;
	//function to send coordinates pairs u,v into RealVector3D,
	var <toFunc;
	//function to get coordinates from RealVector3D.
	var <fromFunc;
	//geodesic distance function - distance along the shortest path between two points on surface.
	var <distFunc;
	//maximum geodesic distance
	var <maxDist;
	var <rangeU; //e.g. [0, 2*pi]
	var <rangeV; //e.g. [-pi, pi]
	//open surface: edges don't wrap around, e.g. square
	//closed surface: edges wrap around, e.g. torus, sphere
	var <isClosed;

	//the points in RealVector3D calculated from toFunc for convenience
	var <pointsRV3D;

	*new { |points, pointsWrapped, toFunc, fromFunc, distFunc, maxDist, rangeU, rangeV, isClosed|
		^super.newCopyArgs(points, pointsWrapped, toFunc, fromFunc, distFunc, maxDist,
			rangeU, rangeV, isClosed, points.collect{ |x| toFunc.(*x) })
	}

	*sphericalGeometry { |points|
		//points :  [ [0,0], ... ]
		^this.new(
			points,
			points.collect{ |xs| UnitSpherical(*xs) },
			{ |u,v| UnitSpherical(u,v).asRealVector3D },
			{ |p| p.asUnitSpherical.storeArgs },
			{ |theta1, phi1, theta2, phi2|
				acos( cos(phi1)*cos(phi2)*cos(theta1-theta2) + (sin(phi1)*sin(phi2) ) )
			},
			pi,
			[0, 2 * pi],
			[-pi, pi],
			true
		)

	}

	// PSurface.sphere(40).plot
	*sphere{ | n = 10 |
		var h = { |k| (2*k-1)/n - 1 };
		var phi = h.acos;
		var theta = sqrt(n*pi)*phi;
		var points = (1..n).collect{ |k| [ theta.(k), pi/2-phi.(k) ] };
		^this.sphericalGeometry( points )
	}

	// PSurface.geodesicSphere.plot
	// best simmetry for 42 points
	*geodesicSphere {
		var points = FP3DPoligon.sphereFaces(1).flatten.as(Set).as(Array).collect{ |xs| xs.asUnitSpherical.storeArgs };
		^this.sphericalGeometry( points )
	}

	//PSurface.plane(RealVector3D[-1.0,-1.0,1.0], RealVector3D[2.0,0.0,0.0], RealVector3D[0.0,2.0,0.0] ).plot
	*plane { |origin, dx, dy, n=6, m=6| //origin, dx, d : RealVector3D
		var xs = all {: [ [ i/(n-1), j/(m-1) ], origin + (dx * i/(n-1) ) + ( dy * j/(m-1) )],
			i <- (0..(n-1)), j <- (0..(m-1)) };
		var dxNorm = dx.norm.postln;
		var dyNorm = dy.norm.postln;
		^this.new( *(xs.flop++
			[
				{ |u,v| origin + (dx * u ) + ( dy * v ) },
				{ |p| [ dx.proj(p-origin).norm / dx.norm, dy.proj(p-origin).norm / dy.norm ] },
				{ |u1, v1, u2, v2| ( (u2 - u1).abs * dxNorm ).hypot( (v2 - v1).abs*dyNorm) },
				dxNorm.hypot(dyNorm),
				[0, 1],
				[0, 1],
				false

		]) )
	}

	//for use with VBAP panners
	pointsDegrees {
		^ClusterArg( pointsWrapped.collect{ |p,i|
			UnitSpherical(p.theta/(2*pi)*360, p.phi/(2*pi)*360)
		})
	}

	size {
		^points.size
	}

	plot {
		Plotter3D( pointsRV3D )
	}

	du {
		^rangeU.at(1)-rangeU.at(0)
	}

	dv {
		^rangeV.at(1)-rangeV.at(0)
	}

	ucenter {
		^rangeU.sum / 2
	}

	vcenter {
		^rangeV.sum / 2
	}

}