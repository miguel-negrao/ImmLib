/*
    ImmLib
    Copyright 2013 Miguel Negrao.

    ImmLib: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

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
	//maximum geodesic distancef
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

	*prToSphericalRange{ |theta, phi|

		^[theta.mod(2pi), (phi+pi).mod(2pi)-pi ]

	}

	// PSurface.sphere(40).plot
	*sphere{ | n = 10 |
		var h = { |k| (2*k-1)/n - 1 };
		var phi = h.acos;
		var theta = sqrt(n*pi)*phi;
		var points = (1..n).collect{ |k| this.prToSphericalRange( theta.(k), pi/2-phi.(k) ) };
		^this.sphericalGeometry( points )
	}

	// PSurface.geodesicSphere.plot
	// best simmetry for 42 points
	*geodesicSphere {
		var points = PSurface.sphereFaces(1).flatten.as(Set).as(Array).collect{ |xs|
			this.prToSphericalRange(*xs.asUnitSpherical.storeArgs) };
		^this.sphericalGeometry( points )
	}

	//PSurface.plane(RealVector3D[-1.0,-1.0,1.0], RealVector3D[2.0,0.0,0.0], RealVector3D[0.0,2.0,0.0] ).plot
	*plane { |origin, dx, dy, n=6, m=6| //origin, dx, d : RealVector3D
		var xs = all {: [ [ i/(n-1), j/(m-1) ], origin + (dx * i/(n-1) ) + ( dy * j/(m-1) )],
			i <- (0..(n-1)), j <- (0..(m-1)) };
		var dxNorm = dx.norm;
		var dyNorm = dy.norm;
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

	/*
	For use with VBAP panners

	VBAP panners use a different coordinate system from UnitSpherical.
	VBAP coordinates positve angles go right (facing to 0 degrees), or clockwise
	UnitSpherical coordinates positive angles go left (facing 0 degrees), or anti-clockwise
	if UnitSpherical(0,0) is to map to (0,0) in vbap coordinats then x=1, y=0,z=0 is front.
	Because of this the theta angle needs to be reversed


	*/
	pointsDegrees {
		^ClusterArg( pointsWrapped.collect{ |p,i|
			UnitSpherical(p.theta/(2*pi)*360.neg, p.phi/(2*pi)*360)
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

	*icosahedronFaces { |transforms, emissive, ambient=0.5, specular=0.5, diffuse=0.5, shininess = 0.5, width|
        var t = (1.0 + 5.sqrt) / 2.0;

        var points = [
            RealVector3D[-1,  t,  0],
            RealVector3D[ 1,  t,  0],
            RealVector3D[-1, t.neg,  0],
            RealVector3D[ 1, t.neg,  0],

            RealVector3D[ 0, -1,  t],
            RealVector3D[ 0,  1,  t],
            RealVector3D[ 0, -1, t.neg],
            RealVector3D[ 0,  1, t.neg],

            RealVector3D[ t,  0, -1],
            RealVector3D[ t,  0,  1],
            RealVector3D[t.neg,  0, -1],
            RealVector3D[t.neg,  0,  1]
        ];

        ^[

        // 5 faces around point 0
        [0, 11, 5],
        [0, 5, 1],
        [0, 1, 7],
        [0, 7, 10],
        [0, 10, 11],

        // 5 adjacent faces
        [1, 5, 9],
        [5, 11, 4],
        [11, 10, 2],
        [10, 7, 6],
        [7, 1, 8],

        // 5 faces around point 3
        [3, 9, 4],
        [3, 4, 2],
        [3, 2, 6],
        [3, 6, 8],
        [3, 8, 9],

        // 5 adjacent faces
        [4, 9, 5],
        [2, 4, 11],
        [6, 2, 10],
        [8, 6, 7],
        [9, 8, 1]
        ].collect{ |arr|
            arr.collect( points.at(_) )
        }
    }

    *sphereFaces { |n|
        var triangles = this.icosahedronFaces().collect{ |tri| tri.collect( _.normalize ) };
        var splitTriangles = { |triangles, n|
            if( n == 0) {
                triangles
            } {
                var newTriangles = triangles.collect{ |tri|
                        var a,b,c, ab, bc, ac;
                        var f = { |x,y| (x + ((y-x)/2)).normalize };
                        #a,b,c = tri;
                        ab = f.(a,b);
                        bc = f.(b,c);
                        ac = f.(a,c);
                        [
                            [a,ab,ac],
                            [b,ab,bc],
                            [c,bc,ac],
                            [ab,bc,ac]
                        ]
                }.flatten;
                splitTriangles.(newTriangles, n-1)
            }
        };
        ^splitTriangles.(triangles,n)
    }

    *cubeFaces {

        [
        // Front face
        [[-1.0, -1.0,  1.0],
        [1.0, -1.0,  1.0],
        [1.0,  1.0,  1.0],
        [-1.0,  1.0,  1.0]],

        // Back face
        [[-1.0, -1.0, -1.0],
        [-1.0,  1.0, -1.0],
        [1.0,  1.0, -1.0],
        [1.0, -1.0, -1.0]],

        // Top face
        [[-1.0,  1.0, -1.0],
        [-1.0,  1.0,  1.0],
        [1.0,  1.0,  1.0],
        [1.0,  1.0, -1.0]],

        // Bottom face
        [[-1.0, -1.0, -1.0],
        [1.0, -1.0, -1.0],
        [1.0, -1.0,  1.0],
        [-1.0, -1.0,  1.0]],

        // Right face
        [[1.0, -1.0, -1.0],
        [1.0,  1.0, -1.0],
        [1.0,  1.0,  1.0],
        [1.0, -1.0,  1.0]],

        // Left face
        [[-1.0, -1.0, -1.0],
        [-1.0, -1.0,  1.0],
        [-1.0,  1.0,  1.0],
        [-1.0,  1.0, -1.0]]];

    }

}