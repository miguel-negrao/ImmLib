ParameterSurface {
	var <points;

	*new { |points|
        ^super.newCopyArgs(points)
    }

    // ParameterSurface.sphere(40).plot
	*sphere{ |n|
		var h = { |k| (2*k-1)/n - 1 };
		var phi = h.acos;
		var theta = sqrt(n*pi)*phi;
		^super.newCopyArgs( (1..n).collect{|k| UnitSpherical( theta.(k), pi/2-phi.(k) ) } )
	}

    // ParameterSurface.geodesicSphere.plot
    // best simmetry for 42 points
    *geodesicSphere {
        var points = FP3DPoligon.sphereFaces(1).flatten.as(Set).as(Array).collect( _.asUnitSpherical );
        ^super.newCopyArgs( points )
    }

    //ParameterSurface.plane(RealVector3D[-1.0,-1.0,1.0], RealVector3D[2.0,0.0,0.0], RealVector3D[0.0,2.0,0.0] ).plot
    *plane { |origin, dx, dy, n=6, m=6| //a,b,c : RealVector3D
        ^super.newCopyArgs( this.makePlanePoints( origin, dx, dy, n, m) )
    }

    *makePlanePoints { |origin, dx, dy, n=6, m=6|
        ^all {: origin + (dx * i/(n-1) ) + ( dy * j/(m-1) ), i <- (0..(n-1)), j <- (0..(m-1)) }
    }

    *planeProjectedIntoSphere{ |origin, dx, dy, n=6, m=6|
        var p = this.makePlanePoints( origin, dx, dy, n, m);
        var projected = p.collect{ |x| x.asUnitSpherical };
        ^super.newCopyArgs( projected )
    }

    pointsDegrees {
        ^ClusterArg( points.collect{ |p,i|
            UnitSpherical(p.theta/(2*pi)*360, p.phi/(2*pi)*360)
        })
    }

    vbapPanners {
        ^[\vbap3D_Simple_Panner, [\angles, this.pointsDegrees, \spread, 0.0 ] ]
    }

    plotIO {
        ^ParameterFieldPlot( this ).show
    }

    plot {
        ^ParameterFieldPlot( this ).showNow
    }

	size {
		^points.size
	}

}
