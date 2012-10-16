
ParameterSurface {
	var <points;

	*new { |points|
        ^super.newCopyArgs(points)
    }

	*sphere{ |n|
		var h = { |k| (2*k-1)/n - 1 };
		var phi = h.acos;
		var theta = sqrt(n*pi)*phi;
		^super.newCopyArgs( (1..n).collect{|k| UnitSpherical( theta.(k), pi/2-phi.(k) ) } )
	}

    pointsDegrees {
        ^ClusterArg( points.collect{ |p,i|
            UnitSpherical(p.theta/(2*pi)*360, p.phi/(2*pi)*360)
        })
    }

    vbapPanners {
        ^[\vbap3D_Simple_Panner, [\angles, this.pointsDegrees, \spread, 0.0 ] ]
    }

}

SphereGrid : ParameterSurface {
	//var <points; //Array[UnitSpherical]

	*new{ |n|
		var h = { |k| (2*k-1)/n - 1 };
		var phi = h.acos;
		var theta = sqrt(n*pi)*phi;
		^super.newCopyArgs( (1..n).collect{|k| UnitSpherical( theta.(k), pi/2-phi.(k) ) } )
	}

}

ParameterFieldPlotOld1 {

	*new { |psurface, arrayES|

		var w, width = 500, height = 400, rate = 0.005, size = 0.05, points, items, func, u;

		w !? _.close;
		w = Window("SphereSphape 1", Rect(128, 64, width, height+35), false).front;
		w.addFlowLayout;


		items = psurface.points.collect{ |p|
			var xzy = p.asCartesian;

			Canvas3DItem.cube
				.transform(Canvas3D.mScale(*size.dup(3)))
				.transform(Canvas3D.mTranslate(xzy.x,xzy.y,xzy.z))

		};

		u = Canvas3D(w, Rect(0, 0, width, height), items++[Canvas3DItem.cube])
			.scale_(200)
			.perspective_(0.3)
			.distance_(2);

		u.mouseMoveAction = {|v,x,y|
			u.transforms = [
				Canvas3D.mRotateY(x / -200 % 2pi),
				Canvas3D.mRotateX(y / 200 % 2pi)
			];
			u.refresh;
		};

		func = { |arr|
			[items,arr].flopWith{ |item, v|
				item.color_(Color.green(v));
			};
			defer{ u.refresh };
		};

		arrayES.do(func);

		w.onClose_{
			arrayES.stopDoing(func)
		}
	}

}
