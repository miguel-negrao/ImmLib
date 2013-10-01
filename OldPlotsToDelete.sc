//unused
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

ParameterFieldPlotOld2 {
	var <items, <u, <w, <es;
	*new { |psurface| //EventSource[Array[Double]]

		var w, width = 500, height = 400, rate = 0.005, size = 0.05, points, items, u;

		w = Window("SphereSphape 1", Rect(128, 64, width, height+35), false);
		w.addFlowLayout;


		items = psurface.points.collect{ |p|
			var xzy = p.asCartesian;

			Canvas3DItem.cube
				.transform(Canvas3D.mScale(*size.dup(3)))
				.transform(Canvas3D.mTranslate(xzy.x,xzy.y,xzy.z))

		};

		u = Canvas3D(w, Rect(0, 0, width, height))
			.scale_(200)
			.perspective_(0.3)
			.distance_(2);

		u.items = items ++ [Canvas3DItem.cube];

		u.transforms = [ Canvas3D.mRotateX(pi/2) ];

		u.mouseMoveAction = {|v,x,y|
			u.transforms = [
				Canvas3D.mRotateY(x / -200 % 2pi),
				Canvas3D.mRotateX(y / 200 % 2pi)
			];
			u.refresh;
		};
		^super.newCopyArgs(items, u, w).init;
	}

	init {
		w.onClose_({ es.remove });
	}

	animate{ |arrayES|
		es = arrayES.collect{ |arr|
			IO{
				[items,arr].flopWith{ |item, v|
					item.color_(Color.green(v));
				};
				defer{ u.refresh };
			}
		};
		^es.reactimate
	}

	show {
		^IO{ w.front }
	}

	close {
		^IO{ w.isClosed.not.if({ w.close}) }
	}

	showNow {
		w.front
	}
}
