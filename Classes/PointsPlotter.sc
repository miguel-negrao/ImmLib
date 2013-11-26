Plotter3D : HaskellPFPlot {
    var <points, <colors;

    *new { |points, colors, closeOnCmdPeriod = false|
		var points2 = points.collect{ |x| if(x.isKindOf(Spherical)) {x.asCartesian.asArray}{x.as(Array)} }.flat.postln;
		^super.basicNew( NetAddr("localhost", currentPort), "Plotter3D" ).init( points2, colors, closeOnCmdPeriod )
    }

    init { |aPoints, aColors, closeOnCmdPeriod|
        points = aPoints;
		colors = aColors !? _.flat ?? { points.collect{rrand(0.5,1.0)} };
		this.startRenderer(closeOnCmdPeriod)
    }

    startRenderer { |closeOnCmdPeriod = true|
        fork{
            super.startRenderer(closeOnCmdPeriod);

            0.5.wait;

            rendererAddr.sendMsg(* (["/cubes"]++points) );

			0.1.wait;

			rendererAddr.sendMsg(* (["/colors"]++colors ) )
			//this.rendererAddr.sendBundle(0,["/cubes"]++points, ["/colors"]++colors)
        }
    }

    startRendererIO {
        ^IO{ this.startRenderer }
    }

	points_{ |xs|
		points = xs;
		rendererAddr.sendMsg(* (["/cubes"]++points) );
	}

	colors_{ |xs|
		colors = xs !? _.flat;
		rendererAddr.sendMsg(* (["/colors"]++(colors ?? {points.collect{rrand(0.5,1.0)} }) ) )
	}

	pointsColors_{ |xs, ys|
		points = xs;
		colors = ys !? _.flat;
		fork{
            rendererAddr.sendMsg(* (["/cubes"]++points) );

			0.1.wait;

			rendererAddr.sendMsg(* (["/colors"]++(colors ?? {points.collect{rrand(0.5,1.0)} }) ) )
        }
	}

}