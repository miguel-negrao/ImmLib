


SphereGrid {
	var <points; //Array[UnitSpherical]
	
	*new{ |n|
		var h = { |k| (2*k-1)/n - 1 };
		var phi = h.acos;
		var theta = sqrt(n*pi)*phi;
		^super.newCopyArgs( (1..n).collect{|k| UnitSpherical( theta.(k), phi.(k) ) } )
	}
	
	plot { |hidden3d = true, pm3d = true|
		var array = points.collect{ |u| 
			var theta = u.theta;
			var phi = u.phi;
			[ cos(theta)*sin(phi),  sin(theta)*sin(phi) , cos(theta) ].postln;
		};
		var gnuplot = GNUPlot.new;
		gnuplot.surf3(array, "Sphere Grid", hidden3d, pm3d);
		
	}
}